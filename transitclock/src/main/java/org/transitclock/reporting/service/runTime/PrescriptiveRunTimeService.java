package org.transitclock.reporting.service.runTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.ServiceType;
import org.transitclock.db.query.RunTimeForRouteQuery;
import org.transitclock.db.structs.*;
import org.transitclock.ipc.data.IpcPrescriptiveRunTime;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimes;
import org.transitclock.reporting.TimePointStatistics;
import org.transitclock.reporting.dao.RunTimeRoutesDao;
import org.transitclock.reporting.keys.StopPathRunTimeKey;
import org.transitclock.statistics.Statistics;
import org.transitclock.statistics.StatisticsV2;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.transitclock.ipc.util.GtfsDbDataUtil.getRouteShortName;
import static org.transitclock.reporting.service.runTime.PrescriptiveRunTimeHelper.*;

public class PrescriptiveRunTimeService {

    @Inject
    private RunTimeRoutesDao dao;

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

        Integer beginTimeSeconds = beginTime != null ? beginTime.toSecondOfDay() : null;
        Integer endTimeSeconds = endTime != null ? endTime.toSecondOfDay(): null;

        Period days_30 = Period.ofDays(30);
        LocalDate endDate = LocalDate.now();
        LocalDate beginDate = endDate.minus(days_30);

        RunTimeForRouteQuery.Builder rtBuilder = new RunTimeForRouteQuery.Builder();
        RunTimeForRouteQuery rtQuery = rtBuilder
                .beginDate(beginDate)
                .endDate(endDate)
                .beginTime(beginTimeSeconds)
                .endTime(endTimeSeconds)
                .serviceType(serviceType)
                .routeShortName(routeShortName)
                .headsign(headsign)
                .directionId(directionId)
                .tripPatternId(tripPatternId)
                .includeRunTimesForStops(true)
                .readOnly(readOnly)
                .build();

        List<RunTimesForRoutes> runTimesForRoutes = dao.getRunTimesForRoutes(rtQuery);

        TimePointRunTimeProcessor timePointRunTimeProcessor = processTripPatternStats(runTimesForRoutes, tripPatternId);

        Map<StopPathRunTimeKey, TimePointStatistics> timePointsStatistics =
                timePointRunTimeProcessor.getSortedTimePointsStatistics();

        Map<StopPathRunTimeKey, StopPath> timePointStopPaths = timePointRunTimeProcessor.getTimePointStopPaths();

        List<IpcPrescriptiveRunTime> prescriptiveRunTimesList = getPrescriptiveRunTimeStats(timePointsStatistics,
                                                                    timePointStopPaths, tripPatternId);

        IpcPrescriptiveRunTimes prescriptiveRunTimes = createPrescriptiveRunTimes(prescriptiveRunTimesList);

        return prescriptiveRunTimes;
    }



    /**
     * Process run time stats information for all of the runtimes for routes
     * @param runTimesForRoutes
     * @param tripPatternId
     * @return
     */
    private TimePointRunTimeProcessor processTripPatternStats(List<RunTimesForRoutes> runTimesForRoutes,
                                                              String tripPatternId) {
        TripPattern tripPattern = Core.getInstance().getDbConfig().getTripPatternForId(tripPatternId);
        TimePointRunTimeProcessor processor =  new TimePointRunTimeProcessor(tripPattern.getStopPaths());

        for(RunTimesForRoutes runTimesForRoute : runTimesForRoutes){
            processor.addRunTimesForStops(runTimesForRoute.getRunTimesForStops());
        }

        return processor;
    }


    private List<IpcPrescriptiveRunTime> getPrescriptiveRunTimeStats(Map<StopPathRunTimeKey, TimePointStatistics> timePointsStatistics,
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
                return Collections.EMPTY_LIST;
            } else {
                state.updateTimePointStats(timePointStatistics);

                List<Double> allRunTimes = StatisticsV2.eliminateOutliers(timePointStatistics.getAllRunTimes(), 2.0f);
                List<Double> allDwellTimes = StatisticsV2.eliminateOutliers(timePointStatistics.getAllDwellTimes(),2.0f);

                boolean isFirstStop = timePointStatistics.isFirstStop();
                boolean isLastStop = timePointStatistics.isLastStop();

                if(!isFirstStop) {
                    Double fixedTime = timePointStatistics.getMinRunTime();
                    state.addFixedTime(fixedTime);
                    state.addDwellTime(getDwellPercentileValue(allDwellTimes, isLastStop));
                    state.addVariableTime(getVariablePercentileValue(fixedTime, allRunTimes, state.getCurrentTimePointIndex(), isLastStop));
                    state.addRemainder(getRemainderPercentileValue(fixedTime, allRunTimes, state.getCurrentTimePointIndex(), isLastStop));
                }
                state.createRunTimeForTimePoint();
            }
        }
        return state.getPrescriptiveRunTimes();

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

    private IpcPrescriptiveRunTimes createPrescriptiveRunTimes(List<IpcPrescriptiveRunTime> prescriptiveRunTimesList) {
        return new IpcPrescriptiveRunTimes(prescriptiveRunTimesList, getCurrentOtp(), getExpectedOtp());
    }

    private Double getCurrentOtp(){
        return 0.85;
    }

    private Double getExpectedOtp(){
        return 0.9;
    }
}
