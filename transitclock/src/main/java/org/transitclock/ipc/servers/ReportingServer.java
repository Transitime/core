package org.transitclock.ipc.servers;

import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.DoubleConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.ServiceType;
import org.transitclock.core.TemporalDifference;
import org.transitclock.core.travelTimes.DataFetcher;
import org.transitclock.core.travelTimes.TravelTimesProcessor;
import org.transitclock.db.reporting.*;
import org.transitclock.db.structs.*;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.ipc.data.*;
import org.transitclock.ipc.interfaces.ReportingInterface;
import org.transitclock.ipc.rmi.AbstractServer;
import org.transitclock.utils.Geo;
import org.transitclock.utils.MapKey;
import org.transitclock.utils.Time;

import java.time.*;
import java.util.*;
import java.util.Calendar;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * @author carabalb
 * Server to allow Reporting information to be queried.
 */
public class ReportingServer extends AbstractServer implements ReportingInterface {

    private final boolean DEFAULT_TIME_POINTS_ONLY = false;
    private final boolean DEFAULT_SCHEDULED_TIMES_ONLY = false;
    private final boolean DEFAULT_DWELL_TIME_ONLY = false;
    private final boolean DEFAULT_INCLUDE_TRIP = false;
    private final boolean DEFAULT_INCLUDE_STOP = false;
    private final boolean DEFAULT_INCLUDE_STOP_PATH = false;
    private final ServiceType DEFAULT_SERVICE_TYPE = null;

    private static int getMaxSchedAdh() {
        return maxSchedAdhSec.getValue();
    }
    private static IntegerConfigValue maxSchedAdhSec =
            new IntegerConfigValue(
                    "transitclock.speedMap.maxSchedAdhSec",
                    30*Time.SEC_PER_MIN,
                    "Maximum allowable schedule adherence in sec");

    private static DoubleConfigValue minStopPathSpeedMps =
            new DoubleConfigValue("transitclock.speedMap.minStopPathSpeed",
                    0.0,
                    "If a stop path is determined to have a lower "
                            + "speed than this value in meters/second then the speed "
                            + "will be decreased to meet this limit. Purpose is "
                            + "to make sure that don't get invalid speed values due to "
                            + "bad data.");

    private static DoubleConfigValue maxStopPathSpeedMps =
            new DoubleConfigValue("transitclock.speedMap.maxStopPathSpeed",
                    27.0, // 27.0m/s = 60mph
                    "If a stop path is determined to have a higher "
                            + "speed than this value in meters/second then the speed "
                            + "will be decreased to meet this limit. Purpose is "
                            + "to make sure that don't get invalid speed values due to "
                            + "bad data.");

    // Should only be accessed as singleton class
    private static ReportingServer singleton;

    private static final Logger logger =
            LoggerFactory.getLogger(ReportingServer.class);

    private ReportingServer(String agencyId) {
        super(agencyId, ReportingInterface.class.getSimpleName());
    }

    public static ReportingServer start(String agencyId) {
        if (singleton == null) {
            singleton = new ReportingServer(agencyId);
        }

        if (!singleton.getAgencyId().equals(agencyId)) {
            logger.error("Tried calling ScheduleAdherenceServer.start() for " +
                            "agencyId={} but the singleton was created for agencyId={}",
                    agencyId, singleton.getAgencyId());
            return null;
        }

        return singleton;
    }

    @Override
    public List<IpcArrivalDepartureScheduleAdherence> getArrivalsDeparturesForOtp(
            LocalDate beginDate, LocalDate endDate, LocalTime beginTime, LocalTime endTime,
            String routeIdOrShortName, ServiceType serviceType,
            boolean timePointsOnly, String headsign) throws Exception{
        return getArrivalsDeparturesForOtp(beginDate, endDate, beginTime, endTime, routeIdOrShortName, serviceType,
                timePointsOnly, headsign, false);
    }

