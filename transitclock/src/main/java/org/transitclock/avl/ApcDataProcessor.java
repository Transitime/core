package org.transitclock.avl;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.db.structs.Agency;
import org.transitclock.db.structs.ApcReport;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.monitoring.MonitoringService;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.Time;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Integrate with Automated Passenger Count data, parse, archive, and
 * feed into dwell time calculations.
 */
public class ApcDataProcessor {

  private static final Logger logger = LoggerFactory.getLogger(ApcDataProcessor.class);

  public static final IntegerConfigValue arrivalDepartureWindowMinutes
          = new IntegerConfigValue("transitclock.apc.arrivalDepartureLoadingWindowInMinutes",
          5,
          "Value in minutes to bookend apc timestamps when searching for appropriate" +
                  " ArrivalDeparture mathces");
  public static final IntegerConfigValue apcStartTimeHour
          = new IntegerConfigValue("transitclock.apc.startTimeHour",
          4,
          "Hour to begin apc analysis");
  public static final StringConfigValue cronPattern
          = new StringConfigValue("transitclock.apc.cron",
          "0 0 4 * * ?",
          "extended cron pattern (includes seconds) to run apc update on");

  private MonitoringService monitoring;
  private static ApcDataProcessor singleton;
  private static Object lockObject = new Object();
  private ApcAggregator apcAggregator = null;
  private String tz = null;
  private boolean enabled = false;

  protected ApcDataProcessor(String tz) {
    monitoring = MonitoringService.getInstance();
    singleton = this;
    this.tz = tz;
    apcAggregator = new ApcAggregator(tz);
  }

  public static ApcDataProcessor getInstance() {
    if (singleton == null) {
      synchronized (lockObject) {
        if (singleton == null) {
          String agencyId = AgencyConfig.getAgencyId();
          TimeZone timeZoneFromDb = Agency.getTimeZoneFromDb(agencyId);
          if (timeZoneFromDb == null) {
            timeZoneFromDb = TimeZone.getDefault();
          }
          singleton = new ApcDataProcessor(timeZoneFromDb.toZoneId().getId());
          singleton.init();
        }
      }
    }
    return singleton;
  }

  private void init() {
    JobDetail job = newJob(ApcDataProcessorJob.class)
            .withIdentity("apcJob", "transitclock")
            .build();

    // Trigger the job to run now, and then repeat every 40 seconds
    Trigger trigger = newTrigger()
            .withIdentity("apcTrigger", "transitclock")
            .startNow()
            .withSchedule(cronSchedule(cronPattern.getValue()))
            .build();

    // Tell quartz to schedule the job using our trigger
    try {
      Core.getInstance().getScheduler().scheduleJob(job, trigger);
    } catch (SchedulerException e) {
      logger.error("Scheduling ApcDataProcessor Job failed");
    }
  }

