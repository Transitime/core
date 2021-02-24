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
package org.transitclock.applications;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.operation.overlay.snap.LineStringSnapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.ConfigFileReader;
import org.transitclock.config.DoubleConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.ActiveRevisions;
import org.transitclock.db.structs.Location;
import org.transitclock.db.structs.StopPath;
import org.transitclock.db.structs.TrafficPath;
import org.transitclock.db.structs.TrafficSensor;
import org.transitclock.traffic.FeatureData;
import org.transitclock.traffic.FeatureGeometry;
import org.transitclock.traffic.TrafficWriter;
import org.transitclock.utils.Geo;
import org.transitclock.utils.JsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Load in traffic sensors shapes, snap to stop paths,
 * then flush to database as TrafficSensors and TrafficPaths.
 */
public class LoadTrafficSensors {

  private static final StringConfigValue TRAFFIC_URL
          = new StringConfigValue("transitclock.traffic.shapeUrl",
          "https://pulse-io.blyncsy.com/geoservices/project_route_data/rest/services/81/FeatureServer/0/query?f=json&returnGeometry=true",
          "URL of traffic sensor shapes");

  // increase the tolerance to include the stops that are off centerline but included in stop shapes
  private static final double DEFAULT_SNAP_TOLERANCE = 0.00006; /* times 110996 per degree = 6 * 1.109964513599842 m */
  private static final DoubleConfigValue SNAP_TOLERANCE
          = new DoubleConfigValue("transitclock.traffic.snapTolerance", DEFAULT_SNAP_TOLERANCE,
          "Distance in degrees two shapes can be considered for snapping.  Smaller is more precise");

  protected static final double DEFAULT_MIN_SNAP_SCORE = 0.5; // 50% match of sensor to segment
  protected static final DoubleConfigValue MIN_SNAP_SCORE = new DoubleConfigValue("transitclock.traffic.minSnapScore",
          0.5,
          "percentage of snapped shape that needs to match before being considered an overall match");

  private static final int DEFAULT_MAX_GAP_SIZE = 2;
  private static final IntegerConfigValue MAX_GAP_SIZE = new IntegerConfigValue("transitclock.traffic.maxGapSize",
          DEFAULT_MAX_GAP_SIZE,
          "max difference in segment indexes of traffic shape, " +
                  "used to detect loops and other possible bad matches");

  private static final int DEFAULT_MIN_GAP_SIZE = 0;
  private static final IntegerConfigValue MIN_GAP_SIZE = new IntegerConfigValue("transitclock.traffic.minGapSize",
          DEFAULT_MIN_GAP_SIZE,
          "min difference in segment indexes of traffic shape");

  private static final int DEFAULT_MIN_STOP_SEGMENT_LENGTH_FOR_GAP_CHECK = 4;
  private static final IntegerConfigValue MIN_STOP_SEGMENT_LENGTH_FOR_GAP_CHECK
          = new IntegerConfigValue("transitclock.traffic.MinStopSegmentLengthForGapCheck",
          DEFAULT_MIN_STOP_SEGMENT_LENGTH_FOR_GAP_CHECK,
          "Minimum number of segments in stop path to apply gap check algorithmn to");
  private static final int MAX_OPPOSING_DEGREES = 120;

  protected String getTrafficUrl() { return TRAFFIC_URL.getValue(); }
  private double getSnapTolerance() { return SNAP_TOLERANCE.getValue(); }
  private double getMinSnapScore() { return MIN_SNAP_SCORE.getValue();}
  private int getMaxGapSize() { return MAX_GAP_SIZE.getValue(); }
  private int getMinGapSize() { return MIN_GAP_SIZE.getValue(); }
  private int getMinStopSegmentLengthForGapCheck() { return MIN_STOP_SEGMENT_LENGTH_FOR_GAP_CHECK.getValue();}

  static {
    ConfigFileReader.processConfig();
  }

  private static final Logger logger =
          LoggerFactory.getLogger(LoadTrafficSensors.class);

