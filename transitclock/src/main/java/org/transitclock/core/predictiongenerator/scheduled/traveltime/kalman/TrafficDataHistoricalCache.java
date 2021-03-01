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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.StopPath;
import org.transitclock.db.structs.TrafficPath;
import org.transitclock.db.structs.TrafficSensorData;
import org.transitclock.utils.Time;

import java.util.HashMap;
import java.util.List;

/**
 * Historical cache of traffic sensor data.  Access
 * via TrafficManager.
 */
public class TrafficDataHistoricalCache {

  private static final Logger logger =
          LoggerFactory.getLogger(TrafficDataHistoricalCache.class);
  private TrafficDataMapper mapper;

  // TODO this needs to purge based on configuration
  // consider guava or equiv impl here
  private HashMap<TrafficDataKey, TrafficSensorData> cache;

  public TrafficDataHistoricalCache(TrafficDataMapper mapper, Integer trafficRev) {
    this.mapper = mapper;
    this.cache= new HashMap<>(); // TODO setup expiry here
  }

  /**
   * Accept traffic data en masse for storage.
   * @param data
   * @return
   */
  public boolean put(List<TrafficSensorData> data) {
    int mapped = 0;
    for (TrafficSensorData element : data) {
      boolean success = put(element);
      if (success) mapped++;
    }
    logger.info("mapped {} sensors of {}",
            mapped, data.size());
    return mapped > 0;
  }

  /**
   * Accept traffic for storage.
   * @param element
   * @return
   */
  public boolean put(TrafficSensorData element) {
    // we spend the effort now to make the retrieval easy
    TrafficDataKey key = hash(element);
    if (key != null) {
      cache.put(key, element);
      return true;
    }
    return false;
  }
  /**
   * create a key of the data element for fast retrieval.
   * @param data
   * @return
   */
  private TrafficDataKey hash(TrafficSensorData data) {
    TrafficPath trafficPath = mapper.getTrafficPath(data);
    if (trafficPath == null) return null;
    StopPath stopPath = mapper.reverseLookup(trafficPath);
    if (stopPath == null) return null;
    return new TrafficDataKey(stopPath, data.getTime().getTime());
  }

  /**
   * Retrieve traffic sensor data relating to the given StopPath
   * at the time.  Time is implicitly shifted to the appropriate
   * precision.
   * @param stopPath
   * @param time
   * @return
   */
  public Long getHistoricalTravelTime(StopPath stopPath, long time) {
    TrafficDataKey cacheKey = new TrafficDataKey(stopPath, time);
    TrafficSensorData trafficSensorData = cache.get(cacheKey);
    if (trafficSensorData == null) {
      // try once more for the previous minute
      time = time - Time.MS_PER_MIN;
      cacheKey = new TrafficDataKey(stopPath, time);
      trafficSensorData = cache.get(cacheKey);
    }
    if (trafficSensorData != null) {
      double speed = trafficSensorData.getSpeed();
      // time = velocity * length
      return new Double(stopPath.getLength() * speed).longValue();
    }
    return null;

  }



}
