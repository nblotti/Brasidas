package ch.nblotti.brasidas.loader;

import ch.nblotti.brasidas.configuration.ConfigDTO;
import ch.nblotti.brasidas.configuration.ConfigService;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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


@RestController
@RequestMapping("/loader")
public class LoaderController {


  private static final Logger logger = Logger.getLogger("LoaderController");

  private static final int WORKER_THREAD_POOL = 1;
  public static final String LOADER = "LOADER";
  public static final String RUNNING_JOBS = "RUNNING_JOBS";
  public static final String CONFIG_DTO_VALUE_STR = "{\"date\":\"%s\",\"partial\":\"%s\",\"status\":\"%s\",\"updated\":\"%s\"}";

  @Autowired
  private ConfigService configService;

  @Autowired
  private DateTimeFormatter format1;

  @Autowired
  private DateTimeFormatter formatMessage;


  @Autowired
  private BeanFactory beanFactory;


  public String runningSatusStr = "$..status";
  public String runningDateStr = "$..date";
  public String updatedDateStr = "$..updated";
  public String runningPartialStr = "$..partial";

  @Resource
  private StateMachine<LOADER_STATES, LOADER_EVENTS> sp500LoaderStateMachine;

  @Value("${loader.job.max.running.time}")
  private long maxRunningTime;


