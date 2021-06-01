package org.transitclock.reporting;

import org.transitclock.statistics.StatisticsV2;

import java.util.*;

public class DoubleStatistics {
    private PriorityQueue<Double> minHeap , maxHeap;
    private double sum;
    private double min = 1.0D / 0.0;
    private double max = -1.0D / 0.0;

    public DoubleStatistics() {
        minHeap = new PriorityQueue<>();
        maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
    }

    public void add(double num) {
        if (!minHeap.isEmpty() && num < minHeap.peek()) {
            maxHeap.offer(num);
            if (maxHeap.size() > minHeap.size() + 1) {
                minHeap.offer(maxHeap.poll());
            }
        } else {
            minHeap.offer(num);
            if (minHeap.size() > maxHeap.size() + 1) {
                maxHeap.offer(minHeap.poll());
            }
        }
        this.min = Math.min(this.min, num);
        this.max = Math.max(this.max, num);
        this.sum += num;
    }

    public final int getCount(){
        return minHeap.size() + maxHeap.size();
    }

    public final double getMin(){
        return this.min;
    }

    public final double getMax(){
        return this.max;
    }

    public final double getSum(){
        return this.sum;
    }

    public final Double getMedian() {
        double median;
        if(getCount() == 0){
            return null;
        }
        else if (minHeap.size() < maxHeap.size()) {
            median = maxHeap.peek();
        } else if (minHeap.size() > maxHeap.size()) {
            median = minHeap.peek();
        } else {
            median = (minHeap.peek() + maxHeap.peek()) / 2;
        }
        return median;
    }

    public final Double getAverage() {
        return this.getCount() > 0 ? this.getSum() / (double)this.getCount() : null;
    }

    public List<Double> getAllValues(){
        List<Double> allValues = new ArrayList<>(minHeap.size() + maxHeap.size());
        for(Double value : minHeap){
            allValues.add(value);
        }
        for(Double value : maxHeap){
            allValues.add(value);
        }
        return allValues;
    }

    public List<Double> getAllValuesFiltered(float scaleOfElimination){
        List<Double> allValues = new ArrayList<>(minHeap.size() + maxHeap.size());
        for(Double value : minHeap){
            allValues.add(value);
        }
        for(Double value : maxHeap){
            allValues.add(value);
        }
        return StatisticsV2.eliminateOutliers(allValues, scaleOfElimination);
    }

    public Double getPercentileValue(double percentile) {
        List<Double> values = getAllValues();
        Collections.sort(getAllValues());
        int index = (int) Math.ceil(percentile / 100.0 * values.size());
        return values.get(index-1);
    }

}