  private String agencyId = null;
  private Integer configRev = null;
  // maintain a list of sensors retrieved for later storage in database
  private List<TrafficSensor> sensors = new ArrayList<>();
  // our database session
  private Session session;
  private Integer trafficRev = null;
  // highest snapping score statistic
  private double highScore = -1.0;
  // count of most stopPats attached to a single traffic sensor
  private int mostMatches = 0;
  // track stop paths to traffic paths independent of shape
  private Set<String> hashOfJoins = new HashSet<>();

  /**
   * public entry point into object.
   */
  public void run() {
    agencyId = AgencyConfig.getAgencyId();
    String url = getTrafficUrl();
    List<FeatureData> featureDataList = null;
    // carefully manage a single session OLD SCHOOL STYLE!
    session = HibernateUtils.getSession(agencyId);
    Transaction tx = null;

    try {

      // Put db access into a transaction
      tx = session.beginTransaction();
      TrafficWriter writer = new TrafficWriter(session, getTrafficRev());

      // our traffic sensor data including shapes
      featureDataList = loadFeatureDataFromURL(url);

      // perform snapping of traffic sensor shapes to StopPaths
      mapFeaturesToStopPaths(featureDataList);

      // create database structs representing the above data
      List<TrafficPath> trafficPaths = createTrafficPaths(featureDataList);

      logger.info("writing {} sensors to database", sensors.size());
      writer.writeSensors(sensors);

      logger.info("writing {} traffic paths to database", trafficPaths.size());
      writer.writeTrafficPaths(trafficPaths);

      logger.info("Flushing data to database...");
      session.flush();
      logger.info("Done flushing");

      logger.info("committing transaction....");
      tx.commit();
      logger.info("commit complete!");

    } catch (Exception any) {
      logger.error("run failed", any);
      Throwable rootCause = HibernateUtils.getRootCause(any);
      logger.error("root cause", rootCause);

      if (tx != null)
        tx.rollback();
    } finally {
      if (session != null) {
        session.close();
      }
    }
  }


  // parse JSON into a POJO, later it will be become a TrafficSensor struct
  private FeatureData parseFeatureData(JSONObject o) {
    JSONObject a = o.getJSONObject("attributes");
    long time = a.getLong("time");
    String label = a.getString("label");
    String externalId = String.valueOf(a.getInt("id"));
    FeatureGeometry fg = parseFeatureGeometry(o.getJSONObject("geometry"));
    FeatureData fd = new FeatureData();
    fd.setTime(time);
    fd.setLabel(label);
    fd.setId(externalId);
    fd.setFeatureGeometry(fg);
    fd.setLength((float) calculateLengthInMeters(fg));
    return fd;
  }

  // calculate the length of a shape
  double calculateLengthInMeters(FeatureGeometry fg) {
    Coordinate[] points = fg.getAsCoordinateArray();
    return calculateLengthInMeters(points);
  }

  double calculateLengthInMeters(Coordinate[] points) {
    double length = 0.0;
    for (int i = 1; i< points.length; i++) {
      length += Geo.distanceHaversine(toLocation(points[i-1]),
              toLocation(points[i]));
    }
    return length;
  }

  // parse the shape into a POJO for later snapping
  private  FeatureGeometry parseFeatureGeometry(JSONObject geometry) {
    JSONArray paths = geometry.getJSONArray("paths");
    // we have an anonymous array -- we only support the first path
    JSONArray perPaths = paths.getJSONArray(0);
    FeatureGeometry fg = new FeatureGeometry();
    for (int i = 0; i < perPaths.length(); i++) {
      // another anonymous array
      JSONArray coordinate = perPaths.getJSONArray(i);
      double lat = coordinate.getDouble(1);
      double lon = coordinate.getDouble(0);
      fg.addLatLon(lat, lon);
    }
    return fg;
  }

  // call out to webservice and parse into POJOs
  List<FeatureData> loadFeatureDataFromURL(String urlStr)
  throws Exception {
    List<FeatureData> elements = new ArrayList<>();

    logger.info("loading feature data from {}", urlStr);
    URL urlObj = new URL(urlStr);
    URLConnection connection = urlObj.openConnection();
    InputStream in = connection.getInputStream();
    String jsonStr = getJsonString(in);

    JSONObject descriptor = new JSONObject(jsonStr);
    JSONArray features = (JSONArray) descriptor.get("features");

    logger.info("constructing {} feature elements", features.length());
    for (int i=0; i<features.length(); i++) {
      FeatureData featureData = parseFeatureData(features.getJSONObject(i));
      if (featureData != null) {
        elements.add(featureData);
      }
    }

    logger.info("feature data creation complete");
    return elements;
  }

