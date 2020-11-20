package ch.nblotti.brasidas.fx;


import ch.nblotti.brasidas.firm.FirmSearchDto;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/forex")
public class FxQuoteController {

  @Autowired
  FXQuoteService fxQuoteService;

  @GetMapping(value = "/{currencyPair}")
  public Map<LocalDate, FXQuoteDTO> getFXQuotes(@PathVariable String currencyPair) {
    return fxQuoteService.getFXQuotes(currencyPair);
  }

  @GetMapping(value = "/{firstCurrency}/{secondCurrency}/{date}")
  public FXQuoteDTO getFXQuoteForDate(@PathVariable String firstCurrency, @PathVariable String secondCurrency, @PathVariable LocalDate date) {
    return fxQuoteService.getFXQuoteForDate(firstCurrency, secondCurrency, date);
  }


}



