package org.transitclock.avl;

import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.utils.Time;

import java.util.List;

/**
 * Match an APC observation to an AVL ArrivalDeparture.  As they come from different
 * systems account for some error in the matching.
 */
public class ApcMatcher {


  public static final IntegerConfigValue APC_MATCH_WINDOW_MINUTES
          = new IntegerConfigValue("transitclock.apc.matchWindowMinutes",
          5,/*minutes*/
          "delta between ArrivalDeparture timestamp and apc timestamp");
  public static final BooleanConfigValue APC_IS_ARRIVAL
          = new BooleanConfigValue("transitclock.apc.recordIsArrival",
          false,
          "if the ping observation is meant to be an arrival by default");

  /**
   * Match an APC record to an arrival/departure
   * @param apcRecords
   * @return
   */
  public List<ApcMatch> match(List<ArrivalDeparture> arrivals, List<ApcParsedRecord> apcRecords) {
    // a match is defined as overlap between apcRecord time and arrivalDeparture
    // more specifically
    // apcRecord will be an arrival
    // ArrivalDeparture.isArrival == true
    // ArrivalDeparture.time - window > apcRecord.time > ArrivalDeparture.time + window
    ArrivalDepartureIntervalList intervals = new ArrivalDepartureIntervalList(arrivals, getApcMatchWindowMillis(), APC_IS_ARRIVAL.getValue());
    return intervals.match(apcRecords);
  }

  private long getApcMatchWindowMillis() {
    return APC_MATCH_WINDOW_MINUTES.getValue() * Time.MS_PER_MIN;
  }



}
