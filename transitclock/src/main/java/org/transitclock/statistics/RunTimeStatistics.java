package org.transitclock.statistics;

import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.DoubleConfigValue;
import org.transitclock.config.FloatConfigValue;
import org.transitclock.db.structs.RunTimesForRoutes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RunTimeStatistics {

    private static BooleanConfigValue useScheduleOutlierFilter = new BooleanConfigValue(
            "transitclock.reporting.runTime.useScheduleFilter", true,
            "When filtering outliers, can use scheduleOutlierFilter standardDeviationFilter. If set to" +
                    "true the scheduleOutlierFilter is used, otherwise the standardDeviationFilter is used.");

    private static Boolean useScheduleFilterType(){
        return useScheduleOutlierFilter.getValue();
    }

    private static FloatConfigValue runTimeScaleOfElimination = new FloatConfigValue(
            "transitclock.reporting.runTime.scaleOfElimination", 1.5f,
            "Configurable value for scale of elimination when filtering run time outliers.");
    private static Float getRunTimeScaleOfElimination(){
        return runTimeScaleOfElimination.getValue();
    }

    private static DoubleConfigValue lowerBoundScheduleFraction = new DoubleConfigValue(
            "transitclock.reporting.runTime.lowerBoundScheduleFraction", .5d,
            "Configurable value for lower bound when filtering runTime outliers when comparing against the" +
                    "schedule. For example if the scheduled runTime is 50 minutes and the lower bound fraction is" +
                    "set to .5, then any trips with a runTime less than 25 minutes are considered outliers and" +
                    "removed.");
    private static Double getLowerBoundScheduleFraction(){
        return lowerBoundScheduleFraction.getValue();
    }

    private static DoubleConfigValue upperBoundScheduleFraction = new DoubleConfigValue(
            "transitclock.reporting.runTime.upperBoundScheduleFraction", 2d,
            "Configurable value for upper bound when filtering runTime outliers when comparing against the" +
                    "schedule. For example if the scheduled runTime is 50 minutes and the upper bound fraction is" +
                    "set to 2, then any trips with a runTime greater than 100 minutes are considered outliers and" +
                    "removed.");
    private static Double getUpperBoundScheduleFraction(){
        return upperBoundScheduleFraction.getValue();
    }

    public static List<RunTimesForRoutes> filterRunTimesForRoutes(List<RunTimesForRoutes> runTimeForRoutes){

        Map<String, List<RunTimesForRoutes>> runTimesByTripId = new HashMap<>();
        for(RunTimesForRoutes rt: runTimeForRoutes){
            if(rt.hasCompleteRunTime() && rt.hasAllScheduledAndActualTimes()){
                List<RunTimesForRoutes> runTimesForRoutesList = runTimesByTripId.get(rt.getTripId());
                if(runTimesForRoutesList == null){
                    runTimesForRoutesList = new ArrayList<>();
                    runTimesByTripId.put(rt.getTripId(), runTimesForRoutesList);
                }
                runTimesForRoutesList.add(rt);
            }
        }

        List<RunTimesForRoutes> filteredRunTimesForRoute = new ArrayList<>();
        for(List<RunTimesForRoutes> completeRunTimesForRoutes : runTimesByTripId.values()){
            if(useScheduleFilterType()){
                filteredRunTimesForRoute.addAll(eliminateRunTimeOutliersSchedule(completeRunTimesForRoutes));
            }
            else{
                filteredRunTimesForRoute.addAll(eliminateRunTimeOutliersStdDev(completeRunTimesForRoutes));
            }
        }

        return filteredRunTimesForRoute;
    }

    private static List<RunTimesForRoutes> eliminateRunTimeOutliersSchedule(List<RunTimesForRoutes> runTimeForRoutes){

        List<RunTimesForRoutes> filteredRunTimesForRoutes = new ArrayList<>();
        for (RunTimesForRoutes rt : runTimeForRoutes) {
            if(rt.getScheduledEndTime() != null && rt.getScheduledStartTime() != null){
                int scheduledRunTime = rt.getScheduledEndTime() - rt.getScheduledStartTime();

                boolean isLessThanLowerBound = rt.getRunTime() < (scheduledRunTime * getLowerBoundScheduleFraction() * 1000);
                boolean isGreaterThanUpperBound = rt.getRunTime() > (scheduledRunTime * getUpperBoundScheduleFraction() * 1000);
                boolean isOutOfBounds = isLessThanLowerBound || isGreaterThanUpperBound;

                if (!isOutOfBounds) {
                    filteredRunTimesForRoutes.add(rt);
                }
            }
        }
        return filteredRunTimesForRoutes;
    }

    private static List<RunTimesForRoutes> eliminateRunTimeOutliersStdDev(List<RunTimesForRoutes> runTimeForRoutes){
        if(runTimeForRoutes.size() <= 2){
            return runTimeForRoutes;
        }

        List<Double> runTimes = runTimeForRoutes.stream()
                                    .map(rt -> rt.getRunTime().doubleValue())
                                    .collect(Collectors.toList());



        List<RunTimesForRoutes> filteredRunTimesForRoutes = new ArrayList<>();
        double mean = StatisticsV2.getMean(runTimes);
        double stdDev = StatisticsV2.getStdDev(runTimes, mean);
        float scaleOfElimination = getRunTimeScaleOfElimination();

        for (RunTimesForRoutes rt : runTimeForRoutes) {
            boolean isLessThanLowerBound = rt.getRunTime() < mean - stdDev * scaleOfElimination;
            boolean isGreaterThanUpperBound = rt.getRunTime() > mean + stdDev * scaleOfElimination;
            boolean isOutOfBounds = isLessThanLowerBound || isGreaterThanUpperBound;

            if (!isOutOfBounds) {
                filteredRunTimesForRoutes.add(rt);
            }
        }

        int countOfOutliers = runTimeForRoutes.size() - filteredRunTimesForRoutes.size();
        if (countOfOutliers == 0) {
            return runTimeForRoutes;
        }

        return eliminateRunTimeOutliersStdDev(filteredRunTimesForRoutes);
    }
}
