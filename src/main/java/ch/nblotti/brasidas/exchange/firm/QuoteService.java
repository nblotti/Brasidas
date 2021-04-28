package ch.nblotti.brasidas.exchange.firm;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;


@Service
@Slf4j
public class QuoteService {


  @Autowired
  protected DateTimeFormatter format1;

  @Autowired
  private RestTemplate internalRestTemplate;

  @Value("${referential.quotes.baseurl}")
  private String quoteStr;
  @Value("${referential.index.baseurl}")
  private String indexStr;


  public void refreshMaterializedView() {

    internalRestTemplate.postForObject(String.format("%srefresh", quoteStr), null, Void.class);
    internalRestTemplate.postForObject(String.format("%srefresh", indexStr), null, Void.class);
  }
}
