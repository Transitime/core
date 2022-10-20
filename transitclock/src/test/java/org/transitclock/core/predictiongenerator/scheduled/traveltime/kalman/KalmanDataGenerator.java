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

import org.transitclock.SingletonSupport;
import org.transitclock.avl.ApcParsedRecord;
import org.transitclock.core.Indices;
import org.transitclock.core.SpatialMatch;
import org.transitclock.core.TemporalDifference;
import org.transitclock.core.TemporalMatch;
import org.transitclock.core.TravelTimeDetails;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.KalmanError;
import org.transitclock.db.structs.ApcReport;
import org.transitclock.db.structs.Arrival;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Departure;
import org.transitclock.db.structs.Location;
import org.transitclock.db.structs.Route;
import org.transitclock.db.structs.ScheduleTime;
import org.transitclock.db.structs.Stop;
import org.transitclock.db.structs.StopPath;
import org.transitclock.db.structs.TravelTimesForStopPath;
import org.transitclock.db.structs.TravelTimesForTrip;
import org.transitclock.db.structs.Trip;
import org.transitclock.db.structs.TripPattern;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.gtfs.TitleFormatter;
import org.transitclock.gtfs.gtfsStructs.GtfsRoute;
import org.transitclock.gtfs.gtfsStructs.GtfsStop;
import org.transitclock.gtfs.gtfsStructs.GtfsTrip;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.utils.Time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Generate test data for Kalman prediction generation.
 */
public class KalmanDataGenerator {

  public static final String VEHICLE = "1234";
  public static final int CONFIG_REV = -1;
  public static final int TRAVEL_TIMES_REV = -2;
  public static final String AGENCY_ID = SingletonSupport.AGENCY_ID;
  public static final String BLOCK_ID = "block1";
  public static final String ROUTE_ID = "route1";
  public static final String SERVICE_ID = "winter";
  public static final String TRIP_ID = "t1";
  public static final String TRIP_HEADSIGN = "h1";
  public static final String DIRECTION_ID = "0";
  public static final String SHAPE_ID = "shape1";
  public static final String PATH_ID = "path1";
  public static final String STOP_ID = "stop1";
  public static final Integer STOP_CODE = 1;
  public static final String STOP_NAME = "StOp1";
  public static final String PROJECT_ID = "project1";
  public static final String STOP_PATH_ID = "sp1";
  private static final String TRIP_PATTERN_ID = "tp1";
  private static final int DEFAULT_TRAVEL_TIMES = 300;
  private Long avlTime = null;

  public KalmanDataGenerator(long referenceTime) {
    avlTime = referenceTime;
  }


  public long getAvlTime() {
    if (avlTime == null || avlTime == 0)
      avlTime = new Date().getTime();
    return avlTime;
  }

  public VehicleState getVehicleState() {
    return getVehicleState(0, 0, 300);
  }

  public VehicleState getVehicleState(int tripIndex, int stopPathIndex, int scheduleDeviation) {
    VehicleState vs = new VehicleState(VEHICLE);
    SpatialMatch spatialMatch = new SpatialMatch(getAvlTime(),
            getBlock(),
            tripIndex, stopPathIndex, 0,
            0.0, 0.0);
    TemporalMatch match = new TemporalMatch(spatialMatch, new TemporalDifference(scheduleDeviation));
    vs.setMatch(match);
    return vs;

  }

  public AvlReport getAvlReport() {
    AvlReport avlReport = new AvlReport(VEHICLE,
            getAvlTime(), 0.0, 0.0,
            0.0f, 0.0f, "source");
    return avlReport;
  }

  public Indices getIndicies() {
    Block block = getBlock();
    int tripIndex = 0;
    int stopPathIndex = 0;
    int segmentIndex = 0;
    Indices index = new Indices(block, tripIndex, stopPathIndex,
            segmentIndex);
    return index;
  }

  public Block getBlock() {
    int configRev = CONFIG_REV;
    String blockId = BLOCK_ID;
    String serviceId = SERVICE_ID;
    int startTime = 0;
    int endTime = 1;

    return new Block(configRev, blockId, serviceId,
            startTime, endTime, getTrips());
  }

  public List<Trip> getTrips() {
    List<Trip> trips = new ArrayList<>();

    Trip t = getTrip();
    trips.add(t);
    return trips;
  }

