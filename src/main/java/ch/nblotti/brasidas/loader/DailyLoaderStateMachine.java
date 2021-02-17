package ch.nblotti.brasidas.loader;

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
import ch.nblotti.brasidas.index.composition.IndexCompositionDTO;
import ch.nblotti.brasidas.index.composition.IndexCompositionService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import javax.annotation.PostConstruct;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Configuration
@EnableStateMachine
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DailyLoaderStateMachine extends EnumStateMachineConfigurerAdapter<LOADER_STATES, LOADER_EVENTS> {


  private static final Logger logger = Logger.getLogger("DailyLoaderStateMachine");

  private final static String EXCHANGE_NYSE = "NYSE";
  private final static String EXCHANGE_NASDAQ = "NASDAQ";

  @Autowired
  private DateTimeFormatter format1;

  @Autowired
  private DateTimeFormatter formatMessage;


  public static final String EVENT_MESSAGE_DAY = "firms";

  @Value("${index.list}")
  private String indexList;


  @Value("${nyse.closed.days}")
  public String nyseClosedDays;

  @Autowired
  IndexCompositionService indexCompositionService;

  @Autowired
  private BeanFactory beanFactory;

  /*
@Autowired
JpaDao jpaDao;
 */
  @PostConstruct
  public void initFFirmHistorical() {

//TODO NBL
  }


  @Override
  public void configure(
    StateMachineConfigurationConfigurer
      <LOADER_STATES, LOADER_EVENTS> config) throws Exception {

    config.withConfiguration()
      .autoStartup(true);
  }

  @Override
  public void configure(StateMachineStateConfigurer<LOADER_STATES, LOADER_EVENTS> states) throws Exception {
    states.withStates()
      .initial(LOADER_STATES.READY, initalAction())
      .state(LOADER_STATES.GET_DATES, getDates())
      .state(LOADER_STATES.LOAD_NYSE, loadNYSE())
      .state(LOADER_STATES.LOAD_NASDAQ, loadNASDAQ())
      .state(LOADER_STATES.LOAD_INDEX, loadIndex())
      .state(LOADER_STATES.SAVE_FIRM, saveFirms())
      .state(LOADER_STATES.REFRESH_MAT_VIEWS, refreshMaterializedViews())
      .end(LOADER_STATES.DONE)
      .end(LOADER_STATES.ERROR);


  }


  @Override
  public void configure(StateMachineTransitionConfigurer<LOADER_STATES, LOADER_EVENTS> transitions) throws Exception {
    transitions.withExternal()
      .source(LOADER_STATES.READY).target(LOADER_STATES.GET_DATES).event(LOADER_EVENTS.EVENT_RECEIVED)
      .and()
      .withExternal()
      .source(LOADER_STATES.READY).target(LOADER_STATES.ERROR).event(LOADER_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(LOADER_STATES.GET_DATES).target(LOADER_STATES.LOAD_NYSE).event(LOADER_EVENTS.WEEK)
      .and()
      .withExternal()
      .source(LOADER_STATES.GET_DATES).target(LOADER_STATES.DONE).event(LOADER_EVENTS.END_OF_WEEK_OR_DAY_OFF)
      .and()
      .withExternal()
      .source(LOADER_STATES.GET_DATES).target(LOADER_STATES.ERROR).event(LOADER_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(LOADER_STATES.LOAD_NYSE).target(LOADER_STATES.LOAD_NASDAQ)
      .and()
      .withLocal()
      .source(LOADER_STATES.LOAD_NYSE).target(LOADER_STATES.ERROR).event(LOADER_EVENTS.ERROR)
      .and()
      .withLocal()
      .source(LOADER_STATES.LOAD_NASDAQ).target(LOADER_STATES.SAVE_FIRM)
      .and()
      .withLocal()
      .source(LOADER_STATES.LOAD_NASDAQ).target(LOADER_STATES.ERROR).event(LOADER_EVENTS.ERROR)
      .and()
      .withLocal()
      .source(LOADER_STATES.SAVE_FIRM).target(LOADER_STATES.LOAD_INDEX)
      .and()
      .withLocal()
      .source(LOADER_STATES.SAVE_FIRM).target(LOADER_STATES.ERROR).event(LOADER_EVENTS.ERROR)
      .and()
      .withLocal()
      .source(LOADER_STATES.LOAD_INDEX).target(LOADER_STATES.REFRESH_MAT_VIEWS)
      .and()
      .withLocal()
      .source(LOADER_STATES.LOAD_INDEX).target(LOADER_STATES.ERROR).event(LOADER_EVENTS.ERROR)
      .and()
      .withLocal()
      .source(LOADER_STATES.REFRESH_MAT_VIEWS).target(LOADER_STATES.DONE)
      .and()
      .withLocal()
      .source(LOADER_STATES.REFRESH_MAT_VIEWS).target(LOADER_STATES.ERROR);
  }


  @Bean
  public Action<LOADER_STATES, LOADER_EVENTS> initalAction() {
    return new Action<LOADER_STATES, LOADER_EVENTS>() {

      @Override
      public void execute(StateContext<LOADER_STATES, LOADER_EVENTS> context) {

        context.getExtendedState().getVariables().put("runDate", LocalDate.now().minusDays(1));
        context.getExtendedState().getVariables().put("runPartial", Boolean.TRUE);
        context.getExtendedState().getVariables().put("runTime", LocalDateTime.now());

      }
    };
  }


  @Bean
  public Action<LOADER_STATES, LOADER_EVENTS> getDates() {
    return new Action<LOADER_STATES, LOADER_EVENTS>() {

      @Override
      public void execute(StateContext<LOADER_STATES, LOADER_EVENTS> context) {

        Message<LOADER_EVENTS> message;

        LocalDate runDate = (LocalDate) context.getMessageHeader("runDate");
        logger.info(String.format("%s - Début du traitement", runDate.format(format1)));

        if (runDate == null)
          runDate = (LocalDate) context.getExtendedState().getVariables().get("runDate");
        else
          context.getExtendedState().getVariables().put("runDate", runDate);

        Boolean runPartial = (Boolean) context.getMessageHeader("runPartial");

        if (runPartial == null)
          runPartial = (Boolean) context.getExtendedState().getVariables().get("runPartial");
        else
          context.getExtendedState().getVariables().put("runPartial", runPartial);

        if (runDate.getDayOfMonth() == 1) {
          String[] indexes = getIndexList();
          for (String index : indexes)
            indexCompositionService.loadAndSaveIndexCompositionAtDate(runDate, index);
        }


        //on est un jour de week-end (dimanche - lundi) ou hier était férié
        if (runDate.getDayOfWeek() == DayOfWeek.SATURDAY
          || runDate.getDayOfWeek() == DayOfWeek.SUNDAY
          || wasDayBeforeRunDateDayDayOff(runDate)) {
          message = MessageBuilder
            .withPayload(LOADER_EVENTS.END_OF_WEEK_OR_DAY_OFF)
            .build();
        } else
        //on est dimanche ou lundi ou hier était férié
        {
          message = MessageBuilder
            .withPayload(LOADER_EVENTS.WEEK)
            .build();
        }
        context.getStateMachine().sendEvent(message);
      }
    };
  }

  private String[] getIndexList() {
    return indexList.split(",");
  }


  private boolean wasDayBeforeRunDateDayDayOff(LocalDate runDate) {
    return false;
  }


  @Bean
  public Action<LOADER_STATES, LOADER_EVENTS> loadNYSE() {
    return new Action<LOADER_STATES, LOADER_EVENTS>() {

      @Override
      public void execute(StateContext<LOADER_STATES, LOADER_EVENTS> stateContext) {
        loadMarket(DailyLoaderStateMachine.this.EXCHANGE_NYSE, stateContext);
      }
    };

  }

  @Bean
  public Action<LOADER_STATES, LOADER_EVENTS> loadNASDAQ() {
    return new Action<LOADER_STATES, LOADER_EVENTS>() {
      @Override
      public void execute(StateContext<LOADER_STATES, LOADER_EVENTS> stateContext) {
        loadMarket(DailyLoaderStateMachine.this.EXCHANGE_NASDAQ, stateContext);
      }

    };

  }

  public void loadMarket(final String exchange, StateContext<LOADER_STATES, LOADER_EVENTS> context) {


    FirmService firmService = beanFactory.getBean(FirmService.class);
    LocalDate runDate = (LocalDate) context.getExtendedState().getVariables().get("runDate");
    Collection<FirmQuoteDTO> firmSaved = (List<FirmQuoteDTO>) context.getExtendedState().getVariables().get("quotes");
    if (firmSaved == null) {
      firmSaved = new ArrayList<>();
    }


    Collection<FirmQuoteDTO> firms = firmService.getExchangeDataForDate(runDate, exchange);

    for (FirmQuoteDTO firmQuoteDTO : firms) {
      if (!firmQuoteDTO.getCode().startsWith("-")) {
        firmSaved.add(firmQuoteDTO);
      }
    }
    ;

    context.getExtendedState()
      .getVariables().put("quotes", firmSaved);
  }


  @Bean
  public Action<LOADER_STATES, LOADER_EVENTS> saveFirms() {
    return new Action<LOADER_STATES, LOADER_EVENTS>() {

      @Override
      public void execute(StateContext<LOADER_STATES, LOADER_EVENTS> context) {

        FirmService firmService = beanFactory.getBean(FirmService.class);
        LocalDate runDate = (LocalDate) context.getExtendedState().getVariables().get("runDate");
        Boolean runPartial = (Boolean) context.getExtendedState().getVariables().get("runPartial");

        List<FirmQuoteDTO> firms = (List<FirmQuoteDTO>) context.getExtendedState().getVariables().get("quotes");
        firmService.saveAllEODMarketQuotes(firms);

        if (Boolean.FALSE == runPartial)
          loadDetails(firms, runDate, context);

      }
    };
  }

  @Bean
  public Action<LOADER_STATES, LOADER_EVENTS> loadIndex() {
    return new Action<LOADER_STATES, LOADER_EVENTS>() {

      @Override
      public void execute(StateContext<LOADER_STATES, LOADER_EVENTS> context) {

        IndexCompositionService indexCompositionService = beanFactory.getBean(IndexCompositionService.class);
        LocalDate runDate = (LocalDate) context.getExtendedState().getVariables().get("runDate");
        String[] indexes = getIndexList();
        for (String index : indexes) {
          List<IndexCompositionDTO> indexQuoteTO = indexCompositionService.getIndexDataByDate(runDate, index);
          indexCompositionService.saveIndexComposition(indexQuoteTO);
        }

        finalAction(runDate);
      }
    };
  }


  @Bean
  public Action<LOADER_STATES, LOADER_EVENTS> refreshMaterializedViews() {
    return new Action<LOADER_STATES, LOADER_EVENTS>() {
      @Override
      public void execute(StateContext<LOADER_STATES, LOADER_EVENTS> context) {

        LocalDateTime runTimeStart = (LocalDateTime) context.getExtendedState().getVariables().get("runTime");
        LocalDate runDate = (LocalDate) context.getExtendedState().getVariables().get("runDate");
        LocalDateTime runTimeEnd = LocalDateTime.now();
        String process = String.format("Daily loading process runDate = %s", runDate.format(format1));

      }
    };
  }

  public void finalAction(LocalDate runDate) {
    logger.info(String.format("%s - Fin du traitement", runDate.format(format1)));
  }


  private void loadDetails(List<FirmQuoteDTO> firms, LocalDate runDate, StateContext<LOADER_STATES, LOADER_EVENTS> context) {

    List<FirmInfoDTO> firmInfos = new ArrayList<>();
    List<FirmValuationDTO> firmValuations = new ArrayList<>();
    List<FirmHighlightsDTO> firmHighlights = new ArrayList<>();
    List<FirmShareStatsDTO> firmSharesStats = new ArrayList<>();


    for (FirmQuoteDTO firmEODQuoteTO : firms) {


      if (LOADER_STATES.ERROR.equals(context.getStateMachine().getState().getId()))
        return;

      EODFirmFundamentalRepository eODFirmFundamentalRepository = beanFactory.getBean(EODFirmFundamentalRepository.class);
      Optional<String> type = eODFirmFundamentalRepository.getTypeByDateAndFirm(runDate, firmEODQuoteTO.getExchangeShortName(), firmEODQuoteTO.getCode());

      if (!type.isPresent())
        continue;

      if (type.get().equals("Common Stock")) {
        Optional<FirmInfoDTO> info = getFirmStockInfo(runDate, firmEODQuoteTO);
        if (info.isPresent())
          firmInfos.add(info.get());
        Optional<FirmValuationDTO> valuation = getFirmStockValuation(runDate, firmEODQuoteTO);
        if (valuation.isPresent())
          firmValuations.add(valuation.get());
        Optional<FirmHighlightsDTO> highlight = getFirmStockHighLights(runDate, firmEODQuoteTO);
        if (highlight.isPresent())
          firmHighlights.add(highlight.get());
        Optional<FirmShareStatsDTO> shareStat = getFirmStockShareStats(runDate, firmEODQuoteTO);
        if (shareStat.isPresent())
          firmSharesStats.add(shareStat.get());
      }
    }
    try {
      FirmInfoService firmInfoService = beanFactory.getBean(FirmInfoService.class);
      firmInfoService.saveAll(firmInfos);
    } catch (Exception e) {
      logger.severe(" firmInfoService.saveAll");
      logger.severe(e.getMessage());
    }
    try {
      FirmValuationService firmValuationService = beanFactory.getBean(FirmValuationService.class);
      firmValuationService.saveAll(firmValuations);
    } catch (Exception e) {
      logger.severe(" firmValuationService.saveAll");
      logger.severe(e.getMessage());
    }
    try {
      FirmSharesStatsService firmSharesStatsService = beanFactory.getBean(FirmSharesStatsService.class);
      firmSharesStatsService.saveAll(firmSharesStats);
    } catch (Exception e) {
      logger.severe(" firmSharesStatsService.saveAll");
      logger.severe(e.getMessage());
    }
    try {
      FirmHighlightsService firmHighlightsService = beanFactory.getBean(FirmHighlightsService.class);
      firmHighlightsService.saveAll(firmHighlights);
    } catch (Exception e) {
      logger.severe(" firmHighlightsService.saveAll");
      logger.severe(e.getMessage());
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

