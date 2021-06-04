package org.transitclock.reporting.service.runTime;

import org.transitclock.core.ServiceType;
import org.transitclock.db.query.RunTimeForRouteQuery;
import org.transitclock.db.structs.RunTimesForRoutes;
import org.transitclock.db.structs.Trip;
import org.transitclock.ipc.data.IpcRunTime;
import org.transitclock.ipc.data.IpcRunTimeForTrip;
import org.transitclock.ipc.data.IpcRunTimeForTripsAndDistribution;
import org.transitclock.reporting.TripStopPathStatistics;
import org.transitclock.reporting.service.RunTimeService;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.transitclock.ipc.util.GtfsDbDataUtil.getRouteShortName;

public class TripRunTimesService {
    @Inject
    RunTimeService runTimeService;

    /**
     * Get RunTime Information for Trip Stop Paths
     * @param beginDate
     * @param endDate
     * @param beginTime
     * @param endTime
     * @param serviceType
     * @param readOnly
     * @return
     */
    public IpcRunTimeForTripsAndDistribution getRunTimeForTripsByStopPath(LocalDate beginDate,
                                                                          LocalDate endDate,
                                                                          LocalTime beginTime,
                                                                          LocalTime endTime,
                                                                          String routeIdOrShortName,
                                                                          String headsign,
                                                                          String tripPatternId,
                                                                          String directionId,
                                                                          ServiceType serviceType,
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
                .headsign(headsign)
                .directionId(directionId)
                .tripPatternId(tripPatternId)
                .includeRunTimesForStops(true)
                .readOnly(readOnly)
                .build();

        List<RunTimesForRoutes> runTimesForRoutes = runTimeService.getRunTimesForRoutes(rtQuery);

        Map<String, TripStopPathStatistics> tripStatisticsByTripId = runTimeService.processTripStatsMap(runTimesForRoutes);

        List<IpcRunTimeForTrip> ipcRunTimeForTripList = getRunTimeStatsForTripsByStopPath(tripStatisticsByTripId);

        Map<String, List<Long>> runTimesByTripId = getRunTimesByTripId(runTimesForRoutes);

        IpcRunTime runTimeSummary = runTimeService.getRunTimeStats(tripStatisticsByTripId);

        IpcRunTimeForTripsAndDistribution ipcRunTimeForTripsAndDistribution = new IpcRunTimeForTripsAndDistribution(
                ipcRunTimeForTripList, runTimesByTripId, runTimeSummary);

        return ipcRunTimeForTripsAndDistribution;
    }

    private List<IpcRunTimeForTrip> getRunTimeStatsForTripsByStopPath(Map<String, TripStopPathStatistics> tripStatsByTripId){

        List<IpcRunTimeForTrip> ipcRunTimeForTrips = new ArrayList<>();

        // Loop through each TripStats grouped by Trip Id
        for(Map.Entry<String, TripStopPathStatistics> tripStatEntry : tripStatsByTripId.entrySet()){

            TripStopPathStatistics tripStatistics = tripStatEntry.getValue();

            //Validation -- Make sure trip is complete
            if(runTimeService.invalidTripStatistics(tripStatistics)){
                continue;
            }

            Double medianRunTime = tripStatistics.getTripMedianRunTime();
            Double fixedTime = tripStatistics.getTripFixedRunTime();
            Double dwellTime = tripStatistics.getTripMedianDwellTime();
            Double variableTime = null;

            // At minimum need median runTime and dwellTime to have complete runTime
            if(medianRunTime == null || dwellTime == null){
                continue;
            }

            if(medianRunTime != null && fixedTime != null) {
                variableTime = medianRunTime - fixedTime;
            }

            // Get Current Trip and Next Trip Info
            Trip currentTrip = tripStatistics.getTrip();
            Integer nextTripStartTime = tripStatistics.getNextTripTime();


            ipcRunTimeForTrips.add(new IpcRunTimeForTrip(currentTrip.getId(),
                    currentTrip.getStartTime(),
                    currentTrip.getEndTime(),
                    nextTripStartTime,
                    medianRunTime,
                    fixedTime,
                    variableTime,
                    dwellTime));
        }

        return ipcRunTimeForTrips;
    }

    private Map<String, List<Long>> getRunTimesByTripId(List<RunTimesForRoutes> runTimesForRoutes) {
        Map<String, List<Long>> runTimesByTripId = new HashMap<>();
        for(RunTimesForRoutes runTimesForRoute : runTimesForRoutes){
            if(runTimesForRoute.hasCompleteRunTime()){
                Long runTime = runTimesForRoute.getRunTime();
                if(runTime != null){
                    String tripId =  runTimesForRoute.getTripId();
                    if(runTime != null){
                        List<Long> runTimes = runTimesByTripId.get(tripId);
                        if(runTimes == null){
                            runTimes = new ArrayList<>();
                            runTimesByTripId.put(tripId, runTimes);
                        }
                        runTimes.add(runTime);
                    }
                }
            }
        }
        return runTimesByTripId;
    }
}