  /**
   * attach StopPaths to FeatureData POJOs if the successfully snap geometries.
   * @param featureDataList
   */
  private void mapFeaturesToStopPaths(List<FeatureData> featureDataList) {
    List<StopPath> allStopPaths = loadStopPaths();
    if (allStopPaths == null) {
      logger.error("no StopPaths loaded, bailing");
      return;
    }
    logger.info("mapping {} features to {} StopPaths.", featureDataList.size(), allStopPaths.size());
    for (FeatureData fd : featureDataList) {
      logger.info("mapping \"{}\"", fd.getLabel());
      mapFeatureToStopPaths(fd, allStopPaths);
    }
  }

  /**
   * load the current set of StopPath structs into memory.
   * @return
   */
  private List<StopPath> loadStopPaths() {
    List<StopPath> allStopPaths = null;
    try {
      logger.info("loading all StopPaths for configRev {}", getConfigRev());
      allStopPaths = StopPath.getPaths(session,
              getConfigRev());
    } finally {
      if (allStopPaths != null) {
        logger.info("StopPath loading complete, loaded {} elements", allStopPaths.size());
      } else {
        logger.info("StopPath loading failed.  No elements present");
      }
    }
    return allStopPaths;
  }

  private String getAgencyId() {
    return agencyId;
  }

  /**
   * lazy load the current configRev from the database.
   * @return
   */
  private int getConfigRev() {
    if (configRev == null) {
      configRev = ActiveRevisions.get(getAgencyId()).getConfigRev();
    }
    return configRev;
  }


  /**
   * attempt to snap the FeatureGeometries to the SnapPaths based on
   * the configured tolerance.  Score the snapping, as shape matching
   * is ultimately a compromise.
   * @param fd
   * @param allStopPaths
   * @return the highest score of the match for unit testing.
   */
  Double mapFeatureToStopPaths(FeatureData fd, List<StopPath> allStopPaths) {
    // this is where some magic happens
    // we have a traffic segment feature path, and a stop path geometry
    // if they snap to each other then we assign
    // that stopPath to the feature
    Coordinate[] sensorLineString = toLineString(fd);
    // LineStringSnapper from JTS helps us with the GIS snapping
    LineStringSnapper snapper = new LineStringSnapper(sensorLineString, getSnapTolerance());
    int sensorBearing = getBearing(sensorLineString);

    // check all stop paths against this feature
    for (StopPath sp : allStopPaths) {

      if (fd.getStopPaths().contains(sp)) {
        // whoops, we've already been here!
        logger.warn("fd {} already has sp {}",
                fd.getLabel(), sp.getId());
        continue;
      }

      try {
        Coordinate[] stopPathLineString = toLineString(sp);
        /*
        * Snap the StopPath shape to the Sensor shape.  This is accomplished by snapping
        * both vertices and edges.  Thus the Matches array may be a mix of both vertices and
        * edges.
        * Note that while this is computationally expensive, the current dataset is both small
        * and in memory and execute quickly relative to database operations.
        */
        Coordinate[] matches = snapper.snapTo(stopPathLineString);
        // matches is now the combination of sensor and stopPath line strings that overlayed
        // for our purposes we only want the stopPath matches for scoring
        // so remove the input sensor coordinates
        matches = removeSourcePoints(sensorLineString, matches);
        // score the result filtering out certain conditions along the way
        double score = scoreOverlay(sensorLineString, stopPathLineString, matches);
        if (score > highScore) {
          // log some stats to confirm proper tolerance configuration
          logger.info("new high score of {} for traffic sensor \"{}\" and stop path \"{}\"",
                  score, fd.getLabel(), sp.getId());
          highScore = score;
        }
        int stopPathBearing = getBearing(stopPathLineString);
        if (score > getMinSnapScore() && Math.abs(sensorBearing - stopPathBearing) < MAX_OPPOSING_DEGREES) {
          logger.info("match score {} for sensor {} to stop path {}",
                  score, fd.getLabel(), sp.getId());
          if (logger.isDebugEnabled()) {
            debugMatch(sensorLineString, stopPathLineString, matches);
          }
          fd.addStopPath(sp);
          int matchesCount = fd.getStopPaths().size();
          if (matchesCount > 1) {
            if (matchesCount > mostMatches) {
              mostMatches = matchesCount;
              // another stat to consider -- how many stop paths are attached to the
              // most popular sensor
              logger.info("most matches {} for sensor {}", matchesCount, fd.getLabel());
              if (logger.isDebugEnabled()) {
                debugMatch(sensorLineString, stopPathLineString, matches);
              }
            }
            logger.debug("multiple matches={} for sensor {} ", matchesCount, fd.getLabel());
          }
        } else {
          if (logger.isDebugEnabled()) {
            logger.debug("rejected for score " + score);
            debugMatch(sensorLineString, stopPathLineString, matches);
          }
        }
      } catch (Exception any) {
        logger.error("exception adding stop path {}", any, any);
      }

    }
    return highScore;
  }

