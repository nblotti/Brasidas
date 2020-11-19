package ch.nblotti.brasidas.fx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(value = "fxQuoteRepository")
public class FXQuoteRepository {

  @Value("${spring.application.eod.quote.url}")
  private String quoteUrl;


  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private CacheManager cacheManager;

  @Autowired
  private DateTimeFormatter quoteDateTimeFormatter;

  @Autowired
  private DateTimeFormatter dateTimeFormatter;


  @Value("${spring.application.eod.api.key}")
  private String eodApiToken;


  Map<LocalDate, FXQuoteDTO> getFXQuotes(String currencyPair) {


    Map<String, Map<LocalDate, FXQuoteDTO>> cachedQuotes;

    if (cacheManager.getCache(QUOTES).get(FOREX) == null)
      cachedQuotes = new HashMap<>();
    else
      cachedQuotes = (Map<String, Map<LocalDate, FXQuoteDTO>>) cacheManager.getCache(QUOTES).get(FOREX).get();

    if (!cachedQuotes.containsKey(currencyPair)) {
      ResponseEntity<FXQuoteDTO[]> responseEntity = restTemplate.getForEntity(String.format(quoteUrl, currencyPair + "." + FOREX, eodApiToken), FXQuoteDTO[].class);

      List<FXQuoteDTO> quotes = Arrays.asList(responseEntity.getBody());

      Map<LocalDate, FXQuoteDTO> quotesByDate = new HashMap<>();
      quotes.forEach(k -> quotesByDate.put(LocalDate.parse(k.getDate(), quoteDateTimeFormatter), k));

      cachedQuotes.put(currencyPair, quotesByDate);
      cacheManager.getCache(QUOTES).put(FOREX, cachedQuotes);

    }

    return cachedQuotes.get(currencyPair);

  }


  @Scheduled(fixedRate = 10800000)
  public void clearCache() {
    cacheManager.getCache(QUOTES).clear();
  }



  static final String QUOTES = "quotes";
  static final String FOREX = "FOREX";


  /*Gestion des jours fériés et week-end : on prend le dernier disponible*/
  public FXQuoteDTO getFXQuoteForDate(String firstCurrency, String secondCurrency, LocalDate date) {

    String currencyPair;
    LocalDate localDate = date;

    if (firstCurrency.equals(secondCurrency)) {
      FXQuoteDTO FXQuoteDTO = new FXQuoteDTO();
      FXQuoteDTO.setAdjustedClose("1");
      FXQuoteDTO.setClose("1");
      FXQuoteDTO.setHigh("1");
      FXQuoteDTO.setLow("1");
      FXQuoteDTO.setOpen("1");
      FXQuoteDTO.setVolume("0");
      FXQuoteDTO.setDate(date.format(quoteDateTimeFormatter));
      return FXQuoteDTO;

    } else {
      currencyPair = String.format("%s%s", firstCurrency, secondCurrency);
    }


    Map<LocalDate, FXQuoteDTO> quotes = getFXQuotes(currencyPair);

    while (!quotes.containsKey(localDate)) {
      localDate = localDate.minusDays(1);
      if (localDate.equals(LocalDate.parse("01.01.1900", dateTimeFormatter)))
        throw new IllegalStateException(String.format("No quotes found for symbol %s", currencyPair));
    }
    return quotes.get(localDate);
  }


}
