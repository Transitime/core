package org.transitclock.core.reporting;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.transitclock.core.ServiceType;
import org.transitclock.core.ServiceUtils;
import org.transitclock.core.travelTimes.DataFetcher;
import org.transitclock.db.structs.*;


import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;


public class RunTimeLoaderTest {


    @Test
    public void testRunTimesOne(){

        TestTrip testTripOne = getTestTripOne();
        RunTimeWriter writer = new TestRunTimeWriterImpl();
        RunTimeCache cache = new RunTimeCacheImpl();

        RunTimeLoader loader = getRunTimeLoader(testTripOne, writer, cache, ServiceType.WEEKDAY);
        loader.run(null, getArrivalsDeparturesOne(testTripOne));

        /**
         * Tests for RunTimesForRoutes
         * */

        // Start Time is NULL because no valid departure record, should throw away instead
        RunTimesForRoutes runTimesForRoutes = cache.getOrCreate(testTripOne.getConfigRev(),testTripOne.getTripId(),null, "1003");

        // Check to see if RunTimesForRoutes is valid
        // In this case it is not valid because it does not have a valid start time
        Assert.assertFalse(cache.isValid(runTimesForRoutes));

        // Check that values for RunTimesForRoutes is what we expect
        Assert.assertNull(runTimesForRoutes.getStartTime());
        Assert.assertEquals(testTripOne.getServiceId(), runTimesForRoutes.getServiceId());
        Assert.assertEquals(testTripOne.getTripId(), runTimesForRoutes.getTripId());
        Assert.assertEquals(testTripOne.getConfigRev(), runTimesForRoutes.getConfigRev());
        Assert.assertEquals(testTripOne.getRouteShortName(), runTimesForRoutes.getRouteShortName());
        Assert.assertEquals(testTripOne.getDirectionId(), runTimesForRoutes.getDirectionId());

        /**
         * Tests for RunTimesForStops
         * */

        // Check that values for RunTimesForStops is what we expect
        Assert.assertNotNull(runTimesForRoutes.getRunTimesForStops());
        Assert.assertEquals(1, runTimesForRoutes.getRunTimesForStops().size());

        RunTimesForStops runTimesForStops = runTimesForRoutes.getRunTimesForStops().get(0);
        Assert.assertTrue(runTimesForStops.getLastStop());
        Assert.assertEquals(9, runTimesForStops.getStopPathIndex());
        Assert.assertEquals(6, runTimesForStops.getConfigRev());
    }

    private TestTrip getTestTripOne(){
        return new TestTrip.Builder()
                .configRev(6)
                .tripId("6673089")
                .serviceId("21")
                .directionId("1")
                .routeShortName("TRE")
                .tripPatternId("shape_133438_28252_to_22748_dabd4fca")
                .headSign("E - TRE - UNION STATION")
                .startTime(44460)
                .endTime(48120)
                .build();
    }

    private RunTimeLoader getRunTimeLoader(TestTrip testTrip, RunTimeWriter writer, RunTimeCache cache, ServiceType serviceType){
        ServiceUtils serviceUtils = mock(ServiceUtils.class);
        when(serviceUtils.getServiceTypeForTrip(any(), any(), any())).thenReturn(serviceType);

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

        MockedStatic<Trip> staticTrip = Mockito.mockStatic(Trip.class);
        staticTrip.when(() -> Trip.getTrip(null, testTrip.getConfigRev(), testTrip.getTripId())).thenReturn(trip);

        return new RunTimeLoader(writer,
                cache,
                null,
                serviceUtils);
    }

    private Map<DataFetcher.DbDataMapKey, List<ArrivalDeparture>> getArrivalsDeparturesOne(TestTrip testTrip){

        Map<DataFetcher.DbDataMapKey, List< ArrivalDeparture >> arrivalsDeparturesMap = new HashMap<>();

        List<ArrivalDeparture> arrivalDepartures = new ArrayList<>();

        TestArrivalDepartureBuilder.Builder adBuilder = new TestArrivalDepartureBuilder.Builder(true);
        ArrivalDeparture arrivalOne = adBuilder
                .configRev(6)
                .vehicleId("1003")
                .time(new Date(1620666483679l))
                .avlTime(new Date(1620666493000l))
                .block(null)
                .tripIndex(6)
                .stopPathIndex(7)
                .freqStartTime(null)
                .stopPathId("28174_to_28172")
                .build();

        TestArrivalDepartureBuilder.Builder adBuilder2 = new TestArrivalDepartureBuilder.Builder(true);
        ArrivalDeparture arrivalTwo = adBuilder2
                .configRev(6)
                .vehicleId("1003")
                .time(new Date(1620667196865l))
                .avlTime(new Date(1620667211000l))
                .block(null)
                .tripIndex(6)
                .stopPathIndex(9)
                .freqStartTime(null)
                .stopPathId("28264_to_22748")
                .build();

        ArrivalDeparture spyArrivalOne = spy(arrivalOne);
        doReturn(testTrip.getTripId()).when(spyArrivalOne).getTripId();

        ArrivalDeparture spyArrivalTwo = spy(arrivalTwo);
        doReturn(testTrip.getTripId()).when(spyArrivalTwo).getTripId();

        arrivalDepartures.add(spyArrivalOne);
        arrivalDepartures.add(spyArrivalTwo);

        DataFetcher dataFetcher = new DataFetcher(TimeZone.getTimeZone("America/Chicago"));
        DataFetcher.DbDataMapKey key = dataFetcher.getKey(arrivalOne.getServiceId(), arrivalOne.getDate(),
                arrivalOne.getTripId(), arrivalOne.getVehicleId());

        arrivalsDeparturesMap.put(key, arrivalDepartures);
        return arrivalsDeparturesMap;
    }

}
