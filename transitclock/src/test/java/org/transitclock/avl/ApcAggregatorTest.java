package org.transitclock.avl;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.SingletonSupport;
import org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman.KalmanDataGenerator;
import org.transitclock.db.structs.ApcReport;
import org.transitclock.utils.DateUtils;
import org.transitclock.utils.Time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ApcAggregatorTest extends ApcTest {

  private static final Logger logger =
          LoggerFactory.getLogger(ApcAggregatorTest.class);

  private ApcMatcher matcher = null;
  private KalmanDataGenerator generator;

  @BeforeClass
  public static void setupClass() throws Exception {
    SingletonSupport.createTestCore();
  }

  @Before
  public void setup() throws Exception {
    generator =  new KalmanDataGenerator(System.currentTimeMillis());
  }


  private List<ApcReport> createTestMatches(int daysBack, int incrementSize) {
    List<ApcReport> reports = new ArrayList<>();
    long referenceTime = DateUtils.addDays(new Date(), -1 * daysBack).getTime();
    for (int i = 0; i<incrementSize; i++) {
      // add some noise into reference time
      referenceTime = referenceTime + (5 * Time.MS_PER_MIN);
      List<ApcParsedRecord> apcParsedRecords = generator.getApcParsedRecords(referenceTime, 0, i);
      for (ApcParsedRecord apr : apcParsedRecords) {
        reports.add(apr.toApcReport());
      }
    }
    return reports;
  }

  @Test
  public void analyze() throws Exception {
    ApcCache aggregator = new ApcCache("CDT");
    List<ApcReport> matches = loadMatches();
    aggregator.analyze(matches);

    /*
    // test pruning of these duplicate messages
    apc[vehicleId=2079,time=Wed Apr 21 16:30:13 EDT 2021,id=2179293311,ons=0,offs=3] "timestamp":"2021-04-21T20:30:13.997"
    apc[vehicleId=3992,time=Wed Apr 21 16:30:20 EDT 2021,id=2179293351,ons=0,offs=4] "timestamp":"2021-04-21T20:30:20.09"
    apc[vehicleId=3992,time=Wed Apr 21 16:30:38 EDT 2021,id=2179293435,ons=0,offs=4] (duplicate)...
    ...
     */

    Double legacyRate = aggregator.getBoardingsPerMinuteLegacy("11861", SingletonSupport.toDate("2021-04-21", "20:28:01", "UTC"));
    Double rate = aggregator.getBoardingsPerMinute("63", "11861", SingletonSupport.toDate("2021-04-21", "20:28:01", "UTC"));
    assertNotNull(rate);
    assertEquals("11861 failed", 0, legacyRate.intValue());
    assertEquals("11861 failed", 0, rate.intValue());

    legacyRate = aggregator.getBoardingsPerMinuteLegacy( "17994", SingletonSupport.toDate("2021-04-21", "23:58:01", "UTC"));
    rate = aggregator.getBoardingsPerMinute("17", "17994", SingletonSupport.toDate("2021-04-21", "23:58:01", "UTC"));
    assertNotNull(rate);
    assertEquals("17994 failed", 2.0/*arrivals/*/ / 7/*records*/ / 15/*window*/, legacyRate, 0.001);
    assertEquals("17994 failed", 0.08333, rate, 0.001);

    legacyRate = aggregator.getBoardingsPerMinuteLegacy( "17976", SingletonSupport.toDate("2021-04-21", "16:59:00", "UTC"));
    rate = aggregator.getBoardingsPerMinute("18", "17976", SingletonSupport.toDate("2021-04-21", "16:59:00", "UTC"));
    assertNotNull(rate);
    assertEquals("17976 failed", 5.0/*arrivals/*/ / 4/*records*/ / 15/*window*/, legacyRate, 0.001);
    assertEquals("17976 failed", 0.1333, rate, 0.001);

    legacyRate = aggregator.getBoardingsPerMinuteLegacy( "11861", SingletonSupport.toDate("2021-04-21", "15:51:00", "UTC"));
    rate = aggregator.getBoardingsPerMinute("74", "11861", SingletonSupport.toDate("2021-04-21", "15:51:00", "UTC"));
    assertNotNull(rate);
    assertEquals("11861 failed", 14.0/*arrivals/*/ / 5/*records*/ / 15/*window*/, legacyRate, 0.001);
    assertEquals("11861 failed", 0.38333, rate, 0.001);

    legacyRate = aggregator.getBoardingsPerMinuteLegacy( "17990", SingletonSupport.toDate("2021-04-21", "21:45:00", "UTC"));
    rate = aggregator.getBoardingsPerMinute("10", "17990", SingletonSupport.toDate("2021-04-21", "21:45:00", "UTC"));
    assertNotNull(rate);
    assertEquals("17990 failed", 5.0/*arrivals/*/ / 8/*records*/ / 15/*window*/, legacyRate, 0.001);
    assertEquals("17990 failed", 0.14666, rate, 0.001);

  }


}