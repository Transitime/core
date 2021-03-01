/*
 * This file is part of Transitime.org
 *
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.transitclock.db.structs.Location;
import org.transitclock.db.structs.StopPath;
import org.transitclock.db.structs.TrafficPath;
import org.transitclock.db.structs.TrafficSensorData;
import org.transitclock.utils.Time;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TrafficManagerTest {

  private TrafficManager tm;
  private TrafficDataCache cache;
  private TrafficDataMapper mapper;

  @Before
  public void setUp() throws Exception {
    int trafficRev = 0;
    tm = new TrafficManager();
    tm.setTrafficRev(trafficRev);
    mapper = new TrafficDataMapper();
    tm.setMapper(mapper);
    TrafficDataHistoricalCache historicalCache = new TrafficDataHistoricalCache(mapper, trafficRev);
    cache = new TrafficDataCache(historicalCache, mapper, trafficRev);
    tm.setCache(cache);
    tm.setHistoricalCache(historicalCache);
    tm.setEnabled(true);
    cache.setArchiving(false);
  }

  @Test
  @Ignore // run this only if integrating with traffic data
  public void loadData() throws Exception {
    List<TrafficSensorData> sensorData = cache.loadData();
    assertNotNull(sensorData);
    assertTrue(sensorData.size() > 40);
    TrafficSensorData data1 = findSensorId(sensorData, "938");
    assertEquals("938", data1.getTrafficSensorId());
    assertTrue(data1.getTime().getTime() > System.currentTimeMillis() - Time.MS_PER_MIN);
  }

  @Test
  @Ignore
  public void getTravelTime() throws Exception {
    Long travelTime = tm.getTravelTime(null);
    assertNull(travelTime);

    StopPath sp = createStopPath();
    TrafficPath tp = createTrafficPath();
    travelTime = tm.getTravelTime(sp);
    assertNull(travelTime);

    mapper.mapStopPath(sp, tp);
    mapper.addTrafficPath(tp);
    travelTime = tm.getTravelTime(sp);
    assertNull(travelTime);

    List<TrafficSensorData> sensorData = cache.loadData();
    cache.updateCache(sensorData);
    cache.archive(sensorData);
    assertNotNull(cache.get(tp));
    long now = System.currentTimeMillis();
    travelTime = tm.getTravelTime(sp);
    assertNotNull(travelTime);

    Long historicalTravelTime = tm.getHistoricalTravelTime(sp, now);
    assertNotNull(historicalTravelTime);
  }

  private TrafficPath createTrafficPath() {
    String trafficPathId = "1522";
    int trafficRev = 0;
    float pathLength = 1000;
    return new TrafficPath(trafficPathId,
    trafficRev,
    pathLength);
  }

  private StopPath createStopPath() {
    String pathId = "p1";
    String stopId = "s1";
    String routeId = "r1";
    StopPath sp = new StopPath(-1,
            pathId,
            stopId,
    0,
    false,
    routeId,
    false,
    false,
    false,
    null,
    null,
    null);
    sp.setLocations(createLocations());

    assertTrue(sp.getLength() > 1.0);
    return sp;
  }

  private ArrayList<Location> createLocations() {
    ArrayList<Location> list = new ArrayList<>();
    list.add(new Location(38.8141,-77.1339));
    list.add(new Location(38.8123,-77.1217));
    return list;
  }

  private TrafficSensorData findSensorId(List<TrafficSensorData> sensorData, String id) {
    for (TrafficSensorData element : sensorData) {
      if (element.getTrafficSensorId().equals(id))
        return element;
    }
    return null;
  }

}