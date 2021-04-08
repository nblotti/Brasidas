package ch.nblotti.brasidas.exchange.firmloader;

import ch.nblotti.brasidas.configuration.ConfigDTO;
import ch.nblotti.brasidas.configuration.JobStatus;
import ch.nblotti.brasidas.exchange.firm.FirmService;
import ch.nblotti.brasidas.exchange.firmhighlights.FirmHighlightsService;
import ch.nblotti.brasidas.exchange.firminfos.FirmInfoService;
import ch.nblotti.brasidas.exchange.firmsharestats.FirmSharesStatsService;
import ch.nblotti.brasidas.exchange.firmvaluation.FirmValuationService;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@EnableStateMachine(name = "marketCleanerStateMachine")
@Slf4j
public class MarketCleaner extends EnumStateMachineConfigurerAdapter<MARKET_CLEANUP_STATES, MARKET_CLEANUP_EVENTS> {


  private final static String EXCHANGE_NYSE = "NYSE";
  private final static String EXCHANGE_NASDAQ = "NASDAQ";

  @Autowired
  private DateTimeFormatter format1;

  @Autowired
  private DateTimeFormatter formatMessage;


  private static final String CLEANER = "CLEANER";
  private static final String ERROR_JOBS = "ERROR_JOBS";

  @Autowired
  private MarketLoadConfigService marketLoadConfigService;


  public static final String EVENT_MESSAGE_DAY = "firms";


  @Autowired
  private BeanFactory beanFactory;


  @Override
  public void configure(
    StateMachineConfigurationConfigurer
      <MARKET_CLEANUP_STATES, MARKET_CLEANUP_EVENTS> config) throws Exception {

    config.withConfiguration()
      .autoStartup(false)
      .taskExecutor(myAsyncTaskExecutor()).taskScheduler(new ConcurrentTaskScheduler());

  }

  @Override
  public void configure(StateMachineStateConfigurer<MARKET_CLEANUP_STATES, MARKET_CLEANUP_EVENTS> states) throws Exception {
    states.withStates()
      .initial(MARKET_CLEANUP_STATES.WAITING_EVENT)
      .state(MARKET_CLEANUP_STATES.READY)
      .state(MARKET_CLEANUP_STATES.GET_DATES, filterCleanupRunDates())
      .state(MARKET_CLEANUP_STATES.DELETE_STATE, deleteAction())
      .state(MARKET_CLEANUP_STATES.ERROR_STATE, errorCleanupAction())
      .end(MARKET_CLEANUP_STATES.DONE)
      .end(MARKET_CLEANUP_STATES.CANCELED);

  }


