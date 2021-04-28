package org.transitclock.reporting.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.ServiceType;
import org.transitclock.core.TemporalDifference;
import org.transitclock.core.travelTimes.TravelTimesProcessor;
import org.transitclock.db.query.ArrivalDepartureQuery;
import org.transitclock.db.query.RunTimeForRouteQuery;
import org.transitclock.db.query.TripQuery;
import org.transitclock.db.structs.*;
import org.transitclock.ipc.data.*;
import org.transitclock.reporting.RouteStatistics;
import org.transitclock.reporting.dao.RunTimeRoutesDao;
import org.transitclock.reporting.keys.StopPathRunTimeKey;
import org.transitclock.reporting.StopPathStatistics;
import org.transitclock.reporting.TripStopPathStatisticsV1;
import org.transitclock.ipc.data.IpcDoubleSummaryStatistics;
import org.transitclock.ipc.data.IpcRunTime;
import org.transitclock.ipc.data.IpcRunTimeForTrip;
import org.transitclock.reporting.keys.ArrivalDepartureTripKey;
import org.transitclock.reporting.keys.TripDateKey;

import javax.inject.Inject;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.transitclock.configData.ReportingConfig.*;
import static org.transitclock.ipc.util.GtfsDbDataUtil.*;

public class RunTimeService {

    private static final Logger logger =
            LoggerFactory.getLogger(RunTimeService.class);

    @Inject
    private RunTimeRoutesDao dao;

    /**
     * Get avg run time for all trips in a route for a specified period of time
     *
     * @param beginDate
     * @param endDate
     * @param beginTime
     * @param endTime
     * @param routeIdOrShortName
     * @param serviceType
     * @param timePointsOnly
     * @param headsign
     * @param readOnly
     * @return
     * @throws Exception
     */
    public IpcDoubleSummaryStatistics getAverageRunTime(LocalDate beginDate,
                                                        LocalDate endDate,
                                                        LocalTime beginTime,
                                                        LocalTime endTime,
                                                        String routeIdOrShortName,
                                                        ServiceType serviceType,
                                                        boolean timePointsOnly,
                                                        String headsign,
                                                        boolean readOnly) throws Exception {

        String routeShortName = getRouteShortName(routeIdOrShortName);

        ArrivalDepartureQuery.Builder adBuilder = new ArrivalDepartureQuery.Builder();
        ArrivalDepartureQuery adQuery = adBuilder
                .beginDate(beginDate)
                .endDate(endDate)
                .beginTime(beginTime)
                .endTime(endTime)
                .routeShortName(routeShortName)
                .headsign(headsign)
                .serviceType(serviceType)
                .timePointsOnly(timePointsOnly)
                .scheduledTimesOnly(true)
                .includeTrip(true)
                .readOnly(readOnly)
                .build();

        List<ArrivalDeparture> arrivalDepartures = ArrivalDeparture.getArrivalsDeparturesFromDb(adQuery);

        Map<TripDateKey, Long> runTimeByTripId = getRunTimeByTripDateKey(arrivalDepartures);

        return getAverageRunTimeForAllTrips(runTimeByTripId);
    }

    /*
     * Calculate the trip run times from the list of arrival departures
     * Currently only gets run times for trips with arrival/departure for first and last stops
     * Trips that do not include first and last stop in the selected time range GET CUT OFF
     *
     * TODO - May want to refactor to be smarter about including the entire trip if
     *  the trip is part of the selected time range
     */
    private Map<TripDateKey, Long> getRunTimeByTripDateKey(List<ArrivalDeparture> arrivalDepartures){

        Map<TripDateKey, Long> runTimeByTripDate = new LinkedHashMap<>();

        Map<TripDateKey, List<ArrivalDeparture>> arrivalDeparturesByTripDateKey = getArrivalDeparturesByTripDateKey(arrivalDepartures);

        for(Map.Entry<TripDateKey, List<ArrivalDeparture>> arrivalDepartureByTripAndDate : arrivalDeparturesByTripDateKey.entrySet()){
            TripDateKey key = arrivalDepartureByTripAndDate.getKey();
            List<ArrivalDeparture> arrivalDeparturesForTrip  = arrivalDepartureByTripAndDate.getValue();

            if(arrivalDeparturesForTrip != null && arrivalDeparturesForTrip.size() > 0){
                Long firstStopDepartureTime = null;
                Long lastStopArrivalTime = null;

                Trip trip = arrivalDeparturesForTrip.get(0).getTripFromDb();
                int lastStopPathIndex = trip.getNumberStopPaths() - 1;

                ArrivalDeparture firstDeparture = arrivalDeparturesForTrip.get(0);
                if(firstDeparture.getStopPathIndex() == 0 && firstDeparture.isDeparture()){
                    firstStopDepartureTime = firstDeparture.getTime();
                } else {
                    continue;
                }

                for(int i=1; i< arrivalDeparturesForTrip.size(); i++){
                    ArrivalDeparture ad = arrivalDeparturesForTrip.get(i);
                    if(ad.getStopPathIndex() == lastStopPathIndex && ad.isArrival()){
                        lastStopArrivalTime = ad.getTime();
                        break;
                    }
                }

                if(firstStopDepartureTime != null && lastStopArrivalTime != null){
                    runTimeByTripDate.put(key,lastStopArrivalTime - firstStopDepartureTime);
                }
            }
        }

        return runTimeByTripDate;
    }