  private int getBearing(Coordinate[] stopPathLineString) {
    if (stopPathLineString.length < 2) return 0;
    int j = stopPathLineString.length -1;
    LineSegment lastSegment = new LineSegment(stopPathLineString[0].x,
            stopPathLineString[0].y,
            stopPathLineString[j].x,
            stopPathLineString[j].y);

    return (int) (lastSegment.angle() * (180/Math.PI));

  }

  /**
   * Often route shapes contain the stops they pass by. Remove them from the
   * match as they are "noise".
   * @param sensorLineString
   * @param matches
   * @return
   */
  private Coordinate[] removeSourcePoints(Coordinate[] sensorLineString, Coordinate[] matches) {
    ArrayList<Coordinate> filtered = new ArrayList<>();
    List<Coordinate> sourceLineString = Arrays.asList(sensorLineString);
    for (Coordinate overlayCoordinate : matches) {
      if (!sourceLineString.contains(overlayCoordinate)) {
        filtered.add(overlayCoordinate);
      }
    }
    return filtered.toArray(new Coordinate[filtered.size()]);
  }

  private void debugJoin(TrafficPath tp, StopPath sp) {
    debugLink(toLineString(tp), toLineString(sp), null);
  }

  /**
   * log some info about the match for debugging/verification
   * @param featureLineString
   * @param stopPathLineString
   * @param matches
   */
  void debugMatch(Coordinate[] featureLineString, Coordinate[] stopPathLineString, Coordinate[] matches) {
    debugLink(featureLineString, stopPathLineString, matches);
    debugShape("feature", featureLineString);
    debugShape("stop path", stopPathLineString);
    debugShape("matches", matches);
  }

  /**
   * debug the shape of the sensor against the matching stop path
   * points via an externally hosted web page.
   * @param featureLineString
   * @param stopPathLineString
   * @param matches
   */
  void debugLink(Coordinate[] featureLineString, Coordinate[] stopPathLineString, Coordinate[] matches) {
    StringBuffer sb = new StringBuffer();
    sb.append("http://developer.onebusaway.org/maps/debug.html?polyline=");
    for (Coordinate c : featureLineString) {
      sb.append(c.x).append("%2C").append(c.y).append("%20");
    }
    sb.append("&points=");
    for (Coordinate c : stopPathLineString) {
      sb.append(c.x).append("%2C").append(c.y).append("%20");
    }

    logger.info("\n" + sb.toString() + "\n");
  }

  /**
   * utility function to log shape
   * @param shapeName
   * @param shape
   */
  void debugShape(String shapeName, Coordinate[] shape) {

    logger.info("{}:", shapeName);
    StringBuffer sb = new StringBuffer();
    sb.append("\n");
    for (Coordinate c : shape) {
      sb.append(c.x).append(",").append(c.y).append('\n');
    }
    logger.info(sb.toString());
  }

