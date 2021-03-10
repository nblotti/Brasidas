package ch.nblotti.brasidas.exchange.firmhighlights;

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
class EODFirmHighlightsRepository {



  @Autowired
  Cache cacheOne;


  @Value("${index.firm.api.url}")
  public String firmUrl;


  public String highlightStr = "$.Highlights";

  private static final int MAX_RETRY = 100;

  @Autowired
  protected RestTemplate externalRestTemplate;


  @Value("${spring.application.eod.api.key}")
  protected String apiKey;




  public Optional<EODFirmHighlightsDTO> getHighlightsByDateAndFirm(LocalDate runDate, String exchange, String symbol) {


    String finalUrl = String.format(firmUrl, symbol, exchange, apiKey);
    final ResponseEntity<String> response = getStringResponseEntity(finalUrl);
    try {
      DocumentContext jsonContext = JsonPath.parse(response.getBody());

      EODFirmHighlightsDTO EODFirmHighlightsDTO = jsonContext.read(highlightStr, EODFirmHighlightsDTO.class);



      return Optional.of(EODFirmHighlightsDTO);
    } catch (Exception ex) {
      log.error(String.format("Error, mapping highlight for symbol %s \r\n%s", symbol, ex.getMessage()));
      return Optional.empty();
    }
  }




  protected ResponseEntity<String> getStringResponseEntity(String finalUrl) {
    if (cacheOne.get(finalUrl.hashCode()) == null) {
      int networkErrorHandling = 0;
      while (networkErrorHandling< MAX_RETRY) {
        try {
          ResponseEntity<String> entity = externalRestTemplate.getForEntity(finalUrl, String.class);
          cacheOne.put(finalUrl.hashCode(), entity);
          return entity;
        } catch (Exception ex) {
          networkErrorHandling++;
          log.error(String.format("Error, retrying\r\n%s", ex.getMessage()));
        }
      }
      throw new IllegalStateException();
    }

    return (ResponseEntity<String>) cacheOne.get(finalUrl.hashCode()).get();
  }


}
