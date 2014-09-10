/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitime.ipc.servers;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.db.structs.Agency;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.Route;
import org.transitime.db.structs.Trip;
import org.transitime.db.structs.TripPattern;
import org.transitime.gtfs.DbConfig;
import org.transitime.ipc.data.IpcBlock;
import org.transitime.ipc.data.IpcRoute;
import org.transitime.ipc.data.IpcRouteSummary;
import org.transitime.ipc.data.IpcStopsForRoute;
import org.transitime.ipc.data.IpcTrip;
import org.transitime.ipc.data.IpcTripPattern;
import org.transitime.ipc.interfaces.ConfigInterface;
import org.transitime.ipc.rmi.AbstractServer;

/**
 * Implements ConfigInterface to serve up configuration information to RMI
 * clients. 
 * 
 * @author SkiBu Smith
 * 
 */
public class ConfigServer extends AbstractServer implements ConfigInterface {

	// Should only be accessed as singleton class
	private static ConfigServer singleton;
	

	private static final Logger logger = 
			LoggerFactory.getLogger(ConfigServer.class);

	/********************** Member Functions **************************/

	/**
	 * Starts up the ConfigServer so that RMI calls can query for configuration
	 * data. This will automatically cause the object to continue to run and
	 * serve requests.
	 * 
	 * @param agencyId
	 * @return the singleton PredictionsServer object. Usually does not need to
	 *         used since the server will be fully running.
	 */
	public static ConfigServer start(String agencyId) {
		if (singleton == null) {
			singleton = new ConfigServer(agencyId);
		}
		
		if (!singleton.getProjectId().equals(agencyId)) {
			logger.error("Tried calling ConfigServer.start() for " +
					"agencyId={} but the singleton was created for agencyId={}", 
					agencyId, singleton.getProjectId());
			return null;
		}
		
		return singleton;
	}

	/**
	 * Constructor. Made private so that can only be instantiated by
	 * get(). Doesn't actually do anything since all the work is done in
	 * the superclass constructor.
	 * 
	 * @param agencyId
	 *            for registering this object with the rmiregistry
	 */
	private ConfigServer(String agencyId) {
		super(agencyId, ConfigInterface.class.getSimpleName());
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.ConfigInterface#getRoutes()
	 */
	@Override
	public Collection<IpcRouteSummary> getRoutes() throws RemoteException {
		// Get the db route info
		DbConfig dbConfig = Core.getInstance().getDbConfig();
		Collection<org.transitime.db.structs.Route> dbRoutes = 
				dbConfig.getRoutes();
		
		// Convert the db routes into ipc routes
		Collection<IpcRouteSummary> ipcRoutes = 
				new ArrayList<IpcRouteSummary>(dbRoutes.size());
		for (org.transitime.db.structs.Route dbRoute : dbRoutes) {
			IpcRouteSummary ipcRoute = new IpcRouteSummary(dbRoute);
			ipcRoutes.add(ipcRoute);
		}
		
		// Return the collection of ipc routes
		return ipcRoutes;
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.ConfigInterface#getRoute(java.lang.String)
	 */
	@Override
	public IpcRoute getRoute(String routeIdOrShortName, String stopId,
			String tripPatternId) throws RemoteException {
		// Get the db route info 
		DbConfig dbConfig = Core.getInstance().getDbConfig();
		org.transitime.db.structs.Route dbRoute = 
				dbConfig.getRouteByShortName(routeIdOrShortName);
		if (dbRoute == null)
			dbRoute = dbConfig.getRouteById(routeIdOrShortName);
		
		// If no such route then return null since can't create a IpcRoute
		if (dbRoute == null)
			return null;
		
		// Convert db route into an ipc route
		IpcRoute ipcRoute = new IpcRoute(dbRoute, stopId, tripPatternId);
		
		// Return the ipc route
		return ipcRoute;
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.ConfigInterface#getStops(java.lang.String)
	 */
	@Override
	public IpcStopsForRoute getStops(String routeIdOrShortName)
			throws RemoteException {
		// Get the db route info 
		DbConfig dbConfig = Core.getInstance().getDbConfig();
		org.transitime.db.structs.Route dbRoute = 
				dbConfig.getRouteByShortName(routeIdOrShortName);
		if (dbRoute == null)
			dbRoute = dbConfig.getRouteById(routeIdOrShortName);

		// If no such route then return null since can't create 
		// a IpcStopsForRoute
		if (dbRoute == null)
			return null;
		
		// Convert db route into an ipc route
		IpcStopsForRoute ipcStopsForRoute = new IpcStopsForRoute(dbRoute);
		
		// Return the ipc route
		return ipcStopsForRoute;
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.ConfigInterface#getBlock(java.lang.String, java.lang.String)
	 */
	@Override
	public IpcBlock getBlock(String blockId, String serviceId)
			throws RemoteException {
		Block dbBlock = 
				Core.getInstance().getDbConfig().getBlock(serviceId, blockId);
		
		// If no such block then return null since can't create a IpcBlock
		if (dbBlock == null)
			return null;
		
		return new IpcBlock(dbBlock);
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.ConfigInterface#getTrip(java.lang.String)
	 */
	@Override
	public IpcTrip getTrip(String tripId) throws RemoteException {
		Trip dbTrip = Core.getInstance().getDbConfig().getTrip(tripId);

		// If no such trip then return null since can't create a IpcTrip
		if (dbTrip == null)
			return null;
		
		return new IpcTrip(dbTrip);
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.ConfigInterface#getTripPattern(java.lang.String)
	 */
	@Override
	public List<IpcTripPattern> getTripPatterns(String routeIdOrShortName)
			throws RemoteException {
		DbConfig dbConfig = Core.getInstance().getDbConfig();
		String routeId = routeIdOrShortName;
		List<TripPattern> dbTripPatterns = 
				dbConfig.getTripPatternsForRoute(routeId);
		if (dbTripPatterns == null) {
			// Couldn't find by assuming that routeIdOrShortName is the route
			// ID so assume it is the route short name and try again
			Route route = dbConfig.getRouteByShortName(routeIdOrShortName);
			if (route != null)
				dbTripPatterns = dbConfig.getTripPatternsForRoute(route.getId());
		}
		
		if (dbTripPatterns == null)
			return null;
		
		List<IpcTripPattern> tripPatterns = new ArrayList<IpcTripPattern>();
		for (TripPattern dbTripPattern : dbTripPatterns) {
			tripPatterns.add(new IpcTripPattern(dbTripPattern));
		}
		return tripPatterns;
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.ConfigInterface#getAgencies()
	 */
	@Override
	public List<Agency> getAgencies() throws RemoteException {
		return Core.getInstance().getDbConfig().getAgencies();
	}
	
}
