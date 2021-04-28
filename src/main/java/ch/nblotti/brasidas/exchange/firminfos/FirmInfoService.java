package ch.nblotti.brasidas.exchange.firminfos;


import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FirmInfoService {


  public static final String FIRMS = "firms";
  public static final String FIRM_MAP = "firmsMap";


  @Value("${referential.firminfo.baseurl}")
  public String firmInfoStr;

  @Autowired
  RestTemplate internalRestTemplate;

  @Autowired
  EODFirmInfoRepository eODFirmInfoRepository;


  @Autowired
  protected ModelMapper modelMapper;

  @Autowired
  DateTimeFormatter format1;


  public Optional<FirmInfoDTO> getInfosByDateAndExchangeAndFirm(LocalDate runDate, String exchange, String symbol) {

    Optional<EODFirmInfosDTO> firmInfosDTO = eODFirmInfoRepository.getInfosByDateAndExchangeAndFirm(runDate, exchange, symbol);

    if (!firmInfosDTO.isPresent())
      return Optional.empty();

    FirmInfoDTO fIpost = modelMapper.map(firmInfosDTO.get(), FirmInfoDTO.class);
    fIpost.setExchange(exchange);
    fIpost.setDate(runDate);
    fIpost.setCode(symbol);

    return Optional.of(fIpost);


  }


  @PostConstruct
  public void initFirmInfoMapper() {

    Converter<EODFirmInfosDTO, FirmInfoDTO> toUppercase = new AbstractConverter<EODFirmInfosDTO, FirmInfoDTO>() {

      @Override
      protected FirmInfoDTO convert(EODFirmInfosDTO eODFirmInfosDTO) {
        FirmInfoDTO firmInfoDTO = new FirmInfoDTO();
        firmInfoDTO.setCode(eODFirmInfosDTO.getCode());
        firmInfoDTO.setName(eODFirmInfosDTO.getName());
        firmInfoDTO.setType(eODFirmInfosDTO.getType());
        firmInfoDTO.setCurrencyName(eODFirmInfosDTO.getCurrencyName());
        firmInfoDTO.setCurrentExchange(eODFirmInfosDTO.getExchange());
        firmInfoDTO.setCurrencySymbol(eODFirmInfosDTO.getCurrencySymbol());
        firmInfoDTO.setCountryISO(eODFirmInfosDTO.getCountryISO());
        firmInfoDTO.setIsin(eODFirmInfosDTO.getISIN());
        firmInfoDTO.setCusip(eODFirmInfosDTO.getCUSIP());
        firmInfoDTO.setCik(eODFirmInfosDTO.getCIK());
        firmInfoDTO.setEmployerIdNumber(eODFirmInfosDTO.getEmployerIdNumber());
        firmInfoDTO.setFiscalYearEnd(eODFirmInfosDTO.getFiscalYearEnd());
        firmInfoDTO.setIPODate(eODFirmInfosDTO.getIPODate());
        firmInfoDTO.setInternationalDomestic(eODFirmInfosDTO.getInternationalDomestic());
        firmInfoDTO.setSector(eODFirmInfosDTO.getSector());

        firmInfoDTO.setIndustry(eODFirmInfosDTO.getIndustry());
        firmInfoDTO.setGicSector(eODFirmInfosDTO.getGicSector());
        firmInfoDTO.setGicGroup(eODFirmInfosDTO.getGicGroup());
        firmInfoDTO.setGicIndustry(eODFirmInfosDTO.getGicIndustry());
        firmInfoDTO.setGicSubIndustry(eODFirmInfosDTO.getGicSubIndustry());
        firmInfoDTO.setDescription(eODFirmInfosDTO.getDescription());
        firmInfoDTO.setAddress(eODFirmInfosDTO.getAddress());

        firmInfoDTO.setPhone(eODFirmInfosDTO.getPhone());
        firmInfoDTO.setWebURL(eODFirmInfosDTO.getWebURL());
        firmInfoDTO.setLogoURL(eODFirmInfosDTO.getLogoURL());
        firmInfoDTO.setFullTimeEmployees(eODFirmInfosDTO.getFullTimeEmployees());
        firmInfoDTO.setUpdatedAt(eODFirmInfosDTO.getUpdatedAt());


        return firmInfoDTO;
      }
    };

    modelMapper.addConverter(toUppercase);

  }


  public FirmInfoDTO save(FirmInfoDTO firmInfoDTO) {

    HttpEntity<FirmInfoDTO> request = new HttpEntity<FirmInfoDTO>(firmInfoDTO);

    FirmInfoDTO responseEntity = internalRestTemplate.postForObject(firmInfoStr, request, FirmInfoDTO.class);


    return firmInfoDTO;
  }



    public void deleteByDate(LocalDate runDate) {

      internalRestTemplate.delete(String.format("%s?localDate=%s",firmInfoStr, runDate.format(format1)));
    }
}
