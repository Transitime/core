package org.transitclock.ipc.servers;

import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.ServiceType;
import org.transitclock.core.dataCache.TripKey;
import org.transitclock.db.structs.*;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.ipc.data.IpcArrivalDepartureScheduleAdherence;
import org.transitclock.ipc.data.IpcDoubleSummaryStatistics;
import org.transitclock.ipc.data.IpcStopWithDwellTime;
import org.transitclock.ipc.interfaces.ReportingInterface;
import org.transitclock.ipc.rmi.AbstractServer;
import org.transitclock.utils.MathUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

/**
 * @author carabalb
 * Server to allow Reporting information to be queried.
 */
public class ReportingServer extends AbstractServer implements ReportingInterface {

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
    public List<IpcArrivalDepartureScheduleAdherence> getArrivalsDeparturesForRoute(
            LocalDate beginDate, LocalDate endDate, LocalTime beginTime, LocalTime endTime,
            String routeIdOrShortName, ServiceType serviceType,
            boolean timePointsOnly, String headsign) throws Exception{
        return getArrivalsDeparturesForRoute(beginDate, endDate, beginTime, endTime, routeIdOrShortName, serviceType,
                timePointsOnly, headsign, false);
    }

    @Override
    public List<IpcArrivalDepartureScheduleAdherence> getArrivalsDeparturesForRoute(
            LocalDate beginDate, LocalDate endDate, LocalTime beginTime, LocalTime endTime,
            String routeIdOrShortName, ServiceType serviceType, boolean timePointsOnly,
            String headsign, boolean readOnly) throws Exception {

        String routeId = null;

        if(StringUtils.isNotBlank(routeIdOrShortName)){
            Route dbRoute = getRoute(routeIdOrShortName);
            if (dbRoute == null)
                return null;
            routeId = dbRoute.getId();
        }

        List<ArrivalDeparture> arrivalDepartures = ArrivalDeparture.getArrivalsDeparturesFromDb(beginDate, endDate,
                                                    beginTime, endTime, routeId, serviceType, timePointsOnly,
                                                    headsign, false, readOnly);

        List<IpcArrivalDepartureScheduleAdherence> ipcArrivalDepartures = new ArrayList<>();

        for(ArrivalDeparture arrivalDeparture : arrivalDepartures){
            IpcArrivalDepartureScheduleAdherence ipcArrivalDeparture = new IpcArrivalDepartureScheduleAdherence(arrivalDeparture);
            ipcArrivalDepartures.add(ipcArrivalDeparture);
        }
        return ipcArrivalDepartures;
    }

    @Override
    public List<IpcStopWithDwellTime> getStopsWithAvgDwellTimes(LocalDate beginDate, LocalDate endDate,
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

        List<ArrivalDeparture> arrivalDepartures = ArrivalDeparture.getArrivalsDeparturesFromDb(beginDate,
                endDate, beginTime, endTime, routeId, serviceType, timePointsOnly, headsign, false, readOnly);

        List<IpcStopWithDwellTime> stopsWithAvgDwellTime = new ArrayList<>();

        for(ArrivalDeparture ad : arrivalDepartures){
            Stop stop = ad.getStopFromDb();
            IpcStopWithDwellTime ipcStopWithDwellTime = new IpcStopWithDwellTime(stop, ad.getDirectionId(), ad.getDwellTime());
            stopsWithAvgDwellTime.add(ipcStopWithDwellTime);
        }

        return stopsWithAvgDwellTime;

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

        List<ArrivalDeparture> arrivalDepartures = ArrivalDeparture.getArrivalsDeparturesFromDb(beginDate,
                endDate, beginTime, endTime, routeId, serviceType, timePointsOnly, headsign, true, readOnly);

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
}
