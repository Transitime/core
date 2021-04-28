package org.transitclock.reporting;

import java.util.DoubleSummaryStatistics;

public class StopPathStatisticsV2 {
    private final String tripId;
    private final String stopPathId;
    private final int stopPathIndex;
    private final boolean isLastStop;
    private final boolean isFirstStop;

    DoubleMedianStatistics dwellTimeStats = new DoubleMedianStatistics();
    DoubleMedianStatistics runTimeStats = new DoubleMedianStatistics();

    public StopPathStatisticsV2(String tripId, String stopPathId, int stopPathIndex, boolean isLastStop) {
        this.tripId = tripId;
        this.stopPathId = stopPathId;
        this.stopPathIndex = stopPathIndex;
        this.isLastStop = isLastStop;
        this.isFirstStop = stopPathIndex == 0;
    }

    public DoubleMedianStatistics getDwellTimeStats() {
        return dwellTimeStats;
    }

    public DoubleMedianStatistics getRunTimeStats() {
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
        return dwellTimeStats.getMedian();
    }

    public long getCount() { return runTimeStats.getCount(); }

    public boolean isLastStop() {
        return isLastStop;
    }

    public boolean isFirstStop(){
        return isFirstStop;
    }
}