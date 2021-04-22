package org.transitclock.avl;

import org.junit.Test;
import org.junit.Ignore;
import org.transitclock.db.structs.ApcArrivalRate;
import org.transitclock.db.structs.ApcReport;
import org.transitclock.db.structs.ApcRecordSupport;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.ArrivalDepartureSupport;

import java.util.List;

public class ApcAggregatorTest {

  private ArrivalDepartureSupport arrivalDepartureSupport = new ArrivalDepartureSupport();
  private ApcRecordSupport apcRecordSupport = new ApcRecordSupport();
  private ApcMatcher matcher = new ApcMatcher();

  @Test
  @Ignore
  public void analyze() throws Exception {
    ApcAggregator aggregator = ApcAggregator.getInstance();
    List<ApcReport> matches = loadMatches();
    List<ApcArrivalRate> rates = aggregator.analyze(matches);
    // TODO -- in progress
//    assertNotNull(rates);
//    assertEquals(10, rates.size());
  }

  private List<ApcReport> loadMatches() throws Exception {
    List<ArrivalDeparture> arrivalDepartureList
            = arrivalDepartureSupport.loadArrivalDepartureList("arrivalDepartures4.csv");
    List<ApcParsedRecord> records = apcRecordSupport.loadApcRecords("apcMessages4.json");
    List<ApcReport> matches = merge(arrivalDepartureList, records);

    return matches;
  }

  private List<ApcReport> merge(List<ArrivalDeparture> arrivalDepartureList, List<ApcParsedRecord> records) {
    return null;
  }
}