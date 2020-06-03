package ch.nblotti.asset.firm;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/firm")
public class FirmController {

  @Autowired
  RestTemplate restTemplate;

  @Value("${solr.server.uri}")
  private String searchServerHost;

  public String highlightStr = "$.response.docs";

  @PostMapping(value = "/search/")
  public List<FirmSearchDto> mirrorRest(@RequestBody String body, @RequestHeader MultiValueMap<String, String> headers) throws URISyntaxException {

    String url = String.format("%s", searchServerHost);

    ResponseEntity<String> responseEntity =
      restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<String>(body, headers), String.class);

    Configuration conf = Configuration
      .builder()
      .mappingProvider(new JacksonMappingProvider())
      .jsonProvider(new JacksonJsonProvider())
      .build();

    TypeRef<List<FirmSearchDto>> type = new TypeRef<List<FirmSearchDto>>() {
    };

    List<FirmSearchDto> firms = JsonPath
      .using(conf)
      .parse(responseEntity.getBody())
      .read(highlightStr, type);

    return firms;


  }
}



