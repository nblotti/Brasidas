package ch.nblotti.brasidas.index.composition;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
public class EODIndexCompositionRepository {


  @Autowired
  private DateTimeFormatter format1;

  @Value("${index.component.api.url}")
  public String eodIndexComponentUrl;



  public String conponentStr = "$.HistoricalTickerComponents[*]";


  @Value("${spring.application.eod.api.key}")
  public String apiKey;


  @Autowired
  private RestTemplate externalShortRestTemplate;


  public Collection<EODIndexCompositionDTO> getIndexComposition(String index) {


    String finalUrl = String.format(eodIndexComponentUrl, index, apiKey);

    final ResponseEntity<String> response = externalShortRestTemplate.getForEntity(finalUrl, String.class);

    DocumentContext jsonContext = JsonPath.parse(response.getBody());

    return Arrays.asList(jsonContext.read(conponentStr, EODIndexCompositionDTO[].class));

  }



}


