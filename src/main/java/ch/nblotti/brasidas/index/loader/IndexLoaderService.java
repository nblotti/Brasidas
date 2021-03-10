package ch.nblotti.brasidas.index.loader;

import ch.nblotti.brasidas.configuration.ConfigDTO;
import ch.nblotti.brasidas.exchange.loader.MARKET_CLEANUP_EVENTS;
import ch.nblotti.brasidas.exchange.loader.MARKET_CLEANUP_STATES;
import ch.nblotti.brasidas.configuration.JobStatus;
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
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
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
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class IndexLoaderService {


  private static final int WORKER_THREAD_POOL = 1;
  private static final String LOADER = "LOADER";
  private static final String RUNNING_JOBS = "RUNNING_JOBS";


  @Autowired
  private LoadIndexConfigService loadIndexConfigService;

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
  private StateMachine<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> indexLoaderStateMachine;

  @Resource
  private StateMachine<INDEX_CLEANUP_STATES, INDEX_CLEANUP_EVENTS> marketCleanerStateMachine;

  @Value("${loader.job.max.running.time}")
  private long maxRunningTime;

  @Autowired
  private RestTemplate externalRestTemplate;


  @PostConstruct
  private void init() {

    indexLoaderStateMachine.start();
    marketCleanerStateMachine.start();
  }


  public void startLoad(Integer startYear, Integer startMonth, Integer startDay, Integer endYear, Integer endMonth, Integer endDay) {


    Message<INDEX_LOADER_EVENTS> message;
    List<LocalDate> localDates = new ArrayList<>();

    int localStartDay;
    if (startDay == null || startDay <= 0)
      localStartDay = 1;
    else
      localStartDay = startDay;

    int localEndDay = LocalDate.of(endYear, endMonth, 1).with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
    if (endDay <= localEndDay)
      localEndDay = endDay;


    for (int year = startYear; year <= endYear; year++) {

      int loopstartMonth = 1;
      int loopLastMonth = 12;

      if (year == endYear)
        loopLastMonth = endMonth;

      if (year == startYear)
        loopstartMonth = startMonth;

      for (int month = loopstartMonth; month <= loopLastMonth; month++) {
        LocalDate localDate = LocalDate.of(year, month, 1);
        localDate = localDate.withDayOfMonth(localDate.lengthOfMonth());

        int loopLastDay = 1;
        int loopStartDay = 1;

        loopLastDay = localDate.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();

        if (year >= endYear && month >= endMonth) {
          loopLastDay = localEndDay;
        }

        if (year == startYear && month == startMonth) {
          loopStartDay = localStartDay;
        }
        for (int day = loopStartDay; day <= loopLastDay; day++) {
          LocalDate runDate = localDate.withDayOfMonth(day);

          if (runDate.isAfter(LocalDate.now().minusDays(1)))
            return;

          localDates.add(runDate);


        }

        List<ConfigDTO> configDTOS = localDates.stream().filter(current -> {
            if (current.getDayOfWeek() != DayOfWeek.SATURDAY
              && current.getDayOfWeek() != DayOfWeek.SUNDAY
              && !wasDayBeforeRunDateDayDayOff(current))
              return true;
            return false;
          }
        ).map(filtred -> {

          ConfigDTO configDTO = new ConfigDTO();
          configDTO.setCode(LOADER);
          configDTO.setType(RUNNING_JOBS);
        //  configDTO.setValue(String.format(LoadIndexConfigService.CONFIG_DTO_VALUE_STR, filtred.format(format1), runPartial, JobStatus.SCHEDULED, LocalDateTime.now().format(formatMessage), 0));
          return configDTO;

        }).collect(Collectors.toList());

        loadIndexConfigService.saveAll(configDTOS);
      }
    }
  }


  //@Scheduled(cron = "${index.loader.monthly.cron.expression}")
  @Transactional
  public void scheduleDailyTask() {

    LocalDate runDate = LocalDate.now().minusDays(1);
    //startLoad(runDate.getYear(), runDate.getMonthValue(), runDate.getDayOfMonth(), runDate.getYear(), runDate.getMonthValue(), runDate.getDayOfMonth(), Boolean.FALSE);
  }

  //@Scheduled(cron = "${index.loader.recurring.cron.expression}")
  public void scheduleRecurringDelayTask() {


    if (isApiCallToElevated())
      return;

    if (true)
      return;

    List<ConfigDTO> configDTOS = loadIndexConfigService.getAll(LOADER, RUNNING_JOBS);

    List<ConfigDTO> running = getJobsInGivenStatus(configDTOS, JobStatus.RUNNING);
    if (!running.isEmpty()) {
      running.stream().forEach(currentRunning -> {
        if (isHanging(currentRunning)) {
          if (loadIndexConfigService.shouldRetry(currentRunning))
            currentRunning.setValue(String.format(LoadIndexConfigService.CONFIG_DTO_VALUE_STR, loadIndexConfigService.parseDate(currentRunning).format(format1), loadIndexConfigService.isPartial(currentRunning), JobStatus.ERROR, LocalDateTime.now().format(formatMessage), loadIndexConfigService.retryCount(currentRunning)));
          else
            currentRunning.setValue(String.format(LoadIndexConfigService.CONFIG_DTO_VALUE_STR, loadIndexConfigService.parseDate(currentRunning).format(format1), loadIndexConfigService.isPartial(currentRunning), JobStatus.CANCELED, LocalDateTime.now().format(formatMessage), loadIndexConfigService.retryCount(currentRunning)));

          loadIndexConfigService.update(currentRunning);
        }

      });
      return;
    }

    List<ConfigDTO> errored = getJobsInGivenStatus(configDTOS, JobStatus.ERROR);
    if (!errored.isEmpty()) {
      errored.stream().forEach(configDTO -> cleanup(configDTO));

      errored.stream().forEach(currentErrored -> {

        if (!loadIndexConfigService.shouldRetry(currentErrored)) {
          currentErrored.setValue(String.format(LoadIndexConfigService.CONFIG_DTO_VALUE_STR, loadIndexConfigService.parseDate(currentErrored).format(format1), loadIndexConfigService.isPartial(currentErrored), JobStatus.CANCELED, LocalDateTime.now().format(formatMessage), loadIndexConfigService.retryCount(currentErrored)));
          loadIndexConfigService.update(currentErrored);
          return;
        }
        LocalDate runDate = loadIndexConfigService.parseDate(currentErrored);

        Message<MARKET_CLEANUP_EVENTS> message = MessageBuilder
          .withPayload(MARKET_CLEANUP_EVENTS.EVENT_RECEIVED)
          .setHeader("runDate", runDate)
          .setHeader("erroredId", currentErrored.getId())
          .build();


     //   marketCleanerStateMachine.sendEvent(message);
        return;

      });
      return;
    }
    List<ConfigDTO> toRun = getJobsInGivenStatus(configDTOS, JobStatus.SCHEDULED);
    if (toRun.isEmpty())
      return;

    ConfigDTO current = toRun.iterator().next();

    if (!loadIndexConfigService.shouldRetry(current)) {
      current.setValue(String.format(LoadIndexConfigService.CONFIG_DTO_VALUE_STR, loadIndexConfigService.parseDate(current).format(format1), loadIndexConfigService.isPartial(current), JobStatus.CANCELED, LocalDateTime.now().format(formatMessage), loadIndexConfigService.retryCount(current)));
      loadIndexConfigService.update(current);
      return;
    }


    LocalDate runDate = loadIndexConfigService.parseDate(current);
    Message<INDEX_LOADER_EVENTS> message = MessageBuilder
      .withPayload(INDEX_LOADER_EVENTS.EVENT_RECEIVED)
      .setHeader("runDate", runDate)
      .setHeader("loadId", current.getId())
      .build();

    indexLoaderStateMachine.sendEvent(message);

  }

  private boolean isHanging(ConfigDTO current) {

    LocalDateTime lateUpdate = loadIndexConfigService.parseUpdatedDate(current);
    LocalDateTime now = LocalDateTime.now();
    long minutes = ChronoUnit.MINUTES.between(lateUpdate, now);

    return minutes >= maxRunningTime;
  }


  private void cleanup(ConfigDTO configDTO) {
  }

  private List<ConfigDTO> getJobsInGivenStatus(List<ConfigDTO> configDTOS, JobStatus status) {

    return configDTOS.stream().filter(configDTO -> {

      return loadIndexConfigService.isInGivenStatus(configDTO, status);
    }).collect(Collectors.toList());

  }


  private boolean wasDayBeforeRunDateDayDayOff(LocalDate runDate) {
    return false;
  }

  private boolean isApiCallToElevated() {

    try {
      ResponseEntity<String> resultJson = externalRestTemplate.getForEntity(String.format(apiStatus, apiKey), String.class);
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