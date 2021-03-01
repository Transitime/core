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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.transitclock.db.structs.Location;
import org.transitclock.db.structs.StopPath;
import org.transitclock.traffic.FeatureData;
import org.transitclock.traffic.FeatureGeometry;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test the loading/scoring of LoadTrafficSensors application.
 */
public class LoadTrafficSensorsTest {

  private LoadTrafficSensors app;

  @Before
  public void setup() {
    app = new LoadTrafficSensors() {
      void debugShape(String shapeName, Coordinate[] shape) {
        System.out.println(shapeName+":");
        StringBuffer sb = new StringBuffer();
        for (Coordinate c : shape) {
          sb.append(c.x).append(",").append(c.y).append('\n');
        }
        System.out.println(sb.toString());
      }

    };
  }

  @Test
  public void gapSizeLargeGap() {
    Coordinate[] featureLineString = parseShape(SENSOR_SHAPE_1);
    Coordinate[] stopPathLineString = parseShape(STOP_PATH_SHAPE_1);
    Coordinate[] matches = parseShape(MATCHES_1);
    int gapSize = app.countGapsInShape(featureLineString, stopPathLineString, matches);
    // 0, 0, 0, 1,...,2,7,8....,9,10 = gap of 5 (2-7)
    assertEquals(5, gapSize);
  }

  @Test
  public void gabSizeSmallBackTracks() {
    Coordinate[] featureLineString = parseShape(SENSOR_SHAPE_2);
    Coordinate[] stopPathLineString = parseShape(STOP_PATH_SHAPE_2);
    Coordinate[] matches = parseShape(MATCHES_2);
    int gapSize = app.countGapsInShape(featureLineString, stopPathLineString, matches);
    assertEquals(2, gapSize);
  }

  @Test
  // center line feature shape, more complex version of testBigFeatureSizeSmallStopSegment
  // short stop segment (4 points, 2 on center line, 2 off center line at stop
  // gapSize counts flapping / backmatches and penalizes this, make sure overall
  // score is still above DEFAULT_MIN_SNAP_SCORE
  public void TestFlappingScoreSizeMediumStopSegment() {
    Coordinate[] featureLineString = parseShape(SENSOR_SHAPE_3);
    Coordinate[] stopPathLineString = parseShape(STOP_PATH_SHAPE_3);
    Coordinate[] matches = parseShape(MATCHES_3);
    FeatureData fd = createFeatureData(" shape3", SENSOR_SHAPE_3);
    List<StopPath> allStopPaths = new ArrayList<>();
    allStopPaths.add(createStopPath("shape3", STOP_PATH_SHAPE_3));
    double score = app.mapFeatureToStopPaths(fd, allStopPaths);
    assertTrue(score > LoadTrafficSensors.DEFAULT_MIN_SNAP_SCORE);

    int gapSize = app.countGapsInShape(featureLineString, stopPathLineString, matches);
    assertEquals(3, gapSize);

  }

  @Test
  // a borderline bad match -- but by our algorithm its acceptable
  public void testFeatureLargeStopSegment() {
    Coordinate[] featureLineString = parseShape(SENSOR_SHAPE_4);
    Coordinate[] stopPathLineString = parseShape(STOP_PATH_SHAPE_4);
    Coordinate[] matches = parseShape(MATCHES_4);
    FeatureData fd = createFeatureData(" shape4", SENSOR_SHAPE_4);
    List<StopPath> allStopPaths = new ArrayList<>();
    allStopPaths.add(createStopPath("shape3", STOP_PATH_SHAPE_4));
    double score = app.mapFeatureToStopPaths(fd, allStopPaths);
    assertTrue(score < LoadTrafficSensors.DEFAULT_MIN_SNAP_SCORE);
    int gapSize = app.countGapsInShape(featureLineString, stopPathLineString, matches);
    assertEquals(2, gapSize);

    assertTrue(app.scoreOverlay(featureLineString, stopPathLineString, matches) > LoadTrafficSensors.DEFAULT_MIN_SNAP_SCORE);
  }

  @Test
  // a really bad match -- confirm by our algorithm its not acceptable
  // snapping to the end of the segment, but many points of a complex
  // shape not matched
  // sensor Walker St. to Pickett St via Duke St. Eastbound to
  // stop path 115_to_340
  // LARGE COMPLEX STOP SHAPES score well based on point count and shouldn't
  public void testFeatureLargerStopSegment() {
    Coordinate[] featureLineString = parseShape(SENSOR_SHAPE_5);
    Coordinate[] stopPathLineString = parseShape(STOP_PATH_SHAPE_5);
    Coordinate[] matches = parseShape(MATCHES_5);
    FeatureData fd = createFeatureData(" shape5", SENSOR_SHAPE_5);
    List<StopPath> allStopPaths = new ArrayList<>();
    allStopPaths.add(createStopPath("shape5", STOP_PATH_SHAPE_5));
    double score = app.mapFeatureToStopPaths(fd, allStopPaths);
    assertTrue(score < LoadTrafficSensors.DEFAULT_MIN_SNAP_SCORE);

    int gapSize = app.countGapsInShape(featureLineString, stopPathLineString, matches);
    assertEquals(2, gapSize);
    assertTrue(app.scoreOverlay(featureLineString, stopPathLineString, matches) > LoadTrafficSensors.DEFAULT_MIN_SNAP_SCORE);
  }

