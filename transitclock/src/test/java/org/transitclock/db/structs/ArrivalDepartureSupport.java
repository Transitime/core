package org.transitclock.db.structs;

import org.transitclock.StopSupport;
import org.transitclock.StructSupport;
import org.transitclock.SingletonSupport;
import org.transitclock.applications.Core;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertNotNull;
import static org.transitclock.SingletonSupport.getStreamAsString;

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

  private StopSupport stopSupport = new StopSupport();

  public List<ArrivalDeparture> loadArrivalDepartureList(String s) throws Exception {
    loadStopsMap();
    InputStream is1;
    if (s.endsWith(".zip")) {
      ZipInputStream zin = new ZipInputStream(this.getClass().getResourceAsStream(s));
      zin.getNextEntry();
      is1 = zin;
    } else {
      is1 = this.getClass().getResourceAsStream(s);
    }
    if (is1 == null) throw new FileNotFoundException("File " + s + " could not be found");
    // ArrivalDeparture data in CST TZ
    return toArrivalDepartures(getStreamAsString(is1), "CST");
  }

  private void loadStopsMap() throws Exception {
    SingletonSupport.createTestCore();
    if (Core.getInstance().getDbConfig().isEmptyStopsMap()) {
      InputStream stopsStream = this.getClass().getResourceAsStream("stops1.txt");
      assertNotNull(stopsStream);

      // ArrivalDeparture.getStop() requires this map be populated!!!
      Map<String, Stop> stopsMap = toStopsMap(getStreamAsString(stopsStream));
      Core.getInstance().getDbConfig().setStopsMap(stopsMap);
    }
  }

  public List<ArrivalDeparture> toArrivalDepartures(String csv, String tz) {
    List<ArrivalDeparture> list = new ArrayList<>();
    String[] lines = csv.split("\n");
    for (String line : lines) {
      // ignore comments
      if (!line.startsWith("//")) {
        list.add(toArrivalDeparture(line, tz));
      }
    }
    return list;
  }


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
            split[stopPathId],
            false
);
    return builder.create();

  }

  private Map<String, Stop> toStopsMap(String csv) {
    Map<String, Stop> map = new HashMap<>();
    String[] lines = csv.split("\n");
    for (String line: lines) {
      // if not header line
      if (line.indexOf("stop_code") == -1) {
        Stop stop = stopSupport.toStop(line);
        map.put(stop.getId(), stop);
      }
    }
    return map;
  }

}
