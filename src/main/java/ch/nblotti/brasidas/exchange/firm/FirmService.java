package ch.nblotti.brasidas.exchange.firm;


import lombok.extern.slf4j.Slf4j;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
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
@Slf4j
public class FirmService {


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

  public List<ExchangeFirmQuoteDTO> getExchangeDataForDate(LocalDate localDate, String exchange) {
    List<EODExchangeDTO> eodFirmQuoteDTOS = eODExchangeRepository.getExchangeDataByDate(localDate, exchange);
    List<ExchangeFirmQuoteDTO> firmsTOs = eodFirmQuoteDTOS.stream().map(x -> modelMapper.map(x, ExchangeFirmQuoteDTO.class)).collect(Collectors.toList());

    List<ExchangeFirmQuoteDTO> filtredFirmsTOs = firmsTOs.stream().map(firmQuoteDTO -> {
      firmQuoteDTO.setActualExchange(exchange);
      return firmQuoteDTO;
    }).filter(y -> !y.getCode().startsWith("-")).collect(Collectors.toList());


    return filtredFirmsTOs;
  }

  public List<FirmQuoteDTO> getFirmQuoteByDate(LocalDate startDate, LocalDate endDate, String code, String exchange) {
    List<EODFirmQuoteDTO> eodFirmQuoteDTOS = eODExchangeRepository.getExchangeQuoteByDate(startDate, endDate, code, exchange);
    List<FirmQuoteDTO> firmsTOs = eodFirmQuoteDTOS.stream().map(x -> modelMapper.map(x, FirmQuoteDTO.class)).collect(Collectors.toList());

    return firmsTOs;

  }

  @PostConstruct
  void initFirmQuoteMapper() {

    Converter<EODFirmQuoteDTO, FirmQuoteDTO> toUppercase = new AbstractConverter<EODFirmQuoteDTO, FirmQuoteDTO>() {

      @Override
      protected FirmQuoteDTO convert(EODFirmQuoteDTO firmDTO) {

        FirmQuoteDTO firmQuoteTO = new FirmQuoteDTO();
        firmQuoteTO.setOpen(firmDTO.getOpen());
        firmQuoteTO.setDate(LocalDate.parse(firmDTO.getDate(), format1));
        firmQuoteTO.setClose(firmDTO.getClose());
        firmQuoteTO.setHigh(firmDTO.getHigh());
        firmQuoteTO.setLow(firmDTO.getLow());
        firmQuoteTO.setVolume(firmDTO.getVolume());
        firmQuoteTO.setAdjustedClose(firmDTO.getAdjusted_close());
        return firmQuoteTO;
      }
    };

    modelMapper.addConverter(toUppercase);

  }




  @PostConstruct
  void initExchangeFirmQuoteMapper() {

    Converter<EODExchangeDTO, ExchangeFirmQuoteDTO> toUppercase = new AbstractConverter<EODExchangeDTO, ExchangeFirmQuoteDTO>() {

      @Override
      protected ExchangeFirmQuoteDTO convert(EODExchangeDTO firmDTO) {
        ExchangeFirmQuoteDTO firmQuoteTO = new ExchangeFirmQuoteDTO();;
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

  public ExchangeFirmQuoteDTO saveEODMarketQuotes(ExchangeFirmQuoteDTO firmsTO) {

    HttpEntity<ExchangeFirmQuoteDTO> request = new HttpEntity<ExchangeFirmQuoteDTO>(firmsTO);

    return restTemplate.postForObject(firmQuoteStr, request, ExchangeFirmQuoteDTO.class);

  }

  public List<ExchangeFirmQuoteDTO> findAllByCodeOrderByDateAsc(String code) {

    ResponseEntity<ExchangeFirmQuoteDTO[]> quotes = restTemplate.getForEntity(String.format("%s%s", firmQuoteStr, code), ExchangeFirmQuoteDTO[].class);
    return Arrays.asList(quotes.getBody());

  }


  public void deleteByDate(LocalDate runDate) {
    restTemplate.delete(String.format("%s?localDate=%s", firmQuoteStr, runDate.format(format1)));
  }
}