    @Override
    public List<IpcArrivalDepartureScheduleAdherence> getArrivalsDeparturesForOtp(
            LocalDate beginDate, LocalDate endDate, LocalTime beginTime, LocalTime endTime,
            String routeIdOrShortName, ServiceType serviceType, boolean timePointsOnly,
            String headsign, boolean readOnly) throws Exception {

        String routeId = null;
        boolean  scheduledStopsOnly = true;

        if(StringUtils.isNotBlank(routeIdOrShortName)){
            Route dbRoute = getRoute(routeIdOrShortName);
            if (dbRoute == null)
                return null;
            routeId = dbRoute.getId();
        }

        List<ArrivalDeparture> arrivalDepartures = ArrivalDeparture.getArrivalsDeparturesFromDb(beginDate, endDate,
                                                    beginTime, endTime, routeId, headsign, serviceType, timePointsOnly,
                                                    scheduledStopsOnly,DEFAULT_DWELL_TIME_ONLY, DEFAULT_INCLUDE_TRIP,
                                                    DEFAULT_INCLUDE_STOP, DEFAULT_INCLUDE_STOP_PATH, readOnly);

        List<IpcArrivalDepartureScheduleAdherence> ipcArrivalDepartures = new ArrayList<>();

        for(ArrivalDeparture arrivalDeparture : arrivalDepartures){
            IpcArrivalDepartureScheduleAdherence ipcArrivalDeparture = new IpcArrivalDepartureScheduleAdherence(arrivalDeparture);
            ipcArrivalDepartures.add(ipcArrivalDeparture);
        }
        return ipcArrivalDepartures;
    }

    @Override
    public List<IpcStopWithDwellTime> getStopsWithAvgDwellTimes(LocalDate beginDate, LocalDate endDate,
                                                                LocalTime beginTime, LocalTime endTime,
                                                                String routeIdOrShortName, ServiceType serviceType,
                                                                boolean timePointsOnly, String headsign,
                                                                boolean readOnly) throws Exception {

        String routeId = null;
        if(StringUtils.isNotBlank(routeIdOrShortName)){
            Route dbRoute = getRoute(routeIdOrShortName);
            if (dbRoute == null)
                return null;
            routeId = dbRoute.getId();
        }

        boolean dwellTimeOnly = true;
        boolean includeStop = true;

        List<ArrivalDeparture> arrivalDepartures = ArrivalDeparture.getArrivalsDeparturesFromDb(beginDate,
                endDate, beginTime, endTime, routeId, headsign, serviceType, timePointsOnly, DEFAULT_SCHEDULED_TIMES_ONLY,
                dwellTimeOnly, DEFAULT_INCLUDE_TRIP, includeStop, DEFAULT_INCLUDE_STOP_PATH, readOnly);

        List<IpcStopWithDwellTime> stopsWithAvgDwellTime = new ArrayList<>();

        for(ArrivalDeparture ad : arrivalDepartures){
            Stop stop = ad.getStopFromDb();
            IpcStopWithDwellTime ipcStopWithDwellTime = new IpcStopWithDwellTime(stop, ad.getDirectionId(), ad.getDwellTime());
            stopsWithAvgDwellTime.add(ipcStopWithDwellTime);
        }

        return stopsWithAvgDwellTime;

    }

