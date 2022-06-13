package org.transitclock.api.data.reporting.prescriptive;

import org.transitclock.api.data.reporting.ReportDataFormatter;
import org.transitclock.ipc.data.IpcPrescriptiveRunTime;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimesForTimeBands;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;



public class PrescriptiveRunTimeOutput implements Serializable {

    public static PrescriptiveRunTimeDataAll getRunTimes(List<IpcPrescriptiveRunTimesForTimeBands> prescriptiveRunTimeBands){
        PrescriptiveRunTimeDataAll prescriptiveRunTimeDataAll = new PrescriptiveRunTimeDataAll();

        String routeShortName = null;
        for(IpcPrescriptiveRunTimesForTimeBands prescriptiveRunTimeBand : prescriptiveRunTimeBands){
            if(routeShortName == null && prescriptiveRunTimeBand.getRouteShortName() != null){
                routeShortName = prescriptiveRunTimeBand.getRouteShortName();
                prescriptiveRunTimeDataAll.setRouteShortName(routeShortName);
            }
            PrescriptiveRunTimeDataForPattern prescriptiveRunTimeDataForPattern = new PrescriptiveRunTimeDataForPattern(prescriptiveRunTimeBand);
            prescriptiveRunTimeDataAll.getDataForPatterns().add(prescriptiveRunTimeDataForPattern);
        }

        return prescriptiveRunTimeDataAll;
    }

    private static String getFormattedStopPath(IpcPrescriptiveRunTime prescriptiveRunTime){
        return ReportDataFormatter.formatStopPath(prescriptiveRunTime.getStopPathId());
    }

    private static String getFormattedPercent(Double value){
        return ReportDataFormatter.formatValueAsPercent(value);
    }

    private static List<IpcPrescriptiveRunTime> sortRunTimes(List<IpcPrescriptiveRunTime> prescriptiveRunTimes){
        return prescriptiveRunTimes.stream()
                .sorted(Comparator.comparingInt(IpcPrescriptiveRunTime::getStopPathIndex))
                .collect(Collectors.toList());
    }


}
