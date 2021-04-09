package ch.nblotti.brasidas.exchange.firm;


import lombok.extern.slf4j.Slf4j;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class QuoteService {


  @Autowired
  protected DateTimeFormatter format1;

  @Autowired
  private RestTemplate restTemplate;

  @Value("${referential.quotes.baseurl}")
  private String quoteStr;
  @Value("${referential.index.baseurl}")
  private String indexStr;


  public void refreshMaterializedView() {

    restTemplate.postForObject(String.format("%srefresh", quoteStr), null, Void.class);
    restTemplate.postForObject(String.format("%srefresh", indexStr), null, Void.class);
  }
}
