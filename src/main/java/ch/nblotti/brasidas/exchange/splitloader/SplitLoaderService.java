package ch.nblotti.brasidas.exchange.splitloader;

import ch.nblotti.brasidas.configuration.ConfigDTO;
import ch.nblotti.brasidas.configuration.JobStatus;
import ch.nblotti.brasidas.exchange.firmloader.MARKET_LOADER_EVENTS;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class SplitLoaderService {


  private static final int WORKER_THREAD_POOL = 1;
  private static final String LOADER = "LOADER";
  private static final String SPLIT_JOBS = "SPLIT_JOBS";


  @Value("${loader.job.max.running.time}")
  private long maxRunningTime;


  @Autowired
  private SplitConfigService splitConfigService;

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
  private StateMachine<MARKET_SPLIT_STATES, MARKET_SPLIT_EVENTS> splitStateMachine;

  @Autowired
  private RestTemplate externalShortRestTemplate;


  @PostConstruct
  private void init() {

    splitStateMachine.start();
  }


  public void startSplit(Integer startYear, Integer startMonth, Integer startDay, Integer endYear, Integer endMonth, Integer endDay) {


    Message<MARKET_LOADER_EVENTS> message;
    List<LocalDate> localDates = new ArrayList<>();


    LocalDate startDate = LocalDate.of(startYear, startMonth, startDay);
    LocalDate endDate = LocalDate.of(endYear, endMonth, endDay);

    for (LocalDate currentDate = startDate; currentDate.isBefore(endDate); currentDate = currentDate.plusDays(1)) {
      if (currentDate.isAfter(LocalDate.now().minusDays(1)))
        continue;

      if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY
        || currentDate.getDayOfWeek() == DayOfWeek.SUNDAY
        || wasDayBeforeRunDateDayDayOff(currentDate))
        continue;


      localDates.add(currentDate);
    }


    List<ConfigDTO> configDTOS = localDates.stream().map(filtred -> {

      ConfigDTO configDTO = new ConfigDTO();
      configDTO.setCode(LOADER);
      configDTO.setType(SPLIT_JOBS);
      configDTO.setValue(String.format(SplitConfigService.CONFIG_DTO_VALUE_STR, filtred.format(format1), JobStatus.SCHEDULED, LocalDateTime.now().format(formatMessage), 0));
      return configDTO;

    }).collect(Collectors.toList());

    splitConfigService.saveAll(configDTOS);
  }


  @Scheduled(cron = "${split.daily.cron.expression}")
  @Transactional
  public void scheduleDailyTask() {

    LocalDate runDate = LocalDate.now().minusDays(1);
    startSplit(runDate.getYear(), runDate.getMonthValue(), runDate.getDayOfMonth(), runDate.getYear(), runDate.getMonthValue(), runDate.getDayOfMonth());
  }

  @Scheduled(cron = "${split.recurring.cron.expression}")
  public void scheduleRecurringDelayTask() {

    List<ConfigDTO> configDTOS = splitConfigService.getAll(LOADER, SPLIT_JOBS);

    List<ConfigDTO> running = getJobsInGivenStatus(configDTOS, JobStatus.RUNNING);
    if (!running.isEmpty()) {
      running.stream().forEach(currentRunning -> {
        if (isHanging(currentRunning)) {
          if (splitConfigService.shouldRetry(currentRunning))
            currentRunning.setValue(String.format(SplitConfigService.CONFIG_DTO_VALUE_STR, splitConfigService.parseDate(currentRunning).format(format1), JobStatus.ERROR, LocalDateTime.now().format(formatMessage), splitConfigService.retryCount(currentRunning)));
          else
            currentRunning.setValue(String.format(SplitConfigService.CONFIG_DTO_VALUE_STR, splitConfigService.parseDate(currentRunning).format(format1), JobStatus.CANCELED, LocalDateTime.now().format(formatMessage), splitConfigService.retryCount(currentRunning)));

          splitConfigService.update(currentRunning);
        }

      });
      return;
    }

    List<ConfigDTO> errored = getJobsInGivenStatus(configDTOS, JobStatus.ERROR);
    if (!errored.isEmpty()) {
      errored.stream().forEach(configDTO -> cleanup(configDTO));

      errored.stream().forEach(currentErrored -> {

        if (!splitConfigService.shouldRetry(currentErrored)) {
          currentErrored.setValue(String.format(SplitConfigService.CONFIG_DTO_VALUE_STR, splitConfigService.parseDate(currentErrored).format(format1), JobStatus.CANCELED, LocalDateTime.now().format(formatMessage), splitConfigService.retryCount(currentErrored)));
          splitConfigService.update(currentErrored);
          return;
        }
        LocalDate runDate = splitConfigService.parseDate(currentErrored);

        Message<MARKET_SPLIT_EVENTS> message = MessageBuilder
          .withPayload(MARKET_SPLIT_EVENTS.EVENT_RECEIVED)
          .setHeader("runDate", runDate)
          .setHeader("splitId", currentErrored.getId())
          .build();


        splitStateMachine.sendEvent(message);
        return;

      });
      return;
    }
    List<ConfigDTO> toRun = getJobsInGivenStatus(configDTOS, JobStatus.SCHEDULED);
    if (toRun.isEmpty())
      return;

    ConfigDTO current = toRun.iterator().next();

    if (!splitConfigService.shouldRetry(current)) {
      current.setValue(String.format(SplitConfigService.CONFIG_DTO_VALUE_STR, splitConfigService.parseDate(current).format(format1), JobStatus.CANCELED, LocalDateTime.now().format(formatMessage), splitConfigService.retryCount(current)));
      splitConfigService.update(current);
      return;
    }

    LocalDate runDate = splitConfigService.parseDate(current);
    Message<MARKET_SPLIT_EVENTS> message = MessageBuilder
      .withPayload(MARKET_SPLIT_EVENTS.EVENT_RECEIVED)
      .setHeader("runDate", runDate)
      .setHeader("splitId", current.getId())
      .build();

    splitStateMachine.sendEvent(message);

  }

  private boolean isHanging(ConfigDTO current) {

    LocalDateTime lateUpdate = splitConfigService.parseUpdatedDate(current);
    LocalDateTime now = LocalDateTime.now();
    long minutes = ChronoUnit.MINUTES.between(lateUpdate, now);

    return minutes >= maxRunningTime;
  }


  private void cleanup(ConfigDTO configDTO) {
  }

  private List<ConfigDTO> getJobsInGivenStatus(List<ConfigDTO> configDTOS, JobStatus status) {

    return configDTOS.stream().filter(configDTO -> {

      return splitConfigService.isInGivenStatus(configDTO, status);
    }).collect(Collectors.toList());

  }


  private boolean wasDayBeforeRunDateDayDayOff(LocalDate runDate) {
    return false;
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
