package org.transitclock.api.data.reporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.api.data.ApiRunTimeSummary;
import org.transitclock.api.data.reporting.chartjs.custom.TripAggregatedRunTimeData;
import org.transitclock.api.data.reporting.chartjs.custom.TripIndividualRunTimeData;
import org.transitclock.api.data.reporting.chartjs.custom.TripRunTimeMixedChart;
import org.transitclock.ipc.data.IpcRunTimeForTrip;
import org.transitclock.ipc.data.IpcRunTimeForTripsAndDistribution;
import org.transitclock.utils.Time;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.transitclock.api.utils.NumberFormatter.*;

@XmlRootElement
public class TripRunTimeOutput implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(TripRunTimeOutput.class);

    public TripRunTimeOutput() {}

    public static TripRunTimeMixedChart getRunTimes(List<IpcRunTimeForTrip> runTimeForTrips){
        TripAggregatedRunTimeData data = new TripAggregatedRunTimeData();
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

    public static TripRunTimeMixedChart getRunTimes(IpcRunTimeForTripsAndDistribution runTimeForTripsAndDistribution){
        TripAggregatedRunTimeData data = new TripAggregatedRunTimeData();
        Map<String, List<Long>> tripRunTimes = runTimeForTripsAndDistribution.getRunTimesForAllTrips();
        for(IpcRunTimeForTrip runTimeForTrip : sortRunTimes(runTimeForTripsAndDistribution.getAggregatedRunTimesForTrips())){
            data.getTripsList().add(getFormattedTripId(runTimeForTrip));
            data.getFixedList().add(getValueAsLong(runTimeForTrip.getFixed()));
            data.getVariableList().add(getValueAsLong(runTimeForTrip.getVariable()));
            data.getDwellList().add(getValueAsLong(runTimeForTrip.getDwell()));
            data.getScheduledList().add(getValueAsLong(runTimeForTrip.getScheduledTripCompletionTime()));
            data.getNextTripStartList().add(getValueAsLong(runTimeForTrip.getNextScheduledTripStartTime()));
            if(tripRunTimes != null && tripRunTimes.containsKey(runTimeForTrip.getTripId())){
                data.getTripRunTimes().add(new TripIndividualRunTimeData(tripRunTimes.get(runTimeForTrip.getTripId())));
            }
            data.setSummary(runTimeForTripsAndDistribution.getRunTimeSummary());
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
