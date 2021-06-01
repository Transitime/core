package org.transitclock.reporting.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.ServiceType;
import org.transitclock.core.TemporalDifference;
import org.transitclock.db.query.ArrivalDepartureQuery;
import org.transitclock.db.structs.*;
import org.transitclock.ipc.data.IpcDoubleSummaryStatistics;
import org.transitclock.ipc.data.IpcStopPathWithSpeed;
import org.transitclock.ipc.data.IpcStopWithDwellTime;
import org.transitclock.reporting.keys.ArrivalDepartureTripKey;
import org.transitclock.reporting.keys.TripDateKey;
import org.transitclock.utils.Geo;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.Time;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

import static org.transitclock.configData.ReportingConfig.*;
import static org.transitclock.ipc.util.GtfsDbDataUtil.*;
import static org.transitclock.ipc.util.GtfsTimeUtil.dayOfYearForTrip;

public class SpeedMapService {

    private static final Logger logger =
            LoggerFactory.getLogger(SpeedMapService.class);


    /**
     *
     * Method to get a list of stops paths and their avg speed for a specified period of time
     *
     * @param beginDate
     * @param endDate
     * @param beginTime
     * @param endTime
     * @param routeIdOrShortName
     * @param serviceType
     * @param headsign
     * @param agencyId
     * @param readOnly
     * @return
     * @throws Exception
     */
    public List<IpcStopPathWithSpeed> getStopPathsWithSpeed(LocalDate beginDate, LocalDate endDate,
                                                            LocalTime beginTime, LocalTime endTime,
                                                            String routeIdOrShortName, ServiceType serviceType,
                                                            String headsign, String agencyId, boolean readOnly) throws Exception{

        String routeShortName = getRouteShortName(routeIdOrShortName);

        ArrivalDepartureQuery.Builder adBuilder = new ArrivalDepartureQuery.Builder();
        ArrivalDepartureQuery adQuery = adBuilder
                .beginDate(beginDate)
                .endDate(endDate)
                .beginTime(beginTime)
                .endTime(endTime)
                .routeShortName(routeShortName)
                .headsign(headsign)
                .serviceType(serviceType)
                .includeStopPath(true)
                .readOnly(readOnly)
                .build();

        IntervalTimer timer = new IntervalTimer();
        List<ArrivalDeparture> arrivalDeparturesList = ArrivalDeparture.getArrivalsDeparturesFromDb(adQuery);
        System.out.println(timer.elapsedMsecStr());

        Map<ArrivalDepartureTripKey, List<ArrivalDeparture>> resultsMap = new HashMap<>();
        Map<String, StopPath> stopPathsMap = new HashMap<>();

        // Put Arrival Departure and StopPaths data into maps
        processSpeedDataIntoMaps(arrivalDeparturesList, resultsMap, stopPathsMap, agencyId);

        // Get Average Speed Data for StopPaths
        Map<String, DoubleSummaryStatistics> stopPathsSpeedsMap = getStopPathsSpeedMap(resultsMap.values());

        List<IpcStopPathWithSpeed> ipcStopPathsWithSpeed = new ArrayList<>();
        for(Map.Entry<String, StopPath> stopPath : stopPathsMap.entrySet()){
            Double speed = stopPathsSpeedsMap.containsKey(stopPath.getKey()) ?
                    stopPathsSpeedsMap.get(stopPath.getKey()).getAverage() : null;
            ipcStopPathsWithSpeed.add(new IpcStopPathWithSpeed(stopPath.getValue(), speed));
        }

        return ipcStopPathsWithSpeed;
    }

    private void processSpeedDataIntoMaps(List<ArrivalDeparture> arrivalDeparturesList,
                                          Map<ArrivalDepartureTripKey,
                                                  List<ArrivalDeparture>> resultsMap,
                                          Map<String, StopPath> stopPathsMap,
                                          String agencyId){

        TimeZone tz = Agency.getTimeZoneFromDb(agencyId);

        for (ArrivalDeparture arrDep : arrivalDeparturesList) {

            addArrivalDepartureToMap(resultsMap, arrDep, tz.toZoneId());

            StopPath stopPath = arrDep.getStopPathFromDb();
            if(stopPath != null){
                if(!stopPathsMap.containsKey(stopPath.getId()) ||
                        stopPathsMap.get(stopPath.getId()).getConfigRev() < stopPath.getConfigRev()){
                    stopPathsMap.put(stopPath.getId(),stopPath);
                }
            }
        }
    }

