package org.transitclock.avl;

import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.db.structs.ApcArrivalRate;
import org.transitclock.db.structs.ApcRecord;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.monitoring.MonitoringService;
import org.transitclock.utils.Time;

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

  public void process(List<ApcRecord> apcRecords) {
    TimeRange range = findRangeForRecords(apcRecords);
    if (range == null) {
      //nothing to do
      return;
    }
    List<ArrivalDeparture> arrivals = findArrivalDepartures(range);
    List<ApcMatch> matches = matcher.match(arrivals, apcRecords);
    archive(matches);
  }

  private long getWindowMillis() {
    return arrivalDepartureWindowMinutes.getValue() * Time.MS_PER_MIN;
  }


  private synchronized void archive(List<ApcMatch> matches) {
    // write to database
    for (ApcMatch match : matches) {
      Core.getInstance().getDbLogger().add(match.getApc());
    }

    // validate/clean
    List<ApcArrivalRate> rates = analyze(matches);

    for (ApcArrivalRate rate : rates) {
      Core.getInstance().getDbLogger().add(rate);
    }

  }

  /**
   * create rates from the matches.
   * @param matches
   * @return
   */
  List<ApcArrivalRate> analyze(List<ApcMatch> matches) {
    return ApcAggregator.getInstance().analyze(matches);
  }

  // unit tests will need to override this!
  protected List<ArrivalDeparture> findArrivalDepartures(TimeRange range) {
    String agencyId = AgencyConfig.getAgencyId();
    return ArrivalDeparture.getArrivalsDeparturesFromDb(agencyId,
            range.getBeginTime(),
            range.getEndTime());
  }

  private TimeRange findRangeForRecords(List<ApcRecord> apcRecords) {
    if (apcRecords == null || apcRecords.isEmpty()) return null;
    ApcRecord firstRecord = apcRecords.get(0);
    ApcRecord lastRecord = apcRecords.get(apcRecords.size());
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
