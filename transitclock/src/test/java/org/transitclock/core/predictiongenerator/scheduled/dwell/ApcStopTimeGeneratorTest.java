package org.transitclock.core.predictiongenerator.scheduled.dwell;

import org.junit.Before;
import org.junit.Test;
import org.transitclock.SingletonSupport;
import org.transitclock.avl.ApcModule;
import org.transitclock.core.Indices;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheFactory;
import org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman.KalmanPredictionGeneratorImpl;
import org.transitclock.db.GtfsBasedDataGenerator;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.utils.Time;

import java.util.Date;

import static org.junit.Assert.*;

public class ApcStopTimeGeneratorTest {

  public static final String BLOCK_ID = "b1";
  public static final String HEADWAY_BLOCK_ID = "b0";
  public static final String TRIP_ID = "18369405-MAR21-MVS-BUS-Weekday-05";
  public static final String HEADWAY_TRIP_ID = "18369406-MAR21-MVS-BUS-Weekday-05";


  private ApcStopTimeGenerator generator = new ApcStopTimeGenerator();
  private KalmanPredictionGeneratorImpl defaultGenerator = new KalmanPredictionGeneratorImpl();
  private GtfsBasedDataGenerator dataGenerator;
  private long referenceTime;
  private int tripIndex = 3;
  private int headwayTripIndex = 2;
  private int stopPathIndex = 2;
  private String stopId;
  private int testScheduleDeviationSeconds = 60;
  private int testHeadwayMinutes = 30;
  private int testHeadwayMinutes1 = 29;
  private int testHeadwayMinutes2 = 30;
  private int testHeadwayMinutes3 = 31;
  private String testVehicle = "1234";
  private String headwayVehicle = "1235";
  private int boardings = 2;
  private int boardingWindow = 15;
  private int dwellSeconds = 9;

