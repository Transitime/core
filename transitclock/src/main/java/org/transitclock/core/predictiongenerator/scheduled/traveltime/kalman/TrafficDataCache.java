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

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.configData.CoreConfig;
import org.transitclock.db.structs.TrafficPath;
import org.transitclock.db.structs.TrafficSensorData;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.JsonUtils;
import org.transitclock.utils.Time;
import org.transitclock.utils.Timer;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Poll and cache traffic sensor data.  Access via TrafficManager.
 */
public class TrafficDataCache {

  private static final StringConfigValue TRAFFIC_URL
          = new StringConfigValue("transitclock.traffic.shapeUrl",
          "https://pulse-io.blyncsy.com/geoservices/project_route_data/rest/services/81/FeatureServer/0/query?f=json&returnGeometry=false",
          "URL of traffic sensor shapes");

  private static final IntegerConfigValue MAX_TRAFFIC_LATENCY_MINUTES
          = new IntegerConfigValue("transitclock.traffic.maxLatency",
          5,
          "Max age of traffic data to be considered valid in minuntes");

  private static final IntegerConfigValue TRAFFIC_REFRESH_RATE_SECONDS
          = new IntegerConfigValue("transitclock.traffic.refreshRate",
          60,
          "Refresh rate in seconds to poll traffic service");

  private int getRefreshRateInSeconds() {
    return TRAFFIC_REFRESH_RATE_SECONDS.getValue();
  }

  private long getMaxTrafficLatencyMillis() {
    return MAX_TRAFFIC_LATENCY_MINUTES.getValue() * Time.MS_PER_MIN;
  }

  private static final Logger logger =
          LoggerFactory.getLogger(TrafficDataCache.class);

  private Map<TrafficPath, TrafficSensorData> trafficSensorDataCache = new HashMap<>();
  private TrafficDataMapper mapper;
  private Integer trafficRev;
  private ScheduledThreadPoolExecutor refreshTimer = Timer.get();
  private TrafficDataHistoricalCache historicalCache;
  private boolean enableArchiving = true;

  public TrafficDataCache(TrafficDataHistoricalCache historicalCache, TrafficDataMapper mapper, Integer trafficRev) {
    this.historicalCache = historicalCache;
    this.mapper = mapper;
    this.trafficRev = trafficRev;
    if (!TrafficManager.trafficDataEnabled.getValue()) {
      // not enabled, don't setup timer thread
      logger.info("Traffic data polling disabled");
      return;
    }
    logger.info("Traffic data polling every {} seconds",
            getRefreshRateInSeconds());

    refreshTimer.scheduleAtFixedRate(
            new Runnable() {
              @Override
              public void run() {
                refresh();
              }
            }
    , 0, getRefreshRateInSeconds(), TimeUnit.SECONDS);
  }

  // for testing.
  public void setArchiving(boolean flag) {
    this.enableArchiving = flag;
  }

  /**
   * add traffic sensor data to the cache.
   * @param mapSensorToStopPath
   * @param data
   */
  public void cache(TrafficPath mapSensorToStopPath, TrafficSensorData data) {
    trafficSensorDataCache.put(mapSensorToStopPath, data);
  }

  /**
   * retrieve traffic sensor data from the cache.
   * @param trafficPath
   * @return
   */
  public TrafficSensorData get(TrafficPath trafficPath) {
    synchronized (trafficSensorDataCache) {
      TrafficSensorData data = trafficSensorDataCache.get(trafficPath);
      if (data != null && isValid(data)) {
        return data;
      }
      return null;
    }
  }

  private boolean isValid(TrafficSensorData data) {
    return data.getSpeed() != null && !isLatent(data);
  }

  boolean isLatent(TrafficSensorData data) {
    logger.info(" Oldest acceptable data {} > data date {}",
            new Date(System.currentTimeMillis() - getMaxTrafficLatencyMillis()),
            new Date(data.getTime().getTime()));

    if (System.currentTimeMillis() - getMaxTrafficLatencyMillis() > data.getTime().getTime())
      return true; // data is too old
    return false;
  }

  /**
   * update the cache on a configurable interval.
   */
  private void refresh() {
    List<TrafficSensorData> sensorData = null;
    IntervalTimer loadTimer = new IntervalTimer();
    try {
      sensorData = loadData();
      archive(sensorData);
    } catch (Exception e) {
      logger.error("loading error:", e);
      // something went wrong -- no sense in updating our cache
      return;
    } finally {
      logger.info("Traffic data loading and archiving complete in {} msec.",
              loadTimer.elapsedMsec());
    }

    IntervalTimer updateTimer = new IntervalTimer();
    synchronized (trafficSensorDataCache) {
      updateCache(sensorData);
    }
    logger.info("Traffic data cache update complete in {} msec",
            updateTimer.elapsedMsec());
  }

  void updateCache(List<TrafficSensorData> sensorData) {
    trafficSensorDataCache.clear();
    int mapped = 0;
    for (TrafficSensorData data : sensorData) {
      TrafficPath trafficPath = mapper.getTrafficPath(data);
      if (trafficPath != null) {
        mapped++;
        cache(trafficPath, data);
      }
    }
    logger.info("mapped {} traffic sensors of {}",
            mapped,
            sensorData.size());
  }

  /**
   * retrieve the data from the traffic sensors.
   * @return
   * @throws Exception
   */
  List<TrafficSensorData> loadData() throws Exception {
    List<TrafficSensorData> elements = new ArrayList<>();

    URL urlObj = new URL(TRAFFIC_URL.getValue());
    URLConnection connection = urlObj.openConnection();
    InputStream in = connection.getInputStream();
    String jsonStr = JsonUtils.getJsonString(in);

    JSONObject descriptor = new JSONObject(jsonStr);
    JSONArray features = (JSONArray) descriptor.get("features");
    for (int i = 0; i < features.length(); i++) {
      try {
        TrafficSensorData data = parseData(features.getJSONObject(i), trafficRev);
        if (data != null) {
          elements.add(data);
        }
      } catch (Exception any) {
        try {
          logger.warn("exception parsing feature {}", features.getJSONObject(i));
        } catch (Exception bury) {}
      }
    }
    return elements;
  }

  /**
   * parse out data into TTC structs.
   * @param o
   * @param trafficRev
   * @return
   */
  private TrafficSensorData parseData(JSONObject o, int trafficRev) {
    JSONObject a = o.getJSONObject("attributes");
    if (!a.has("mph"))
      return null;
    long time = a.getLong("time");
    String externalId = String.valueOf(a.getInt("id"));
    double speed = toMetersPerSecond(a.getDouble("mph"));
    double delayMillis = a.getDouble("delaySeconds") * 1000;
    Integer travelTimeMillis = a.getInt("travelTime");
    double confidence = a.getDouble("confidence");
    TrafficSensorData data = new TrafficSensorData(
            externalId,
            trafficRev,
            new Date(time),
            speed,
            delayMillis,
            confidence,
            travelTimeMillis
    );
    return data;
  }

  private double toMetersPerSecond(double mph) {
    return (mph / 2.237);
  }

  /**
   * The data we retrieve needs to be archived to the database
   * (if configured) and copied into the historical cache.
   * @param sensorData
   */
  void archive(List<TrafficSensorData> sensorData) {
    if (historicalCache != null) {
      // update historical cache
      historicalCache.put(sensorData);
    }

    if (!enableArchiving) return;
    if (!CoreConfig.storeDataInDatabase()) return;

    // flush to the database
    for (TrafficSensorData data : sensorData) {
      Core.getInstance().getDbLogger().add(data);
    }

  }


}
