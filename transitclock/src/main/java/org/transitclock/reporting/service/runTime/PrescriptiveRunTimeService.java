package org.transitclock.reporting.service.runTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.ServiceType;
import org.transitclock.db.query.RunTimeForRouteQuery;
import org.transitclock.db.structs.*;
import org.transitclock.ipc.data.IpcPrescriptiveRunTime;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimes;
import org.transitclock.reporting.TimePointStatistics;
import org.transitclock.reporting.keys.StopPathRunTimeKey;
import org.transitclock.reporting.service.RunTimeService;

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

    private static final IntegerConfigValue prescriptiveHistoricalSearchDays = new IntegerConfigValue(
            "transitclock.runTime.prescriptiveHistoricalSearchDays",
            30,
            "The number of days in the past that prescriptive runtimes looks back.");

    private Integer historicalSearchDays(){
        return prescriptiveHistoricalSearchDays.getValue();
    }

    private static final Logger logger = LoggerFactory.getLogger(PrescriptiveRunTimeService.class);

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
    public IpcPrescriptiveRunTimes getPrescriptiveRunTimes(LocalTime beginTime,
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

        IpcPrescriptiveRunTimes prescriptiveRunTimes = getPrescriptiveRunTimeStats(timePointsStatistics,
                                                                                    timePointStopPaths,
                                                                                    tripPatternId);

        return prescriptiveRunTimes;
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
    private IpcPrescriptiveRunTimes getPrescriptiveRunTimeStats(Map<StopPathRunTimeKey, TimePointStatistics> timePointsStatistics,
                                                                     Map<StopPathRunTimeKey, StopPath> timePointStopPaths,
                                                                     String tripPatternId){

        Trip trip = getTrip(tripPatternId);
        Map<Integer, ScheduleTime> scheduleTimesByStopPathIndexMap = createScheduledTimesGroupedById(trip);

        PrescriptiveRunTimeState state = new PrescriptiveRunTimeState(scheduleTimesByStopPathIndexMap);

        for(Map.Entry<StopPathRunTimeKey, StopPath> timePointStopPath : timePointStopPaths.entrySet()) {
            StopPathRunTimeKey key = timePointStopPath.getKey();
            TimePointStatistics timePointStatistics = timePointsStatistics.get(key);

            if (!isValid(timePointStatistics)) {
                logger.warn("No matching timepoint for {}, unable to provide prescriptive runtime", timePointStatistics);
                return null;
            } else {
                state.updateScheduleAdjustments(timePointStatistics);
                state.createPrescriptiveRunTimeForTimePoint();
            }
        }

        List<IpcPrescriptiveRunTime> prescriptiveRunTimesList = state.getPrescriptiveRunTimes();

        IpcPrescriptiveRunTimes prescriptiveRunTimes = new IpcPrescriptiveRunTimes(prescriptiveRunTimesList,
                state.getCurrentOnTimeFraction(), state.getExpectedOnTimeFraction(), state.getAvgRunTimes());

        return prescriptiveRunTimes;
    }


    private Trip getTrip(String tripPatternId){
        List<String> tripIds = Core.getInstance().getDbConfig().getTripIdsForTripPattern(tripPatternId);
        List<Trip> trips = new ArrayList<>();
        for(String tripId : tripIds){
            trips.add(Core.getInstance().getDbConfig().getTrip(tripId));
        }

        Trip trip = trips.stream().findAny().orElse(null);
        return trip;
    }

    private Map<Integer, ScheduleTime> createScheduledTimesGroupedById(Trip trip){
        List<ScheduleTime> scheduleTimes = trip.getScheduleTimes();
        return IntStream.range(0, scheduleTimes.size())
                .boxed()
                .collect(Collectors.toMap(Function.identity(), scheduleTimes::get));
    }

    private boolean isValid(TimePointStatistics timePointStatistics){
        return timePointStatistics != null;
    }
}
