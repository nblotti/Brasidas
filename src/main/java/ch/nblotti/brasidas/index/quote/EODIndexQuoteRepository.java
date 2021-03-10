package ch.nblotti.brasidas.index.quote;

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
public class EODIndexQuoteRepository {



  @Autowired
  private DateTimeFormatter format1;


  public static final String INDEX_SUFFIX = "INDX";

  @Value("${index.quote.url}")
  private String indexUrl;
  public String indexHistoryStr = "$.[*]";

  @Autowired
  protected RestTemplate externalRestTemplate;


  @Value("${spring.application.eod.api.key}")
  protected String apiKey;


  @Autowired
  Cache cacheOne;


  public List<EODIndexQuoteDTO> getIndexDataByDate(LocalDate fromDate, LocalDate toDate, String index) {

    String finalUrl = String.format(indexUrl, index, INDEX_SUFFIX, fromDate.format(format1), toDate.format(format1), apiKey);

    DocumentContext jsonContext = getDocumentContext(finalUrl);
    List<EODIndexQuoteDTO> firms = Arrays.asList(jsonContext.read(indexHistoryStr, EODIndexQuoteDTO[].class));
    firms.stream().forEach(x -> x.setCode(index));

    return firms;
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
        log.warn( String.format("Error, retrying\r\n%s", ex.getMessage()));
      }
    }
    return jsonContext;
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
          log.warn(String.format("Error, retrying\r\n%s", ex.getMessage()));
        }
      }
      throw new IllegalStateException();
    }

    return (ResponseEntity<String>) cacheOne.get(finalUrl.hashCode()).get();
  }

}
