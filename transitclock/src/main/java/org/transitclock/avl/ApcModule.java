package org.transitclock.avl;

import org.transitclock.applications.Core;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.db.structs.ApcRecord;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.monitoring.MonitoringService;

import java.util.Date;
import java.util.List;

/**
 * Integrate with Automated Passenger Count data, parse, archive, and
 * feed into dwell time calculations.
 */
public class ApcModule {
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

  private void archive(List<ApcMatch> matches) {
    // TODO
    // validate/clean
    // write to database
    // store in cache
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
    long referenceTime = apcRecords.get(0).getTime();
    long window = 5000;
    return new TimeRange(referenceTime-window, referenceTime+window);
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
