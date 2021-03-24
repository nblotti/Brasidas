package ch.nblotti.brasidas.exchange.firminfos;

import ch.nblotti.brasidas.exchange.firmhighlights.EODFirmHighlightsDTO;
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
class EODFirmInfoRepository {


  private static final int MAX_RETRY = 100;


  @Value("${index.firm.api.url}")
  public String firmUrl;


  public String infoStr = "$.General";


  @Autowired
  protected RestTemplate externalShortRestTemplate;


  @Value("${spring.application.eod.api.key}")
  protected String apiKey;


  @Autowired
  Cache cacheOne;

  @Autowired
  protected ModelMapper modelMapper;


  public Optional<EODFirmInfosDTO> getInfosByDateAndExchangeAndFirm(LocalDate runDate, String exchange, String symbol) {


    String finalUrl = String.format(firmUrl, symbol, exchange, apiKey);
    final ResponseEntity<String> response = getStringResponseEntity(finalUrl);
    try {
      DocumentContext jsonContext = JsonPath.parse(response.getBody());
      jsonContext.delete("$.General.Officers");
      jsonContext.delete("$.General.AddressData");
      jsonContext.delete("$.General.Listings");
      jsonContext.delete("$.General.isDelisted");

      boolean isDelisted = jsonContext.read("$.General.IsDelisted", boolean.class);

      if (isDelisted)
        return Optional.empty();

      JSONObject eODExchangeDTOs = jsonContext.read(infoStr,JSONObject.class);

      EODFirmInfosDTO eODFirmHighlightsDTO = modelMapper.map(eODExchangeDTOs, EODFirmInfosDTO.class);
      eODFirmHighlightsDTO.setExchange(exchange);


      return Optional.of(eODFirmHighlightsDTO);

    } catch (Exception ex) {
      log.error(String.format("Error, mapping info for symbol %s \r\n%s", symbol, ex.getMessage()));
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

    Converter<JSONObject, EODFirmInfosDTO> toUppercase = new AbstractConverter<JSONObject, EODFirmInfosDTO>() {

      @Override
      protected EODFirmInfosDTO convert(JSONObject firmDTO) {
        EODFirmInfosDTO eODFirmInfosDTO = new EODFirmInfosDTO();

        eODFirmInfosDTO.setCode(firmDTO.getAsString("Code"));
        eODFirmInfosDTO.setType(firmDTO.getAsString("Type"));
        eODFirmInfosDTO.setName(firmDTO.getAsString("Name"));
        eODFirmInfosDTO.setExchange(firmDTO.getAsString("Exchange"));
        eODFirmInfosDTO.setCurrencyCode(firmDTO.getAsString("CurrencyCode"));
        eODFirmInfosDTO.setCurrencyName(firmDTO.getAsString("CurrencyName"));
        eODFirmInfosDTO.setCurrencySymbol(firmDTO.getAsString("CurrencySymbol"));
        eODFirmInfosDTO.setCountryName(firmDTO.getAsString("CountryName"));
        eODFirmInfosDTO.setCountryISO(firmDTO.getAsString("CountryISO"));
        eODFirmInfosDTO.setISIN(firmDTO.getAsString("ISIN"));
        eODFirmInfosDTO.setCUSIP(firmDTO.getAsString("CUSIP"));
        eODFirmInfosDTO.setCIK(firmDTO.getAsString("CIK"));
        eODFirmInfosDTO.setEmployerIdNumber(firmDTO.getAsString("EmployerIdNumber"));
        eODFirmInfosDTO.setFiscalYearEnd(firmDTO.getAsString("FiscalYearEnd"));
        eODFirmInfosDTO.setIPODate(firmDTO.getAsString("IPODate"));
        eODFirmInfosDTO.setInternationalDomestic(firmDTO.getAsString("InternationalDomestic"));
        eODFirmInfosDTO.setSector(firmDTO.getAsString("Sector"));
        eODFirmInfosDTO.setIndustry(firmDTO.getAsString("Industry"));
        eODFirmInfosDTO.setGicSector(firmDTO.getAsString("GicSector"));
        eODFirmInfosDTO.setGicGroup(firmDTO.getAsString("GicGroup"));
        eODFirmInfosDTO.setGicIndustry(firmDTO.getAsString("GicIndustry"));
        eODFirmInfosDTO.setGicSubIndustry(firmDTO.getAsString("GicSubIndustry"));
        eODFirmInfosDTO.setHomeCategory(firmDTO.getAsString("HomeCategory"));
        eODFirmInfosDTO.setIsDelisted(Boolean.parseBoolean(firmDTO.getAsString("IsDelisted")));
        eODFirmInfosDTO.setDescription(firmDTO.getAsString("Description"));
        eODFirmInfosDTO.setAddress(firmDTO.getAsString("Address"));
        eODFirmInfosDTO.setPhone(firmDTO.getAsString("Phone"));
        eODFirmInfosDTO.setWebURL(firmDTO.getAsString("WebURL"));
        eODFirmInfosDTO.setLogoURL(firmDTO.getAsString("LogoURL"));
        eODFirmInfosDTO.setFullTimeEmployees(Integer.parseInt(firmDTO.getAsString("FullTimeEmployees")));
        eODFirmInfosDTO.setUpdatedAt(firmDTO.getAsString("UpdatedAt"));


        return eODFirmInfosDTO;
      }
    };

    modelMapper.addConverter(toUppercase);

  }


}