  /**
   * another example of above:
   * simple sensor, large complex stop segment
   * ensure socre is below acceptable min
   * 10:47:56.399 INFO  thread=main [o.t.a.LoadTrafficSensors:179] match score 0.3333333333333333 for sensor King St & Callahan Dr to King St & S West St to stop path 17_to_25
   *
   * 10:47:56.399 INFO  thread=main [o.t.a.LoadTrafficSensors:233]
   * http://developer.onebusaway.org/maps/debug.html?polyline=38.8072%2C-77.0625%2038.8071%2C-77.0624%2038.807%2C-77.0619%2038.8069%2C-77.0613%2038.8064%2C-77.0573%2038.806%2C-77.0546%20&points=38.805386%2C-77.068962%2038.80543%2C-77.06895%2038.80502%2C-77.06552%2038.805227%2C-77.065193%2038.80559%2C-77.06453%2038.80634%2C-77.06303%2038.80696%2C-77.06262%2038.8071%2C-77.06236%2038.80694%2C-77.0616%2038.806896%2C-77.061607%20
   */
  @Test
  public void testFeatureLargerStopSegment2() {
    Coordinate[] featureLineString = parseShape(SENSOR_SHAPE_6);
    Coordinate[] stopPathLineString = parseShape(STOP_PATH_SHAPE_6);
    Coordinate[] matches = parseShape(MATCHES_6);
    FeatureData fd = createFeatureData(" shape6", SENSOR_SHAPE_6);
    List<StopPath> allStopPaths = new ArrayList<>();
    allStopPaths.add(createStopPath("shape6", STOP_PATH_SHAPE_6));
    double score = app.mapFeatureToStopPaths(fd, allStopPaths);
    assertTrue(score < LoadTrafficSensors.DEFAULT_MIN_SNAP_SCORE);

    int gapSize = app.countGapsInShape(featureLineString, stopPathLineString, matches);
    assertEquals(1, gapSize);
  }

  @Test
  public void score() {
    Coordinate[] featureLineString = parseShape(SENSOR_SHAPE_1);
    Coordinate[] stopPathLineString = parseShape(STOP_PATH_SHAPE_1);
    Coordinate[] matches = parseShape(MATCHES_1);

    FeatureData fd = createFeatureData(" shape1", SENSOR_SHAPE_1);
    List<StopPath> allStopPaths = new ArrayList<>();
    allStopPaths.add(createStopPath("shape1", STOP_PATH_SHAPE_1));

    double score = app.scoreOverlay(featureLineString, stopPathLineString, matches);
    assertEquals(-5.0, score, 0.01);

    score = app.mapFeatureToStopPaths(fd, allStopPaths);
    assertTrue(score < LoadTrafficSensors.DEFAULT_MIN_SNAP_SCORE);
  }

  private Coordinate[] parseShape(String csvShape) {
    return toLineString(csvShape);
  }

  @Test
  public void testNoMatchMapFeatureToStopPaths() {
    FeatureData fd = createFeatureData(" S. Van Dorn St & Edsall Rd. to N. Van Dorn St & W. Braddock Rd", SENSOR_SHAPE_1);
    List<StopPath> allStopPaths = new ArrayList<>();
    allStopPaths.add(createStopPath("shape1", STOP_PATH_SHAPE_1));
    app.mapFeatureToStopPaths(fd, allStopPaths);
    List<StopPath> results = fd.getStopPaths();
    // this is one of the more interesting shapes.  To see it
    // uncomment this statement
    //debugMatch(STOP_PATH_SHAPE_1, SENSOR_SHAPE_1, MATCHES_1);
    assertNotNull(results);
    // it didn't match!  No results
    assertEquals(0, results.size());
  }

  @Test
  @Ignore
  public void testLengthOfShape() throws Exception {
    List<FeatureData> featureDataList = app.loadFeatureDataFromURL(app.getTrafficUrl());
    FeatureData fd = findFeature(featureDataList, "938");
    assertEquals(fd.getLength(), app.calculateLengthInMeters(fd.getFeatureGeometry().getAsCoordinateArray()), 0.001);
  }

  private FeatureData findFeature(List<FeatureData> featureDataList, String s) {
    for (FeatureData fd : featureDataList) {
      if (fd.getId().equals(s))
        return fd;
    }
    return null;
  }

  private void debugMatch(String stopPathShape1, String sensorShape1, String matches1) {
    app.debugMatch(toLineString(sensorShape1), toLineString(stopPathShape1), toLineString(matches1));

  }

