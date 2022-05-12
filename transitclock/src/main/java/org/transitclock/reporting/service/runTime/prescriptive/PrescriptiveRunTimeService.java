package org.transitclock.reporting.service.runTime.prescriptive;

import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.ServiceType;
import org.transitclock.core.ServiceTypeUtil;
import org.transitclock.db.query.RunTimeForRouteQuery;
import org.transitclock.db.structs.*;
import org.transitclock.db.structs.Calendar;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.ipc.data.*;
import org.transitclock.reporting.TimePointStatistics;
import org.transitclock.reporting.keys.StopPathRunTimeKey;
import org.transitclock.reporting.service.RunTimeService;
import org.transitclock.reporting.service.runTime.PrescriptiveAdjustmentResult;
import org.transitclock.reporting.service.runTime.TimePointRunTimeProcessor;
import org.transitclock.reporting.service.runTime.prescriptive.helper.RunTimeDateRangeHelper;
import org.transitclock.reporting.service.runTime.prescriptive.model.PrescriptiveRunTimeState;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.model.TimebandTime;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.model.TimebandsForTripPattern;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
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

    private static final IntegerConfigValue prescriptiveHistoricalSearchDays = new IntegerConfigValue(
            "transitclock.runTime.prescriptiveHistoricalSearchDays",
            30,
            "The number of days in the past that prescriptive runtimes looks back.");

    private static final BooleanConfigValue prescriptiveAvgIncludeFirstStop = new BooleanConfigValue(
            "transitclock.runTime.prescriptiveAvgIncludeFirstStop",
            true,
            "Whether or not to include the first stop dwell time in the prescriptive runtime avg calculation.");

    private Integer historicalSearchDays(){
        return prescriptiveHistoricalSearchDays.getValue();
    }

    private static final Logger logger = LoggerFactory.getLogger(PrescriptiveRunTimeService.class);



    /**
     * Get Prescriptive RunTime Information for Run Times
     * @param routeIdOrShortName
     * @param serviceType
     * @param readOnly
     * @return
     * @throws Exception
     */
    public List<IpcPrescriptiveRunTimeBands> getPrescriptiveRunTimeBands(LocalDate beginDate,
                                                                         LocalDate endDate,
                                                                         String routeIdOrShortName,
                                                                         ServiceType serviceType,
                                                                         int configRev,
                                                                         boolean readOnly) throws Exception {

        // Prescriptive RunTimes
        List<IpcPrescriptiveRunTimeBands> prescriptiveRunTimesForRoute = new ArrayList<>();

        // Get timebands for trip pattern
        Map<String, TimebandsForTripPattern>  timebandsForTripPattern =
                timebandService.generateTimebands(beginDate, endDate, routeIdOrShortName, serviceType, readOnly);

        // Get list of all tripPatternIds
        List<TripPattern> tripPatterns = TripPattern.getTripPatternsForRoute(routeIdOrShortName, configRev, readOnly);


        // Go through tripPatterns and generate runtimes for tripPattern
        for(TripPattern tripPattern : tripPatterns){

            String tripPatternId = tripPattern.getId();

            // Populate Timepoints for trip pattern
            List<IpcStopPath> timePoints = new ArrayList<>();
            for(StopPath timePoint : tripPattern.getScheduleAdhStopPaths()){
                timePoints.add(new IpcStopPath(timePoint));
            }

            IpcPrescriptiveRunTimeBands prescriptiveRunTimeBands =new IpcPrescriptiveRunTimeBands(timePoints);

            // Check to see if tripPattern has associated timeband

            // Get the timebands for a trip pattern
            TimebandsForTripPattern timebands = timebandsForTripPattern.get(tripPattern.getId());

            getPrescriptiveRunTimeBand(timebands, timePoints, routeIdOrShortName, tripPatternId, serviceType, readOnly);

            prescriptiveRunTimesForRoute.add(prescriptiveRunTimeBands);

        }

        return prescriptiveRunTimesForRoute;
    }

    private List<IpcPrescriptiveRunTimeBand> getPrescriptiveRunTimeBand(TimebandsForTripPattern timebandsByPattern,
                                                                       List<IpcStopPath> timePoints,
                                                                       String routeIdOrShortName,
                                                                       String tripPatternId,
                                                                       ServiceType serviceType,
                                                                       boolean readOnly) throws Exception {

        List<IpcPrescriptiveRunTimeBand> prescriptiveRunTimeBands = new ArrayList<>();

        // If timeband is found add it to timebands list
        if(timebandsByPattern != null){
            // for each timeband create an IpcPrescriptiveRunTimeBand
            // this includes timeband start time, timeband end time and timepoint run time info
            for(TimebandTime timebandTime : timebandsByPattern.getTimebandTimes()){
                LocalTime beginTime = timebandTime.getStartTime();
                LocalTime endTime = timebandTime.getEndTime();

                // Get timepoint specific runtime info
                PrescriptiveRunTimeState prescriptiveRunTimeState =
                        getPrescriptiveRunTimeState(beginTime, endTime, routeIdOrShortName, null,
                                null, tripPatternId, serviceType, readOnly);

                // confirm that the run time info lines up with the expected timepoints
                // if it doesn't match up then skip
                if(hasValidTimePoints(prescriptiveRunTimeState.getPrescriptiveRunTimes(), timePoints)){
                    continue;
                }

                // build IpcPrescriptiveRunTimeBand using runtime info and timeband info
                IpcPrescriptiveRunTimeBand prescriptiveRunTimeBand =
                        new IpcPrescriptiveRunTimeBand(beginTime.toString(), endTime.toString(),
                                prescriptiveRunTimeState.getPrescriptiveRunTimes());

                //add timeband to our timebands list
                prescriptiveRunTimeBands.add(prescriptiveRunTimeBand);
            }
        }

        return prescriptiveRunTimeBands;

    }

    private boolean hasValidTimePoints(List<IpcPrescriptiveRunTime> prescriptiveRunTimes,
                                    List<IpcStopPath> timePoints) {

        if(prescriptiveRunTimes.size() != timePoints.size()){
            return false;
        }

        for(int i=0; i < prescriptiveRunTimes.size(); i++){
            IpcPrescriptiveRunTime runTimeStopPath = prescriptiveRunTimes.get(i);
            IpcStopPath timePoint = timePoints.get(i);
            if(!runTimeStopPath.getStopPathId().equals(timePoint.getStopPathId())){
                return false;
            }
        }
        return true;
    }


    /**
     * Get Prescriptive RunTime Information for Run Times
     * @param beginTime
     * @param endTime
     * @param routeIdOrShortName
     * @param headsign
     * @param directionId
     * @param tripPatternId
     * @param serviceType
     * @param readOnly
     * @return
     * @throws Exception
     */
    public IpcPrescriptiveRunTimes getPrescriptiveRunTimes(LocalDate beginDate,
                                                           LocalDate endDate,
                                                           LocalTime beginTime,
                                                           LocalTime endTime,
                                                           String routeIdOrShortName,
                                                           String headsign,
                                                           String directionId,
                                                           String tripPatternId,
                                                           ServiceType serviceType,
                                                           int configRev,
                                                           boolean readOnly) throws Exception {





        PrescriptiveRunTimeState prescriptiveRunTimeState = getPrescriptiveRunTimeState(beginTime,
                                                                                        endTime,
                                                                                        routeIdOrShortName,
                                                                                        headsign,
                                                                                        directionId,
                                                                                        tripPatternId,
                                                                                        serviceType,
                                                                                        readOnly);

        IpcPrescriptiveRunTimes prescriptiveRunTimes = new IpcPrescriptiveRunTimes(
                                                                prescriptiveRunTimeState.getPrescriptiveRunTimes(),
                                                                prescriptiveRunTimeState.getCurrentOnTimeFraction(),
                                                                prescriptiveRunTimeState.getExpectedOnTimeFraction(),
                                                                prescriptiveRunTimeState.getAvgRunTimes());


        return prescriptiveRunTimes;
    }

    private PrescriptiveRunTimeState getPrescriptiveRunTimeState(LocalTime beginTime,
                                                                 LocalTime endTime,
                                                                 String routeIdOrShortName,
                                                                 String headsign,
                                                                 String directionId,
                                                                 String tripPatternId,
                                                                 ServiceType serviceType,
                                                                 boolean readOnly) throws Exception {

        String routeShortName = getRouteShortName(routeIdOrShortName);
        Period daysToLookBack = Period.ofDays(historicalSearchDays());
        LocalDate endDate = LocalDate.now();
        LocalDate beginDate = endDate.minus(daysToLookBack);

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

        List<RunTimesForRoutes> runTimesForRoutes = runTimeService.getRunTimesForRoutes(rtQuery);

        TimePointRunTimeProcessor timePointRunTimeProcessor = processTimePointStatsForRunTimes(runTimesForRoutes, tripPatternId);

        Map<StopPathRunTimeKey, TimePointStatistics> timePointsStatistics =
                timePointRunTimeProcessor.getSortedTimePointsStatistics();

        Map<StopPathRunTimeKey, StopPath> timePointStopPaths = timePointRunTimeProcessor.getTimePointStopPaths();

        return getPrescriptiveRunTimeStateFromTimepoints(timePointsStatistics, timePointStopPaths, tripPatternId, serviceType);
    }




    /**
     * Process RunTimesForStops to group them into timepoints and generate
     * runTime stat info for each timepoint grouping
     * @param runTimesForRoutes
     * @param tripPatternId
     * @return
     */
    private TimePointRunTimeProcessor processTimePointStatsForRunTimes(List<RunTimesForRoutes> runTimesForRoutes,
                                                                        String tripPatternId) {
        TripPattern tripPattern = Core.getInstance().getDbConfig().getTripPatternForId(tripPatternId);
        TimePointRunTimeProcessor processor =  new TimePointRunTimeProcessor(tripPattern.getStopPaths());

        for(RunTimesForRoutes runTimesForRoute : runTimesForRoutes){
            processor.addRunTimesForStops(runTimesForRoute.getRunTimesForStops());
        }

        return processor;
    }

    /**
     * Calculates the suggested schedule modifications by retreiving appropriate variable, dwellTime, and remainder
     * for each timepoint segment (based on their index) and applying that to the scheduled times.
     * @param timePointsStatistics
     * @param timePointStopPaths
     * @param tripPatternId
     * @return
     */
    private PrescriptiveRunTimeState getPrescriptiveRunTimeStateFromTimepoints(Map<StopPathRunTimeKey, TimePointStatistics> timePointsStatistics,
                                                                               Map<StopPathRunTimeKey, StopPath> timePointStopPaths,
                                                                               String tripPatternId,
                                                                               ServiceType serviceType){

        Trip trip = getTrip(tripPatternId, serviceType);
        Map<Integer, ScheduleTime> scheduleTimesByStopPathIndexMap = createScheduledTimesGroupedById(trip);

        PrescriptiveRunTimeState state = new PrescriptiveRunTimeState(scheduleTimesByStopPathIndexMap);

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

    /**
     * Return first trip available trip for TripPatternId for specified serviceType
     * @param tripPatternId
     * @return
     */
    private Trip getTrip(String tripPatternId, ServiceType serviceType){
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
     * @param trip
     * @return
     */
    private Map<Integer, ScheduleTime> createScheduledTimesGroupedById(Trip trip){
        List<ScheduleTime> scheduleTimes = trip.getScheduleTimes();
        return IntStream.range(0, scheduleTimes.size())
                .boxed()
                .collect(Collectors.toMap(Function.identity(), scheduleTimes::get));
    }

    private boolean isValid(TimePointStatistics timePointStatistics){
        return timePointStatistics != null;
    }

    /**
     * Get Prescriptive RunTime Information for Run Times
     * @param beginTime
     * @param endTime
     * @param routeIdOrShortName
     * @param headsign
     * @param directionId
     * @param tripPatternId
     * @param readOnly
     * @return
     * @throws Exception
     */
    public List<IpcStopTime> getPrescriptiveRunTimesSchedule(LocalTime beginTime,
                                                             LocalTime endTime,
                                                             String routeIdOrShortName,
                                                             String headsign,
                                                             String directionId,
                                                             String tripPatternId,
                                                             ServiceType serviceType,
                                                             boolean readOnly) throws Exception {

        PrescriptiveRunTimeState prescriptiveRunTimeState = getPrescriptiveRunTimeState(beginTime,
                                                                                        endTime,
                                                                                        routeIdOrShortName,
                                                                                        headsign,
                                                                                        directionId,
                                                                                        tripPatternId,
                                                                                        serviceType,
                                                                                        readOnly);

        Map<Integer, IpcPrescriptiveRunTime> prescriptiveRunTimes =
                prescriptiveRunTimeState.getPrescriptiveRunTimesByStopPathIndex();

        PrescriptiveAdjustmentResult result = getPrescriptiveAdjustmentResult(prescriptiveRunTimes);

        List<IpcStopTime> stopTimes = getStopTimesWithScheduleAdjustments(tripPatternId, result, serviceType);

        return stopTimes;
    }

    private PrescriptiveAdjustmentResult getPrescriptiveAdjustmentResult(Map<Integer, IpcPrescriptiveRunTime> prescriptiveRunTimes){
        PrescriptiveAdjustmentResult result = new PrescriptiveAdjustmentResult();
        for(Map.Entry<Integer, IpcPrescriptiveRunTime> entry : prescriptiveRunTimes.entrySet()){
            Integer currentStopPathIndex = entry.getKey();
            IpcPrescriptiveRunTime prescriptiveRunTime = entry.getValue();
            result.addScheduleAdjustmentsToStops(currentStopPathIndex, prescriptiveRunTime);
        }
    return result;
    }

    private List<IpcStopTime> getStopTimesWithScheduleAdjustments(String tripPatternId,
                                                                  PrescriptiveAdjustmentResult result,
                                                                  ServiceType serviceType){

        // Get TripPattern and associated stopPaths and tripIds
        TripPattern tripPattern = Core.getInstance().getDbConfig().getTripPatternForId(tripPatternId);
        List<StopPath> stopPaths = tripPattern.getStopPaths();
        List<String> tripIds = Core.getInstance().getDbConfig().getTripIdsForTripPattern(tripPatternId);

        Map<Integer, Double> scheduleAdjustmentMap = result.getScheduleAdjustmentByStopPathIndex();

        // Get map of all tripPattern trips mapped by tripId
        Map<String, Trip> tripPatternTripsByTripId = getTripsByIdFromTripIds(tripIds, serviceType);

        // Use list to block and all associated trips for block
        // This is necessary since we may have to adjust start times for other trips on block
        // if trips with schedule adjustments run longer than originally scheduled
        Map<String, Map<String,Trip>> tripsByBlockId = getTripsByBlockId(tripPatternTripsByTripId.values());

        PrescriptiveScheduleProcessor processor = new PrescriptiveScheduleProcessor(tripPatternTripsByTripId,
                                                                                    scheduleAdjustmentMap,
                                                                                    stopPaths);

        processor.processTripsForBlocks(tripsByBlockId);

        List<IpcStopTime> stopTimes = processor.getStopTimes();

        return stopTimes;
    }

    private Map<String, Trip> getTripsByIdFromTripIds(List<String> tripIds, ServiceType serviceType){
        Map<String, Trip> trips = new HashMap<>();
        for(String tripId : tripIds) {
            DbConfig dbConfig = Core.getInstance().getDbConfig();
            Trip trip = dbConfig.getTrip(tripId);
            Calendar calendar = dbConfig.getCalendarByServiceId(trip.getServiceId());
            if(ServiceTypeUtil.isCalendarValidForServiceType(calendar, serviceType)){
                trips.put(trip.getId(),trip);
            }
        }
        return trips;
    }

    private Map<String, Map<String,Trip>> getTripsByBlockId(Collection<Trip> trips){
        Map<String, Map<String,Trip>> tripsByBlockIdMap = new HashedMap();
        for(Trip trip : trips) {
            Map<String,Trip> tripByTripId = tripsByBlockIdMap.get(trip.getBlockId());
            if(tripByTripId == null){
                Block block = Core.getInstance().getDbConfig().getBlock(trip.getServiceId(), trip.getBlockId());
                tripsByBlockIdMap.put(block.getId(), getTripById(block.getTrips()));
            }
        }
        return tripsByBlockIdMap;
    }

    private Map<String,Trip> getTripById(List<Trip> trips){
        Map<String, Trip> tripsByTripId = new LinkedHashMap<>();
        for(Trip trip : trips){
            tripsByTripId.put(trip.getId(), trip);
        }
        return tripsByTripId;
    }
}
