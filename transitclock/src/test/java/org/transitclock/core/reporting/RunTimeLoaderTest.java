package org.transitclock.core.reporting;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.transitclock.core.ServiceType;
import org.transitclock.core.ServiceUtilsImpl;
import org.transitclock.core.travelTimes.DataFetcher;
import org.transitclock.db.structs.*;


import java.io.*;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;


public class RunTimeLoaderTest {

    private static Map<String, TestTrip> trips = new HashMap<>();
    private static Map<DataFetcher.DbDataMapKey, List<ArrivalDeparture>> arrivalsDeparturesMap;

    private static void loadTestTrips() throws IOException {
        MockedStatic<Trip> staticTrip = mockStatic(Trip.class);
        InputStream inputStream = RunTimeLoaderTest.class.getClassLoader().getResourceAsStream("reporting/runtime/trips.csv");
        Reader in = new BufferedReader(new InputStreamReader(inputStream));
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in);
        for (CSVRecord record : records) {
            String tripId = record.get("tripId");
            TestTrip testTrip = new TestTrip.Builder()
                    .configRev(Integer.parseInt(record.get("configRev")))
                    .tripId(record.get("tripId"))
                    .serviceId(record.get("serviceId"))
                    .directionId(record.get("directionId"))
                    .routeShortName(record.get("routeShortName"))
                    .tripPatternId(record.get("tripPatternId"))
                    .headSign(record.get("headsign"))
                    .startTime(Integer.parseInt(record.get("startTime")))
                    .endTime(Integer.parseInt(record.get("endTime")))
                    .build();
            trips.put(tripId, testTrip);
            mockTrip(testTrip, staticTrip);
        }
    }

    private static void loadTestArrivalsDepartures() throws IOException {
        List<ArrivalDeparture> arrivalsDepartures = new ArrayList<>();
        InputStream inputStream = RunTimeLoaderTest.class.getClassLoader().getResourceAsStream("reporting/runtime/arrivals_departures.csv");
        Reader in = new BufferedReader(new InputStreamReader(inputStream));
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in);
        for (CSVRecord record : records) {
            ArrivalDeparture arrivalDeparture = new TestArrivalDeparture.Builder(Boolean.parseBoolean("true"))
                    .configRev(Integer.parseInt(record.get("configRev")))
                    .vehicleId(record.get("vehicleId"))
                    .time(new Date(Long.parseLong(record.get("time"))))
                    .avlTime(new Date(Long.parseLong(record.get("avlTime"))))
                    .block(null)
                    .tripIndex(Integer.parseInt(record.get("tripIndex")))
                    .stopPathIndex(Integer.parseInt(record.get("stopPathIndex")))
                    .freqStartTime(null)
                    .stopPathId(record.get("stopPathId"))
                    .build();
            ArrivalDeparture spyArrivalDeparture = spy(arrivalDeparture);
            String tripId = record.get("tripId");
            doReturn(tripId).when(spyArrivalDeparture).getTripId();
            arrivalsDepartures.add(spyArrivalDeparture);
        }
        arrivalsDeparturesMap = getArrivalDepartureToMap(arrivalsDepartures);
    }

    private static void mockTrip(TestTrip testTrip, MockedStatic<Trip> staticTrip){
        Trip trip = mock(Trip.class, Mockito.RETURNS_DEEP_STUBS);
        when(trip.getConfigRev()).thenReturn(testTrip.getConfigRev());
        when(trip.getId()).thenReturn(testTrip.getTripId());
        when(trip.getServiceId()).thenReturn(testTrip.getServiceId());
        when(trip.getDirectionId()).thenReturn(testTrip.getDirectionId());
        when(trip.getRouteShortName()).thenReturn(testTrip.getRouteShortName());
        when(trip.getTripPattern().getId()).thenReturn(testTrip.getTripPatternId());
        when(trip.getHeadsign()).thenReturn(testTrip.getHeadSign());
        when(trip.getStartTime()).thenReturn(testTrip.getStartTime());
        when(trip.getEndTime()).thenReturn(testTrip.getEndTime());

        staticTrip.when(() -> Trip.getTrip(null, testTrip.getConfigRev(), testTrip.getTripId())).thenReturn(trip);
    }

    private static Map<DataFetcher.DbDataMapKey, List<ArrivalDeparture>> getArrivalDepartureToMap(List<ArrivalDeparture> arrivalsDepartures){
        Map<DataFetcher.DbDataMapKey, List< ArrivalDeparture >> arrivalsDeparturesMap = new HashMap<>();
        DataFetcher dataFetcher = new DataFetcher(TimeZone.getTimeZone("America/Chicago"));
        for(ArrivalDeparture ad : arrivalsDepartures){
            ArrivalDeparture spyArrivalOne = spy(ad);
            doReturn(ad.getTripId()).when(spyArrivalOne).getTripId();
            dataFetcher.addArrivalDepartureToMap(arrivalsDeparturesMap, ad);
        }

        return arrivalsDeparturesMap;
    }

    @BeforeClass
    public static void setup() throws IOException {
        loadTestTrips();
        loadTestArrivalsDepartures();
    }

    @Test
    public void testRunTimesWithDuplicates(){

        for(DataFetcher.DbDataMapKey key : arrivalsDeparturesMap.keySet()){
            String tripId = key.getTripId();
            String vehicleId = key.getVehicleId();

            TestTrip testTrip = trips.get(tripId);
            RunTimeWriter writer = new TestRunTimeWriterImpl();
            RunTimeCache cache = new RunTimeCacheImpl();

            RunTimeLoader loader = getRunTimeLoader(writer, cache, ServiceType.WEEKDAY);

            loader.run(null, arrivalsDeparturesMap);

            /**
             * Tests for RunTimesForRoutes
             * */

            // Start Time is NULL because no valid departure record, should throw away instead
            RunTimesForRoutes runTimesForRoutes = cache.getOrCreate(testTrip.getConfigRev(),testTrip.getTripId(),null, vehicleId);

            // Check to see if RunTimesForRoutes is valid
            // In this case it is not valid because it does not have a valid start time
            Assert.assertFalse(cache.isValid(runTimesForRoutes));

            // Check that values for RunTimesForRoutes is what we expect
            Assert.assertNull(runTimesForRoutes.getStartTime());
            Assert.assertEquals(testTrip.getServiceId(), runTimesForRoutes.getServiceId());
            Assert.assertEquals(testTrip.getTripId(), runTimesForRoutes.getTripId());
            Assert.assertEquals(testTrip.getConfigRev(), runTimesForRoutes.getConfigRev());
            Assert.assertEquals(testTrip.getRouteShortName(), runTimesForRoutes.getRouteShortName());
            Assert.assertEquals(testTrip.getDirectionId(), runTimesForRoutes.getDirectionId());

            /**
             * Tests for RunTimesForStops
             * */

            // Check for duplicate stops
            Assert.assertTrue(cache.containsDuplicateStops(runTimesForRoutes));

            // Check that values for RunTimesForStops is what we expect
            Assert.assertNotNull(runTimesForRoutes.getRunTimesForStops());

        }
    }

    private RunTimeLoader getRunTimeLoader(RunTimeWriter writer, RunTimeCache cache, ServiceType serviceType){
        ServiceUtilsImpl serviceUtils = mock(ServiceUtilsImpl.class);
        when(serviceUtils.getServiceTypeForTrip(any(), any())).thenReturn(serviceType);

        return new RunTimeLoader(writer,
                cache,
                null,
                serviceUtils);
    }

}
