package org.transitclock.reporting.service.runTime.prescriptive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.ServiceType;
import org.transitclock.db.query.RunTimeForRouteQuery;
import org.transitclock.db.structs.RunTimesForRoutes;
import org.transitclock.ipc.util.GtfsDbDataUtil;
import org.transitclock.reporting.service.RunTimeService;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.PrescriptiveRuntimeClusteringService;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.model.PrescriptiveRuntimeResult;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.model.RunTimeData;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class PrescriptiveTimebandService {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptiveTimebandService.class);

    @Inject
    private RunTimeService runTimeService;

    @Inject
    private PrescriptiveRuntimeClusteringService clusteringService;


    public List<PrescriptiveRuntimeResult> generateTimebands(LocalDate beginDate,
                                                             LocalDate endDate,
                                                             LocalTime beginTime,
                                                             LocalTime endTime,
                                                             String routeIdOrShortName,
                                                             String headsign,
                                                             String directionId,
                                                             String tripPatternId,
                                                             ServiceType serviceType,
                                                             boolean readOnly) throws Exception {


        String routeShortName = GtfsDbDataUtil.getRouteShortName(routeIdOrShortName);

        RunTimeForRouteQuery.Builder rtBuilder = new RunTimeForRouteQuery.Builder();
        RunTimeForRouteQuery rtQuery = rtBuilder
                .beginDate(beginDate)
                .endDate(endDate)
                .beginTime(beginTime)
                .endTime(endTime)
                .serviceType(serviceType)
                .routeShortName(routeShortName)
                .headsign(headsign)
                .directionId(directionId)
                .tripPatternId(tripPatternId)
                .includeRunTimesForStops(true)
                .readOnly(readOnly)
                .build();

        List<RunTimesForRoutes> runTimesForRoutes = runTimeService.getRunTimesForRoutes(rtQuery);

        List<RunTimeData> runTimeData = getRunTimesForRoutesAsRunTimeData(runTimesForRoutes);

        return clusteringService.processRunTimeData(runTimeData);
    }

    private List<RunTimeData> getRunTimesForRoutesAsRunTimeData(List<RunTimesForRoutes> runTimesForRoutes) {
        List<RunTimeData> runTimeData;
        runTimeData = runTimesForRoutes.stream().map(rt -> new RunTimeData(rt)).collect(Collectors.toList());
        return runTimeData;
    }

}