  public void enable() {
    enabled = true;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void process(List<ApcParsedRecord> apcRecords) {
    TimeRange range = findRangeForRecords(apcRecords);
    if (range == null) {
      //nothing to do
      return;
    }
    List<ArrivalDeparture> arrivals = findArrivalDepartures(range);
    List<ApcMatch> matches = internalProcess(apcRecords, arrivals);

    archive(matches);
  }

  public void populateFromDb(List<ArrivalDeparture> arrivalDepartures) {
    if (arrivalDepartures == null || arrivalDepartures.isEmpty()) {
      logger.error("populateFromDb called with nothing to do, exiting.");
      return;
    }
    TimeRange apcTimeRange = findRangeForArrivalRecords(arrivalDepartures);
    List<ApcParsedRecord> apcRecords = findApcRecords(apcTimeRange);
    internalProcess(apcRecords, arrivalDepartures);
  }

  private List<ApcMatch> internalProcess(List<ApcParsedRecord> apcRecords, List<ArrivalDeparture> arrivalDepartures) {
    ApcMatcher matcher = new ApcMatcher(arrivalDepartures);
    List<ApcMatch> matches = matcher.match(apcRecords);

    List<ApcReport> reports = new ArrayList<>();
    for (ApcMatch match : matches) {
      reports.add(match.getApc().toApcReport());
    }

    analyze(reports);
    return matches;
  }

  private List<ApcParsedRecord> findApcRecords(TimeRange apcTimeRange) {
    List<ApcReport> reports = findMatches(apcTimeRange);
    List<ApcParsedRecord> records = new ArrayList<>();
    for (ApcReport report : reports) {
      records.add(new ApcParsedRecord(report));
    }
    return records;
  }

  public int loadYesterdaysRates() {
    IntervalTimer timer = new IntervalTimer();
    try {
      TimeRange apcRange = getRangeForMatches(System.currentTimeMillis());
      List<ArrivalDeparture> arrivalDepartures = findArrivalDepartures(apcRange);
      if (arrivalDepartures != null) {
        populateFromDb(arrivalDepartures);
        return arrivalDepartures.size();
      } else {
        logger.error("no arrival/departures found for range {}", apcRange);
      }
    } finally {
      logger.error("loadYesterdayRates complete in {} msec", timer.elapsedMsecStr());
    }
    return -1;
  }

  private List<ApcReport> findMatches(TimeRange rangeForMatches) {
    if (rangeForMatches == null) {
      throw new NullPointerException("rangeForMatches cannot be null");
    }
    String agencyId = AgencyConfig.getAgencyId();
    return ApcReport.getApcReportsFromDb(agencyId,
            rangeForMatches.getBeginTime(),
            rangeForMatches.getEndTime());
  }

  private TimeRange getRangeForMatches(long referenceTime) {
    // time range is a 24 hour period ending at 4am of reference time day
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date(referenceTime));
    calendar.set(Calendar.HOUR, apcStartTimeHour.getValue());
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND,0);
    calendar.set(Calendar.MILLISECOND, 0);
    long endTime = calendar.getTimeInMillis();
    calendar.roll(Calendar.DAY_OF_YEAR, -1);
    long startTime = calendar.getTimeInMillis();
    return new TimeRange(startTime, endTime);
  }

  private long getWindowMillis() {
    return arrivalDepartureWindowMinutes.getValue() * Time.MS_PER_MIN;
  }


  private synchronized void archive(List<ApcMatch> matches) {
    // write to database
    for (ApcMatch match : matches) {
      Core.getInstance().getDbLogger().add(match.getApc());
    }

  }

  /**
   * create rates from the matches.
   * @param matches
   * @return
   */
   public synchronized void analyze(List<ApcReport> matches) {
    // validate/clean
    apcAggregator.analyze(matches);
  }

  public synchronized int cacheSize() {
     return apcAggregator.cacheSize();
  }

  protected List<ArrivalDeparture> findArrivalDepartures(TimeRange range) {
    String agencyId = AgencyConfig.getAgencyId();
    return ArrivalDeparture.getArrivalsDeparturesFromDb(agencyId,
            range.getBeginTime(),
            range.getEndTime());
  }

  private TimeRange findRangeForRecords(List<ApcParsedRecord> apcRecords) {
    if (apcRecords == null || apcRecords.isEmpty()) return null;
    ApcParsedRecord firstRecord = apcRecords.get(0);
    ApcParsedRecord lastRecord = apcRecords.get(apcRecords.size()-1);
    long window = getWindowMillis();
    return new TimeRange(firstRecord.getTime()-window, lastRecord.getTime()+window);
  }

  private TimeRange findRangeForArrivalRecords(List<ArrivalDeparture> arrivalDepartures) {
     if (arrivalDepartures == null || arrivalDepartures.isEmpty()) return  null;
     ArrivalDeparture firstRecord = arrivalDepartures.get(0);
     ArrivalDeparture lastRecord = arrivalDepartures.get(arrivalDepartures.size()-1);
    long window = getWindowMillis();
    return new TimeRange(firstRecord.getTime()-window, lastRecord.getTime()+window);
  }


  public Integer getBoardingsPerMinute(String stopId, Date arrivalTime) {
    return apcAggregator.getBoardingsPerMinute(stopId, arrivalTime);
  }

  public static class TimeRange {
    private long start;
    private long finish;
    public TimeRange(long start, long finish) {
      this.start = start;
      this.finish = finish;
    }

    public Date getBeginTime() {
      return new Date(start);
    }

    public Date getEndTime() {
      return new Date(finish);
    }

    @Override
    public String toString() {
      return ""
              + getBeginTime()
              + " -> "
              + getEndTime();
    }
  }

  public static class ApcDataProcessorJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
      ApcDataProcessor.getInstance().loadYesterdaysRates();
    }
  }
}
