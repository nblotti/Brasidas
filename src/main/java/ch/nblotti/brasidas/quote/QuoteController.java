package ch.nblotti.brasidas.quote;


import ch.nblotti.brasidas.fx.FXQuoteDTO;
import ch.nblotti.brasidas.fx.FXQuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/market")
public class QuoteController {


  @Autowired
  QuoteService quoteService;

  @GetMapping("/{exchange}/{symbol}")
  public Map<LocalDate, QuoteDTO> getQuote(@PathVariable String exchange, @PathVariable String symbol) {
    return quoteService.getQuotes(exchange, symbol);
  }

  @GetMapping("/{exchange}/{symbol}/{date}")
  public QuoteDTO getQuoteForDate(@PathVariable String exchange, @PathVariable String symbol, @PathVariable LocalDate date) {
    return quoteService.getQuoteForDate(exchange, symbol, date);
  }


}
