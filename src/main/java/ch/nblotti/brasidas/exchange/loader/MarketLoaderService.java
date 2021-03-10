package ch.nblotti.brasidas.exchange.loader;

import ch.nblotti.brasidas.configuration.ConfigDTO;
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
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Service
@Slf4j
public class MarketLoaderService {


  private static final int WORKER_THREAD_POOL = 1;
  private static final String LOADER = "LOADER";
  private static final String RUNNING_JOBS = "RUNNING_JOBS";


  @Autowired
  private MarketLoadConfigService marketLoadConfigService;

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
  private StateMachine<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> marketLoaderStateMachine;

  @Resource
  private StateMachine<MARKET_CLEANUP_STATES, MARKET_CLEANUP_EVENTS> marketCleanerStateMachine;

  @Value("${loader.job.max.running.time}")
  private long maxRunningTime;

  @Autowired
  private RestTemplate externalRestTemplate;


  @PostConstruct
  private void init() {

    marketLoaderStateMachine.addStateListener(new StateMachineListener<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS>() {
      @Override
      public void stateChanged(State<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> state, State<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> state1) {

      }

      @Override
      public void stateEntered(State<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> state) {

        if (state == null)
          log.info(String.format("Loader - state changed. entering  %s ", state.getId()));
      }

      @Override
      public void stateExited(State<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> state) {

        if (state == null)
          log.info(String.format("Loader - state changed. exited  %s ", state.getId()));
      }

      @Override
      public void eventNotAccepted(Message<MARKET_LOADER_EVENTS> message) {

      }

      @Override
      public void transition(Transition<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> transition) {

      }

      @Override
      public void transitionStarted(Transition<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> transition) {

      }

      @Override
      public void transitionEnded(Transition<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> transition) {

      }

      @Override
      public void stateMachineStarted(StateMachine<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> stateMachine) {
      }

      @Override
      public void stateMachineStopped(StateMachine<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> stateMachine) {

      }

      @Override
      public void stateMachineError(StateMachine<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> stateMachine, Exception e) {

      }

      @Override
      public void extendedStateChanged(Object o, Object o1) {

      }

      @Override
      public void stateContext(StateContext<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> stateContext) {

      }
    });
    marketLoaderStateMachine.start();
    marketCleanerStateMachine.start();
  }


