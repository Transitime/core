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

import org.transitclock.db.structs.StopPath;
import org.transitclock.db.structs.TrafficPath;
import org.transitclock.db.structs.TrafficSensorData;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintain a mapping of Traffic Sensor data to TrafficPaths and
 * StopPaths for fast retrieval.
 */
public class TrafficDataMapper {

  private Map<StopPath, TrafficPath> stopPathTrafficPathMap = new HashMap<>();
  private Map<TrafficPath, StopPath> trafficPathStopPathMap = new HashMap<>();
  private Map<String, TrafficPath> sensorIdToTrafficPath = new HashMap<>();

  public void addTrafficPath(TrafficPath trafficPath) {
    this.sensorIdToTrafficPath.put(trafficPath.getTrafficPathId(), trafficPath);
  }

  public TrafficPath getTrafficPath(TrafficSensorData data) {
    return sensorIdToTrafficPath.get(data.getTrafficSensorId());
  }

  public boolean has(StopPath stopPath) {
    return stopPathTrafficPathMap.containsKey(stopPath);
  }

  public void mapStopPath(StopPath stopPath, TrafficPath trafficPath) {
    this.stopPathTrafficPathMap.put(stopPath, trafficPath);
    this.trafficPathStopPathMap.put(trafficPath, stopPath);
  }

  public TrafficPath get(StopPath stopPath) {
    return stopPathTrafficPathMap.get(stopPath);
  }

  public StopPath reverseLookup(TrafficPath trafficPath) { return trafficPathStopPathMap.get(trafficPath); }
}
