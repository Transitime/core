package org.transitclock.reporting.service.runTime.prescriptive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.DoubleConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.ServiceType;
import org.transitclock.core.ServiceTypeUtil;
import org.transitclock.db.query.ArrivalDepartureQuery;
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

    public RunTimeService getRunTimeService() {
        return runTimeService;
    }

    public void setRunTimeService(RunTimeService runTimeService) {
        this.runTimeService = runTimeService;
    }

    public PrescriptiveTimebandService getTimebandService() {
        return timebandService;
    }

    public void setTimebandService(PrescriptiveTimebandService timebandService) {
        this.timebandService = timebandService;
    }

    private static final BooleanConfigValue prescriptiveAvgIncludeFirstStop = new BooleanConfigValue(
            "transitclock.runTime.prescriptiveAvgIncludeFirstStop",
            false,
            "Whether or not to include the first stop dwell time in the prescriptive runtime avg calculation.");

    private static final DoubleConfigValue adjustedTimePositiveError = new DoubleConfigValue(
            "transitclock.runTime.prescriptivePositiveErrorSec",
            20d,
            "Whether or not to include the first stop dwell time in the prescriptive runtime avg calculation.");

    private Double getAdjustedTimePositiveError(){
        //return 60d;
        return adjustedTimePositiveError.getValue() * 1000;
    }

    private static final DoubleConfigValue adjustedTimeNegativeError = new DoubleConfigValue(
            "transitclock.runTime.prescriptivePositiveErrorSec",
            20d,
            "Negative error threshold for prescriptive adjustment. Accounts for driver schedule adjustments" +
                    "for new schedule.");

    private Double getAdjustedTimeNegativeError(){
        //return 60d;
        return adjustedTimeNegativeError.getValue() * 1000;
    }


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
     *
     * @param beginDate
     * @param endDate
     * @param routeShortName
     * @param serviceType
     * @param configRev
     * @param readOnly
     * @return
     * @throws Exception
     */
    public IpcPrescriptiveRunTimesForPatterns getPrescriptiveRunTimeBands(LocalDate beginDate,
                                                                           LocalDate endDate,
                                                                           String routeShortName,
                                                                           ServiceType serviceType,
                                                                           int configRev,
                                                                           boolean readOnly) throws Exception {
        // Prescriptive RunTimes Result
        IpcPrescriptiveRunTimesForPatterns runTimesForPatterns = new IpcPrescriptiveRunTimesForPatterns();

        // Get timebands for trip pattern
        Map<String, TimebandsForTripPattern> timebandsForTripPatternByPatternId =
                timebandService.generateTimebands(beginDate, endDate, routeShortName, serviceType, configRev, readOnly);

        // Get Service Types for all Service Ids for specified date range
        Map<String, Set<ServiceType>> serviceTypesByServiceId =
                ServiceTypeUtil.getServiceTypesByIdForCalendars(configRev, beginDate, endDate);

        // Get all trips for provided Route grouped by TripPatternId
        Map<String, List<Trip>> tripsByTripPatternId = getTripsForRouteByTripPattern(routeShortName, configRev);

        // Get list of all tripPatterns for Route
        // Go through tripPatterns and generate runtimes for tripPattern
        List<TripPattern> tripPatterns = getTripPatternsForRoute(routeShortName, configRev, readOnly);

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
            try{
                fillAdjustedTimeBandTimes(timebandsForTripPattern.getTimebandTimes());
            } catch (Exception e){
                e.printStackTrace();
                continue;
            }

            // Gets new prescriptive runtimes for timebands
            IpcPrescriptiveRunTimesForTimeBands runTimesForTimeBands = getPrescriptiveRunTimesForTimeBands(timebandsForTripPattern,
                    tripsByTripPatternId, timePoints, serviceTypesByServiceId, routeShortName, tripPattern, serviceType,
                    beginDate, endDate, configRev, readOnly);


            IpcPrescriptiveRunTimesForPattern prescriptiveRunTimesForPattern =
                    new IpcPrescriptiveRunTimesForPattern(timePoints, routeShortName, runTimesForTimeBands);


            runTimesForPatterns.addRunTimesForPatterns(prescriptiveRunTimesForPattern);
            runTimesForPatterns.addCurrentOnTime(runTimesForTimeBands.getCurrentOnTime());
            runTimesForPatterns.addExpectedOnTime(runTimesForTimeBands.getExpectedOnTime());
            runTimesForPatterns.addTotalRunTime(runTimesForTimeBands.getTotalRunTimes());
        }

        return runTimesForPatterns;

    }

    public List<TripPattern> getTripPatternsForRoute(String routeShortName, int configRev, boolean readOnly) {
        return TripPattern.getTripPatternsForRoute(routeShortName, configRev, readOnly);
    }

    private void fillAdjustedTimeBandTimes(List<TimebandTime> timebandTimes) {
        // Set Adjusted From Times
        int timebandSize = timebandTimes.size();
        int lastTimebandIndex = timebandSize - 1;
        // Set Adjusted Start Times
        for(int i=0; i<timebandSize; i++){
            boolean isFirstTimeBand = i == 0;
            TimebandTime currentTimeBandTime = timebandTimes.get(i);
            LocalTime adjustedBeginTime = getAdjustedBeginTime(currentTimeBandTime, isFirstTimeBand);
            currentTimeBandTime.setAdjustedStartTime(adjustedBeginTime);
        }

        // Set Adjusted End Times
        for(int i=0; i<timebandSize; i++){
            boolean isLastTimeBand = i == lastTimebandIndex;
            boolean hasSingleTimeBandTime = timebandSize == 1;
            TimebandTime currentTimeBandTime = timebandTimes.get(i);
            TimebandTime nextTimeBandTime = isLastTimeBand ? null : timebandTimes.get(i + 1);
            LocalTime adjustedEndTime = getAdjustedEndTime(currentTimeBandTime, nextTimeBandTime, isLastTimeBand, hasSingleTimeBandTime);
            currentTimeBandTime.setAdjustedEndTime(adjustedEndTime);
        }
    }

    private LocalTime getAdjustedBeginTime(TimebandTime timebandTime, boolean isFirstTimeBand){
        LocalTime adjustedBeginTime;

        if(isFirstTimeBand && timebandTime.getStartTime().getHour() < 7){
            adjustedBeginTime = LocalTime.MIDNIGHT;
        } else {

            int minute = timebandTime.getStartTime().getMinute();
            if (minute < 15) {
                adjustedBeginTime = timebandTime.getStartTime().truncatedTo(ChronoUnit.HOURS);
            } else if (minute >= 15 && minute < 45) {
                adjustedBeginTime = timebandTime.getStartTime().withMinute(30);
            } else if (minute >= 45) {
                adjustedBeginTime = timebandTime.getStartTime().plusHours(1).truncatedTo(ChronoUnit.HOURS);
            } else {
                adjustedBeginTime = timebandTime.getStartTime();
            }
        }

        return adjustedBeginTime;
    }

    private LocalTime getAdjustedEndTime(TimebandTime currentTimeBandTime,
                                         TimebandTime nextTimeBandTime,
                                         boolean isLastBandTime,
                                         boolean hasSingleTimeBandTime){
        if(hasSingleTimeBandTime){
            return getAdjustedBeginTime(currentTimeBandTime, false).plusHours(1);
        }
        if(isLastBandTime){
            return LocalTime.of(3,0);
        }
        return nextTimeBandTime.getAdjustedStartTime();
    }

    /**
     * Gets new prescriptive runtimes for timebands
     * @param timebandsByPattern
     * @param tripsByTripPatternId
     * @param timePoints
     * @param serviceTypesByServiceId
     * @param routeShortName
     * @param beginDate
     * @param endDate
     * @param configRev
     * @param readOnly
     * @return
     * @throws Exception
     */
    private IpcPrescriptiveRunTimesForTimeBands getPrescriptiveRunTimesForTimeBands(TimebandsForTripPattern timebandsByPattern,
                                                                                    Map<String, List<Trip>> tripsByTripPatternId,
                                                                                     List<IpcStopPath> timePoints,
                                                                                     Map<String, Set<ServiceType>> serviceTypesByServiceId,
                                                                                     String routeShortName,
                                                                                     TripPattern tripPattern,
                                                                                     ServiceType serviceType,
                                                                                     LocalDate beginDate,
                                                                                     LocalDate endDate,
                                                                                     int configRev,
                                                                                     boolean readOnly) throws Exception {

        IpcPrescriptiveRunTimesForTimeBands prescriptiveRunTimesForTimeBands = new IpcPrescriptiveRunTimesForTimeBands();

        // If timeband is found add it to timebands list
        if(timebandsByPattern != null){
            // Get all trips for trip pattern
            List<Trip> tripsForTripPattern = tripsByTripPatternId.get(tripPattern.getId());

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
                                                                  .beginTime(adjustedBeginTime)
                                                                  .endTime(adjustedEndTime)
                                                                  .routeShortName(routeShortName)
                                                                  .tripPatternId(tripPattern.getId())
                                                                  .serviceType(serviceType)
                                                                  .configRev(configRev)
                                                                  .readOnly(readOnly)
                                                                  .build();

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
                                                               prescriptiveRunTimeState.getPrescriptiveRunTimes(),
                                                               tripPattern.getId());

                //add timeband to our timebands list
                prescriptiveRunTimesForTimeBands.addRunTimesForTimeBands(prescriptiveRunTimeBand);
                prescriptiveRunTimesForTimeBands.addCurrentOnTime(prescriptiveRunTimeState.getCurrentOnTime());
                prescriptiveRunTimesForTimeBands.addExpectedOnTime(prescriptiveRunTimeState.getExpectedOnTime());
                prescriptiveRunTimesForTimeBands.addTotalRunTime(prescriptiveRunTimeState.getTotalRunTimes());
            }
        }

        return prescriptiveRunTimesForTimeBands;

    }

    private Map<String, List<Trip>> getTripsForRouteByTripPattern(String routeShortName, int configRev) throws InvalidRouteException {
        Map<String, List<Trip>> tripsByTripPatternId = new HashMap<>();

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


        RunTimeForRouteQuery.Builder rtBuilder = new RunTimeForRouteQuery.Builder();
        RunTimeForRouteQuery rtQuery = rtBuilder
                .beginDate(prtsQuery.getBeginDate())
                .endDate(prtsQuery.getEndDate())
                .beginTime(prtsQuery.getBeginTime())
                .endTime(prtsQuery.getEndTime())
                .serviceType(prtsQuery.getServiceType())
                .routeShortName(prtsQuery.getRouteShortName())
                .headsign(prtsQuery.getHeadsign())
                .directionId(prtsQuery.getDirectionId())
                .tripPatternId(prtsQuery.getTripPatternId())
                .includeRunTimesForStops(true)
                .readOnly(prtsQuery.isReadOnly())
                .build();


        // Get Arrivals and Departures for OTP
        ArrivalDepartureQuery.Builder adBuilder = new ArrivalDepartureQuery.Builder();
        ArrivalDepartureQuery adQuery = adBuilder
                .beginDate(prtsQuery.getBeginDate())
                .endDate(prtsQuery.getEndDate())
                .beginTime(prtsQuery.getBeginTime())
                .endTime(prtsQuery.getEndTime())
                .serviceType(prtsQuery.getServiceType())
                .timePointsOnly(true)
                .scheduledTimesOnly(true)
                .routeShortName(prtsQuery.getRouteShortName())
                .tripPatternId(prtsQuery.getTripPatternId())
                .readOnly(prtsQuery.isReadOnly())
                .build();

        List<ArrivalDeparture> arrivalDepartures = ArrivalDeparture.getArrivalsDeparturesFromDb(adQuery);

        // Get list of historical runTimes to help build PrescriptiveRunTimeState
        List<RunTimesForRoutes> runTimesForRoutes = runTimeService.getRunTimesForRoutes(rtQuery);

        // Get list of all unique trip ids in runTimes results
        Set<String> existingRunTimeTripIds = runTimesForRoutes.stream().map(rt -> rt.getTripId()).collect(Collectors.toSet());

        // Get list of applicable scheduled trips
        List<Trip> singleTripForSampleRunTime = new ArrayList<>();
        Map<Integer, AvgScheduleTime> allTripScheduleTimesByStopIndex = new LinkedHashMap<>();

        // Loop through trips for trip pattern and time range and get running avg of all trip runtimes by stop index
        // Also adds a single scheduled trip to the realtime runtime data if only 1 runTime data sample is available
        for(Trip trip : trips){
            if(includeTripAsScheduledRunTime(trip, rtQuery, serviceTypesByServiceId)){
                // Supplement realtime runtime with schedule data if only 1 data sample is available
                if(allowSingleTripForRunTime(singleTripForSampleRunTime, existingRunTimeTripIds)){
                    singleTripForSampleRunTime.add(trip);
                }
                // Updates running avg of stopPath running time
                // Necessary to compare and calculate scheduled running time for custom time windows
                updateTripScheduleTimes(allTripScheduleTimesByStopIndex, trip.getScheduleTimes());
            }
        }

        // Average Schedule Times for all valid schedule trips
        List<ScheduleTime> avgTripScheduleTimesPerStop = getAvgTripScheduleTimes(allTripScheduleTimesByStopIndex);

        // CASE 1 : Has at least 1 complete realtime runTime result
        // Supplement realtime runTime data with scheduled runTime data as necessary
        addScheduledTripRunTimeToResults(singleTripForSampleRunTime, rtQuery, runTimesForRoutes);

        // CASE 2: Has 0 complete realtime runTime results for timeband
        // Get list of all runTimes for scheduled trips NOT included in the original historical results
        // Goal here is to include runTime information for trips that never got captured by the runtime query
        if(avgTripScheduleTimesPerStop.size() > 0) {
            try {
                // Put RunTimes and StopPaths into TimePoint RunTime Processor
                // Groups them into timepoints and gets runtime info for timepoints
                TimePointRunTimeProcessor timePointRunTimeProcessor =
                        getTimePointRunTimeProcessorWithStopPaths(runTimesForRoutes, tripPattern.getStopPaths());

                // Create Prescriptive RunTimeState using timepoint stats and stop paths info
                PrescriptiveRunTimeState prescriptiveRunTimeState =
                        createPrescriptiveRunTimeState(timePointRunTimeProcessor, avgTripScheduleTimesPerStop, arrivalDepartures);

                if (prescriptiveRunTimeState != null) {
                    return new PrescriptiveRunTimeStates(prescriptiveRunTimeState);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
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

    /**
     * Get a combined list of avg ScheduleTimes for all applicable scheduled trips
     * Used when calculating runTime for schedule trips
     * @param allTripScheduleTimesByIndex
     * @return
     */
    private List<ScheduleTime> getAvgTripScheduleTimes(Map<Integer, AvgScheduleTime> allTripScheduleTimesByIndex) {
        List<ScheduleTime> avgScheduleTimes = new ArrayList<>();
        for(AvgScheduleTime avgScheduleTime : allTripScheduleTimesByIndex.values()){
            avgScheduleTimes.add(avgScheduleTime.getAverageScheduleTime());
        }
        return avgScheduleTimes;
    }

    /**
     * Takes sample trip and creates single scheduledRunTime
     * Proceeds to add scheduledRunTime to list of actual runtimes (if available)
     * @param singleTripForSampleRunTime
     * @param rtQuery
     * @param results
     */
    private void addScheduledTripRunTimeToResults(List<Trip> singleTripForSampleRunTime,
                                                  RunTimeForRouteQuery rtQuery,
                                                  List<RunTimesForRoutes> results){

        if(!singleTripForSampleRunTime.isEmpty()) {
            List<RunTimesForRoutes> missingTripRunTimesForRoutes =
                    runTimeService.getScheduledRunTimesForRoutes(rtQuery, singleTripForSampleRunTime);
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


        try {
            if (trip.getStartTime() % 86400 < rtQuery.getBeginTime() || trip.getStartTime() % 86400 > rtQuery.getEndTime()) {
                return false;
            }
        }catch (Exception e){
            return false;
        }

        Set<ServiceType> serviceTypesForServiceId = serviceTypesByServiceId.get(trip.getServiceId());
        if(serviceTypesForServiceId == null || !serviceTypesForServiceId.contains(rtQuery.getServiceType())){
            return false;
        }

        return true;

    }

    private boolean allowSingleTripForRunTime(List<Trip> singleTripForSampleRunTime,
                                              Set<String> existingRunTimeTripIds){
        // We already have a single trip, skip
        if(!singleTripForSampleRunTime.isEmpty()){
            return false;
        }
        // We have more than 1 run time, don't need a sample schedule run time
        if(existingRunTimeTripIds.size() > 1){
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
            processor.updateScheduledOnlyStatus(runTimesForRoute.isScheduled());
        }

        return processor;
    }

    /**
     * Calculates the suggested schedule modifications
     * @param timePointRunTimeProcessor
     * @param scheduleTimes
     * @param arrivalDepartures
     * @return
     */
    private PrescriptiveRunTimeState createPrescriptiveRunTimeState(TimePointRunTimeProcessor timePointRunTimeProcessor,
                                                                    List<ScheduleTime> scheduleTimes,
                                                                    List<ArrivalDeparture> arrivalDepartures){

        // Get TimePoint stop paths, TimePoint runtime stats, and scheduledOnly info from TimePointProcessor
        Map<StopPathRunTimeKey, StopPath> timePointStopPaths = timePointRunTimeProcessor.getTimePointStopPaths();
        Map<StopPathRunTimeKey, TimePointStatistics> timePointsStatistics = timePointRunTimeProcessor.getSortedTimePointsStatistics();
        boolean isScheduledOnly = timePointRunTimeProcessor.getScheduledOnlyStatus();


        Map<Integer, ScheduleTime> scheduleTimesByStopPathIndexMap = createScheduledTimesGroupedById(scheduleTimes);

        Map<String, List<ArrivalDeparture>> arrivalDeparturesByStopPath = getArrivalDeparturesByStopPath(arrivalDepartures);

        PrescriptiveRunTimeState state = new PrescriptiveRunTimeState(scheduleTimesByStopPathIndexMap,
                arrivalDeparturesByStopPath, getAdjustedTimePositiveError(), getAdjustedTimeNegativeError());

        for(Map.Entry<StopPathRunTimeKey, StopPath> timePointStopPath : timePointStopPaths.entrySet()) {
            StopPathRunTimeKey key = timePointStopPath.getKey();
            TimePointStatistics timePointStatistics = timePointsStatistics.get(key);

            if (!isValid(timePointStatistics)) {
                logger.warn("No matching timepoint for {}, unable to provide prescriptive runtime", timePointStatistics);
                return null;
            } else {
                state.updateScheduleAdjustments(timePointStatistics, prescriptiveAvgIncludeFirstStop.getValue());
                state.createPrescriptiveRunTimeForTimePoint(isScheduledOnly);
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
        if(scheduleTimes.size() == 0){
            System.out.println("bad");
        }
        return IntStream.range(0, scheduleTimes.size())
                .boxed()
                .collect(Collectors.toMap(Function.identity(), scheduleTimes::get));
    }

    private boolean isValid(TimePointStatistics timePointStatistics){
        return timePointStatistics != null;
    }
}