    private void addArrivalDepartureToMap(
            Map<ArrivalDepartureTripKey, List<ArrivalDeparture>> map,
            ArrivalDeparture arrDep,
            ZoneId zoneId) {
        ArrivalDepartureTripKey key = new ArrivalDepartureTripKey(arrDep.getServiceId(),
                dayOfYearForTrip(arrDep.getDate(), zoneId), arrDep.getTripId(), arrDep.getVehicleId());
        List<ArrivalDeparture> list = map.get(key);
        if (list == null) {
            list = new ArrayList<>();
            map.put(key, list);
        }
        list.add(arrDep);
    }


    private Map<String, DoubleSummaryStatistics> getStopPathsSpeedMap(Collection<List<ArrivalDeparture>> arrivalDepartures){
        Map<String, DoubleSummaryStatistics> stopPathsSpeedsMap = new HashMap<>();
        for (List<ArrivalDeparture> arrDepList : arrivalDepartures) {

            for (int i=0; i<arrDepList.size()-1; ++i) {
                ArrivalDeparture arrDep1 = arrDepList.get(i);
                // Handle first stop in trip specially
                if (arrDep1.getStopPathIndex() == 0) {
                    // Don't need to deal with first arrival stop for trip since
                    // don't care when vehicle arrives at layover
                    if (arrDep1.isArrival())
                        continue;
                }
                // Deal with normal travel times
                ArrivalDeparture arrDep2 = arrDepList.get(i+1);
                Double speed = getSpeedDataBetweenTwoArrivalDepartures(arrDep1, arrDep2);
                if(speed != null) {
                    DoubleSummaryStatistics summaryStatistics = stopPathsSpeedsMap.get(arrDep2.getStopPathId());
                    if(summaryStatistics == null){
                        summaryStatistics = new DoubleSummaryStatistics();
                        stopPathsSpeedsMap.put(arrDep2.getStopPathId(), summaryStatistics);
                    }
                    summaryStatistics.accept(speed);
                }
            }
        }
        return stopPathsSpeedsMap;
    }

    private Double getSpeedDataBetweenTwoArrivalDepartures(ArrivalDeparture arrDep1,
                                                           ArrivalDeparture arrDep2) {

        Double travelSpeedForStopPath = null;

        // If schedule adherence is really far off then ignore the data
        // point because it would skew the results.
        TemporalDifference schedAdh = arrDep1.getScheduleAdherence();
        if (schedAdh == null)
            schedAdh = arrDep2.getScheduleAdherence();
        if (schedAdh != null
                && !schedAdh.isWithinBounds(getMaxSchedAdh(),
                getMaxSchedAdh())) {
            // Schedule adherence is off so don't use this data
            return null;
        }

        // If looking at arrival and departure for same stop then continue
        if (arrDep1.getStopPathIndex() == arrDep2.getStopPathIndex()
                && arrDep1.isArrival()
                && arrDep2.isDeparture()) {
            return null;
        }

        // If looking at departure from one stop to the arrival time at the
        // very next stop then can determine the travel times between the stops.

        /*if (arrDep1.getStopPathIndex() - arrDep2.getStopPathIndex() != 1
                && arrDep1.isDeparture()
                && arrDep2.isArrival()) {*/
        if(arrDep1.getStopPathIndex() - arrDep2.getStopPathIndex() == -1){

            // Determine the travel times and add them to the map
            travelSpeedForStopPath = determineTravelSpeedForStopPath(arrDep1,arrDep2);

            // Ignore a stop path if any segment travel time is negative. Nulls will
            // be ignored downstream anyway so can also ignore those.
            if (travelSpeedForStopPath == null)
                return null;
        }
        return travelSpeedForStopPath;
    }