  public void startLoad(Integer startYear, Integer startMonth, Integer startDay, Integer endYear, Integer endMonth, Integer endDay, Boolean runPartial) {


    Message<MARKET_LOADER_EVENTS> message;
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
          configDTO.setValue(String.format(MarketLoadConfigService.CONFIG_DTO_VALUE_STR, filtred.format(format1), runPartial, JobStatus.SCHEDULED, LocalDateTime.now().format(formatMessage),0));
          return configDTO;

        }).collect(Collectors.toList());

        marketLoadConfigService.saveAll(configDTOS);
      }
    }
  }


  @Scheduled(cron = "${market.loader.daily.cron.expression}")
  @Transactional
  public void scheduleDailyTask() {

    LocalDate runDate = LocalDate.now().minusDays(1);
    startLoad(runDate.getYear(), runDate.getMonthValue(), runDate.getDayOfMonth(), runDate.getYear(), runDate.getMonthValue(), runDate.getDayOfMonth(), Boolean.FALSE);
  }

  @Scheduled(cron = "${market.loader.recurring.cron.expression}")
  public void scheduleRecurringDelayTask() {


    if(   isApiCallToElevated())
      return;


    List<ConfigDTO> configDTOS = marketLoadConfigService.getAll(LOADER, RUNNING_JOBS);

    List<ConfigDTO> running = getJobsInGivenStatus(configDTOS, JobStatus.RUNNING);
    if (!running.isEmpty()) {
      running.stream().forEach(currentRunning -> {
        if (isHanging(currentRunning)) {
          if(marketLoadConfigService.shouldRetry(currentRunning))
            currentRunning.setValue(String.format(MarketLoadConfigService.CONFIG_DTO_VALUE_STR, marketLoadConfigService.parseDate(currentRunning).format(format1), marketLoadConfigService.isPartial(currentRunning), JobStatus.ERROR, LocalDateTime.now().format(formatMessage), marketLoadConfigService.retryCount(currentRunning)));
          else
            currentRunning.setValue(String.format(MarketLoadConfigService.CONFIG_DTO_VALUE_STR, marketLoadConfigService.parseDate(currentRunning).format(format1), marketLoadConfigService.isPartial(currentRunning), JobStatus.CANCELED, LocalDateTime.now().format(formatMessage), marketLoadConfigService.retryCount(currentRunning)));

          marketLoadConfigService.update(currentRunning);
        }

      });
      return;
    }

    List<ConfigDTO> errored = getJobsInGivenStatus(configDTOS, JobStatus.ERROR);
    if (!errored.isEmpty()) {
      errored.stream().forEach(configDTO -> cleanup(configDTO));

      errored.stream().forEach(currentErrored -> {

        if(!marketLoadConfigService.shouldRetry(currentErrored)) {
          currentErrored.setValue(String.format(MarketLoadConfigService.CONFIG_DTO_VALUE_STR, marketLoadConfigService.parseDate(currentErrored).format(format1), marketLoadConfigService.isPartial(currentErrored), JobStatus.CANCELED, LocalDateTime.now().format(formatMessage), marketLoadConfigService.retryCount(currentErrored) ));
          marketLoadConfigService.update(currentErrored);
          return;
        }
        LocalDate runDate = marketLoadConfigService.parseDate(currentErrored);

        Message<MARKET_CLEANUP_EVENTS> message = MessageBuilder
          .withPayload(MARKET_CLEANUP_EVENTS.EVENT_RECEIVED)
          .setHeader("runDate", runDate)
          .setHeader("erroredId", currentErrored.getId())
          .build();


        marketCleanerStateMachine.sendEvent(message);
        return;

      });
      return;
    }
    List<ConfigDTO> toRun = getJobsInGivenStatus(configDTOS, JobStatus.SCHEDULED);
    if (toRun.isEmpty())
      return;

    ConfigDTO current = toRun.iterator().next();

    if(!marketLoadConfigService.shouldRetry(current)) {
      current.setValue(String.format(MarketLoadConfigService.CONFIG_DTO_VALUE_STR, marketLoadConfigService.parseDate(current).format(format1), marketLoadConfigService.isPartial(current), JobStatus.CANCELED, LocalDateTime.now().format(formatMessage), marketLoadConfigService.retryCount(current) ));
      marketLoadConfigService.update(current);
      return;
    }


    LocalDate runDate = marketLoadConfigService.parseDate(current);
    Message<MARKET_LOADER_EVENTS> message = MessageBuilder
      .withPayload(MARKET_LOADER_EVENTS.EVENT_RECEIVED)
      .setHeader("runDate", runDate)
      .setHeader("loadId", current.getId())
      .build();

    marketLoaderStateMachine.sendEvent(message);

  }

  private boolean isHanging(ConfigDTO current) {

    LocalDateTime lateUpdate = marketLoadConfigService.parseUpdatedDate(current);
    LocalDateTime now = LocalDateTime.now();
    long minutes = ChronoUnit.MINUTES.between(lateUpdate, now);

    return minutes >= maxRunningTime;
  }



  private void cleanup(ConfigDTO configDTO) {
  }

  private List<ConfigDTO> getJobsInGivenStatus(List<ConfigDTO> configDTOS, JobStatus status) {

    return configDTOS.stream().filter(configDTO -> {

      return marketLoadConfigService.isInGivenStatus(configDTO, status);
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
      if (type != null && Integer.parseInt(type) > 80000)
        return true;
    } catch (Exception ex) {
      log.error(ex.getMessage());
    }
    return false;
  }

}