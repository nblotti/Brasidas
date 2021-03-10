package ch.nblotti.brasidas.index.loader;

import ch.nblotti.brasidas.configuration.ConfigDTO;
import ch.nblotti.brasidas.exchange.firm.EODFirmFundamentalRepository;
import ch.nblotti.brasidas.exchange.firm.ExchangeFirmQuoteDTO;
import ch.nblotti.brasidas.exchange.firm.FirmService;
import ch.nblotti.brasidas.exchange.firmhighlights.FirmHighlightsDTO;
import ch.nblotti.brasidas.exchange.firmhighlights.FirmHighlightsService;
import ch.nblotti.brasidas.exchange.firminfos.FirmInfoDTO;
import ch.nblotti.brasidas.exchange.firminfos.FirmInfoService;
import ch.nblotti.brasidas.exchange.firmsharestats.FirmShareStatsDTO;
import ch.nblotti.brasidas.exchange.firmsharestats.FirmSharesStatsService;
import ch.nblotti.brasidas.exchange.firmvaluation.FirmValuationDTO;
import ch.nblotti.brasidas.exchange.firmvaluation.FirmValuationService;
import ch.nblotti.brasidas.configuration.JobStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
@EnableStateMachine(name = "indexLoaderStateMachine")
@Slf4j
public class IndexLoader extends EnumStateMachineConfigurerAdapter<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> {


  private final static String EXCHANGE_NYSE = "NYSE";
  private final static String EXCHANGE_NASDAQ = "NASDAQ";

  @Autowired
  private DateTimeFormatter format1;

  @Autowired
  private DateTimeFormatter formatMessage;


  private static final String LOADER = "LOADER";
  private static final String RUNNING_JOBS = "RUNNING_JOBS";

  @Autowired
  private LoadIndexConfigService loadIndexConfigService;


  public static final String EVENT_MESSAGE_DAY = "firms";

  @Value("${index.list}")
  private String indexList;


  @Value("${nyse.closed.days}")
  public String nyseClosedDays;


  @Autowired
  private BeanFactory beanFactory;


