package ch.nblotti.brasidas.configuration;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;


public abstract class ConfigService {


  public static final String CONFIG_DTO_VALUE_STR = "{\"date\":\"%s\",\"partial\":\"%s\",\"status\":\"%s\",\"updated\":\"%s\",\"retry\":\"%s\"}";

  private String runningSatusStr = "$..status";
  private String runningDateStr = "$..date";
  private String updatedDateStr = "$..updated";
  private String runningPartialStr = "$..partial";
  private String shouldRetryStr = "$..retry";


  @Value("${referential.config.baseurl}")
  private String firmQuoteStr;

  @Value("${loader.job.max.retry}")
  private int maxRetry;

  @Autowired
  private RestTemplate internalRestTemplate;


  @Autowired
  private DateTimeFormatter format1;

  @Autowired
  private DateTimeFormatter formatMessage;


  public ConfigDTO findById(Long id) {


    ResponseEntity<ConfigDTO> returned = internalRestTemplate.getForEntity(String.format("%sid?id=%s", firmQuoteStr, id), ConfigDTO.class);

    return returned.getBody();
  }


  public ConfigDTO save(ConfigDTO configDTO) {

    HttpEntity<ConfigDTO> request = new HttpEntity<ConfigDTO>(configDTO);

    return internalRestTemplate.postForObject(firmQuoteStr, request, ConfigDTO.class);
  }

  public void update(ConfigDTO configDTO) {

    HttpEntity<ConfigDTO> request = new HttpEntity<ConfigDTO>(configDTO);
    internalRestTemplate.put(firmQuoteStr, request);
  }


  public List<ConfigDTO> getAll(String code, String type) {

    String url = String.format("%s?code=%s&type=%s", firmQuoteStr, code, type);
    ConfigDTO[] responseEntity = internalRestTemplate.getForObject(url, ConfigDTO[].class);

    return Arrays.asList(responseEntity);

  }


  public List<ConfigDTO> saveAll(List<ConfigDTO> configDTOS) {

    configDTOS.forEach(configDTO -> save(configDTO));

    return configDTOS;
  }

}


