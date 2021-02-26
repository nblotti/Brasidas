package ch.nblotti.brasidas.exchange.firminfos;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
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
class EODFirmInfoRepository {


  private static final Logger logger = Logger.getLogger("EODFirmInfoRepository");

  private static final int MAX_RETRY = 100;


  @Value("${index.firm.api.url}")
  public String firmUrl;


  public String infoStr = "$.General";


  @Autowired
  protected RestTemplate externalRestTemplate;


  @Value("${spring.application.eod.api.key}")
  protected String apiKey;


  @Autowired
  Cache cacheOne;


  public Optional<EODFirmInfosDTO> getInfosByDateAndExchangeAndFirm(LocalDate runDate, String exchange, String symbol) {


    String finalUrl = String.format(firmUrl, symbol, exchange, apiKey);
    final ResponseEntity<String> response = getStringResponseEntity(finalUrl);
    try {
      DocumentContext jsonContext = JsonPath.parse(response.getBody());
      jsonContext.delete("$.General.Officers");
      jsonContext.delete("$.General.AddressData");
      jsonContext.delete("$.General.Listings");
      jsonContext.delete("$.General.isDelisted");

      boolean isDelisted = jsonContext.read("$.General.IsDelisted", boolean.class);

      if (isDelisted)
        return Optional.empty();

      EODFirmInfosDTO eODFirmHighlightsDTO = jsonContext.read(infoStr, EODFirmInfosDTO.class);
      eODFirmHighlightsDTO.setExchange(exchange);
      return Optional.of(eODFirmHighlightsDTO);

    } catch (Exception ex) {
      logger.log(Level.INFO, String.format("Error, mapping info for symbol %s \r\n%s", symbol, ex.getMessage()));
      return Optional.empty();
    }
  }


  protected ResponseEntity<String> getStringResponseEntity(String finalUrl) {
    if (cacheOne.get(finalUrl.hashCode()) == null) {
      int networkErrorHandling = 0;
      while (networkErrorHandling < MAX_RETRY) {
        try {
          ResponseEntity<String> entity = externalRestTemplate.getForEntity(finalUrl, String.class);
          cacheOne.put(finalUrl.hashCode(), entity);
          return entity;
        } catch (Exception ex) {
          networkErrorHandling++;
          logger.log(Level.INFO, String.format("Error, retrying\r\n%s", ex.getMessage()));
        }
      }
      throw new IllegalStateException();
    }

    return (ResponseEntity<String>) cacheOne.get(finalUrl.hashCode()).get();
  }


}
