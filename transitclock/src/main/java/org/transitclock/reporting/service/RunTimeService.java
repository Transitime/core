package org.transitclock.reporting.service;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.LongConfigValue;
import org.transitclock.core.ServiceType;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.query.RunTimeForRouteQuery;
import org.transitclock.db.query.TripQuery;
import org.transitclock.db.structs.*;
import org.transitclock.ipc.data.*;
import org.transitclock.reporting.*;
import org.transitclock.reporting.dao.RunTimeRoutesDao;
import org.transitclock.reporting.keys.TripConfigRevKey;
import org.transitclock.statistics.RunTimeStatistics;
import org.transitclock.utils.Time;

import javax.inject.Inject;
import java.time.*;
import java.util.*;

import static org.transitclock.ipc.util.GtfsDbDataUtil.getRouteShortName;

public class RunTimeService {

    private static final Logger logger = LoggerFactory.getLogger(RunTimeService.class);

    private static BooleanConfigValue filterRunTimeOutliers = new BooleanConfigValue(
            "transitclock.reporting.runTime.filterOutliers", true, "When set to true allows" +
            "filtering of runTime ouliers.");

    private static LongConfigValue defaultScheduledDwellTime = new LongConfigValue(
            "transitclock.reporting.runTime.scheduledDwellTime", 0l,
            "Default value for scheduled run dwell time");

    private static Boolean getFilterRunTimeOutliers(){
        return filterRunTimeOutliers.getValue();
    }

    @Inject
    private RunTimeRoutesDao dao;

    public RunTimeRoutesDao getDao() {
        return dao;
    }

    public void setDao(RunTimeRoutesDao dao) {
        this.dao = dao;
    }

    /**
     * Get summary information for RunTimes
     * @param beginDate
     * @param endDate
     * @param beginTime
     * @param endTime
     * @param routeShortName
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
                                        String routeShortName,
                                        String headsign,
                                        String directionId,
                                        String tripPatternId,
                                        ServiceType serviceType,
                                        boolean readOnly) throws Exception {

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

    /**
     * Get scheduled runtimes for routes
     * Uses a list of scheduled trips to build runtimes
     * Checks to make sure that the scheduled trip id doesn't already exist in the list of runtimes
     *
     * @param rtQuery
     * @param scheduledRouteTrips
     * @return
     */
    public List<RunTimesForRoutes> getScheduledRunTimesForRoutes(RunTimeForRouteQuery rtQuery,
                                                                 List<Trip> scheduledRouteTrips) {

        List<RunTimesForRoutes> scheduledRunTimesForRoutes = new ArrayList<>();

        LocalDate tripDate = rtQuery.getBeginDate();
        ServiceType serviceType = rtQuery.getServiceType();

        for(Trip scheduledTrip : scheduledRouteTrips){
            RunTimesForRoutes runTimesForRoutes = new RunTimesForRoutes(scheduledTrip, serviceType, tripDate);
            List<RunTimesForStops> runTimesForStops = getScheduledRunTimesForStops(scheduledTrip, tripDate, runTimesForRoutes);
            runTimesForRoutes.setScheduled(true);
            runTimesForRoutes.setRunTimesForStops(runTimesForStops);
            scheduledRunTimesForRoutes.add(runTimesForRoutes);
        }

        return scheduledRunTimesForRoutes;

    }

    /**
     * Checks to make sure the scheduled trip should actually be included in the list of runtimes
     * Checks for things like making sure the serviceId is valid, making sure a runtime with that trip id doesn't
     * already exist, and making sure the trip start and end times line up
     *
     * @param trip
     * @param rtQuery
     * @param existingRunTimeTripIds
     * @param serviceTypesByServiceId
     * @return
     */
    private boolean includeTripAsScheduledRunTime(Trip trip,
                                                  RunTimeForRouteQuery rtQuery,
                                                  Set<String> existingRunTimeTripIds,
                                                  Map<String, Set<ServiceType>> serviceTypesByServiceId) {

        if(existingRunTimeTripIds.contains(trip.getId())){
            return false;
        }

        if(trip.getStartTime() < rtQuery.getBeginTime() || trip.getEndTime() > rtQuery.getEndTime()) {
            return false;
        }

        Set<ServiceType> serviceTypesForServiceId = serviceTypesByServiceId.get(trip.getServiceId());
        if(serviceTypesForServiceId == null || !serviceTypesForServiceId.contains(rtQuery.getServiceType())){
            return false;
        }

        return true;

    }

    /**
     * Builds runtime stop information for scheduled trip runtimes
     * Spoofs the date and marks runtimes for stops as scheduled
     * @param trip
     * @param beginDate
     * @param runTimesForRoutes
     * @return
     */
    private List<RunTimesForStops> getScheduledRunTimesForStops(Trip trip,
                                                                LocalDate beginDate,
                                                                RunTimesForRoutes runTimesForRoutes){

        Date tripDate = Time.getLocalDateAsDate(beginDate);

        List<RunTimesForStops> runTimesForStopsList = new ArrayList<>();

        List<ScheduleTime> tripScheduleTimes = trip.getScheduleTimes();
        List<StopPath> tripStopPaths = trip.getStopPaths();

        if(!hasValidTripStops(tripScheduleTimes, tripStopPaths)){
            return Collections.EMPTY_LIST;
        }

        for(int i=0; i < tripStopPaths.size(); i++){
            RunTimesForStops runTimesForStops = new RunTimesForStops();
            runTimesForStops.setScheduled(true);
            runTimesForStops.setStopPathIndex(i);
            runTimesForStops.setStopPathId(tripStopPaths.get(i).getStopPathId());
            runTimesForStops.setScheduledTime(tripScheduleTimes.get(i).getTime());
            runTimesForStops.setDwellTime(getScheduledDwellTime(tripScheduleTimes.get(i)));
            runTimesForStops.setLastStop(tripStopPaths.get(i).isLastStopInTrip());
            runTimesForStops.setTimePoint(tripStopPaths.get(i).isScheduleAdherenceStop());

            // Set Stop Time
            Date stopTime = new Date(tripDate.getTime());
            stopTime.setTime(stopTime.getTime() + (tripScheduleTimes.get(i).getTime() * 1000));
            runTimesForStops.setTime(stopTime);

            // Set prev departure time
            if(i > 0){
                Date prevStopDepartureTime = new Date(tripDate.getTime());
                prevStopDepartureTime.setTime(prevStopDepartureTime.getTime() + (tripScheduleTimes.get(i-1).getDepartureTime() * 1000));
                runTimesForStops.setPrevStopDepartureTime(prevStopDepartureTime);
            }

            runTimesForStops.setRunTimesForRoutes(runTimesForRoutes);

            runTimesForStopsList.add(runTimesForStops);
        }

        return runTimesForStopsList;
    }

    private Long getScheduledDwellTime(ScheduleTime scheduleTime){
        Integer scheduledDwellTime = scheduleTime.getScheduledDwellTime();
        if(scheduledDwellTime > 0){
            return scheduledDwellTime.longValue();
        } else {
            return defaultScheduledDwellTime.getValue();
        }
    }

    private boolean hasValidTripStops(List<ScheduleTime> tripScheduleTimes,
                                      List<StopPath> tripStopPaths) {
        if(tripScheduleTimes.size() != tripStopPaths.size()){
            return false;
        }
        return true;
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
