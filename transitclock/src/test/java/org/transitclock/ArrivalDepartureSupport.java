package org.transitclock;

import org.transitclock.db.structs.ArrivalDeparture;

/**
 * Helper methods for ArrivalDepartures within Unit Tests.
 */
public class ArrivalDepartureSupport extends StructSupport {
  private static final int vehicleId = 0;
  private static final int time = 1;
  private static final int avlTime = 2;
  private static final int block = 3;
  private static final int directionId = 4;
  private static final int tripIndex = 5;
  private static final int stopPathIndex = 6;
  private static final int stopOrder = 7;
  private static final int isArrival = 8;
  private static final int configRev = 9;
  private static final int scheduledTime = 10;
  private static final int blockId = 11;
  private static final int tripId = 12;
  private static final int stopId = 13;
  private static final int gtfsStopSeq = 14;
  private static final int stopPathLength  = 15;
  private static final int routeId = 16;
  private static final int routeShortName = 17;
  private static final int serviceId = 18;
  private static final int freqStartTime = 19;
  private static final int dwellTime = 20;
  private static final int tripPatternId = 21;
  private static final int stopPathId = 22;

  public ArrivalDeparture toArrivalDeparture(String csv, String tz) {
    String[] split = csv.split(",");
    ArrivalDeparture.Builder builder
            = new ArrivalDeparture.Builder(
            split[vehicleId],
            dateToLong(split, time, tz),
            dateToLong(split, avlTime, tz),
            null, /*block*/
            split[directionId],
            toInt(split, tripIndex),
            toInt(split, stopPathIndex),
            toInt(split, stopOrder),
            toBoolean(split, isArrival),
            toInt(split, configRev),
            dateToLong(split, scheduledTime, tz),
            split[blockId],
            split[tripId],
            split[stopId],
            toInt(split, gtfsStopSeq),
            toFloat(split, stopPathLength),
            split[routeId],
            split[routeShortName],
            split[serviceId],
            toLong(split, freqStartTime),
            toLong(split, dwellTime),
            split[tripPatternId],
            split[stopPathId]
);
    return builder.create();

  }


}