  /**
   * Adjust snapping tolerance - settle for a high match that
   * is meant to be a perfect match
   * http://developer.onebusaway.org/maps/debug.html?polyline=38.8009%2C-77.0673%2038.802%2C-77.0675%2038.8023%2C-77.0675%2038.8024%2C-77.0675%2038.8024%2C-77.0674%2038.8024%2C-77.0672%2038.8026%2C-77.0669%2038.8028%2C-77.0665%2038.8032%2C-77.0658%2038.8034%2C-77.0655%2038.8035%2C-77.0653%2038.8036%2C-77.065%2038.8037%2C-77.0646%2038.8037%2C-77.0642%2038.8036%2C-77.0636%2038.8036%2C-77.0634%2038.8042%2C-77.0634%2038.8048%2C-77.0633%20&points=38.80357%2C-77.063774%2038.80363%2C-77.06376%2038.803585%2C-77.063448%2038.804244%2C-77.063389%2038.804371%2C-77.063303%20
   */
  @Test
  public void testMatchingRadius() {
    FeatureData fd = createFeatureData("f\n" +
            "or sensor Eisenhower Ave @ Mill Rd (East of Telegraph) to Duke St & Dulany", SENSOR_SHAPE_7);
    List<StopPath> allStopPaths = new ArrayList<>();
    allStopPaths.add(createStopPath("shape1", STOP_PATH_SHAPE_7));
    assertEquals(0.8, app.mapFeatureToStopPaths(fd, allStopPaths), 0.001);
  }


  /**
   * Another radius snapping test, with some centerline data that isn't perfect
   * http://developer.onebusaway.org/maps/debug.html?polyline=38.8068%2C-77.0759%2038.8068%2C-77.0767%2038.8069%2C-77.0773%2038.8071%2C-77.0786%2038.8073%2C-77.0801%2038.8075%2C-77.0812%2038.8074%2C-77.0815%2038.8075%2C-77.0817%2038.8075%2C-77.082%2038.8076%2C-77.0827%2038.8078%2C-77.0838%2038.808%2C-77.0852%2038.8081%2C-77.0862%2038.8082%2C-77.0869%2038.8083%2C-77.0872%2038.8083%2C-77.0873%2038.8084%2C-77.0876%2038.8084%2C-77.088%2038.8085%2C-77.0887%20&points=38.806889%2C-77.076523%2038.80682%2C-77.07653%2038.80745%2C-77.08118%2038.80745%2C-77.08168%2038.807552%2C-77.081673%20
   */
  @Test
  public void testMatchingRadius2() {
    FeatureData fd = createFeatureData("f\n" +
            " Taylor Run Pkwy. to Quaker Ln.  via Duke St. Westbound", SENSOR_SHAPE_8);
    List<StopPath> allStopPaths = new ArrayList<>();
    allStopPaths.add(createStopPath("shape1", STOP_PATH_SHAPE_8)); //2 4_to_32
    assertEquals(0.6, app.mapFeatureToStopPaths(fd, allStopPaths), 0.001);
  }

  /**
   * Another radius snapping test, this one that if radius is too large it will match to the wrong lane
   * sensor Taylor Run Pkwy to Patrick St via Duke St. Eastbound to stop path 6_to_7
   *
   * http://developer.onebusaway.org/maps/debug.html?polyline=38.8067%2C-77.0761%2038.8065%2C-77.0747%2038.8064%2C-77.0739%2038.8062%2C-77.073%2038.8062%2C-77.0728%2038.8059%2C-77.0715%2038.8057%2C-77.0703%2038.8055%2C-77.0694%2038.8053%2C-77.0675%2038.805%2C-77.0655%2038.8048%2C-77.0639%2038.8046%2C-77.0624%2038.8045%2C-77.0613%2038.8043%2C-77.06%2038.8041%2C-77.0584%2038.804%2C-77.0573%2038.8039%2C-77.0568%2038.8039%2C-77.0568%2038.8039%2C-77.0567%2038.8039%2C-77.0564%2038.8038%2C-77.0556%2038.8038%2C-77.0553%2038.8037%2C-77.0551%2038.8035%2C-77.054%2038.8034%2C-77.0529%2038.8034%2C-77.0526%2038.8033%2C-77.0522%2038.8033%2C-77.0519%2038.8033%2C-77.0517%2038.8032%2C-77.0508%20&points=38.803116%2C-77.04953%2038.80304%2C-77.04954%2038.80332%2C-77.05174%2038.80339%2C-77.05198%2038.80343%2C-77.05244%2038.803493%2C-77.052436%20
   */
  @Test
  public void testMatchingRadius3() {
    FeatureData fd = createFeatureData("f\n" +
            "Taylor Run Pkwy to Patrick St via Duke St. Eastbound", SENSOR_SHAPE_9);
    List<StopPath> allStopPaths = new ArrayList<>();
    allStopPaths.add(createStopPath("shape1", STOP_PATH_SHAPE_9)); // 6_to_7
    assertTrue(app.mapFeatureToStopPaths(fd, allStopPaths) < LoadTrafficSensors.DEFAULT_MIN_SNAP_SCORE);
  }

