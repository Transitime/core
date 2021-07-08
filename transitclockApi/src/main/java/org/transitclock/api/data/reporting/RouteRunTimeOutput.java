package org.transitclock.api.data.reporting;

import org.apache.commons.lang3.text.WordUtils;
import org.transitclock.api.data.reporting.chartjs.custom.RouteRunTimeData;
import org.transitclock.api.data.reporting.chartjs.custom.RouteRunTimeMixedChart;
import org.transitclock.ipc.data.IpcRunTimeForRoute;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class RouteRunTimeOutput implements Serializable {

    public static RouteRunTimeMixedChart getRunTimes(List<IpcRunTimeForRoute> runTimeForRoutes){
        RouteRunTimeData data = new RouteRunTimeData();
        for(IpcRunTimeForRoute runTimeForRoute : sortRunTimes(runTimeForRoutes)){
            data.getRoutesList().add(getFormattedRouteId(runTimeForRoute));
            data.getEarlyList().add(Long.valueOf(runTimeForRoute.getEarlyCount()));
            data.getOnTimeList().add(Long.valueOf(runTimeForRoute.getOnTimeCount()));
            data.getLateList().add(Long.valueOf(runTimeForRoute.getLateCount()));
        }
        return new RouteRunTimeMixedChart(data);
    }

    private static String getFormattedRouteId(IpcRunTimeForRoute runTimeForRoute){
        return WordUtils.capitalizeFully(runTimeForRoute.getRouteShortName());
    }

    private static List<IpcRunTimeForRoute> sortRunTimes(List<IpcRunTimeForRoute> runTimeForRoutes){
        return runTimeForRoutes.stream()
                .sorted(Comparator.comparing(IpcRunTimeForRoute::getRouteShortName))
                .collect(Collectors.toList());
    }

}
