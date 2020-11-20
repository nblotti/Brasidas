package ch.nblotti.brasidas.quote;

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

@Component
public class QuoteService {

  @Value("${spring.application.eod.quote.url}")
  private String quoteUrl;


  @Autowired
  private RestTemplate rt;

  @Autowired
  private CacheManager cacheManager;

  @Autowired
  private DateTimeFormatter quoteDateTimeFormatter;

  @Autowired
  private DateTimeFormatter dateTimeFormatter;


  @Value("${spring.application.eod.api.key}")
  private String eodApiToken;

  static final String QUOTES = "quotes";


  /*Gestion des jours fériés et week-end : on prend le dernier disponible*/
  public QuoteDTO getQuoteForDate(String exchange, String symbol, LocalDate date) {

    LocalDate localDate = date;


    Map<LocalDate, QuoteDTO> quotes = getQuotes(exchange, symbol);

    while (!quotes.containsKey(localDate)) {
      localDate = localDate.minusDays(1);
      if (localDate.equals(LocalDate.parse("01.01.1900", getDateTimeFormatter())))
        throw new IllegalStateException(String.format("No quotes found for symbol %s", symbol));
    }
    return quotes.get(localDate);

  }


  Map<LocalDate, QuoteDTO> getQuotes(String exchange, String symbol) {

    Map<String, Map<LocalDate, QuoteDTO>> cachedQuotes;

    if (cacheManager.getCache(QUOTES).get(exchange) == null)
      cachedQuotes = new HashMap<>();
    else
      cachedQuotes = (Map<String, Map<LocalDate, QuoteDTO>>) cacheManager.getCache(QUOTES).get(exchange).get();

    if (!cachedQuotes.containsKey(symbol)) {
      ResponseEntity<QuoteDTO[]> responseEntity = rt.getForEntity(String.format(quoteUrl, symbol + "." + exchange, eodApiToken), QuoteDTO[].class);

      List<QuoteDTO> quotes = Arrays.asList(responseEntity.getBody());

      Map<LocalDate, QuoteDTO> quotesByDate = new HashMap<>();
      quotes.forEach(k -> quotesByDate.put(LocalDate.parse(k.getDate(), getQuoteDateTimeFormatter()), k));

      cachedQuotes.put(symbol, quotesByDate);
      cacheManager.getCache(QUOTES).put(exchange, cachedQuotes);

    }

    return cachedQuotes.get(symbol);

  }


  @Scheduled(fixedRate = 10800000)
  public void clearCache() {
    cacheManager.getCache(QUOTES).clear();
  }


  protected DateTimeFormatter getQuoteDateTimeFormatter() {
    return quoteDateTimeFormatter;
  }

  protected DateTimeFormatter getDateTimeFormatter() {
    return dateTimeFormatter;
  }
}