  // based on the inputs and matches determine a percentage score, the higher the score the better
  // as part of scoring, down-score some special circumstances we don't want to consider as matches
  double scoreOverlay(Coordinate[] sensorShape, Coordinate[] stopSegmentShape, Coordinate[] matches) {
    if (matches.length <= 0) return 0.0;
    // simple percentage calculation -- if all stopSegmentShapes match we get 1.0
    double score = (double)matches.length / stopSegmentShape.length;
    if (score >= getMinSnapScore()) {
      int gapSize = countGapsInShape(sensorShape, stopSegmentShape, matches);
      // only consider gaps if the stop segment is large enough to have them
      if (stopSegmentShape.length > getMinStopSegmentLengthForGapCheck()) {
        if (gapSize > getMaxGapSize()) {
          logger.info("disqualifying match due to gap of {}", gapSize);
          if (logger.isDebugEnabled()) {
            debugMatch(sensorShape, stopSegmentShape, matches);
          }
          return gapSize * -1;
        } else if (gapSize < getMinGapSize()) {
          logger.info("disqualifying match due to back matches of {}", gapSize * -1);
          if (logger.isDebugEnabled()) {
            debugMatch(sensorShape, stopSegmentShape, matches);
          }
          return gapSize;
        }
      }
    }
    return score;
  }

  /**
   * Check for gaps in the overlay matching, and count how many points failed.
   * Multiple point gaps might indicate a
   * loop or otherwise mismatch in shape that should disqualify the
   * overlay.  Gaps on the ends are fine and expected.
   * @param source
   * @param test
   * @param matches
   * @return
   */
  int countGapsInShape(Coordinate[] source, Coordinate[] test, Coordinate[] matches) {
    // for each value in test that's in match, keep track of the index
    // return the largest gap that is not the start or end
    ArrayList<Integer> indices = new ArrayList<>();
    for (int i = 0; i < matches.length; i++) {
      indices.add(findMatchInArray(test, matches[i]));
    }

    ArrayList<Integer> diffs = new ArrayList<>();
    // not compute the gap sizes between elements
    // throw out start and end gaps
    // 0,1,2,3,4,5 (GOOD)
    // 0, 0, 0, 1,...,2,7,8....,9,10 (BAD) = gap of 5 (2-7)
    for (int i = 1; i < indices.size(); i++) {
      diffs.add(indices.get(i) - indices.get(i-1));
    }

    int maxGap = 0;
    int gapCount = 0;
    for (int i = 0; i < diffs.size(); i++) {
      if (diffs.get(i) > maxGap) {
        maxGap = diffs.get(i);
      } else if (diffs.get(i) < -1) {
        // we backtracked on the shape by more than one point, record that as well
        // we occasionally swap points as the shape climbs the stop
        gapCount++;
      }
    }
    if (gapCount > 1)
      return gapCount * -1;
    return maxGap;
  }

  /**
   * test if the exact coordinate exists in the array.  Used to remove
   * stops from stop shapes that are noise to the snapping algorithm
   * @param test
   * @param match
   * @return
   */
  private Integer findMatchInArray(Coordinate[] test, Coordinate match) {
    double closestMatch = Double.POSITIVE_INFINITY;
    int closestIndex = -1;

    for (int i = 0; i < test.length; i++) {
      double distanceAways = Geo.distanceHaversine(toLocation(match),
              toLocation(test[i]));
      if (distanceAways < closestMatch) {
        closestMatch = distanceAways;
        closestIndex = i;
      }

    }
    return closestIndex;
  }


  private Location toLocation(Coordinate match) {
    return new Location(match.x, match.y);
  }

  Coordinate[] toLineString(FeatureData fd) {
    return fd.getFeatureGeometry().getAsCoordinateArray();
  }

  Coordinate[] toLineString(TrafficPath tp) {
    ArrayList<Coordinate> list = new ArrayList<>();

    for (Location l : tp.getLocations()) {
      list.add(new Coordinate(l.getLat(), l.getLon()));
    }
    return list.toArray(new Coordinate[list.size()]);
  }

  Coordinate[] toLineString(StopPath sp) {
    ArrayList<Coordinate> list = new ArrayList<>();

    for (Location l : sp.getLocations()) {
      list.add(new Coordinate(l.getLat(), l.getLon()));
    }
    return list.toArray(new Coordinate[list.size()]);
  }

