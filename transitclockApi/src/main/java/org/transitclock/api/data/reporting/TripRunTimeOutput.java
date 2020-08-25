package org.transitclock.api.data.reporting;

import org.transitclock.api.data.reporting.chartjs.custom.TripRunTimeData;
import org.transitclock.api.data.reporting.chartjs.custom.TripRunTimeMixedChart;
import org.transitclock.ipc.data.IpcRunTimeForTrip;
import org.transitclock.utils.Time;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@XmlRootElement
public class TripRunTimeOutput implements Serializable {

    public TripRunTimeOutput() {}

    public static TripRunTimeMixedChart  getAvgTripRunTimes(List<IpcRunTimeForTrip> runTimeForTrips){
        TripRunTimeData data = new TripRunTimeData();
        for(IpcRunTimeForTrip runTimeForTrip : sortRunTimes(runTimeForTrips)){
            data.getTripsList().add(getFormattedTripId(runTimeForTrip));
            data.getFixedList().add(new BigDecimal(runTimeForTrip.getFixed()));
            data.getVariableList().add(new BigDecimal(runTimeForTrip.getVariable()));
            data.getDwellList().add(new BigDecimal(runTimeForTrip.getDwell()));
            data.getScheduledList().add(new BigDecimal(runTimeForTrip.getScheduledTripCompletionTime()));
            data.getNextTripStartList().add(new BigDecimal(runTimeForTrip.getNextScheduledTripStartTime()));
        }
        return new TripRunTimeMixedChart(data);
    }

    private static String getFormattedTripId(IpcRunTimeForTrip runTimeForTrip){
        return Time.timeOfDayShortStr(runTimeForTrip.getScheduledTripStartTime()) + " - " + runTimeForTrip.getTripId();
    }

    private static List<IpcRunTimeForTrip> sortRunTimes(List<IpcRunTimeForTrip> runTimeForTrips){
        return runTimeForTrips.stream()
                .sorted(Comparator.comparingInt(IpcRunTimeForTrip::getScheduledTripStartTime))
                .collect(Collectors.toList());
    }
}
