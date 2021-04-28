package org.transitclock.reporting.service;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.ServiceType;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.query.RunTimeForRouteQuery;
import org.transitclock.db.structs.*;
import org.transitclock.ipc.data.*;
import org.transitclock.reporting.*;
import org.transitclock.reporting.dao.RunTimeRoutesDao;
import org.transitclock.reporting.keys.TripConfigRevKey;

import javax.inject.Inject;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.transitclock.ipc.util.GtfsDbDataUtil.getRouteShortName;

public class RunTimeServiceV2 {

    private static final Logger logger =
            LoggerFactory.getLogger(RunTimeServiceV2.class);

    @Inject
    private RunTimeRoutesDao dao;

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

    /**
     *
     * @param beginDate
     * @param endDate
     * @param beginTime
     * @param endTime
     * @param serviceType
     * @param earlyThreshold
     * @param lateThreshold
     * @param readOnly
     * @return
     * @throws Exception
     */
    public Map<String, List<Long>> getAllRunTimesForTrips(LocalDate beginDate,
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

        Map<String, List<Long>> runTimesByTripId = getRunTimesByTripId(runTimesForRoutes);

        return runTimesByTripId;
    }

    private Map<String, List<Long>> getRunTimesByTripId(List<RunTimesForRoutes> runTimesForRoutes) {
        Map<String, List<Long>> runTimesByTripId = new HashMap<>();
        for(RunTimesForRoutes runTimesForRoute : runTimesForRoutes){
            if(runTimesForRoute.getRunTime() != null){
                String tripId =  runTimesForRoute.getTripId();
                Long runTime = runTimesForRoute.getRunTime();
                List<Long> runTimes = runTimesByTripId.get(runTimesByTripId.get(tripId));
                if(runTime != null){
                    if(runTimes == null){
                        runTimes = new ArrayList<>();
                        runTimesByTripId.put(tripId, runTimes);
                    }
                    runTimes.add(runTime);
                }
            }
        }
        return runTimesByTripId;
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
    public IpcRunTimeForTripsAndDistribution getRunTimeForTripsByStopPath(LocalDate beginDate,
                                                                                LocalDate endDate,
                                                                                LocalTime beginTime,
                                                                                LocalTime endTime,
                                                                                String routeIdOrShortName,
                                                                                String headsign,
                                                                                String tripPatternId,
                                                                                ServiceType serviceType,
                                                                                boolean readOnly) throws Exception {

        String routeShortName = getRouteShortName(routeIdOrShortName);

        Integer beginTimeSeconds = beginTime != null ? beginTime.toSecondOfDay() : null;
        Integer endTimeSeconds = endTime != null ? endTime.toSecondOfDay(): null;

        RunTimeForRouteQuery.Builder rtBuilder = new RunTimeForRouteQuery.Builder();
        RunTimeForRouteQuery rtQuery = rtBuilder
                .beginDate(beginDate)
                .endDate(endDate)
                .beginTime(beginTimeSeconds)
                .endTime(endTimeSeconds)
                .serviceType(serviceType)
                .routeShortName(routeShortName)
                .headsign(headsign)
                .tripPatternId(tripPatternId)
                .includeRunTimesForStops(true)
                .readOnly(readOnly)
                .build();

        List<RunTimesForRoutes> runTimesForRoutes = dao.getRunTimesForRoutes(rtQuery);

        Map<String, TripStopPathStatisticsV2> routeStatisticsByRouteName = processTripStatsMap(runTimesForRoutes);

        List<IpcRunTimeForTrip> ipcRunTimeForTripList = getRunTimeStatsForTripsByStopPath(routeStatisticsByRouteName);

        Map<String, List<Long>> runTimesByTripId = getRunTimesByTripId(runTimesForRoutes);

        IpcRunTimeForTripsAndDistribution ipcRunTimeForTripsAndDistribution = new IpcRunTimeForTripsAndDistribution(
                ipcRunTimeForTripList, runTimesByTripId);

        return ipcRunTimeForTripsAndDistribution;
    }

    private Map<String, TripStopPathStatisticsV2> processTripStatsMap(List<RunTimesForRoutes> runTimesForRoutes) {
        Map<String, TripStopPathStatisticsV2> tripStatsByTripId = new HashMap<>();
        Map<TripConfigRevKey, Trip> cachedTrips = new HashMap<>();

        for(RunTimesForRoutes rt : runTimesForRoutes){
            for(RunTimesForStops runTimesForStops : rt.getRunTimesForStops()){
                TripStopPathStatisticsV2 tripStats = getTripStats(tripStatsByTripId, cachedTrips, rt.getConfigRev(), runTimesForStops);
                tripStats.addStopPathRunTime(runTimesForStops);
                tripStats.addStopPathDwellTime(runTimesForStops);
            }
        }

        return tripStatsByTripId;
    }

    private TripStopPathStatisticsV2 getTripStats(Map<String, TripStopPathStatisticsV2> tripStatsByTripId,
                                                  Map<TripConfigRevKey, Trip> cachedTrips,
                                                  Integer configRev,
                                                  RunTimesForStops runTimesForStops){

        String tripId = runTimesForStops.getRunTimesForRoutes().getTripId();
        TripStopPathStatisticsV2 tripStatistics = tripStatsByTripId.get(tripId);
        if(tripStatistics == null){
            Trip trip = getTripFromCache(cachedTrips, tripId, configRev);
            tripStatistics = new TripStopPathStatisticsV2(trip, runTimesForStops.getRunTimesForRoutes().getNextTripStartTime());
            tripStatsByTripId.put(tripId, tripStatistics);
        }
        return tripStatistics;
    }

    private Trip getTripFromCache(Map<TripConfigRevKey, Trip> cachedTrips, String tripId, Integer configRev){
        TripConfigRevKey key = new TripConfigRevKey(tripId, configRev);
        Trip trip = cachedTrips.get(key);
        if(trip == null){
            Session session = HibernateUtils.getSession(true);
            try{
                trip = Trip.getTrip(session, configRev, tripId);
            }catch (HibernateException e){
                logger.error("Unable to retrieve trip {}", tripId, e);
            }finally{
                if(session != null){
                    session.close();
                }
            }

        }
        return trip;
    }

    private List<IpcRunTimeForTrip> getRunTimeStatsForTripsByStopPath(Map<String, TripStopPathStatisticsV2> tripStatsByTripId){

        List<IpcRunTimeForTrip> ipcRunTimeForTrips = new ArrayList<>();


        // Loop through each TripStats grouped by Trip Id
        for(Map.Entry<String, TripStopPathStatisticsV2> tripStatEntry : tripStatsByTripId.entrySet()){

            TripStopPathStatisticsV2 tripStatistics = tripStatEntry.getValue();

            //Validation -- Make sure trip is complete
            if(invalidTripStatistics(tripStatistics)){
               continue;
            }


            Double medianRunTime = tripStatistics.getTripMedianRunTime();
            Double fixedTime = tripStatistics.getTripFixedRunTime();
            Double dwellTime = tripStatistics.getTripMedianDwellTime();
            Double variableTime = null;

            // At minimum need median runTime and dwellTime to have complete runTime
            if(medianRunTime == null || dwellTime == null){
                continue;
            }

            if(medianRunTime != null && fixedTime != null) {
                variableTime = medianRunTime - fixedTime;
            }

            // Get Current Trip and Next Trip Info
            Trip currentTrip = tripStatistics.getTrip();
            Integer nextTripStartTime = tripStatistics.getNextTripTime();


            ipcRunTimeForTrips.add(new IpcRunTimeForTrip(currentTrip.getId(),
                    currentTrip.getStartTime(),
                    currentTrip.getEndTime(),
                    nextTripStartTime,
                    medianRunTime,
                    fixedTime,
                    variableTime,
                    dwellTime));
        }

        return ipcRunTimeForTrips;
    }

    private boolean invalidTripStatistics(TripStopPathStatisticsV2 tripStatistics) {
        return !tripStatistics.hasAllStopPathsForRunTimes() || !tripStatistics.hasAllStopPathsForDwellTimes();
    }
}
