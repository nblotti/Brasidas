package ch.nblotti.brasidas.exchange.firm;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
class EODExchangeRepository {


  public static final String EXCHANGE = "exchange";
  public static final String EXCHANGE_JSON = "exchangelJson";
  public static final String FIRMS_FINANCIALS = "firms";
  public static final String FIRMS_FINANCIALS_JSON = "firmsFinancialJson";


  @Value("${firm.marketCap.bulk.url}")
  private String marketCap;


  @Value("${firm.quote.url}")
  private String firmQuoteUrl;

  public String sharesHistoryStr = "$.[*]";


  @Autowired
  protected DateTimeFormatter format1;

  @Autowired
  protected RestTemplate externalLongRestTemplate;

  @Value("${spring.application.eod.api.key}")
  protected String apiKey;


  @Autowired
  Cache cacheOne;

  @Autowired
  protected ModelMapper modelMapper;


  public List<EODFirmQuoteDTO> getExchangeQuoteByDate(LocalDate startDate, LocalDate endDate, String code, String exchange) {

    String finalUrl = String.format(firmQuoteUrl, code, exchange, startDate.format(format1), endDate.format(format1), apiKey);

    DocumentContext jsonContext = getDocumentContext(finalUrl);
    JSONArray eODExchangeDTOs = jsonContext.read(sharesHistoryStr);

    List<EODFirmQuoteDTO> firmsTOs = eODExchangeDTOs.stream().map(x -> modelMapper.map(x, EODFirmQuoteDTO.class)).collect(Collectors.toList());

    return firmsTOs;
  }


  public List<EODExchangeDTO> getExchangeDataByDate(LocalDate runDate, String exchange) {

    String finalUrl = String.format(marketCap, exchange, apiKey, runDate.format(format1));

    DocumentContext jsonContext = getDocumentContext(finalUrl);
    JSONArray eODExchangeDTOs = jsonContext.read(sharesHistoryStr);

    List<EODExchangeDTO> firmsTOs = eODExchangeDTOs.stream().map(x -> modelMapper.map(x, EODExchangeDTO.class)).collect(Collectors.toList());

    return firmsTOs;
  }

  private ResponseEntity<String> getStringResponseEntity(String finalUrl) {
    if (cacheOne.get(finalUrl.hashCode()) == null) {
      boolean networkErrorHandling = false;
      while (!networkErrorHandling) {
        try {
          ResponseEntity<String> entity = externalLongRestTemplate.getForEntity(finalUrl, String.class);
          cacheOne.put(finalUrl.hashCode(), entity);
          return entity;
        } catch (Exception ex) {
          log.error(String.format("Error, retrying\r\n%s", ex.getMessage()));
        }
      }
      throw new IllegalStateException();
    }

    return (ResponseEntity<String>) cacheOne.get(finalUrl.hashCode()).get();
  }

  private DocumentContext getDocumentContext(String finalUrl) {
    DocumentContext jsonContext = null;
    boolean networkErrorHandling = false;
    while (!networkErrorHandling) {
      try {
        final ResponseEntity<String> response = getStringResponseEntity(finalUrl);
        jsonContext = JsonPath.parse(response.getBody());
        networkErrorHandling = true;
      } catch (Exception ex) {
        log.error(String.format("Error, retrying\r\n%s", ex.getMessage()));
      }
    }
    return jsonContext;
  }


  @PostConstruct
  void initExchangeFirmMapper() {

    Converter<JSONObject, EODExchangeDTO> toUppercase = new AbstractConverter<JSONObject, EODExchangeDTO>() {

      @Override
      protected EODExchangeDTO convert(JSONObject firmDTO) {
        EODExchangeDTO eODExchangeDTO = new EODExchangeDTO();

        eODExchangeDTO.setCode(firmDTO.getAsString("code"));
        eODExchangeDTO.setName(firmDTO.getAsString("name"));
        eODExchangeDTO.setExchange_short_name(firmDTO.getAsString("exchange_short_name"));
        eODExchangeDTO.setDate(firmDTO.getAsString("date"));
        eODExchangeDTO.setMarketCapitalization(Long.parseLong(firmDTO.getAsString("marketCapitalization")));
        eODExchangeDTO.setOpen(Float.parseFloat(firmDTO.getAsString("open")));
        eODExchangeDTO.setHigh(Float.parseFloat(firmDTO.getAsString("high")));
        eODExchangeDTO.setLow(Float.parseFloat(firmDTO.getAsString("low")));
        eODExchangeDTO.setClose(Float.parseFloat(firmDTO.getAsString("close")));
        eODExchangeDTO.setAdjusted_close(Float.parseFloat(firmDTO.getAsString("adjusted_close")));
        eODExchangeDTO.setEma_50d(Float.parseFloat(firmDTO.getAsString("ema_50d")));
        eODExchangeDTO.setEma_200d(Float.parseFloat(firmDTO.getAsString("ema_50d")));
        eODExchangeDTO.setMarketCapitalization(Long.parseLong(firmDTO.getAsString("MarketCapitalization")));
        eODExchangeDTO.setLo_250d(Float.parseFloat(firmDTO.getAsString("lo_250d")));
        eODExchangeDTO.setHi_250d(Float.parseFloat(firmDTO.getAsString("hi_250d")));
        eODExchangeDTO.setAvgvol_14d(Float.parseFloat(firmDTO.getAsString("avgvol_14d")));
        eODExchangeDTO.setAvgvol_50d(Float.parseFloat(firmDTO.getAsString("avgvol_50d")));
        eODExchangeDTO.setAvgvol_200d(Float.parseFloat(firmDTO.getAsString("avgvol_200d")));


        return eODExchangeDTO;
      }
    };

    modelMapper.addConverter(toUppercase);

  }

  @PostConstruct
  void initFirmQuoteMapper() {

    Converter<JSONObject, EODFirmQuoteDTO> toUppercase = new AbstractConverter<JSONObject, EODFirmQuoteDTO>() {

      @Override
      protected EODFirmQuoteDTO convert(JSONObject firmDTO) {
        EODFirmQuoteDTO eodFirmQuoteDTO = new EODFirmQuoteDTO();

        eodFirmQuoteDTO.setDate(firmDTO.getAsString("code"));
        eodFirmQuoteDTO.setOpen(Float.parseFloat(firmDTO.getAsString("open")));
        eodFirmQuoteDTO.setHigh(Float.parseFloat(firmDTO.getAsString("high")));
        eodFirmQuoteDTO.setLow(Float.parseFloat(firmDTO.getAsString("low")));
        eodFirmQuoteDTO.setClose(Float.parseFloat(firmDTO.getAsString("close")));
        eodFirmQuoteDTO.setAdjusted_close(Float.parseFloat(firmDTO.getAsString("adjusted_close")));
        eodFirmQuoteDTO.setVolume(Long.parseLong(firmDTO.getAsString("volume")));


        return eodFirmQuoteDTO;
      }
    };

    modelMapper.addConverter(toUppercase);

  }

}
