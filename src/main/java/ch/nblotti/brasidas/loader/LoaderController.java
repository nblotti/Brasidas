package ch.nblotti.brasidas.loader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;


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

  @Autowired
  private SplitService splitService;



  @PostMapping(value = "/split")
  public void split(@RequestParam(name = "startyear", required = true) Integer startYear,
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

    splitService.startSplit(startYear, startMonth, startDay, endYear, endMonth, endDay);
  }




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