  @Test
  public void testMatchMapFeatureToStopPaths() {
    FeatureData fd = createFeatureData("sensor2", SENSOR_SHAPE_2);
    List<StopPath> allStopPaths = new ArrayList<>();
    allStopPaths.add(createStopPath("shape2", STOP_PATH_SHAPE_2));
    app.mapFeatureToStopPaths(fd, allStopPaths);
    List<StopPath> results = fd.getStopPaths();
    assertNotNull(results);
    // we matched, expecting a result
    assertEquals(1, results.size());
    Coordinate[] matches = app.toLineString(results.get(0));
    app.debugShape("test match", matches);
  }


  private StopPath createStopPath(String shapeName, String stopPathShape) {

    StopPath sp = new StopPath(-1,
    shapeName,
    "stopId",
    -1,
    false,
    "routeId",
    false,
    false,
    false,
    -1,
    1.0,
    2.0);
    sp.setLocations(new ArrayList<>());
    for (Coordinate c : toLineString(stopPathShape)) {
      sp.getLocations().add(new Location(c.x, c.y));
    }
    return sp;
  }

  private FeatureData createFeatureData(String s, String sensorShape) {
    FeatureData fd = new FeatureData();
    FeatureGeometry fg = new FeatureGeometry();
    fd.setFeatureGeometry(fg);
    for (Coordinate c : toLineString(sensorShape)) {
      fg.addLatLon(c.x, c.y);
    }

    return fd;
  }

  private Coordinate[] toLineString(String csvCoordinates) {
    List<Coordinate> lineString = new ArrayList<>();
    String[] lines = csvCoordinates.split("\n");
    for (String line : lines) {
      String[] latLonStr = line.split(",");
      lineString.add(new Coordinate(Double.parseDouble(latLonStr[0]), Double.parseDouble(latLonStr[1])));
    }
    return lineString.toArray(new Coordinate[lineString.size()]);
  }

