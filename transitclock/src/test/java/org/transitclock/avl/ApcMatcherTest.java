package org.transitclock.avl;

import org.junit.Before;
import org.junit.Test;
import org.transitclock.TestSupport;
import org.transitclock.db.structs.ApcRecordSupport;
import org.transitclock.db.structs.ArrivalDepartureSupport;
import org.transitclock.db.structs.ArrivalDeparture;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ApcMatcherTest {

  private ArrivalDepartureSupport arrivalDepartureSupport = new ArrivalDepartureSupport();
  private ApcRecordSupport apcRecordSupport = new ApcRecordSupport();
  private ApcMatcher matcher;

  @Before
  public void setUp() throws Exception {

    TestSupport.createTestCore();


  }

  @Test
  public void match() throws Exception {
    List<ArrivalDeparture> arrivalDepartureList
            = arrivalDepartureSupport.loadArrivalDepartureList("arrivalDepartures3.csv");
    List<ApcParsedRecord> records = apcRecordSupport.loadApcRecords("apcMessages3.json");
    ApcMatcher matcher = new ApcMatcher(arrivalDepartureList);
    List<ApcMatch> matches = matcher.match(records);
    assertNotNull(matches);
    assertEquals(10, matches.size());

    for (ApcMatch match : matches) {
      ApcParsedRecord apc = match.getApc();
      assertNotNull(apc);
      ArrivalDeparture ad = match.getArrivalDeparture();
      if (ad == null) {
        System.out.println("no match for ad " + apc.getVehicleId() + " at " + new Date(apc.getArrivalEpoch()));
        // we successfully matched all example data to historical ArrivalDeparture data!
        assertNotNull(ad);
      }
      assertEquals(apc.getVehicleId(), ad.getVehicleId());
    }

  }

}