package org.transitclock.reporting.service.runTime;

import org.transitclock.core.ServiceType;
import org.transitclock.db.query.RunTimeForRouteQuery;
import org.transitclock.db.structs.RunTimesForRoutes;
import org.transitclock.ipc.data.IpcRunTimeForStopPath;
import org.transitclock.reporting.StopPathStatistics;
import org.transitclock.reporting.TripStopPathStatistics;
import org.transitclock.reporting.keys.StopPathRunTimeKey;
import org.transitclock.reporting.service.RunTimeService;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.transitclock.ipc.util.GtfsDbDataUtil.getRouteShortName;

public class StopRunTimesService {
    @Inject
    RunTimeService runTimeService;


    /**
     * Get RunTime Information for Trip Stop Paths
     * @param beginDate
     * @param endDate
     * @param beginTime
     * @param endTime
     * @param routeIdOrShortName
     * @param tripId
     * @param serviceType
     * @param agencyId
     * @param readOnly
     * @return
     */
    public List<IpcRunTimeForStopPath> getRunTimeForStopPaths(LocalDate beginDate,
                                                          LocalDate endDate,
                                                          LocalTime beginTime,
                                                          LocalTime endTime,
                                                          String routeIdOrShortName,
                                                          String tripId,
                                                          ServiceType serviceType,
                                                          boolean timePointsOnly,
                                                          String agencyId,
                                                          boolean readOnly) throws Exception {

        String routeShortName = getRouteShortName(routeIdOrShortName);

        RunTimeForRouteQuery.Builder rtBuilder = new RunTimeForRouteQuery.Builder();
        RunTimeForRouteQuery rtQuery = rtBuilder
                .beginDate(beginDate)
                .endDate(endDate)
                .beginTime(beginTime)
                .endTime(endTime)
                .serviceType(serviceType)
                .routeShortName(routeShortName)
                .tripId(tripId)
                .includeRunTimesForStops(true)
                .readOnly(readOnly)
                .build();

        List<RunTimesForRoutes> runTimesForRoutes = runTimeService.getRunTimesForRoutes(rtQuery);

        Map<String, TripStopPathStatistics> tripStatisticsByTripId = runTimeService.processTripStatsMap(runTimesForRoutes);

        List<IpcRunTimeForStopPath> runTimeForStopPaths;
        if(timePointsOnly){
            runTimeForStopPaths = getRunTimeStatsForTimePoints(tripStatisticsByTripId);
        } else {
            runTimeForStopPaths = getRunTimeStatsForStopPaths(tripStatisticsByTripId);
        }

        return runTimeForStopPaths;
    }

    private List<IpcRunTimeForStopPath> getRunTimeStatsForStopPaths(Map<String, TripStopPathStatistics> tripStatsByTripId){

        List<IpcRunTimeForStopPath> ipcRunTimeForStopPaths = new ArrayList<>();

        // Loop through each TripStats grouped by Trip Id
        for(Map.Entry<String, TripStopPathStatistics> tripStatEntry : tripStatsByTripId.entrySet()){

            TripStopPathStatistics tripStatistics = tripStatEntry.getValue();

            //Validation -- Make sure trip is complete
            if(runTimeService.invalidTripStatistics(tripStatistics)){
                continue;
            }

            Map<StopPathRunTimeKey, StopPathStatistics> stopPathStatsMap = tripStatistics.getAllStopPathStatistics();

            for(Map.Entry<StopPathRunTimeKey, StopPathStatistics> stopPathEntry : stopPathStatsMap.entrySet()) {

                StopPathRunTimeKey stopPathRunTimeKey = stopPathEntry.getKey();
                StopPathStatistics stopPathStatistics = stopPathEntry.getValue();

                Double medianRunTime = stopPathStatistics.getMedianRunTime();
                Double fixedTime = stopPathStatistics.getMinRunTime();
                Double dwellTime = stopPathStatistics.getMedianDwellTime();
                Double variableTime = null;

                // At minimum need median runTime and dwellTime to have complete runTime
                if (medianRunTime == null || dwellTime == null) {
                    continue;
                }

                if (medianRunTime != null && fixedTime != null) {
                    variableTime = medianRunTime - fixedTime;
                }

                ipcRunTimeForStopPaths.add(
                        new IpcRunTimeForStopPath(
                                stopPathRunTimeKey.getStopPathId(),
                                stopPathStatistics.getStopName(),
                                stopPathRunTimeKey.getStopPathIndex(),
                                null,
                                null,
                                stopPathStatistics.isTimePoint(),
                                medianRunTime,
                                fixedTime,
                                variableTime,
                                dwellTime
                        )
                );
            }
        }

        return ipcRunTimeForStopPaths;
    }

    private List<IpcRunTimeForStopPath> getRunTimeStatsForTimePoints(Map<String, TripStopPathStatistics> tripStatsByTripId){

        List<IpcRunTimeForStopPath> ipcRunTimeForStopPaths = new ArrayList<>();

        // Loop through each TripStats grouped by Trip Id
        for(Map.Entry<String, TripStopPathStatistics> tripStatEntry : tripStatsByTripId.entrySet()){

            TripStopPathStatistics tripStatistics = tripStatEntry.getValue();

            //Validation -- Make sure trip is complete
            if(runTimeService.invalidTripStatistics(tripStatistics)){
                continue;
            }

            Map<StopPathRunTimeKey, StopPathStatistics> stopPathStatsMap = tripStatistics.getAllStopPathStatistics();
            Map<StopPathRunTimeKey, StopPathStatistics> sortedStopPathsStatsMap = stopPathStatsMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));

            Double currentTimePointRunTime = 0d;
            Double currentTimePointFixedTime = 0d;
            Double currentTimePointDwellTime = 0d;
            Double currentTimePointVariableTime = 0d;

            for(Map.Entry<StopPathRunTimeKey, StopPathStatistics> stopPathEntry : sortedStopPathsStatsMap.entrySet()) {

                StopPathRunTimeKey stopPathRunTimeKey = stopPathEntry.getKey();
                StopPathStatistics stopPathStatistics = stopPathEntry.getValue();

                Double medianRunTime = stopPathStatistics.getMedianRunTime();
                Double fixedTime = stopPathStatistics.getMinRunTime();
                Double dwellTime = stopPathStatistics.getMedianDwellTime();

                // At minimum need median runTime and dwellTime to have complete runTime
                if (medianRunTime == null || dwellTime == null) {
                    continue;
                }

                currentTimePointRunTime += medianRunTime;
                currentTimePointFixedTime += fixedTime;
                currentTimePointDwellTime += dwellTime;
                currentTimePointVariableTime += medianRunTime - fixedTime;

                if(stopPathStatistics.isTimePoint()) {
                    ipcRunTimeForStopPaths.add(
                            new IpcRunTimeForStopPath(
                                    stopPathRunTimeKey.getStopPathId(),
                                    stopPathStatistics.getStopName(),
                                    stopPathRunTimeKey.getStopPathIndex(),
                                    null,
                                    null,
                                    stopPathStatistics.isTimePoint(),
                                    currentTimePointRunTime,
                                    currentTimePointFixedTime,
                                    currentTimePointVariableTime,
                                    currentTimePointDwellTime
                            )
                    );

                    currentTimePointRunTime = 0d;
                    currentTimePointFixedTime = 0d;
                    currentTimePointDwellTime = 0d;
                    currentTimePointVariableTime = 0d;
                }
            }
        }

        return ipcRunTimeForStopPaths;
    }
}
