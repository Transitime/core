package org.transitclock.reporting.service.runTime.prescriptive.timebands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.DoubleConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.helper.RunTimeDataHelper;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.kmeans.BasicDistance;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.kmeans.Centroid;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.kmeans.EmpiricalCombining;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.kmeans.KMeans;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.model.PrescriptiveRuntimeResult;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.model.RunTimeData;

import java.util.*;
import java.util.stream.Collectors;

public class PrescriptiveRuntimeClusteringService {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptiveRuntimeClusteringService.class);

    private static final IntegerConfigValue prescriptiveTimebandClusters = new IntegerConfigValue(
            "transitclock.runTime.prescriptiveTimebandClusters",
            6,
            "The starting number of clusters to use for k-means processing of prescriptive timeband clusters");

    public Integer getNumberOfClusters(){
        return prescriptiveTimebandClusters.getValue();
    }

    private static final IntegerConfigValue prescriptiveTimebandMaxIterations = new IntegerConfigValue(
            "transitclock.runTime.prescriptiveTimebandMaxIterations",
            20,
            "The max number of iterations for k-means processing of prescriptive timeband clusters");

    public Integer getMaxIterations(){
        return prescriptiveTimebandMaxIterations.getValue();
    }

    private static final DoubleConfigValue prescriptiveTimebandMinPartitionLength = new DoubleConfigValue(
            "transitclock.runTime.prescriptiveTimebandMinPartitionLengthSec",
            30 * 60.0, // 30 min
            "The minimum partition length for prescriptive timeband clusters");

    public Double getMinPartitionLength(){
        return prescriptiveTimebandMinPartitionLength.getValue();
    }

    private static final DoubleConfigValue prescriptiveTimebandMaxPartitionLength = new DoubleConfigValue(
            "transitclock.runTime.prescriptiveTimebandMaxPartitionLengthSec",
            480 * 60.0, // 8 hours
            "The maximum partition length for prescriptive timeband clusters");

    public Double getMaxPartitionLength(){
        return prescriptiveTimebandMaxPartitionLength.getValue();
    }

    public List<PrescriptiveRuntimeResult> processRunTimeData(List<RunTimeData> runTimeData) {

        //reduce data set to single entry for each trip id
        runTimeData = RunTimeDataHelper.reduce(runTimeData);

        //map run time data by trip pattern id
        Map<String, List<RunTimeData>> tripPatternMap = RunTimeDataHelper.mapRunTimeDataByTripPattern(runTimeData);

        Set<PrescriptiveRuntimeResult> results = new LinkedHashSet<>();

        for (String tripPatternId : tripPatternMap.keySet()) {
            //for each trip pattern, sequence runtimes
            RunTimeDataHelper.assignSequence(tripPatternMap.get(tripPatternId));

            //list of runtimes for trip pattern
            List<RunTimeData> runTimeDataForTripPattern = tripPatternMap.get(tripPatternId);

            //for each trip pattern, run kmeans
            Map<Centroid, List<RunTimeData>> kmeansResult =
                    KMeans.fit(
                        runTimeDataForTripPattern,
                        getNumberOfClusters(),
                        new BasicDistance(),
                        getMaxIterations()
                    );


            //partition kmeans centroids
            Map<Centroid, List<RunTimeData>> partitionedResults = TimePartitioning.run(kmeansResult);

            //combine partitioned results
            Map<Centroid, List<RunTimeData>> combinedResults =
                    EmpiricalCombining.run(partitionedResults, getMinPartitionLength(), getMaxPartitionLength());

            RunTimeData firstRunTime = runTimeDataForTripPattern.stream().findAny().orElse(null);

            results.add(new PrescriptiveRuntimeResult(firstRunTime, combinedResults));
        }

        List<PrescriptiveRuntimeResult> resultList = results.stream().collect(Collectors.toList());

        Collections.sort(resultList, new PrescriptiveRuntimeResultComparator());

        return resultList;
    }
}
