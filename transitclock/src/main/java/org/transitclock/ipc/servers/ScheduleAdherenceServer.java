package org.transitclock.ipc.servers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.ServiceType;
import org.transitclock.core.ServiceUtils;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Calendar;
import org.transitclock.db.structs.Route;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.ipc.interfaces.ScheduleAdherenceInterface;
import org.transitclock.ipc.rmi.AbstractServer;

import java.util.*;

/**
 * @author carabalb
 * Server to allow Schedule Adherence information to be queried.
 */
public class ScheduleAdherenceServer extends AbstractServer implements ScheduleAdherenceInterface {

    // Should only be accessed as singleton class
    private static ScheduleAdherenceServer singleton;

    private static final Logger logger =
            LoggerFactory.getLogger(ScheduleAdherenceServer.class);

    private ScheduleAdherenceServer(String agencyId) {
        super(agencyId, ScheduleAdherenceInterface.class.getSimpleName());
    }

    public static ScheduleAdherenceServer start(String agencyId) {
        if (singleton == null) {
            singleton = new ScheduleAdherenceServer(agencyId);
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
    public List<IpcArrivalDeparture> getArrivalsDeparturesForRoute(
            Date beginDate, Date endDate, String routeIdOrShortName, ServiceType serviceType) throws Exception{
        return getArrivalsDeparturesForRoute(beginDate, endDate, routeIdOrShortName, serviceType, true);
    }

    @Override
    public List<IpcArrivalDeparture> getArrivalsDeparturesForRoute(
            Date beginDate, Date endDate, String routeIdOrShortName, ServiceType serviceType, boolean readOnly) throws Exception {

        String routeId = null;

        if(StringUtils.isNotBlank(routeIdOrShortName)){
            Route dbRoute = getRoute(routeIdOrShortName);
            if (dbRoute == null)
                return null;
            routeId = dbRoute.getId();
        }

        List<IpcArrivalDeparture> arrivalDepartures = ArrivalDeparture.getArrivalsDeparturesForRouteFromDb(beginDate, endDate, routeId, readOnly);
        if(serviceType == null){
            return arrivalDepartures;
        }

        DbConfig dbConfig = Core.getInstance().getDbConfig();

        List<IpcArrivalDeparture> filteredArrivalDepartures = new ArrayList<>();
        Set<String> serviceIdForServiceType = new HashSet<>();
        List<Calendar> allCalendars = dbConfig.getCalendars();

        for (Calendar calendar : allCalendars) {
            if(ServiceUtils.isServiceTypeActiveForServiceCal(serviceType, calendar)){
                serviceIdForServiceType.add(calendar.getServiceId());
            }
        }

        for(IpcArrivalDeparture arrivalDeparture : arrivalDepartures){
            if(serviceIdForServiceType.contains(arrivalDeparture.getServiceId())){
                filteredArrivalDepartures.add(arrivalDeparture);
            }
        }
        return filteredArrivalDepartures;
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
