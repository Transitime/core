package org.transitclock.ipc.servers.reporting.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.ServiceType;
import org.transitclock.core.TemporalDifference;
import org.transitclock.core.travelTimes.TravelTimesProcessor;
import org.transitclock.db.query.ArrivalDepartureQuery;
import org.transitclock.db.reporting.StopPathRunTimeKey;
import org.transitclock.db.reporting.StopPathStatistics;
import org.transitclock.db.reporting.TripStatistics;
import org.transitclock.db.reporting.TripStatisticsForConfigRev;
import org.transitclock.db.structs.Agency;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Trip;
import org.transitclock.ipc.data.IpcDoubleSummaryStatistics;
import org.transitclock.ipc.data.IpcRunTime;
import org.transitclock.ipc.data.IpcRunTimeForTrip;
import org.transitclock.ipc.servers.reporting.keys.ArrivalDepartureTripKey;
import org.transitclock.ipc.servers.reporting.keys.TripDateKey;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.transitclock.configData.ReportingConfig.*;
import static org.transitclock.ipc.util.GtfsDbDataUtil.*;

public class RunTimeService {

    private static final Logger logger =
            LoggerFactory.getLogger(RunTimeService.class);

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
    public IpcDoubleSummaryStatistics getAverageRunTime(LocalDate beginDate, LocalDate endDate,
                                                        LocalTime beginTime, LocalTime endTime, String routeIdOrShortName,
                                                        ServiceType serviceType, boolean timePointsOnly,
                                                        String headsign, boolean readOnly) throws Exception {

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


        Map<TripDateKey, Long> runTimeByTripId = getRunTimeByTripId(arrivalDepartures);

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
    private Map<TripDateKey, Long> getRunTimeByTripId(List<ArrivalDeparture> arrivalDepartures){

        Map<TripDateKey, Long> runTimeByTripId = new LinkedHashMap<>();
        Map<TripDateKey, List<ArrivalDeparture>> arrivalDeparturesByTripKey = getArrivalDeparturesByTripKey(arrivalDepartures);

        for(Map.Entry<TripDateKey, List<ArrivalDeparture>> adByTrip : arrivalDeparturesByTripKey.entrySet()){
            TripDateKey key =adByTrip.getKey();
            List<ArrivalDeparture> arrivalDeparturesForTrip  = adByTrip.getValue();
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
                    runTimeByTripId.put(key,lastStopArrivalTime - firstStopDepartureTime);
                }
            }
        }

