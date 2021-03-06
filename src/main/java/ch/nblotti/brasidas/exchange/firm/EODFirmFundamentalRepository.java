package ch.nblotti.brasidas.exchange.firm;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Slf4j
public class EODFirmFundamentalRepository {



  @Value("${index.firm.api.url}")
  public String firmUrl;

  private static final int MAX_RETRY = 100;

  public String typeStr = "$.General.Type";

  public String isDelistedStr = "$.General.IsDelisted";

  @Autowired
  protected RestTemplate externalShortRestTemplate;

  @Value("${spring.application.eod.api.key}")
  protected String apiKey;


  @Autowired
  Cache cacheOne;



  public Optional<String> getTypeByDateAndFirm(LocalDate runDate, String exchange, String symbol) {


    String finalUrl = String.format(firmUrl, symbol, exchange, apiKey);
    final ResponseEntity<String> response = getStringResponseEntity(finalUrl);
    try {
      DocumentContext content = JsonPath.parse(response.getBody());

      String type = content.read(typeStr, String.class);


      return Optional.of(type);
    } catch (Exception ex) {
      log.warn(String.format("Error, mapping valuation for symbol %s \r\n%s", symbol, ex.getMessage()));
      return Optional.empty();
    }


  }



  protected ResponseEntity<String> getStringResponseEntity(String finalUrl) {
    if (cacheOne.get(finalUrl.hashCode()) == null) {
      int networkErrorHandling = 0;
      while (networkErrorHandling< MAX_RETRY) {
        try {
          ResponseEntity<String> entity = externalShortRestTemplate.getForEntity(finalUrl, String.class);
          cacheOne.put(finalUrl.hashCode(), entity);
          return entity;
        } catch (Exception ex) {
          networkErrorHandling++;
          log.warn( String.format("Error, retrying\r\n%s", ex.getMessage()));
        }
      }
      throw new IllegalStateException();
    }

    return (ResponseEntity<String>) cacheOne.get(finalUrl.hashCode()).get();
  }


}
