package ch.nblotti.brasidas.loader;

import ch.nblotti.brasidas.configuration.ConfigDTO;
import ch.nblotti.brasidas.configuration.ConfigService;
import ch.nblotti.brasidas.exchange.firm.EODFirmFundamentalRepository;
import ch.nblotti.brasidas.exchange.firm.FirmQuoteDTO;
import ch.nblotti.brasidas.exchange.firm.FirmService;
import ch.nblotti.brasidas.exchange.firmhighlights.FirmHighlightsDTO;
import ch.nblotti.brasidas.exchange.firmhighlights.FirmHighlightsService;
import ch.nblotti.brasidas.exchange.firminfos.FirmInfoDTO;
import ch.nblotti.brasidas.exchange.firminfos.FirmInfoService;
import ch.nblotti.brasidas.exchange.firmsharestats.FirmShareStatsDTO;
import ch.nblotti.brasidas.exchange.firmsharestats.FirmSharesStatsService;
import ch.nblotti.brasidas.exchange.firmvaluation.FirmValuationDTO;
import ch.nblotti.brasidas.exchange.firmvaluation.FirmValuationService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.statemachine.guard.Guard;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

@Configuration
@EnableStateMachine(name = "marketLoaderStateMachine")
public class MarketLoader extends EnumStateMachineConfigurerAdapter<LOADER_STATES, LOADER_EVENTS> {


  private static final Logger logger = Logger.getLogger("DailyLoaderStateMachine");

  private final static String EXCHANGE_NYSE = "NYSE";
  private final static String EXCHANGE_NASDAQ = "NASDAQ";

  @Autowired
  private DateTimeFormatter format1;

  @Autowired
  private DateTimeFormatter formatMessage;


  private static final String LOADER = "LOADER";
  private static final String RUNNING_JOBS = "RUNNING_JOBS";

