package org.transitclock.reporting.service.runTime;

import org.transitclock.db.structs.RunTimesForStops;

public class RunTimeHelper {
    public static Double getRunTimeForStopPath(RunTimesForStops runTimesForStops) {
        Long runTimeForStopPath = runTimesForStops.getRunTime();
        if(runTimeForStopPath != null){
            return new Double(runTimeForStopPath);
        }
        return null;
    }

    public static Double getScheduledRunTimeForStopPath(RunTimesForStops runTimesForStops){
        Integer runTimeForStopPath = runTimesForStops.getScheduledRunTime();
        if(runTimeForStopPath != null){
            return new Double(runTimeForStopPath);
        }
        return null;
    }

    public static Double getDwellTimeForStopPath(RunTimesForStops runTimesForStops) {
        if (runTimesForStops.getDwellTime() != null) {
            return new Double(runTimesForStops.getDwellTime());
        }
        return null;
    }
}
