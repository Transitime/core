package org.transitclock.ipc.data;

import java.io.Serializable;
import java.util.DoubleSummaryStatistics;

public class IpcDoubleSummaryStatistics implements Serializable {
    private static final long serialVersionUID = 6366506370164076626L;

    private final long count;
    private final double sum;
    private final double min;
    private final double max;
    private final double average;

    public IpcDoubleSummaryStatistics(DoubleSummaryStatistics doubleSummaryStatistics){
        this.count = doubleSummaryStatistics.getCount();
        this.sum = doubleSummaryStatistics.getSum();
        this.min = doubleSummaryStatistics.getMin();
        this.max = doubleSummaryStatistics.getMax();
        this.average = doubleSummaryStatistics.getAverage();
    }

    public long getCount() {
        return count;
    }

    public double getSum() {
        return sum;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getAverage() {
        return average;
    }
}