  private static final String SENSOR_SHAPE_1 =
          "38.8076,-77.1331\n" +
                  "38.8107,-77.132\n" +
                  "38.8111,-77.1319\n" +
                  "38.8115,-77.1317\n" +
                  "38.8118,-77.1315\n" +
                  "38.8122,-77.1312\n" +
                  "38.8126,-77.1309\n" +
                  "38.8131,-77.1305\n" +
                  "38.8139,-77.1297\n" +
                  "38.8153,-77.1284\n" +
                  "38.8155,-77.1283\n" +
                  "38.8157,-77.1283\n" +
                  "38.816,-77.1282\n" +
                  "38.8162,-77.1281\n" +
                  "38.8164,-77.1281\n" +
                  "38.8167,-77.1282\n" +
                  "38.817,-77.1283\n" +
                  "38.8174,-77.1285\n" +
                  "38.8176,-77.1285\n" +
                  "38.8178,-77.1286\n" +
                  "38.818,-77.1286\n" +
                  "38.8183,-77.1286\n" +
                  "38.8185,-77.1285\n" +
                  "38.8187,-77.1285\n" +
                  "38.819,-77.1283\n" +
                  "38.8191,-77.1283\n" +
                  "38.8192,-77.1283\n" +
                  "38.8192,-77.1283\n" +
                  "38.8195,-77.1281\n" +
                  "38.82,-77.1279\n" +
                  "38.8205,-77.1276\n" +
                  "38.8209,-77.1274\n" +
                  "38.821,-77.1272\n" +
                  "38.8217,-77.1264\n" +
                  "38.8222,-77.1257\n" +
                  "38.8226,-77.1252\n" +
                  "38.8228,-77.125\n" +
                  "38.8237,-77.1237\n" +
                  "38.8244,-77.1226\n" +
                  "38.8252,-77.1214\n" +
                  "38.826,-77.1201\n" +
                  "38.8266,-77.119\n" +
                  "38.827,-77.1182\n" +
                  "38.8279,-77.1164\n" +
                  "38.8287,-77.1145\n" +
                  "38.8295,-77.1128\n" +
                  "38.8297,-77.1123\n" +
                  "38.8299,-77.1116\n" +
                  "38.8299,-77.1114\n" +
                  "38.8301,-77.111\n" +
                  "38.8301,-77.1109\n" +
                  "38.8301,-77.1109\n" +
                  "38.8302,-77.1104\n" +
                  "38.8304,-77.1099\n" +
                  "38.8305,-77.1097\n" +
                  "38.8305,-77.1094\n" +
                  "38.8306,-77.1091\n" +
                  "38.8308,-77.1087\n" +
                  "38.8309,-77.1084\n" +
                  "38.831,-77.1084\n" +
                  "38.8313,-77.1077\n" +
                  "38.8314,-77.1074\n";
  private static final String SENSOR_SHAPE_2 =
          "38.8067,-77.0761\n" +
                  "38.8065,-77.0747\n" +
                  "38.8064,-77.0739\n" +
                  "38.8062,-77.073\n" +
                  "38.8062,-77.0728\n" +
                  "38.8059,-77.0715\n" +
                  "38.8057,-77.0703\n" +
                  "38.8055,-77.0694\n" +
                  "38.8053,-77.0675\n" +
                  "38.805,-77.0655\n" +
                  "38.8048,-77.0639\n" +
                  "38.8046,-77.0624\n" +
                  "38.8045,-77.0613\n" +
                  "38.8043,-77.06\n" +
                  "38.8041,-77.0584\n" +
                  "38.804,-77.0573\n" +
                  "38.8039,-77.0568\n" +
                  "38.8039,-77.0568\n" +
                  "38.8039,-77.0567\n" +
                  "38.8039,-77.0564\n" +
                  "38.8038,-77.0556\n" +
                  "38.8038,-77.0553\n" +
                  "38.8037,-77.0551\n" +
                  "38.8035,-77.054\n" +
                  "38.8034,-77.0529\n" +
                  "38.8034,-77.0526\n" +
                  "38.8033,-77.0522\n" +
                  "38.8033,-77.0519\n" +
                  "38.8033,-77.0517\n" +
                  "38.8032,-77.0508";
  private static final String SENSOR_SHAPE_3 =
          "38.814,-77.0716\n" +
                  "38.8139,-77.0718\n" +
                  "38.8138,-77.0731\n" +
                  "38.8138,-77.0736\n" +
                  "38.8137,-77.0741\n" +
                  "38.8138,-77.0744\n" +
                  "38.8138,-77.0748\n" +
                  "38.814,-77.0756\n" +
                  "38.8141,-77.0766\n" +
                  "38.8141,-77.0768\n" +
                  "38.8142,-77.077\n" +
                  "38.8142,-77.0783\n" +
                  "38.8142,-77.0792\n" +
                  "38.8142,-77.0802\n" +
                  "38.8142,-77.0803\n" +
                  "38.8142,-77.0806\n" +
                  "38.8142,-77.0812\n" +
                  "38.8143,-77.0815\n" +
                  "38.8145,-77.0823\n" +
                  "38.8147,-77.0826\n" +
                  "38.8149,-77.0831\n" +
                  "38.8151,-77.0835\n" +
                  "38.8153,-77.0838\n" +
                  "38.8155,-77.0842\n" +
                  "38.816,-77.0856\n" +
                  "38.8167,-77.0872\n" +
                  "38.8173,-77.0889\n" +
                  "38.8173,-77.0891\n" +
                  "38.8174,-77.0895";
  private static final String SENSOR_SHAPE_4 =
          "38.8141,-77.1339\n" +
                  "38.8141,-77.1333\n" +
                  "38.8141,-77.1325\n" +
                  "38.8142,-77.1312\n" +
                  "38.8142,-77.1309\n" +
                  "38.8142,-77.1304\n" +
                  "38.8142,-77.1298\n" +
                  "38.8142,-77.129\n" +
                  "38.8141,-77.1283\n" +
                  "38.8139,-77.1276\n" +
                  "38.8137,-77.1274\n" +
                  "38.8132,-77.1261\n" +
                  "38.813,-77.1257\n" +
                  "38.8128,-77.1252\n" +
                  "38.8127,-77.1246\n" +
                  "38.8126,-77.1239\n" +
                  "38.8123,-77.122\n" +
                  "38.8123,-77.1217";
  private static final String SENSOR_SHAPE_5 =
          "38.8141,-77.1339\n" +
                  "38.8141,-77.1333\n" +
                  "38.8141,-77.1325\n" +
                  "38.8142,-77.1312\n" +
                  "38.8142,-77.1309\n" +
                  "38.8142,-77.1304\n" +
                  "38.8142,-77.1298\n" +
                  "38.8142,-77.129\n" +
                  "38.8141,-77.1283\n" +
                  "38.8139,-77.1276\n" +
                  "38.8137,-77.1274\n" +
                  "38.8132,-77.1261\n" +
                  "38.813,-77.1257\n" +
                  "38.8128,-77.1252\n" +
                  "38.8127,-77.1246\n" +
                  "38.8126,-77.1239\n" +
                  "38.8123,-77.122\n" +
                  "38.8123,-77.1217";
  private static final String SENSOR_SHAPE_6 =
          "38.8072,-77.0625\n" +
                  "38.8071,-77.0624\n" +
                  "38.807,-77.0619\n" +
                  "38.8069,-77.0613\n" +
                  "38.8064,-77.0573\n" +
                  "38.806,-77.0546";
  private static final String SENSOR_SHAPE_7 =
          "38.8009,-77.0673\n" +
                  "38.802,-77.0675\n" +
                  "38.8023,-77.0675\n" +
                  "38.8024,-77.0675\n" +
                  "38.8024,-77.0674\n" +
                  "38.8024,-77.0672\n" +
                  "38.8026,-77.0669\n" +
                  "38.8028,-77.0665\n" +
                  "38.8032,-77.0658\n" +
                  "38.8034,-77.0655\n" +
                  "38.8035,-77.0653\n" +
                  "38.8036,-77.065\n" +
                  "38.8037,-77.0646\n" +
                  "38.8037,-77.0642\n" +
                  "38.8036,-77.0636\n" +
                  "38.8036,-77.0634\n" +
                  "38.8042,-77.0634\n" +
                  "38.8048,-77.0633";
  private static final String SENSOR_SHAPE_8 =
          "38.8068,-77.0759\n" +
                  "38.8068,-77.0767\n" +
                  "38.8069,-77.0773\n" +
                  "38.8071,-77.0786\n" +
                  "38.8073,-77.0801\n" +
                  "38.8075,-77.0812\n" +
                  "38.8074,-77.0815\n" +
                  "38.8075,-77.0817\n" +
                  "38.8075,-77.082\n" +
                  "38.8076,-77.0827\n" +
                  "38.8078,-77.0838\n" +
                  "38.808,-77.0852\n" +
                  "38.8081,-77.0862\n" +
                  "38.8082,-77.0869\n" +
                  "38.8083,-77.0872\n" +
                  "38.8083,-77.0873\n" +
                  "38.8084,-77.0876\n" +
                  "38.8084,-77.088\n" +
                  "38.8085,-77.0887\n";
  private static final String SENSOR_SHAPE_9 =
          "38.8067,-77.0761\n" +
                  "38.8065,-77.0747\n" +
                  "38.8064,-77.0739\n" +
                  "38.8062,-77.073\n" +
                  "38.8062,-77.0728\n" +
                  "38.8059,-77.0715\n" +
                  "38.8057,-77.0703\n" +
                  "38.8055,-77.0694\n" +
                  "38.8053,-77.0675\n" +
                  "38.805,-77.0655\n" +
                  "38.8048,-77.0639\n" +
                  "38.8046,-77.0624\n" +
                  "38.8045,-77.0613\n" +
                  "38.8043,-77.06\n" +
                  "38.8041,-77.0584\n" +
                  "38.804,-77.0573\n" +
                  "38.8039,-77.0568\n" +
                  "38.8039,-77.0568\n" +
                  "38.8039,-77.0567\n" +
                  "38.8039,-77.0564\n" +
                  "38.8038,-77.0556\n" +
                  "38.8038,-77.0553\n" +
                  "38.8037,-77.0551\n" +
                  "38.8035,-77.054\n" +
                  "38.8034,-77.0529\n" +
                  "38.8034,-77.0526\n" +
                  "38.8033,-77.0522\n" +
                  "38.8033,-77.0519\n" +
                  "38.8033,-77.0517\n" +
                  "38.8032,-77.0508";
  private static final String STOP_PATH_SHAPE_1 =
          "38.812252,-77.131073\n" +
                  "38.81228,-77.13114\n" +
                  "38.81316,-77.13039\n" +
                  "38.81362,-77.13105\n" +
                  "38.81386,-77.13108\n" +
                  "38.81406,-77.1309\n" +
                  "38.81421,-77.13061\n" +
                  "38.81422,-77.12945\n" +
                  "38.81413,-77.1287\n" +
                  "38.81385,-77.12765\n" +
                  "38.81324,-77.12627\n" +
                  "38.813187,-77.126305\n";
  private static final String STOP_PATH_SHAPE_2 =
          "38.80352,-77.054298\n" +
                  "38.80356,-77.05429\n" +
                  "38.80332,-77.05216\n" +
                  "38.803212,-77.050955\n" +
                  "38.80309,-77.04989\n" +
                  "38.803051,-77.049896";
  private static final String STOP_PATH_SHAPE_3 =
          "38.813942,-77.075043\n" +
                  "38.81387,-77.07506\n" +
                  "38.81415,-77.07693\n" +
                  "38.814194,-77.076904";
  private static final String STOP_PATH_SHAPE_4 =
          "38.814014,-77.133179\n" +
                  "38.8141,-77.13318\n" +
                  "38.81408,-77.1325\n" +
                  "38.81404,-77.13241\n" +
                  "38.81403,-77.13154\n" +
                  "38.81384,-77.1313\n" +
                  "38.81344,-77.13103\n" +
                  "38.81328,-77.13076\n" +
                  "38.81298,-77.13075\n" +
                  "38.8123,-77.13136\n" +
                  "38.812359,-77.131416";
  private static final String STOP_PATH_SHAPE_5 =
          "38.814014,-77.133179\n" +
                  "38.8141,-77.13318\n" +
                  "38.81408,-77.1325\n" +
                  "38.81404,-77.13241\n" +
                  "38.81403,-77.13154\n" +
                  "38.81384,-77.1313\n" +
                  "38.81344,-77.13103\n" +
                  "38.81309,-77.13045\n" +
                  "38.815261,-77.12853\n" +
                  "38.815424,-77.128426\n" +
                  "38.815636,-77.128721\n" +
                  "38.815846,-77.128813\n" +
                  "38.816217,-77.128653\n" +
                  "38.817843,-77.12934\n" +
                  "38.818376,-77.129372\n" +
                  "38.818804,-77.12956\n" +
                  "38.818867,-77.12964\n" +
                  "38.81888,-77.129806\n" +
                  "38.81872,-77.130213\n" +
                  "38.817751,-77.132086\n" +
                  "38.817283,-77.131855\n" +
                  "38.817145,-77.131459\n" +
                  "38.817223,-77.131126";
  private static final String STOP_PATH_SHAPE_6 =
          "38.805386,-77.068962\n" +
                  "38.80543,-77.06895\n" +
                  "38.80502,-77.06552\n" +
                  "38.805227,-77.065193\n" +
                  "38.80559,-77.06453\n" +
                  "38.80634,-77.06303\n" +
                  "38.80696,-77.06262\n" +
                  "38.8071,-77.06236\n" +
                  "38.80694,-77.0616\n" +
                  "38.806896,-77.061607";
  private static final String STOP_PATH_SHAPE_7 =
  "38.80357,-77.063774\n" +
          "38.80363,-77.06376\n" +
          "38.803585,-77.063448\n" +
          "38.804244,-77.063389\n" +
          "38.804371,-77.063303";
  private static final String STOP_PATH_SHAPE_8 =
          "38.806889,-77.076523\n" +
                  "38.80682,-77.07653\n" +
                  "38.80745,-77.08118\n" +
                  "38.80745,-77.08168\n" +
                  "38.807552,-77.081673\n";
  private static final String STOP_PATH_SHAPE_9 =
          "38.803116,-77.04953\n" +
                  "38.80304,-77.04954\n" +
                  "38.80332,-77.05174\n" +
                  "38.80339,-77.05198\n" +
                  "38.80343,-77.05244\n" +
                  "38.803493,-77.052436";
  private static final String MATCHES_1 =
          "38.8076,-77.1331\n" +
                  "38.8107,-77.132\n" +
                  "38.8111,-77.1319\n" +
                  "38.8115,-77.1317\n" +
                  "38.8118,-77.1315\n" +
                  "38.8122,-77.1312\n" +
                  "38.81228,-77.13114\n" +
                  "38.8126,-77.1309\n" +
                  "38.8131,-77.1305\n" +
                  "38.8139,-77.1297\n" +
                  "38.8153,-77.1284\n" +
                  "38.8155,-77.1283\n" +
                  "38.8157,-77.1283\n" +
                  "38.816,-77.1282\n" +
                  "38.8162,-77.1281\n" +
                  "38.8164,-77.1281\n" +
                  "38.8167,-77.1282\n" +
                  "38.817,-77.1283\n" +
                  "38.8174,-77.1285\n" +
                  "38.8176,-77.1285\n" +
                  "38.8178,-77.1286\n" +
                  "38.818,-77.1286\n" +
                  "38.8183,-77.1286\n" +
                  "38.8185,-77.1285\n" +
                  "38.8187,-77.1285\n" +
                  "38.819,-77.1283\n" +
                  "38.8191,-77.1283\n" +
                  "38.8192,-77.1283\n" +
                  "38.8192,-77.1283\n" +
                  "38.8195,-77.1281\n" +
                  "38.82,-77.1279\n" +
                  "38.8205,-77.1276\n" +
                  "38.8209,-77.1274\n" +
                  "38.821,-77.1272\n" +
                  "38.8217,-77.1264\n" +
                  "38.8222,-77.1257\n" +
                  "38.8226,-77.1252\n" +
                  "38.8228,-77.125\n" +
                  "38.8237,-77.1237\n" +
                  "38.8244,-77.1226\n" +
                  "38.8252,-77.1214\n" +
                  "38.826,-77.1201\n" +
                  "38.8266,-77.119\n" +
                  "38.827,-77.1182\n" +
                  "38.8279,-77.1164\n" +
                  "38.8287,-77.1145\n" +
                  "38.8295,-77.1128\n" +
                  "38.8297,-77.1123\n" +
                  "38.8299,-77.1116\n" +
                  "38.8299,-77.1114\n" +
                  "38.8301,-77.111\n" +
                  "38.8301,-77.1109\n" +
                  "38.8301,-77.1109\n" +
                  "38.8302,-77.1104\n" +
                  "38.8304,-77.1099\n" +
                  "38.8305,-77.1097\n" +
                  "38.8305,-77.1094\n" +
                  "38.8306,-77.1091\n" +
                  "38.8308,-77.1087\n" +
                  "38.8309,-77.1084\n" +
                  "38.831,-77.1084\n" +
                  "38.8313,-77.1077\n" +
                  "38.8314,-77.1074";
  private static final String MATCHES_2 =
          "38.8067,-77.0761\n" +
                  "38.8065,-77.0747\n" +
                  "38.8064,-77.0739\n" +
                  "38.8062,-77.073\n" +
                  "38.8062,-77.0728\n" +
                  "38.8059,-77.0715\n" +
                  "38.8057,-77.0703\n" +
                  "38.8055,-77.0694\n" +
                  "38.8053,-77.0675\n" +
                  "38.805,-77.0655\n" +
                  "38.8048,-77.0639\n" +
                  "38.8046,-77.0624\n" +
                  "38.8045,-77.0613\n" +
                  "38.8043,-77.06\n" +
                  "38.8041,-77.0584\n" +
                  "38.804,-77.0573\n" +
                  "38.8039,-77.0568\n" +
                  "38.8039,-77.0568\n" +
                  "38.8039,-77.0567\n" +
                  "38.8039,-77.0564\n" +
                  "38.8038,-77.0556\n" +
                  "38.8038,-77.0553\n" +
                  "38.8037,-77.0551\n" +
                  "38.80356,-77.05429\n" +
                  "38.8035,-77.054\n" +
                  "38.8034,-77.0529\n" +
                  "38.8034,-77.0526\n" +
                  "38.8033,-77.0522\n" +
                  "38.8033,-77.0519\n" +
                  "38.8033,-77.0517\n" +
                  "38.803212,-77.050955\n" +
                  "38.8032,-77.0508";
  private static final String MATCHES_3 =
                  "38.814,-77.0716\n" +
                          "38.8139,-77.0718\n" +
                          "38.8138,-77.0731\n" +
                          "38.8138,-77.0736\n" +
                          "38.8137,-77.0741\n" +
                          "38.8138,-77.0744\n" +
                          "38.8138,-77.0748\n" +
                          "38.81387,-77.07506\n" +
                          "38.814,-77.0756\n" +
                          "38.8141,-77.0766\n" +
                          "38.8141,-77.0768\n" +
                          "38.8142,-77.077\n" +
                          "38.8142,-77.0783\n" +
                          "38.8142,-77.0792\n" +
                          "38.8142,-77.0802\n" +
                          "38.8142,-77.0803\n" +
                          "38.8142,-77.0806\n" +
                          "38.8142,-77.0812\n" +
                          "38.8143,-77.0815\n" +
                          "38.8145,-77.0823\n" +
                          "38.8147,-77.0826\n" +
                          "38.8149,-77.0831\n" +
                          "38.8151,-77.0835\n" +
                          "38.8153,-77.0838\n" +
                          "38.8155,-77.0842\n" +
                          "38.816,-77.0856\n" +
                          "38.8167,-77.0872\n" +
                          "38.8173,-77.0889\n" +
                          "38.8173,-77.0891\n" +
                          "38.8174,-77.0895";