  private Trip getTrip() {
    Trip t = new Trip(CONFIG_REV, getGtfsTrip(), ROUTE_ID,
            ROUTE_ID, TRIP_HEADSIGN,
            new TitleFormatter("", false));
    TripPattern pattern = getTripPattern(t);
    Route route = getRoute(t);
    t.setRoute(route);
    t.setTripPattern(pattern);
    t.setTravelTimes(getTravelTimes(t));
    t.getStopPaths().addAll(getStopPaths());
    ScheduleTime scheduleTime = new ScheduleTime(getScheduleTime(getArrivalTime().getTime()),
            getScheduleTime(getDepartureTime().getTime()));
    ArrayList<ScheduleTime> times = new ArrayList<>();
    times.add(scheduleTime);
    t.addScheduleTimes(times);

    return t;
  }

  private int getScheduleTime(long time) {
    return new Time(getTimeZone()).getSecondsIntoDay(time);
  }

  public String getTimeZone() {
    return SingletonSupport.getTimeZone();
  }

  public GtfsRoute getGtfsRoute() {
    return new GtfsRoute(
            ROUTE_ID,
            AGENCY_ID,
            ROUTE_ID,
            ROUTE_ID,
            "3",
            "black",
            "white"
    );
  }

  public GtfsStop getGtfsStop() {
    return new GtfsStop(STOP_ID, STOP_CODE, STOP_NAME,
            0.0, 0.0);
  }

  public Stop getStop() {
    return new Stop(CONFIG_REV, getGtfsStop(), null,
            new TitleFormatter("", false));
  }

  public Route getRoute(Trip trip) {
    GtfsRoute gtfsRoute = getGtfsRoute();
    List<TripPattern> patterns = new ArrayList<>();
    patterns.add(getTripPattern(trip));
    Route route = new Route(
            CONFIG_REV,
            gtfsRoute,
            patterns,
            new TitleFormatter("", false) );
    ArrayList<String> directionIds = new ArrayList<>();
    directionIds.add("0");
    route.setDirectionIds(directionIds);
    route.setTripPatterns(patterns);
  return route;
  }

  private TripPattern getTripPattern(Trip t) {
    return  new TripPattern(CONFIG_REV, SHAPE_ID, getStopPaths(),
            t, getGtfsData(t));

  }

  public TravelTimesForTrip getTravelTimes(Trip trip) {
  return getTravelTimes(trip, DEFAULT_TRAVEL_TIMES);
  }

  public TravelTimesForTrip getTravelTimes(Trip trip, int travelTimes) {
    TravelTimesForTrip ttt = new TravelTimesForTrip(
            CONFIG_REV,
            TRAVEL_TIMES_REV,
            trip
    );
    List<Integer> travelTimesMsec = new ArrayList<>();
    travelTimesMsec.add(travelTimes);
    double travelTimeSegmentDistance = 200;
    TravelTimesForStopPath ttsp = new TravelTimesForStopPath(
            CONFIG_REV,
            TRAVEL_TIMES_REV,
            STOP_PATH_ID,
            travelTimeSegmentDistance,
            travelTimesMsec,
            0,
            0,
            TravelTimesForStopPath.HowSet.TRIP,
            trip
    );
    ttt.add(ttsp);
    return ttt;
  }

  public GtfsData getGtfsData(Trip t) {
    GtfsData gd = new GtfsTestData(
            getAvlTime(),
            CONFIG_REV,
            null,
            new Date(),
            false,
            false,
            PROJECT_ID,
            "gtfs",
            null,
            50.,
            200.,
            200.,
            10,
            100,
            200,
            false,
            new TitleFormatter("", false));

    return gd;
  }

