package ch.nblotti.brasidas.fx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component(value = "fxQuoteService")
public  class FXQuoteService {

  @Autowired
  private FXQuoteRepository fxQuoteRepository;


  public Map<LocalDate, FXQuoteDTO> getFXQuotes(String currencyPair) {
    return fxQuoteRepository.getFXQuotes(currencyPair);
  }

  public FXQuoteDTO getFXQuoteForDate(String firstCurrency, String secondCurrency, LocalDate date) {
    return fxQuoteRepository.getFXQuoteForDate(firstCurrency, secondCurrency, date);
  }



}
