package ch.nblotti.brasidas.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.logging.Logger;

@Component
@Slf4j
public class JwtLocalToken {


  private static final String ALGORITHM = "RSA";
  public static final int RETRY_SLEEP_TIME_IN_MS = 5000;


  @Value("${zeus.sharedkey.url}")
  private String sharedkeyUrl;


  @Value("${token.technical.expiration}")
  private long technicalExpiration;


  @Value("${zeus.login.url}")
  private String loginUrl;

  @Autowired
  private Key key;

  @Autowired
  private RestTemplate externalShortRestTemplate;


  private String jwtToken;


  public String createJWT() {

    String responseStr = new String();

    while (responseStr.isEmpty()) {
      log.info(String.format("Starting technical login"));
      try {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("applicationId", "local_brasidas");


        //We will sign our JWT with our ApiKey secret
        JwtBuilder builder = Jwts.builder()
          .setIssuedAt(now)
          .signWith(SignatureAlgorithm.RS512, key);
        //Let's set the JWT Claims
        Claims claimsIdentity = new DefaultClaims();


        claimsIdentity.put("SHARED_KEY", getGeneratedString());

        LocalDateTime ldt = LocalDateTime.now().plusSeconds(technicalExpiration);
        Date exp = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        claimsIdentity.setExpiration(exp);
        builder.setClaims(claimsIdentity);
        String token = builder.compact();

        jsonObject.put("applicationId", "local_brasidas");

        jsonObject.put("idTokenString", token);

        ResponseEntity<JSONObject> response = externalShortRestTemplate.postForEntity(loginUrl, jsonObject, JSONObject.class);
        responseStr = String.format("Bearer %s", response.getBody().get("response").toString());
      } catch (Exception ex) {
        try {
          log.error(String.format("Error creating JWT (technical login), retrying in 5s"));
          log.error(ex.getMessage());
          Thread.sleep(RETRY_SLEEP_TIME_IN_MS);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

      }
    }
    return responseStr;


  }


  private String getGeneratedString() {

    ResponseEntity<String> response = externalShortRestTemplate.getForEntity(sharedkeyUrl, String.class);

    return response.getBody();
  }


  public String getJWT() {

    if (this.jwtToken == null || this.jwtToken.isEmpty())
      this.jwtToken = createJWT();

    return jwtToken;
  }

  public String getNewJWT() {

    this.jwtToken = createJWT();
    return jwtToken;
  }

}
