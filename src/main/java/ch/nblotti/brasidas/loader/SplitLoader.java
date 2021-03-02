package ch.nblotti.brasidas.loader;

import ch.nblotti.brasidas.configuration.ConfigDTO;
import ch.nblotti.brasidas.exchange.firm.ExchangeFirmQuoteDTO;
import ch.nblotti.brasidas.exchange.firm.FirmQuoteDTO;
import ch.nblotti.brasidas.exchange.firm.FirmService;
import ch.nblotti.brasidas.exchange.split.FirmSplitDTO;
import ch.nblotti.brasidas.exchange.split.FirmSplitService;
import ch.nblotti.brasidas.exchange.split.SplitConfigService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Configuration
@EnableStateMachine(name = "splitStateMachine")
public class SplitLoader extends EnumStateMachineConfigurerAdapter<SPLIT_STATES, SPLIT_EVENTS> {


  private static final Logger logger = Logger.getLogger("SplitLoader");


  @Autowired
  private DateTimeFormatter format1;

  @Autowired
  private DateTimeFormatter formatMessage;


  private static final String EXCHANGE = "US";

  private static final String SPLIT = "SPLIT";
  private static final String SPLIT_JOBS = "SPLIT_JOBS";

  @Autowired
  private LoadConfigService loadConfigService;


  @Autowired
  private BeanFactory beanFactory;


  @Autowired
  FirmSplitService firmSplitService;
  @Autowired
  FirmService firmService;


  @Override
  public void configure(
    StateMachineConfigurationConfigurer
      <SPLIT_STATES, SPLIT_EVENTS> config) throws Exception {

    config.withConfiguration()
      .autoStartup(false);
  }

  @Override
  public void configure(StateMachineStateConfigurer<SPLIT_STATES, SPLIT_EVENTS> states) throws Exception {
    states.withStates()
      .initial(SPLIT_STATES.WAITING_EVENT)
      .state(SPLIT_STATES.READY)
      .state(SPLIT_STATES.LOAD_SPLITS, loadSplits())
      .state(SPLIT_STATES.UPDATE_QUOTES, updateQuotes())
      .state(SPLIT_STATES.ERROR_STATE, errorState())
      .end(SPLIT_STATES.DONE)
      .end(SPLIT_STATES.CANCELED);

  }


