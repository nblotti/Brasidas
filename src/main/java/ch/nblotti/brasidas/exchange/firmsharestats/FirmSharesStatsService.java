package ch.nblotti.brasidas.exchange.firmsharestats;


import ch.nblotti.brasidas.exchange.firmhighlights.FirmHighlightsDTO;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;


@Service
public class FirmSharesStatsService {

  private static final Logger logger = Logger.getLogger("FirmService");

  public static final String FIRMS = "firms";
  public static final String FIRM_MAP = "firmsMap";

  @Value("${referential.firmsharesstat.baseurl}")
  public String firmShareStatsStr;

  @Autowired
  RestTemplate restTemplate;

  @Autowired
  protected ModelMapper modelMapper;


  @Autowired
  private EODFirmSharesStatsRepository eodFirmSharesStatsRepository;


  public Optional<FirmShareStatsDTO> getSharesStatByDateAndFirm(LocalDate runDate, String exchange, String symbol) {
    Optional<EODSharesStatsDTO> eODSharesStatsDTO = eodFirmSharesStatsRepository.getSharesStatByDateAndExchangeAndFirm(runDate, exchange, symbol);

    if (!eODSharesStatsDTO.isPresent())
      return Optional.empty();

    FirmShareStatsDTO fSpost = modelMapper.map(eODSharesStatsDTO.get(), FirmShareStatsDTO.class);
    fSpost.setExchange(exchange);
    fSpost.setDate(runDate);
    fSpost.setCode(symbol);

    return Optional.of(fSpost);


  }

  @PostConstruct
  public void initShareStatsMapper() {

    Converter<EODSharesStatsDTO, FirmShareStatsDTO> toUppercase = new AbstractConverter<EODSharesStatsDTO, FirmShareStatsDTO>() {

      @Override
      protected FirmShareStatsDTO convert(EODSharesStatsDTO sharesStatsDTO) {
        FirmShareStatsDTO firmEODShareStatsTO = new FirmShareStatsDTO();
        firmEODShareStatsTO.setSharesOutstanding(sharesStatsDTO.getSharesOutstanding());
        firmEODShareStatsTO.setSharesFloat(sharesStatsDTO.getSharesFloat());
        firmEODShareStatsTO.setPercentInsiders(sharesStatsDTO.getPercentInsiders());
        firmEODShareStatsTO.setPercentInstitutions(sharesStatsDTO.getPercentInstitutions());
        firmEODShareStatsTO.setSharesShort(sharesStatsDTO.getSharesShort());
        firmEODShareStatsTO.setSharesShortPriorMonth(sharesStatsDTO.getSharesShortPriorMonth());
        firmEODShareStatsTO.setShortRatio(sharesStatsDTO.getShortRatio());
        firmEODShareStatsTO.setShortPercentOutstanding(sharesStatsDTO.getShortPercentOutstanding());
        firmEODShareStatsTO.setShortPercentFloat(sharesStatsDTO.getShortPercentFloat());
        return firmEODShareStatsTO;
      }
    };

    modelMapper.addConverter(toUppercase);

  }

  public FirmShareStatsDTO save(FirmShareStatsDTO firmShareStatsDTO) {

    HttpEntity<FirmShareStatsDTO> request = new HttpEntity<FirmShareStatsDTO>(firmShareStatsDTO);

    FirmShareStatsDTO responseEntity = restTemplate.postForObject(firmShareStatsStr, request,FirmShareStatsDTO.class);

    return responseEntity;
  }

  public List<FirmShareStatsDTO> saveAll(List<FirmShareStatsDTO> firmSharesStats) {

    HttpEntity<List<FirmShareStatsDTO>> request = new HttpEntity<>(firmSharesStats);

    FirmShareStatsDTO[] responseEntity = restTemplate.postForObject(firmShareStatsStr, request, FirmShareStatsDTO[].class);

    return Arrays.asList(responseEntity.clone());
  }

    public void deleteByDate(LocalDate runDate) {
    }
}
