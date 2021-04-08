package ch.nblotti.brasidas.exchange.dayoffloader;

import ch.nblotti.brasidas.configuration.ConfigDTO;
import ch.nblotti.brasidas.configuration.JobStatus;
import ch.nblotti.brasidas.exchange.splitloader.SplitConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
@EnableStateMachine(name = "dayOffStateMachine")
public class DayOffLoader extends EnumStateMachineConfigurerAdapter<DAYOFF_STATES, DAYOFF_EVENTS> {


  @Autowired
  private DateTimeFormatter format1;

  @Autowired
  private DateTimeFormatter formatMessage;


  private static final String EXCHANGE = "US";



  @Autowired
  private DayOffConfigService dayOffConfigService;


  @Autowired
  private BeanFactory beanFactory;


  @Autowired
  DayOffService dayOffService;


  @Override
  public void configure(
    StateMachineConfigurationConfigurer
      <DAYOFF_STATES, DAYOFF_EVENTS> config) throws Exception {

    config.withConfiguration()
      .autoStartup(false);
  }

  @Override
  public void configure(StateMachineStateConfigurer<DAYOFF_STATES, DAYOFF_EVENTS> states) throws Exception {
    states.withStates()
      .initial(DAYOFF_STATES.WAITING_EVENT)
      .state(DAYOFF_STATES.READY)
      .state(DAYOFF_STATES.LOAD_DAYOFF, loadDayOff())
      .state(DAYOFF_STATES.SAVE_WEEKS, saveWeeks())
      .state(DAYOFF_STATES.SAVE_MONTHS, saveMonths())
      .end(DAYOFF_STATES.DONE)
      .end(DAYOFF_STATES.CANCELED);

  }


