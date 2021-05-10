package org.transitclock.api.data.reporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.api.data.reporting.chartjs.custom.StopPathRunTimeData;
import org.transitclock.api.data.reporting.chartjs.custom.StopPathRunTimeMixedChart;
import org.transitclock.ipc.data.IpcRunTimeForStopPath;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import static org.transitclock.api.utils.NumberFormatter.*;


public class StopPathRunTimeOutput implements Serializable {

    public static StopPathRunTimeMixedChart getRunTimes(List<IpcRunTimeForStopPath> runTimeForStopPaths){
        StopPathRunTimeData data = new StopPathRunTimeData();
        for(IpcRunTimeForStopPath runTimeForStopPath : sortRunTimes(runTimeForStopPaths)){
            data.getStopPathsList().add(getFormattedStopPath(runTimeForStopPath));
            data.getStopNames().add(runTimeForStopPath.getStopName());
            data.getFixedList().add(getValueAsLong(runTimeForStopPath.getFixed()));
            data.getVariableList().add(getValueAsLong(runTimeForStopPath.getVariable()));
            data.getDwellList().add(getValueAsLong(runTimeForStopPath.getDwell()));
            data.getScheduledList().add(getValueAsLong(runTimeForStopPath.getScheduledCompletionTime()));
        }
        return new StopPathRunTimeMixedChart(data);
    }

    private static String getFormattedStopPath(IpcRunTimeForStopPath runTimeForStopPath){
        return ReportDataFormatter.formatStopPath(runTimeForStopPath.getStopPathId());
    }

    private static List<IpcRunTimeForStopPath> sortRunTimes(List<IpcRunTimeForStopPath> runTimeForStopPaths){
        return runTimeForStopPaths.stream()
                .sorted(Comparator.comparingInt(IpcRunTimeForStopPath::getStopPathIndex))
                .collect(Collectors.toList());
    }

}
