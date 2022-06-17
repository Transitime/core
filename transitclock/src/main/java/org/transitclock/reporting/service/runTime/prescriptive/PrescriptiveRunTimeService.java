package org.transitclock.reporting.service.runTime.prescriptive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.core.ServiceType;
import org.transitclock.core.ServiceTypeUtil;
import org.transitclock.db.query.PrescriptiveRunTimeStateQuery;
import org.transitclock.db.query.RunTimeForRouteQuery;
import org.transitclock.db.query.TripQuery;
import org.transitclock.db.structs.*;
import org.transitclock.db.structs.Calendar;
import org.transitclock.exceptions.InvalidRouteException;
import org.transitclock.ipc.data.*;
import org.transitclock.ipc.util.GtfsDbDataUtil;
import org.transitclock.reporting.TimePointStatistics;
import org.transitclock.reporting.keys.StopPathRunTimeKey;
import org.transitclock.reporting.service.OnTimePerformanceService;
import org.transitclock.reporting.service.RunTimeService;
import org.transitclock.reporting.service.runTime.TimePointRunTimeProcessor;
import org.transitclock.reporting.service.runTime.prescriptive.helper.DatedGtfsService;
import org.transitclock.reporting.service.runTime.prescriptive.model.AvgScheduleTime;
import org.transitclock.reporting.service.runTime.prescriptive.model.PrescriptiveRunTimeState;
import org.transitclock.reporting.service.runTime.prescriptive.model.DatedGtfs;
import org.transitclock.reporting.service.runTime.prescriptive.model.PrescriptiveRunTimeStates;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.model.TimebandTime;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.model.TimebandsForTripPattern;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.transitclock.ipc.util.GtfsDbDataUtil.getRouteShortName;

public class PrescriptiveRunTimeService {

    @Inject
    private RunTimeService runTimeService;

    @Inject
    private PrescriptiveTimebandService timebandService;

    @Inject
    private OnTimePerformanceService onTimePerformanceService;

    private static final BooleanConfigValue prescriptiveAvgIncludeFirstStop = new BooleanConfigValue(
            "transitclock.runTime.prescriptiveAvgIncludeFirstStop",
            false,
            "Whether or not to include the first stop dwell time in the prescriptive runtime avg calculation.");


    private static final Logger logger = LoggerFactory.getLogger(PrescriptiveRunTimeService.class);


    /**
     * Get Dated Gtfs
     */
     public List<IpcDatedGtfs> getDatedGtfs(){
         List<IpcDatedGtfs> ipcDatedGtfs = new ArrayList<>();
         for(DatedGtfs datedGtfs : DatedGtfsService.getDatedGtfs()){
             ipcDatedGtfs.add(new IpcDatedGtfs(datedGtfs));
         }
         return ipcDatedGtfs;
     }


