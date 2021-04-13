package org.transitclock.avl;

import org.junit.Before;
import org.junit.Test;
import org.transitclock.ArrivalDepartureSupport;
import org.transitclock.StopSupport;
import org.transitclock.applications.Core;
import org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman.KalmanDataGenerator;
import org.transitclock.db.structs.ApcRecord;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Stop;
import org.transitclock.db.structs.apc.SimpleApcMessageUnmarshaller;
import org.transitclock.utils.Time;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.transitclock.TestSupport.getStreamAsString;
import static org.transitclock.TestSupport.toEpoch;

public class ApcMatcherTest {

  private KalmanDataGenerator dataGenerator;
  private SimpleApcMessageUnmarshaller unmarshaller = new SimpleApcMessageUnmarshaller();
  private ArrivalDepartureSupport arrivalDepartureSupport = new ArrivalDepartureSupport();
  private StopSupport stopSupport = new StopSupport();
  private ApcMatcher matcher;

  @Before
  public void setUp() throws Exception {
    matcher = new ApcMatcher();
    // our APC sample data occurred in the past
    long referenceTime = toEpoch("2021-02-18", null);
    dataGenerator = new KalmanDataGenerator(referenceTime);

    // some structs require Core / DbConfig
    if (!Core.isCoreApplication()) {
      Core.createTestCore(dataGenerator.AGENCY_ID);
      if (Core.getInstance().getTime() == null) {
        Core.getInstance().setTime(new Time(dataGenerator.getTimeZone()));
      }
    }

    InputStream stopsStream = this.getClass().getResourceAsStream("stops1.txt");
    assertNotNull(stopsStream);
    // ArrivalDeparture.getStop() requires this map be populated!!!
    Map<String, Stop> stopsMap = toStopsMap(getStreamAsString(stopsStream));
    Core.getInstance().getDbConfig().setStopsMap(stopsMap);


  }

  @Test
  public void match() throws Exception {
    List<ArrivalDeparture> arrivalDepartureList = loadArrivalDepartureList("arrivalDepartures3.csv");
    List<ApcRecord> records = loadApcRecords("apcMessages3.json");
    List<ApcMatch> matches = matcher.match(arrivalDepartureList, records);
    assertNotNull(matches);
    assertEquals(10, matches.size());

    for (ApcMatch match : matches) {
      ApcRecord apc = match.getApc();
      assertNotNull(apc);
      ArrivalDeparture ad = match.getArrivalDeparture();
      if (ad == null) {
        System.out.println("no match for ad " + apc);
        // we successfully matched all example data to historical ArrivalDeparture data!
        assertNotNull(ad);
      }
      assertEquals(apc.getVehicleId(), ad.getVehicleId());
    }

  }

  private List<ApcRecord> loadApcRecords(String s) throws Exception {
    InputStream is1 = this.getClass().getResourceAsStream(s);
    // apc data in UTC TZ
    return unmarshaller.toApcRecord(getStreamAsString(is1), "CST","UTC");

  }

  private List<ArrivalDeparture> loadArrivalDepartureList(String s) throws Exception {
    InputStream is1 = this.getClass().getResourceAsStream(s);
    // ArrivalDeparture data in CST TZ
    return toArrivalDepartures(getStreamAsString(is1), "CST");
  }

  private List<ArrivalDeparture> toArrivalDepartures(String csv, String tz) {
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

  private ArrivalDeparture toArrivalDeparture(String csv, String tz) {
    return  arrivalDepartureSupport.toArrivalDeparture(csv, tz);
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