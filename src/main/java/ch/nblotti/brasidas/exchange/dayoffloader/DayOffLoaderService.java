package ch.nblotti.brasidas.exchange.dayoffloader;

import ch.nblotti.brasidas.configuration.ConfigDTO;
import ch.nblotti.brasidas.configuration.JobStatus;
import ch.nblotti.brasidas.exchange.splitloader.MARKET_SPLIT_EVENTS;
import ch.nblotti.brasidas.exchange.splitloader.SplitConfigService;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class DayOffLoaderService {


  private static final int WORKER_THREAD_POOL = 1;
  private static final String LOADER = "LOADER";
  private static final String DAYOFF_JOBS = "DAYOFF_JOBS";


  @Value("${loader.job.max.running.time}")
  private long maxRunningTime;


  @Autowired
  private DayOffConfigService dayOffConfigService;

  @Autowired
  private DateTimeFormatter format1;

  @Autowired
  private DateTimeFormatter formatMessage;


  @Autowired
  private BeanFactory beanFactory;

  @Value("${spring.application.eod.api.key}")
  protected String apiKey;


  @Value("${spring.application.eod.api.satus}")
  private String apiStatus;


  private String apiLevelStr = "$..apiRequests";

  @Resource
  private StateMachine<DAYOFF_STATES, DAYOFF_EVENTS> dayOffStateMachine;

  @Autowired
  private RestTemplate externalShortRestTemplate;


  @PostConstruct
  private void init() {

    dayOffStateMachine.start();
  }


  public void startDayOff(Integer year) {





      ConfigDTO configDTO = new ConfigDTO();
      configDTO.setCode(LOADER);
      configDTO.setType(DAYOFF_JOBS);
      configDTO.setValue(String.format(DayOffConfigService.CONFIG_DTO_VALUE_STR, year, JobStatus.SCHEDULED, LocalDateTime.now().format(formatMessage), 0));

    dayOffConfigService.save(configDTO);
  }


  //@Scheduled(cron = "${dayoff.yearly.cron.expression}")
  @Transactional
  public void scheduleDailyTask() {

    LocalDate runDate = LocalDate.now().minusDays(1);
    startDayOff(runDate.getYear() +1);
  }

  @Scheduled(cron = "${dayoff.recurring.cron.expression}")
  public void scheduleRecurringDelayTask() {

    List<ConfigDTO> configDTOS = dayOffConfigService.getAll(LOADER, DAYOFF_JOBS);

    List<ConfigDTO> running = getJobsInGivenStatus(configDTOS, JobStatus.RUNNING);
    if (!running.isEmpty()) {
      running.stream().forEach(currentRunning -> {
        if (isHanging(currentRunning)) {
          if (dayOffConfigService.shouldRetry(currentRunning))
            currentRunning.setValue(String.format(DayOffConfigService.CONFIG_DTO_VALUE_STR, dayOffConfigService.parsedayOffYear(currentRunning),  JobStatus.ERROR, LocalDateTime.now().format(formatMessage), dayOffConfigService.retryCount(currentRunning)));
          else
            currentRunning.setValue(String.format(DayOffConfigService.CONFIG_DTO_VALUE_STR, dayOffConfigService.parsedayOffYear(currentRunning), JobStatus.CANCELED, LocalDateTime.now().format(formatMessage), dayOffConfigService.retryCount(currentRunning)));

          dayOffConfigService.update(currentRunning);
        }

      });
      return;
    }

    List<ConfigDTO> errored = getJobsInGivenStatus(configDTOS, JobStatus.ERROR);
    if (!errored.isEmpty()) {

      errored.forEach(currentErrored -> {

        if (!dayOffConfigService.shouldRetry(currentErrored)) {
          currentErrored.setValue(String.format(SplitConfigService.CONFIG_DTO_VALUE_STR, dayOffConfigService.parsedayOffYear(currentErrored), JobStatus.CANCELED, LocalDateTime.now().format(formatMessage), dayOffConfigService.retryCount(currentErrored)));
          dayOffConfigService.update(currentErrored);
          return;
        }

        Message<DAYOFF_EVENTS> message = MessageBuilder
          .withPayload(DAYOFF_EVENTS.EVENT_RECEIVED)
          .setHeader("dayOffId", currentErrored.getId())
          .build();


        dayOffStateMachine.sendEvent(message);
        return;

      });
      return;
    }
    List<ConfigDTO> toRun = getJobsInGivenStatus(configDTOS, JobStatus.SCHEDULED);
    if (toRun.isEmpty())
      return;

    ConfigDTO current = toRun.iterator().next();

    if (!dayOffConfigService.shouldRetry(current)) {
      current.setValue(String.format(SplitConfigService.CONFIG_DTO_VALUE_STR, dayOffConfigService.parsedayOffYear(current), JobStatus.CANCELED, LocalDateTime.now().format(formatMessage), dayOffConfigService.retryCount(current)));
      dayOffConfigService.update(current);
      return;
    }

    Message<DAYOFF_EVENTS> message = MessageBuilder
      .withPayload(DAYOFF_EVENTS.EVENT_RECEIVED)
      .setHeader("dayOffId", current.getId())
      .build();

    dayOffStateMachine.sendEvent(message);

  }

  private boolean isHanging(ConfigDTO current) {

    LocalDateTime lateUpdate = dayOffConfigService.parseUpdatedDate(current);
    LocalDateTime now = LocalDateTime.now();
    long minutes = ChronoUnit.MINUTES.between(lateUpdate, now);

    return minutes >= maxRunningTime;
  }



  private List<ConfigDTO> getJobsInGivenStatus(List<ConfigDTO> configDTOS, JobStatus status) {

    return configDTOS.stream().filter(configDTO -> {

      return dayOffConfigService.isInGivenStatus(configDTO, status);
    }).collect(Collectors.toList());

  }



  private boolean isApiCallToElevated() {

    try {
      ResponseEntity<String> resultJson = externalShortRestTemplate.getForEntity(String.format(apiStatus, apiKey), String.class);
      if (resultJson.getStatusCode() != HttpStatus.OK)
        return true;


      DocumentContext content = JsonPath.parse(resultJson.getBody());
      JSONArray json = content.read(apiLevelStr);
      String type = json.get(0).toString();
      if (type != null && Integer.parseInt(type) > 85000)
        return true;
    } catch (Exception ex) {
      log.error(ex.getMessage());
    }
    return false;
  }

}
