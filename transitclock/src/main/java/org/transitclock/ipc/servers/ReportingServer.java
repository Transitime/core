package org.transitclock.ipc.servers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.ServiceType;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Route;
import org.transitclock.db.structs.Stop;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.ipc.data.IpcArrivalDepartureScheduleAdherence;
import org.transitclock.ipc.data.IpcStopWithDwellTime;
import org.transitclock.ipc.interfaces.ReportingInterface;
import org.transitclock.ipc.rmi.AbstractServer;

import java.util.*;

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
            Date beginDate, Date endDate, String routeIdOrShortName, ServiceType serviceType,
            boolean timePointsOnly, String headsign) throws Exception{
        return getArrivalsDeparturesForRoute(beginDate, endDate, routeIdOrShortName, serviceType,
                timePointsOnly, headsign, false);
    }

    @Override
    public List<IpcArrivalDepartureScheduleAdherence> getArrivalsDeparturesForRoute(
            Date beginDate, Date endDate, String routeIdOrShortName, ServiceType serviceType, boolean timePointsOnly,
            String headsign, boolean readOnly) throws Exception {

        String routeId = null;

        if(StringUtils.isNotBlank(routeIdOrShortName)){
            Route dbRoute = getRoute(routeIdOrShortName);
            if (dbRoute == null)
                return null;
            routeId = dbRoute.getId();
        }

        List<ArrivalDeparture> arrivalDepartures = ArrivalDeparture.getArrivalsDeparturesFromDb(beginDate,
                                                            endDate, routeId, serviceType, timePointsOnly, headsign, readOnly);

        List<IpcArrivalDepartureScheduleAdherence> ipcArrivalDepartures = new ArrayList<>();

        for(ArrivalDeparture arrivalDeparture : arrivalDepartures){
            IpcArrivalDepartureScheduleAdherence ipcArrivalDeparture = new IpcArrivalDepartureScheduleAdherence(arrivalDeparture);
            ipcArrivalDepartures.add(ipcArrivalDeparture);
        }
        return ipcArrivalDepartures;
    }

    @Override
    public List<IpcStopWithDwellTime> getStopsWithAvgDwellTimes(Date beginDate, Date endDate, String routeIdOrShortName,
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
                endDate, routeId, serviceType, timePointsOnly, headsign, readOnly);

        List<IpcStopWithDwellTime> stopsWithAvgDwellTime = new ArrayList<>();

        for(ArrivalDeparture ad : arrivalDepartures){
            /*Trip trip1 = ad.getTripFromDb();
            if(headsign != null) {
                Trip trip = ad.getTripFromDb();
                if(!trip.getHeadsign().equalsIgnoreCase(headsign)){
                    continue;
                }
            }*/
            Stop stop = ad.getStopFromDb();
            IpcStopWithDwellTime ipcStopWithDwellTime = new IpcStopWithDwellTime(stop, ad.getDirectionId(), ad.getDwellTime());
            stopsWithAvgDwellTime.add(ipcStopWithDwellTime);
        }

        return stopsWithAvgDwellTime;

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
}
