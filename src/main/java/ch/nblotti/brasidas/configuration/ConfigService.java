package ch.nblotti.brasidas.configuration;


import ch.nblotti.brasidas.loader.JobStatus;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;


@Service
@Transactional
public class ConfigService {


  public static final String CONFIG_DTO_VALUE_STR = "{\"date\":\"%s\",\"partial\":\"%s\",\"status\":\"%s\",\"updated\":\"%s\"}";

  private String runningSatusStr = "$..status";
  private String runningDateStr = "$..date";
  private String updatedDateStr = "$..updated";
  private String runningPartialStr = "$..partial";


  @Value("${referential.config.baseurl}")
  private String firmQuoteStr;

  @Autowired
  private RestTemplate restTemplate;


  @Autowired
  private DateTimeFormatter format1;

  @Autowired
  private DateTimeFormatter formatMessage;


  public ConfigDTO findById(Long id) {


    ResponseEntity<ConfigDTO> returned = restTemplate.getForEntity(String.format("%sid?id=%s", firmQuoteStr, id), ConfigDTO.class);

    return returned.getBody();
  }


  public ConfigDTO save(ConfigDTO configDTO) {

    HttpEntity<ConfigDTO> request = new HttpEntity<ConfigDTO>(configDTO);

    return restTemplate.postForObject(firmQuoteStr, request, ConfigDTO.class);
  }

  public void update(ConfigDTO configDTO) {

    HttpEntity<ConfigDTO> request = new HttpEntity<ConfigDTO>(configDTO);
    restTemplate.put(firmQuoteStr, request);
  }


  public List<ConfigDTO> getAll(String code, String type) {

    String url = String.format("%s?code=%s&type=%s", firmQuoteStr, code, type);
    ConfigDTO[] responseEntity = restTemplate.getForObject(url, ConfigDTO[].class);

    return Arrays.asList(responseEntity);

  }


  public List<ConfigDTO> saveAll(List<ConfigDTO> configDTOS) {

    configDTOS.forEach(configDTO -> save(configDTO));

    return configDTOS;
  }

  public boolean isInGivenStatus(ConfigDTO configDTO, JobStatus status) {
    DocumentContext content = JsonPath.parse(configDTO.getValue());
    JSONArray json = content.read(runningSatusStr);

    String type = json.get(0).toString();

    if (type != null && type.contains(status.toString()))
      return true;
    return false;
  }

  public LocalDate parseDate(ConfigDTO configDTO) {
    DocumentContext content = JsonPath.parse(configDTO.getValue());
    JSONArray json = content.read(runningDateStr);
    String type = json.get(0).toString();
    if (type != null)
      return LocalDate.parse(type, format1);
    return null;
  }

  public LocalDateTime parseUpdatedDate(ConfigDTO configDTO) {
    DocumentContext content = JsonPath.parse(configDTO.getValue());
    JSONArray json = content.read(updatedDateStr);
    String type = json.get(0).toString();
    if (type != null)
      return LocalDateTime.parse(type, formatMessage);
    return null;
  }

  public boolean isPartial(ConfigDTO configDTO) {
    DocumentContext content = JsonPath.parse(configDTO.getValue());
    JSONArray json = content.read(runningPartialStr);
    String type = json.get(0).toString();
    if (type != null && type.compareToIgnoreCase("true") == 0)
      return true;
    return false;
  }


}



