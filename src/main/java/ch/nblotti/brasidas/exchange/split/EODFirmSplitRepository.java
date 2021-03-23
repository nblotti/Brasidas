package ch.nblotti.brasidas.exchange.split;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class EODFirmSplitRepository {


  @Value("${firm.split.bulk.url}")
  public String firmSplitUrl;

  private static final int MAX_RETRY = 100;

  public String firmSplitStr = "$.[*]";


  @Autowired
  protected DateTimeFormatter format1;

  @Autowired
  protected RestTemplate externalShortRestTemplate;

  @Value("${spring.application.eod.api.key}")
  protected String apiKey;


  public List<EODFirmSplitDTO> getSplitByDate(LocalDate runDate, String exchange) {


    String finalUrl = String.format(firmSplitUrl, exchange, apiKey, runDate.format(format1));

    DocumentContext jsonContext = getDocumentContext(finalUrl);
    return Arrays.asList(jsonContext.read(firmSplitStr, EODFirmSplitDTO[].class));

  }

  private ResponseEntity<String> getStringResponseEntity(String finalUrl) {
    boolean networkErrorHandling = false;
    while (!networkErrorHandling) {
      try {
        ResponseEntity<String> entity = externalShortRestTemplate.getForEntity(finalUrl, String.class);
        return entity;
      } catch (Exception ex) {
        log.error(String.format("Error, retrying\r\n%s", ex.getMessage()));
      }
    }
    throw new IllegalStateException();
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
