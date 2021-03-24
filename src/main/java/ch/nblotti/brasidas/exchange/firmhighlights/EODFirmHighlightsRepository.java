package ch.nblotti.brasidas.exchange.firmhighlights;

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

@Component
@Slf4j
class EODFirmHighlightsRepository {


  @Autowired
  Cache cacheOne;


  @Value("${index.firm.api.url}")
  public String firmUrl;


  public String highlightStr = "$.Highlights";

  private static final int MAX_RETRY = 100;

  @Autowired
  protected RestTemplate externalShortRestTemplate;


  @Value("${spring.application.eod.api.key}")
  protected String apiKey;

  @Autowired
  protected ModelMapper modelMapper;


  public Optional<EODFirmHighlightsDTO> getHighlightsByDateAndFirm(LocalDate runDate, String exchange, String symbol) {


    String finalUrl = String.format(firmUrl, symbol, exchange, apiKey);
    final ResponseEntity<String> response = getStringResponseEntity(finalUrl);
    try {
      DocumentContext jsonContext = JsonPath.parse(response.getBody());
      JSONObject eODExchangeDTOs = jsonContext.read(highlightStr,JSONObject.class);

      EODFirmHighlightsDTO eODFirmHighlightsDTO = modelMapper.map(eODExchangeDTOs, EODFirmHighlightsDTO.class);


      return Optional.of(eODFirmHighlightsDTO);
    } catch (Exception ex) {
      log.error(String.format("Error, mapping highlight for symbol %s \r\n%s", symbol, ex.getMessage()));
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
          log.error(String.format("Error, retrying\r\n%s", ex.getMessage()));
        }
      }
      throw new IllegalStateException();
    }

    return (ResponseEntity<String>) cacheOne.get(finalUrl.hashCode()).get();
  }


  @PostConstruct
  void initFirmQuoteMapper() {

    Converter<JSONObject, EODFirmHighlightsDTO> toUppercase = new AbstractConverter<JSONObject, EODFirmHighlightsDTO>() {

      @Override
      protected EODFirmHighlightsDTO convert(JSONObject firmDTO) {
        EODFirmHighlightsDTO eODFirmHighlightsDTO = new EODFirmHighlightsDTO();

        eODFirmHighlightsDTO.setMarketCapitalization(Long.parseLong(firmDTO.getAsString("MarketCapitalizationMln")));
        eODFirmHighlightsDTO.setEBITDA(Long.parseLong(firmDTO.getAsString("EBITDA")));
        eODFirmHighlightsDTO.setPERatio(Double.parseDouble(firmDTO.getAsString("PERatio")));
        eODFirmHighlightsDTO.setPEGRatio(Double.parseDouble(firmDTO.getAsString("PEGRatio")));
        eODFirmHighlightsDTO.setWallStreetTargetPrice(Double.parseDouble(firmDTO.getAsString("WallStreetTargetPrice")));
        eODFirmHighlightsDTO.setBookValue(Double.parseDouble(firmDTO.getAsString("BookValue")));
        eODFirmHighlightsDTO.setDividendShare(Double.parseDouble(firmDTO.getAsString("DividendShare")));
        eODFirmHighlightsDTO.setDividendYield(Double.parseDouble(firmDTO.getAsString("DividendYield")));
        eODFirmHighlightsDTO.setEarningsShare(Double.parseDouble(firmDTO.getAsString("EarningsShare")));
        eODFirmHighlightsDTO.setEPSEstimateCurrentYear(Double.parseDouble(firmDTO.getAsString("EPSEstimateCurrentYear")));
        eODFirmHighlightsDTO.setEPSEstimateNextYear(Double.parseDouble(firmDTO.getAsString("EPSEstimateNextYear")));
        eODFirmHighlightsDTO.setEPSEstimateNextQuarter(Double.parseDouble(firmDTO.getAsString("EPSEstimateNextQuarter")));
        eODFirmHighlightsDTO.setEPSEstimateCurrentQuarter(Double.parseDouble(firmDTO.getAsString("EPSEstimateCurrentQuarter")));
        eODFirmHighlightsDTO.setMostRecentQuarter(firmDTO.getAsString("MostRecentQuarter"));
        eODFirmHighlightsDTO.setProfitMargin(Double.parseDouble(firmDTO.getAsString("ProfitMargin")));
        eODFirmHighlightsDTO.setOperatingMarginTTM(Double.parseDouble(firmDTO.getAsString("OperatingMarginTTM")));
        eODFirmHighlightsDTO.setReturnOnAssetsTTM(Double.parseDouble(firmDTO.getAsString("ReturnOnAssetsTTM")));
        eODFirmHighlightsDTO.setReturnOnEquityTTM(Double.parseDouble(firmDTO.getAsString("ReturnOnEquityTTM")));
        eODFirmHighlightsDTO.setRevenueTTM(Long.parseLong(firmDTO.getAsString("RevenueTTM")));
        eODFirmHighlightsDTO.setRevenuePerShareTTM(Double.parseDouble(firmDTO.getAsString("RevenuePerShareTTM")));
        eODFirmHighlightsDTO.setQuarterlyEarningsGrowthYOY(Double.parseDouble(firmDTO.getAsString("QuarterlyRevenueGrowthYOY")));
        eODFirmHighlightsDTO.setGrossProfitTTM(Long.parseLong(firmDTO.getAsString("GrossProfitTTM")));
        eODFirmHighlightsDTO.setDilutedEpsTTM(Double.parseDouble(firmDTO.getAsString("DilutedEpsTTM")));
        eODFirmHighlightsDTO.setQuarterlyRevenueGrowthYOY(Double.parseDouble(firmDTO.getAsString("QuarterlyEarningsGrowthYOY")));


        return eODFirmHighlightsDTO;
      }
    };

    modelMapper.addConverter(toUppercase);

  }


}
