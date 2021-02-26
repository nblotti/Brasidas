package ch.nblotti.brasidas;


import ch.nblotti.brasidas.fx.FXQuoteService;
import ch.nblotti.brasidas.loader.LOADER_EVENTS;
import ch.nblotti.brasidas.loader.LOADER_STATES;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.websocket.server.PathParam;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/ping")
public class PingController {


  @Resource
  private StateMachine<LOADER_STATES, LOADER_EVENTS> sp500LoaderStateMachine;

  @Autowired
  protected DateTimeFormatter format1;


  @GetMapping
  public ResponseEntity<String> ping(@PathParam(value = "key") String key) {


    if (LOADER_STATES.READY == sp500LoaderStateMachine.getState().getId())
      return ResponseEntity.ok(String.format("%s - %s", key == null ? "" : key, LocalDateTime.now().format(format1)));
    else
      return ResponseEntity.notFound().build();


  }


}



