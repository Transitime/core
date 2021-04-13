package org.transitclock;

import org.transitclock.db.structs.Stop;

/**
 * Helper methods for loading Stops in Unit Tests.
 */
public class StopSupport extends StructSupport {
  private static final int stopId = 0;
  private static final int stopCode = 1;
  private static final int stopName = 2;
  private static final int stopDescription = 3;
  private static final int stopLat = 4;
  private static final int stopLon = 5;

  public Stop toStop(String csv) {
    //stop_id,stop_code,stop_name,stop_desc,stop_lat,stop_lon,stop_url,location_type,wheelchair_boarding,platform_code
    String[] split = csv.split(",");
    Stop stop = new Stop.Builder(-1,
            split[stopId],
            toInt(split, stopCode),
            split[stopName],
            toDouble(split, stopLat),
            toDouble(split, stopLon),
            false,
            false,
            false).create();
    return stop;
  }

}
