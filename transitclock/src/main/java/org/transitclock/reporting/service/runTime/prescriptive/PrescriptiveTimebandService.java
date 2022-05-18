package org.transitclock.reporting.service.runTime.prescriptive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.ServiceType;
import org.transitclock.db.query.RunTimeForRouteQuery;
import org.transitclock.db.structs.RunTimesForRoutes;
import org.transitclock.ipc.util.GtfsDbDataUtil;
import org.transitclock.reporting.service.RunTimeService;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.PrescriptiveRuntimeClusteringService;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.kmeans.Centroid;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.model.PrescriptiveRuntimeResult;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.model.RunTimeData;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.model.TimebandTime;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.model.TimebandsForTripPattern;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PrescriptiveTimebandService {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptiveTimebandService.class);

    @Inject
    private RunTimeService runTimeService;

    @Inject
    private PrescriptiveRuntimeClusteringService clusteringService;



    public Map<String, TimebandsForTripPattern> generateTimebands(LocalDate beginDate,
                                                             LocalDate endDate,
                                                             String routeShortName,
                                                             ServiceType serviceType,
                                                             boolean readOnly) throws Exception {



        RunTimeForRouteQuery.Builder rtBuilder = new RunTimeForRouteQuery.Builder();
        RunTimeForRouteQuery rtQuery = rtBuilder
                .beginDate(beginDate)
                .endDate(endDate)
                .serviceType(serviceType)
                .routeShortName(routeShortName)
                .readOnly(readOnly)
                .build();

        List<RunTimesForRoutes> runTimesForRoutes = runTimeService.getRunTimesForRoutes(rtQuery);

        List<RunTimeData> runTimeData = getRunTimesForRoutesAsRunTimeData(runTimesForRoutes);

        List<PrescriptiveRuntimeResult> results = clusteringService.processRunTimeData(runTimeData);

        return getTimebandsForTripPattern(results);
    }

    private List<RunTimeData> getRunTimesForRoutesAsRunTimeData(List<RunTimesForRoutes> runTimesForRoutes) {
        List<RunTimeData> runTimeData;
        runTimeData = runTimesForRoutes.stream().map(rt -> new RunTimeData(rt)).collect(Collectors.toList());
        return runTimeData;
    }

    private Map<String, TimebandsForTripPattern> getTimebandsForTripPattern(List<PrescriptiveRuntimeResult> results){
        Map<String, TimebandsForTripPattern> timebandsForTripPatternMap = new HashMap<>();
        try{
            for (PrescriptiveRuntimeResult result : results) {
                List<TimebandTime> timebandTimes = new ArrayList<>();
                String routeShortName = result.getFirstRunTime().getRouteShortName();
                String tripPatternId = result.getFirstRunTime().getTripPatternId();
                for (Map.Entry<Centroid, List<RunTimeData>> centroidResult : result.getRunTimeDataPerCentroid().entrySet()) {
                    Centroid centroid = centroidResult.getKey();
                    List<RunTimeData> centroidRunTimeData = centroidResult.getValue();
                    timebandTimes.add(new TimebandTime(centroidRunTimeData));
                }
                TimebandsForTripPattern timebandsForTripPattern = new TimebandsForTripPattern(routeShortName, tripPatternId, timebandTimes);
                timebandsForTripPatternMap.put(tripPatternId, timebandsForTripPattern);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return timebandsForTripPatternMap;

    }

}
