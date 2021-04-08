package ch.nblotti.brasidas.exchange.firmloader;

import ch.nblotti.brasidas.configuration.ConfigDTO;
import ch.nblotti.brasidas.configuration.JobStatus;
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
import lombok.extern.slf4j.Slf4j;
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

@Configuration
@Slf4j
@EnableStateMachine(name = "marketLoaderStateMachine")
public class MarketLoader extends EnumStateMachineConfigurerAdapter<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> {


  private final static String EXCHANGE_NYSE = "NYSE";
  private final static String EXCHANGE_NASDAQ = "NASDAQ";

  private static final int MIN_FIRM = 2000;

  @Autowired
  private DateTimeFormatter format1;

  @Autowired
  private DateTimeFormatter formatMessage;


  private static final String LOADER = "LOADER";
  private static final String RUNNING_JOBS = "RUNNING_JOBS";

  @Autowired
  private MarketLoadConfigService marketLoadConfigService;


  public static final String EVENT_MESSAGE_DAY = "firms";

  @Value("${index.list}")
  private String indexList;



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
      <MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> config) throws Exception {

    config.withConfiguration()
      .autoStartup(false)
      .taskExecutor(myAsyncTaskExecutor()).taskScheduler(new ConcurrentTaskScheduler());

  }

  @Override
  public void configure(StateMachineStateConfigurer<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> states) throws Exception {
    states.withStates()
      .initial(MARKET_LOADER_STATES.WAITING_EVENT)
      .state(MARKET_LOADER_STATES.READY, initalAction())
      .state(MARKET_LOADER_STATES.GET_DATES, filterRunDates())
      .fork(MARKET_LOADER_STATES.LOAD_FORK)
      .state(MARKET_LOADER_STATES.LOAD)
      .join(MARKET_LOADER_STATES.LOAD_JOIN)
      .choice(MARKET_LOADER_STATES.CHOICE)
      .state(MARKET_LOADER_STATES.CLEANUP, cleanup())
      .state(MARKET_LOADER_STATES.ERROR_STATE, errorAction())
      .end(MARKET_LOADER_STATES.DONE)
      .end(MARKET_LOADER_STATES.ERROR)
      .and()
      .withStates()
      .parent(MARKET_LOADER_STATES.LOAD)
      .initial(MARKET_LOADER_STATES.LOAD_NYSE, loadNYSE())
      .end(MARKET_LOADER_STATES.LOAD_NYSE_END)
      .and()
      .withStates()
      .parent(MARKET_LOADER_STATES.LOAD)
      .initial(MARKET_LOADER_STATES.LOAD_NASDAQ, loadNASDAQ())
      .end(MARKET_LOADER_STATES.LOAD_NASDAQ_END);


  }


  @Override
  public void configure(StateMachineTransitionConfigurer<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> transitions) throws Exception {
    transitions.withExternal()
      .source(MARKET_LOADER_STATES.WAITING_EVENT).target(MARKET_LOADER_STATES.READY)
      .and()
      .withExternal()
      .source(MARKET_LOADER_STATES.READY).target(MARKET_LOADER_STATES.GET_DATES).event(MARKET_LOADER_EVENTS.EVENT_RECEIVED)
      .and()
      .withExternal()
      .source(MARKET_LOADER_STATES.GET_DATES).target(MARKET_LOADER_STATES.LOAD_FORK).event(MARKET_LOADER_EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(MARKET_LOADER_STATES.GET_DATES).target(MARKET_LOADER_STATES.DONE).event(MARKET_LOADER_EVENTS.ERROR)
      .and()
      .withFork()
      .source(MARKET_LOADER_STATES.LOAD_FORK).target(MARKET_LOADER_STATES.LOAD)
      .and()
      .withExternal()
      .source(MARKET_LOADER_STATES.LOAD_NASDAQ).target(MARKET_LOADER_STATES.LOAD_NASDAQ_END)
      .and()
      .withExternal()
      .source(MARKET_LOADER_STATES.LOAD_NYSE).target(MARKET_LOADER_STATES.LOAD_NYSE_END)
      .and()
      .withJoin()
      .source(MARKET_LOADER_STATES.LOAD).target(MARKET_LOADER_STATES.LOAD_JOIN)
      .and()
      .withExternal()
      .source(MARKET_LOADER_STATES.LOAD_JOIN).target(MARKET_LOADER_STATES.CHOICE)
      .and()
      .withChoice()
      .source(MARKET_LOADER_STATES.CHOICE)
      .first(MARKET_LOADER_STATES.CLEANUP, tasksChoiceGuard())
      .last(MARKET_LOADER_STATES.ERROR_STATE)
      .and()
      .withExternal()
      .source(MARKET_LOADER_STATES.CLEANUP).target(MARKET_LOADER_STATES.WAITING_EVENT).event(MARKET_LOADER_EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(MARKET_LOADER_STATES.CLEANUP).target(MARKET_LOADER_STATES.ERROR_STATE).event(MARKET_LOADER_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(MARKET_LOADER_STATES.ERROR_STATE).target(MARKET_LOADER_STATES.WAITING_EVENT).event(MARKET_LOADER_EVENTS.ERROR_TREATED)
      .and()
      .withExternal()
      .source(MARKET_LOADER_STATES.ERROR_STATE).target(MARKET_LOADER_STATES.ERROR).event(MARKET_LOADER_EVENTS.ERROR)

    ;
  }


  @Bean
  public Action<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> initalAction() {
    return new Action<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS>() {

      @Override
      public void execute(StateContext<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> context) {


      }
    };
  }


  @Bean
  public Action<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> filterRunDates() {
    return new Action<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS>() {

      @Override
      public void execute(StateContext<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> context) {

        Message<MARKET_LOADER_EVENTS> message;

        LocalDate runDate = (LocalDate) context.getMessageHeader("runDate");


        Long id = (Long) context.getMessageHeader("loadId");
        context.getExtendedState().getVariables().put("loadId", id);
        context.getExtendedState().getVariables().put("runTime", LocalDateTime.now());

        Map<Object, Object> variables = context.getExtendedState().getVariables();
        variables.put("T1", true);
        variables.put("T2", true);


        ConfigDTO current = marketLoadConfigService.findById(id);

        Boolean partial = marketLoadConfigService.isPartial(current);
        context.getExtendedState().getVariables().put("partial", partial);

        current.setValue(String.format(MarketLoadConfigService.CONFIG_DTO_VALUE_STR, marketLoadConfigService.parseDate(current).format(format1), partial, JobStatus.RUNNING, LocalDateTime.now().format(formatMessage), marketLoadConfigService.retryCount(current) + 1));
        marketLoadConfigService.update(current);


        if (runDate == null) {
          message = MessageBuilder
            .withPayload(MARKET_LOADER_EVENTS.ERROR)
            .build();
        } else {
          context.getExtendedState().getVariables().put("runDate", runDate);

          message = MessageBuilder
            .withPayload(MARKET_LOADER_EVENTS.SUCCESS)
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
  public Action<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> loadNYSE() {
    return new Action<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS>() {

      @Override
      public void execute(StateContext<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> stateContext) {

        Map<Object, Object> variables = stateContext.getExtendedState().getVariables();

        try {
          loadMarket(MarketLoader.this.EXCHANGE_NYSE, stateContext);
        } catch (Exception ex) {
          variables.put("T1", false);
          log.error(ex.getMessage());
          return;
        }

      }
    };

  }

  @Bean
  public Action<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> loadNASDAQ() {
    return new Action<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS>() {
      @Override
      public void execute(StateContext<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> stateContext) {

        Map<Object, Object> variables = stateContext.getExtendedState().getVariables();

        try {
          loadMarket(MarketLoader.this.EXCHANGE_NASDAQ, stateContext);
        } catch (Exception ex) {
          variables.put("T2", false);
          log.error(ex.getMessage());
          return;
        }

      }
    };

  }

  @Bean
  public Guard<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> tasksChoiceGuard() {
    return new Guard<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS>() {

      @Override
      public boolean evaluate(StateContext<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> context) {

        return isMarketLoadingProcessStatusStillValid(context);
      }
    };
  }

  private boolean isMarketLoadingProcessStatusStillValid(StateContext<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> context) {
    Map<Object, Object> variables = context.getExtendedState().getVariables();
    return ObjectUtils.nullSafeEquals(variables.get("T1"), true)
      && ObjectUtils.nullSafeEquals(variables.get("T2"), true);
  }

  @Bean
  public Action<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> errorAction() {
    return new Action<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS>() {

      @Override
      public void execute(StateContext<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> context) {


        Long id = (Long) context.getExtendedState().getVariables().get("loadId");

        ConfigDTO errored = marketLoadConfigService.findById(id);

        errored.setValue(String.format(MarketLoadConfigService.CONFIG_DTO_VALUE_STR, marketLoadConfigService.parseDate(errored).format(format1), marketLoadConfigService.isPartial(errored), JobStatus.ERROR, LocalDateTime.now().format(formatMessage), marketLoadConfigService.retryCount(errored)));
        marketLoadConfigService.update(errored);

        context.getStateMachine().sendEvent(MARKET_LOADER_EVENTS.ERROR_TREATED);
      }
    };
  }

  public void loadMarket(final String exchange, StateContext<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> context) {


    FirmService firmService = beanFactory.getBean(FirmService.class);
    LocalDate runDate = (LocalDate) context.getExtendedState().getVariables().get("runDate");
    LocalDateTime runTimeStart = (LocalDateTime) context.getExtendedState().getVariables().get("runTime");


    log.info(String.format("%s - %s - Starting load process", exchange, runDate.format(format1)));
    int maxretry = 1;

    List<ExchangeFirmQuoteDTO> firmsForGivenExchange = new ArrayList<>();
    while (firmsForGivenExchange.isEmpty() && maxretry <= 3)
      try {
        firmsForGivenExchange = firmService.getExchangeDataForDate(runDate, exchange);
      } catch (Exception ex) {
        log.error(String.format("Error accessing market data (%s try)", maxretry));
        log.error(ex.toString());
        maxretry++;
      }

    int size = firmsForGivenExchange.size();

    if (size < MIN_FIRM) {
      throw new IllegalStateException("Not enough firm found to start job");
    }
    for (
      int i = 0; i < firmsForGivenExchange.size(); i++) {
      ExchangeFirmQuoteDTO current = firmsForGivenExchange.get(i);

      if (!isMarketLoadingProcessStatusStillValid(context))
        throw new IllegalStateException(String.format("%s - %s Job stopped in another thread, exiting", runDate.format(format1), exchange));

      loadAndSave(exchange, current, runDate, context);
      if (i != 0) {
        double percentDone = (i * 100 / size);
        if (i % 100 == 0) {


          LocalDateTime runTimeEnd = LocalDateTime.now();

          Duration diff = Duration.between(runTimeStart, runTimeEnd);

          double percent = new BigDecimal(percentDone / 100).setScale(2, RoundingMode.HALF_UP).doubleValue();

          if (percent != 0) {
            int minutesLeft = new BigDecimal((diff.toMinutes() / percent) - diff.toMinutes()).setScale(2, RoundingMode.HALF_UP).intValue();
            log.info(String.format("%s - %s - %s/%s treated in %s minutes. (%s%%). Expected end time in %s minutes ", runDate.format(format1), exchange, i, size, diff.toMinutes(), percent * 100, minutesLeft));
          } else {
            log.info(String.format("%s - %s - %s/%s treated in %s minutes. (%s%%).", runDate.format(format1), exchange, i, size, diff.toMinutes(), percent * 100));
          }
        }
      }

    }

    LocalDateTime runTimeEnd = LocalDateTime.now();
    Duration diff = Duration.between(runTimeStart, runTimeEnd);

    log.info(String.format("%s - %s - End load process. %s treated in %s minutes", runDate.format(format1), exchange, size, diff.toMinutes()));
  }

  @Bean
  public Action<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> cleanup() {
    return new Action<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS>() {
      @Override
      public void execute(StateContext<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> context) {


        Long id = (Long) context.getExtendedState().getVariables().get("loadId");

        ConfigDTO current = marketLoadConfigService.findById(id);

        current.setValue(String.format(MarketLoadConfigService.CONFIG_DTO_VALUE_STR, marketLoadConfigService.parseDate(current).format(format1), marketLoadConfigService.isPartial(current), JobStatus.FINISHED, LocalDateTime.now().format(formatMessage), marketLoadConfigService.retryCount(current)));
        marketLoadConfigService.update(current);

        LocalDate runDate = (LocalDate) context.getExtendedState().getVariables().get("runDate");

        LocalDateTime runTimeStart = (LocalDateTime) context.getExtendedState().getVariables().get("runTime");
        LocalDateTime runTimeEnd = LocalDateTime.now();

        Duration diff = Duration.between(runTimeStart, runTimeEnd);

        String process = String.format("Loading ended in %s minutes", diff.toMinutes());
        context.getStateMachine().sendEvent(MARKET_LOADER_EVENTS.SUCCESS);

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


  private void loadAndSave(String exchange, ExchangeFirmQuoteDTO firm, LocalDate runDate, StateContext<MARKET_LOADER_STATES, MARKET_LOADER_EVENTS> context) {

    String type = "";
    Boolean partial = (Boolean) context.getExtendedState().getVariables().get("partial");

    if (MARKET_LOADER_STATES.ERROR.equals(context.getStateMachine().getState().getId()))
      return;

    try {
      EODFirmFundamentalRepository eODFirmFundamentalRepository = beanFactory.getBean(EODFirmFundamentalRepository.class);
      Optional<String> typeOpt = eODFirmFundamentalRepository.getTypeByDateAndFirm(runDate, firm.getExchangeShortName(), firm.getCode());
      if (!typeOpt.isPresent())
        return;

      type = typeOpt.get();

    } catch (Exception e) {
      log.error(String.format("%s - %s - loadAndSave", firm.getExchangeShortName(), firm.getCode()));
      log.error(e.getMessage());
    }


    if (type.equals("Common Stock")) {

      String currentCode = firm.getCode();
      try {
        FirmService firmService = beanFactory.getBean(FirmService.class);
        firmService.saveEODMarketQuotes(firm);
      } catch (Exception e) {
        log.error(String.format("%s - %s -  Error saving quote", exchange, currentCode));
        log.error(e.getMessage());
      }
      if (partial)
        return;

      try {
        FirmInfoService firmInfoService = beanFactory.getBean(FirmInfoService.class);
        Optional<FirmInfoDTO> info = getFirmStockInfo(runDate, firm);
        if (info.isPresent())
          firmInfoService.save(info.get());
      } catch (Exception e) {
        log.error(String.format("%s - %s -  Error saving info", exchange, currentCode));
        log.error(e.getMessage());
      }
      try {
        FirmValuationService firmValuationService = beanFactory.getBean(FirmValuationService.class);
        Optional<FirmValuationDTO> valuation = getFirmStockValuation(runDate, firm);
        if (valuation.isPresent())
          firmValuationService.save(valuation.get());
      } catch (Exception e) {
        log.error(String.format("%s - %s -  Error saving valuation", exchange, currentCode));
        log.error(e.getMessage());
      }
      try {
        FirmSharesStatsService firmSharesStatsService = beanFactory.getBean(FirmSharesStatsService.class);

        Optional<FirmShareStatsDTO> shareStat = getFirmStockShareStats(runDate, firm);
        if (shareStat.isPresent())
          firmSharesStatsService.save(shareStat.get());

      } catch (Exception e) {
        log.error(String.format("%s - %s -  Error saving shareStats", exchange, currentCode));
        log.error(e.getMessage());
      }
      try {
        FirmHighlightsService firmHighlightsService = beanFactory.getBean(FirmHighlightsService.class);
        Optional<FirmHighlightsDTO> highlight = getFirmStockHighLights(runDate, firm);
        if (highlight.isPresent())
          firmHighlightsService.save(highlight.get());

      } catch (Exception e) {
        log.error(String.format("%s - %s -  Error saving highlights", exchange, currentCode));
        log.error(e.getMessage());
      }
    }


  }

  private Optional<FirmInfoDTO> getFirmStockInfo(LocalDate runDate, ExchangeFirmQuoteDTO firmEODQuoteTO) {

    FirmInfoService firmInfoService = beanFactory.getBean(FirmInfoService.class);
    Optional<FirmInfoDTO> fIpost = firmInfoService.getInfosByDateAndExchangeAndFirm(runDate, firmEODQuoteTO.getExchangeShortName(), firmEODQuoteTO.getCode());
    if (fIpost.isPresent()) {
      fIpost.get().setCurrentExchange(firmEODQuoteTO.getActualExchange());
    }
    return fIpost;
  }


  private Optional<FirmValuationDTO> getFirmStockValuation(LocalDate runDate, ExchangeFirmQuoteDTO firmEODQuoteTO) {

    FirmValuationService firmValuationService = beanFactory.getBean(FirmValuationService.class);
    Optional<FirmValuationDTO> fVpost = firmValuationService.getValuationByDateAndFirm(runDate, firmEODQuoteTO.getExchangeShortName(), firmEODQuoteTO.getCode());
    return fVpost;
  }

  private Optional<FirmHighlightsDTO> getFirmStockHighLights(LocalDate runDate, ExchangeFirmQuoteDTO firmEODQuoteTO) {

    FirmHighlightsService firmHighlightsService = beanFactory.getBean(FirmHighlightsService.class);
    Optional<FirmHighlightsDTO> fHpost = firmHighlightsService.getHighlightsByDateAndFirm(runDate, firmEODQuoteTO.getExchangeShortName(), firmEODQuoteTO.getCode());
    return fHpost;

  }

  private Optional<FirmShareStatsDTO> getFirmStockShareStats(LocalDate runDate, ExchangeFirmQuoteDTO firmEODQuoteTO) {

    FirmSharesStatsService firmSharesStatsService = beanFactory.getBean(FirmSharesStatsService.class);
    Optional<FirmShareStatsDTO> fSpost = firmSharesStatsService.getSharesStatByDateAndFirm(runDate, firmEODQuoteTO.getExchangeShortName(), firmEODQuoteTO.getCode());
    return fSpost;
  }


}

