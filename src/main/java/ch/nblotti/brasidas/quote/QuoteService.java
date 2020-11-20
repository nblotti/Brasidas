package ch.nblotti.brasidas.quote;

import ch.nblotti.brasidas.fx.FXQuoteDTO;
import ch.nblotti.brasidas.fx.FXQuoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component(value = "quoteService")
public class QuoteService {

  @Autowired
  private QuoteRepository quoteRepository;


  public Map<LocalDate, QuoteDTO> getQuotes(String exchange, String symbol) {
    return quoteRepository.getQuotes(exchange, symbol);
  }

  public QuoteDTO getQuoteForDate(String firstCurrency, String secondCurrency, LocalDate date) {
    return quoteRepository.getQuoteForDate(firstCurrency, secondCurrency, date);
  }


}
