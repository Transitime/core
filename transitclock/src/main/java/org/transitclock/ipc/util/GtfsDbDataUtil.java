package org.transitclock.ipc.util;

import org.apache.commons.lang3.StringUtils;
import org.transitclock.applications.Core;
import org.transitclock.db.structs.Route;
import org.transitclock.exceptions.InvalidRouteException;
import org.transitclock.gtfs.DbConfig;

public class GtfsDbDataUtil {
    /**
     * For getting route from routeIdOrShortName. Tries using
     * routeIdOrShortName as first a route short name to see if there is such a
     * route. If not, then uses routeIdOrShortName as a routeId.
     *
     * @param routeIdOrShortName
     * @return The Route, or null if no such route
     */
    public static String getRouteShortName(String routeIdOrShortName) throws InvalidRouteException {
        if(StringUtils.isNotBlank(routeIdOrShortName)){
            DbConfig dbConfig = Core.getInstance().getDbConfig();
            Route dbRoute =
                    dbConfig.getRouteByShortName(routeIdOrShortName);
            if (dbRoute == null)
                dbRoute = dbConfig.getRouteById(routeIdOrShortName);
            if (dbRoute != null){
                String route = dbRoute.getShortName();
                if(StringUtils.isNotBlank(route)){
                    return route;
                }
            }
        } else {
            return "";
        }
        throw new InvalidRouteException(routeIdOrShortName);
    }
}
