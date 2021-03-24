package ch.nblotti.brasidas.index.quote;

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
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EODIndexQuoteRepository {


  @Autowired
  private DateTimeFormatter format1;


  public static final String INDEX_SUFFIX = "INDX";

  @Value("${index.quote.url}")
  private String indexUrl;
  public String indexHistoryStr = "$.[*]";

  @Autowired
  protected RestTemplate externalShortRestTemplate;


  @Value("${spring.application.eod.api.key}")
  protected String apiKey;


  @Autowired
  Cache cacheOne;

  @Autowired
  protected ModelMapper modelMapper;

  public List<EODIndexQuoteDTO> getIndexDataByDate(LocalDate fromDate, LocalDate toDate, String index) {

    String finalUrl = String.format(indexUrl, index, INDEX_SUFFIX, fromDate.format(format1), toDate.format(format1), apiKey);

    DocumentContext jsonContext = getDocumentContext(finalUrl);
    JSONArray eODExchangeDTOs = jsonContext.read(indexHistoryStr);

    List<EODIndexQuoteDTO> indexQuoteDTOS = eODExchangeDTOs.stream().map(x -> modelMapper.map(x, EODIndexQuoteDTO.class)).collect(Collectors.toList());

    indexQuoteDTOS.stream().forEach(x -> x.setCode(index));

    return indexQuoteDTOS;
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
        log.warn(String.format("Error, retrying\r\n%s", ex.getMessage()));
      }
    }
    return jsonContext;
  }

  private ResponseEntity<String> getStringResponseEntity(String finalUrl) {
    if (cacheOne.get(finalUrl.hashCode()) == null) {
      boolean networkErrorHandling = false;
      while (!networkErrorHandling) {
        try {
          ResponseEntity<String> entity = externalShortRestTemplate.getForEntity(finalUrl, String.class);
          cacheOne.put(finalUrl.hashCode(), entity);
          return entity;
        } catch (Exception ex) {
          log.warn(String.format("Error, retrying\r\n%s", ex.getMessage()));
        }
      }
      throw new IllegalStateException();
    }

    return (ResponseEntity<String>) cacheOne.get(finalUrl.hashCode()).get();
  }


  @PostConstruct
  void initFirmQuoteMapper() {

    Converter<JSONObject, EODIndexQuoteDTO> toUppercase = new AbstractConverter<JSONObject, EODIndexQuoteDTO>() {

      @Override
      protected EODIndexQuoteDTO convert(JSONObject firmDTO) {
        EODIndexQuoteDTO eodFirmQuoteDTO = new EODIndexQuoteDTO();

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
