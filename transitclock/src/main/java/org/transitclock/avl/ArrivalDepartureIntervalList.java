package org.transitclock.avl;

import org.transitclock.db.structs.ArrivalDeparture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * List of ArrivalDepartureIntervals an match operations against them.
 */
public class ArrivalDepartureIntervalList {

  private ArrayList<ArrivalDepartureInterval> intervals;
  public ArrivalDepartureIntervalList(List<ArrivalDeparture> arrivalDepartures, long window, boolean isArrival) {
    intervals = new ArrayList<>();
    for (ArrivalDeparture ad : arrivalDepartures) {
      intervals.add(new ArrivalDepartureInterval(ad, window, isArrival));
    }
    // TODO make precondition that list is sorted
    Collections.sort(intervals);
  }

  public List<ApcMatch> match(List<ApcParsedRecord> apcRecords) {
    List<ApcMatch> matches = new ArrayList<>();
    for (ApcParsedRecord apc : apcRecords) {
      ApcMatch match = new ApcMatch(apc, match(apc));
      matches.add(match);
    }
    return matches;
  }

  private List<ArrivalDeparture> match(ApcParsedRecord apc) {
    List<ArrivalDeparture> matches = new ArrayList<>();

    int n = intervals.size();
    // currently a trivial search
    // TODO eventually will be overlapping range join
    for( int i = 1; i < n; i++) {
      ArrivalDepartureInterval previousInterval = intervals.get(i - 1);
      if (previousInterval.getArrivalDeparture() == null) {
        System.out.println("no A/D for index " + i);
        continue;
      }
      if (previousInterval.getArrivalDeparture().getVehicleId() == null) {
        System.out.println("no vehicleId for A/D " + i);
        continue;
      }
      if (previousInterval.getArrivalDeparture().isArrival() == previousInterval.getArrival()
              && previousInterval.getArrivalDeparture().getVehicleId().equals(apc.getVehicleId())
              && apc.getTime() > previousInterval.getStart()
              && previousInterval.getEnd() > apc.getTime()) {
        matches.add(previousInterval.getArrivalDeparture());
      }
    }

    if (matches.isEmpty())
      return null;
    return matches;
  }

}
