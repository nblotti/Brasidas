package ch.nblotti.brasidas.exchange.firmsharestats;

import ch.nblotti.brasidas.exchange.firminfos.EODFirmInfosDTO;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Slf4j
class EODFirmSharesStatsRepository {


  private static final int MAX_RETRY = 100;


  @Value("${index.firm.api.url}")
  public String firmUrl;


  public String sharesStatStr = "$.SharesStats";


  @Autowired
  protected RestTemplate externalShortRestTemplate;

  @Value("${spring.application.eod.api.key}")
  protected String apiKey;


  @Autowired
  Cache cacheOne;

  @Autowired
  protected ModelMapper modelMapper;


  public Optional<EODSharesStatsDTO> getSharesStatByDateAndExchangeAndFirm(LocalDate runDate, String exchange, String symbol) {


    String finalUrl = String.format(firmUrl, symbol, exchange, apiKey);
    final ResponseEntity<String> response = getStringResponseEntity(finalUrl);
    try {
      DocumentContext jsonContext = JsonPath.parse(response.getBody());

      JSONObject eODExchangeDTOs = jsonContext.read(sharesStatStr,JSONObject.class);;

      EODSharesStatsDTO EODSharesStatsDTO = modelMapper.map(eODExchangeDTOs, EODSharesStatsDTO.class);

      return Optional.of(EODSharesStatsDTO);


    } catch (Exception ex) {
      log.warn(String.format("Error, mapping Share stats for symbol %s \r\n%s", symbol, ex.getMessage()));
      return Optional.empty();
    }
  }


  protected ResponseEntity<String> getStringResponseEntity(String finalUrl) {
    if (cacheOne.get(finalUrl.hashCode()) == null) {
      int networkErrorHandling = 0;
      while (networkErrorHandling < MAX_RETRY) {
        try {
          ResponseEntity<String> entity = externalShortRestTemplate.getForEntity(finalUrl, String.class);
          cacheOne.put(finalUrl.hashCode(), entity);
          return entity;
        } catch (Exception ex) {
          networkErrorHandling++;
          log.warn(String.format("Error, retrying\r\n%s", ex.getMessage()));
        }
      }
      throw new IllegalStateException();
    }

    return (ResponseEntity<String>) cacheOne.get(finalUrl.hashCode()).get();
  }

  @PostConstruct
  void initFirmQuoteMapper() {

    Converter<JSONObject, EODSharesStatsDTO> toUppercase = new AbstractConverter<JSONObject, EODSharesStatsDTO>() {

      @Override
      protected EODSharesStatsDTO convert(JSONObject firmDTO) {
        EODSharesStatsDTO eODSharesStatsDTO = new EODSharesStatsDTO();

        eODSharesStatsDTO.setSharesOutstanding(Long.parseLong(firmDTO.getAsString("SharesOutstanding")));
        eODSharesStatsDTO.setSharesFloat(Long.parseLong(firmDTO.getAsString("SharesFloat")));
        eODSharesStatsDTO.setPercentInsiders(Float.parseFloat(firmDTO.getAsString("PercentInsiders")));
        eODSharesStatsDTO.setPercentInstitutions(Float.parseFloat(firmDTO.getAsString("PercentInstitutions")));
        eODSharesStatsDTO.setSharesOutstanding(Long.parseLong(firmDTO.getAsString("SharesShort")));
        eODSharesStatsDTO.setSharesShortPriorMonth(Long.parseLong(firmDTO.getAsString("SharesShortPriorMonth")));
        eODSharesStatsDTO.setShortRatio(Float.parseFloat(firmDTO.getAsString("ShortRatio")));
        eODSharesStatsDTO.setShortPercentOutstanding(Float.parseFloat(firmDTO.getAsString("ShortPercentOutstanding")));
        eODSharesStatsDTO.setShortPercentFloat(Float.parseFloat(firmDTO.getAsString("ShortPercentFloat")));


        return eODSharesStatsDTO;
      }
    };

    modelMapper.addConverter(toUppercase);

  }


}