    private Double determineTravelSpeedForStopPath(ArrivalDeparture arrDep1,
                                                   ArrivalDeparture arrDep2) {
        // Determine departure time. If shouldn't use departures times
        // for terminal departure that are earlier then schedule time
        // then use the scheduled departure time. This prevents creating
        // bad predictions due to incorrectly determined travel times.
        long departureTime = arrDep1.getTime();
        if (arrDep1.getStopPathIndex() == 0
                && arrDep1.getTime() < arrDep1.getScheduledTime()) {
            departureTime = arrDep1.getScheduledTime();
        }

        // Determine and return the travel time between the stops
        try {
            double travelTimeBetweenStopsMsec = (arrDep2.getTime() - departureTime) / Time.MS_PER_SEC;
            double speedMps = (arrDep2.getStopPathLength() / travelTimeBetweenStopsMsec);

            if(getMaxStopPathSpeedMps() < speedMps){
                logger.warn("For stopPath {} the speed of {} is above the max stoppath speed limit of {} m/s. " +
                                "The speed is getting reset to max stoppath speed. arrival/departure {} and {}.",
                        arrDep2.getStopPathId(), speedMps, getMaxStopPathSpeedMps(),arrDep2, arrDep1);
                speedMps = getMaxStopPathSpeedMps();
            } else if(speedMps < getMinStopPathSpeedMps()){
                logger.warn("For stopPath {} the speed of {} is above the below stoppath speed limit of {} m/s. " +
                                "The speed is getting reset to min stoppath speed. arrival/departure {} and {}.",
                        arrDep2.getStopPathId(), speedMps, getMinStopPathSpeedMps(),arrDep2, arrDep1);
                speedMps = getMinStopPathSpeedMps();
            }
            double speedMph = speedMps * Geo.MPS_TO_MPH;
            return speedMph;
        } catch (Exception e){
            logger.error("Unable to calculate speed between arrival/departure {} and {}.", arrDep2, arrDep1, e);
            return null;
        }

    }

    /**
     *
     * Method to get a list of stops and their avg dwell time for a specified period of time
     *
     * @param beginDate
     * @param endDate
     * @param beginTime
     * @param endTime
     * @param routeIdOrShortName
     * @param serviceType
     * @param timePointsOnly
     * @param headsign
     * @param readOnly
     * @return
     * @throws Exception
     */
    public List<IpcStopWithDwellTime> getStopsWithAvgDwellTimes(LocalDate beginDate, LocalDate endDate,
                                                                LocalTime beginTime, LocalTime endTime,
                                                                String routeIdOrShortName, ServiceType serviceType,
                                                                boolean timePointsOnly, String headsign,
                                                                boolean readOnly) throws Exception {

        String routeShortName = getRouteShortName(routeIdOrShortName);

        ArrivalDepartureQuery.Builder adBuilder = new ArrivalDepartureQuery.Builder();
        ArrivalDepartureQuery adQuery = adBuilder
                .beginDate(beginDate)
                .endDate(endDate)
                .beginTime(beginTime)
                .endTime(endTime)
                .routeShortName(routeShortName)
                .headsign(headsign)
                .serviceType(serviceType)
                .timePointsOnly(timePointsOnly)
                .dwellTimeOnly(true)
                .includeStop(true)
                .readOnly(readOnly)
                .build();

        List<ArrivalDeparture> arrivalDepartures = ArrivalDeparture.getArrivalsDeparturesFromDb(adQuery);

        List<IpcStopWithDwellTime> stopsWithAvgDwellTime = new ArrayList<>();

        for(ArrivalDeparture ad : arrivalDepartures){
            Stop stop = ad.getStopFromDb();
            IpcStopWithDwellTime ipcStopWithDwellTime = new IpcStopWithDwellTime(stop, ad.getDirectionId(), ad.getDwellTime());
            stopsWithAvgDwellTime.add(ipcStopWithDwellTime);
        }

        return stopsWithAvgDwellTime;
    }

    /**
     * Get avg run time for all trips in a route for a specified period of time
     *
     * @param beginDate
     * @param endDate
     * @param beginTime
     * @param endTime
     * @param routeIdOrShortName
     * @param serviceType
     * @param timePointsOnly
     * @param headsign
     * @param readOnly
     * @return
     * @throws Exception
     */
    public IpcDoubleSummaryStatistics getAverageRunTime(LocalDate beginDate,
                                                        LocalDate endDate,
                                                        LocalTime beginTime,
                                                        LocalTime endTime,
                                                        String routeIdOrShortName,
                                                        ServiceType serviceType,
                                                        boolean timePointsOnly,
                                                        String headsign,
                                                        boolean readOnly) throws Exception {

        String routeShortName = getRouteShortName(routeIdOrShortName);

        ArrivalDepartureQuery.Builder adBuilder = new ArrivalDepartureQuery.Builder();
        ArrivalDepartureQuery adQuery = adBuilder
                .beginDate(beginDate)
                .endDate(endDate)
                .beginTime(beginTime)
                .endTime(endTime)
                .routeShortName(routeShortName)
                .headsign(headsign)
                .serviceType(serviceType)
                .timePointsOnly(timePointsOnly)
                .scheduledTimesOnly(true)
                .includeTrip(true)
                .readOnly(readOnly)
                .build();

        List<ArrivalDeparture> arrivalDepartures = ArrivalDeparture.getArrivalsDeparturesFromDb(adQuery);

        Map<TripDateKey, Long> runTimeByTripId = getRunTimeByTripDateKey(arrivalDepartures);

        return getAverageRunTimeForAllTrips(runTimeByTripId);
    }

