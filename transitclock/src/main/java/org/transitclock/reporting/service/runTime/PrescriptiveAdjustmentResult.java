package org.transitclock.reporting.service.runTime;

import org.transitclock.ipc.data.IpcPrescriptiveRunTime;

import java.util.HashMap;
import java.util.Map;

public class PrescriptiveAdjustmentResult {
    Map<Integer, Double> scheduleAdjustmentMappedByStopPathIndex = new HashMap<>();
    int lastTimePointIndex = 0;
    double timePointScheduleDelta = 0;
    double tripScheduleDelta = 0;


    /**
     * Get scheduleAdjustments for all stopPaths
     * Adjustment is a fraction of the original schedule time
     * In theory the adjustment should be the same for all trips, but just in case
     * Adjusting by fraction is safter to avoid negative times etc.
     * @return
     */
    public Map<Integer, Double> getScheduleAdjustmentByStopPathIndex() {
        return scheduleAdjustmentMappedByStopPathIndex;
    }

    public void setScheduleAdjustmentMappedByStopPathIndex(Map<Integer, Double> scheduleAdjustmentMappedByStopPathIndex) {
        this.scheduleAdjustmentMappedByStopPathIndex = scheduleAdjustmentMappedByStopPathIndex;
    }

    public int getLastTimePointIndex() {
        return lastTimePointIndex;
    }

    public void setLastTimePointIndex(int lastTimePointIndex) {
        this.lastTimePointIndex = lastTimePointIndex;
    }

    public double getTimePointScheduleDelta() {
        return timePointScheduleDelta;
    }

    public void setTimePointScheduleDelta(double timePointScheduleDelta) {
        this.timePointScheduleDelta = timePointScheduleDelta;
    }

    public void addScheduleAdjustmentsToStops(Integer currentStopPathIndex, IpcPrescriptiveRunTime prescriptiveRunTime){
        double newTimepointRunTime = prescriptiveRunTime.getScheduled() + prescriptiveRunTime.getAdjustment();
        double fractionChange = newTimepointRunTime / prescriptiveRunTime.getScheduled();

        if(currentStopPathIndex > 0){
            for(int i = currentStopPathIndex; i > lastTimePointIndex; i--){
                scheduleAdjustmentMappedByStopPathIndex.put(i, fractionChange);
            }
        }else{
            scheduleAdjustmentMappedByStopPathIndex.put(currentStopPathIndex, 1d);
        }
        lastTimePointIndex = currentStopPathIndex;
    }
}
