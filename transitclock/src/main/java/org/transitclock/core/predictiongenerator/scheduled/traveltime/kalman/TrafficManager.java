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

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.db.structs.ActiveRevisions;
import org.transitclock.db.structs.StopPath;
import org.transitclock.db.structs.TrafficPath;
import org.transitclock.db.structs.TrafficSensorData;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Manage traffic data access, retrieval, and storage;
 */
public class TrafficManager {

  public static final BooleanConfigValue trafficDataEnabled
          = new BooleanConfigValue("transitclock.traffic.enabled",
          false,
          "Enable Traffic Data integration");

  private static final Logger logger =
          LoggerFactory.getLogger(TrafficManager.class);

  private static TrafficManager INSTANCE;
  private TrafficDataMapper mapper;
  private TrafficDataCache cache;
  private TrafficDataHistoricalCache historicalCache;

  private Integer trafficRev = null;


  /**
   * Static access via getInstance() as is the convention with TTC.
   */
  protected TrafficManager() {
  }

  public static TrafficManager getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new TrafficManager();
      INSTANCE.init();
    }
    return INSTANCE;
  }

  private boolean testingEnable = false;
  public void setEnabled(boolean b) {
    testingEnable = true;
  }
  public boolean isEnabled() {
    return testingEnable || trafficDataEnabled.getValue();
  }

  /**
   * for testing.
   * @param mapper
   */
  public void setMapper(TrafficDataMapper mapper) {
    this.mapper = mapper;
  }
  /**
   * for testing.
   * @param trafficRev
   */
  public void setTrafficRev(int trafficRev) {
    this.trafficRev = trafficRev;
  }
  // for testing.
  public void setCache(TrafficDataCache cache) {
    this.cache = cache;
  }
  // for testing.
  public void setHistoricalCache(TrafficDataHistoricalCache historicalCache) {
    this.historicalCache = historicalCache;
  }

  /**
   * load internal state and setup the caches.
   */
  private void init() {
    Session session = Core.getInstance().getDbConfig().getGlobalSession();
    ActiveRevisions activeRevisions = ActiveRevisions.get(session);

    trafficRev = activeRevisions.getTrafficRev();

    mapper = new TrafficDataMapper();
    if (trafficRev == null) {
      // no traffic data -- nothing to do
      return;
    }

    // setup mapping for fast retrieval
    List<TrafficPath> allTrafficPaths = TrafficPath.getTrafficPaths(session, trafficRev);
    for (TrafficPath trafficPath : allTrafficPaths) {
      mapper.addTrafficPath(trafficPath);
      for (StopPath stopPath : trafficPath.getStopPaths()) {
        if (mapper.has(stopPath)){
          logger.info("ignoring duplicate stopPath {} to tripPath {} mapping",
                  stopPath, trafficPath);
        } else {
          mapper.mapStopPath(stopPath, trafficPath);
        }
      }
    }

    // initialize our caches
    historicalCache = new TrafficDataHistoricalCache(mapper, trafficRev);
    cache = new TrafficDataCache(historicalCache, mapper, trafficRev);

  }


  public boolean hasTrafficData(StopPath stopPath) {
    if (!isEnabled()) return false;
    return mapper.has(stopPath);
  }

  /**
   * Retrieve the current travel time in millis for the given
   * StopPath.
   * @param stopPath
   * @return
   */
  public Long getTravelTime(StopPath stopPath) {
    if (!isEnabled()) return null;
    TrafficPath trafficPath = mapper.get(stopPath);
    TrafficSensorData data = getTrafficSensorData(trafficPath);
    if (data == null) return null;
    if (data.getSpeed() == null) return null;
    // time = distance(m) / velocity (m/s)
    return new Double(stopPath.getLength()
                      / data.getSpeed()).longValue() * 1000;
  }

  public TrafficSensorData getTrafficSensorDataForStopPath(StopPath stopPath) {
    if (!isEnabled()) return null;
    TrafficPath trafficPath = mapper.get(stopPath);
    return getTrafficSensorData(trafficPath);
  }

  public Double getSpeed(StopPath stopPath) {
    if (!isEnabled()) return null;
    TrafficPath trafficPath = mapper.get(stopPath);
    TrafficSensorData data = getTrafficSensorData(trafficPath);
    if (data == null) return null;
    return data.getSpeed();
  }

  /**
   * estimated length of traffic sensor.
   * @param stopPath
   * @return
   */
  public Double getLength(StopPath stopPath) {
    if (!isEnabled()) return null;
    TrafficPath trafficPath = mapper.get(stopPath);
    TrafficSensorData data = getTrafficSensorData(trafficPath);
    if (data == null) return null;
    return data.getLength();
  }

  public Integer getTotalTravelTime(StopPath stopPath) {
    if (!isEnabled()) return null;
    TrafficPath trafficPath = mapper.get(stopPath);
    TrafficSensorData data = getTrafficSensorData(trafficPath);
    if (data == null) return null;
    return data.getTravelTimeMillis();
  }

  private TrafficSensorData getTrafficSensorData(TrafficPath trafficPath) {
    if (!isEnabled()) return null;
    return cache.get(trafficPath);
  }

  /**
   * Retrieve the travel time in millis for the StopPath at the given
   * time period, if it exists.  The precision of thetime parameter is implicitly
   * shifted to the appropriate value.
   * @param stopPath
   * @param time
   * @return
   */
  public Long getHistoricalTravelTime (StopPath stopPath, long time) {
    if (!isEnabled()) return null;
    return historicalCache.getHistoricalTravelTime(stopPath, time);
  }

/**
 * load data on startup populating internal caches.
 */
  public void populateCacheFromDb(Session session, Date startDate, Date endDate) {

    List<TrafficSensorData> list = TrafficSensorData.getTrafficSensorDataFromDb(session, startDate, endDate);
    Iterator<TrafficSensorData> iterator = list.iterator();

    int i = 0;
    while (iterator.hasNext()) {
      i++;
      if (i % 1000 == 0) {
        logger.info("loaded {} traffic sensors for {}", i, startDate);
      }
      TrafficSensorData element = iterator.next();
      historicalCache.put(element);
    }
  }
}
