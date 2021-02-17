package ch.nblotti.brasidas;


import ch.nblotti.brasidas.fx.FXQuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.websocket.server.PathParam;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/ping")
public class PingController {

  @Autowired
  protected DateTimeFormatter format1;

  @Autowired
  RestTemplate restTemplate;

  @GetMapping
  public String ping(@PathParam(value = "key") String key) {

    return String.format("%s - %s", key, LocalDateTime.now().format(format1));

  }



}



