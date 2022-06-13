package org.transitclock.reporting.service.runTime.prescriptive.timebands.kmeans;

import org.transitclock.reporting.service.runTime.prescriptive.timebands.model.RunTimeData;

import java.util.*;
import java.util.stream.Collectors;

public class KMeans {
    private static final Random random = new Random();

    public static Map<Centroid, List<RunTimeData>> fit(
            List<RunTimeData> data,
            int k,
            Distance distance,
            int maxIterations) {

        List<Centroid> centroids = initializeCentroids(data, k);
        Map<Centroid, List<RunTimeData>> clusters = new HashMap<>();
        Map<Centroid, List<RunTimeData>> lastState = new HashMap<>();

        for (int i = 0; i < maxIterations; i++) {
            boolean isLastIteration = i == maxIterations - 1;

            for (RunTimeData rtd : data) {
                Centroid centroid = nearestCentroid(rtd, centroids, distance);
                assignToCluster(clusters, rtd, centroid);
            }

            boolean shouldTerminate = isLastIteration || clusters.equals(lastState);
            lastState = clusters;
            if (shouldTerminate) {
                break;
            }

            centroids = relocateCentroids(clusters);
            clusters = new HashMap<>();
        }
        return lastState;
    }

    protected static List<Centroid> initializeCentroids(List<RunTimeData> data, int k) {
        List<Centroid> centroids = new ArrayList<>();

        Double max = data.stream().map(x -> x.getActualRuntime()).max(Double::compare).get();
        Double min = data.stream().map(x -> x.getActualRuntime()).min(Double::compare).get();

        //randomly assign locations
//        for (int i = 0; i < k; i++) {
//            Double candidate = (random.nextDouble() * (max - min)) + min;
//            centroids.add(new Centroid(candidate));
//        }
        //end

        //place equidistant in range between min and max
        Double candidate = min;
        centroids.add(new Centroid(candidate));//put one at min

        Double spacing = (max - min) / (k - 1);
        for (int i = 1; i < k; i++) {
            candidate = min + (i * spacing);
            centroids.add(new Centroid(candidate));//put at equal spacing apart in range
        }
        //end

        return centroids;
    }

    protected static Centroid nearestCentroid(RunTimeData rtd, List<Centroid> centroids, Distance distance) {
        Double minimumDistance = Double.MAX_VALUE;
        Centroid nearest = null;

        for (Centroid centroid : centroids) {
            Double currentDistance = distance.calculate(rtd.getActualRuntime(), centroid.getCentroidValue());
            if (currentDistance < minimumDistance) {
                //found a cluster closer so use that instead
                minimumDistance = currentDistance;
                nearest = centroid;
            }
        }
        return nearest;
    }

    protected static void assignToCluster(
            Map<Centroid, List<RunTimeData>> clusters,
            RunTimeData rtd,
            Centroid centroid) {
        clusters.compute(centroid, (key, list) -> {
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(rtd);
            return list;
        });
    }

    protected static Centroid average(Centroid centroid, List<RunTimeData> runTimeData) {
        if (runTimeData == null || runTimeData.isEmpty()) {
            return centroid;
        }

        Double average = runTimeData.stream().collect(Collectors.averagingDouble(RunTimeData::getActualRuntime));

        return new Centroid(average);
    }

    protected static List<Centroid> relocateCentroids(Map<Centroid, List<RunTimeData>> clusters) {
        return clusters.entrySet().stream().map(e -> average(e.getKey(), e.getValue())).collect(Collectors.toList());
    }
}
