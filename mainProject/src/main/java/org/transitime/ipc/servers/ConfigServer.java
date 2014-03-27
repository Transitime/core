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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.gtfs.DbConfig;
import org.transitime.ipc.data.Route;
import org.transitime.ipc.interfaces.ConfigInterface;
import org.transitime.ipc.rmi.AbstractServer;

/**
 * Implements ConfigInterface to serve up configuration information to RMI
 * clients.
 * 
 * @author SkiBu Smith
 * 
 */
public class ConfigServer  extends AbstractServer implements ConfigInterface {

	// Should only be accessed as singleton class
	private static ConfigServer singleton;
	

	private static final Logger logger = 
			LoggerFactory.getLogger(ConfigServer.class);

	/********************** Member Functions **************************/

	/**
	 * Starts up the ConfigServer so that RMI calls can query for
	 * configuration data. This will automatically cause the object to continue to run
	 * and serve requests.
	 * 
	 * @param projectId
	 * @return the singleton PredictionsServer object. Usually does not need to
	 *         used since the server will be fully running.
	 */
	public static ConfigServer start(String projectId) {
		if (singleton == null) {
			singleton = new ConfigServer(projectId);
		}
		
		if (!singleton.getProjectId().equals(projectId)) {
			logger.error("Tried calling PredictionsServer.getInstance() for " +
					"projectId={} but the singleton was created for projectId={}", 
					projectId, singleton.getProjectId());
			return null;
		}
		
		return singleton;
	}

	/*
	 * Constructor. Made private so that can only be instantiated by
	 * get(). Doesn't actually do anything since all the work is done in
	 * the superclass constructor.
	 * 
	 * @param projectId
	 *            for registering this object with the rmiregistry
	 */
	private ConfigServer(String projectId) {
		super(projectId, ConfigInterface.class.getSimpleName());
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.ConfigInterface#getRoutes()
	 */
	@Override
	public Collection<Route> getRoutes() throws RemoteException {
		DbConfig dbConfig = Core.getInstance().getDbConfig();
		Collection<org.transitime.db.structs.Route> dbRoutes = 
				dbConfig.getRoutes();
		Collection<Route> ipcRoutes = new ArrayList<Route>(dbRoutes.size());
		for (org.transitime.db.structs.Route dbRoute : dbRoutes) {
			Route ipcRoute = new Route(dbRoute);
			ipcRoutes.add(ipcRoute);
		}
		return ipcRoutes;
	}

}
