package org.transitclock.avl;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.TestSupport;
import org.transitclock.db.structs.ApcReport;
import org.transitclock.db.structs.ApcRecordSupport;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.ArrivalDepartureSupport;
import org.transitclock.utils.IntervalTimer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ApcAggregatorTest {

  private static final Logger logger =
          LoggerFactory.getLogger(ApcAggregatorTest.class);

  private ArrivalDepartureSupport arrivalDepartureSupport = new ArrivalDepartureSupport();
  private ApcRecordSupport apcRecordSupport = new ApcRecordSupport();
  private ApcMatcher matcher = null;

  @BeforeClass
  public static void setup() throws Exception {
    TestSupport.createTestCore();
  }

  @Test
  public void analyze() throws Exception {
    ApcAggregator aggregator = new ApcAggregator("CDT");
    List<ApcReport> matches = loadMatches();
    aggregator.analyze(matches);

    /*
    // test pruning of these duplicate messages
    apc[vehicleId=2079,time=Wed Apr 21 16:30:13 EDT 2021,id=2179293311,ons=0,offs=3] "timestamp":"2021-04-21T20:30:13.997"
    apc[vehicleId=3992,time=Wed Apr 21 16:30:20 EDT 2021,id=2179293351,ons=0,offs=4] "timestamp":"2021-04-21T20:30:20.09"
    apc[vehicleId=3992,time=Wed Apr 21 16:30:38 EDT 2021,id=2179293435,ons=0,offs=4] (duplicate)...
    ...
     */
    Integer count = aggregator.getCount("11861", TestSupport.toDate("2021-04-21", "20:28:01", "UTC"));
    assertNotNull(count);
    assertEquals(0, count.intValue());

    count = aggregator.getCount("17994", TestSupport.toDate("2021-04-21", "23:58:01", "UTC"));
    assertNotNull(count);
    assertEquals(2, count.intValue());

    count = aggregator.getCount("17976", TestSupport.toDate("2021-04-21", "16:59:00", "UTC"));
    assertNotNull(count);
    assertEquals(1, count.intValue());

    count = aggregator.getCount("11861", TestSupport.toDate("2021-04-21", "15:51:00", "UTC"));
    assertNotNull(count);
    assertEquals(11, count.intValue());

    count = aggregator.getCount("17990", TestSupport.toDate("2021-04-21", "21:45:00", "UTC"));
    assertNotNull(count);
    assertEquals(4, count.intValue());

  }

  private List<ApcReport> loadMatches() throws Exception {
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
    assertEquals(54343, matches.size());
    return matches;
  }

  private List<ApcReport> merge(List<ArrivalDeparture> arrivalDepartureList, List<ApcParsedRecord> records) {
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
        + " (" + (matched/total) + ")");
    return reports;
  }
}