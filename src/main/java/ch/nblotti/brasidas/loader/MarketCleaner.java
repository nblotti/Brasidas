package ch.nblotti.brasidas.loader;

import ch.nblotti.brasidas.configuration.ConfigDTO;
import ch.nblotti.brasidas.configuration.ConfigService;
import ch.nblotti.brasidas.exchange.firm.FirmService;
import ch.nblotti.brasidas.exchange.firmhighlights.FirmHighlightsService;
import ch.nblotti.brasidas.exchange.firminfos.FirmInfoService;
import ch.nblotti.brasidas.exchange.firmsharestats.FirmSharesStatsService;
import ch.nblotti.brasidas.exchange.firmvaluation.FirmValuationService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Configuration
@EnableStateMachine(name = "marketCleanerStateMachine")
public class MarketCleaner extends EnumStateMachineConfigurerAdapter<CLEANUP_STATES, CLEANUP_EVENTS> {


  private static final Logger logger = Logger.getLogger("DailyLoaderStateMachine");

  private final static String EXCHANGE_NYSE = "NYSE";
  private final static String EXCHANGE_NASDAQ = "NASDAQ";

  @Autowired
  private DateTimeFormatter format1;

  @Autowired
  private DateTimeFormatter formatMessage;


  private static final String CLEANER = "CLEANER";
  private static final String ERROR_JOBS = "ERROR_JOBS";

  @Autowired
  private ConfigService configService;


  public static final String EVENT_MESSAGE_DAY = "firms";


  @Autowired
  private BeanFactory beanFactory;


  @Override
  public void configure(
    StateMachineConfigurationConfigurer
      <CLEANUP_STATES, CLEANUP_EVENTS> config) throws Exception {

    config.withConfiguration()
      .autoStartup(false)
      .taskExecutor(myAsyncTaskExecutor()).taskScheduler(new ConcurrentTaskScheduler());

  }

  @Override
  public void configure(StateMachineStateConfigurer<CLEANUP_STATES, CLEANUP_EVENTS> states) throws Exception {
    states.withStates()
      .initial(CLEANUP_STATES.WAITING_EVENT)
      .state(CLEANUP_STATES.READY)
      .state(CLEANUP_STATES.GET_DATES, filterCleanupRunDates())
      .state(CLEANUP_STATES.DELETE_STATE, deleteAction())
      .state(CLEANUP_STATES.ERROR_STATE, errorCleanupAction())
      .end(CLEANUP_STATES.DONE)
      .end(CLEANUP_STATES.CANCELED);

  }


