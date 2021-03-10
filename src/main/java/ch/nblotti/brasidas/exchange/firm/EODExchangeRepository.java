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
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Slf4j
class EODExchangeRepository {



  public static final String EXCHANGE = "exchange";
  public static final String EXCHANGE_JSON = "exchangelJson";
  public static final String FIRMS_FINANCIALS = "firms";
  public static final String FIRMS_FINANCIALS_JSON = "firmsFinancialJson";


  @Value("${firm.marketCap.bulk.url}")
  private String marketCap;


  @Value("${firm.quote.url}")
  private String firmQuoteUrl;

  public String sharesHistoryStr = "$.[*]";


  @Autowired
  protected DateTimeFormatter format1;

  @Autowired
  protected RestTemplate externalRestTemplate;

  @Value("${spring.application.eod.api.key}")
  protected String apiKey;


  @Autowired
  Cache cacheOne;


  public List<EODFirmQuoteDTO> getExchangeQuoteByDate(LocalDate startDate, LocalDate endDate, String code, String exchange) {

    String finalUrl = String.format(firmQuoteUrl, code, exchange, startDate.format(format1), endDate.format(format1),apiKey);

    DocumentContext jsonContext = getDocumentContext(finalUrl);
    List<EODFirmQuoteDTO> firms = Arrays.asList(jsonContext.read(sharesHistoryStr, EODFirmQuoteDTO[].class));

    return firms;
  }


  public List<EODExchangeDTO> getExchangeDataByDate(LocalDate runDate, String exchange) {

    String finalUrl = String.format(marketCap, exchange, apiKey, runDate.format(format1));

    DocumentContext jsonContext = getDocumentContext(finalUrl);
    List<EODExchangeDTO> firms = Arrays.asList(jsonContext.read(sharesHistoryStr, EODExchangeDTO[].class));

    return firms;
  }

  private ResponseEntity<String> getStringResponseEntity(String finalUrl) {
    if (cacheOne.get(finalUrl.hashCode()) == null) {
      boolean networkErrorHandling = false;
      while (!networkErrorHandling) {
        try {
          ResponseEntity<String> entity = externalRestTemplate.getForEntity(finalUrl, String.class);
          cacheOne.put(finalUrl.hashCode(), entity);
          return entity;
        } catch (Exception ex) {
          log.error(String.format("Error, retrying\r\n%s", ex.getMessage()));
        }
      }
      throw new IllegalStateException();
    }

    return (ResponseEntity<String>) cacheOne.get(finalUrl.hashCode()).get();
  }

  private DocumentContext getDocumentContext(String finalUrl) {
    DocumentContext jsonContext = null;
    boolean networkErrorHandling = false;
    while (!networkErrorHandling) {
      try {
        final ResponseEntity<String> response = getStringResponseEntity(finalUrl);
        jsonContext = JsonPath.parse(response.getBody());
        networkErrorHandling = true;
      } catch (Exception ex) {
        log.error(String.format("Error, retrying\r\n%s", ex.getMessage()));
      }
    }
    return jsonContext;
  }


}
