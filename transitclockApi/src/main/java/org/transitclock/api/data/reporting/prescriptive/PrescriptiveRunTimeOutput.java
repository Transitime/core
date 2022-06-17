package org.transitclock.api.data.reporting.prescriptive;

import org.transitclock.api.data.reporting.ReportDataFormatter;
import org.transitclock.api.utils.NumberFormatter;
import org.transitclock.ipc.data.IpcPrescriptiveRunTime;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimesForPattern;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimesForPatterns;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;



public class PrescriptiveRunTimeOutput implements Serializable {

    public static PrescriptiveRunTimeDataAll getRunTimes(IpcPrescriptiveRunTimesForPatterns prescriptiveRunTimesForPatterns){
        PrescriptiveRunTimeDataAll prescriptiveRunTimeDataAll = new PrescriptiveRunTimeDataAll();

        String routeShortName = null;
        for(IpcPrescriptiveRunTimesForPattern prescriptiveRunTimeBand : prescriptiveRunTimesForPatterns.getRunTimesForPatterns()){
            if(routeShortName == null && prescriptiveRunTimeBand.getRouteShortName() != null){
                routeShortName = prescriptiveRunTimeBand.getRouteShortName();
                prescriptiveRunTimeDataAll.setRouteShortName(routeShortName);
            }
            PrescriptiveRunTimeDataForPattern prescriptiveRunTimeDataForPattern = new PrescriptiveRunTimeDataForPattern(prescriptiveRunTimeBand);
            prescriptiveRunTimeDataAll.getDataForPatterns().add(prescriptiveRunTimeDataForPattern);
        }

        double currentOnTime = prescriptiveRunTimesForPatterns.getCurrentOnTime() / prescriptiveRunTimesForPatterns.getTotalRunTimes();
        double expectedOnTime = prescriptiveRunTimesForPatterns.getExpectedOnTime() / prescriptiveRunTimesForPatterns.getTotalRunTimes();

        prescriptiveRunTimeDataAll.setCurrentOnTime(NumberFormatter.getFractionAsPercentage(currentOnTime));
        prescriptiveRunTimeDataAll.setExpectedOnTime(NumberFormatter.getFractionAsPercentage(expectedOnTime));

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
