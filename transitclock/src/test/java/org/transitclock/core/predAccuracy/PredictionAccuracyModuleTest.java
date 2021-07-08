package org.transitclock.core.predAccuracy;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.transitclock.db.structs.*;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.gtfs.TitleFormatter;
import org.transitclock.gtfs.gtfsStructs.GtfsRoute;
import org.transitclock.gtfs.gtfsStructs.GtfsStop;
import org.transitclock.gtfs.gtfsStructs.GtfsTrip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PredictionAccuracyModuleTest {
    @Mock
    private GtfsData gtfsData;


    @Before
    public void setup(){
        gtfsData = mock(GtfsData.class);
        when(gtfsData.isTripPatternIdAlreadyUsed(any())).thenReturn(false);
        when(gtfsData.getStop(any())).thenReturn(getStop());
    }

    @Test
    public void testGetRoutesAndStops(){
        PredictionAccuracyModule predictionAccuracyModule = new PredictionAccuracyModule("1");
        List<Route> routes = new ArrayList<>();
        String routeId = "A";
        TitleFormatter formatter = getTitleFormatter();
        List<TripPattern> tripPatterns = getLongestTripPatternsForRoute(routeId, formatter);

        Route route = getRoute(routeId, tripPatterns, formatter);

        Route spyRoute = spy(route);
        doReturn(Arrays.asList("0","1")).when(spyRoute).getDirectionIds();
        doReturn(tripPatterns.get(0)).when(spyRoute).getLongestTripPatternForDirection("0");
        doReturn(tripPatterns.get(1)).when(spyRoute).getLongestTripPatternForDirection("1");

        routes.add(spyRoute);

        List<PredictionAccuracyModule.RouteAndStops> routeAndStops = predictionAccuracyModule.getRoutesAndStops(routes);

        assertEquals("A", routeAndStops.get(0).routeId);

        // test to confirm number of stops on longest trip pattern for dir 0
        assertEquals(5, routeAndStops.get(0).stopIds.get("0").size());

        // test to confirm number of stops on longest trip pattern for dir 1
        assertEquals(4, routeAndStops.get(0).stopIds.get("1").size());

    }

    @Test
    public void testGetAllRoutesAndStops(){
        PredictionAccuracyModule predictionAccuracyModule = new PredictionAccuracyModule("1");

        List<Route> routes = new ArrayList<>();
        String routeId = "A";
        TitleFormatter formatter = getTitleFormatter();
        List<TripPattern> tripPatterns = getAllTripPatterns(routeId, formatter);

        List<TripPattern> tripPatternsDir0 = getAllTripPatternsForRouteDir0(routeId, formatter);
        List<TripPattern> tripPatternsDir1 = getAllTripPatternsForRouteDir1(routeId, formatter);

        Route route = getRoute(routeId, tripPatterns, formatter);

        Route spyRoute = spy(route);
        doReturn(Arrays.asList("0","1")).when(spyRoute).getDirectionIds();
        doReturn(tripPatternsDir0).when(spyRoute).getTripPatterns("0");
        doReturn(tripPatternsDir1).when(spyRoute).getTripPatterns("1");

        routes.add(spyRoute);

        List<PredictionAccuracyModule.RouteAndStops> routeAndStops = predictionAccuracyModule.getAllRoutesAndStops(routes);

        assertEquals("A", routeAndStops.get(0).routeId);

        // test to confirm number of unique stops for all trip patterns for dir 0
        // first trip pattern has two stops {0, 1}
        // second trip pattern has three stops {1, 2, 3}
        // stopIds should contain union of all trip patterns in direction 0
        assertEquals(4, routeAndStops.get(0).stopIds.get("0").size());

        // test to confirm number of unique stops for all trip patterns for dir 1
        // first trip pattern has two stops {0, 1}
        // second trip pattern has three stops {2, 3, 4}
        // stopIds should contain union of all trip patterns in direction 1
        assertEquals(5, routeAndStops.get(0).stopIds.get("1").size());
    }

    private TitleFormatter getTitleFormatter(){
        return new TitleFormatter("", false);
    }

    private List<TripPattern> getLongestTripPatternsForRoute(String routeId, TitleFormatter formatter){
        List<TripPattern> tripPatternsForRoute = new ArrayList<>();

        TripPattern tripPattern0 = getTripPattern("0", routeId, 0, 5, formatter);
        TripPattern tripPattern1 = getTripPattern("1", routeId, 10, 4, formatter);

        tripPatternsForRoute.add(tripPattern0);
        tripPatternsForRoute.add(tripPattern1);

        return tripPatternsForRoute;
    }

    private List<TripPattern> getAllTripPatterns(String routeId, TitleFormatter formatter){
        List<TripPattern> tripPatternsForRoute = new ArrayList<>();
        tripPatternsForRoute.addAll(getAllTripPatternsForRouteDir0(routeId, formatter));
        tripPatternsForRoute.addAll(getAllTripPatternsForRouteDir1(routeId, formatter));
        return tripPatternsForRoute;
    }

    // Get Trip Patterns that have Overlapping Stops in direction 0
    private List<TripPattern> getAllTripPatternsForRouteDir0(String routeId, TitleFormatter formatter){
        List<TripPattern> tripPatternsForRoute = new ArrayList<>();

        TripPattern tripPattern0a = getTripPattern("0", routeId, 0, 2, formatter);
        TripPattern tripPattern0b = getTripPattern("0", routeId, 1, 3, formatter);

        tripPatternsForRoute.add(tripPattern0a);
        tripPatternsForRoute.add(tripPattern0b);

        return tripPatternsForRoute;
    }

    // Get Trip Patterns that have no overlapping Stops in direction 1
    private List<TripPattern> getAllTripPatternsForRouteDir1(String routeId, TitleFormatter formatter){
        List<TripPattern> tripPatternsForRoute = new ArrayList<>();

        TripPattern tripPattern1a = getTripPattern("1", routeId, 0, 2, formatter);
        TripPattern tripPattern1b = getTripPattern("1", routeId, 2, 3, formatter);

        tripPatternsForRoute.add(tripPattern1a);
        tripPatternsForRoute.add(tripPattern1b);

        return tripPatternsForRoute;
    }


    private Route getRoute(String routeId, List<TripPattern> tripPatterns, TitleFormatter formatter){
        GtfsRoute gtfsRoute = getGtfsRoute(routeId);
        return new Route(-1,gtfsRoute, tripPatterns, formatter);
    }

    private GtfsRoute getGtfsRoute(String routeId){
        String agencyId = "1";
        String routeShortName = "A";
        String routeLongName = "A";
        String routeType = "routeType";
        String routeColor = "color";
        String routeTextColor = "routeTextColor";
        return new GtfsRoute(routeId, agencyId, routeShortName, routeLongName, routeType, routeColor,
                routeTextColor);
    }

    private TripPattern getTripPattern(String directionId, String routeId, int stopIndexStart, int numberOfStops, TitleFormatter formatter){
        Trip trip = getTrip(directionId, formatter, routeId);
        List<StopPath> stopPathsDir = getStopPaths(routeId, stopIndexStart,numberOfStops);
        return new TripPattern(-1, "shapeId", stopPathsDir, trip, gtfsData);
    }

    private Trip getTrip(String directionId, TitleFormatter formatter, String routeId){
        String routeShortName = routeId;
        String headsign = "headsign";
        GtfsTrip gtfsTrip = getGtfsTrip(routeId, directionId, headsign);

        return new Trip(-1, gtfsTrip, routeId, routeShortName, headsign, formatter);
    }

    private GtfsTrip getGtfsTrip(String routeId, String directionId, String headsign){
        String serviceId = "serviceId";
        String tripId = "tripId";
        String tripShortName = "tripA";
        String blockId = "blockId";
        String shapeId = "shapeId";

        return new GtfsTrip(routeId, serviceId, tripId, headsign, tripShortName, directionId, blockId, shapeId);
    }

    private List<StopPath> getStopPaths(String routeId, int indexStart, int numberOfStops){
        List<StopPath> stopPaths = new ArrayList<>();
        for(int i=indexStart; i < indexStart + numberOfStops; i++){
            stopPaths.add(new StopPath(-1, null, Integer.toString(i), i, false, routeId,
                    false, false, false, null, null,
                    null));
        }
        return stopPaths;
    }

    private Stop getStop(){
        return new Stop(-1, getGtfsStop(), 12345, getTitleFormatter());
    }

    private GtfsStop getGtfsStop(){
        return new GtfsStop("1", 12345, "stop1", 38.272689,-98.964961);
    }
}