  @PostConstruct
  public void init() {

    sp500LoaderStateMachine.addStateListener(new StateMachineListener<LOADER_STATES, LOADER_EVENTS>() {
      @Override
      public void stateChanged(State<LOADER_STATES, LOADER_EVENTS> state, State<LOADER_STATES, LOADER_EVENTS> state1) {

      }

      @Override
      public void stateEntered(State<LOADER_STATES, LOADER_EVENTS> state) {

        if (state == null)
          logger.info(String.format("State Changed. entering  %s ", state.getId()));
      }

      @Override
      public void stateExited(State<LOADER_STATES, LOADER_EVENTS> state) {

        if (state == null)
          logger.info(String.format("State Changed. exited  %s ", state.getId()));
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
    sp500LoaderStateMachine.start();
  }

  @PostMapping(value = "/load")
  public void load(@RequestParam(name = "startyear", required = true) Integer startYear,
                   @RequestParam(name = "endyear", required = false) Integer endYear,
                   @RequestParam(name = "startmonth", required = true) Integer startMonth,
                   @RequestParam(name = "endmonth", required = false) Integer endMonth,
                   @RequestParam(name = "startday", required = false) Integer startDay,
                   @RequestParam(name = "endday", required = false) Integer endDay) {

    if (endYear == null || endYear == 0)
      endYear = startYear;

    if (startYear < 2000 || startYear > endYear)
      throw new IllegalArgumentException("Start year cannot be bigger than end year");


    if (endMonth == null || endMonth == 0)
      endMonth = startMonth;

    if (endYear == null || endYear == 0)
      endYear = startYear;

    if (startMonth > 12 || endMonth > 12) {
      throw new IllegalArgumentException("End month or start month cannot be bigger than 12. start month cannot be bigger than end month");
    }

    startLoad(startYear, startMonth, startDay, endYear, endMonth, endDay, Boolean.TRUE);
  }

  @PostMapping(value = "/loadall")
  public void loadAll(@RequestParam(name = "startyear", required = true) Integer startYear,
                      @RequestParam(name = "endyear", required = false) Integer endYear,
                      @RequestParam(name = "startmonth", required = true) Integer startMonth,
                      @RequestParam(name = "endmonth", required = false) Integer endMonth,
                      @RequestParam(name = "startday", required = false) Integer startDay,
                      @RequestParam(name = "endday", required = false) Integer endDay) {

    if (endYear == null || endYear == 0)
      endYear = startYear;

    if (startYear < 2000 || startYear > endYear)
      throw new IllegalArgumentException("Start year cannot be bigger than end year");


    if (endMonth == null || endMonth == 0)
      endMonth = startMonth;

    if (endYear == null || endYear == 0)
      endYear = startYear;

    if (startMonth > 12 || endMonth > 12) {
      throw new IllegalArgumentException("End month or start month cannot be bigger than 12. start month cannot be bigger than end month");
    }

    startLoad(startYear, startMonth, startDay, endYear, endMonth, endDay, Boolean.FALSE);
  }

  private void startLoad(Integer startYear, Integer startMonth, Integer startDay, Integer endYear, Integer endMonth, Integer endDay, Boolean runPartial) {


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
          configDTO.setValue(String.format(CONFIG_DTO_VALUE_STR, filtred.format(format1), runPartial, JobStatus.SCHEDULED, LocalDateTime.now().format(formatMessage)));
          return configDTO;

        }).collect(Collectors.toList());

        configService.saveAll(configDTOS);
      }
    }
  }

  /*  message = MessageBuilder
      .withPayload(LOADER_EVENTS.EVENT_RECEIVED)
      .setHeader("runDate", localDates)
      .setHeader("runPartial", runPartial)
      .build();

    boolean result = sp500LoaderStateMachine.sendEvent(message);
*/

  @Scheduled(cron = "${loader.daily.cron.expression}")
  public void scheduleDailyTask() {

    LocalDate runDate = LocalDate.now().minusDays(1);
    startLoad(runDate.getYear(), runDate.getMonthValue(), runDate.getDayOfMonth(), runDate.getYear(), runDate.getMonthValue(), runDate.getDayOfMonth(), Boolean.FALSE);
  }

  @Scheduled(cron = "${loader.recurring.cron.expression}")
  public void scheduleRecurringDelayTask() {

    List<ConfigDTO> configDTOS = configService.getAll(LOADER, RUNNING_JOBS);

    List<ConfigDTO> running = getJobsInGivenStatus(configDTOS, JobStatus.RUNNING);
    if (!running.isEmpty()) {
      running.stream().forEach(current -> {
        if (isHanging(current)) {
          current.setValue(String.format(CONFIG_DTO_VALUE_STR, parseDate(current).format(format1), isPartial(current), JobStatus.ERROR, LocalDateTime.now().format(formatMessage)));
          configService.save(current);
        }

      });
      return;
    }

    List<ConfigDTO> errored = getJobsInGivenStatus(configDTOS, JobStatus.ERROR);
    if (!errored.isEmpty()) {
      errored.stream().forEach(configDTO -> cleanup(configDTO));

      errored.stream().forEach(current -> {

        LocalDate runDate = parseDate(current);

        current.setValue(String.format(CONFIG_DTO_VALUE_STR, parseDate(current).format(format1), isPartial(current), JobStatus.SCHEDULED, LocalDateTime.now().format(formatMessage)));
        configService.save(current);

      });
      return;
    }
    List<ConfigDTO> toRun = getJobsInGivenStatus(configDTOS, JobStatus.SCHEDULED);
    if (toRun.isEmpty())
      return;

    ConfigDTO current = toRun.iterator().next();
    current.setValue(String.format(CONFIG_DTO_VALUE_STR, parseDate(current).format(format1), isPartial(current), JobStatus.RUNNING, LocalDateTime.now().format(formatMessage)));
    configService.save(current);

  }

  private boolean isHanging(ConfigDTO current) {

    LocalDateTime lateUpdate = parseUpdatedDate(current);
    LocalDateTime now = LocalDateTime.now();
    long minutes = ChronoUnit.MINUTES.between(lateUpdate, now);

    return minutes >= maxRunningTime;
  }


  private void cleanup(ConfigDTO configDTO) {
  }

  private List<ConfigDTO> getJobsInGivenStatus(List<ConfigDTO> configDTOS, JobStatus status) {

    return configDTOS.stream().filter(configDTO -> {

      return isInGivenStatus(configDTO, status);
    }).collect(Collectors.toList());

  }


  private boolean isInGivenStatus(ConfigDTO configDTO, JobStatus status) {
    DocumentContext content = JsonPath.parse(configDTO.getValue());
    JSONArray json = content.read(runningSatusStr);

    String type = json.get(0).toString();

    if (type != null && type.contains(status.toString()))
      return true;
    return false;
  }

  private LocalDate parseDate(ConfigDTO configDTO) {
    DocumentContext content = JsonPath.parse(configDTO.getValue());
    JSONArray json = content.read(runningDateStr);
    String type = json.get(0).toString();
    if (type != null)
      return LocalDate.parse(type, format1);
    return null;
  }

  private LocalDateTime parseUpdatedDate(ConfigDTO configDTO) {
    DocumentContext content = JsonPath.parse(configDTO.getValue());
    JSONArray json = content.read(updatedDateStr);
    String type = json.get(0).toString();
    if (type != null)
      return LocalDateTime.parse(type, formatMessage);
    return null;
  }

  private boolean isPartial(ConfigDTO configDTO) {
    DocumentContext content = JsonPath.parse(configDTO.getValue());
    JSONArray json = content.read(runningPartialStr);
    String type = json.get(0).toString();
    if (type != null && type.compareToIgnoreCase("true") == 0)
      return true;
    return false;
  }

  private boolean wasDayBeforeRunDateDayDayOff(LocalDate runDate) {
    return false;
  }

  private boolean isApiCallToElevated() {
    return false;
  }

}
