package ch.nblotti.brasidas.exchange.dayoffloader;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EODDayOffRepository {


  @Value("${eod.market.dayoff}")
  public String marketDayOff;

  public final String exchangeHolidays = "$.ExchangeHolidays[*]";
  private final String NAME = "$.Name";
  private final String CODE = "$.Code";
  private final String MICS = "$.OperatingMIC";
  private final String COUNTRY = "$.Country";
  private final String CURRENCY = "$.Currency";
  private final String TIMEZONE = "$.Timezone";

  private final String DATE = "Date";
  private final String TYPE = "Type";
  private final String HOLIDAY = "Holiday";


  @Autowired
  protected DateTimeFormatter format1;

  @Autowired
  protected RestTemplate externalShortRestTemplate;

  @Value("${spring.application.eod.api.key}")
  protected String apiKey;


  @Autowired
  protected ModelMapper modelMapper;

  public List<EODDayOffDTO> getDayOff(LocalDate fromDate, LocalDate toDate, String exchange) {

    List<EODDayOffDTO> eODDayOffDTOs= new ArrayList<>();
    String finalUrl = String.format(marketDayOff, exchange, apiKey, fromDate.format(format1), toDate.format(format1));

    DocumentContext jsonContext = getDocumentContext(finalUrl);

    String name = jsonContext.read(NAME);
    String code = jsonContext.read(CODE);
    String mics = jsonContext.read(MICS);
    String country = jsonContext.read(COUNTRY);
    String currency = jsonContext.read(CURRENCY);
    String timeZone = jsonContext.read(TIMEZONE);

    JSONArray eODExchangeHolidays = jsonContext.read(exchangeHolidays);

    for(Object object : eODExchangeHolidays){

      EODDayOffDTO eodDayOffDTO = new EODDayOffDTO();
      LinkedHashMap<String,String> dayOff = (LinkedHashMap<String, String>) object;
      eodDayOffDTO.setDate(LocalDate.parse(dayOff.get(DATE).toString(), format1));
      eodDayOffDTO.setHoliday(dayOff.get(HOLIDAY).toString());
      eodDayOffDTO.setType(dayOff.get(TYPE).toString());
      eodDayOffDTO.setName(name);
      eodDayOffDTO.setCode(code);
      eodDayOffDTO.setMics(mics);
      eodDayOffDTO.setCountry(country);
      eodDayOffDTO.setCurrency(currency);
      eodDayOffDTO.setTimezone(timeZone);

      eODDayOffDTOs.add(eodDayOffDTO);
    };

    return eODDayOffDTOs;

  }

  private ResponseEntity<String> getStringResponseEntity(String finalUrl) {
    boolean networkErrorHandling = false;
    while (!networkErrorHandling) {
      try {
        ResponseEntity<String> entity = externalShortRestTemplate.getForEntity(finalUrl, String.class);
        return entity;
      } catch (Exception ex) {
        log.error(String.format("Error, retrying\r\n%s", ex.getMessage()));
      }
    }
    throw new IllegalStateException();
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




}