    /**
     * Get Prescriptive RunTime Information for Run Times
     * @param routeIdOrShortName
     * @param serviceType
     * @param readOnly
     * @return
     * @throws Exception
     */
    public IpcPrescriptiveRunTimesForPatterns getPrescriptiveRunTimeBands(LocalDate beginDate,
                                                                               LocalDate endDate,
                                                                               String routeIdOrShortName,
                                                                               ServiceType serviceType,
                                                                               int configRev,
                                                                               boolean readOnly) throws Exception {
        // Prescriptive RunTimes Result
        IpcPrescriptiveRunTimesForPatterns runTimesForPatterns = new IpcPrescriptiveRunTimesForPatterns();

        // Get Route Short Name
        String routeShortName = GtfsDbDataUtil.getRouteShortName(routeIdOrShortName);

        // Get timebands for trip pattern
        Map<String, TimebandsForTripPattern> timebandsForTripPatternByPatternId =
                timebandService.generateTimebands(beginDate, endDate, routeShortName, serviceType, configRev, readOnly);

        // Get Service Types for all Service Ids for specified date range
        Map<String, Set<ServiceType>> serviceTypesByServiceId =
                ServiceTypeUtil.getServiceTypesByIdForCalendars(configRev, beginDate, endDate);

        // Get list of all tripPatterns for Route
        // Go through tripPatterns and generate runtimes for tripPattern
        List<TripPattern> tripPatterns = TripPattern.getTripPatternsForRoute(routeShortName, configRev, readOnly);
        for (TripPattern tripPattern : tripPatterns) {

            // Populate Timepoints for trip pattern
            List<IpcStopPath> timePoints = new ArrayList<>();
            for (StopPath timePoint : tripPattern.getScheduleAdhStopPaths()) {
                timePoints.add(new IpcStopPath(timePoint));
            }

            // Check to see if tripPattern has associated timeband

            // Get the timebands for a trip pattern
            TimebandsForTripPattern timebandsForTripPattern = timebandsForTripPatternByPatternId.get(tripPattern.getId());

            // Add adjusted list of timebands
            fillAdjustedTimeBandTimes(timebandsForTripPattern.getTimebandTimes());

            // Gets new prescriptive runtimes for timebands
            IpcPrescriptiveRunTimesForTimeBands runTimesForTimeBands = getPrescriptiveRunTimesForTimeBands(timebandsForTripPattern,
                    timePoints, serviceTypesByServiceId, routeShortName, tripPattern, serviceType, beginDate, endDate, configRev, readOnly);


            IpcPrescriptiveRunTimesForPattern prescriptiveRunTimesForPattern =
                    new IpcPrescriptiveRunTimesForPattern(timePoints, routeShortName, runTimesForTimeBands);


            runTimesForPatterns.addRunTimesForPatterns(prescriptiveRunTimesForPattern);
            runTimesForPatterns.addCurrentOnTime(runTimesForTimeBands.getCurrentOnTime());
            runTimesForPatterns.addExpectedOnTime(runTimesForTimeBands.getExpectedOnTime());
            runTimesForPatterns.addTotalRunTime(runTimesForTimeBands.getTotalRunTimes());
        }

        return runTimesForPatterns;

    }

    private void fillAdjustedTimeBandTimes(List<TimebandTime> timebandTimes) {
        // Set Adjusted From Times
        int timebandSize = timebandTimes.size();
        int lastTimebandIndex = timebandSize - 1;
        for(int i=0; i<timebandSize; i++){
            if(i==0 && timebandTimes.get(i).getStartTime().getHour() < 7){
                timebandTimes.get(i).setAdjustedStartTime(LocalTime.MIDNIGHT);
            } else {
                int minute = timebandTimes.get(i).getStartTime().getMinute();
                LocalTime adjustedEndTime;
                if(minute < 15){
                    adjustedEndTime = timebandTimes.get(i).getStartTime().truncatedTo(ChronoUnit.HOURS);
                } else if(minute >= 15 &&  minute < 45){
                    adjustedEndTime = timebandTimes.get(i).getStartTime().withMinute(30);
                } else if(minute >= 45){
                    adjustedEndTime = timebandTimes.get(i).getStartTime().plusHours(1).truncatedTo(ChronoUnit.HOURS);
                } else {
                    adjustedEndTime = timebandTimes.get(i).getStartTime();
                }
                timebandTimes.get(i).setAdjustedStartTime(adjustedEndTime);
            }
        }
        // Set adjusted To Times
        for(int i=0; i<timebandSize; i++){
            if(i < lastTimebandIndex){
                timebandTimes.get(i).setAdjustedEndTime(timebandTimes.get(i+1).getAdjustedStartTime());
            }else {
                timebandTimes.get(i).setAdjustedEndTime(LocalTime.of(3,0));
            }
        }
    }

