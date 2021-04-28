package ch.nblotti.brasidas.exchange.firmvaluation;


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
import java.util.Optional;


@Service
public class FirmValuationService {



  @Value("${referential.firmvaluation.baseurl}")
  public String firmValuationStr;

  @Autowired
  RestTemplate internalRestTemplate;

  @Autowired
  private EODFirmValuationRepository EODFirmValuationRepository;


  @Autowired
  protected ModelMapper modelMapper;

  @Autowired
  DateTimeFormatter format1;


  public Optional<FirmValuationDTO> getValuationByDateAndFirm(LocalDate runDate, String exchange, String symbol) {
    Optional<EODValuationDTO> eODValuationDTO = EODFirmValuationRepository.getValuationByDateAndFirm(runDate, exchange, symbol);

    if (!eODValuationDTO.isPresent())
      return Optional.empty();

    FirmValuationDTO fVpost = modelMapper.map(eODValuationDTO.get(), FirmValuationDTO.class);
    fVpost.setExchange(exchange);
    fVpost.setDate(runDate);
    fVpost.setCode(symbol);
    return Optional.of(fVpost);

  }


  @PostConstruct
  public void initValuationMapper() {

    Converter<EODValuationDTO, FirmValuationDTO> toUppercase = new AbstractConverter<EODValuationDTO, FirmValuationDTO>() {

      @Override
      protected FirmValuationDTO convert(EODValuationDTO valuationDTO) {
        FirmValuationDTO firmEODValuationTO = new FirmValuationDTO();
        firmEODValuationTO.setForwardPE(valuationDTO.getForwardPE());
        firmEODValuationTO.setTrailingPE(valuationDTO.getTrailingPE());
        firmEODValuationTO.setPriceSalesTTM(valuationDTO.getPriceSalesTTM());
        firmEODValuationTO.setPriceBookMRQ(valuationDTO.getPriceBookMRQ());
        firmEODValuationTO.setEnterpriseValueRevenue(valuationDTO.getEnterpriseValueRevenue());
        firmEODValuationTO.setEnterpriseValueEbitda(valuationDTO.getEnterpriseValueEbitda());
        return firmEODValuationTO;
      }
    };

    modelMapper.addConverter(toUppercase);

  }

  public FirmValuationDTO save(FirmValuationDTO firmValuationDTO) {

    HttpEntity<FirmValuationDTO> request = new HttpEntity<FirmValuationDTO>(firmValuationDTO);

    FirmValuationDTO responseEntity = internalRestTemplate.postForObject(firmValuationStr, request, FirmValuationDTO.class);


    return responseEntity;
  }


  public void deleteByDate(LocalDate runDate) {
    internalRestTemplate.delete(String.format("%s?localDate=%s",firmValuationStr, runDate.format(format1)));
  }
}