  @Before
  public void setUp() throws Exception {


    //stop1.202104281208
    referenceTime = SingletonSupport.toEpoch("2021-04-21", "09:40:00", "CST");

    dataGenerator = new GtfsBasedDataGenerator("20210412_met_gtfs.zip", "0");
    dataGenerator.load();

    // setup core instance to prevent exceptions
    SingletonSupport.createTestCore("CST");

    ApcModule module = ApcModule.getInstance();

    long yesterdayReferenceTime = referenceTime - Time.MS_PER_DAY;
    long referenceTimeN2 = yesterdayReferenceTime - Time.MS_PER_DAY;
    long referenceTimeN3 = referenceTimeN2 - Time.MS_PER_DAY;

    // add apc for yesterday
    assertEquals("18369405-MAR21-MVS-BUS-Weekday-05", dataGenerator.getBlock().getTrip(tripIndex).getId());
    stopId = dataGenerator.getBlock().getTrip(tripIndex).getStopPath(stopPathIndex).getStopId();
    assertEquals("17978", stopId);
    String previousStopId = dataGenerator.getBlock().getTrip(tripIndex).getStopPath(stopPathIndex-1).getStopId();
    // 17976 is previousStop
    assertEquals("17976", previousStopId);

    // add apc for future stop
    module.getProcessor().analyze(dataGenerator.getApcReports(referenceTime, tripIndex, stopPathIndex, stopId, testVehicle, boardings, dwellSeconds));
    module.getProcessor().analyze(dataGenerator.getApcReports(yesterdayReferenceTime, tripIndex, stopPathIndex, stopId, testVehicle, boardings, dwellSeconds));
    module.getProcessor().analyze(dataGenerator.getApcReports(referenceTimeN2, tripIndex, stopPathIndex, stopId, testVehicle, boardings, dwellSeconds));
    module.getProcessor().analyze(dataGenerator.getApcReports(referenceTimeN3, tripIndex, stopPathIndex, stopId, testVehicle, boardings, dwellSeconds));


    // add apc for previous stop
    module.getProcessor().analyze(dataGenerator.getApcReports(referenceTime, tripIndex, stopPathIndex-1, previousStopId, testVehicle, boardings, dwellSeconds));
    module.getProcessor().analyze(dataGenerator.getApcReports(yesterdayReferenceTime, tripIndex, stopPathIndex-1, previousStopId, testVehicle, boardings, dwellSeconds));


    // add stop cache history
    // setup a headway arrival departure
    StopArrivalDepartureCacheFactory.getInstance().putArrivalDeparture(
            dataGenerator.getHeadwayArrivalDeparture(referenceTime - (testHeadwayMinutes * Time.MS_PER_MIN) /*30min headway*/,
                    HEADWAY_BLOCK_ID, HEADWAY_TRIP_ID,
                    headwayTripIndex, stopPathIndex, testScheduleDeviationSeconds, headwayVehicle, stopId));

    StopArrivalDepartureCacheFactory.getInstance().putArrivalDeparture(
            dataGenerator.getHeadwayArrivalDeparture(referenceTime - (testHeadwayMinutes * Time.MS_PER_MIN) /*30min headway*/,
                    HEADWAY_BLOCK_ID, HEADWAY_TRIP_ID,
                    headwayTripIndex, stopPathIndex-1, testScheduleDeviationSeconds, headwayVehicle, previousStopId));

    //yesterday A/D
    StopArrivalDepartureCacheFactory.getInstance().putArrivalDeparture(
            dataGenerator.getHeadwayArrivalDeparture(yesterdayReferenceTime,
                    BLOCK_ID, TRIP_ID,
                    tripIndex, stopPathIndex, testScheduleDeviationSeconds, testVehicle, stopId));
    StopArrivalDepartureCacheFactory.getInstance().putArrivalDeparture(
            dataGenerator.getHeadwayArrivalDeparture(referenceTimeN2,
                    BLOCK_ID, TRIP_ID,
                    tripIndex, stopPathIndex, testScheduleDeviationSeconds, testVehicle, stopId));
    StopArrivalDepartureCacheFactory.getInstance().putArrivalDeparture(
            dataGenerator.getHeadwayArrivalDeparture(referenceTimeN3,
                    BLOCK_ID, TRIP_ID,
                    tripIndex, stopPathIndex, testScheduleDeviationSeconds, testVehicle, stopId));

    //yesterday headway
    StopArrivalDepartureCacheFactory.getInstance().putArrivalDeparture(
            dataGenerator.getHeadwayArrivalDeparture(yesterdayReferenceTime - (testHeadwayMinutes1 * Time.MS_PER_MIN) /*30min headway*/,
                    HEADWAY_BLOCK_ID, HEADWAY_TRIP_ID,
                    headwayTripIndex, stopPathIndex, testScheduleDeviationSeconds, headwayVehicle, stopId));
    StopArrivalDepartureCacheFactory.getInstance().putArrivalDeparture(
            dataGenerator.getHeadwayArrivalDeparture(yesterdayReferenceTime - (testHeadwayMinutes1 * Time.MS_PER_MIN) /*30min headway*/,
                    HEADWAY_BLOCK_ID, HEADWAY_TRIP_ID,
                    headwayTripIndex, stopPathIndex-1, testScheduleDeviationSeconds, headwayVehicle, previousStopId));
    StopArrivalDepartureCacheFactory.getInstance().putArrivalDeparture(
            dataGenerator.getHeadwayArrivalDeparture(referenceTimeN2 - (testHeadwayMinutes2 * Time.MS_PER_MIN) /*30min headway*/,
                    HEADWAY_BLOCK_ID, HEADWAY_TRIP_ID,
                    headwayTripIndex, stopPathIndex, testScheduleDeviationSeconds, headwayVehicle, stopId));
    StopArrivalDepartureCacheFactory.getInstance().putArrivalDeparture(
            dataGenerator.getHeadwayArrivalDeparture(referenceTimeN2 - (testHeadwayMinutes2 * Time.MS_PER_MIN) /*30min headway*/,
                    HEADWAY_BLOCK_ID, HEADWAY_TRIP_ID,
                    headwayTripIndex, stopPathIndex-1, testScheduleDeviationSeconds, headwayVehicle, previousStopId));
    StopArrivalDepartureCacheFactory.getInstance().putArrivalDeparture(
            dataGenerator.getHeadwayArrivalDeparture(referenceTimeN3 - (testHeadwayMinutes3 * Time.MS_PER_MIN) /*30min headway*/,
                    HEADWAY_BLOCK_ID, HEADWAY_TRIP_ID,
                    headwayTripIndex, stopPathIndex, testScheduleDeviationSeconds, headwayVehicle, stopId));
    StopArrivalDepartureCacheFactory.getInstance().putArrivalDeparture(
            dataGenerator.getHeadwayArrivalDeparture(referenceTimeN3 - (testHeadwayMinutes3 * Time.MS_PER_MIN) /*30min headway*/,
                    HEADWAY_BLOCK_ID, HEADWAY_TRIP_ID,
                    headwayTripIndex, stopPathIndex-1, testScheduleDeviationSeconds, headwayVehicle, previousStopId));

  }

  @Test
  public void getStopTimeForPath() {

    checkPreConditions();

    Indices indices = dataGenerator.getIndicies(tripIndex, stopPathIndex);
    VehicleState vehicleState = dataGenerator.getVehicleStateForApc(referenceTime, tripIndex, stopPathIndex, testScheduleDeviationSeconds);
    AvlReport avlReport = vehicleState.getAvlReport();
    long stopTimeForPath = generator.getStopTimeForPath(indices, avlReport, vehicleState);
    assertEquals(8000, stopTimeForPath);

    long alternate = defaultGenerator.getStopTimeForPath(indices, avlReport, vehicleState);
    assertEquals(10000, alternate);
  }

  private void checkPreConditions() {
    // ensure test data for boardings
    assertNotNull(ApcModule.getInstance());

    Double boardingsPerSecond = ApcModule.getInstance().getPassengerArrivalRate(dataGenerator.getBlock().getTrip(tripIndex), stopId, new Date(referenceTime));
    assertNotNull(boardingsPerSecond);
    assertEquals(new Double(boardings).doubleValue() / 15 / Time.SEC_PER_MIN, boardingsPerSecond, 0.0001);
  }
}