    /**
     * Gets new prescriptive runtimes for timebands
     * @param timebandsByPattern
     * @param timePoints
     * @param routeIdOrShortName
     * @param tripPattern
     * @param serviceType
     * @param beginDate
     * @param endDate
     * @param configRev
     * @param readOnly
     * @return
     * @throws Exception
     */
    private IpcPrescriptiveRunTimesForTimeBands getPrescriptiveRunTimesForTimeBands(TimebandsForTripPattern timebandsByPattern,
                                                                                         List<IpcStopPath> timePoints,
                                                                                         Map<String, Set<ServiceType>> serviceTypesByServiceId,
                                                                                         String routeIdOrShortName,
                                                                                         TripPattern tripPattern,
                                                                                         ServiceType serviceType,
                                                                                         LocalDate beginDate,
                                                                                         LocalDate endDate,
                                                                                         int configRev,
                                                                                         boolean readOnly) throws Exception {

        IpcPrescriptiveRunTimesForTimeBands prescriptiveRunTimesForTimeBands = new IpcPrescriptiveRunTimesForTimeBands();

        // If timeband is found add it to timebands list
        if(timebandsByPattern != null){

            // Get all trips for provided Route grouped by TripPatternId
            Map<String, List<Trip>> tripsByTripPatternId = getTripsForRouteByTripPattern(routeIdOrShortName, configRev);

            // For each timeband create an IpcPrescriptiveRunTimeBand
            // this includes timeband start time, timeband end time and timepoint run time info
            for(TimebandTime timebandTime : timebandsByPattern.getTimebandTimes()){
                LocalTime beginTime = timebandTime.getStartTime();
                LocalTime endTime = timebandTime.getEndTime();
                LocalTime adjustedBeginTime = timebandTime.getAdjustedStartTime();
                LocalTime adjustedEndTime = timebandTime.getAdjustedEndTime();


                PrescriptiveRunTimeStateQuery.Builder queryBuilder = new PrescriptiveRunTimeStateQuery.Builder();
                PrescriptiveRunTimeStateQuery query = queryBuilder.beginDate(beginDate)
                                                                  .endDate(endDate)
                                                                  .beginTime(beginTime)
                                                                  .endTime(endTime)
                                                                  .routeShortName(routeIdOrShortName)
                                                                  .tripPatternId(tripPattern.getId())
                                                                  .serviceType(serviceType)
                                                                  .configRev(configRev)
                                                                  .readOnly(readOnly)
                                                                  .build();


                List<Trip> tripsForTripPattern = tripsByTripPatternId.get(tripPattern.getId());

                // Get timepoint specific runtime info
                PrescriptiveRunTimeStates prescriptiveRunTimeStates =
                        getPrescriptiveRunTimeStates(query, serviceTypesByServiceId, tripPattern, tripsForTripPattern);

                // confirm that the run time info lines up with the expected timepoints
                // if it doesn't match up then skip
                if(!hasValidRunTimeStates(prescriptiveRunTimeStates, timePoints)){
                    continue;
                }

                PrescriptiveRunTimeState prescriptiveRunTimeState = prescriptiveRunTimeStates.getPrescriptiveRunTimeState();

                // build IpcPrescriptiveRunTimeBand using runtime info and timeband info
                IpcPrescriptiveRunTimesForTimeBand prescriptiveRunTimeBand =
                        new IpcPrescriptiveRunTimesForTimeBand(adjustedBeginTime.toString(),
                                                               adjustedEndTime.toString(),
                                                               prescriptiveRunTimeState.getCurrentOnTimeFraction(),
                                                               prescriptiveRunTimeState.getExpectedOnTimeFraction(),
                                                               prescriptiveRunTimeState.getPrescriptiveRunTimes());

                //add timeband to our timebands list
                prescriptiveRunTimesForTimeBands.addRunTimesForTimeBands(prescriptiveRunTimeBand);
                prescriptiveRunTimesForTimeBands.addCurrentOnTime(prescriptiveRunTimeState.getCurrentOnTime());
                prescriptiveRunTimesForTimeBands.addExpectedOnTime(prescriptiveRunTimeState.getExpectedOnTime());
                prescriptiveRunTimesForTimeBands.addTotalRunTime(prescriptiveRunTimeState.getTotalRunTimes());
            }
        }

        return prescriptiveRunTimesForTimeBands;

    }

    private Map<String, List<Trip>> getTripsForRouteByTripPattern(String routeIdOrShortName, int configRev) throws InvalidRouteException {
        Map<String, List<Trip>> tripsByTripPatternId = new HashMap<>();

        String routeShortName = getRouteShortName(routeIdOrShortName);
        Set<Integer> configRevs = new HashSet<>();
        configRevs.add(configRev);

        TripQuery tripQuery = new TripQuery.Builder(routeShortName, configRevs).build();

        List<Trip> trips = Trip.getTripsFromDb(tripQuery);

        for(Trip trip : trips){
            List<Trip> tripsForPattern = tripsByTripPatternId.get(trip.getTripPatternId());
            if(tripsForPattern == null){
                tripsForPattern = new ArrayList<>();
                tripsByTripPatternId.put(trip.getTripPatternId(), tripsForPattern);
            }
            tripsForPattern.add(trip);
        }

        return tripsByTripPatternId;
    }