  @Autowired
  private ConfigService configService;


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
      <LOADER_STATES, LOADER_EVENTS> config) throws Exception {

    config.withConfiguration()
      .autoStartup(false)
      .taskExecutor(myAsyncTaskExecutor()).taskScheduler(new ConcurrentTaskScheduler());

  }

  @Override
  public void configure(StateMachineStateConfigurer<LOADER_STATES, LOADER_EVENTS> states) throws Exception {
    states.withStates()
      .initial(LOADER_STATES.WAITING_EVENT)
      .state(LOADER_STATES.READY, initalAction())
      .state(LOADER_STATES.GET_DATES, filterRunDates())
      .fork(LOADER_STATES.LOAD_FORK)
      .state(LOADER_STATES.LOAD)
      .join(LOADER_STATES.LOAD_JOIN)
      .choice(LOADER_STATES.CHOICE)
      .state(LOADER_STATES.CLEANUP, cleanup())
      .state(LOADER_STATES.ERROR_STATE, errorAction())
      .end(LOADER_STATES.DONE)
      .end(LOADER_STATES.ERROR)
      .and()
      .withStates()
      .parent(LOADER_STATES.LOAD)
      .initial(LOADER_STATES.LOAD_NYSE, loadNYSE())
      .end(LOADER_STATES.LOAD_NYSE_END)
      .and()
      .withStates()
      .parent(LOADER_STATES.LOAD)
      .initial(LOADER_STATES.LOAD_NASDAQ, loadNASDAQ())
      .end(LOADER_STATES.LOAD_NASDAQ_END);


  }


  @Override
  public void configure(StateMachineTransitionConfigurer<LOADER_STATES, LOADER_EVENTS> transitions) throws Exception {
    transitions.withExternal()
      .source(LOADER_STATES.WAITING_EVENT).target(LOADER_STATES.READY)
      .and()
      .withExternal()
      .source(LOADER_STATES.READY).target(LOADER_STATES.GET_DATES).event(LOADER_EVENTS.EVENT_RECEIVED)
      .and()
      .withExternal()
      .source(LOADER_STATES.GET_DATES).target(LOADER_STATES.LOAD_FORK).event(LOADER_EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(LOADER_STATES.GET_DATES).target(LOADER_STATES.DONE).event(LOADER_EVENTS.ERROR)
      .and()
      .withFork()
      .source(LOADER_STATES.LOAD_FORK).target(LOADER_STATES.LOAD)
      .and()
      .withExternal()
      .source(LOADER_STATES.LOAD_NASDAQ).target(LOADER_STATES.LOAD_NASDAQ_END)
      .and()
      .withExternal()
      .source(LOADER_STATES.LOAD_NYSE).target(LOADER_STATES.LOAD_NYSE_END)
      .and()
      .withJoin()
      .source(LOADER_STATES.LOAD).target(LOADER_STATES.LOAD_JOIN)
      .and()
      .withExternal()
      .source(LOADER_STATES.LOAD_JOIN).target(LOADER_STATES.CHOICE)
      .and()
      .withChoice()
      .source(LOADER_STATES.CHOICE)
      .first(LOADER_STATES.ERROR_STATE, tasksChoiceGuard())
      .last(LOADER_STATES.CLEANUP)
      .and()
      .withExternal()
      .source(LOADER_STATES.CLEANUP).target(LOADER_STATES.WAITING_EVENT).event(LOADER_EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(LOADER_STATES.CLEANUP).target(LOADER_STATES.ERROR_STATE).event(LOADER_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(LOADER_STATES.ERROR_STATE).target(LOADER_STATES.WAITING_EVENT).event(LOADER_EVENTS.ERROR_TREATED)
      .and()
      .withExternal()
      .source(LOADER_STATES.ERROR_STATE).target(LOADER_STATES.ERROR).event(LOADER_EVENTS.ERROR)

    ;
  }


  @Bean
  public Action<LOADER_STATES, LOADER_EVENTS> initalAction() {
    return new Action<LOADER_STATES, LOADER_EVENTS>() {

      @Override
      public void execute(StateContext<LOADER_STATES, LOADER_EVENTS> context) {

        context.getExtendedState().getVariables().put("runTime", LocalDateTime.now());
        context.getExtendedState().getVariables().put("runTime", LocalDateTime.now());


      }
    };
  }


  @Bean
  public Action<LOADER_STATES, LOADER_EVENTS> filterRunDates() {
    return new Action<LOADER_STATES, LOADER_EVENTS>() {

      @Override
      public void execute(StateContext<LOADER_STATES, LOADER_EVENTS> context) {

        Message<LOADER_EVENTS> message;

        LocalDate runDate = (LocalDate) context.getMessageHeader("runDate");
        Long id = (Long) context.getMessageHeader("loadId");
        context.getExtendedState().getVariables().put("loadId", id);

        ConfigDTO current = configService.findById(id);

        current.setValue(String.format(ConfigService.CONFIG_DTO_VALUE_STR, configService.parseDate(current).format(format1), configService.isPartial(current), JobStatus.RUNNING, LocalDateTime.now().format(formatMessage),configService.retryCount(current)+1));
        configService.update(current);


        if (runDate == null) {
          message = MessageBuilder
            .withPayload(LOADER_EVENTS.ERROR)
            .build();
        } else {
          context.getExtendedState().getVariables().put("runDate", runDate);

          message = MessageBuilder
            .withPayload(LOADER_EVENTS.SUCCESS)
            .build();
        }
        context.getStateMachine().sendEvent(message);
      }
    };

  }

  private String[] getIndexList() {
    return indexList.split(",");
  }


  @Bean
  public Action<LOADER_STATES, LOADER_EVENTS> loadNYSE() {
    return new Action<LOADER_STATES, LOADER_EVENTS>() {

      @Override
      public void execute(StateContext<LOADER_STATES, LOADER_EVENTS> stateContext) {

        Map<Object, Object> variables = stateContext.getExtendedState().getVariables();

        try {
          loadMarket(MarketLoader.this.EXCHANGE_NYSE, stateContext);
        } catch (Exception ex) {
          variables.put("T1", false);
          logger.severe(ex.toString());
          return;
        }
        variables.put("T1", true);
      }
    };

  }

  @Bean
  public Action<LOADER_STATES, LOADER_EVENTS> loadNASDAQ() {
    return new Action<LOADER_STATES, LOADER_EVENTS>() {
      @Override
      public void execute(StateContext<LOADER_STATES, LOADER_EVENTS> stateContext) {

        Map<Object, Object> variables = stateContext.getExtendedState().getVariables();

        try {
          loadMarket(MarketLoader.this.EXCHANGE_NASDAQ, stateContext);
        } catch (Exception ex) {
          variables.put("T2", false);
          logger.severe(ex.toString());
          return;
        }
        variables.put("T2", true);
      }
    };

  }

  @Bean
  public Guard<LOADER_STATES, LOADER_EVENTS> tasksChoiceGuard() {
    return new Guard<LOADER_STATES, LOADER_EVENTS>() {

      @Override
      public boolean evaluate(StateContext<LOADER_STATES, LOADER_EVENTS> context) {
        Map<Object, Object> variables = context.getExtendedState().getVariables();
        return !(ObjectUtils.nullSafeEquals(variables.get("T1"), true)
          && ObjectUtils.nullSafeEquals(variables.get("T2"), true)
          && ObjectUtils.nullSafeEquals(variables.get("T3"), true));
      }
    };
  }

  @Bean
  public Action<LOADER_STATES, LOADER_EVENTS> errorAction() {
    return new Action<LOADER_STATES, LOADER_EVENTS>() {

      @Override
      public void execute(StateContext<LOADER_STATES, LOADER_EVENTS> context) {


        Long id = (Long) context.getExtendedState().getVariables().get("loadId");

        ConfigDTO errored = configService.findById(id);

        errored.setValue(String.format(ConfigService.CONFIG_DTO_VALUE_STR, configService.parseDate(errored).format(format1), configService.isPartial(errored), JobStatus.ERROR, LocalDateTime.now().format(formatMessage),configService.retryCount(errored)));
        configService.update(errored);

        context.getStateMachine().sendEvent(LOADER_EVENTS.ERROR_TREATED);
      }
    };
  }

  public void loadMarket(final String exchange, StateContext<LOADER_STATES, LOADER_EVENTS> context) {


    FirmService firmService = beanFactory.getBean(FirmService.class);
    LocalDate runDate = (LocalDate) context.getExtendedState().getVariables().get("runDate");


    logger.info(String.format("%s - %s - Starting load process", exchange, runDate.format(format1)));
    List<FirmQuoteDTO> firmsForGivenExchange = firmService.getExchangeDataForDate(runDate, exchange);

    int size = firmsForGivenExchange.size();

    for (int i = 0; i <= firmsForGivenExchange.size(); i++) {
      FirmQuoteDTO current = firmsForGivenExchange.get(i);

      loadAndSave(exchange, current, runDate, context);
      if (i != 0) {
        double percentDone = (i * 100 / size);
        if (i % 100 == 0) {

          LocalDateTime runTimeStart = (LocalDateTime) context.getExtendedState().getVariables().get("runTime");
          LocalDateTime runTimeEnd = LocalDateTime.now();

          Duration diff = Duration.between(runTimeStart, runTimeEnd);

          double percent = new BigDecimal(percentDone / 100).setScale(2, RoundingMode.HALF_UP).doubleValue();

          if (percent != 0) {
            int minutesLeft = new BigDecimal((diff.toMinutes() / percent)- diff.toMinutes()).setScale(2, RoundingMode.HALF_UP).intValue() ;
            logger.info(String.format("%s - %s treated in %s minutes. (%s%%). Expected end time int %s minutes ", exchange, i, diff.toMinutes(), percent*100, minutesLeft));
          } else {
            logger.info(String.format("%s - %s treated in %s minutes. (%s%%).", exchange, i, diff.toMinutes(), percent*100));
          }
        }
      }

    }

    logger.info(String.format("%s - %s - End load process", exchange, runDate.format(format1)));
  }

  @Bean
  public Action<LOADER_STATES, LOADER_EVENTS> cleanup() {
    return new Action<LOADER_STATES, LOADER_EVENTS>() {
      @Override
      public void execute(StateContext<LOADER_STATES, LOADER_EVENTS> context) {


        Long id = (Long) context.getMessageHeader("loadId");

        ConfigDTO current = configService.findById(id);

        current.setValue(String.format(ConfigService.CONFIG_DTO_VALUE_STR, configService.parseDate(current).format(format1), configService.isPartial(current), JobStatus.FINISHED, LocalDateTime.now().format(formatMessage),configService.retryCount(current)));
        configService.update(current);

        List<LocalDate> runDates = (List<LocalDate>) context.getExtendedState().getVariables().get("runDate");

        LocalDateTime runTimeStart = (LocalDateTime) context.getExtendedState().getVariables().get("runTime");
        LocalDateTime runTimeEnd = LocalDateTime.now();

        Duration diff = Duration.between(runTimeStart, runTimeEnd);

        String process = String.format("Loading ended. %s days treated in %s minutes", runDates.size(), diff.toMinutes());
        context.getStateMachine().sendEvent(LOADER_EVENTS.SUCCESS);

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
    logger.info(String.format("%s - Fin du traitement", runDate.format(format1)));
  }


  private void loadAndSave(String exchange, FirmQuoteDTO firm, LocalDate runDate, StateContext<LOADER_STATES, LOADER_EVENTS> context) {

    String type = "";
    if (LOADER_STATES.ERROR.equals(context.getStateMachine().getState().getId()))
      return;

    try {
      EODFirmFundamentalRepository eODFirmFundamentalRepository = beanFactory.getBean(EODFirmFundamentalRepository.class);
      Optional<String> typeOpt = eODFirmFundamentalRepository.getTypeByDateAndFirm(runDate, firm.getExchangeShortName(), firm.getCode());
      if (!typeOpt.isPresent())
        return;

      type = typeOpt.get();

    } catch (Exception e) {
      logger.severe(String.format("%s - %s - loadAndSave",firm.getExchangeShortName(),firm.getCode()));
      logger.severe(e.getMessage());
    }


    if (type.equals("Common Stock")) {


      try {
        FirmService firmService = beanFactory.getBean(FirmService.class);
        firmService.saveEODMarketQuotes(firm);
      } catch (Exception e) {
        logger.severe(" FirmService.saveAll");
        logger.severe(e.getMessage());
      }
      try {
        FirmInfoService firmInfoService = beanFactory.getBean(FirmInfoService.class);
        Optional<FirmInfoDTO> info = getFirmStockInfo(runDate, firm);
        if (info.isPresent())
          firmInfoService.save(info.get());
      } catch (Exception e) {
        logger.severe(" firmInfoService.saveAll");
        logger.severe(e.getMessage());
      }
      try {
        FirmValuationService firmValuationService = beanFactory.getBean(FirmValuationService.class);
        Optional<FirmValuationDTO> valuation = getFirmStockValuation(runDate, firm);
        if (valuation.isPresent())
          firmValuationService.save(valuation.get());
      } catch (Exception e) {
        logger.severe(" firmValuationService.saveAll");
        logger.severe(e.getMessage());
      }
      try {
        FirmSharesStatsService firmSharesStatsService = beanFactory.getBean(FirmSharesStatsService.class);

        Optional<FirmShareStatsDTO> shareStat = getFirmStockShareStats(runDate, firm);
        if (shareStat.isPresent())
          firmSharesStatsService.save(shareStat.get());

      } catch (Exception e) {
        logger.severe(" firmSharesStatsService.saveAll");
        logger.severe(e.getMessage());
      }
      try {
        FirmHighlightsService firmHighlightsService = beanFactory.getBean(FirmHighlightsService.class);
        Optional<FirmHighlightsDTO> highlight = getFirmStockHighLights(runDate, firm);
        if (highlight.isPresent())
          firmHighlightsService.save(highlight.get());

      } catch (Exception e) {
        logger.severe(" firmHighlightsService.saveAll");
        logger.severe(e.getMessage());
      }
    }


  }

  private Optional<FirmInfoDTO> getFirmStockInfo(LocalDate runDate, FirmQuoteDTO firmEODQuoteTO) {

    FirmInfoService firmInfoService = beanFactory.getBean(FirmInfoService.class);
    Optional<FirmInfoDTO> fIpost = firmInfoService.getInfosByDateAndExchangeAndFirm(runDate, firmEODQuoteTO.getExchangeShortName(), firmEODQuoteTO.getCode());
    if (fIpost.isPresent()) {
      fIpost.get().setCurrentExchange(firmEODQuoteTO.getActualExchange());
    }
    return fIpost;
  }


  private Optional<FirmValuationDTO> getFirmStockValuation(LocalDate runDate, FirmQuoteDTO firmEODQuoteTO) {

    FirmValuationService firmValuationService = beanFactory.getBean(FirmValuationService.class);
    Optional<FirmValuationDTO> fVpost = firmValuationService.getValuationByDateAndFirm(runDate, firmEODQuoteTO.getExchangeShortName(), firmEODQuoteTO.getCode());
    return fVpost;
  }

  private Optional<FirmHighlightsDTO> getFirmStockHighLights(LocalDate runDate, FirmQuoteDTO firmEODQuoteTO) {

    FirmHighlightsService firmHighlightsService = beanFactory.getBean(FirmHighlightsService.class);
    Optional<FirmHighlightsDTO> fHpost = firmHighlightsService.getHighlightsByDateAndFirm(runDate, firmEODQuoteTO.getExchangeShortName(), firmEODQuoteTO.getCode());
    return fHpost;

  }

  private Optional<FirmShareStatsDTO> getFirmStockShareStats(LocalDate runDate, FirmQuoteDTO firmEODQuoteTO) {

    FirmSharesStatsService firmSharesStatsService = beanFactory.getBean(FirmSharesStatsService.class);
    Optional<FirmShareStatsDTO> fSpost = firmSharesStatsService.getSharesStatByDateAndFirm(runDate, firmEODQuoteTO.getExchangeShortName(), firmEODQuoteTO.getCode());
    return fSpost;
  }


}

