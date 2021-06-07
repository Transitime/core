package org.transitclock.api.data.reporting;

import org.transitclock.api.data.ApiRunTimeSummary;
import org.transitclock.ipc.data.IpcPrescriptiveRunTime;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimes;
import org.transitclock.ipc.data.IpcStopTime;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.transitclock.api.utils.NumberFormatter.getValueAsLong;


public class PrescriptiveRunTimeOutput implements Serializable {

    public static PrescriptiveRunTimeData getRunTimes(IpcPrescriptiveRunTimes prescriptiveRunTimes){
        PrescriptiveRunTimeData data = new PrescriptiveRunTimeData();
        for(IpcPrescriptiveRunTime prescriptiveRunTime : sortRunTimes(prescriptiveRunTimes.getPrescriptiveRunTimes())){
            data.getAdjustments().add(getScheduleAdjustment(prescriptiveRunTime));
        }
        data.setSummary(new ApiRunTimeSummary(prescriptiveRunTimes.getRunTimeSummary()));
        data.setCurrentOtp(getFormattedPercent(prescriptiveRunTimes.getCurrentOtp()));
        data.setExpectedOtp(getFormattedPercent(prescriptiveRunTimes.getExpectedOtp()));
        return data;
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

    private static PrescriptiveRunTimeAdjustment getScheduleAdjustment(IpcPrescriptiveRunTime prescriptiveRunTime) {
        return new PrescriptiveRunTimeAdjustment(prescriptiveRunTime.getStopName(),
                                                 getValueAsLong(prescriptiveRunTime.getScheduled()),
                                                 getValueAsLong(prescriptiveRunTime.getAdjustment()));

    }

}