    /*
     *  Groups Arrival Departures by Trip/Date key
     *  Allows us to retrieve first and last arrival/departure per trip
     */
    private Map<TripDateKey, List<ArrivalDeparture>> getArrivalDeparturesByTripDateKey(List<ArrivalDeparture> arrivalDepartures){
        Map<TripDateKey, List<ArrivalDeparture>> arrivalDeparturesByTripKey = new LinkedHashMap<>();

        for(ArrivalDeparture ad : arrivalDepartures){
            LocalDate localDate = Instant.ofEpochMilli(ad.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
            TripDateKey tripKey = new TripDateKey(ad.getTripId(), localDate);
            if(arrivalDeparturesByTripKey.get(tripKey) == null){
                arrivalDeparturesByTripKey.put(tripKey, new ArrayList<>());
            }
            arrivalDeparturesByTripKey.get(tripKey).add(ad);
        }
        return arrivalDeparturesByTripKey;
    }

    /*
     * Get summary statistics for trip/date run times
     */
    private IpcDoubleSummaryStatistics getAverageRunTimeForAllTrips(Map<TripDateKey, Long> runTimeByTripId) {
        DoubleSummaryStatistics summaryStatistics =  runTimeByTripId.entrySet().stream()
                .mapToDouble(Map.Entry::getValue)
                .summaryStatistics();

        return new IpcDoubleSummaryStatistics(summaryStatistics);
    }


    public IpcRunTime getRunTimeSummary(LocalDate beginDate,
                                        LocalDate endDate,
                                        LocalTime beginTime,
                                        LocalTime endTime,
                                        String routeIdOrShortName,
                                        String headsign,
                                        String startStop,
                                        String endStop,
                                        ServiceType serviceType,
                                        boolean timePointsOnly,
                                        boolean currentTripsOnly,
                                        String agencyId,
                                        boolean readOnly) throws Exception {

        String routeShortName = getRouteShortName(routeIdOrShortName);

        ArrivalDepartureQuery.Builder adBuilder = new ArrivalDepartureQuery.Builder();
        ArrivalDepartureQuery adQuery = adBuilder
                .beginDate(beginDate)
                .endDate(endDate)
                .beginTime(beginTime)
                .endTime(endTime)
                .routeShortName(routeShortName)
                .headsign(headsign)
                .startStop(startStop)
                .endStop(endStop)
                .serviceType(serviceType)
                .timePointsOnly(timePointsOnly)
                .includeTrip(true)
                .readOnly(readOnly)
                .build();

        List<ArrivalDeparture> arrivalDepartures = ArrivalDeparture.getArrivalsDeparturesFromDb(adQuery);

        Set<Integer> configRevs = new HashSet<>();
        Map<ArrivalDepartureTripKey, List<ArrivalDeparture>> arrivalDeparturesByTripMap =
                groupArrivalDeparturesByUniqueTrip(arrivalDepartures, agencyId);

        Collection<List<ArrivalDeparture>> arrivalDeparturesByTrip = arrivalDeparturesByTripMap.values();

        Map<String, TripStopPathStatisticsV1> tripStatsByRunTimeKey = new HashMap<>();
        for(List<ArrivalDeparture> arrivalDepartureList : arrivalDeparturesByTrip){
            processTripStatsMap(tripStatsByRunTimeKey, arrivalDepartureList);
        }

        IpcRunTime runTime = getRunTimeStats(tripStatsByRunTimeKey);
        return runTime;
    }

    private Map<ArrivalDepartureTripKey, List<ArrivalDeparture>> groupArrivalDeparturesByUniqueTrip(
            List<ArrivalDeparture> arrivalDepartures,
            String agencyId){

        TimeZone tz = Agency.getTimeZoneFromDb(agencyId);
        Map<ArrivalDepartureTripKey, List<ArrivalDeparture>> arrivalDeparturesByTrip = new HashMap<>();

        for(ArrivalDeparture ad: arrivalDepartures){
            ArrivalDepartureTripKey key = ArrivalDepartureTripKey.getKey(ad, tz.toZoneId());
            List<ArrivalDeparture> list = arrivalDeparturesByTrip.get(key);
            if (list == null) {
                list = new ArrayList<>();
                arrivalDeparturesByTrip.put(key, list);
            }
            list.add(ad);
        }
        return arrivalDeparturesByTrip;
    }

    /**
     * Store TripStatistics in map with tripId as key
     * This is done by going through list of ArrivalsDepartures grouped by TripId and populating stopPath info
     * for TripStastics. Its keyed off Trip so it will collect run time information for all trips that share the same
     * trip Id. This is inspired by TravelTimesProcessor.
     * @param tripStatsByTripId
     * @param arrDepList
     */
    private void processTripStatsMap(Map<String, TripStopPathStatisticsV1> tripStatsByTripId,
                                     List<ArrivalDeparture> arrDepList) {

        for (int i=0; i<arrDepList.size()-1; ++i) {
            ArrivalDeparture arrDep1 = arrDepList.get(i);

            // Deal with normal travel times
            ArrivalDeparture arrDep2 = arrDepList.get(i+1);
            processTravelTimeBetweenTwoArrivalDepartures(tripStatsByTripId, arrDep1, arrDep2);
        }
    }

    /**
     * Processes travel time and dwell time between two arrival departures
     * Stores them in TripStatistics
     * @param tripStatsByTripId
     * @param arrDep1
     * @param arrDep2
     */
    private void processTravelTimeBetweenTwoArrivalDepartures(Map<String, TripStopPathStatisticsV1> tripStatsByTripId,
                                                              ArrivalDeparture arrDep1,
                                                              ArrivalDeparture arrDep2) {

        Double travelTimeForStopPath = null;
        Double dwellTime = null;
        long departureTime = arrDep1.getTime();

        if (TravelTimesProcessor.shouldResetEarlyTerminalDepartures()
                && arrDep1.isDeparture()
                && arrDep1.getStopPathIndex() == 0
                && arrDep1.getTime() < arrDep1.getScheduledTime()) {
            logger.debug("Note: for {} using scheduled departure time instead "
                            + "of the calculated departure time since "
                            + "transitclock.travelTimes.resetEarlyTerminalDepartures is "
                            + "true and the departure time was (likely incorrectly) "
                            + "calculated to be before the scheduled departure time",
                    arrDep1);
            departureTime = arrDep1.getScheduledTime();
        }

        // If schedule adherence is really far off then ignore the data
        // point because it would skew the results.
        TemporalDifference schedAdh = arrDep1.getScheduleAdherence();
        if (schedAdh == null)
            schedAdh = arrDep2.getScheduleAdherence();
        if (schedAdh != null
                && !schedAdh.isWithinBounds(getMaxSchedAdh(),
                getMaxSchedAdh())) {
            // Schedule adherence is off so don't use this data
            return ;
        }

        // If looking at arrival and departure for same stop then continue
        if (arrDep1.getStopPathIndex() == arrDep2.getStopPathIndex()
                && arrDep1.isArrival()
                && arrDep2.isDeparture()) {
            if(arrDep1.getStopPathIndex() == 0){
                long expectedDepartureTime = arrDep2.getScheduledTime() >= arrDep1.getTime() ? arrDep2.getScheduledTime() : arrDep1.getTime();
                double firstDwellTime = (double) (arrDep2.getTime() - expectedDepartureTime);
                // check for case where trip left before expected departure time
                if(firstDwellTime < 0){
                    dwellTime = 1000.0;
                } else {
                    dwellTime = firstDwellTime;
                }
            }else {
                dwellTime = (double) (arrDep2.getTime() - arrDep1.getTime());
            }
            if (dwellTime == null)
                return;
            addStopPathDwellTimeToMap(tripStatsByTripId, arrDep2, dwellTime);
        }

        // If looking at departure from one stop to the arrival time at the
        // very next stop then can determine the travel times between the stops.
        else if (arrDep1.getStopPathIndex() - arrDep2.getStopPathIndex() != 1
                && arrDep1.isDeparture()
                && arrDep2.isArrival()) {
            // Determine the travel times and add them to the map
            travelTimeForStopPath = (double) (arrDep2.getTime() - departureTime);

            // Ignore a stop path if any segment travel time is negative. Nulls will
            // be ignored downstream anyway so can also ignore those.
            if (travelTimeForStopPath == null)
                return;

            addStopPathRunTimeToMap(tripStatsByTripId, arrDep2, travelTimeForStopPath);
        }
    }

    private void addStopPathRunTimeToMap(Map<String, TripStopPathStatisticsV1> tripStatsByTripId,
                                         ArrivalDeparture ad,
                                         Double runTime){
        TripStopPathStatisticsV1 tps = getTripStats(tripStatsByTripId, ad);
        tps.addStopPathRunTime(ad, runTime);
    }

    private void addStopPathDwellTimeToMap(Map<String, TripStopPathStatisticsV1> tripStatsByTripId,
                                           ArrivalDeparture ad,
                                           Double dwellTime){
        TripStopPathStatisticsV1 tps = getTripStats(tripStatsByTripId, ad);
        tps.addStopPathDwellTime(ad, dwellTime);
    }

    private TripStopPathStatisticsV1 getTripStats(Map<String, TripStopPathStatisticsV1> tripStatsByTripId,
                                                  ArrivalDeparture ad){
        String key = ad.getTripId();
        TripStopPathStatisticsV1 tps = tripStatsByTripId.get(key);
        if(tps == null){
            tps = new TripStopPathStatisticsV1(ad.getTripFromDb(), ad.getTripIndex());
            tripStatsByTripId.put(key, tps);
        }
        return tps;
    }

    private IpcRunTime getRunTimeStats(Map<String, TripStopPathStatisticsV1> tripStatsByRunTimeKey){
        DoubleSummaryStatistics avgRunTimeStats = new DoubleSummaryStatistics();
        DoubleSummaryStatistics fixedTimeStats = new DoubleSummaryStatistics();
        DoubleSummaryStatistics avgDwellTimeStats = new DoubleSummaryStatistics();

        for(Map.Entry<String, TripStopPathStatisticsV1> entry : tripStatsByRunTimeKey.entrySet()){

            Double avgTripRunTime = entry.getValue().getTripAverageRunTime();
            if(avgTripRunTime != null && avgTripRunTime > 0){
                avgRunTimeStats.accept(avgTripRunTime);
            }

            Double tripFixedTime = entry.getValue().getTripFixedRunTime();
            if(tripFixedTime != null && tripFixedTime > 0){
                fixedTimeStats.accept(tripFixedTime);
            }

            Double tripDwellTime = entry.getValue().getTripAvgDwellTime();
            if(tripDwellTime != null && tripDwellTime > 0){
                avgDwellTimeStats.accept(tripDwellTime);
            }

        }

        Double avgRunTime = avgRunTimeStats.getCount() > 0 ? avgRunTimeStats.getAverage() : null;
        Double fixedTime = fixedTimeStats.getCount() > 0 ? fixedTimeStats.getMin() : null;
        Double dwellTime = avgDwellTimeStats.getCount() > 0 ? avgDwellTimeStats.getAverage() : null;
        Double variableTime = null;


        if(avgRunTime != null && fixedTime != null) {
            variableTime = avgRunTime - fixedTime;
        }

        return new IpcRunTime(avgRunTime, fixedTime, variableTime, dwellTime);
    }

    /**
     * Get RunTime for Group of Trips
     * @param beginDate
     * @param endDate
     * @param beginTime
     * @param endTime
     * @param routeIdOrShortName
     * @param headsign
     * @param tripPatternId
     * @param serviceType
     * @param timePointsOnly
     * @param currentTripsOnly
     * @param agencyId
     * @param readOnly
     * @return
     * @throws Exception
     */
    public List<IpcRunTimeForTrip> getRunTimeForTrips(LocalDate beginDate,
                                                      LocalDate endDate,
                                                      LocalTime beginTime,
                                                      LocalTime endTime,
                                                      String routeIdOrShortName,
                                                      String headsign,
                                                      String tripPatternId,
                                                      ServiceType serviceType,
                                                      boolean timePointsOnly,
                                                      boolean currentTripsOnly,
                                                      String agencyId,
                                                      boolean readOnly) throws Exception {

        String routeShortName = getRouteShortName(routeIdOrShortName);

        // Step 1 - Get Relevant Trips for Specified Time Range Grouped By Trip Id

        Map<String, Trip> scheduledTripsGroupedById = getScheduledTripsGroupedByIdForDateRange(routeShortName,
                                                            headsign,beginDate,endDate,beginTime,endTime,readOnly);

        if(scheduledTripsGroupedById == null || scheduledTripsGroupedById.size() == 0){
            return null;
        }

        // Step 2 - Get Arrival/Departures filtering by tripIds specified in Step 1
        ArrivalDepartureQuery.Builder adBuilder = new ArrivalDepartureQuery.Builder();
        ArrivalDepartureQuery adQuery = adBuilder
                .beginDate(beginDate)
                .endDate(endDate)
                .beginTime(beginTime)
                .endTime(endTime)
                .routeShortName(routeShortName)
                .headsign(headsign)
                .tripIds(scheduledTripsGroupedById.keySet())
                .tripPatternId(tripPatternId)
                .serviceType(serviceType)
                .timePointsOnly(timePointsOnly)
                .includeTrip(true)
                .readOnly(readOnly)
                .build();

        List<ArrivalDeparture> arrivalDepartures = ArrivalDeparture.getArrivalsDeparturesFromDb(adQuery);


        if(arrivalDepartures.size() > 0) {

            Map<ArrivalDepartureTripKey, List<ArrivalDeparture>> arrivalDeparturesByTripMap =
                    groupArrivalDeparturesByUniqueTrip(arrivalDepartures, agencyId);


            Collection<List<ArrivalDeparture>> arrivalDeparturesByTrip = arrivalDeparturesByTripMap.values();

            Map<String, TripStopPathStatisticsV1> tripStatsByTripId = new HashMap<>();
            for (List<ArrivalDeparture> arrivalDepartureList : arrivalDeparturesByTrip) {
                processTripStatsMap(tripStatsByTripId, arrivalDepartureList);
            }

            List<IpcRunTimeForTrip> runTime = getRunTimeStatsForTrips(tripStatsByTripId);

            return runTime;
        }
        return Collections.EMPTY_LIST;
    }


    private Map<String, Trip> getScheduledTripsGroupedByIdForDateRange(String routeShortName,
                                                      String headSign,
                                                      LocalDate beginDate,
                                                      LocalDate endDate,
                                                      LocalTime beginTime,
                                                      LocalTime endTime,
                                                      boolean readOnly){

        LocalDateTime configRevEndDate =  LocalDateTime.of(endDate, endTime);
        List<ConfigRevision> configRevisions = ConfigRevision.getConfigRevisionsForMaxDate(configRevEndDate, readOnly);

        Set<Integer> configRevisionIds = configRevisions.stream()
                                            .map(c -> c.getConfigRev())
                                            .collect(Collectors.toSet());


        TripQuery.Builder tripBuilder = new TripQuery.Builder(routeShortName, configRevisionIds);
        tripBuilder.headsign(headSign);
        tripBuilder.firstStartTime(beginTime);
        tripBuilder.lastStartTime(endTime);

        List<Trip> scheduledTrips = Trip.getTripsFromDb(tripBuilder.build());

        Map<String, Trip> scheduledTripsGroupedById = scheduledTrips.stream()
                                                        .collect(Collectors.toMap(Trip::getId, Function.identity()));

        return scheduledTripsGroupedById;
    }

    private List<IpcRunTimeForTrip> getRunTimeStatsForTrips(Map<String, TripStopPathStatisticsV1> tripStatsByTripId){

        List<IpcRunTimeForTrip> ipcRunTimeForTrips = new ArrayList<>();


        // Loop through each TripStats grouped by Trip Id
        for(Map.Entry<String, TripStopPathStatisticsV1> tripStatEntry : tripStatsByTripId.entrySet()){

            TripStopPathStatisticsV1 tripStatistics = tripStatEntry.getValue();

            //Validation -- Make sure trip is complete
            if(!tripStatistics.hasAllStopPathsForRunTimes() || !tripStatistics.hasAllStopPathsForDwellTimes()){
                continue;
            }

            ////////////////////////////////////////////////


            // Looping through the stoppath stats
            Map<StopPathRunTimeKey, StopPathStatistics> stopPathStatsMap = tripStatistics.getAllStopPathStatistics();

            for(Map.Entry<StopPathRunTimeKey, StopPathStatistics> stopPathEntry : stopPathStatsMap.entrySet()){
                // Combining stop path values
                StopPathStatistics sps = stopPathStatsMap.get(stopPathEntry.getKey());
                if(sps != null){
                    sps.getRunTimeStats().combine(stopPathEntry.getValue().getRunTimeStats());
                    sps.getDwellTimeStats().combine(stopPathEntry.getValue().getDwellTimeStats());
                }
            }

            DoubleSummaryStatistics avgRunTimeStats = new DoubleSummaryStatistics();
            DoubleSummaryStatistics fixedTimeStats = new DoubleSummaryStatistics();
            DoubleSummaryStatistics avgDwellTimeStats = new DoubleSummaryStatistics();

            Double avgTripRunTime = tripStatistics.getTripAverageRunTime();
            if(avgTripRunTime != null && avgTripRunTime > 0){
                avgRunTimeStats.accept(avgTripRunTime);
            }

            Double tripFixedTime = tripStatistics.getTripFixedRunTime();
            if(tripFixedTime != null && tripFixedTime > 0){
                fixedTimeStats.accept(tripFixedTime);
            }

            Double tripDwellTime = tripStatistics.getTripAvgDwellTime();
            if(tripDwellTime != null && tripDwellTime > 0){
                avgDwellTimeStats.accept(tripDwellTime);
            }

            Double avgRunTime = avgRunTimeStats.getCount() > 0 ? avgRunTimeStats.getAverage() : null;
            Double fixedTime = fixedTimeStats.getCount() > 0 ? fixedTimeStats.getMin() : null;
            Double dwellTime = avgDwellTimeStats.getCount() > 0 ? avgDwellTimeStats.getAverage() : null;
            Double variableTime = null;

            if(avgRunTime != null && fixedTime != null) {
                variableTime = avgRunTime - fixedTime;
            }

            // Get Current Trip and Next Trip Info
            Trip currentTrip = tripStatistics.getTrip();
            Trip nextTrip = null;

            // Commenting out for now - takes too long to lookup
            //Trip nextTrip = currentTrip.getBlockFromDb(readOnly).getTripFromDb(tripStatistics.getTripIndex() + 1, readOnly);

            Integer nextTripStartTime = null;
            if(nextTrip != null){
                nextTripStartTime = nextTrip.getStartTime();
            }

            ipcRunTimeForTrips.add(new IpcRunTimeForTrip(currentTrip.getId(),
                                                         currentTrip.getStartTime(),
                                                         currentTrip.getEndTime(),
                                                         nextTripStartTime,
                                                         avgRunTime,
                                                         fixedTime,
                                                         variableTime,
                                                         dwellTime));
        }

        return ipcRunTimeForTrips;
    }

    /**
     * Get RunTime Information for Trip Stop Paths
     * @param beginDate
     * @param endDate
     * @param beginTime
     * @param endTime
     * @param routeIdOrShortName
     * @param tripId
     * @param serviceType
     * @param timePointsOnly
     * @param agencyId
     * @param readOnly
     * @return
     */
    public List<IpcRunTimeForStopPath> getRunTimeForStopPaths(LocalDate beginDate,
                                                          LocalDate endDate,
                                                          LocalTime beginTime,
                                                          LocalTime endTime,
                                                          String routeIdOrShortName,
                                                          String tripId,
                                                          ServiceType serviceType,
                                                          boolean timePointsOnly,
                                                          String agencyId,
                                                          boolean readOnly) throws Exception {

        String routeShortName = getRouteShortName(routeIdOrShortName);

        // Step 1 - Get Relevant Trips for Specified Time Range Grouped By Trip Id

        Set<String> tripIds = Stream.of(tripId).collect(Collectors.toCollection(HashSet::new));

        // Step 2 - Get Arrival/Departures filtering by tripIds specified in Step 1
        ArrivalDepartureQuery.Builder adBuilder = new ArrivalDepartureQuery.Builder();
        ArrivalDepartureQuery adQuery = adBuilder
                .beginDate(beginDate)
                .endDate(endDate)
                .beginTime(beginTime)
                .endTime(endTime)
                .routeShortName(routeShortName)
                .tripIds(tripIds)
                .serviceType(serviceType)
                .timePointsOnly(timePointsOnly)
                .includeTrip(true)
                .readOnly(readOnly)
                .build();

        List<ArrivalDeparture> arrivalDepartures = ArrivalDeparture.getArrivalsDeparturesFromDb(adQuery);

        if(arrivalDepartures.size() > 0) {

            Map<ArrivalDepartureTripKey, List<ArrivalDeparture>> arrivalDeparturesByTripMap =
                    groupArrivalDeparturesByUniqueTrip(arrivalDepartures, agencyId);


            Collection<List<ArrivalDeparture>> arrivalDeparturesByTrip = arrivalDeparturesByTripMap.values();

            Map<String, TripStopPathStatisticsV1> tripStatsByTripId = new HashMap<>();
            for (List<ArrivalDeparture> arrivalDepartureList : arrivalDeparturesByTrip) {
                processTripStatsMap(tripStatsByTripId, arrivalDepartureList);
            }

            List<IpcRunTimeForStopPath> runTime = getRunTimeStatsForStopPaths(tripStatsByTripId);

            return runTime;
        }

        return null;
    }

    private List<IpcRunTimeForStopPath> getRunTimeStatsForStopPaths(Map<String, TripStopPathStatisticsV1> tripStatsByTripId){

        List<IpcRunTimeForStopPath> ipcRunTimeForStopPaths = new ArrayList<>();


        // Loop through each TripStats grouped by Trip Id
        for(Map.Entry<String, TripStopPathStatisticsV1> tripStatEntry : tripStatsByTripId.entrySet()){

            TripStopPathStatisticsV1 tripStatistics = tripStatEntry.getValue();

            //Validation -- Make sure trip is complete
            if(!tripStatistics.hasAllStopPathsForRunTimes() || !tripStatistics.hasAllStopPathsForDwellTimes()){
                continue;
            }

            // Looping through the stoppath stats
            Map<StopPathRunTimeKey, StopPathStatistics> stopPathStatsMap = tripStatistics.getAllStopPathStatistics();

            for(Map.Entry<StopPathRunTimeKey, StopPathStatistics> stopPathEntry : stopPathStatsMap.entrySet()){

                StopPathRunTimeKey stopPathRunTimeKey = stopPathEntry.getKey();
                StopPathStatistics stopPathStatistics = stopPathEntry.getValue();

                DoubleSummaryStatistics avgRunTimeStats = new DoubleSummaryStatistics();
                DoubleSummaryStatistics fixedTimeStats = new DoubleSummaryStatistics();
                DoubleSummaryStatistics avgDwellTimeStats = new DoubleSummaryStatistics();

                Double avgStopPathRunTime = stopPathStatistics.getAverageRunTime();
                if(avgStopPathRunTime != null && avgStopPathRunTime > 0){
                    avgRunTimeStats.accept(avgStopPathRunTime);
                }

                Double stopPathFixedTime = stopPathStatistics.getMinRunTime();
                if(stopPathFixedTime != null && stopPathFixedTime > 0){
                    fixedTimeStats.accept(stopPathFixedTime);
                }

                Double stopPathDwellTime = stopPathStatistics.getAverageDwellTime();
                if(stopPathDwellTime != null && stopPathDwellTime > 0){
                    avgDwellTimeStats.accept(stopPathDwellTime);
                }

                Double avgRunTime = avgRunTimeStats.getCount() > 0 ? avgRunTimeStats.getAverage() : null;
                Double fixedTime = fixedTimeStats.getCount() > 0 ? fixedTimeStats.getMin() : null;
                Double dwellTime = avgDwellTimeStats.getCount() > 0 ? avgDwellTimeStats.getAverage() : null;
                Double variableTime = null;

                if(avgRunTime != null && fixedTime != null) {
                    variableTime = avgRunTime - fixedTime;
                }


                ipcRunTimeForStopPaths.add(
                        new IpcRunTimeForStopPath(
                            stopPathRunTimeKey.getStopPathId(),
                            stopPathRunTimeKey.getStopPathIndex(),
                            null,
                            null,
                            avgRunTime,
                            fixedTime,
                            variableTime,
                            dwellTime
                        )
                );

            }


        }

        return ipcRunTimeForStopPaths;
    }

    /**
     * Get RunTime Information for Trip Stop Paths
     * @param beginDate
     * @param endDate
     * @param beginTime
     * @param endTime
     * @param serviceType
     * @param readOnly
     * @return
     */
    public List<IpcRunTimeForRoute> getRunTimeForRoutes(LocalDate beginDate,
                                                           LocalDate endDate,
                                                           LocalTime beginTime,
                                                           LocalTime endTime,
                                                           ServiceType serviceType,
                                                           Integer earlyThreshold,
                                                           Integer lateThreshold,
                                                           boolean readOnly) throws Exception {


        Integer beginTimeSeconds = beginTime != null ? beginTime.toSecondOfDay() : null;
        Integer endTimeSeconds = endTime != null ? endTime.toSecondOfDay(): null;

        RunTimeForRouteQuery.Builder rtBuilder = new RunTimeForRouteQuery.Builder();
        RunTimeForRouteQuery rtQuery = rtBuilder
                .beginDate(beginDate)
                .endDate(endDate)
                .beginTime(beginTimeSeconds)
                .endTime(endTimeSeconds)
                .serviceType(serviceType)
                .includeRunTimesForStops(false)
                .readOnly(readOnly)
                .build();

        List<RunTimesForRoutes> runTimesForRoutes = dao.getRunTimesForRoutes(rtQuery);

        Map<String, RouteStatistics> routeStatisticsByRouteName = getRouteRunTimeStatistics(runTimesForRoutes, earlyThreshold, lateThreshold);

        List<IpcRunTimeForRoute> ipcRunTimeForRoutes = getRunTimeStatsForRoutes(routeStatisticsByRouteName);

        return ipcRunTimeForRoutes;
    }

    private Map<String, RouteStatistics> getRouteRunTimeStatistics(List<RunTimesForRoutes> runTimesForRoutes,
                                                                   Integer earlyThresholdSec,
                                                                   Integer lateThresholdSec) {
        Map<String, RouteStatistics> routeStatisticsByRouteName = new HashMap<>();
        for(RunTimesForRoutes rt : runTimesForRoutes){
            if(rt.getRouteShortName() == null || rt.getEndTime() == null || rt.getStartTime() == null ||
                    rt.getScheduledStartTime() == null || rt.getScheduledEndTime() == null){
                continue;
            }
            RouteStatistics routeStatistics  = routeStatisticsByRouteName.get(rt.getRouteShortName());
            if(routeStatistics == null){
                routeStatistics = new RouteStatistics();
                routeStatisticsByRouteName.put(rt.getRouteShortName(), routeStatistics);
            }
            long runTimeSec = TimeUnit.MILLISECONDS.toSeconds(rt.getEndTime().getTime() - rt.getStartTime().getTime());
            int scheduledRunTimeSec = rt.getScheduledEndTime() - rt.getScheduledStartTime();
            long runTimeDiff = runTimeSec - scheduledRunTimeSec;
            if(runTimeDiff < 0 && Math.abs(runTimeDiff) > earlyThresholdSec){
                routeStatistics.addEarly();
            }
            else if(runTimeDiff > 0 && runTimeDiff > lateThresholdSec){
                routeStatistics.addLate();
            }
            else {
                routeStatistics.addOnTime();
            }
        }

        return routeStatisticsByRouteName;
    }


    private List<IpcRunTimeForRoute> getRunTimeStatsForRoutes(Map<String, RouteStatistics> routeRunTimeStatistics) {
        List<IpcRunTimeForRoute> ipcRunTimeForRoutes = new ArrayList<>();
        for(Map.Entry<String, RouteStatistics> rtStats : routeRunTimeStatistics.entrySet()){
            String routeName = rtStats.getKey();
            RouteStatistics stats = rtStats.getValue();
            ipcRunTimeForRoutes.add(new IpcRunTimeForRoute(routeName, stats.getEarlyCount(), stats.getOnTimeCount(),
                    stats.getLateCount()));

        }
        return ipcRunTimeForRoutes;
    }
}