  public List<StopPath> getStopPaths() {
    List<StopPath> paths = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      StopPath path = new StopPath(
              CONFIG_REV,
              PATH_ID,
              "stop" + i,
              i,
              false,
              ROUTE_ID,
              false,
              false,
              false,
              null,
              null,
              null);
      path.setTripPatternId(TRIP_PATTERN_ID);
      path.setLocations(getLocations(i));
      path.onLoad(null, null);
      paths.add(path);
    }
    return paths;
  }

  public ArrayList<Location> getLocations(int stopsAway) {
    ArrayList<Location> locations = new ArrayList<>();
    Location l1 = getLocation(stopsAway, 0);
    locations.add(l1);
    Location l2 = getLocation(stopsAway, stopsAway);
    locations.add(l2);
    return locations;
  }

  public Location getLocation(int xIncrement, int yIncrement) {
    double distanceIncrement = 0.001 * 4; //444m
    return new Location(
            38.831741 + (xIncrement * distanceIncrement),
            -77.116982 + (yIncrement * distanceIncrement));
  }

  public GtfsTrip getGtfsTrip() {
    return new GtfsTrip(ROUTE_ID, SERVICE_ID, TRIP_ID,
            TRIP_HEADSIGN, TRIP_ID, DIRECTION_ID,
            BLOCK_ID, SHAPE_ID);
  }

  public Date getDepartureTime() {
    return new Date(getAvlTime() + 1001);
  }

  // sample data of 300
  public Date getArrivalTime() {
    return new Date(getAvlTime() + 1001 + 300);
  }

  // sample data of  380,420,400
  private int[] travelTimes = {380, 420, 400};
  public List<TravelTimeDetails> getLastDaysTimes() {
    List<TravelTimeDetails> lastDays = new ArrayList<>();
    long dayTime = getAvlTime();
    // we can only generate 3 as max kalman days defaults to 3
    for (int i = 0; i < 3; i++) {
      dayTime = dayTime - Time.MS_PER_DAY;
      TravelTimeDetails ttd = null;
      try {
        ttd = new TravelTimeDetails(
        new IpcArrivalDeparture(getDeparture(new Date(dayTime),
                new Date(dayTime),
                getBlock())),
        new IpcArrivalDeparture(getArrival(new Date(dayTime + travelTimes[i]),
                new Date(dayTime),
                getBlock())));
      } catch (Exception e) {
        e.printStackTrace();
      }
      lastDays.add(ttd);
    }

    return lastDays;
  }

  public Departure getDeparture(Date departureTime,
                                Date avlTime,
                                Block block) {
    Departure d = new Departure(
            KalmanDataGenerator.CONFIG_REV,
            KalmanDataGenerator.VEHICLE,
            departureTime,
            avlTime,
            block,
            0,
            0,
            null,
            0l,
            KalmanDataGenerator.STOP_PATH_ID,
            false);
    return d;
  }

  public Arrival getArrival(Date arrivalTime,
                            Date avlTime,
                            Block block) {
    Arrival a = new Arrival(KalmanDataGenerator.CONFIG_REV,
            KalmanDataGenerator.VEHICLE,
            arrivalTime,
            avlTime,
            block,
            0,
            0,
            null,
            KalmanDataGenerator.STOP_PATH_ID,
            false);
    return a;
  }

  public KalmanError getErrorValue(Indices indices) {
    return new KalmanError(72.40);
  }

  public KalmanError getDwellErrorValue(Indices indices) {
    return new KalmanError(25.1);
  }

  public VehicleState getVehicleStateForApc(long tripStartTime, int tripIndex, int stopPathIndex, int scheduleDeviation) {
    VehicleState vs = getVehicleState(tripIndex, stopPathIndex, scheduleDeviation);
    vs.putTripStartTime(0, tripStartTime);
    return vs;
  }

  public String getAgencyId() {
    return AGENCY_ID;
  }

  public List<ApcParsedRecord> getApcParsedRecords(long referenceTime, int tripIndex, int stopPathIndex) {
    List<ApcParsedRecord> records = new ArrayList<>();
    long startOfDay = Time.getStartOfDay(new Date(referenceTime));
    ApcParsedRecord apc = new ApcParsedRecord(
            new Long(System.currentTimeMillis()).toString(),
    referenceTime,
    startOfDay,
    "driverId",
    0,
    VEHICLE,
    1,
    1,
    0,
    0,
            new Long((referenceTime - startOfDay)/Time.MS_PER_SEC).intValue(),
            new Long((referenceTime - startOfDay + Time.SEC_PER_MIN)/Time.MS_PER_SEC).intValue(),
    0.01,
    0.01);
    ArrivalDeparture.Builder ad = new ArrivalDeparture.Builder(VEHICLE,
    referenceTime,
    referenceTime,
    null,
    DIRECTION_ID,
    tripIndex,
    stopPathIndex,
    stopPathIndex,
    true,
    -1,
    1,
    BLOCK_ID,
    TRIP_ID,
    STOP_ID,
    0,
    0.0f,
    ROUTE_ID,
    ROUTE_ID,
    SERVICE_ID,
    null,
    null,
    TRIP_PATTERN_ID,
    STOP_PATH_ID,
    false);
    apc.setArrivalDeparture(ad.create());
    records.add(apc);
    return records;
  }

  public List<ApcReport> getApcReports(long referenceTime, int tripIndex, int stopPathIndex) {
    List<ApcReport> reports = new ArrayList<>();
    for (ApcParsedRecord apc : getApcParsedRecords(referenceTime, tripIndex, stopPathIndex)) {
      reports.add(apc.toApcReport());
    }
    return reports;
  }

}