    private boolean hasValidRunTimeStates(PrescriptiveRunTimeStates prescriptiveRunTimeStates,
                                    List<IpcStopPath> timePoints) {

        if(prescriptiveRunTimeStates == null){
            logger.error("Null prescriptive runtime states");
            return false;
        }

        PrescriptiveRunTimeState prescriptiveRunTimeState = prescriptiveRunTimeStates.getPrescriptiveRunTimeState();

        if(prescriptiveRunTimeState == null){
            logger.error("Null prescriptive runtime state (either scheduled or realtime)");
            return false;
        }

        List<IpcPrescriptiveRunTime> prescriptiveRunTimes = prescriptiveRunTimeState.getPrescriptiveRunTimes();

        if(prescriptiveRunTimes.size() != timePoints.size()){
            logger.warn("PrescriptiveRunTimes size {} doesn't match timePoints size {}",
                    prescriptiveRunTimes.size(),timePoints.size());
            return false;
        }

        for(int i=0; i < timePoints.size(); i++){
            IpcPrescriptiveRunTime runTimeStopPath = prescriptiveRunTimes.get(i);
            IpcStopPath timePoint = timePoints.get(i);

            if(!runTimeStopPath.getStopPathId().equals(timePoint.getStopPathId())){
                logger.warn("PrescriptiveRunTime stopPathId {} doesn't match timePoints stopPathId {}",
                        runTimeStopPath.getStopPathId(), timePoint.getStopPathId());
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param prtsQuery
     * @return
     * @throws Exception
     */
    public PrescriptiveRunTimeStates getPrescriptiveRunTimeStates(PrescriptiveRunTimeStateQuery prtsQuery,
                                                                Map<String, Set<ServiceType>> serviceTypesByServiceId,
                                                                TripPattern tripPattern,
                                                                List<Trip> trips) throws Exception{

        String routeShortName = getRouteShortName(prtsQuery.getRouteShortName());

        RunTimeForRouteQuery.Builder rtBuilder = new RunTimeForRouteQuery.Builder();
        RunTimeForRouteQuery rtQuery = rtBuilder
                .beginDate(prtsQuery.getBeginDate())
                .endDate(prtsQuery.getEndDate())
                .beginTime(prtsQuery.getBeginTime())
                .endTime(prtsQuery.getEndTime())
                .serviceType(prtsQuery.getServiceType())
                .routeShortName(routeShortName)
                .headsign(prtsQuery.getHeadsign())
                .directionId(prtsQuery.getDirectionId())
                .tripPatternId(prtsQuery.getTripPatternId())
                .includeRunTimesForStops(true)
                .readOnly(prtsQuery.isReadOnly())
                .build();


        // Get Arrivals and Departures for OTP
        List<ArrivalDeparture> arrivalDepartures = onTimePerformanceService.getArrivalsDepartures(
                prtsQuery.getBeginDate(), prtsQuery.getEndDate(), prtsQuery.getBeginTime(), prtsQuery.getEndTime(),
                prtsQuery.getRouteShortName(), prtsQuery.getServiceType(), true, prtsQuery.getHeadsign(),
                prtsQuery.isReadOnly());


        // Get list of historical runTimes to help build PrescriptiveRunTimeState
        List<RunTimesForRoutes> runTimesForRoutes = runTimeService.getRunTimesForRoutes(rtQuery);

        // Get list of all unique trip ids in runTimes results
        Set<String> existingRunTimeTripIds = runTimesForRoutes.stream().map(rt -> rt.getTripId()).collect(Collectors.toSet());

        // Get list of applicable scheduled trips
        List<Trip> singleTripForSampleRunTime = new ArrayList<>();
        Map<Integer, AvgScheduleTime> allTripScheduleTimesByIndex = new LinkedHashMap<>();

        for(Trip trip : trips){
            if(includeTripAsScheduledRunTime(trip, rtQuery, serviceTypesByServiceId)){
                if(allowSingleTripForRunTime(singleTripForSampleRunTime, existingRunTimeTripIds, trip)){
                    singleTripForSampleRunTime.add(trip);
                }
                updateTripScheduleTimes(allTripScheduleTimesByIndex, trip.getScheduleTimes());
            }
        }

        // Average Schedule Times for all valid trips
        List<ScheduleTime> avgTripScheduleTimes = getAvgTripScheduleTimes(allTripScheduleTimesByIndex);

        // Supplement realtime runTime data with scheduled runTime data as necessary
        addScheduledTripRunTimeToResults(singleTripForSampleRunTime, rtQuery, runTimesForRoutes);

        // Get list of all runTimes for scheduled trips NOT included in the original historical results
        // Goal here is to include runTime information for trips that never got captured by the runtime query
        try{
            TimePointRunTimeProcessor timePointRunTimeProcessor =
                    getTimePointRunTimeProcessorWithStopPaths(runTimesForRoutes, tripPattern.getStopPaths());


            Map<StopPathRunTimeKey, StopPath> timePointStopPaths = timePointRunTimeProcessor.getTimePointStopPaths();

            // Realtime
            Map<StopPathRunTimeKey, TimePointStatistics> timePointsStatistics = timePointRunTimeProcessor.getSortedTimePointsStatistics();
            PrescriptiveRunTimeState prescriptiveRunTimeState =
                    createPrescriptiveRunTimeState(timePointsStatistics, timePointStopPaths, avgTripScheduleTimes, arrivalDepartures);

            if(prescriptiveRunTimeState != null){
                return new PrescriptiveRunTimeStates(prescriptiveRunTimeState);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }



    private void updateTripScheduleTimes(Map<Integer, AvgScheduleTime> allTripScheduleTimesByIndex,
                                         List<ScheduleTime> scheduleTimes) {
        for(int i=0; i<scheduleTimes.size(); i++){
            AvgScheduleTime avgScheduleTime = allTripScheduleTimesByIndex.get(i);
            if(avgScheduleTime == null){
                avgScheduleTime = new AvgScheduleTime(scheduleTimes.get(i));
            } else {
                avgScheduleTime.add(scheduleTimes.get(i));
            }
            allTripScheduleTimesByIndex.put(i, avgScheduleTime);
        }
    }

    private List<ScheduleTime> getAvgTripScheduleTimes(Map<Integer, AvgScheduleTime> allTripScheduleTimesByIndex) {
        List<ScheduleTime> avgScheduleTimes = new ArrayList<>();
        for(AvgScheduleTime avgScheduleTime : allTripScheduleTimesByIndex.values()){
            avgScheduleTimes.add(avgScheduleTime.getAverageScheduleTime());
        }
        return avgScheduleTimes;
    }

    private void addScheduledTripRunTimeToResults(List<Trip> filteredRouteTrip,
                                                  RunTimeForRouteQuery rtQuery,
                                                  List<RunTimesForRoutes> results){

        if(!filteredRouteTrip.isEmpty()) {
            List<RunTimesForRoutes> missingTripRunTimesForRoutes = runTimeService.getScheduledRunTimesForRoutes(rtQuery, filteredRouteTrip);
            if(!missingTripRunTimesForRoutes.isEmpty()) {
                results.addAll(missingTripRunTimesForRoutes);
            }
        }
    }

    /**
     * Checks to make sure the scheduled trip should actually be included in the list of runtimes
     * Checks for things like making sure the serviceId is valid, making sure a runtime with that trip id doesn't
     * already exist, and making sure the trip start and end times line up
     *
     * @param trip
     * @param rtQuery
     * @param serviceTypesByServiceId
     * @return
     */
    private boolean includeTripAsScheduledRunTime(Trip trip,
                                                  RunTimeForRouteQuery rtQuery,
                                                  Map<String, Set<ServiceType>> serviceTypesByServiceId) {


        if(trip.getStartTime() < rtQuery.getBeginTime() || trip.getEndTime() > rtQuery.getEndTime()) {
            return false;
        }

        Set<ServiceType> serviceTypesForServiceId = serviceTypesByServiceId.get(trip.getServiceId());
        if(serviceTypesForServiceId == null || !serviceTypesForServiceId.contains(rtQuery.getServiceType())){
            return false;
        }

        return true;

    }

    private boolean allowSingleTripForRunTime(List<Trip> singleTripForSampleRunTime,
                                              Set<String> existingRunTimeTripIds,
                                              Trip trip){
        if(singleTripForSampleRunTime.isEmpty()){
            return false;
        }
        if(existingRunTimeTripIds.size() <= 1){
            return false;
        }
        if(!existingRunTimeTripIds.contains(trip.getId())){
            return false;
        }

        return true;
    }


    /**
     *  Return time point run time processor with Stop Paths
     *  runTime stat info for each timepoint grouping
     * @param runTimesForRoutes
     * @param stopPaths
     * @return
     */
    private TimePointRunTimeProcessor getTimePointRunTimeProcessorWithStopPaths(List<RunTimesForRoutes> runTimesForRoutes,
                                                                                List<StopPath> stopPaths) {

        TimePointRunTimeProcessor processor =  new TimePointRunTimeProcessor(stopPaths);

        for(RunTimesForRoutes runTimesForRoute : runTimesForRoutes){
            processor.addRunTimesForStops(runTimesForRoute.getRunTimesForStops());
        }

        return processor;
    }

    /**
     * Calculates the suggested schedule modifications by retrieving appropriate variable, dwellTime, and remainder
     * for each timepoint segment (based on their index) and applying that to the scheduled times.
     * @param timePointsStatistics
     * @param timePointStopPaths
     * @param scheduleTimes
     * @return
     */
    private PrescriptiveRunTimeState createPrescriptiveRunTimeState(Map<StopPathRunTimeKey, TimePointStatistics> timePointsStatistics,
                                                                    Map<StopPathRunTimeKey, StopPath> timePointStopPaths,
                                                                    List<ScheduleTime> scheduleTimes,
                                                                    List<ArrivalDeparture> arrivalDepartures){

        Map<Integer, ScheduleTime> scheduleTimesByStopPathIndexMap = createScheduledTimesGroupedById(scheduleTimes);

        Map<String, List<ArrivalDeparture>> arrivalDeparturesByStopPath = getArrivalDeparturesByStopPath(arrivalDepartures);

        PrescriptiveRunTimeState state = new PrescriptiveRunTimeState(scheduleTimesByStopPathIndexMap, arrivalDeparturesByStopPath);

        for(Map.Entry<StopPathRunTimeKey, StopPath> timePointStopPath : timePointStopPaths.entrySet()) {
            StopPathRunTimeKey key = timePointStopPath.getKey();
            TimePointStatistics timePointStatistics = timePointsStatistics.get(key);

            if (!isValid(timePointStatistics)) {
                logger.warn("No matching timepoint for {}, unable to provide prescriptive runtime", timePointStatistics);
                return null;
            } else {
                state.updateScheduleAdjustments(timePointStatistics, prescriptiveAvgIncludeFirstStop.getValue());
                state.createPrescriptiveRunTimeForTimePoint();
            }
        }

        return state;
    }

    private Map<String, List<ArrivalDeparture>> getArrivalDeparturesByStopPath(List<ArrivalDeparture> arrivalDepartures){
        Map<String, List<ArrivalDeparture>> arrivalDeparturesByStopPath = new HashMap<>();
        for(ArrivalDeparture ad : arrivalDepartures){
            List<ArrivalDeparture> adForStopPath = arrivalDeparturesByStopPath.get(ad.getStopPathId());
            if(adForStopPath == null){
                adForStopPath = new ArrayList<>();
                arrivalDeparturesByStopPath.put(ad.getStopPathId(), adForStopPath);
            }
            adForStopPath.add(ad);
        }
        return arrivalDeparturesByStopPath;
    }

    /**
     * Return first trip available trip for TripPatternId for specified serviceType
     * @param tripPatternId
     * @param serviceType
     * @return
     */
    private Trip getFirstTripForPattern(String tripPatternId, ServiceType serviceType){
        List<String> tripIds = Core.getInstance().getDbConfig().getTripIdsForTripPattern(tripPatternId);
        List<Trip> trips = new ArrayList<>();
        for(String tripId : tripIds){
            trips.add(Core.getInstance().getDbConfig().getTrip(tripId));
        }
        for(Trip trip : trips){
            Calendar calendar = Core.getInstance().getDbConfig().getCalendarByServiceId(trip.getServiceId());
            if(ServiceTypeUtil.isCalendarValidForServiceType(calendar, serviceType)){
                return trip;
            }
        }
        return null;
    }

    /**
     * Get scheduled times for trip grouped by stopPathIndex
     * StopPathIndex is determined by index when traversing scheduledTimes
     * @param scheduleTimes
     * @return
     */
    private Map<Integer, ScheduleTime> createScheduledTimesGroupedById(List<ScheduleTime> scheduleTimes){
        return IntStream.range(0, scheduleTimes.size())
                .boxed()
                .collect(Collectors.toMap(Function.identity(), scheduleTimes::get));
    }

    private boolean isValid(TimePointStatistics timePointStatistics){
        return timePointStatistics != null;
    }
}
