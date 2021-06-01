package org.transitclock.statistics;

import org.transitclock.db.structs.RunTimesForRoutes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RunTimeStatistics {

    public static List<RunTimesForRoutes> filterRunTimesForRoutes(List<RunTimesForRoutes> runTimeForRoutes,
                                                                  float scaleOfElimination){

        List<RunTimesForRoutes> completeRunTimesForRoutes = runTimeForRoutes.stream()
                .filter(rt -> rt.hasCompleteRunTime() && rt.hasAllScheduledAndActualTimes())
                .collect(Collectors.toList());


        return eliminateRunTimeOutliers(completeRunTimesForRoutes, scaleOfElimination);
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
