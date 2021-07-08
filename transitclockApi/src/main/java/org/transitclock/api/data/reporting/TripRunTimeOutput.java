package org.transitclock.api.data.reporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.api.data.reporting.chartjs.custom.TripRunTimeData;
import org.transitclock.api.data.reporting.chartjs.custom.TripRunTimeMixedChart;
import org.transitclock.ipc.data.IpcRunTimeForTrip;
import org.transitclock.utils.Time;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import static org.transitclock.api.utils.NumberFormatter.*;

@XmlRootElement
public class TripRunTimeOutput implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(TripRunTimeOutput.class);

    public TripRunTimeOutput() {}

    public static TripRunTimeMixedChart getRunTimes(List<IpcRunTimeForTrip> runTimeForTrips){
        TripRunTimeData data = new TripRunTimeData();
        for(IpcRunTimeForTrip runTimeForTrip : sortRunTimes(runTimeForTrips)){
            data.getTripsList().add(getFormattedTripId(runTimeForTrip));
            data.getFixedList().add(getValueAsLong(runTimeForTrip.getFixed()));
            data.getVariableList().add(getValueAsLong(runTimeForTrip.getVariable()));
            data.getDwellList().add(getValueAsLong(runTimeForTrip.getDwell()));
            data.getScheduledList().add(getValueAsLong(runTimeForTrip.getScheduledTripCompletionTime()));
            data.getNextTripStartList().add(getValueAsLong(runTimeForTrip.getNextScheduledTripStartTime()));
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
