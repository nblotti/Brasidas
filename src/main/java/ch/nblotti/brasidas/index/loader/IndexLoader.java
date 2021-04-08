package ch.nblotti.brasidas.index.loader;

import ch.nblotti.brasidas.configuration.ConfigDTO;
import ch.nblotti.brasidas.configuration.JobStatus;
import ch.nblotti.brasidas.index.composition.Index;
import ch.nblotti.brasidas.index.composition.IndexCompositionDTO;
import ch.nblotti.brasidas.index.composition.IndexCompositionService;
import ch.nblotti.brasidas.index.quote.IndexQuoteDTO;
import ch.nblotti.brasidas.index.quote.IndexQuoteService;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.Message;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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
  private IndexQuoteService indexQuoteService;

  @Autowired
  private LoadIndexConfigService loadIndexConfigService;

  @Autowired
  private IndexCompositionService indexCompositionService;


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
      .state(INDEX_LOADER_STATES.ERROR_STATE, errorIndexAction())
      .state(INDEX_LOADER_STATES.LOAD_INDEX, loadIndexes())
      .state(INDEX_LOADER_STATES.CLEANUP_INDEX_COMPOSITION, cleanupCurrentIndexHistory())
      .state(INDEX_LOADER_STATES.LOAD_INDEX_COMPOSITION, loadCurrentIndexHistory())
      .end(INDEX_LOADER_STATES.DONE)
      .end(INDEX_LOADER_STATES.ERROR);


  }


  @Override
  public void configure(StateMachineTransitionConfigurer<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> transitions) throws Exception {
    transitions.withExternal()
      .source(INDEX_LOADER_STATES.WAITING_EVENT).target(INDEX_LOADER_STATES.READY).event(INDEX_LOADER_EVENTS.EVENT_RECEIVED)
      .and()
      .withExternal()
      .source(INDEX_LOADER_STATES.READY).target(INDEX_LOADER_STATES.LOAD_INDEX)
      .and()
      .withExternal()
      .source(INDEX_LOADER_STATES.LOAD_INDEX).target(INDEX_LOADER_STATES.CLEANUP_INDEX_COMPOSITION).event(INDEX_LOADER_EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(INDEX_LOADER_STATES.LOAD_INDEX).target(INDEX_LOADER_STATES.ERROR_STATE).event(INDEX_LOADER_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(INDEX_LOADER_STATES.CLEANUP_INDEX_COMPOSITION).target(INDEX_LOADER_STATES.LOAD_INDEX_COMPOSITION).event(INDEX_LOADER_EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(INDEX_LOADER_STATES.CLEANUP_INDEX_COMPOSITION).target(INDEX_LOADER_STATES.ERROR_STATE).event(INDEX_LOADER_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(INDEX_LOADER_STATES.LOAD_INDEX_COMPOSITION).target(INDEX_LOADER_STATES.DONE).event(INDEX_LOADER_EVENTS.SUCCESS)
      .and()
      .withExternal()
      .source(INDEX_LOADER_STATES.LOAD_INDEX_COMPOSITION).target(INDEX_LOADER_STATES.ERROR_STATE).event(INDEX_LOADER_EVENTS.ERROR)
      .and()
      .withExternal()
      .source(INDEX_LOADER_STATES.DONE).target(INDEX_LOADER_STATES.WAITING_EVENT)
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


        Message<INDEX_LOADER_EVENTS> message;
        LocalDateTime runTimeStart = LocalDateTime.now();

        //on charge la liste des index
        List<Index> indexList = getIndexList();
        Long id = (Long) context.getMessageHeader("indexJobId");
        ConfigDTO current = loadIndexConfigService.findById(id);
        String runDate = loadIndexConfigService.parseDate(current).format(format1);

        context.getExtendedState().getVariables().put("indexJobId", id);
        context.getExtendedState().getVariables().put("runTime", runTimeStart);
        context.getExtendedState().getVariables().put("indexList", indexList);


        log.info(String.format("Index - %s - Starting load process", runDate));


        current.setValue(String.format(LoadIndexConfigService.CONFIG_DTO_VALUE_STR, runDate, JobStatus.RUNNING, LocalDateTime.now().format(formatMessage), loadIndexConfigService.retryCount(current) + 1));
        loadIndexConfigService.update(current);

        context.getStateMachine().sendEvent(INDEX_LOADER_EVENTS.SUCCESS);

      }

    };
  }


  public List<Index> getIndexList() {

    String namePath = "$.name";
    String compositionPath = "$.composition";


    DocumentContext indexesJson = JsonPath.parse(indexList);

    JSONArray indexJSONArray = indexesJson.read("$..indexes[*]");

    List<Index> indexes = indexJSONArray.stream().map(s -> {
      DocumentContext content = JsonPath.parse(s);
      String name = content.read(namePath);
      String composition = content.read(compositionPath);
      return new Index(name, Boolean.parseBoolean(composition));
    }).collect(Collectors.toList());

    return indexes;
  }


  @Bean
  public Action<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> loadIndexes() {
    return new Action<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS>() {

      @Override
      public void execute(StateContext<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> stateContext) {

        Message<INDEX_LOADER_EVENTS> message;

        try {
          Long id = (Long) stateContext.getMessageHeader("indexJobId");
          ConfigDTO current = loadIndexConfigService.findById(id);
          LocalDate runDate = loadIndexConfigService.parseDate(current);
          List<Index> indexList = (List<Index>) stateContext.getExtendedState().getVariables().get("indexList");
          for (Index currentIndex : indexList) {

            IndexQuoteDTO indexQuoteDTO = indexQuoteService.getIndexDataByDate(runDate, currentIndex.getName());
            if (indexQuoteDTO != null)
              indexQuoteService.saveEODIndexQuotes(indexQuoteDTO);
            else
              log.warn(String.format("%s - %s - no quote", runDate.format(format1), currentIndex.getName()));
          }
          stateContext.getStateMachine().sendEvent(INDEX_LOADER_EVENTS.SUCCESS);
        } catch (Exception ex) {
          stateContext.getStateMachine().sendEvent(INDEX_LOADER_EVENTS.ERROR);
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


        Long id = (Long) context.getExtendedState().getVariables().get("indexJobId");

        ConfigDTO errored = loadIndexConfigService.findById(id);

        errored.setValue(String.format(LoadIndexConfigService.CONFIG_DTO_VALUE_STR, loadIndexConfigService.parseDate(errored).format(format1), JobStatus.ERROR, LocalDateTime.now().format(formatMessage), loadIndexConfigService.retryCount(errored)));
        loadIndexConfigService.update(errored);

        context.getStateMachine().sendEvent(INDEX_LOADER_EVENTS.ERROR_TREATED);
      }
    };
  }

  @Bean
  public Action<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> cleanupCurrentIndexHistory() {
    return new Action<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS>() {
      @Override
      public void execute(StateContext<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> context) {

        try {
          indexCompositionService.deleteAll();
        } catch (Exception ex) {
          log.error(ex.getMessage());
          context.getStateMachine().sendEvent(INDEX_LOADER_EVENTS.ERROR);
          return;
        }
        context.getStateMachine().sendEvent(INDEX_LOADER_EVENTS.SUCCESS);

      }
    };
  }

  @Bean
  public Action<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> loadCurrentIndexHistory() {
    return new Action<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS>() {
      @Override
      public void execute(StateContext<INDEX_LOADER_STATES, INDEX_LOADER_EVENTS> stateContext) {


        Long id = (Long) stateContext.getExtendedState().getVariables().get("indexJobId");
        ConfigDTO current = loadIndexConfigService.findById(id);
        LocalDate runDate = loadIndexConfigService.parseDate(current);
        current.setValue(String.format(LoadIndexConfigService.CONFIG_DTO_VALUE_STR, runDate, JobStatus.FINISHED, LocalDateTime.now().format(formatMessage), loadIndexConfigService.retryCount(current)));
        loadIndexConfigService.update(current);

        try {
          List<Index> indexList = (List<Index>) stateContext.getExtendedState().getVariables().get("indexList");

          for (Index currentIndex : indexList) {

            if (!currentIndex.isLoadComposition())
              continue;

            List<IndexCompositionDTO> indexCompositionDTOS = indexCompositionService.getIndexComposition(currentIndex.getName());

            indexCompositionService.saveAll(indexCompositionDTOS);
          }

          stateContext.getStateMachine().sendEvent(INDEX_LOADER_EVENTS.SUCCESS);
        } catch (Exception ex) {
          stateContext.getStateMachine().sendEvent(INDEX_LOADER_EVENTS.ERROR);
          log.error(ex.toString());
        }
        LocalDateTime runTimeEnd = LocalDateTime.now();
        LocalDateTime runTimeStart = (LocalDateTime) stateContext.getExtendedState().getVariables().get("runTime");
        Duration diff = Duration.between(runTimeStart, runTimeEnd);

        log.info(String.format("%s - %s - End load process. Treated in %s minutes", runDate, stateContext, diff.toMinutes()));


        stateContext.getStateMachine().sendEvent(INDEX_LOADER_EVENTS.SUCCESS);

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

