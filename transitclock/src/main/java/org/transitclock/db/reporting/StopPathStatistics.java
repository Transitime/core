package org.transitclock.db.reporting;

import java.util.DoubleSummaryStatistics;

public class StopPathStatistics {
    private final String tripId;
    private final String stopPathId;
    private final int stopPathIndex;
    private final boolean isLastStop;
    private final boolean isFirstStop;

    DoubleSummaryStatistics dwellTimeStats = new DoubleSummaryStatistics();
    DoubleSummaryStatistics runTimeStats = new DoubleSummaryStatistics();

    public StopPathStatistics(String tripId, String stopPathId, int stopPathIndex, boolean isLastStop) {
        this.tripId = tripId;
        this.stopPathId = stopPathId;
        this.stopPathIndex = stopPathIndex;
        this.isLastStop = isLastStop;
        this.isFirstStop = stopPathIndex == 0;
    }

    public DoubleSummaryStatistics getDwellTimeStats() {
        return dwellTimeStats;
    }

    public DoubleSummaryStatistics getRunTimeStats() {
        return runTimeStats;
    }

    public Double getAverageRunTime() {
        return runTimeStats.getAverage();
    }

    public Double getMinRunTime() {
        return runTimeStats.getMin();
    }

    public Double getAverageDwellTime() {
        return dwellTimeStats.getAverage();
    }

    public boolean isLastStop() {
        return isLastStop;
    }

    public boolean isFirstStop() {
        return isFirstStop;
    }
}
