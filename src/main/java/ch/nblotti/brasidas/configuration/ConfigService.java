package ch.nblotti.brasidas.configuration;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;


@Service
public class ConfigService {


  @Value("${referential.config.baseurl}")
  private String firmQuoteStr;

  @Autowired
  private RestTemplate restTemplate;


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
}



