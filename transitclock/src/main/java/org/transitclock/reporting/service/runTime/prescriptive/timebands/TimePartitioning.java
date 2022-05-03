package org.transitclock.reporting.service.runTime.prescriptive.timebands;

import org.transitclock.reporting.service.runTime.prescriptive.timebands.kmeans.Centroid;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.model.RunTimeData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimePartitioning {

    public static Map<Centroid, List<RunTimeData>> run(Map<Centroid, List<RunTimeData>> clusters) {
        Map<Centroid, List<RunTimeData>> results = new HashMap<>();

        for (Centroid centroid : clusters.keySet()) {
            Centroid currentCentroid = centroid;
            List<RunTimeData> currentRunTimeDataList = new ArrayList<>();

            Integer prevSequence = clusters.get(centroid).get(0).getSequence();
            for (RunTimeData rtd : clusters.get(centroid)) {
                if (rtd.getSequence() - prevSequence > 1) {
                    //end previous cluster
                    results.put(currentCentroid, currentRunTimeDataList);

                    //current rtd needs to start a new cluster
                    currentCentroid = new Centroid(-centroid.getCentroidValue());//use negative to show it was not a k-means centroid
                    currentRunTimeDataList = new ArrayList<>();
                }
                currentRunTimeDataList.add(rtd);
                prevSequence = rtd.getSequence();
            }
            results.put(currentCentroid, currentRunTimeDataList);
        }
        return results;
    }
}