  public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(5);
    taskExecutor.initialize();
    return taskExecutor;
  }

  @Override
  public void configure(
    StateMachineConfigurationConfigurer
      <INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> config) throws Exception {

    config.withConfiguration()
      .autoStartup(false)
      .taskExecutor(myAsyncTaskExecutor()).taskScheduler(new ConcurrentTaskScheduler());

  }

  @Override
  public void configure(StateMachineStateConfigurer<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> states) throws Exception {
    states.withStates()
      .initial(INDEX_LOADER_STATES.WAITING_EVENT)
      .state(INDEX_LOADER_STATES.READY, initalIndexAction())
      .state(INDEX_LOADER_STATES.CLEANUP, cleanupIndex())
      .state(INDEX_LOADER_STATES.ERROR_STATE, errorIndexAction())
      .state(INDEX_LOADER_STATES.LOAD_INDEX, loadIndexSP500())
      .end(INDEX_LOADER_STATES.DONE)
      .end(INDEX_LOADER_STATES.ERROR);


  }


  @Override
  public void configure(StateMachineTransitionConfigurer<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> transitions) throws Exception {
    transitions.withExternal()
      .source(INDEX_LOADER_STATES.WAITING_EVENT).target(INDEX_LOADER_STATES.READY)
      .and()
      .withExternal()
      .source(INDEX_LOADER_STATES.READY).target(INDEX_LOADER_STATES.CLEANUP).event(INDEX_LOADER_EVENTS.EVENT_RECEIVED)
      .and()
      .withExternal()
      .source(INDEX_LOADER_STATES.CLEANUP).target(INDEX_LOADER_STATES.LOAD_INDEX).event(INDEX_LOADER_EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(INDEX_LOADER_STATES.CLEANUP).target(INDEX_LOADER_STATES.ERROR_STATE).event(INDEX_LOADER_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(INDEX_LOADER_STATES.LOAD_INDEX).target(INDEX_LOADER_STATES.LOAD_INDEX).event(INDEX_LOADER_EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(INDEX_LOADER_STATES.LOAD_INDEX).target(INDEX_LOADER_STATES.ERROR_STATE).event(INDEX_LOADER_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(INDEX_LOADER_STATES.ERROR_STATE).target(INDEX_LOADER_STATES.ERROR).event(INDEX_LOADER_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(INDEX_LOADER_STATES.ERROR_STATE).target(INDEX_LOADER_STATES.WAITING_EVENT).event(INDEX_LOADER_EVENTS.ERROR_TREATED)
    ;
  }


  @Bean
  public Action<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> initalIndexAction() {
    return new Action<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS>() {

      @Override
      public void execute(StateContext<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> context) {


      }
    };
  }


  private String[] getIndexList() {
    return indexList.split(",");
  }


  @Bean
  public Action<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> loadIndexSP500() {
    return new Action<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS>() {

      @Override
      public void execute(StateContext<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> stateContext) {

        Map<Object, Object> variables = stateContext.getExtendedState().getVariables();

        try {
          LocalDate runDate = (LocalDate) stateContext.getExtendedState().getVariables().get("runDate");
          LocalDateTime runTimeStart = (LocalDateTime) stateContext.getExtendedState().getVariables().get("runTime");

          log.info(String.format("SP500 - %s - Starting load process", runDate.format(format1)));

          LocalDateTime runTimeEnd = LocalDateTime.now();
          Duration diff = Duration.between(runTimeStart, runTimeEnd);

          log.info(String.format("%s - %s - End load process. Treated in %s minutes", runDate.format(format1), stateContext, diff.toMinutes()));
        } catch (Exception ex) {
          log.error(ex.toString());
        }
      }
    };

  }


  @Bean
  public Action<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> errorIndexAction() {
    return new Action<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS>() {

      @Override
      public void execute(StateContext<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> context) {


        Long id = (Long) context.getExtendedState().getVariables().get("loadId");

        ConfigDTO errored = loadIndexConfigService.findById(id);

        errored.setValue(String.format(LoadIndexConfigService.CONFIG_DTO_VALUE_STR, loadIndexConfigService.parseDate(errored).format(format1), loadIndexConfigService.isPartial(errored), JobStatus.ERROR, LocalDateTime.now().format(formatMessage), loadIndexConfigService.retryCount(errored)));
        loadIndexConfigService.update(errored);

        context.getStateMachine().sendEvent(INDEX_LOADER_EVENTS.ERROR_TREATED);
      }
    };
  }

  @Bean
  public Action<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> cleanupIndex() {
    return new Action<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS>() {
      @Override
      public void execute(StateContext<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> context) {


        Long id = (Long) context.getExtendedState().getVariables().get("loadId");

        ConfigDTO current = loadIndexConfigService.findById(id);

        current.setValue(String.format(LoadIndexConfigService.CONFIG_DTO_VALUE_STR, loadIndexConfigService.parseDate(current).format(format1), loadIndexConfigService.isPartial(current), JobStatus.FINISHED, LocalDateTime.now().format(formatMessage), loadIndexConfigService.retryCount(current)));
        loadIndexConfigService.update(current);

        LocalDate runDate = (LocalDate) context.getExtendedState().getVariables().get("runDate");

        LocalDateTime runTimeStart = (LocalDateTime) context.getExtendedState().getVariables().get("runTime");
        LocalDateTime runTimeEnd = LocalDateTime.now();

        Duration diff = Duration.between(runTimeStart, runTimeEnd);

        String process = String.format("Loading ended in %s minutes", diff.toMinutes());
        context.getStateMachine().sendEvent(INDEX_LOADER_EVENTS.SUCCESS);

      }
    };
  }


  public TaskExecutor myAsyncTaskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(5);
    taskExecutor.initialize();
    return taskExecutor;
  }

  public void finalAction(LocalDate runDate) {
    log.info(String.format("%s - Fin du traitement", runDate.format(format1)));
  }


}

