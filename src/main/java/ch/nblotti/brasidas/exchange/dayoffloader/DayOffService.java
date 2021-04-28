package ch.nblotti.brasidas.exchange.dayoffloader;


import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;


@Slf4j
@Service
public class DayOffService {


  @Autowired
  protected ModelMapper modelMapper;

  @Autowired
  protected DateTimeFormatter format1;

  @Autowired
  private EODDayOffRepository eODDayOffRepository;

  @Autowired
  private RestTemplate internalRestTemplate;


  @Value("${referential.dayoff.baseurl}")
  private String dayOffStr;

  @Value("${referential.time.baseurl}")
  private String timeStr;


  public List<DayOffDTO> getDayOffForYearSplitByDate(String exchange, int year) {


    List<EODDayOffDTO> splits = eODDayOffRepository.getDayOff(LocalDate.of(year, 1, 1), LocalDate.of(year + 1, 1, 1), exchange);

    return splits.stream().map(x -> modelMapper.map(x, DayOffDTO.class)).collect(Collectors.toList());

  }


  @PostConstruct
  void initFirmQuoteMapper() {

    Converter<EODDayOffDTO, DayOffDTO> toUppercase = new AbstractConverter<EODDayOffDTO, DayOffDTO>() {

      @Override
      protected DayOffDTO convert(EODDayOffDTO eODDayOffDTO) {
        DayOffDTO dayOffDTO = new DayOffDTO();
        dayOffDTO.setName(eODDayOffDTO.getName());
        dayOffDTO.setCode(eODDayOffDTO.getCode());
        dayOffDTO.setMics(eODDayOffDTO.getMics());
        dayOffDTO.setCountry(eODDayOffDTO.getCountry());
        dayOffDTO.setCurrency(eODDayOffDTO.getCurrency());
        dayOffDTO.setTimezone(eODDayOffDTO.getTimezone());
        dayOffDTO.setDate(eODDayOffDTO.getDate());
        dayOffDTO.setHoliday(eODDayOffDTO.getHoliday());
        return dayOffDTO;
      }
    };

    modelMapper.addConverter(toUppercase);

  }

  public DayOffDTO saveEODDayOff(DayOffDTO s) {

    HttpEntity<DayOffDTO> request = new HttpEntity<DayOffDTO>(s);

    return internalRestTemplate.postForObject(dayOffStr, request, DayOffDTO.class);

  }

  public TimeDTO saveTimeDto(TimeDTO s) {

    HttpEntity<TimeDTO> request = new HttpEntity<>(s);

    return internalRestTemplate.postForObject(timeStr, request, TimeDTO.class);
  }
}
