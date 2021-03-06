package ch.nblotti.brasidas.exchange.splitloader;


import ch.nblotti.brasidas.configuration.ConfigDTO;
import ch.nblotti.brasidas.configuration.ConfigService;
import ch.nblotti.brasidas.configuration.JobStatus;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Service
@Transactional
public class SplitConfigService extends ConfigService {


  public static final String CONFIG_DTO_VALUE_STR = "{\"date\":\"%s\",\"status\":\"%s\",\"updated\":\"%s\",\"retry\":\"%s\"}";

  private String runningSatusStr = "$..status";
  private String runningDateStr = "$..date";
  private String updatedDateStr = "$..updated";

  private String shouldRetryStr = "$..retry";



  @Value("${loader.job.max.retry}")
  private int maxRetry;


  @Autowired
  private DateTimeFormatter format1;

  @Autowired
  private DateTimeFormatter formatMessage;


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

  public int retryCount(ConfigDTO configDTO) {

    DocumentContext content = JsonPath.parse(configDTO.getValue());
    JSONArray json = content.read(shouldRetryStr);
    String type = json.get(0).toString();

    try {
      return Integer.parseInt(type);
    } catch (NumberFormatException nf) {
      return 1;
    }
  }

  public boolean shouldRetry(ConfigDTO configDTO) {

    return retryCount(configDTO) < maxRetry;

  }
}