        return runTimeByTripId;
    }

    /*
     *  Groups Arrival Departures by Trip/Date key
     *  Allows us to retrieve first and last arrival/departure per trip
     */
    private Map<TripDateKey, List<ArrivalDeparture>> getArrivalDeparturesByTripKey(List<ArrivalDeparture> arrivalDepartures){
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


    public IpcRunTime getRunTimeSummary(LocalDate beginDate, LocalDate endDate,
                                        LocalTime beginTime, LocalTime endTime,
                                        String routeIdOrShortName, String headsign,
                                        String startStop, String endStop,
                                        ServiceType serviceType, boolean timePointsOnly,
                                        boolean currentTripsOnly, String agencyId, boolean readOnly) throws Exception {

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
                groupArrivalDeparturesByUniqueTrip(arrivalDepartures, configRevs, agencyId);
        Collection<List<ArrivalDeparture>> arrivalDeparturesByTrip = arrivalDeparturesByTripMap.values();

        Map<String, TripStatistics> tripStatsByRunTimeKey = new HashMap<>();
        for(List<ArrivalDeparture> arrivalDepartureList : arrivalDeparturesByTrip){
            processTripStatsMap(tripStatsByRunTimeKey, arrivalDepartureList);
        }

        IpcRunTime runTime = getRunTimeStats(tripStatsByRunTimeKey);
        return runTime;
    }

    private Map<ArrivalDepartureTripKey, List<ArrivalDeparture>> groupArrivalDeparturesByUniqueTrip(
            List<ArrivalDeparture> arrivalDepartures,
            Set<Integer> configRevs,
            String agencyId){
        TimeZone tz = Agency.getTimeZoneFromDb(agencyId);
        Map<ArrivalDepartureTripKey, List<ArrivalDeparture>> arrivalDeparturesByTrip = new HashMap<>();

        for(ArrivalDeparture ad: arrivalDepartures){
            ArrivalDepartureTripKey key = ArrivalDepartureTripKey.getKey(ad, tz.toZoneId());
            List<ArrivalDeparture> list = arrivalDeparturesByTrip.get(key);
            if (list == null) {
                list = new ArrayList<>();
                arrivalDeparturesByTrip.put(key, list);
                configRevs.add(ad.getConfigRev());
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
    private void processTripStatsMap(Map<String, TripStatistics> tripStatsByTripId, List<ArrivalDeparture> arrDepList) {

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
    private void processTravelTimeBetweenTwoArrivalDepartures(Map<String, TripStatistics> tripStatsByTripId,
                                                              ArrivalDeparture arrDep1, ArrivalDeparture arrDep2) {

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

    private void addStopPathRunTimeToMap(Map<String, TripStatistics> tripStatsByTripId, ArrivalDeparture ad,
                                         Double runTime){
        TripStatistics tps = getTripStats(tripStatsByTripId, ad);
        tps.addStopPathRunTime(ad, runTime);
    }

    private void addStopPathDwellTimeToMap(Map<String, TripStatistics> tripStatsByTripId, ArrivalDeparture ad,
                                           Double dwellTime){
        TripStatistics tps = getTripStats(tripStatsByTripId, ad);
        tps.addStopPathDwellTime(ad, dwellTime);
    }

    private TripStatistics getTripStats(Map<String, TripStatistics> tripStatsByTripId, ArrivalDeparture ad){
        String key = ad.getTripId();
        TripStatistics tps = tripStatsByTripId.get(key);
        if(tps == null){
            tps = new TripStatistics(ad.getTripId());
            tripStatsByTripId.put(key, tps);
        }
        return tps;
    }

    private IpcRunTime getRunTimeStats(Map<String, TripStatistics> tripStatsByRunTimeKey){
        DoubleSummaryStatistics avgRunTimeStats = new DoubleSummaryStatistics();
        DoubleSummaryStatistics fixedTimeStats = new DoubleSummaryStatistics();
        DoubleSummaryStatistics avgDwellTimeStats = new DoubleSummaryStatistics();

        for(Map.Entry<String, TripStatistics> entry : tripStatsByRunTimeKey.entrySet()){

            for(TripStatisticsForConfigRev tps: entry.getValue().getAllTripStatisticsGroupedByConfigRev()){
                Double avgTripRunTime = tps.getTripAverageRunTime();
                if(avgTripRunTime != null && avgTripRunTime > 0){
                    avgRunTimeStats.accept(avgTripRunTime);
                }

                Double tripFixedTime = tps.getTripFixedRunTime();
                if(tripFixedTime != null && tripFixedTime > 0){
                    fixedTimeStats.accept(tripFixedTime);
                }

                Double tripDwellTime = tps.getAvgDwellTime();
                if(tripDwellTime != null && tripDwellTime > 0){
                    avgDwellTimeStats.accept(tripDwellTime);
                }
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


    public List<IpcRunTimeForTrip> getRunTimeForTrips(LocalDate beginDate, LocalDate endDate,
                                                      LocalTime beginTime, LocalTime endTime,
                                                      String routeIdOrShortName, String headsign,
                                                      String startStop, String endStop,
                                                      ServiceType serviceType, boolean timePointsOnly,
                                                      boolean currentTripsOnly, String agencyId, boolean readOnly) throws Exception {

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

        if(arrivalDepartures.size() > 0) {

            Set<Integer> configRevs = new HashSet<>();
            Map<ArrivalDepartureTripKey, List<ArrivalDeparture>> arrivalDeparturesByTripMap =
                    groupArrivalDeparturesByUniqueTrip(arrivalDepartures, configRevs, agencyId);
            Collection<List<ArrivalDeparture>> arrivalDeparturesByTrip = arrivalDeparturesByTripMap.values();

            Map<String, TripStatistics> tripStatsByTripId = new HashMap<>();
            for (List<ArrivalDeparture> arrivalDepartureList : arrivalDeparturesByTrip) {
                processTripStatsMap(tripStatsByTripId, arrivalDepartureList);
            }

            List<Trip> trips = Trip.getTripsFromDb(routeShortName, headsign, configRevs, readOnly);
            if (trips != null) {
                Map<String, List<Trip>> tripsByConfigRev = trips.stream().collect(Collectors.groupingBy(Trip::getId));
                List<IpcRunTimeForTrip> runTime = getRunTimeStatsForTrips(tripStatsByTripId, tripsByConfigRev, readOnly);
                return runTime;
            }

        }
        return Collections.EMPTY_LIST;
    }

    private List<IpcRunTimeForTrip> getRunTimeStatsForTrips(Map<String, TripStatistics> tripStatsByTripId,
                                                            Map<String, List<Trip>> tripsByConfigRev,
                                                            boolean readOnly){

        List<IpcRunTimeForTrip> ipcRunTimeForTrips = new ArrayList<>();

        // Loop through each tripId trip Stats
        for(Map.Entry<String, TripStatistics> tripStatEntry : tripStatsByTripId.entrySet()){

            TripStatistics tripStatistics = tripStatEntry.getValue();
            List<TripStatisticsForConfigRev> tripStatsForConfigRev = new ArrayList<>(tripStatistics.getAllTripStatisticsGroupedByConfigRev());

            // Identify first complete trip for tripId
            TripStatisticsForConfigRev mainTrip =  tripStatsForConfigRev.stream()
                    .filter(t -> t.hasAllStopPathsForRunTimes() && t.hasAllStopPathsForDwellTimes())
                    .findFirst()
                    .orElse(null);
            if(mainTrip == null) {
                continue;
            }

            Map<StopPathRunTimeKey, StopPathStatistics> stopPathStatsMap = mainTrip.getAllStopPathStatistics();

            // Looping through all the trip stats for each config rev
            for(int i=0; i < tripStatsForConfigRev.size(); i++){
                if(mainTrip.getConfigRev() == tripStatsForConfigRev.get(i).getConfigRev()){
                    continue;
                }

                // Looping through the stoppath stats
                Map<StopPathRunTimeKey, StopPathStatistics> map = tripStatsForConfigRev.get(i).getAllStopPathStatistics();
                for(Map.Entry<StopPathRunTimeKey, StopPathStatistics> stopPathEntry : map.entrySet()){
                    // Combining stop path values
                    StopPathStatistics sps = stopPathStatsMap.get(stopPathEntry.getKey());
                    if(sps != null){
                        sps.getRunTimeStats().combine(stopPathEntry.getValue().getRunTimeStats());
                        sps.getDwellTimeStats().combine(stopPathEntry.getValue().getDwellTimeStats());
                    }
                }

            }

            DoubleSummaryStatistics avgRunTimeStats = new DoubleSummaryStatistics();
            DoubleSummaryStatistics fixedTimeStats = new DoubleSummaryStatistics();
            DoubleSummaryStatistics avgDwellTimeStats = new DoubleSummaryStatistics();

            Double avgTripRunTime = mainTrip.getTripAverageRunTime();
            if(avgTripRunTime != null && avgTripRunTime > 0){
                avgRunTimeStats.accept(avgTripRunTime);
            }

            Double tripFixedTime = mainTrip.getTripFixedRunTime();
            if(tripFixedTime != null && tripFixedTime > 0){
                fixedTimeStats.accept(tripFixedTime);
            }

            Double tripDwellTime = mainTrip.getAvgDwellTime();
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

            List<Trip> trips = tripsByConfigRev.get(mainTrip.getTripId());
            if(trips != null){
                for(Trip trip : trips){
                    if(trip.getConfigRev() == mainTrip.getConfigRev()){
                        int currentTripIndex = mainTrip.getTripIndex();
                        Trip nextTrip = trip.getBlockFromDb(readOnly).getTripFromDb(currentTripIndex + 1, readOnly);
                        Integer nextTripStartTime = null;
                        if(nextTrip != null){
                            nextTripStartTime = nextTrip.getStartTime();
                        }
                        ipcRunTimeForTrips.add(new IpcRunTimeForTrip(mainTrip.getTripId(), trip.getStartTime(), trip.getEndTime(), nextTripStartTime, avgRunTime, fixedTime, variableTime, dwellTime));
                        break;
                    }

                }
            }
        }

        return ipcRunTimeForTrips;
    }



}
