package org.transitclock.statistics;

import org.transitclock.db.structs.RunTimesForRoutes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RunTimeStatistics {

    public static List<RunTimesForRoutes> filterRunTimesForRoutes(List<RunTimesForRoutes> runTimeForRoutes,
                                                                  float scaleOfElimination){

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
            filteredRunTimesForRoute.addAll(eliminateRunTimeOutliers(completeRunTimesForRoutes, scaleOfElimination));
        }

        return filteredRunTimesForRoute;
    }

    private static List<RunTimesForRoutes> eliminateRunTimeOutliers(List<RunTimesForRoutes> runTimeForRoutes,
                                                                    float scaleOfElimination){
        List<Double> runTimes = runTimeForRoutes.stream()
                                    .map(rt -> rt.getRunTime().doubleValue())
                                    .collect(Collectors.toList());

        List<RunTimesForRoutes> filteredRunTimesForRoutes = new ArrayList<>();
        double mean = StatisticsV2.getMean(runTimes);
        double stdDev = StatisticsV2.getStdDev(runTimes, mean);

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

        return eliminateRunTimeOutliers(filteredRunTimesForRoutes,scaleOfElimination);
    }
}
