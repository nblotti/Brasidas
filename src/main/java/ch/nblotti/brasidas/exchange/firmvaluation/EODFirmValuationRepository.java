package ch.nblotti.brasidas.exchange.firmvaluation;

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
class EODFirmValuationRepository {


  private static final int MAX_RETRY = 100;


  @Value("${index.firm.api.url}")
  public String firmUrl;


  public String valuationStr = "$.Valuation";

  @Autowired
  protected RestTemplate externalShortRestTemplate;

  @Value("${spring.application.eod.api.key}")
  protected String apiKey;


  @Autowired
  Cache cacheOne;

  @Autowired
  protected ModelMapper modelMapper;


  public Optional<EODValuationDTO> getValuationByDateAndFirm(LocalDate runDate, String exchange, String symbol) {


    String finalUrl = String.format(firmUrl, symbol, exchange, apiKey);
    final ResponseEntity<String> response = getStringResponseEntity(finalUrl);
    try {
      DocumentContext jsonContext = JsonPath.parse(response.getBody());


      JSONObject eODExchangeDTOs = jsonContext.read(valuationStr, JSONObject.class);

      EODValuationDTO eODValuationDTO = modelMapper.map(eODExchangeDTOs, EODValuationDTO.class);

      return Optional.of(eODValuationDTO);

    } catch (Exception ex) {
      log.warn(String.format("Error, mapping valuation for symbol %s \r\n%s", symbol, ex.getMessage()));
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

    Converter<JSONObject, EODValuationDTO> toUppercase = new AbstractConverter<JSONObject, EODValuationDTO>() {

      @Override
      protected EODValuationDTO convert(JSONObject firmDTO) {
        EODValuationDTO eodValuationDTO = new EODValuationDTO();

        eodValuationDTO.setTrailingPE(Float.parseFloat(firmDTO.getAsString("TrailingPE")));
        eodValuationDTO.setForwardPE(Float.parseFloat(firmDTO.getAsString("ForwardPE")));
        eodValuationDTO.setPriceSalesTTM(Float.parseFloat(firmDTO.getAsString("PriceSalesTTM")));
        eodValuationDTO.setPriceBookMRQ(Float.parseFloat(firmDTO.getAsString("PriceBookMRQ")));
        eodValuationDTO.setEnterpriseValueRevenue(Float.parseFloat(firmDTO.getAsString("EnterpriseValueRevenue")));
        eodValuationDTO.setEnterpriseValueEbitda(Float.parseFloat(firmDTO.getAsString("EnterpriseValueEbitda")));

        return eodValuationDTO;
      }
    };

    modelMapper.addConverter(toUppercase);

  }


}
