package ch.nblotti.brasidas.loader;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


@RestController
@RequestMapping("/loader")
public class LoaderController {


  private static final Logger logger = Logger.getLogger("LoaderController");

  private static final int WORKER_THREAD_POOL = 1;

  @Autowired
  private DateTimeFormatter format1;

  @Autowired
  private DateTimeFormatter formatMessage;


  @Autowired
  private BeanFactory beanFactory;


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

    startLoad(startYear, startMonth, startDay, endYear, endMonth, endDay, Boolean.TRUE);
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

    startLoad(startYear, startMonth, startDay, endYear, endMonth, endDay, Boolean.FALSE);
  }

  private void startLoad(Integer startYear, Integer startMonth, Integer startDay, Integer endYear, Integer endMonth, Integer endDay, Boolean runPartial) {


    LoaderThread loaderThread = new LoaderThread(beanFactory, startYear, startMonth, startDay, endYear, endMonth, endDay, runPartial);

    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(loaderThread);
    executor.shutdown();
    try {
      if (!executor.awaitTermination(3, TimeUnit.HOURS)) {
        loaderThread.stopNow();
        executor.shutdownNow();
        logger.severe("Ending daily eod security loading- seems blocked");

      }
    } catch (InterruptedException e) {
      logger.severe("Ending daily eod security loading");
      logger.severe(e.getMessage());
    } finally {
      logger.info("Ending daily eod security loading");
    }

  }

  @Scheduled(cron = "${loader.cron.expression}")
  public void scheduleFixedDelayTask() {

    LocalDate runDate = LocalDate.now().minusDays(1);
    startLoad(runDate.getYear(), runDate.getMonthValue(), runDate.getDayOfMonth(), runDate.getYear(), runDate.getMonthValue(), runDate.getDayOfMonth(), Boolean.FALSE);
  }

  class LoaderThread implements Runnable {


    private StateMachine<LOADER_STATES, LOADER_EVENTS> sp500LoaderStateMachine;
    private final Logger logger = LoaderController.this.logger;

    private final BeanFactory beanFactory;
    private final Integer startYear;
    private final Integer startMonth;
    private final Integer startDay;
    private final Integer endYear;
    private final Integer endMonth;
    private final Integer endDay;
    private final Boolean runPartial;

    public LoaderThread(BeanFactory beanFactory, Integer startYear, Integer startMonth, Integer startDay, Integer endYear, Integer endMonth, Integer endDay, Boolean runPartial) {
      this.beanFactory = beanFactory;
      this.startYear = startYear;
      this.startMonth = startMonth;
      this.startDay = startDay;
      this.endYear = endYear;
      this.endMonth = endMonth;
      this.endDay = endDay;
      this.runPartial = runPartial;
    }

    public LoaderThread(BeanFactory beanFactory, Integer startYear, Integer startMonth, Integer startDay, Integer endYear, Integer endMonth, Integer endDay) {
      this(beanFactory, startYear, startMonth, startDay, endYear, endMonth, endDay, Boolean.TRUE);
    }

    private void startLoadingProcess(LocalDate localDate, Message<LOADER_EVENTS> message) {

      long start = System.nanoTime();
      sp500LoaderStateMachine = (StateMachine<LOADER_STATES, LOADER_EVENTS>) beanFactory.getBean("stateMachine");
      sp500LoaderStateMachine.start();
      boolean result = sp500LoaderStateMachine.sendEvent(message);
      while (sp500LoaderStateMachine.getState().getId() != LOADER_STATES.DONE  && sp500LoaderStateMachine.getState().getId() != LOADER_STATES.ERROR)  {
        try {
          long temp = System.nanoTime();
          long secs = TimeUnit.SECONDS.convert(temp - start, TimeUnit.NANOSECONDS);
          logger.log(Level.INFO, String.format("%s - still running - temps : %s secondes", localDate, secs));

          Thread.sleep(15000);

        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

      sp500LoaderStateMachine.stop();


      long end = System.nanoTime();
      long secs = TimeUnit.SECONDS.convert(end - start, TimeUnit.NANOSECONDS);

      logger.log(Level.INFO, String.format("%s - %s - temps : %s secondes", localDate, result, secs));

    }

    public void stopNow() {
      this.sp500LoaderStateMachine.sendEvent(LOADER_EVENTS.ERROR);
    }

    @Override
    public void run() {


      Message<LOADER_EVENTS> message;

      int localStartDay;
      if (startDay == null || startDay <= 0)
        localStartDay = 1;
      else
        localStartDay = startDay;

      int localEndDay = LocalDate.of(endYear, endMonth, 1).with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
      if (endDay <= localEndDay)
        localEndDay = endDay;


      for (int year = startYear; year <= endYear; year++) {

        int loopstartMonth = 1;
        int loopLastMonth = 12;

        if (year == endYear)
          loopLastMonth = endMonth;

        if (year == startYear)
          loopstartMonth = startMonth;

        for (int month = loopstartMonth; month <= loopLastMonth; month++) {
          LocalDate localDate = LocalDate.of(year, month, 1);
          localDate = localDate.withDayOfMonth(localDate.lengthOfMonth());

          int loopLastDay = 1;
          int loopStartDay = 1;

          loopLastDay = localDate.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();

          if (year >= endYear && month >= endMonth) {
            loopLastDay = localEndDay;
          }

          if (year == startYear && month == startMonth) {
            loopStartDay = localStartDay;
          }
          for (int day = loopStartDay; day <= loopLastDay; day++) {
            LocalDate runDate = localDate.withDayOfMonth(day);

            if (runDate.isAfter(LocalDate.now().minusDays(1)))
              return;

            message = MessageBuilder
              .withPayload(LOADER_EVENTS.EVENT_RECEIVED)
              .setHeader("runDate", runDate)
              .setHeader("runPartial", runPartial)
              .build();

            logger.log(Level.INFO, String.format("%s-%s-%s", year, month, day));
            startLoadingProcess(runDate, message);
          }


        }
      }
    }


  }


}