    @Override
    public List<IpcStopPathWithSpeed> getStopPathsWithSpeed(LocalDate beginDate, LocalDate endDate,
                                                            LocalTime beginTime, LocalTime endTime,
                                                            String routeIdOrShortName, ServiceType serviceType,
                                                            String headsign, boolean readOnly) throws Exception{

        String routeId = null;
        if(StringUtils.isNotBlank(routeIdOrShortName)){
            Route dbRoute = getRoute(routeIdOrShortName);
            if (dbRoute == null)
                return null;
            routeId = dbRoute.getId();
        }

        boolean includeStopPath = true;

        List<ArrivalDeparture> arrivalDeparturesList = ArrivalDeparture.getArrivalsDeparturesFromDb(beginDate,
                endDate, beginTime, endTime, routeId, headsign, serviceType, DEFAULT_TIME_POINTS_ONLY,
                DEFAULT_SCHEDULED_TIMES_ONLY, DEFAULT_DWELL_TIME_ONLY, DEFAULT_INCLUDE_TRIP, DEFAULT_INCLUDE_STOP,
                includeStopPath, readOnly);

        Map<ArrivalDepartureTripKey, List<ArrivalDeparture>> resultsMap = new HashMap<>();
        Map<String, StopPath> stopPathsMap = new HashMap<>();

        // Put Arrival Departure and StopPaths data into maps
        processSpeedDataIntoMaps(arrivalDeparturesList, resultsMap, stopPathsMap);

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
                              Map<ArrivalDepartureTripKey, List<ArrivalDeparture>> resultsMap,
                              Map<String, StopPath> stopPathsMap){

        TimeZone tz = Agency.getTimeZoneFromDb(this.getAgencyId());

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
                ArrivalDepartureTripKey.dayOfYear(arrDep.getDate(), zoneId), arrDep.getTripId(), arrDep.getVehicleId());
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

    private Double getSpeedDataBetweenTwoArrivalDepartures(ArrivalDeparture arrDep1, ArrivalDeparture arrDep2) {

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
        if (arrDep1.getStopPathIndex() - arrDep2.getStopPathIndex() != 1
                && arrDep1.isDeparture()
                && arrDep2.isArrival()) {
            // Determine the travel times and add them to the map
            travelSpeedForStopPath = determineTravelSpeedForStopPath(arrDep1,arrDep2);

            // Ignore a stop path if any segment travel time is negative. Nulls will
            // be ignored downstream anyway so can also ignore those.
            if (travelSpeedForStopPath == null)
                return null;
        }
        return travelSpeedForStopPath;
    }

