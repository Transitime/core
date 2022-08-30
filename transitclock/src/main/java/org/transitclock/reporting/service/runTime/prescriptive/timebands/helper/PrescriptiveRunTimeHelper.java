package org.transitclock.reporting.service.runTime.prescriptive.timebands.helper;

import org.transitclock.config.DoubleConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.utils.Time;

import java.util.Collections;
import java.util.List;

public class PrescriptiveRunTimeHelper {

    private static final DoubleConfigValue prescriptiveDwellTime = new DoubleConfigValue(
                "transitclock.runTime.prescriptiveDwellTimePercentile", 65d,
         "The dwellTime percentile that prescriptive runtime algorithm uses when calculating schedule adjustments");

    private static Double getDwellTimePercentile(){
        return prescriptiveDwellTime.getValue();
    }

    public static Double getVariablePercentileValue(Double fixedTime, List<Double> runTimeValues, int currentIndex, boolean isLastStop){
        if(isLastStop){
            return getVariable(fixedTime, getPercentileValue(runTimeValues, 65));
        }
        switch(currentIndex){
            case 1:
                return getVariable(fixedTime, getPercentileValue(runTimeValues, 40));
            case 2:
                return getVariable(fixedTime, getPercentileValue(runTimeValues, 50));
            /*case 3:
                return getVariable(fixedTime, getPercentileValue(runTimeValues, 55));
            case 4:
                return getVariable(fixedTime, getPercentileValue(runTimeValues, 60));*/
            default:
                return getVariable(fixedTime, getPercentileValue(runTimeValues, 60));
        }
    }

    private static Double getVariable(Double fixedTime, Double runTime){
        return runTime - fixedTime;
    }

    public static Double getRemainderPercentileValue(Double fixedTime, List<Double> runTimeValues, int currentIndex, boolean isLastStop){
        if(isLastStop){
            return 0d;
        }
        switch(currentIndex){
            case 1:
                return getVariable(fixedTime, getPercentileValue(runTimeValues, 15));
            /*case 2:
                return getVariable(fixedTime, getPercentileValue(runTimeValues, 10));
            case 3:
                return getVariable(fixedTime, getPercentileValue(runTimeValues, 5));*/
            default:
                return 0d;
        }
    }

    public static Double getDwellPercentileValue(List<Double> dwellValues){
        return getPercentileValue(dwellValues, getDwellTimePercentile());
    }

    public static Double getPercentileValue(List<Double> values, double percentile) {
        Collections.sort(values);
        int index = (int) Math.ceil(percentile / 100.0 * values.size());
        return values.get(index-1);
    }
}
