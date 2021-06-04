package org.transitclock.reporting.service.runTime;

import org.transitclock.core.ServiceType;
import org.transitclock.db.query.RunTimeForRouteQuery;
import org.transitclock.db.structs.RunTimesForRoutes;
import org.transitclock.ipc.data.IpcRunTimeForRoute;
import org.transitclock.reporting.RouteStatistics;
import org.transitclock.reporting.service.RunTimeService;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RouteRunTimesService {

    @Inject
    RunTimeService runTimeService;

    /**
     * Get RunTime Information for Trip Stop Paths
     * @param beginDate
     * @param endDate
     * @param beginTime
     * @param endTime
     * @param serviceType
     * @param readOnly
     * @return
     */
    public List<IpcRunTimeForRoute> getRunTimeForRoutes(LocalDate beginDate,
                                                        LocalDate endDate,
                                                        LocalTime beginTime,
                                                        LocalTime endTime,
                                                        ServiceType serviceType,
                                                        Integer earlyThreshold,
                                                        Integer lateThreshold,
                                                        boolean readOnly) throws Exception {

        RunTimeForRouteQuery.Builder rtBuilder = new RunTimeForRouteQuery.Builder();
        RunTimeForRouteQuery rtQuery = rtBuilder
                .beginDate(beginDate)
                .endDate(endDate)
                .beginTime(beginTime)
                .endTime(endTime)
                .serviceType(serviceType)
                .includeRunTimesForStops(false)
                .readOnly(readOnly)
                .build();

        List<RunTimesForRoutes> runTimesForRoutes = runTimeService.getRunTimesForRoutes(rtQuery);

        Map<String, RouteStatistics> routeStatisticsByRouteName = getRouteRunTimeStatistics(runTimesForRoutes, earlyThreshold, lateThreshold);

        List<IpcRunTimeForRoute> ipcRunTimeForRoutes = getRunTimeStatsForRoutes(routeStatisticsByRouteName);

        return ipcRunTimeForRoutes;
    }

    private Map<String, RouteStatistics> getRouteRunTimeStatistics(List<RunTimesForRoutes> runTimesForRoutes,
                                                                   Integer earlyThresholdSec,
                                                                   Integer lateThresholdSec) {
        Map<String, RouteStatistics> routeStatisticsByRouteName = new HashMap<>();

        for (RunTimesForRoutes rt : runTimesForRoutes) {
            if (rt.getRouteShortName() == null || !rt.hasAllScheduledAndActualTimes() && !rt.hasCompleteRunTime()) {
                continue;
            }

            Long runTime = rt.getRunTime();
            if (runTime == null) {
                continue;
            }

            RouteStatistics routeStatistics = routeStatisticsByRouteName.get(rt.getRouteShortName());
            if (routeStatistics == null) {
                routeStatistics = new RouteStatistics();
                routeStatisticsByRouteName.put(rt.getRouteShortName(), routeStatistics);
            }
            long runTimeSec = TimeUnit.MILLISECONDS.toSeconds(runTime);
            int scheduledRunTimeSec = rt.getScheduledEndTime() - rt.getScheduledStartTime();
            long runTimeDiff = runTimeSec - scheduledRunTimeSec;
            if (runTimeDiff < 0 && Math.abs(runTimeDiff) > earlyThresholdSec) {
                routeStatistics.addEarly();
            } else if (runTimeDiff > 0 && runTimeDiff > lateThresholdSec) {
                routeStatistics.addLate();
            } else {
                routeStatistics.addOnTime();
            }
        }


        return routeStatisticsByRouteName;
    }


    private List<IpcRunTimeForRoute> getRunTimeStatsForRoutes(Map<String, RouteStatistics> routeRunTimeStatistics) {
        List<IpcRunTimeForRoute> ipcRunTimeForRoutes = new ArrayList<>();
        for(Map.Entry<String, RouteStatistics> rtStats : routeRunTimeStatistics.entrySet()){
            String routeName = rtStats.getKey();
            RouteStatistics stats = rtStats.getValue();
            ipcRunTimeForRoutes.add(new IpcRunTimeForRoute(routeName, stats.getEarlyCount(), stats.getOnTimeCount(),
                    stats.getLateCount()));

        }
        return ipcRunTimeForRoutes;
    }

}
