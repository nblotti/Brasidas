package ch.nblotti.brasidas.exchange.firm;


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
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Service
public class FirmService {

  private static final Logger logger = Logger.getLogger("FirmService");

  public static final String FIRMS = "firms";
  public static final String FIRM_MAP = "firmsMap";


  @Autowired
  protected ModelMapper modelMapper;

  @Autowired
  protected DateTimeFormatter format1;

  @Autowired
  private EODExchangeRepository eODExchangeRepository;

  @Autowired
  private RestTemplate restTemplate;

  @Value("${referential.firm.quote.baseurl}")
  private String firmQuoteStr;

  public List<FirmQuoteDTO> getExchangeDataForDate(LocalDate localDate, String exchange) {
    List<EODExchangeDTO> eodFirmQuoteDTOS = eODExchangeRepository.getExchangeDataByDate(localDate, exchange);
    List<FirmQuoteDTO> firmsTOs = eodFirmQuoteDTOS.stream().map(x -> modelMapper.map(x, FirmQuoteDTO.class)).collect(Collectors.toList());

    List<FirmQuoteDTO> filtredFirmsTOs = firmsTOs.stream().map(firmQuoteDTO -> {
      firmQuoteDTO.setActualExchange(exchange);
      return firmQuoteDTO;
    }).filter(y -> !y.getCode().startsWith("-")).collect(Collectors.toList());


    return filtredFirmsTOs;
  }


  @PostConstruct
  void initFirmQuoteMapper() {

    Converter<EODExchangeDTO, FirmQuoteDTO> toUppercase = new AbstractConverter<EODExchangeDTO, FirmQuoteDTO>() {

      @Override
      protected FirmQuoteDTO convert(EODExchangeDTO firmDTO) {
        FirmQuoteDTO firmQuoteTO = new FirmQuoteDTO();
        firmQuoteTO.setName(firmDTO.getName());
        firmQuoteTO.setCode(firmDTO.getCode());
        firmQuoteTO.setExchangeShortName(firmDTO.getExchange_short_name());
        firmQuoteTO.setDate(LocalDate.parse(firmDTO.getDate(), format1));
        firmQuoteTO.setMarketCapitalization(firmDTO.getMarketCapitalization());
        firmQuoteTO.setVolume(firmDTO.getVolume());
        firmQuoteTO.setAdjustedClose(firmDTO.getAdjusted_close());
        return firmQuoteTO;
      }
    };

    modelMapper.addConverter(toUppercase);

  }

  public FirmQuoteDTO saveEODMarketQuotes(FirmQuoteDTO firmsTO) {

    HttpEntity<FirmQuoteDTO> request = new HttpEntity<FirmQuoteDTO>(firmsTO);

    return restTemplate.postForObject(firmQuoteStr, request, FirmQuoteDTO.class);

  }


  public void deleteByDate(LocalDate runDate) {
    restTemplate.delete(String.format("%s?localDate=%s", firmQuoteStr, runDate.format(format1)));
  }
}
