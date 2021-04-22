package org.transitclock.avl;

import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.db.structs.ApcArrivalRate;
import org.transitclock.db.structs.ApcReport;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.monitoring.MonitoringService;
import org.transitclock.utils.Time;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Integrate with Automated Passenger Count data, parse, archive, and
 * feed into dwell time calculations.
 */
public class ApcModule {

  public static final IntegerConfigValue arrivalDepartureWindowMinutes
          = new IntegerConfigValue("transitclock.apc.arrivalDepartureLoadingWindowInMinutes",
          5,
          "Value in minutes to bookend apc timestamps when searching for appropriate" +
                  " ArrivalDeparture mathces");
  public static final IntegerConfigValue apcStartTimeHour
          = new IntegerConfigValue("transitclock.apc.startTimeHour",
          4,
          "Hour to begin apc analysis");

  private MonitoringService monitoring;
  private static ApcModule singleton;
  private static Object lockObject = new Object();
  private ApcMatcher matcher = new ApcMatcher();

  protected ApcModule() {
    monitoring = MonitoringService.getInstance();
    singleton = this;
  }

  public static ApcModule getInstance() {
    if (singleton == null) {
      synchronized (lockObject) {
        if (singleton == null) {
          singleton = new ApcModule();
        }
      }
    }
    return singleton;
  }

  public void process(List<ApcParsedRecord> apcRecords) {
    TimeRange range = findRangeForRecords(apcRecords);
    if (range == null) {
      //nothing to do
      return;
    }
    List<ArrivalDeparture> arrivals = findArrivalDepartures(range);
    List<ApcMatch> matches = matcher.match(arrivals, apcRecords);
    archive(matches);
  }

  // TODO call this off scheduler or timer....
  public void loadYesterdaysRates() {
    List<ApcReport> matches = findMatches(getRangeForMatches(System.currentTimeMillis()));
    analyze(matches);
  }

  private List<ApcReport> findMatches(TimeRange rangeForMatches) {
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
   synchronized List<ApcArrivalRate> analyze(List<ApcReport> matches) {
    // validate/clean
    List<ApcArrivalRate> rates = ApcAggregator.getInstance().analyze(matches);

    for (ApcArrivalRate rate : rates) {
      Core.getInstance().getDbLogger().add(rate);
    }


    return rates;
  }

  // unit tests will need to override this!
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
  }
}
