package org.transitclock.db.reporting;

import org.transitclock.db.structs.StopRunTime;

import java.util.*;

public class TripPatternStatistics {
    private String tripPatternId;
    private int configRev;
    private Map<String, StopPathStatistics> stopPathStatistics = new HashMap<>();
    private final int expectedStopPathCount;

    public TripPatternStatistics(String tripPatternId, int configRev, int expectedStopPathCount) {
        this.tripPatternId = tripPatternId;
        this.configRev = configRev;
        this.expectedStopPathCount = expectedStopPathCount;
    }

    public Map<String, StopPathStatistics> getStopPathStatistics() {
        return stopPathStatistics;
    }

    public String getTripPatternId() {
        return tripPatternId;
    }

    public void setTripPatternId(String tripPatternId) {
        this.tripPatternId = tripPatternId;
    }

    public int getConfigRev() {
        return configRev;
    }

    public void setConfigRev(int configRev) {
        this.configRev = configRev;
    }

    public void addStopPath(StopRunTime stopRunTime){
        if(stopRunTime.getTripPatternId().equals(tripPatternId)){
            StopPathStatistics result = stopPathStatistics.get(stopRunTime.getStopPathId());
            if(result == null){
                result = new StopPathStatistics(stopRunTime.getTripPatternId(), stopRunTime.getStopPathId(),
                        stopRunTime.getStopPathIndex(), stopRunTime.isFinalStop());
                stopPathStatistics.put(stopRunTime.getStopPathId(), result);
            }
            result.getRunTimeStats().accept(stopRunTime.getRunTime());
            result.getDwellTimeStats().accept(stopRunTime.getDwellTime());
        }
    }

    public boolean hasAllStopPaths(){
        return stopPathStatistics.size() == expectedStopPathCount;
    }

    public Double getTripAverageRunTime(){
        double tripRunTime = 0;
        if(hasAllStopPaths()){
            for(Map.Entry<String, StopPathStatistics> entry : stopPathStatistics.entrySet()){
                StopPathStatistics sps = entry.getValue();
                Double avgRunTime = sps.getAverageRunTime();
                if(avgRunTime == null){
                    return null;
                }
                tripRunTime += avgRunTime;
            }
            return tripRunTime;
        }
        return null;
    }

    public Double getTripFixedRunTime(){
        double tripFixedRunTime = 0;
        if(hasAllStopPaths()){
            for(Map.Entry<String, StopPathStatistics> entry : stopPathStatistics.entrySet()){
                StopPathStatistics sps = entry.getValue();
                Double minRunTime = sps.getMinRunTime();
                if(minRunTime == null){
                    return null;
                }
                tripFixedRunTime += minRunTime;
            }
            return tripFixedRunTime;
        }
        return null;
    }

    public Double getAvgDwellTime(){
        double tripDwellTime = 0;
        if(hasAllStopPaths()){
            for(Map.Entry<String, StopPathStatistics> entry : stopPathStatistics.entrySet()){
                StopPathStatistics sps = entry.getValue();
                Double avgDwellTime = sps.getAverageDwellTime();
                if(avgDwellTime == null){
                    return null;
                }
                tripDwellTime += avgDwellTime;
            }
            return tripDwellTime;
        }
        return null;
    }
}
