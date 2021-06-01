package org.transitclock.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatisticsV2 {

    public static double getMean(final List<Double> input) {
        double sum = input.stream()
                .mapToDouble(value -> value)
                .sum();
        return (sum / input.size());
    }

    public static double getVariance(List<Double> input) {
        double mean = getMean(input);
        return getVariance(input, mean);
    }

    public static double getVariance(List<Double> input, double mean) {
        double temp = input.stream()
                .mapToDouble(a -> a)
                .map(a -> (a - mean) * (a - mean))
                .sum();
        return temp / (input.size() - 1);
    }

    public static double getStdDev(List<Double> input, double mean) {
        return Math.sqrt(getVariance(input));
    }

    public static double getStdDev(List<Double> input) {
        double mean = getMean(input);
        return getStdDev(input, mean);
    }

    public static List<Double> eliminateOutliers(List<Double> input, float scaleOfElimination) {
        final List<Double> newList = new ArrayList<>();

        double mean = getMean(input);
        double stdDev = getStdDev(input, mean);

        for (Double value : input) {
            boolean isLessThanLowerBound = value < mean - stdDev * scaleOfElimination;
            boolean isGreaterThanUpperBound = value > mean + stdDev * scaleOfElimination;
            boolean isOutOfBounds = isLessThanLowerBound || isGreaterThanUpperBound;

            if (!isOutOfBounds) {
                newList.add(value);
            }
        }

        int countOfOutliers = input.size() - newList.size();
        if (countOfOutliers == 0) {
            return input;
        }

        return eliminateOutliers(newList,scaleOfElimination);
    }

    public static double filteredMean(List<Double> input, float scaleOfElimination){
        return getMean(eliminateOutliers(input, scaleOfElimination));
    }

    public static void main(String args[]) {
        Double a[] = {4.0,4.0,5.0,10.0,9.0,7.0,9.0,1.0,4.0,7.0,5.0};
        Double b[] = {7.0,4.0,1.0,2.0,3.0,8.0,1.0,3.0,10.0,5.0,4.0};
        Double c[] = {3.0,4.0,9.0,9.0,7.0,3.0,8.0,8.0,3.0,7.0,9.0};
        Double d[] = {1.0,10.0,7.0,6.0,7.0,6.0,2.0,6.0,5.0,9.0,1.0};
        Double e[] = {3.0,7.0,13.0,1.0,5.0,1.0,5.0,8.0,7.0,10.0,7.0};
        Double f[] = {9.0,1.0,4.0,7.0,6.0,20.0,2.0,9.0,8.0,6.0,9.0};
        Double g[] = {5.0,5.0,1.0,1.0,2.0,4.0,1.0,3.0,7.0,4.0,1.0};
        Double h[] = {6.0,10.0,3.0,3.0,4.0,9.0,3.0,3.0,6.0,1.0,1.0};
        Double i[] = {2.0,3.0,4.0,6.0,9.0,2.0,6.0,6.0,8.0,2.0,1.0};
        Double j[] = {40.0,48.0,47.0,45.0,52.0,60.0,37.0,47.0,58.0,51.0,38.0};


        double mean_a = StatisticsV2.filteredMean(Arrays.asList(a), 2.0f);
        double mean_b = StatisticsV2.filteredMean(Arrays.asList(b), 2.0f);
        double mean_c = StatisticsV2.filteredMean(Arrays.asList(c), 2.0f);
        double mean_d = StatisticsV2.filteredMean(Arrays.asList(d), 2.0f);
        double mean_e = StatisticsV2.filteredMean(Arrays.asList(e), 2.0f);
        double mean_f = StatisticsV2.filteredMean(Arrays.asList(f), 2.0f);
        double mean_g = StatisticsV2.filteredMean(Arrays.asList(g), 2.0f);
        double mean_h = StatisticsV2.filteredMean(Arrays.asList(h), 2.0f);
        double mean_i = StatisticsV2.filteredMean(Arrays.asList(i), 2.0f);
        double mean_j = StatisticsV2.filteredMean(Arrays.asList(j), 2.0f);


        double total =mean_a + mean_b + mean_c + mean_d + mean_e + mean_f + mean_g + mean_h + mean_i;


        System.out.println("filtered mean = " + total);

    }

}
