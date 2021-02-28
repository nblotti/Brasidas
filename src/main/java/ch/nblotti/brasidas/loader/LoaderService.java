package ch.nblotti.brasidas.loader;

import ch.nblotti.brasidas.configuration.ConfigDTO;
import ch.nblotti.brasidas.configuration.ConfigService;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
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
public class LoaderService {


  private static final Logger logger = Logger.getLogger("LoaderService");

  private static final int WORKER_THREAD_POOL = 1;
  private static final String LOADER = "LOADER";
  private static final String RUNNING_JOBS = "RUNNING_JOBS";


  @Autowired
  private ConfigService configService;

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
  private StateMachine<LOADER_STATES, LOADER_EVENTS> marketLoaderStateMachine;

  @Resource
  private StateMachine<CLEANUP_STATES, CLEANUP_EVENTS> marketCleanerStateMachine;

  @Value("${loader.job.max.running.time}")
  private long maxRunningTime;

  @Autowired
  private RestTemplate externalRestTemplate;


  @PostConstruct
  private void init() {

    marketLoaderStateMachine.addStateListener(new StateMachineListener<LOADER_STATES, LOADER_EVENTS>() {
      @Override
      public void stateChanged(State<LOADER_STATES, LOADER_EVENTS> state, State<LOADER_STATES, LOADER_EVENTS> state1) {

      }

      @Override
      public void stateEntered(State<LOADER_STATES, LOADER_EVENTS> state) {

        if (state == null)
          logger.info(String.format("Loader - state changed. entering  %s ", state.getId()));
      }

      @Override
      public void stateExited(State<LOADER_STATES, LOADER_EVENTS> state) {

        if (state == null)
          logger.info(String.format("Loader - state changed. exited  %s ", state.getId()));
      }

      @Override
      public void eventNotAccepted(Message<LOADER_EVENTS> message) {

      }

      @Override
      public void transition(Transition<LOADER_STATES, LOADER_EVENTS> transition) {

      }

      @Override
      public void transitionStarted(Transition<LOADER_STATES, LOADER_EVENTS> transition) {

      }

      @Override
      public void transitionEnded(Transition<LOADER_STATES, LOADER_EVENTS> transition) {

      }

      @Override
      public void stateMachineStarted(StateMachine<LOADER_STATES, LOADER_EVENTS> stateMachine) {
      }

      @Override
      public void stateMachineStopped(StateMachine<LOADER_STATES, LOADER_EVENTS> stateMachine) {

      }

      @Override
      public void stateMachineError(StateMachine<LOADER_STATES, LOADER_EVENTS> stateMachine, Exception e) {

      }

      @Override
      public void extendedStateChanged(Object o, Object o1) {

      }

      @Override
      public void stateContext(StateContext<LOADER_STATES, LOADER_EVENTS> stateContext) {

      }
    });
    marketLoaderStateMachine.start();
    marketCleanerStateMachine.start();
  }


  public void startLoad(Integer startYear, Integer startMonth, Integer startDay, Integer endYear, Integer endMonth, Integer endDay, Boolean runPartial) {


    Message<LOADER_EVENTS> message;
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
              && !wasDayBeforeRunDateDayDayOff(current)
              && !isApiCallToElevated())
              return true;
            return false;
          }
        ).map(filtred -> {

          ConfigDTO configDTO = new ConfigDTO();
          configDTO.setCode(LOADER);
          configDTO.setType(RUNNING_JOBS);
          configDTO.setValue(String.format(ConfigService.CONFIG_DTO_VALUE_STR, filtred.format(format1), runPartial, JobStatus.SCHEDULED, LocalDateTime.now().format(formatMessage)));
          return configDTO;

        }).collect(Collectors.toList());

        configService.saveAll(configDTOS);
      }
    }
  }


  @Scheduled(cron = "${loader.daily.cron.expression}")
  @Transactional
  public void scheduleDailyTask() {

    LocalDate runDate = LocalDate.now().minusDays(1);
    startLoad(runDate.getYear(), runDate.getMonthValue(), runDate.getDayOfMonth(), runDate.getYear(), runDate.getMonthValue(), runDate.getDayOfMonth(), Boolean.FALSE);
  }

  //@Scheduled(cron = "${loader.recurring.cron.expression}")
  public void scheduleRecurringDelayTask() {

    List<ConfigDTO> configDTOS = configService.getAll(LOADER, RUNNING_JOBS);

    List<ConfigDTO> running = getJobsInGivenStatus(configDTOS, JobStatus.RUNNING);
    if (!running.isEmpty()) {
      running.stream().forEach(currentRunning -> {
        if (isHanging(currentRunning)) {
          currentRunning.setValue(String.format(ConfigService.CONFIG_DTO_VALUE_STR, configService.parseDate(currentRunning).format(format1), configService.isPartial(currentRunning), JobStatus.ERROR, LocalDateTime.now().format(formatMessage)));
          configService.update(currentRunning);
        }

      });
      return;
    }

    List<ConfigDTO> errored = getJobsInGivenStatus(configDTOS, JobStatus.ERROR);
    if (!errored.isEmpty()) {
      errored.stream().forEach(configDTO -> cleanup(configDTO));

      errored.stream().forEach(currentErrored -> {

        LocalDate runDate = configService.parseDate(currentErrored);



        Message<CLEANUP_EVENTS> message = MessageBuilder
          .withPayload(CLEANUP_EVENTS.EVENT_RECEIVED)
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

    LocalDate runDate = configService.parseDate(current);
    Message<LOADER_EVENTS> message = MessageBuilder
      .withPayload(LOADER_EVENTS.EVENT_RECEIVED)
      .setHeader("runDate", runDate)
      .setHeader("loadId", current.getId())
      .build();

    marketLoaderStateMachine.sendEvent(message);

  }

  private boolean isHanging(ConfigDTO current) {

    LocalDateTime lateUpdate = configService.parseUpdatedDate(current);
    LocalDateTime now = LocalDateTime.now();
    long minutes = ChronoUnit.MINUTES.between(lateUpdate, now);

    return minutes >= maxRunningTime;
  }


  private void cleanup(ConfigDTO configDTO) {
  }

  private List<ConfigDTO> getJobsInGivenStatus(List<ConfigDTO> configDTOS, JobStatus status) {

    return configDTOS.stream().filter(configDTO -> {

      return configService.isInGivenStatus(configDTO, status);
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
      logger.severe(ex.getMessage());
    }
    return false;
  }

}