  private static final String MATCHES_4 =
          "38.8141,-77.1339\n" +
                  "38.8141,-77.1333\n" +
                  "38.8141,-77.13318\n" +
                  "38.8141,-77.1325\n" +
                  "38.8142,-77.1312\n" +
                  "38.8142,-77.1309\n" +
                  "38.8142,-77.1304\n" +
                  "38.8142,-77.1298\n" +
                  "38.8142,-77.129\n" +
                  "38.8141,-77.1283\n" +
                  "38.8139,-77.1276\n" +
                  "38.8137,-77.1274\n" +
                  "38.8132,-77.1261\n" +
                  "38.813,-77.1257\n" +
                  "38.8128,-77.1252\n" +
                  "38.8127,-77.1246\n" +
                  "38.8126,-77.1239\n" +
                  "38.8123,-77.122\n" +
                  "38.8123,-77.1217";
  private static final String MATCHES_5 =
          "38.8141,-77.1339\n" +
                  "38.8141,-77.1333\n" +
                  "38.8141,-77.13318\n" +
                  "38.8141,-77.1325\n" +
                  "38.8142,-77.1312\n" +
                  "38.8142,-77.1309\n" +
                  "38.8142,-77.1304\n" +
                  "38.8142,-77.1298\n" +
                  "38.8142,-77.129\n" +
                  "38.8141,-77.1283\n" +
                  "38.8139,-77.1276\n" +
                  "38.8137,-77.1274\n" +
                  "38.8132,-77.1261\n" +
                  "38.813,-77.1257\n" +
                  "38.8128,-77.1252\n" +
                  "38.8127,-77.1246\n" +
                  "38.8126,-77.1239\n" +
                  "38.8123,-77.122\n" +
                  "38.8123,-77.1217";
  private static final String MATCHES_6 =
          "38.8071,-77.06236\n" +
          "38.80694,-77.0616";
}