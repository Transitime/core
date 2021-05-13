package org.transitclock.core.reporting;

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
    public void testOne(){

        ServiceUtils serviceUtils = mock(ServiceUtils.class);
        when(serviceUtils.getServiceTypeForTrip(any(), any(), any())).thenReturn(ServiceType.WEEKDAY);

        Trip trip = mock(Trip.class, Mockito.RETURNS_DEEP_STUBS);
        when(trip.getId()).thenReturn("6673089");
        when(trip.getServiceId()).thenReturn("21");
        when(trip.getDirectionId()).thenReturn("1");
        when(trip.getRouteShortName()).thenReturn("TRE");
        when(trip.getTripPattern().getId()).thenReturn("shape_133438_28252_to_22748_dabd4fca");
        when(trip.getHeadsign()).thenReturn("E - TRE - UNION STATION");
        when(trip.getStartTime()).thenReturn(44460);
        when(trip.getEndTime()).thenReturn(48120);

        MockedStatic<Trip> staticTrip = Mockito.mockStatic(Trip.class);
        staticTrip.when(() -> Trip.getTrip(null, 6, "6673089")).thenReturn(trip);


        RunTimeLoader loader = new RunTimeLoader(new TestRunTimeWriterImpl(),
                                                 new RunTimeCacheImpl(),
                                   null,
                                                 serviceUtils);

        loader.run(null, getArrivalsDeparturesOne());

    }

    private Map<DataFetcher.DbDataMapKey, List<ArrivalDeparture>> getArrivalsDeparturesOne(){

        Map<DataFetcher.DbDataMapKey, List< ArrivalDeparture >> arrivalsDeparturesMap = new HashMap<>();

       List<ArrivalDeparture> arrivalDepartures = new ArrayList<>();

        ArrivalDeparture arrivalOne = new Arrival(
                                        6,
                                                "1003",
                                                new Date(1620666483679l),
                                                new Date(1620666493000l),
                                                null,
                                                6,
                                                7,
                                                null,
                                                "28174_to_28172"
                                                 );

        ArrivalDeparture arrivalTwo = new Arrival(
                                                    6,
                                                    "1003",
                                                    new Date(1620667196865l),
                                                    new Date(1620667211000l),
                                                    null,
                                                    6,
                                                    9,
                                                    null,
                                                    "28264_to_22748"
                                                );

        ArrivalDeparture spyArrivalOne = spy(arrivalOne);
        doReturn("6673089").when(spyArrivalOne).getTripId();


        ArrivalDeparture spyArrivalTwo = spy(arrivalTwo);
        doReturn("6673089").when(spyArrivalTwo).getTripId();

        arrivalDepartures.add(spyArrivalOne);
        arrivalDepartures.add(spyArrivalTwo);

        DataFetcher dataFetcher = new DataFetcher(TimeZone.getTimeZone("America/Chicago"));
        DataFetcher.DbDataMapKey key = dataFetcher.getKey(arrivalOne.getServiceId(), arrivalOne.getDate(),
                arrivalOne.getTripId(), arrivalOne.getVehicleId());

        arrivalsDeparturesMap.put(key, arrivalDepartures);
        return arrivalsDeparturesMap;
    }

}