  @Override
  public void configure(StateMachineTransitionConfigurer<MARKET_CLEANUP_STATES, MARKET_CLEANUP_EVENTS> transitions) throws Exception {
    transitions.withExternal()
      .source(MARKET_CLEANUP_STATES.WAITING_EVENT).target(MARKET_CLEANUP_STATES.READY)
      .and()
      .withExternal()
      .source(MARKET_CLEANUP_STATES.READY).target(MARKET_CLEANUP_STATES.GET_DATES).event(MARKET_CLEANUP_EVENTS.EVENT_RECEIVED)
      .and()
      .withExternal()
      .source(MARKET_CLEANUP_STATES.GET_DATES).target(MARKET_CLEANUP_STATES.DELETE_STATE).event(MARKET_CLEANUP_EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(MARKET_CLEANUP_STATES.GET_DATES).target(MARKET_CLEANUP_STATES.DONE).event(MARKET_CLEANUP_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(MARKET_CLEANUP_STATES.DELETE_STATE).target(MARKET_CLEANUP_STATES.DONE).event(MARKET_CLEANUP_EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(MARKET_CLEANUP_STATES.DELETE_STATE).target(MARKET_CLEANUP_STATES.ERROR_STATE).event(MARKET_CLEANUP_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(MARKET_CLEANUP_STATES.ERROR_STATE).target(MARKET_CLEANUP_STATES.WAITING_EVENT).event(MARKET_CLEANUP_EVENTS.ERROR_TREATED)
      .and()
      .withExternal()
      .source(MARKET_CLEANUP_STATES.ERROR_STATE).target(MARKET_CLEANUP_STATES.CANCELED).event(MARKET_CLEANUP_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(MARKET_CLEANUP_STATES.CANCELED).target(MARKET_CLEANUP_STATES.WAITING_EVENT)
      .and()
      .withExternal()
      .source(MARKET_CLEANUP_STATES.DONE).target(MARKET_CLEANUP_STATES.WAITING_EVENT);


    ;
  }


  @Bean
  public Action<MARKET_CLEANUP_STATES, MARKET_CLEANUP_EVENTS> filterCleanupRunDates() {
    return new Action<MARKET_CLEANUP_STATES, MARKET_CLEANUP_EVENTS>() {

      @Override
      public void execute(StateContext<MARKET_CLEANUP_STATES, MARKET_CLEANUP_EVENTS> context) {

        Message<MARKET_CLEANUP_EVENTS> message;

        LocalDate runDate = (LocalDate) context.getMessageHeader("runDate");
        Long id = (Long) context.getMessageHeader("erroredId");


        if (runDate == null || id == null) {
          message = MessageBuilder
            .withPayload(MARKET_CLEANUP_EVENTS.ERROR)
            .build();
        } else {
          context.getExtendedState().getVariables().put("runDate", runDate);
          context.getExtendedState().getVariables().put("erroredId", id);
          message = MessageBuilder
            .withPayload(MARKET_CLEANUP_EVENTS.SUCCESS)
            .build();
        }
        context.getStateMachine().sendEvent(message);
      }
    };

  }

  @Bean
  public Action<MARKET_CLEANUP_STATES, MARKET_CLEANUP_EVENTS> deleteAction() {
    return new Action<MARKET_CLEANUP_STATES, MARKET_CLEANUP_EVENTS>() {

      @Override
      public void execute(StateContext<MARKET_CLEANUP_STATES, MARKET_CLEANUP_EVENTS> stateContext) {

        Message<MARKET_CLEANUP_EVENTS> message;
        LocalDate runDate = (LocalDate) stateContext.getExtendedState().getVariables().get("runDate");
        Long id = (Long) stateContext.getExtendedState().getVariables().get("erroredId");

        ConfigDTO errored = marketLoadConfigService.findById(id);


        if (errored == null || !marketLoadConfigService.isInGivenStatus(errored, JobStatus.ERROR)) {
          message = MessageBuilder
            .withPayload(MARKET_CLEANUP_EVENTS.ERROR)
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

            errored.setValue(String.format(MarketLoadConfigService.CONFIG_DTO_VALUE_STR, marketLoadConfigService.parseDate(errored).format(format1), marketLoadConfigService.isPartial(errored), JobStatus.SCHEDULED, LocalDateTime.now().format(formatMessage), marketLoadConfigService.retryCount(errored)));
            marketLoadConfigService.save(errored);

            message = MessageBuilder
              .withPayload(MARKET_CLEANUP_EVENTS.SUCCESS).build();

          } catch (Exception ex) {
            log.error(ex.getMessage());
            message = MessageBuilder
              .withPayload(MARKET_CLEANUP_EVENTS.ERROR)
              .build();
          }
        }
        stateContext.getStateMachine().sendEvent(message);
      }
    };

  }

  @Bean
  public Action<MARKET_CLEANUP_STATES, MARKET_CLEANUP_EVENTS> errorCleanupAction() {
    return new Action<MARKET_CLEANUP_STATES, MARKET_CLEANUP_EVENTS>() {

      @Override
      public void execute(StateContext<MARKET_CLEANUP_STATES, MARKET_CLEANUP_EVENTS> context) {

        LocalDate runDate = (LocalDate) context.getExtendedState().getVariables().get("runDate");
        Long id = (Long) context.getExtendedState().getVariables().get("erroredId");
        ConfigDTO errored = marketLoadConfigService.findById(id);

        errored.setValue(String.format(MarketLoadConfigService.CONFIG_DTO_VALUE_STR, marketLoadConfigService.parseDate(errored).format(format1), marketLoadConfigService.isPartial(errored), JobStatus.ERROR, LocalDateTime.now().format(formatMessage), marketLoadConfigService.retryCount(errored) + 1));
        marketLoadConfigService.save(errored);

        context.getStateMachine().sendEvent(MARKET_CLEANUP_EVENTS.ERROR_TREATED);

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

