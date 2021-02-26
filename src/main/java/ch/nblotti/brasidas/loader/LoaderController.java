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
  private LoaderService loaderService;





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

    loaderService.startLoad(startYear, startMonth, startDay, endYear, endMonth, endDay, Boolean.TRUE);
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

    loaderService.startLoad(startYear, startMonth, startDay, endYear, endMonth, endDay, Boolean.FALSE);
  }


}
