package ch.nblotti.brasidas.exchange.firmhighlights;


import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


@Service
public class FirmHighlightsService {


  public static final String FIRMS = "firms";
  public static final String FIRM_MAP = "firmsMap";


  @Value("${referential.firmhighlight.baseurl}")
  public String firmHighlightStr;

  @Autowired
  RestTemplate internalRestTemplate;


  @Autowired
  private EODFirmHighlightsRepository EODFirmHighlightsRepository;


  @Autowired
  protected ModelMapper modelMapper;

  @Autowired
  DateTimeFormatter format1;


  public Optional<FirmHighlightsDTO> getHighlightsByDateAndFirm(LocalDate runDate, String exchange, String symbol) {


    Optional<EODFirmHighlightsDTO> EODFirmHighlightsDTO = EODFirmHighlightsRepository.getHighlightsByDateAndFirm(runDate, exchange, symbol);

    if (!EODFirmHighlightsDTO.isPresent())
      return Optional.empty();

    FirmHighlightsDTO fHpost = modelMapper.map(EODFirmHighlightsDTO.get(), FirmHighlightsDTO.class);
    fHpost.setExchange(exchange);
    fHpost.setDate(runDate);
    fHpost.setCode(symbol);
    return Optional.of(fHpost);
  }

  @PostConstruct
  public void initFirmHighlightsMapper() {

    Converter<EODFirmHighlightsDTO, FirmHighlightsDTO> toUppercase = new AbstractConverter<EODFirmHighlightsDTO, FirmHighlightsDTO>() {

      @Override
      protected FirmHighlightsDTO convert(EODFirmHighlightsDTO eODFirmHighlightsDTO) {
        FirmHighlightsDTO firmHighlightsDTO = new FirmHighlightsDTO();
        firmHighlightsDTO.setMarketCapitalization(eODFirmHighlightsDTO.getMarketCapitalization());
        firmHighlightsDTO.setMarketCapitalizationMln(eODFirmHighlightsDTO.getMarketCapitalizationMln());
        firmHighlightsDTO.setEBITDA(eODFirmHighlightsDTO.getEBITDA());
        firmHighlightsDTO.setPERatio(eODFirmHighlightsDTO.getPERatio());
        firmHighlightsDTO.setPEGRatio(eODFirmHighlightsDTO.getPEGRatio());
        firmHighlightsDTO.setWallStreetTargetPrice(eODFirmHighlightsDTO.getWallStreetTargetPrice());
        firmHighlightsDTO.setBookValue(eODFirmHighlightsDTO.getBookValue());
        firmHighlightsDTO.setDividendShare(eODFirmHighlightsDTO.getDividendShare());
        firmHighlightsDTO.setDividendYield(eODFirmHighlightsDTO.getDividendYield());
        firmHighlightsDTO.setEarningsShare(eODFirmHighlightsDTO.getEarningsShare());
        firmHighlightsDTO.setEPSEstimateCurrentYear(eODFirmHighlightsDTO.getEPSEstimateCurrentYear());
        firmHighlightsDTO.setEPSEstimateNextYear(eODFirmHighlightsDTO.getEPSEstimateNextYear());
        firmHighlightsDTO.setEPSEstimateNextQuarter(eODFirmHighlightsDTO.getEPSEstimateNextQuarter());
        firmHighlightsDTO.setEPSEstimateCurrentQuarter(eODFirmHighlightsDTO.getEPSEstimateCurrentQuarter());
        firmHighlightsDTO.setMostRecentQuarter(eODFirmHighlightsDTO.getMostRecentQuarter());
        firmHighlightsDTO.setProfitMargin(eODFirmHighlightsDTO.getProfitMargin());
        firmHighlightsDTO.setOperatingMarginTTM(eODFirmHighlightsDTO.getOperatingMarginTTM());
        firmHighlightsDTO.setReturnOnAssetsTTM(eODFirmHighlightsDTO.getReturnOnAssetsTTM());
        firmHighlightsDTO.setReturnOnEquityTTM(eODFirmHighlightsDTO.getReturnOnEquityTTM());
        firmHighlightsDTO.setRevenueTTM(eODFirmHighlightsDTO.getRevenueTTM());
        firmHighlightsDTO.setRevenuePerShareTTM(eODFirmHighlightsDTO.getRevenuePerShareTTM());
        firmHighlightsDTO.setQuarterlyRevenueGrowthYOY(eODFirmHighlightsDTO.getQuarterlyRevenueGrowthYOY());
        firmHighlightsDTO.setGrossProfitTTM(eODFirmHighlightsDTO.getGrossProfitTTM());
        firmHighlightsDTO.setDilutedEpsTTM(eODFirmHighlightsDTO.getDilutedEpsTTM());
        firmHighlightsDTO.setQuarterlyEarningsGrowthYOY(eODFirmHighlightsDTO.getQuarterlyEarningsGrowthYOY());

        return firmHighlightsDTO;
      }
    };

    modelMapper.addConverter(toUppercase);

  }

  public FirmHighlightsDTO save(FirmHighlightsDTO firmHighlightsDTO) {


    HttpEntity<FirmHighlightsDTO> request = new HttpEntity<FirmHighlightsDTO>(firmHighlightsDTO);

    FirmHighlightsDTO responseEntity = internalRestTemplate.postForObject(firmHighlightStr, request, FirmHighlightsDTO.class);

    return firmHighlightsDTO;

  }


  public void deleteByDate(LocalDate runDate) {
    internalRestTemplate.delete(String.format("%s?localDate=%s",firmHighlightStr, runDate.format(format1)));
  }
}
