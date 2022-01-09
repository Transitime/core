package org.transitclock.avl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.ApcRecordSupport;
import org.transitclock.db.structs.ApcReport;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.ArrivalDepartureSupport;
import org.transitclock.utils.IntervalTimer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ApcTest {

  private static final Logger logger =
          LoggerFactory.getLogger(ApcTest.class);

  private ArrivalDepartureSupport arrivalDepartureSupport = new ArrivalDepartureSupport();
  private ApcRecordSupport apcRecordSupport = new ApcRecordSupport();

  protected List<ApcReport> loadMatches() throws Exception {
    //2021-04-21T13:27:28.94 to 2021-04-22T10:33:31.017
    List<ArrivalDeparture> arrivalDepartureList
            = arrivalDepartureSupport.loadArrivalDepartureList("arrivalDepartures4.csv.zip");
    assertNotNull(arrivalDepartureList);
    assertEquals(590510, arrivalDepartureList.size());
    List<ApcParsedRecord> records = apcRecordSupport.loadApcRecords("apcMessages4.json.zip");
    assertNotNull(records);
    assertEquals(56968, records.size());
    IntervalTimer mergeTimer = new IntervalTimer();
    List<ApcReport> matches = merge(arrivalDepartureList, records);
    logger.info("merged {} apc into {} ad records in {} ms",
            records.size(), arrivalDepartureList.size(),
            mergeTimer.elapsedMsec());
    assertNotNull(matches);
    assertEquals(54454, matches.size());
    return matches;
  }

  protected List<ApcReport> merge(List<ArrivalDeparture> arrivalDepartureList, List<ApcParsedRecord> records) {
    List<ApcReport> reports = new ArrayList<>();
    int total = 0;
    int matched = 0;
    ApcMatcher matcher = new ApcMatcher(arrivalDepartureList);
    List<ApcMatch> matches = matcher.match(records);
    for (ApcMatch match : matches) {
      ApcReport report = match.getApc().toApcReport();
      reports.add(report);
      if (report.getArrivalDeparture() != null)
        matched++;
      total++;
    }
    System.out.println("matched " + matched + " out of " + total
            + " (" + (matched / total) + ")");
    return reports;
  }
}