  @Override
  public void configure(StateMachineTransitionConfigurer<SPLIT_STATES, SPLIT_EVENTS> transitions) throws Exception {
    transitions.withExternal()
      .source(SPLIT_STATES.WAITING_EVENT).target(SPLIT_STATES.READY)
      .and()
      .withExternal()
      .source(SPLIT_STATES.READY).target(SPLIT_STATES.LOAD_SPLITS).event(SPLIT_EVENTS.EVENT_RECEIVED)
      .and()
      .withExternal()
      .source(SPLIT_STATES.LOAD_SPLITS).target(SPLIT_STATES.UPDATE_QUOTES).event(SPLIT_EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(SPLIT_STATES.LOAD_SPLITS).target(SPLIT_STATES.ERROR_STATE).event(SPLIT_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(SPLIT_STATES.UPDATE_QUOTES).target(SPLIT_STATES.DONE).event(SPLIT_EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(SPLIT_STATES.UPDATE_QUOTES).target(SPLIT_STATES.ERROR_STATE).event(SPLIT_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(SPLIT_STATES.ERROR_STATE).target(SPLIT_STATES.WAITING_EVENT).event(SPLIT_EVENTS.ERROR_TREATED)
      .and()
      .withExternal()
      .source(SPLIT_STATES.ERROR_STATE).target(SPLIT_STATES.CANCELED).event(SPLIT_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(SPLIT_STATES.CANCELED).target(SPLIT_STATES.WAITING_EVENT)
      .and()
      .withExternal()
      .source(SPLIT_STATES.DONE).target(SPLIT_STATES.WAITING_EVENT);


    ;
  }


  @Bean
  public Action<SPLIT_STATES, SPLIT_EVENTS> loadSplits() {
    return new Action<SPLIT_STATES, SPLIT_EVENTS>() {

      @Override
      public void execute(StateContext<SPLIT_STATES, SPLIT_EVENTS> context) {

        Message<SPLIT_EVENTS> message;
        List<FirmSplitDTO> firmSplits;

        LocalDate runDate = (LocalDate) context.getMessageHeader("runDate");
        Long id = (Long) context.getMessageHeader("splitId");
        context.getExtendedState().getVariables().put("splitId", id);

        ConfigDTO current = loadConfigService.findById(id);

        current.setValue(String.format(SplitConfigService.CONFIG_DTO_VALUE_STR, loadConfigService.parseDate(current).format(format1), JobStatus.RUNNING, LocalDateTime.now().format(formatMessage), loadConfigService.retryCount(current)+1));
        loadConfigService.update(current);


        try {
          if (runDate == null) {
            message = MessageBuilder
              .withPayload(SPLIT_EVENTS.ERROR)
              .build();
          } else {
            firmSplits = firmSplitService.getSplitByDate(runDate, EXCHANGE);
            firmSplits.stream().map(s -> {
              return firmSplitService.saveFirmSplit(s);
            });

            context.getExtendedState().getVariables().put("runDate", runDate);
            context.getExtendedState().getVariables().put("splits", firmSplits);
            message = MessageBuilder
              .withPayload(SPLIT_EVENTS.SUCCESS)
              .build();
          }
        } catch (Exception ex) {
          logger.severe("Error loading splits");
          message = MessageBuilder
            .withPayload(SPLIT_EVENTS.ERROR)
            .build();

        }
        context.getStateMachine().sendEvent(message);

      }
    };

  }

  @Bean
  public Action<SPLIT_STATES, SPLIT_EVENTS> updateQuotes() {
    return new Action<SPLIT_STATES, SPLIT_EVENTS>() {

      @Override
      public void execute(StateContext<SPLIT_STATES, SPLIT_EVENTS> stateContext) {

        Message<SPLIT_EVENTS> message;
        LocalDate runDate = (LocalDate) stateContext.getExtendedState().getVariables().get("runDate");
        List<FirmSplitDTO> firmSplits = (List<FirmSplitDTO>) stateContext.getExtendedState().getVariables().get("splits");
        Long id = (Long)stateContext.getExtendedState().getVariables().get("splitId");

        for (FirmSplitDTO firmSplitDTO : firmSplits) {

          try {
            String code = firmSplitDTO.getCode();

            //on charge les quotes en DB pour cette société
            List<ExchangeFirmQuoteDTO> savedQuotes = firmService.findAllByCodeOrderByDateAsc(code);

            if (savedQuotes.size() == 0) {
              continue;
            }

            //on prend la première date
            ExchangeFirmQuoteDTO first = savedQuotes.get(0);
            //on charge les quotes historiques pour ce code et ce range
            List<FirmQuoteDTO> dbfirmQuotes = firmService.getFirmQuoteByDate(first.getDate(), runDate, code, EXCHANGE);
            //on crée une map par date et prix
            Map<LocalDate, Float> quotesByDate = dbfirmQuotes.stream().collect(Collectors.toMap(FirmQuoteDTO::getDate,FirmQuoteDTO::getAdjustedClose));

            //on update les prix en base
            for (ExchangeFirmQuoteDTO current : savedQuotes) {

              if (quotesByDate.containsKey(current.getDate())) {
                current.setAdjustedClose(quotesByDate.get(current.getDate()));
                firmService.saveEODMarketQuotes(current);
              }
            }
            ConfigDTO current = loadConfigService.findById(id);
            current.setValue(String.format(SplitConfigService.CONFIG_DTO_VALUE_STR, loadConfigService.parseDate(current).format(format1), JobStatus.FINISHED, LocalDateTime.now().format(formatMessage), loadConfigService.retryCount(current) + 1));
            loadConfigService.save(current);
            message = MessageBuilder
              .withPayload(SPLIT_EVENTS.SUCCESS).build();
          } catch (Exception ex) {
            logger.severe(String.format("%s - %s error updating quote", firmSplitDTO.getExchange(), firmSplitDTO.getCode()));
            logger.severe(ex.getMessage());
            continue;
          }
          stateContext.getStateMachine().sendEvent(message);
        }
      }
    };

  }

  @Bean
  public Action<SPLIT_STATES, SPLIT_EVENTS> errorState() {
    return new Action<SPLIT_STATES, SPLIT_EVENTS>() {

      @Override
      public void execute(StateContext<SPLIT_STATES, SPLIT_EVENTS> context) {

        LocalDate runDate = (LocalDate) context.getExtendedState().getVariables().get("runDate");
        Long id = (Long) context.getExtendedState().getVariables().get("erroredId");
        ConfigDTO errored = loadConfigService.findById(id);

        errored.setValue(String.format(SplitConfigService.CONFIG_DTO_VALUE_STR, loadConfigService.parseDate(errored).format(format1), JobStatus.ERROR, LocalDateTime.now().format(formatMessage), loadConfigService.retryCount(errored) + 1));
        loadConfigService.save(errored);

        context.getStateMachine().sendEvent(SPLIT_EVENTS.ERROR_TREATED);

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

