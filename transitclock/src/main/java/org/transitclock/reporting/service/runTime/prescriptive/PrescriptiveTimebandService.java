package org.transitclock.reporting.service.runTime.prescriptive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.ServiceType;
import org.transitclock.core.ServiceTypeUtil;
import org.transitclock.db.query.RunTimeForRouteQuery;
import org.transitclock.db.query.TripQuery;
import org.transitclock.db.structs.*;
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


    /**
     * Get list of historical runtimes
     *
     * @param beginDate
     * @param endDate
     * @param routeShortName
     * @param serviceType
     * @param configRev
     * @param readOnly
     * @return
     * @throws Exception
     */
    public Map<String, TimebandsForTripPattern> generateTimebands(LocalDate beginDate,
                                                                 LocalDate endDate,
                                                                 String routeShortName,
                                                                 ServiceType serviceType,
                                                                 int configRev,
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

        List<RunTimeData> realtimeRunTimeData = getRunTimesForRoutesAsRunTimeData(runTimesForRoutes,
                routeShortName, serviceType, beginDate, endDate, configRev);

        List<PrescriptiveRuntimeResult> results = clusteringService.processRunTimeData(realtimeRunTimeData);

        Map<String, TimebandsForTripPattern> timebandsForTripPatternMap = getTimebandsForTripPattern(results);

        return timebandsForTripPatternMap;
    }

    /**
     * Converts List of @RunTimesForRoutes into List of @RunTimeData
     * Populates list of missing run times data from schedule runtimes
     *
     * @param runTimesForRoutes
     * @param routeShortName
     * @param serviceType
     * @param beginDate
     * @param endDate
     * @param configRev
     * @return
     */
    private List<RunTimeData> getRunTimesForRoutesAsRunTimeData(List<RunTimesForRoutes> runTimesForRoutes,
                                                                String routeShortName,
                                                                ServiceType serviceType,
                                                                LocalDate beginDate,
                                                                LocalDate endDate,
                                                                int configRev) {

        List<RunTimeData> existingRunTimeData = runTimesForRoutes.stream().map(rt -> new RunTimeData(rt)).collect(Collectors.toList());
        //List<RunTimeData> existingRunTimeData = new ArrayList<>();

        List<RunTimeData> missingRunTimeData = getMissingRunTimesForRoutesAsRunTimeData(runTimesForRoutes,
                routeShortName, serviceType, beginDate, endDate, configRev);

        existingRunTimeData.addAll(missingRunTimeData);

        return existingRunTimeData;
    }

    /**
     * Searches for trips that match our runtime criteria
     * Filters out trip ids that we already have realtime data for
     * Get list of trips that we don't have runtime data for
     * Convert those trips to runtime data
     *
     * @param runTimesForRoutes
     * @param routeShortName
     * @param serviceType
     * @param beginDate
     * @param endDate
     * @param configRev
     * @return
     */
    private List<RunTimeData> getMissingRunTimesForRoutesAsRunTimeData(List<RunTimesForRoutes> runTimesForRoutes,
                                                                       String routeShortName,
                                                                       ServiceType serviceType,
                                                                       LocalDate beginDate,
                                                                       LocalDate endDate,
                                                                       int configRev){

        List<RunTimeData> missingRunTimesForRoutesAsRunTimeData = new ArrayList<>();

        Set<String> existingRunTimeDataTripIds = runTimesForRoutes.stream()
                                                                         .map(rt -> rt.getTripId())
                                                                         .collect(Collectors.toSet());

        //Set<String> existingRunTimeDataTripIds = new HashSet<>();

        Set<Integer> configRevs = new HashSet<>();
        configRevs.add(configRev);
        TripQuery tripQuery = new TripQuery.Builder(routeShortName, configRevs).build();

        // Get Service Types for all Service Ids
        Map<String, Set<ServiceType>> serviceTypesByServiceId =
                ServiceTypeUtil.getServiceTypesByIdForCalendars(configRev, beginDate, endDate);

        // Get list of Trips for selected Service Period
        List<Trip> trips = Trip.getTripsFromDb(tripQuery);

        for(Trip trip : trips){
            // Skip trips that already have run time data
            if(existingRunTimeDataTripIds.contains(trip.getId())){
                continue;
            }
            Set<ServiceType> serviceTypesForServiceId = serviceTypesByServiceId.get(trip.getServiceId());
            if(serviceTypesForServiceId != null && serviceTypesForServiceId.contains(serviceType)){
                missingRunTimesForRoutesAsRunTimeData.add(new RunTimeData(trip, trip.getTripPatternId(), serviceType, beginDate));
            }
        }

        return missingRunTimesForRoutesAsRunTimeData;
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
