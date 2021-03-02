package ch.nblotti.brasidas.exchange.split;


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
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Service
public class FirmSplitService {

  private static final Logger logger = Logger.getLogger("FirmSplitService");

  @Autowired
  protected ModelMapper modelMapper;

  @Autowired
  protected DateTimeFormatter format1;

  @Autowired
  private EODFirmSplitRepository eODFirmSplitRepository;

  @Autowired
  private RestTemplate restTemplate;

  @Value("${referential.firm.split.baseurl}")
  private String firmSplitStr;





  @PostConstruct
  void initFirmQuoteMapper() {

    Converter<EODFirmSplitDTO, FirmSplitDTO> toUppercase = new AbstractConverter<EODFirmSplitDTO, FirmSplitDTO>() {

      @Override
      protected FirmSplitDTO convert(EODFirmSplitDTO firmDTO) {
        FirmSplitDTO firmSplitDTO = new FirmSplitDTO();
        firmSplitDTO.setSplit(firmDTO.getSplit());
        firmSplitDTO.setCode(firmDTO.getCode());
        firmSplitDTO.setExchange(firmDTO.getExchange());
        firmSplitDTO.setDate(LocalDate.parse(firmDTO.getDate(), format1));
        return firmSplitDTO;
      }
    };

    modelMapper.addConverter(toUppercase);

  }

  public List<FirmSplitDTO> getSplitByDate(LocalDate runDate, String exchange) {
    List<EODFirmSplitDTO> splits = eODFirmSplitRepository.getSplitByDate(runDate, exchange);

    return splits.stream().map(x -> modelMapper.map(x, FirmSplitDTO.class)).collect(Collectors.toList());

  }

  public FirmSplitDTO saveFirmSplit(FirmSplitDTO firmSplitDTO) {

    HttpEntity<FirmSplitDTO> request = new HttpEntity<FirmSplitDTO>(firmSplitDTO);

    return restTemplate.postForObject(firmSplitStr, request, FirmSplitDTO.class);

  }
}
