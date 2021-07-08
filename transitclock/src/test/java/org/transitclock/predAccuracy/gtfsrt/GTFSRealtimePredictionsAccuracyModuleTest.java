package org.transitclock.predAccuracy.gtfsrt;

import com.google.transit.realtime.GtfsRealtime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.transitclock.core.predAccuracy.PredAccuracyPrediction;
import org.transitclock.core.predAccuracy.gtfsrt.GTFSRealtimePredictionAccuracyModule;
import org.transitclock.db.structs.*;
import org.transitclock.gtfs.DbConfig;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class GTFSRealtimePredictionsAccuracyModuleTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DbConfig dbConfig;

    @Captor
    ArgumentCaptor<PredAccuracyPrediction> arrivalPredictionCaptor;

    @Captor
    ArgumentCaptor<PredAccuracyPrediction> departurePredictionCaptor;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Trip gtfsTrip;

    @Spy
    private GTFSRealtimePredictionAccuracyModule module = new GTFSRealtimePredictionAccuracyModule("1");

    @Before
    public void setup(){
        TimeZone.setDefault(TimeZone.getTimeZone("America/Chicago"));
        MockitoAnnotations.initMocks(this);
        when(dbConfig.getFirstAgency().getTimeZone()).thenReturn(getTimeZone());
    }

    private TimeZone getTimeZone(){
        return TimeZone.getTimeZone("America/Chicago");
    }

    @Test
    public void feedMessageTest(){
        List<StopTimeTestClass> stopTimes = getStopTimesOne();
        TripUpdateTestClass tripUpdate = getTripUpdateOne(120, stopTimes);
        List<GtfsRealtime.FeedEntity> entities = getEntities(tripUpdate);

        assertEquals(1, entities.size());

        for (int i = 0; i < entities.size(); i++) {
            GtfsRealtime.FeedEntity fe = entities.get(i);
            assertFalse(fe.hasAlert());
            assertTrue(fe.hasTripUpdate());
            assertFalse(fe.hasVehicle());
            assertEquals(Integer.toString(i), fe.getId());
        }

        // (GMT) Saturday, April 10, 2021 5:54:12 AM
        long feedMessageRequestTime = 1618034052000l;
        long feedMessageRequestTimeSec = feedMessageRequestTime / 1000;

        GtfsRealtime.FeedMessage feedMessage = getFeedMessage(feedMessageRequestTime, entities);
        assertEquals(entities.size(), feedMessage.getEntityCount());
        assertTrue(feedMessage.hasHeader());
        assertEquals(feedMessageRequestTimeSec, feedMessage.getHeader().getTimestamp());

    }

    /**
     * Test stop time predictions and trip update delay
     */
    @Test
    public void testTripUpdatesWithDelay(){
        // (GMT) Saturday, April 10, 2021 5:54:12 AM
        long feedMessageRequestTime = 1618034052000l;

        // Mock StopPaths
        List<StopPathTestClass> stopPathTestClasses = new ArrayList<>();
        StopPathTestClass stopPathOne = new StopPathTestClass("0", 0);
        stopPathTestClasses.add(stopPathOne);
        StopPathTestClass stopPathTwo = new StopPathTestClass("1", 1);
        stopPathTestClasses.add(stopPathTwo);
        StopPathTestClass stopPathThree = new StopPathTestClass("2", 2);
        stopPathTestClasses.add(stopPathThree);
        List<StopPath> stopPaths = getStopPaths(stopPathTestClasses);

        // Mock ScheduleTime
        ScheduleTime scheduleTimeOne = new ScheduleTime(3115, 3155);
        when(gtfsTrip.getScheduleTime(0)).thenReturn(scheduleTimeOne);

        ScheduleTime scheduleTimeTwo = new ScheduleTime(3255, 3270);
        when(gtfsTrip.getScheduleTime(1)).thenReturn(scheduleTimeTwo);

        ScheduleTime scheduleTimeThree = new ScheduleTime(3375, 3390);
        when(gtfsTrip.getScheduleTime(2)).thenReturn(scheduleTimeThree);

        // Mock Trip
        when(gtfsTrip.getDirectionId()).thenReturn("0");
        when(gtfsTrip.getStopPaths()).thenReturn(stopPaths);
        when(gtfsTrip.getStopPath(anyInt())).then(i -> stopPaths.get(i.getArgument(0)));
        when(dbConfig.getTrip(anyString())).thenReturn(gtfsTrip);
        doNothing().when(module).storeArrivalPrediction(arrivalPredictionCaptor.capture());
        doNothing().when(module).storeDeparturePrediction(departurePredictionCaptor.capture());

        // Setup Feed Message
        List<StopTimeTestClass> stopTimes = getStopTimesOne();
        TripUpdateTestClass tripUpdate = getTripUpdateOne(120, stopTimes);
        List<GtfsRealtime.FeedEntity> entities = getEntities(tripUpdate);
        GtfsRealtime.FeedMessage feedMessage = getFeedMessage(feedMessageRequestTime, entities);

        module.processExternalPredictions(feedMessage, dbConfig);
        List<PredAccuracyPrediction> arrivalPredictions = arrivalPredictionCaptor.getAllValues();
        List<PredAccuracyPrediction> departurePredictions = departurePredictionCaptor.getAllValues();

        assertEquals(2, arrivalPredictions.size());
        assertEquals(2, departurePredictions.size());

        // Check Stop Time Update Arrival and Departure Times
        long expectedArrivalTime = 1618034175000l;
        long expectedDepartureTime = 1618034190000l;

        assertEquals(expectedArrivalTime, arrivalPredictions.get(0).getPredictedTime().getTime());
        assertEquals(expectedDepartureTime, departurePredictions.get(0).getPredictedTime().getTime());

        // Check Scheduled Arrival and Departure Times With Trip Delay
        expectedArrivalTime = 1618034295000l;
        expectedDepartureTime = 1618034310000l;
        assertEquals(expectedArrivalTime, arrivalPredictions.get(1).getPredictedTime().getTime());
        assertEquals(expectedDepartureTime, departurePredictions.get(1).getPredictedTime().getTime());

    }

    /**
     * Test StopTimes with No Delay
     */
    @Test
    public void testStopTimeWithNoDelays(){
        // (GMT) Saturday, April 10, 2021 5:54:12 AM
        long feedMessageRequestTime = 1618034052000l;

        // Mock StopPaths
        List<StopPathTestClass> stopPathTestClasses = new ArrayList<>();
        StopPathTestClass stopPathOne = new StopPathTestClass("0", 0);
        stopPathTestClasses.add(stopPathOne);
        StopPathTestClass stopPathTwo = new StopPathTestClass("1", 1);
        stopPathTestClasses.add(stopPathTwo);
        StopPathTestClass stopPathThree = new StopPathTestClass("2", 2);
        stopPathTestClasses.add(stopPathThree);
        List<StopPath> stopPaths = getStopPaths(stopPathTestClasses);

        // Mock ScheduleTime
        ScheduleTime scheduleTimeOne = new ScheduleTime(3115, 3155);
        when(gtfsTrip.getScheduleTime(0)).thenReturn(scheduleTimeOne);

        ScheduleTime scheduleTimeTwo = new ScheduleTime(3255, 3270);
        when(gtfsTrip.getScheduleTime(1)).thenReturn(scheduleTimeTwo);

        ScheduleTime scheduleTimeThree = new ScheduleTime(3375, 3390);
        when(gtfsTrip.getScheduleTime(2)).thenReturn(scheduleTimeThree);

        // Mock Trip
        when(gtfsTrip.getDirectionId()).thenReturn("0");
        when(gtfsTrip.getStopPaths()).thenReturn(stopPaths);
        when(gtfsTrip.getStopPath(anyInt())).then(i -> stopPaths.get(i.getArgument(0)));
        when(dbConfig.getTrip(anyString())).thenReturn(gtfsTrip);
        doNothing().when(module).storeArrivalPrediction(arrivalPredictionCaptor.capture());
        doNothing().when(module).storeDeparturePrediction(departurePredictionCaptor.capture());

        // Setup Feed Message
        List<StopTimeTestClass> stopTimes = getStopTimesOne();
        TripUpdateTestClass tripUpdate = getTripUpdateOne(null, stopTimes);
        List<GtfsRealtime.FeedEntity> entities = getEntities(tripUpdate);
        GtfsRealtime.FeedMessage feedMessage = getFeedMessage(feedMessageRequestTime, entities);

        module.processExternalPredictions(feedMessage, dbConfig);
        List<PredAccuracyPrediction> arrivalPredictions = arrivalPredictionCaptor.getAllValues();
        List<PredAccuracyPrediction> departurePredictions = departurePredictionCaptor.getAllValues();

        assertEquals(2, arrivalPredictions.size());
        assertEquals(2, departurePredictions.size());

        // Check Stop Time Update Arrival and Departure Times
        long expectedArrivalTime = 1618034175000l;
        long expectedDepartureTime = 1618034190000l;

        assertEquals(expectedArrivalTime, arrivalPredictions.get(0).getPredictedTime().getTime());
        assertEquals(expectedDepartureTime, departurePredictions.get(0).getPredictedTime().getTime());

        // Check Scheduled Arrival and Departure Times With No Trip Delay or Stop Time Delay
        expectedArrivalTime = 1618034295000l;
        expectedDepartureTime = 1618034310000l;
        assertEquals(expectedArrivalTime, arrivalPredictions.get(1).getPredictedTime().getTime());
        assertEquals(expectedDepartureTime, departurePredictions.get(1).getPredictedTime().getTime());
    }

    @Test
    public void testStopTimeWithMultipleStopTimes(){
        // (GMT) Saturday, April 10, 2021 5:54:12 AM
        long feedMessageRequestTime = 1618034052000l;

        // Mock StopPaths
        List<StopPathTestClass> stopPathTestClasses = new ArrayList<>();
        StopPathTestClass stopPathOne = new StopPathTestClass("0", 0);
        stopPathTestClasses.add(stopPathOne);
        StopPathTestClass stopPathTwo = new StopPathTestClass("1", 1);
        stopPathTestClasses.add(stopPathTwo);
        StopPathTestClass stopPathThree = new StopPathTestClass("2", 2);
        stopPathTestClasses.add(stopPathThree);
        StopPathTestClass stopPathFour = new StopPathTestClass("3", 3);
        stopPathTestClasses.add(stopPathFour);
        StopPathTestClass stopPathFive = new StopPathTestClass("4", 4);
        stopPathTestClasses.add(stopPathFive);
        List<StopPath> stopPaths = getStopPaths(stopPathTestClasses);

        // Mock ScheduleTime
        ScheduleTime scheduleTimeOne = new ScheduleTime(3115, 3155);
        when(gtfsTrip.getScheduleTime(0)).thenReturn(scheduleTimeOne);

        ScheduleTime scheduleTimeTwo = new ScheduleTime(3255, 3270);
        when(gtfsTrip.getScheduleTime(1)).thenReturn(scheduleTimeTwo);

        ScheduleTime scheduleTimeThree = new ScheduleTime(3375, 3390);
        when(gtfsTrip.getScheduleTime(2)).thenReturn(scheduleTimeThree);

        ScheduleTime scheduleTimeFour = new ScheduleTime(3490, 3520);
        when(gtfsTrip.getScheduleTime(3)).thenReturn(scheduleTimeFour);

        ScheduleTime scheduleTimeFive = new ScheduleTime(3600, 3615);
        when(gtfsTrip.getScheduleTime(4)).thenReturn(scheduleTimeFive);

        // Mock Trip
        when(gtfsTrip.getDirectionId()).thenReturn("0");
        when(gtfsTrip.getStopPaths()).thenReturn(stopPaths);
        when(gtfsTrip.getStopPath(anyInt())).then(i -> stopPaths.get(i.getArgument(0)));
        when(dbConfig.getTrip(anyString())).thenReturn(gtfsTrip);
        doNothing().when(module).storeArrivalPrediction(arrivalPredictionCaptor.capture());
        doNothing().when(module).storeDeparturePrediction(departurePredictionCaptor.capture());

        // Setup Feed Message
        List<StopTimeTestClass> stopTimes = getStopTimesTwo();
        TripUpdateTestClass tripUpdate = getTripUpdateOne(null, stopTimes);
        List<GtfsRealtime.FeedEntity> entities = getEntities(tripUpdate);
        GtfsRealtime.FeedMessage feedMessage = getFeedMessage(feedMessageRequestTime, entities);

        module.processExternalPredictions(feedMessage, dbConfig);
        List<PredAccuracyPrediction> arrivalPredictions = arrivalPredictionCaptor.getAllValues();
        List<PredAccuracyPrediction> departurePredictions = departurePredictionCaptor.getAllValues();

        assertEquals(4, arrivalPredictions.size());
        assertEquals(4, departurePredictions.size());

        // Check Stop Time Update Arrival and Departure Times With No Trip Delay and Stop Time arrival departure time
        long expectedArrivalTime = 1618034175000l;
        long expectedDepartureTime = 1618034190000l;

        assertEquals(expectedArrivalTime, arrivalPredictions.get(0).getPredictedTime().getTime());
        assertEquals(expectedDepartureTime, departurePredictions.get(0).getPredictedTime().getTime());

        // Check Scheduled Arrival and Departure Times With No Trip Delay and inferred stop time delay
        expectedArrivalTime = 1618034295000l;
        expectedDepartureTime = 1618034310000l;
        assertEquals(expectedArrivalTime, arrivalPredictions.get(1).getPredictedTime().getTime());
        assertEquals(expectedDepartureTime, departurePredictions.get(1).getPredictedTime().getTime());

        // Check Scheduled Arrival and Departure Times With No Trip Delay and Stop Time arrival departure time
        expectedArrivalTime = 1618034350000l;
        expectedDepartureTime = 1618034385000l;
        assertEquals(expectedArrivalTime, arrivalPredictions.get(2).getPredictedTime().getTime());
        assertEquals(expectedDepartureTime, departurePredictions.get(2).getPredictedTime().getTime());

        // Check Scheduled Arrival and Departure Times With 60 second Stop Time Delay
        expectedArrivalTime = 1618034460000l;
        expectedDepartureTime = 1618034475000l;
        assertEquals(expectedArrivalTime, arrivalPredictions.get(3).getPredictedTime().getTime());
        assertEquals(expectedDepartureTime, departurePredictions.get(3).getPredictedTime().getTime());
    }

    private List<StopPath> getStopPaths(List<StopPathTestClass> stopPathTestClasses){
        List<StopPath> stopPaths = new ArrayList<>();

        for(StopPathTestClass sptc : stopPathTestClasses){
            StopPath stopPath = new StopPath(sptc.getConfigRev(),
                                             sptc.getStopPathId(),
                                             sptc.getStopId(),
                                             sptc.getGtfsStopSeq(),
                                             sptc.isLastStopInTrip(),
                                             sptc.getRouteId(),
                                             sptc.isLayoverStop(),
                                             sptc.isWaitStop(),
                                             sptc.isScheduleAdherenceStop(),
                                             sptc.getBreakTime(),
                                             sptc.getMaxDistance(),
                                             sptc.getMaxSpeed());
            stopPaths.add(stopPath);
        }
        return stopPaths;
    }

    private TripUpdateTestClass getTripUpdateOne(Integer delay, List<StopTimeTestClass> stopTimes){
        // (GMT) Saturday, April 10, 2021 5:53:30 AM
        long tripStartTime = 1618034010000l;

        LocalDateTime tripStartDateTime = Instant.ofEpochMilli(tripStartTime).atZone(getTimeZone().toZoneId()).toLocalDateTime();
        TripUpdateTestClass tripUpdate = new TripUpdateTestClass("0", "001", "0",
                tripStartDateTime, "111", delay,  stopTimes);

        return tripUpdate;
    }

    private List<StopTimeTestClass> getStopTimesOne(){
        // (GMT) Saturday, April 10, 2021 5:56:15 AM
        long arrivalTime = 1618034175000l;

        // (GMT) Saturday, April 10, 2021 5:56:30 AM
        long departureTime = 1618034190000l;

        List<StopTimeTestClass> stopTimes = new ArrayList<>();
        StopTimeTestClass stopTime = new StopTimeTestClass("1", 1, arrivalTime, departureTime, null);
        stopTimes.add(stopTime);

        return stopTimes;
    }

    private List<StopTimeTestClass> getStopTimesTwo(){
        List<StopTimeTestClass> stopTimes = new ArrayList<>();

        // (GMT) Saturday, April 10, 2021 5:56:15 AM
        long arrivalTimeOne = 1618034175000l;

        // (GMT) Saturday, April 10, 2021 5:56:30 AM
        long departureTimeOne = 1618034190000l;

        StopTimeTestClass stopTimeOne = new StopTimeTestClass("1", 1, arrivalTimeOne, departureTimeOne, null);
        stopTimes.add(stopTimeOne);

        // (GMT) Saturday, April 10, 2021 5:59:10 AM
        long arrivalTimeTwo = 1618034350000l;

        // (GMT) Saturday, April 10, 2021 5:59:45 AM
        long departureTimeTwo = 1618034385000l;

        StopTimeTestClass stopTimeTwo = new StopTimeTestClass("3", 3, arrivalTimeTwo, departureTimeTwo, 60);
        stopTimes.add(stopTimeTwo);

        return stopTimes;
    }

    private GtfsRealtime.FeedMessage getFeedMessage(Long requestTime, List<GtfsRealtime.FeedEntity> feedEntities){
        GtfsRealtime.FeedMessage.Builder builder = GtfsRealtime.FeedMessage.newBuilder();
        GtfsRealtime.FeedHeader.Builder header = GtfsRealtime.FeedHeader.newBuilder();
        header.setGtfsRealtimeVersion("1.0");
        long headerTime = requestTime != null ? requestTime : System.currentTimeMillis();
        header.setTimestamp(headerTime / 1000);
        header.setIncrementality(GtfsRealtime.FeedHeader.Incrementality.FULL_DATASET);
        builder.setHeader(header);
        builder.addAllEntity(feedEntities);
        return builder.build();
    }

    private List<GtfsRealtime.FeedEntity> getEntities(TripUpdateTestClass tripUpdateTestClass){

        List<GtfsRealtime.FeedEntity> entities = new ArrayList<>();
        List<GtfsRealtime.TripUpdate.StopTimeUpdate> stopTimeUpdates = new ArrayList<>();

        for(StopTimeTestClass sttc : tripUpdateTestClass.getStopTimeTestClasses()){
            stopTimeUpdates.add(makeStopTimeUpdate(sttc.getStopId(),
                                                   sttc.getStopSeq(),
                                                   sttc.getArrivalTimeMillis(),
                                                   sttc.getDepartureTimeMillis(),
                                                   sttc.getDelay()));

        }

        GtfsRealtime.TripUpdate tripUpdate = makeTripUpdate(tripUpdateTestClass.getTripId(),
                                                            tripUpdateTestClass.getRouteId(),
                                                            tripUpdateTestClass.getDirectionId(),
                                                            tripUpdateTestClass.getTripDateTime(),
                                                            tripUpdateTestClass.getVehicleId(),
                                                            tripUpdateTestClass.getDelay(),
                                                            stopTimeUpdates);

        GtfsRealtime.FeedEntity.Builder entity = GtfsRealtime.FeedEntity.newBuilder();
        entity.setTripUpdate(tripUpdate);
        entity.setId(tripUpdate.getTrip().getTripId());
        entities.add(entity.build());

        return entities;
    }

    private GtfsRealtime.TripUpdate makeTripUpdate(String tripId,
                                                           String routeId,
                                                           String directionId,
                                                           LocalDateTime tripDateTime,
                                                           String vehicleId,
                                                           Integer delay,
                                                           List<GtfsRealtime.TripUpdate.StopTimeUpdate> stopTimeUpdates){
        GtfsRealtime.TripUpdate.Builder tripUpdate = GtfsRealtime.TripUpdate.newBuilder();
        tripUpdate.setTrip(getTripDescriptor(tripId, routeId, directionId, tripDateTime.toLocalDate()));
        tripUpdate.setVehicle(getVehicleDescriptor(vehicleId));
        if(delay != null){
            tripUpdate.setDelay(delay);
        }
        tripUpdate.setTimestamp(tripDateTime.atZone(getTimeZone().toZoneId()).toInstant().toEpochMilli());
        tripUpdate.addAllStopTimeUpdate(stopTimeUpdates);
        return tripUpdate.build();
    }

    private GtfsRealtime.TripUpdate.StopTimeUpdate makeStopTimeUpdate(String stopId,
                                                                           int stopSeq,
                                                                           long arrivalTimeMillis,
                                                                           long departureTimeMillis,
                                                                           Integer delay) {
        GtfsRealtime.TripUpdate.StopTimeUpdate.Builder builder = GtfsRealtime.TripUpdate.StopTimeUpdate.newBuilder();
        builder.setStopId(stopId);
        builder.setArrival(makeStopTimeEvent(arrivalTimeMillis/1000, delay));
        builder.setDeparture(makeStopTimeEvent(departureTimeMillis/1000, delay));
        if (stopSeq >= 0)
            builder.setStopSequence(stopSeq);
        return builder.build();
    }

    private GtfsRealtime.TripUpdate.StopTimeEvent.Builder makeStopTimeEvent(long time, Integer delay) {
        GtfsRealtime.TripUpdate.StopTimeEvent.Builder builder = GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder();
        builder.setTime(time);
        if(delay != null){
            builder.setDelay(delay);
        }
        return builder;
    }



    private GtfsRealtime.TripDescriptor.Builder getTripDescriptor(String tripId, String routeId, String directionId,
                                                                  LocalDate startDate){
        GtfsRealtime.TripDescriptor.Builder trip = GtfsRealtime.TripDescriptor.newBuilder();
        trip.setTripId(tripId);
        trip.setRouteId(routeId);
        int direction = Integer.parseInt(directionId);
        trip.setDirectionId(direction);

        trip.setStartDate(startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        return trip;
    }

    private GtfsRealtime.VehicleDescriptor.Builder getVehicleDescriptor(String vehicleId){
        GtfsRealtime.VehicleDescriptor.Builder vehicle = GtfsRealtime.VehicleDescriptor.newBuilder();
        vehicle.setId(vehicleId);
        return vehicle;
    }

    private class TripUpdateTestClass{
        private String tripId;
        private String routeId;
        private String directionId;
        private LocalDateTime tripDateTime;
        private String vehicleId;
        private Integer delay;
        private List<StopTimeTestClass> stopTimeTestClasses;

        public TripUpdateTestClass(String tripId, String routeId, String directionId, LocalDateTime tripDateTime,
                                   String vehicleId, Integer delay, List<StopTimeTestClass> stopTimeTestClasses) {
            this.tripId = tripId;
            this.routeId = routeId;
            this.directionId = directionId;
            this.tripDateTime = tripDateTime;
            this.vehicleId = vehicleId;
            this.delay = delay;
            this.stopTimeTestClasses = stopTimeTestClasses;
        }

        public String getTripId() {
            return tripId;
        }

        public String getRouteId() {
            return routeId;
        }

        public String getDirectionId() {
            return directionId;
        }

        public LocalDateTime getTripDateTime() {
            return tripDateTime;
        }

        public String getVehicleId() {
            return vehicleId;
        }

        public Integer getDelay() {
            return delay;
        }

        public List<StopTimeTestClass> getStopTimeTestClasses() {
            return stopTimeTestClasses;
        }

        public void setStopTimeTestClasses(List<StopTimeTestClass> stopTimeTestClasses) {
            this.stopTimeTestClasses = stopTimeTestClasses;
        }
    }

    private class StopTimeTestClass {
        private String stopId;
        private int stopSeq;
        private long arrivalTimeMillis;
        private long departureTimeMillis;
        private Integer delay;

        public StopTimeTestClass(String stopId, int stopSeq, long arrivalTimeMillis, long departureTimeMillis, Integer delay) {
            this.stopId = stopId;
            this.stopSeq = stopSeq;
            this.arrivalTimeMillis = arrivalTimeMillis;
            this.departureTimeMillis = departureTimeMillis;
            this.delay = delay;
        }

        public String getStopId() {
            return stopId;
        }

        public int getStopSeq() {
            return stopSeq;
        }

        public long getArrivalTimeMillis() {
            return arrivalTimeMillis;
        }

        public long getDepartureTimeMillis() {
            return departureTimeMillis;
        }

        public Integer getDelay(){
            return delay;
        }

    }

    private class StopPathTestClass {
        private String stopId;
        private int gtfsStopSeq;
        private int configRev = -1;
        private String stopPathId = null;
        private boolean lastStopInTrip = false;
        private String routeId = null;
        private boolean layoverStop = false;
        private boolean waitStop = false;
        private boolean scheduleAdherenceStop = false;
        private Integer breakTime = null;
        private Double maxDistance = null;
        private Double maxSpeed = null;

        public StopPathTestClass(String stopId, int gtfsStopSeq) {
            this.stopId = stopId;
            this.gtfsStopSeq = gtfsStopSeq;
        }

        public String getStopId() {
            return stopId;
        }

        public int getGtfsStopSeq() {
            return gtfsStopSeq;
        }

        public int getConfigRev() {
            return configRev;
        }

        public String getStopPathId() {
            return stopPathId;
        }

        public boolean isLastStopInTrip() {
            return lastStopInTrip;
        }

        public String getRouteId() {
            return routeId;
        }

        public boolean isLayoverStop() {
            return layoverStop;
        }

        public boolean isWaitStop() {
            return waitStop;
        }

        public boolean isScheduleAdherenceStop() {
            return scheduleAdherenceStop;
        }

        public Integer getBreakTime() {
            return breakTime;
        }

        public Double getMaxDistance() {
            return maxDistance;
        }

        public Double getMaxSpeed() {
            return maxSpeed;
        }
    }

}
