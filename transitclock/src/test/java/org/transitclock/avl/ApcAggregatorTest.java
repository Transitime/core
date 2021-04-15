package org.transitclock.avl;

import org.junit.Test;
import org.transitclock.db.structs.ApcArrivalRate;
import org.transitclock.db.structs.ApcRecord;
import org.transitclock.db.structs.ApcRecordSupport;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.ArrivalDepartureSupport;

import java.util.List;

import static org.junit.Assert.*;

public class ApcAggregatorTest {

  private ArrivalDepartureSupport arrivalDepartureSupport = new ArrivalDepartureSupport();
  private ApcRecordSupport apcRecordSupport = new ApcRecordSupport();
  private ApcMatcher matcher = new ApcMatcher();

  @Test
  public void analyze() throws Exception {
    ApcAggregator aggregator = ApcAggregator.getInstance();
    List<ApcMatch> matches = loadMatches();
    List<ApcArrivalRate> rates = aggregator.analyze(matches);
    // TODO -- in progress
//    assertNotNull(rates);
//    assertEquals(10, rates.size());
  }

  private List<ApcMatch> loadMatches() throws Exception {
    List<ArrivalDeparture> arrivalDepartureList
            = arrivalDepartureSupport.loadArrivalDepartureList("arrivalDepartures3.csv");
    List<ApcRecord> records = apcRecordSupport.loadApcRecords("apcMessages3.json");
    List<ApcMatch> matches = matcher.match(arrivalDepartureList, records);

    return matches;
  }
}