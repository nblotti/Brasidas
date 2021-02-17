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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;


@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FirmInfoService {

  private static final Logger logger = Logger.getLogger("FirmService");

  public static final String FIRMS = "firms";
  public static final String FIRM_MAP = "firmsMap";


  @Value("${referential.firminfo.baseurl}")
  public String firmhighlightStr;

  @Autowired
  RestTemplate restTemplate;

  @Autowired
  EODFirmInfoRepository eODFirmInfoRepository;


  @Autowired
  protected ModelMapper modelMapper;


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
        firmInfoDTO.setExchange(eODFirmInfosDTO.getExchange());
        firmInfoDTO.setCurrencyName(eODFirmInfosDTO.getCurrencyName());
        firmInfoDTO.setCurrencySymbol(eODFirmInfosDTO.getCurrencySymbol());
        firmInfoDTO.setCountryISO(eODFirmInfosDTO.getCountryISO());
        firmInfoDTO.setIsin(eODFirmInfosDTO.getISIN());
        firmInfoDTO.setcCusip(eODFirmInfosDTO.getCUSIP());
        firmInfoDTO.setcCik(eODFirmInfosDTO.getCIK());
        firmInfoDTO.setEmployerIdNumber(eODFirmInfosDTO.getEmployerIdNumber());
        firmInfoDTO.setFiscalYearEnd(eODFirmInfosDTO.getFiscalYearEnd());
        firmInfoDTO.setiPODate(eODFirmInfosDTO.getIPODate());
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

    FirmInfoDTO responseEntity = restTemplate.postForObject(firmhighlightStr, request, FirmInfoDTO.class);


    return firmInfoDTO;
  }

  public List<FirmInfoDTO> saveAll(List<FirmInfoDTO> firmInfos) {

    HttpEntity<List<FirmInfoDTO>> request = new HttpEntity<>(firmInfos);

    FirmInfoDTO[] responseEntity = restTemplate.postForObject(firmhighlightStr, request, FirmInfoDTO[].class);


    return Arrays.asList(responseEntity.clone());

  }
}