  Coordinate[] toLineString(double[] trafficSensorLats, double[] trafficSensorLons) {
    ArrayList<Coordinate> list = new ArrayList<>();
    if (trafficSensorLats == null || trafficSensorLons == null) return null;
    if (trafficSensorLats.length != trafficSensorLons.length)
      throw new IllegalStateException("unequal length inputs");

    for (int i = 0; i<trafficSensorLats.length; i++) {
      list.add(new Coordinate(trafficSensorLats[i], trafficSensorLons[i]));
    }
    return list.toArray(new Coordinate[list.size()]);
  }

  /**
   * Converts the input stream into a JSON string. Useful for when processing
   * a JSON feed.
   *
   * @param in
   * @return the JSON string
   * @throws IOException
   * @throws JSONException
   */
  protected String getJsonString(InputStream in) throws IOException,
          JSONException {
    return JsonUtils.getJsonString(in);
  }



  private List<TrafficPath> createTrafficPaths(List<FeatureData> featureDataList) {
    List<TrafficPath> trafficPaths = new ArrayList<>();
    for (FeatureData fd : featureDataList) {
      TrafficPath path = createTrafficPath(fd);
      trafficPaths.add(path);
      createTrafficSensor(fd, path);
    }
    return trafficPaths;
  }

  private TrafficPath createTrafficPath(FeatureData fd) {
    logger.info("creating TrafficPath({}, {}) for {}", fd.getId(),
            getTrafficRev(),
            fd.getLabel());
    // current convention of using natural keys (not a great convention)
    TrafficPath tp = new TrafficPath(fd.getId(),
            getTrafficRev(),
            fd.getLength()
    );

    tp.setLocations(new ArrayList());

    for (Coordinate c :fd.getFeatureGeometry().getAsCoordinateArray()) {
      tp.getLocations().add(new Location(c.x, c.y));
    }

    for (StopPath sp : fd.getStopPaths()) {
      joinStopPathToTrafficPath(tp, sp);
    }

    return tp;
  }


  private void joinStopPathToTrafficPath(TrafficPath tp, StopPath sp) {
    String hash = hash(tp, sp);
    if (!hashOfJoins.contains(hash)) {
      debugJoin(tp, sp);
      hashOfJoins.add(hash);
  }
    logger.info("joining {}:{} to {}:{}:{}",
            tp.getTrafficPathId(), tp.getTrafficRev(),
            sp.getTripPatternId(),
            sp.getId(),
            sp.getConfigRev());
    tp.getStopPaths().add(sp);
  }

  private String hash(TrafficPath tp, StopPath sp) {
    return tp.getTrafficPathId() + "."
            + tp.getTrafficRev() + "."
            + sp.getId() + "."
            + sp.getConfigRev();
  }

  private TrafficSensor createTrafficSensor(FeatureData featureData, TrafficPath trafficPath) {
    logger.info("creating TrafficSensor({}, {}) for {}",
            featureData.getId(),
            getTrafficRev(),
            featureData.getLabel());
    TrafficSensor sensor = new TrafficSensor(featureData.getId(),
            getTrafficRev(),
            featureData.getId(),
            trafficPath.getTrafficPathId());
    sensor.setDescription(featureData.getLabel());
    sensors.add(sensor);
    return sensor;
  }


  /**
   * lazy load (and increment) the trafficRev considering it may be null
   * @return
   */
  private Integer getTrafficRev() {
    if (trafficRev == null) {
      ActiveRevisions activeRevisions = ActiveRevisions.get(session);
      if (activeRevisions.getTrafficRev() == null) {
        // first use, set it to 0
        trafficRev = 0;
      } else {
        trafficRev = activeRevisions.getTrafficRev() + 1;
      }
      activeRevisions.setTrafficRev(trafficRev);
    }

    return trafficRev;
  }


  /**
   * entry point into Application.  Currently accepts no arguments
   * as all configuration happens via config file infrastructure.
   * @param args
   */
  public static void main(String[] args) {

    LoadTrafficSensors loader = new LoadTrafficSensors();
    loader.run();
    logger.info("LoadTrafficSensors exiting!");
    // daemon threads running in background, we need to
    // exist in a less graceful fashion
    System.exit(0);
  }


}
