package org.transitclock.reporting;

import org.transitclock.db.structs.RunTimesForStops;

import java.util.ArrayList;
import java.util.List;

public class TimePointStatistics {
    private final String stopPathId;
    private final Integer stopPathIndex;
    private final String stopName;
    private final boolean isLastStop;
    private final boolean isFirstStop;

    DoubleStatistics dwellTimeStats = new DoubleStatistics();
    DoubleStatistics runTimeStats = new DoubleStatistics();
    List<Double> totalRunTime = new ArrayList<>();

    public TimePointStatistics(String stopPathId, int stopPathIndex, String stopName,boolean isLastStop) {
        this.stopPathId = stopPathId;
        this.stopPathIndex = stopPathIndex;
        this.stopName =  stopName;
        this.isLastStop = isLastStop;
        this.isFirstStop = stopPathIndex == 0;
    }

    public DoubleStatistics getDwellTimeStats() {
        return dwellTimeStats;
    }

    public DoubleStatistics getRunTimeStats() {
        return runTimeStats;
    }

    public Double getAverageRunTime() {
        return runTimeStats.getAverage();
    }

    public Double getMedianRunTime() {
        return runTimeStats.getMedian();
    }

    public Double getMinRunTime() {
        return runTimeStats.getMin();
    }

    public Double getAverageDwellTime() {
        return dwellTimeStats.getAverage();
    }

    public Double getMedianDwellTime() {
        if(isLastStop){
            return 0.0;
        }
        return dwellTimeStats.getMedian();
    }

    public List<Double> getAllRunTimes(){
        return runTimeStats.getAllValues();
    }

    public List<Double> getAllDwellTimes(){
        return dwellTimeStats.getAllValues();
    }

    public long getCount() { return runTimeStats.getCount(); }

    public boolean isLastStop() {
        return isLastStop;
    }

    public boolean isFirstStop(){
        return isFirstStop;
    }

    public String getStopName() {
        return stopName;
    }

    public String getStopPathId() {
        return stopPathId;
    }

    public Integer getStopPathIndex() {
        return stopPathIndex;
    }

    public Double getRunTimePercentile(double percentile){
        return runTimeStats.getPercentileValue(percentile);
    }

    public Double getDwellTimePercentile(double percentile){
        return dwellTimeStats.getPercentileValue(percentile);
    }

    public List<Double> getTotalRunTimes() {
        return totalRunTime;
    }

    @Override
    public String toString() {
        return "TimePointStatistics{" +
                "stopPathId='" + stopPathId + '\'' +
                ", stopPathIndex=" + stopPathIndex +
                ", stopName='" + stopName + '\'' +
                ", isLastStop=" + isLastStop +
                ", isFirstStop=" + isFirstStop +
                ", dwellTimeStats=" + dwellTimeStats +
                ", runTimeStats=" + runTimeStats +
                '}';
    }
}