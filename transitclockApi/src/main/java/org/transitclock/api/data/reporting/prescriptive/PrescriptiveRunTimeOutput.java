package org.transitclock.api.data.reporting.prescriptive;

import org.transitclock.api.data.ApiRunTimeSummary;
import org.transitclock.api.data.reporting.prescriptive.PrescriptiveRunTimeAdjustment;
import org.transitclock.api.data.reporting.prescriptive.PrescriptiveRunTimeData;
import org.transitclock.api.data.reporting.ReportDataFormatter;
import org.transitclock.ipc.data.IpcPrescriptiveRunTime;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimeBands;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;



public class PrescriptiveRunTimeOutput implements Serializable {

    public static List<PrescriptiveRunTimeData> getRunTimes(List<IpcPrescriptiveRunTimeBands> prescriptiveRunTimeBands){
        List<PrescriptiveRunTimeData> prescriptiveRunTimeDataList = new ArrayList<>();

        for(IpcPrescriptiveRunTimeBands prescriptiveRunTimeBand : prescriptiveRunTimeBands){
            PrescriptiveRunTimeData prescriptiveRunTimeData = new PrescriptiveRunTimeData(prescriptiveRunTimeBand);
            prescriptiveRunTimeDataList.add(prescriptiveRunTimeData);
        }

        return prescriptiveRunTimeDataList;
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