    private Double determineTravelSpeedForStopPath(ArrivalDeparture arrDep1, ArrivalDeparture arrDep2) {
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

            if(maxStopPathSpeedMps.getValue() < speedMps){
                logger.warn("For stopPath {} the speed of {} is above the max stoppath speed limit of {} m/s. " +
                            "The speed is getting reset to max stoppath speed. arrival/departure {} and {}.",
                        arrDep2.getStopPathId(), speedMps, maxStopPathSpeedMps.getValue(),arrDep2, arrDep1);
                speedMps = maxStopPathSpeedMps.getValue();
            } else if(speedMps < minStopPathSpeedMps.getValue()){
                logger.warn("For stopPath {} the speed of {} is above the below stoppath speed limit of {} m/s. " +
                                "The speed is getting reset to min stoppath speed. arrival/departure {} and {}.",
                        arrDep2.getStopPathId(), speedMps, minStopPathSpeedMps.getValue(),arrDep2, arrDep1);
                speedMps = minStopPathSpeedMps.getValue();
            }
            double speedMph = speedMps * Geo.MPS_TO_MPH;
            return speedMph;
        } catch (Exception e){
            logger.error("Unable to calculate speed between arrival/departure {} and {}.", arrDep2, arrDep1, e);
            return null;
        }

    }


    @Override
    public IpcDoubleSummaryStatistics getAverageRunTime(LocalDate beginDate, LocalDate endDate,
                                    LocalTime beginTime, LocalTime endTime, String routeIdOrShortName,
                                    ServiceType serviceType, boolean timePointsOnly,
                                    String headsign, boolean readOnly) throws Exception {

        String routeId = null;
        if(StringUtils.isNotBlank(routeIdOrShortName)){
            Route dbRoute = getRoute(routeIdOrShortName);
            if (dbRoute == null)
                return null;
            routeId = dbRoute.getId();
        }

        boolean includeTrip = true;
        boolean scheduledTimesOnly = true;

        List<ArrivalDeparture> arrivalDepartures = ArrivalDeparture.getArrivalsDeparturesFromDb(beginDate,
                endDate, beginTime, endTime, routeId, headsign, serviceType, timePointsOnly, scheduledTimesOnly,
                DEFAULT_DWELL_TIME_ONLY, includeTrip, DEFAULT_INCLUDE_STOP, DEFAULT_INCLUDE_STOP_PATH, readOnly);

        Map<TripDateKey, Long> runTimeByTripId = getRunTimeByTripId(arrivalDepartures);

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
    private Map<TripDateKey, Long> getRunTimeByTripId(List<ArrivalDeparture> arrivalDepartures){

        Map<TripDateKey, Long> runTimeByTripId = new LinkedHashMap<>();
        Map<TripDateKey, List<ArrivalDeparture>> arrivalDeparturesByTripKey = getArrivalDeparturesByTripKey(arrivalDepartures);

        for(Map.Entry<TripDateKey, List<ArrivalDeparture>> adByTrip : arrivalDeparturesByTripKey.entrySet()){
            TripDateKey key =adByTrip.getKey();
            List<ArrivalDeparture> arrivalDeparturesForTrip  = adByTrip.getValue();
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
                    runTimeByTripId.put(key,lastStopArrivalTime - firstStopDepartureTime);
                }
            }
        }

        return runTimeByTripId;
    }

    /*
     *  Groups Arrival Departures by Trip/Date key
     *  Allows us to retrieve first and last arrival/departure per trip
     */
    private Map<TripDateKey, List<ArrivalDeparture>> getArrivalDeparturesByTripKey(List<ArrivalDeparture> arrivalDepartures){
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

    @Override
    public IpcRunTime getRunTimeSummary(LocalDate beginDate, LocalDate endDate,
                                  LocalTime beginTime, LocalTime endTime,
                                  String routeIdOrShortName, String headsign,
                                  String startStop, String endStop,
                                  ServiceType serviceType, boolean timePointsOnly,
                                  boolean currentTripsOnly,boolean readOnly) throws Exception {


        String routeId = null;
        if(StringUtils.isNotBlank(routeIdOrShortName)){
            Route dbRoute = getRoute(routeIdOrShortName);
            if (dbRoute == null)
                return null;
            routeId = dbRoute.getId();
        }

        boolean includeTrip = true;

        List<ArrivalDeparture> arrivalDepartures = ArrivalDeparture.getArrivalsDeparturesFromDb(
                beginDate, endDate, beginTime, endTime, routeId, headsign, startStop, endStop, serviceType,
                timePointsOnly, DEFAULT_SCHEDULED_TIMES_ONLY, DEFAULT_DWELL_TIME_ONLY, includeTrip,
                DEFAULT_INCLUDE_STOP, DEFAULT_INCLUDE_STOP_PATH, readOnly);

        Set<Integer> configRevs = new HashSet<>();
        Map<ArrivalDepartureTripKey, List<ArrivalDeparture>> arrivalDeparturesByTripMap =
                groupArrivalDeparturesByUniqueTrip(arrivalDepartures, configRevs);
        Collection<List<ArrivalDeparture>> arrivalDeparturesByTrip = arrivalDeparturesByTripMap.values();

        Map<String, TripStatistics> tripStatsByRunTimeKey = new HashMap<>();
        for(List<ArrivalDeparture> arrivalDepartureList : arrivalDeparturesByTrip){
            processTripStatsMap(tripStatsByRunTimeKey, arrivalDepartureList);
        }

        IpcRunTime runTime = getRunTimeStats(tripStatsByRunTimeKey);
        return runTime;
    }

    @Override
    public List<IpcRunTimeForTrip> getRunTimeForTrips(LocalDate beginDate, LocalDate endDate,
                                         LocalTime beginTime, LocalTime endTime,
                                         String routeIdOrShortName, String headsign,
                                         String startStop, String endStop,
                                         ServiceType serviceType, boolean timePointsOnly,
                                         boolean currentTripsOnly, boolean readOnly) throws Exception {

        String routeId = null;
        if(StringUtils.isNotBlank(routeIdOrShortName)){
            Route dbRoute = getRoute(routeIdOrShortName);
            if (dbRoute == null)
                return null;
            routeId = dbRoute.getId();
        }

        boolean includeTrip = true;

        List<ArrivalDeparture> arrivalDepartures = ArrivalDeparture.getArrivalsDeparturesFromDb(
                beginDate, endDate, beginTime, endTime, routeId, headsign, startStop, endStop,
                serviceType, timePointsOnly, DEFAULT_SCHEDULED_TIMES_ONLY, DEFAULT_DWELL_TIME_ONLY,
                includeTrip, DEFAULT_INCLUDE_STOP, DEFAULT_INCLUDE_STOP_PATH, readOnly);

        Set<Integer> configRevs = new HashSet<>();
        Map<ArrivalDepartureTripKey, List<ArrivalDeparture>> arrivalDeparturesByTripMap =
                groupArrivalDeparturesByUniqueTrip(arrivalDepartures, configRevs);
        Collection<List<ArrivalDeparture>> arrivalDeparturesByTrip = arrivalDeparturesByTripMap.values();

        Map<String, TripStatistics> tripStatsByTripId = new HashMap<>();
        for(List<ArrivalDeparture> arrivalDepartureList : arrivalDeparturesByTrip){
            processTripStatsMap(tripStatsByTripId, arrivalDepartureList);
        }

        List<Trip> trips = Trip.getTripsFromDb(routeId, headsign, configRevs, readOnly);
        Map<String, List<Trip>> tripsByConfigRev = trips.stream().collect(Collectors.groupingBy(Trip::getId));
        List<IpcRunTimeForTrip> runTime = getRunTimeStatsForTrips(tripStatsByTripId, tripsByConfigRev, readOnly);

        return runTime;
    }

    private Map<ArrivalDepartureTripKey, List<ArrivalDeparture>> groupArrivalDeparturesByUniqueTrip(
                                                                            List<ArrivalDeparture> arrivalDepartures,
                                                                            Set<Integer> configRevs){
        TimeZone tz = Agency.getTimeZoneFromDb(this.getAgencyId());
        Map<ArrivalDepartureTripKey, List<ArrivalDeparture>> arrivalDeparturesByTrip = new HashMap<>();

        for(ArrivalDeparture ad: arrivalDepartures){
            ArrivalDepartureTripKey key = ArrivalDepartureTripKey.getKey(ad, tz.toZoneId());
            List<ArrivalDeparture> list = arrivalDeparturesByTrip.get(key);
            if (list == null) {
                list = new ArrayList<>();
                arrivalDeparturesByTrip.put(key, list);
                configRevs.add(ad.getConfigRev());
            }
            list.add(ad);
        }
        return arrivalDeparturesByTrip;
    }

    /**
     * Store TripStatistics in map with tripId as key
     * This is done by going through list of ArrivalsDepartures grouped by TripId and populating stopPath info
     * for TripStastics. Its keyed off Trip so it will collect run time information for all trips that share the same
     * trip Id. This is inspired by TravelTimesProcessor.
     * @param tripStatsByTripId
     * @param arrDepList
     */
    private void processTripStatsMap(Map<String, TripStatistics> tripStatsByTripId, List<ArrivalDeparture> arrDepList) {

        for (int i=0; i<arrDepList.size()-1; ++i) {
            ArrivalDeparture arrDep1 = arrDepList.get(i);

            // Deal with normal travel times
            ArrivalDeparture arrDep2 = arrDepList.get(i+1);
            processTravelTimeBetweenTwoArrivalDepartures(tripStatsByTripId, arrDep1, arrDep2);
        }
    }

    /**
     * Processes travel time and dwell time between two arrival departures
     * Stores them in TripStatistics
     * @param tripStatsByTripId
     * @param arrDep1
     * @param arrDep2
     */
    private void processTravelTimeBetweenTwoArrivalDepartures(Map<String, TripStatistics> tripStatsByTripId,
                                                              ArrivalDeparture arrDep1, ArrivalDeparture arrDep2) {

        Double travelTimeForStopPath = null;
        Double dwellTime = null;
        long departureTime = arrDep1.getTime();

        if (TravelTimesProcessor.shouldResetEarlyTerminalDepartures()
                && arrDep1.getStopPathIndex() == 0
                && arrDep1.getTime() < arrDep1.getScheduledTime()) {
            logger.debug("Note: for {} using scheduled departure time instead "
                            + "of the calculated departure time since "
                            + "transitclock.travelTimes.resetEarlyTerminalDepartures is "
                            + "true and the departure time was (likely incorrectly) "
                            + "calculated to be before the scheduled departure time",
                    arrDep1);
            departureTime = arrDep1.getScheduledTime();
        }

        // If schedule adherence is really far off then ignore the data
        // point because it would skew the results.
        TemporalDifference schedAdh = arrDep1.getScheduleAdherence();
        if (schedAdh == null)
            schedAdh = arrDep2.getScheduleAdherence();
        if (schedAdh != null
                && !schedAdh.isWithinBounds(getMaxSchedAdh(),
                getMaxSchedAdh())) {
            // Schedule adherence is off so don't use this data
            return ;
        }

        // If looking at arrival and departure for same stop then continue
        if (arrDep1.getStopPathIndex() == arrDep2.getStopPathIndex()
                && arrDep1.isArrival()
                && arrDep2.isDeparture()) {
            if(arrDep1.getStopPathIndex() == 0){
                dwellTime = (double) (arrDep2.getTime() - arrDep2.getScheduledTime());
            }else {
                dwellTime = (double) (arrDep2.getTime() - arrDep1.getTime());
            }
            if (dwellTime == null)
                return;
            addStopPathDwellTimeToMap(tripStatsByTripId, arrDep2, dwellTime);
        }

        // If looking at departure from one stop to the arrival time at the
        // very next stop then can determine the travel times between the stops.
        else if (arrDep1.getStopPathIndex() - arrDep2.getStopPathIndex() != 1
                && arrDep1.isDeparture()
                && arrDep2.isArrival()) {
            // Determine the travel times and add them to the map
            travelTimeForStopPath = (double) (arrDep2.getTime() - departureTime);

            // Ignore a stop path if any segment travel time is negative. Nulls will
            // be ignored downstream anyway so can also ignore those.
            if (travelTimeForStopPath == null)
                return;

            addStopPathRunTimeToMap(tripStatsByTripId, arrDep2, travelTimeForStopPath);
        }
    }

    private void addStopPathRunTimeToMap(Map<String, TripStatistics> tripStatsByTripId, ArrivalDeparture ad,
                                         Double runTime){
        TripStatistics tps = getTripStats(tripStatsByTripId, ad);
        tps.addStopPathRunTime(ad, runTime);
    }

    private void addStopPathDwellTimeToMap(Map<String, TripStatistics> tripStatsByTripId, ArrivalDeparture ad,
                                         Double dwellTime){
        TripStatistics tps = getTripStats(tripStatsByTripId, ad);
        tps.addStopPathDwellTime(ad, dwellTime);
    }

    private TripStatistics getTripStats(Map<String, TripStatistics> tripStatsByTripId, ArrivalDeparture ad){
        String key = ad.getTripId();
        TripStatistics tps = tripStatsByTripId.get(key);
        if(tps == null){
            tps = new TripStatistics(ad.getTripId());
            tripStatsByTripId.put(key, tps);
        }
        return tps;
    }

    private IpcRunTime getRunTimeStats(Map<String, TripStatistics> tripStatsByRunTimeKey){
        DoubleSummaryStatistics avgRunTimeStats = new DoubleSummaryStatistics();
        DoubleSummaryStatistics fixedTimeStats = new DoubleSummaryStatistics();
        DoubleSummaryStatistics avgDwellTimeStats = new DoubleSummaryStatistics();

        for(Map.Entry<String, TripStatistics> entry : tripStatsByRunTimeKey.entrySet()){

            for(TripStatisticsForConfigRev tps: entry.getValue().getAllTripStatisticsGroupedByConfigRev()){
                Double avgTripRunTime = tps.getTripAverageRunTime();
                if(avgTripRunTime != null && avgTripRunTime > 0){
                    avgRunTimeStats.accept(avgTripRunTime);
                }

                Double tripFixedTime = tps.getTripFixedRunTime();
                if(tripFixedTime != null && tripFixedTime > 0){
                    fixedTimeStats.accept(tripFixedTime);
                }

                Double tripDwellTime = tps.getAvgDwellTime();
                if(tripDwellTime != null && tripDwellTime > 0){
                    avgDwellTimeStats.accept(tripDwellTime);
                }
            }
        }

        Double avgRunTime = avgRunTimeStats.getCount() > 0 ? avgRunTimeStats.getAverage() : null;
        Double fixedTime = fixedTimeStats.getCount() > 0 ? fixedTimeStats.getMin() : null;
        Double dwellTime = avgDwellTimeStats.getCount() > 0 ? avgDwellTimeStats.getAverage() : null;
        Double variableTime = null;


        if(avgRunTime != null && fixedTime != null) {
            variableTime = avgRunTime - fixedTime;
        }

        return new IpcRunTime(avgRunTime, fixedTime, variableTime, dwellTime);
    }

    private List<IpcRunTimeForTrip> getRunTimeStatsForTrips(Map<String, TripStatistics> tripStatsByTripId,
                                                            Map<String, List<Trip>> tripsByConfigRev,
                                                            boolean readOnly){

        List<IpcRunTimeForTrip> ipcRunTimeForTrips = new ArrayList<>();

        // Loop through each tripId trip Stats
        for(Map.Entry<String, TripStatistics> tripStatEntry : tripStatsByTripId.entrySet()){

            TripStatistics tripStatistics = tripStatEntry.getValue();
            List<TripStatisticsForConfigRev> tripStatsForConfigRev = new ArrayList<>(tripStatistics.getAllTripStatisticsGroupedByConfigRev());

            // Identify first complete trip for tripId
            TripStatisticsForConfigRev mainTrip =  tripStatsForConfigRev.stream()
                                                    .filter(t -> t.hasAllStopPathsForRunTimes() && t.hasAllStopPathsForDwellTimes())
                                                    .findFirst()
                                                    .orElse(null);
            if(mainTrip == null) {
                continue;
            }

            Map<StopPathRunTimeKey, StopPathStatistics> stopPathStatsMap = mainTrip.getAllStopPathStatistics();

            // Looping through all the trip stats for each config rev
            for(int i=0; i < tripStatsForConfigRev.size(); i++){
                if(mainTrip.getConfigRev() == tripStatsForConfigRev.get(i).getConfigRev()){
                    continue;
                }

                // Looping through the stoppath stats
                Map<StopPathRunTimeKey, StopPathStatistics> map = tripStatsForConfigRev.get(i).getAllStopPathStatistics();
                for(Map.Entry<StopPathRunTimeKey, StopPathStatistics> stopPathEntry : map.entrySet()){
                    // Combining stop path values
                    StopPathStatistics sps = stopPathStatsMap.get(stopPathEntry.getKey());
                    if(sps != null){
                        sps.getRunTimeStats().combine(stopPathEntry.getValue().getRunTimeStats());
                        sps.getDwellTimeStats().combine(stopPathEntry.getValue().getDwellTimeStats());
                    }
                }

            }

            DoubleSummaryStatistics avgRunTimeStats = new DoubleSummaryStatistics();
            DoubleSummaryStatistics fixedTimeStats = new DoubleSummaryStatistics();
            DoubleSummaryStatistics avgDwellTimeStats = new DoubleSummaryStatistics();

            Double avgTripRunTime = mainTrip.getTripAverageRunTime();
            if(avgTripRunTime != null && avgTripRunTime > 0){
                avgRunTimeStats.accept(avgTripRunTime);
            }

            Double tripFixedTime = mainTrip.getTripFixedRunTime();
            if(tripFixedTime != null && tripFixedTime > 0){
                fixedTimeStats.accept(tripFixedTime);
            }

            Double tripDwellTime = mainTrip.getAvgDwellTime();
            if(tripDwellTime != null && tripDwellTime > 0){
                avgDwellTimeStats.accept(tripDwellTime);
            }

            Double avgRunTime = avgRunTimeStats.getCount() > 0 ? avgRunTimeStats.getAverage() : null;
            Double fixedTime = fixedTimeStats.getCount() > 0 ? fixedTimeStats.getMin() : null;
            Double dwellTime = avgDwellTimeStats.getCount() > 0 ? avgDwellTimeStats.getAverage() : null;
            Double variableTime = null;

            if(avgRunTime != null && fixedTime != null) {
                variableTime = avgRunTime - fixedTime;
            }

            List<Trip> trips = tripsByConfigRev.get(mainTrip.getTripId());
            if(trips != null){
                for(Trip trip : trips){
                    if(trip.getConfigRev() == mainTrip.getConfigRev()){
                        int currentTripIndex = mainTrip.getTripIndex();
                        Trip nextTrip = trip.getBlockFromDb(readOnly).getTripFromDb(currentTripIndex + 1, readOnly);
                        Integer nextTripStartTime = null;
                        if(nextTrip != null){
                            nextTripStartTime = nextTrip.getStartTime();
                        }
                        ipcRunTimeForTrips.add(new IpcRunTimeForTrip(mainTrip.getTripId(), trip.getStartTime(), trip.getEndTime(), nextTripStartTime, avgRunTime, fixedTime, variableTime, dwellTime));
                        break;
                    }

                }
            }
        }

        return ipcRunTimeForTrips;
    }


    /**
     * For getting route from routeIdOrShortName. Tries using
     * routeIdOrShortName as first a route short name to see if there is such a
     * route. If not, then uses routeIdOrShortName as a routeId.
     *
     * @param routeIdOrShortName
     * @return The Route, or null if no such route
     */
    private Route getRoute(String routeIdOrShortName) {
        DbConfig dbConfig = Core.getInstance().getDbConfig();
        Route dbRoute =
                dbConfig.getRouteByShortName(routeIdOrShortName);
        if (dbRoute == null)
            dbRoute = dbConfig.getRouteById(routeIdOrShortName);
        if (dbRoute != null)
            return dbRoute;
        else return null;
    }

    /**
     * Map Key Classes
     */
    private class TripDateKey{
        private final String tripId;
        private final LocalDate date;

        public TripDateKey(String tripId, LocalDate date){
            this.tripId = tripId;
            this.date = date;
        }

        public String getTripId() {
            return tripId;
        }

        public LocalDate getDate() {
            return date;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TripDateKey that = (TripDateKey) o;
            return Objects.equal(tripId, that.tripId) &&
                    Objects.equal(date, that.date);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(tripId, date);
        }
    }

    public static class ArrivalDepartureTripKey extends MapKey {
        private ArrivalDepartureTripKey(String serviceId, Integer dayOfYear, String tripId,
                             String vehicleId) {
            super(serviceId, dayOfYear, tripId, vehicleId);
        }

        public static ArrivalDepartureTripKey getKey(final String serviceId, final Date date,
                                                     final String tripId, final String vehicleId, ZoneId zone) {
            return new ArrivalDepartureTripKey(serviceId, dayOfYear(date, zone), tripId, vehicleId);
        }

        public static ArrivalDepartureTripKey getKey(final ArrivalDeparture ad, final ZoneId zoneId) {
            return new ArrivalDepartureTripKey(ad.getServiceId(), dayOfYear(ad.getDate(), zoneId),
                                                ad.getTripId(), ad.getVehicleId());
        }

        private static int dayOfYear(Date date, ZoneId zone) {
            // Adjust date by three hours so if get a time such as 2:30 am
            // it will be adjusted back to the previous day. This way can handle
            // trips that span midnight. But this doesn't work for trips that
            // span 3am.
            return date.toInstant().minusMillis(3*Time.MS_PER_HOUR).atZone(zone).toLocalDate().getDayOfYear();
        }

        @Override
        public String toString() {
            return "DbDataMapKey ["
                    + "serviceId=" + o1
                    + ", dayOfYear=" + o2
                    + ", tripId=" + o3
                    + ", vehicleId=" + o4
                    + "]";
        }

    }

}