  @Override
  public void configure(StateMachineTransitionConfigurer<CLEANUP_STATES, CLEANUP_EVENTS> transitions) throws Exception {
    transitions.withExternal()
      .source(CLEANUP_STATES.WAITING_EVENT).target(CLEANUP_STATES.READY)
      .and()
      .withExternal()
      .source(CLEANUP_STATES.READY).target(CLEANUP_STATES.GET_DATES).event(CLEANUP_EVENTS.EVENT_RECEIVED)
      .and()
      .withExternal()
      .source(CLEANUP_STATES.GET_DATES).target(CLEANUP_STATES.DELETE_STATE).event(CLEANUP_EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(CLEANUP_STATES.GET_DATES).target(CLEANUP_STATES.DONE).event(CLEANUP_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(CLEANUP_STATES.DELETE_STATE).target(CLEANUP_STATES.DONE).event(CLEANUP_EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(CLEANUP_STATES.DELETE_STATE).target(CLEANUP_STATES.ERROR_STATE).event(CLEANUP_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(CLEANUP_STATES.ERROR_STATE).target(CLEANUP_STATES.WAITING_EVENT).event(CLEANUP_EVENTS.ERROR_TREATED)
      .and()
      .withExternal()
      .source(CLEANUP_STATES.ERROR_STATE).target(CLEANUP_STATES.CANCELED).event(CLEANUP_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(CLEANUP_STATES.CANCELED).target(CLEANUP_STATES.WAITING_EVENT)
      .and()
      .withExternal()
      .source(CLEANUP_STATES.DONE).target(CLEANUP_STATES.WAITING_EVENT);


    ;
  }


  @Bean
  public Action<CLEANUP_STATES, CLEANUP_EVENTS> filterCleanupRunDates() {
    return new Action<CLEANUP_STATES, CLEANUP_EVENTS>() {

      @Override
      public void execute(StateContext<CLEANUP_STATES, CLEANUP_EVENTS> context) {

        Message<CLEANUP_EVENTS> message;

        LocalDate runDate = (LocalDate) context.getMessageHeader("runDate");
        Long id = (Long) context.getMessageHeader("erroredId");


        if (runDate == null || id == null) {
          message = MessageBuilder
            .withPayload(CLEANUP_EVENTS.ERROR)
            .build();
        } else {
          context.getExtendedState().getVariables().put("runDate", runDate);
          context.getExtendedState().getVariables().put("erroredId", id);
          message = MessageBuilder
            .withPayload(CLEANUP_EVENTS.SUCCESS)
            .build();
        }
        context.getStateMachine().sendEvent(message);
      }
    };

  }

  @Bean
  public Action<CLEANUP_STATES, CLEANUP_EVENTS> deleteAction() {
    return new Action<CLEANUP_STATES, CLEANUP_EVENTS>() {

      @Override
      public void execute(StateContext<CLEANUP_STATES, CLEANUP_EVENTS> stateContext) {

        Message<CLEANUP_EVENTS> message;
        LocalDate runDate = (LocalDate) stateContext.getExtendedState().getVariables().get("runDate");
        Long id = (Long) stateContext.getExtendedState().getVariables().get("erroredId");

        ConfigDTO errored = configService.findById(id);


        if (errored == null || !configService.isInGivenStatus(errored, JobStatus.ERROR)) {
          message = MessageBuilder
            .withPayload(CLEANUP_EVENTS.ERROR)
            .build();

        } else {


          try {


            FirmService firmService = beanFactory.getBean(FirmService.class);
            firmService.deleteByDate(runDate);

            FirmInfoService firmInfoService = beanFactory.getBean(FirmInfoService.class);
            firmInfoService.deleteByDate(runDate);

            FirmValuationService firmValuationService = beanFactory.getBean(FirmValuationService.class);
            firmValuationService.deleteByDate(runDate);

            FirmSharesStatsService firmSharesStatsService = beanFactory.getBean(FirmSharesStatsService.class);
            firmSharesStatsService.deleteByDate(runDate);

            FirmHighlightsService firmHighlightsService = beanFactory.getBean(FirmHighlightsService.class);
            firmHighlightsService.deleteByDate(runDate);


            message = MessageBuilder
              .withPayload(CLEANUP_EVENTS.SUCCESS).build();

          } catch (Exception ex) {
            logger.severe(ex.getMessage());
            message = MessageBuilder
              .withPayload(CLEANUP_EVENTS.ERROR)
              .build();
          }
        }
        stateContext.getStateMachine().sendEvent(message);
      }
    };

  }

  @Bean
  public Action<CLEANUP_STATES, CLEANUP_EVENTS> errorCleanupAction() {
    return new Action<CLEANUP_STATES, CLEANUP_EVENTS>() {

      @Override
      public void execute(StateContext<CLEANUP_STATES, CLEANUP_EVENTS> context) {

        LocalDate runDate = (LocalDate) context.getExtendedState().getVariables().get("runDate");
        Long id = (Long) context.getExtendedState().getVariables().get("erroredId");
        ConfigDTO errored = configService.findById(id);

        errored.setValue(String.format(ConfigService.CONFIG_DTO_VALUE_STR, configService.parseDate(errored).format(format1), configService.isPartial(errored), JobStatus.ERROR, LocalDateTime.now().format(formatMessage),configService.retryCount(errored)+1));
        configService.save(errored);

        context.getStateMachine().sendEvent(CLEANUP_EVENTS.ERROR_TREATED);

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

