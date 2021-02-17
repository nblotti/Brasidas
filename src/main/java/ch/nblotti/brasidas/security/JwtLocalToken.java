package ch.nblotti.brasidas.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
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

@Component
public class JwtLocalToken {


  private static final String ALGORITHM = "RSA";


  private RestTemplate restTemplate;

  @Value("${zeus.sharedkey.url}")
  private String sharedkeyUrl;


  @Value("${token.technical.expiration}")
  private long technicalExpiration;


  @Value("${zeus.login.url}")
  private String loginUrl;

  @Autowired
  private Key key;


  private String jwtToken;


  public String createJWT() throws Exception {


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


    ResponseEntity<JSONObject> response = restTemplate.postForEntity(loginUrl, jsonObject, JSONObject.class);
    String responseStr = String.format("Bearer %s", response.getBody().get("response").toString());
    return responseStr;

  }


  private String getGeneratedString() {


    ResponseEntity<String> response = restTemplate.getForEntity(sharedkeyUrl, String.class);

    return response.getBody();
  }

  public void setRestTemplate(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  public String getJWT() throws Exception {

    if (this.jwtToken == null || this.jwtToken.isEmpty())
      this.jwtToken = createJWT();

    return jwtToken;
  }

  public String getNewJWT() throws Exception {

    this.jwtToken = createJWT();
    return jwtToken;
  }

}