  @Override
  public void configure(StateMachineTransitionConfigurer<DAYOFF_STATES, DAYOFF_EVENTS> transitions) throws Exception {
    transitions.withExternal()
      .source(DAYOFF_STATES.WAITING_EVENT).target(DAYOFF_STATES.READY)
      .and()
      .withExternal()
      .source(DAYOFF_STATES.READY).target(DAYOFF_STATES.LOAD_DAYOFF).event(DAYOFF_EVENTS.EVENT_RECEIVED)
      .and()
      .withExternal()
      .source(DAYOFF_STATES.LOAD_DAYOFF).target(DAYOFF_STATES.SAVE_WEEKS).event(DAYOFF_EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(DAYOFF_STATES.LOAD_DAYOFF).target(DAYOFF_STATES.ERROR_STATE).event(DAYOFF_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(DAYOFF_STATES.SAVE_WEEKS).target(DAYOFF_STATES.SAVE_MONTHS).event(DAYOFF_EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(DAYOFF_STATES.SAVE_WEEKS).target(DAYOFF_STATES.ERROR_STATE).event(DAYOFF_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(DAYOFF_STATES.SAVE_MONTHS).target(DAYOFF_STATES.DONE).event(DAYOFF_EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(DAYOFF_STATES.SAVE_MONTHS).target(DAYOFF_STATES.ERROR_STATE).event(DAYOFF_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(DAYOFF_STATES.ERROR_STATE).target(DAYOFF_STATES.WAITING_EVENT).event(DAYOFF_EVENTS.ERROR_TREATED)
      .and()
      .withExternal()
      .source(DAYOFF_STATES.ERROR_STATE).target(DAYOFF_STATES.CANCELED).event(DAYOFF_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(DAYOFF_STATES.CANCELED).target(DAYOFF_STATES.WAITING_EVENT)
      .and()
      .withExternal()
      .source(DAYOFF_STATES.DONE).target(DAYOFF_STATES.WAITING_EVENT);


    ;
  }


  @Bean
  public Action<DAYOFF_STATES, DAYOFF_EVENTS> loadDayOff() {
    return new Action<DAYOFF_STATES, DAYOFF_EVENTS>() {

      @Override
      public void execute(StateContext<DAYOFF_STATES, DAYOFF_EVENTS> context) {

        Message<DAYOFF_EVENTS> message;
        List<DayOffDTO> dayOffs;

        Long id = (Long) context.getMessageHeader("dayOffId");
        context.getExtendedState().getVariables().put("dayOffId", id);

        ConfigDTO current = dayOffConfigService.findById(id);

        current.setValue(String.format(DayOffConfigService.CONFIG_DTO_VALUE_STR, dayOffConfigService.parsedayOffYear(current), JobStatus.RUNNING, LocalDateTime.now().format(formatMessage), dayOffConfigService.retryCount(current) + 1));
        dayOffConfigService.update(current);

        Integer year = dayOffConfigService.parsedayOffYear(current);

        try {
          if (year == null) {
            message = MessageBuilder
              .withPayload(DAYOFF_EVENTS.ERROR)
              .build();
          } else {
            dayOffs = dayOffService.getDayOffForYearSplitByDate(EXCHANGE, year);
            for (DayOffDTO dayOffDTO : dayOffs) {
              dayOffService.saveEODDayOff(dayOffDTO);
            }

            context.getExtendedState().getVariables().put("year", year);
            context.getExtendedState().getVariables().put("dayoff", dayOffs);
            message = MessageBuilder
              .withPayload(DAYOFF_EVENTS.SUCCESS)
              .build();
          }
        } catch (Exception ex) {
          log.error("Error loading dayoff");
          message = MessageBuilder
            .withPayload(DAYOFF_EVENTS.ERROR)
            .build();

        }
        context.getStateMachine().sendEvent(message);

      }
    };

  }


  @Bean
  public Action<DAYOFF_STATES, DAYOFF_EVENTS> saveWeeks() {
    return new Action<DAYOFF_STATES, DAYOFF_EVENTS>() {

      @Override
      public void execute(StateContext<DAYOFF_STATES, DAYOFF_EVENTS> context) {

        Message<DAYOFF_EVENTS> message;

        List<DayOffDTO> dayOffs = (List<DayOffDTO>) context.getExtendedState().getVariables().get("dayoff");
        Integer year = (Integer) context.getExtendedState().getVariables().get("year");

        if (dayOffs == null || year == null) {
          message = MessageBuilder
            .withPayload(DAYOFF_EVENTS.ERROR)
            .build();
        } else {
          List<TimeDTO> timeDTOS = getWeeks(year, dayOffs);
          for (TimeDTO timeDTO : timeDTOS) {
            dayOffService.saveTimeDto(timeDTO);
          }

          message = MessageBuilder
            .withPayload(DAYOFF_EVENTS.SUCCESS)
            .build();
        }
        context.getStateMachine().sendEvent(message);
      }
    };
  }

  private List<TimeDTO> getWeeks(Integer year, List<DayOffDTO> dayOffs) {

    List<TimeDTO> weeks = new ArrayList<>();
    LocalDate from = LocalDate.of(year, 1, 1);
    LocalDate to = LocalDate.of(year, 12, 31);

    for (LocalDate currentDate = from; currentDate.isBefore(to); currentDate = currentDate.plusWeeks(1)) {
      weeks.add(new TimeDTO(currentDate, currentDate.plusWeeks(1).minusDays(1), ChronoUnit.WEEKS,currentDate.get(WeekFields.ISO.weekOfYear())));
    }
    return weeks;
  }


  @Bean
  public Action<DAYOFF_STATES, DAYOFF_EVENTS> saveMonths() {
    return new Action<DAYOFF_STATES, DAYOFF_EVENTS>() {

      @Override
      public void execute(StateContext<DAYOFF_STATES, DAYOFF_EVENTS> context) {

        Message<DAYOFF_EVENTS> message;

        List<DayOffDTO> dayOffs = (List<DayOffDTO>) context.getExtendedState().getVariables().get("dayoff");
        Integer year = (Integer) context.getExtendedState().getVariables().get("year");
        Long id = (Long)  context.getExtendedState().getVariables().get("dayOffId");

        if (dayOffs == null || year == null) {
          message = MessageBuilder
            .withPayload(DAYOFF_EVENTS.ERROR)
            .build();
        } else {

          List<TimeDTO> monthDTOS = getMonths(year, dayOffs);
          for (TimeDTO timeDTO : monthDTOS) {
            dayOffService.saveTimeDto(timeDTO);
          }


          ConfigDTO current = dayOffConfigService.findById(id);
          current.setValue(String.format(DayOffConfigService.CONFIG_DTO_VALUE_STR, dayOffConfigService.parsedayOffYear(current), JobStatus.FINISHED, LocalDateTime.now().format(formatMessage), dayOffConfigService.retryCount(current)));
          dayOffConfigService.save(current);

          message = MessageBuilder
            .withPayload(DAYOFF_EVENTS.SUCCESS)
            .build();
        }
        context.getStateMachine().sendEvent(message);
      }

    };
  }

  private List<TimeDTO> getMonths(Integer year, List<DayOffDTO> dayOffs) {


    List<TimeDTO> months = new ArrayList<>();
    LocalDate from = LocalDate.of(year, 1, 1);
    LocalDate to = LocalDate.of(year, 12, 31);

    for (LocalDate currentDate = from; currentDate.isBefore(to); currentDate = currentDate.plusMonths(1)) {
      months.add(new TimeDTO(currentDate, currentDate.plusMonths(1).minusDays(1), ChronoUnit.MONTHS, currentDate.getMonthValue()));
    }
    return months;


  }

  @Bean
  public Action<DAYOFF_STATES, DAYOFF_EVENTS> dayOffErrorState() {
    return new Action<DAYOFF_STATES, DAYOFF_EVENTS>() {

      @Override
      public void execute(StateContext<DAYOFF_STATES, DAYOFF_EVENTS> context) {

        LocalDate runDate = (LocalDate) context.getExtendedState().getVariables().get("runDate");
        Long id = (Long) context.getExtendedState().getVariables().get("erroredId");
        ConfigDTO errored = dayOffConfigService.findById(id);

        errored.setValue(String.format(DayOffConfigService.CONFIG_DTO_VALUE_STR, dayOffConfigService.parsedayOffYear(errored), JobStatus.ERROR, LocalDateTime.now().format(formatMessage), dayOffConfigService.retryCount(errored) + 1));
        dayOffConfigService.save(errored);

        context.getStateMachine().sendEvent(DAYOFF_EVENTS.ERROR_TREATED);

      }

    };
  }

  public TaskExecutor myAsyncTaskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(5);
    taskExecutor.initialize();
    return taskExecutor;
  }


}

