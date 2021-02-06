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
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.StringConfigValue;
import org.transitclock.configData.CoreConfig;
import org.transitclock.db.structs.ActiveRevisions;
import org.transitclock.db.structs.StopPath;
import org.transitclock.db.structs.TrafficPath;
import org.transitclock.db.structs.TrafficSensorData;
import org.transitclock.utils.JsonUtils;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manage traffic data access, retrieval, and storage;
 */
public class TrafficManager {

  private static final StringConfigValue TRAFFIC_URL
          = new StringConfigValue("transitclock.traffic.shapeUrl",
          "https://pulse-io.blyncsy.com/geoservices/project_route_data/rest/services/81/FeatureServer/0/query?f=json&returnGeometry=false",
          "URL of traffic sensor shapes");

  private static final Logger logger =
          LoggerFactory.getLogger(TrafficManager.class);
  private static TrafficManager INSTANCE;
  private Map<StopPath, TrafficPath> stopPathTrafficPathMap = new HashMap<>();
  private Map<String, TrafficPath> sensorIdToTrafficPath = new HashMap<>();

  private Integer trafficRev = null;

  protected TrafficManager() {
  }

  public static TrafficManager getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new TrafficManager();
      INSTANCE.init();
    }
    return INSTANCE;
  }

  public void setTrafficRev(int trafficRev) {
    this.trafficRev = trafficRev;
  }

  private void init() {
    Session session = Core.getInstance().getDbConfig().getGlobalSession();
    ActiveRevisions activeRevisions = ActiveRevisions.get(session);

    trafficRev = activeRevisions.getTrafficRev();
    if (trafficRev == null) {
      // no traffic data -- nothing to do
      return;
    }
    List<TrafficPath> allTrafficPaths = TrafficPath.getTrafficPaths(session, trafficRev);
    for (TrafficPath trafficPath : allTrafficPaths) {
      this.sensorIdToTrafficPath.put(trafficPath.getTrafficPathId(), trafficPath);
      for (StopPath stopPath : trafficPath.getStopPaths()) {
        if (stopPathTrafficPathMap.containsKey(stopPath)) {
          throw new IllegalStateException("duplicate mapping "
                  + stopPath.toString() + " to "
                  + trafficPath.toString());
        }
        this.stopPathTrafficPathMap.put(stopPath, trafficPath);
      }
    }

  }


  public boolean hasTrafficData(StopPath stopPath) {
    return this.stopPathTrafficPathMap.containsKey(stopPath);
  }

  public Long getTravelTime(StopPath stopPath) {
    TrafficPath trafficPath = this.stopPathTrafficPathMap.get(stopPath);
    TrafficSensorData data = getTrafficSensorData(trafficPath);
    double speed = data.getSpeed();
    return new Double(stopPath.getLength() * speed).longValue();
  }

  private Map<TrafficPath, TrafficSensorData> trafficSensorDataMap = new HashMap<>();

  private TrafficSensorData getTrafficSensorData(TrafficPath trafficPath) {
    TrafficSensorData trafficSensorData = trafficSensorDataMap.get(trafficPath);
    if (trafficSensorData == null) {
      refresh();
      trafficSensorData = trafficSensorDataMap.get(trafficPath);
    }
    return trafficSensorData;
  }

  private synchronized void refresh() {
    List<TrafficSensorData> sensorData = null;
    try {
      sensorData = loadData();
      addtoQueue(sensorData);
    } catch (Exception e) {
      logger.error("loading error:", e);
    }
    for (TrafficSensorData data : sensorData) {
      this.trafficSensorDataMap.put(mapSensorToStopPath(data), data);
    }
  }

  private void addtoQueue(List<TrafficSensorData> sensorData) {
    if (!CoreConfig.storeDataInDatabase()) return;

    for (TrafficSensorData data : sensorData) {
      Core.getInstance().getDbLogger().add(data);
    }
  }

  private TrafficPath mapSensorToStopPath(TrafficSensorData data) {
    return sensorIdToTrafficPath.get(data.getTrafficSensorId());
  }

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

  public Long getHistoricalTravelTime (StopPath stopPath, long time) {
    return null;
  }

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
    return (mph / 0.61) * 1000;
  }
}
