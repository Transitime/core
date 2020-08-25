package org.transitclock.db.reporting;

import java.util.DoubleSummaryStatistics;

public class StopPathStatistics {
    final String tripId;
    final String stopPathId;
    final int stopPathIndex;
    final boolean isLastStop;

    DoubleSummaryStatistics dwellTimeStats = new DoubleSummaryStatistics();
    DoubleSummaryStatistics runTimeStats = new DoubleSummaryStatistics();

    public StopPathStatistics(String tripId, String stopPathId, int stopPathIndex, boolean isLastStop) {
        this.tripId = tripId;
        this.stopPathId = stopPathId;
        this.stopPathIndex = stopPathIndex;
        this.isLastStop = isLastStop;
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
}
