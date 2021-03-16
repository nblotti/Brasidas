package ch.nblotti.brasidas;


import ch.nblotti.brasidas.exchange.loader.MARKET_LOADER_EVENTS;
import ch.nblotti.brasidas.exchange.loader.MARKET_LOADER_STATES;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.websocket.server.PathParam;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/ping")
public class PingController {


  @Resource
  private StateMachine<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> sp500LoaderStateMachine;

  @Autowired
  protected DateTimeFormatter format1;


  @GetMapping
  public ResponseEntity<String> ping(@PathParam(value = "key") String key) {

    return ResponseEntity.ok(String.format("%s - %s", key == null ? "" : key, LocalDateTime.now().format(format1)));

  }


}



