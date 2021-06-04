package org.transitclock.reporting.service;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.FloatConfigValue;
import org.transitclock.core.ServiceType;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.query.RunTimeForRouteQuery;
import org.transitclock.db.structs.*;
import org.transitclock.ipc.data.*;
import org.transitclock.reporting.*;
import org.transitclock.reporting.dao.RunTimeRoutesDao;
import org.transitclock.reporting.keys.TripConfigRevKey;
import org.transitclock.statistics.RunTimeStatistics;

import javax.inject.Inject;
import java.time.*;
import java.util.*;

import static org.transitclock.ipc.util.GtfsDbDataUtil.getRouteShortName;

public class RunTimeService {

    private static final Logger logger = LoggerFactory.getLogger(RunTimeService.class);

    private static BooleanConfigValue filterRunTimeOutliers = new BooleanConfigValue(
            "transitclock.reporting.runTime.filterOutliers", true, "When set to true allows" +
            "filtering of runTime ouliers.");

    private static Boolean getFilterRunTimeOutliers(){
        return filterRunTimeOutliers.getValue();
    }

    @Inject
    private RunTimeRoutesDao dao;


    /**
     * Get summary information for RunTimes
     * @param beginDate
     * @param endDate
     * @param beginTime
     * @param endTime
     * @param routeIdOrShortName
     * @param headsign
     * @param tripPatternId
     * @param serviceType
     * @param readOnly
     * @return
     * @throws Exception
     */
    public IpcRunTime getRunTimeSummary(LocalDate beginDate,
                                        LocalDate endDate,
                                        LocalTime beginTime,
                                        LocalTime endTime,
                                        String routeIdOrShortName,
                                        String headsign,
                                        String directionId,
                                        String tripPatternId,
                                        ServiceType serviceType,
                                        boolean readOnly) throws Exception {

        String routeShortName = getRouteShortName(routeIdOrShortName);

        RunTimeForRouteQuery.Builder rtBuilder = new RunTimeForRouteQuery.Builder();
        RunTimeForRouteQuery rtQuery = rtBuilder
                .beginDate(beginDate)
                .endDate(endDate)
                .beginTime(beginTime)
                .endTime(endTime)
                .serviceType(serviceType)
                .routeShortName(routeShortName)
                .headsign(headsign)
                .directionId(directionId)
                .tripPatternId(tripPatternId)
                .includeRunTimesForStops(true)
                .readOnly(readOnly)
                .build();

        List<RunTimesForRoutes> runTimesForRoutes = getRunTimesForRoutes(rtQuery);

        Map<String, TripStopPathStatistics> tripStatisticsByTripId = processTripStatsMap(runTimesForRoutes);

        IpcRunTime runTime = getRunTimeStats(tripStatisticsByTripId);

        return runTime;
    }

    public List<RunTimesForRoutes> getRunTimesForRoutes(RunTimeForRouteQuery rtQuery) throws Exception {
        List<RunTimesForRoutes> runTimesForRoutes = dao.getRunTimesForRoutes(rtQuery);
        if(getFilterRunTimeOutliers()){
            return RunTimeStatistics.filterRunTimesForRoutes(runTimesForRoutes);
        }
        return runTimesForRoutes;
    }

    public IpcRunTime getRunTimeStats(Map<String, TripStopPathStatistics> tripStatsByRunTimeKey){

        DoubleStatistics avgRunTimeStats =  new DoubleStatistics();
        DoubleStatistics fixedTimeStats =  new DoubleStatistics();
        DoubleStatistics avgDwellTimeStats =  new DoubleStatistics();

        for(Map.Entry<String, TripStopPathStatistics> entry : tripStatsByRunTimeKey.entrySet()){

            Double avgTripRunTime = entry.getValue().getTripAverageRunTime();
            if(avgTripRunTime != null && avgTripRunTime > 0){
                avgRunTimeStats.add(avgTripRunTime);
            }

            Double tripFixedTime = entry.getValue().getTripFixedRunTime();
            if(tripFixedTime != null && tripFixedTime > 0){
                fixedTimeStats.add(tripFixedTime);
            }

            Double tripDwellTime = entry.getValue().getTripAvgDwellTime();
            if(tripDwellTime != null && tripDwellTime > 0){
                avgDwellTimeStats.add(tripDwellTime);
            }

        }

        Double fixedTime = fixedTimeStats.getCount() > 0 ? fixedTimeStats.getAverage() : null;
        Double dwellTime = avgDwellTimeStats.getCount() > 0 ? avgDwellTimeStats.getAverage() : null;
        Double avgRunTime = avgRunTimeStats.getCount() > 0 ? avgRunTimeStats.getAverage() : null;
        Double variableTime = null;


        if(avgRunTime != null && fixedTime != null) {
            variableTime = avgRunTime - fixedTime;
        }

        if(avgRunTime != null && dwellTime != null){
            avgRunTime += dwellTime;
        }

        return new IpcRunTime(avgRunTime, fixedTime, variableTime, dwellTime);
    }


    /**
     * Convert RunTimesForRoutes into TripStopPathStatistics
     * @param runTimesForRoutes
     * @return
     */
    public Map<String, TripStopPathStatistics> processTripStatsMap(List<RunTimesForRoutes> runTimesForRoutes) {
        Map<String, TripStopPathStatistics> tripStatsByTripId = new HashMap<>();
        Map<TripConfigRevKey, Trip> cachedTrips = new HashMap<>();

        try {
            for (RunTimesForRoutes rt : runTimesForRoutes) {
                for (RunTimesForStops runTimesForStops : rt.getRunTimesForStops()) {
                    TripStopPathStatistics tripStats = getOrCreateTripStats(tripStatsByTripId, cachedTrips,
                                rt.getConfigRev(), runTimesForStops);
                    tripStats.addStopPathRunTime(runTimesForStops);
                    tripStats.addStopPathDwellTime(runTimesForStops);
                }
            }
        } catch (Exception e){
            logger.error("Problem processing trip stats for RunTimesForRoutes", e);
        }

        return tripStatsByTripId;
    }

    private TripStopPathStatistics getOrCreateTripStats(Map<String, TripStopPathStatistics> tripStatsByTripId,
                                                        Map<TripConfigRevKey, Trip> cachedTrips,
                                                        Integer configRev,
                                                        RunTimesForStops runTimesForStops){

        String tripId = runTimesForStops.getRunTimesForRoutes().getTripId();
        TripStopPathStatistics tripStatistics = tripStatsByTripId.get(tripId);
        if(tripStatistics == null){
            Trip trip = getTripFromCache(cachedTrips, tripId, configRev);
            tripStatistics = new TripStopPathStatistics(trip, runTimesForStops.getRunTimesForRoutes().getNextTripStartTime());
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

    public boolean invalidTripStatistics(TripStopPathStatistics tripStatistics) {
        return !tripStatistics.hasAllStopPathsForRunTimes() || !tripStatistics.hasAllStopPathsForDwellTimes();
    }

}
