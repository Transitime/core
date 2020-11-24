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

@XmlRootElement
public class TripRunTimeOutput implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(TripRunTimeOutput.class);

    public TripRunTimeOutput() {}

    public static TripRunTimeMixedChart  getAvgTripRunTimes(List<IpcRunTimeForTrip> runTimeForTrips){
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

    private static Long getValueAsLong(Double value){
        if(value != null){
            try{
                return value.longValue();
            } catch (NumberFormatException nfe){
                logger.warn("Unable to convert {} to a Long do to a number format exception",value, nfe);
            } catch(Exception e){
                logger.warn("Hit unexpected issue converting value {} to Big Decimal", value, e);
            }
        }
        return 0L;
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
