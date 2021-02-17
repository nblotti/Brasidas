package ch.nblotti.brasidas.index.composition;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
public class EODIndexCompositionRepository {


  @Autowired
  private DateTimeFormatter format1;

  @Value("${index.component.api.url}")
  public String indexComponentUrl;


  public String conponentStr = "$.Components[*]";


  @Value("${spring.application.eod.api.key}")
  public String apiKey;


  @Autowired
  private RestTemplate restTemplate;


  public Collection<EODIndexCompositionDTO> getIndexCompositionAtDate(LocalDate localDate, String index) {


    String finalUrl = String.format(indexComponentUrl, index, apiKey, localDate.format(format1), localDate.format(format1));

    final ResponseEntity<String> response = restTemplate.getForEntity(finalUrl, String.class);

    DocumentContext jsonContext = JsonPath.parse(response.getBody());

    List<EODIndexCompositionDTO> firms = Arrays.asList(jsonContext.read(conponentStr, EODIndexCompositionDTO[].class));
    firms.stream().forEach(x -> x.setExchange(index));

    return firms;
  }


}