    /*
     * Calculate the trip run times from the list of arrival departures
     * Currently only gets run times for trips with arrival/departure for first and last stops
     * Trips that do not include first and last stop in the selected time range GET CUT OFF
     *
     * TODO - May want to refactor to be smarter about including the entire trip if
     *  the trip is part of the selected time range
     */
    private Map<TripDateKey, Long> getRunTimeByTripDateKey(List<ArrivalDeparture> arrivalDepartures){

        Map<TripDateKey, Long> runTimeByTripDate = new LinkedHashMap<>();

        Map<TripDateKey, List<ArrivalDeparture>> arrivalDeparturesByTripDateKey = getArrivalDeparturesByTripDateKey(arrivalDepartures);

        for(Map.Entry<TripDateKey, List<ArrivalDeparture>> arrivalDepartureByTripAndDate : arrivalDeparturesByTripDateKey.entrySet()){
            TripDateKey key = arrivalDepartureByTripAndDate.getKey();
            List<ArrivalDeparture> arrivalDeparturesForTrip  = arrivalDepartureByTripAndDate.getValue();

            if(arrivalDeparturesForTrip != null && arrivalDeparturesForTrip.size() > 0){
                Long firstStopDepartureTime = null;
                Long lastStopArrivalTime = null;

                Trip trip = arrivalDeparturesForTrip.get(0).getTripFromDb();
                int lastStopPathIndex = trip.getNumberStopPaths() - 1;

                ArrivalDeparture firstDeparture = arrivalDeparturesForTrip.get(0);
                if(firstDeparture.getStopPathIndex() == 0 && firstDeparture.isDeparture()){
                    firstStopDepartureTime = firstDeparture.getTime();
                } else {
                    continue;
                }

                for(int i=1; i< arrivalDeparturesForTrip.size(); i++){
                    ArrivalDeparture ad = arrivalDeparturesForTrip.get(i);
                    if(ad.getStopPathIndex() == lastStopPathIndex && ad.isArrival()){
                        lastStopArrivalTime = ad.getTime();
                        break;
                    }
                }

                if(firstStopDepartureTime != null && lastStopArrivalTime != null){
                    runTimeByTripDate.put(key,lastStopArrivalTime - firstStopDepartureTime);
                }
            }
        }

        return runTimeByTripDate;
    }

    /*
     *  Groups Arrival Departures by Trip/Date key
     *  Allows us to retrieve first and last arrival/departure per trip
     */
    private Map<TripDateKey, List<ArrivalDeparture>> getArrivalDeparturesByTripDateKey(List<ArrivalDeparture> arrivalDepartures){
        Map<TripDateKey, List<ArrivalDeparture>> arrivalDeparturesByTripKey = new LinkedHashMap<>();

        for(ArrivalDeparture ad : arrivalDepartures){
            LocalDate localDate = Instant.ofEpochMilli(ad.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
            TripDateKey tripKey = new TripDateKey(ad.getTripId(), localDate);
            if(arrivalDeparturesByTripKey.get(tripKey) == null){
                arrivalDeparturesByTripKey.put(tripKey, new ArrayList<>());
            }
            arrivalDeparturesByTripKey.get(tripKey).add(ad);
        }
        return arrivalDeparturesByTripKey;
    }

    /*
     * Get summary statistics for trip/date run times
     */
    private IpcDoubleSummaryStatistics getAverageRunTimeForAllTrips(Map<TripDateKey, Long> runTimeByTripId) {
        DoubleSummaryStatistics summaryStatistics =  runTimeByTripId.entrySet().stream()
                .mapToDouble(Map.Entry::getValue)
                .summaryStatistics();

        return new IpcDoubleSummaryStatistics(summaryStatistics);
    }
}
