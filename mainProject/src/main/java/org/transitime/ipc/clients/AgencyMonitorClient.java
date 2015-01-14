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

package org.transitime.ipc.clients;

import java.rmi.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.ipc.interfaces.ServerStatusInterface;

/**
 * Makes the ServerStatusInterface.monitor() RMI call easy to access.
 *
 * @author SkiBu Smith
 *
 */
public class AgencyMonitorClient {

	private static final Logger logger = LoggerFactory
			.getLogger(AgencyMonitorClient.class);

	/********************** Member Functions **************************/

	/**
	 * Uses RMI to invoke monitoring of the agency core to see if everything is
	 * operating properly. If there is a problem then and error message is
	 * returned. If everything OK then null returned.
	 * 
	 * @param agencyId
	 *            Which agency to monitor
	 * @return Error message if problem, or null
	 */
	public static String monitor(String agencyId) {
	       ServerStatusInterface serverStatusInterface = 
	    		   ServerStatusInterfaceFactory.get(agencyId);
	   	if (serverStatusInterface == null) {
	   	    logger.error("Could not create ServerStatusInterface for RMI for "
	   	    		+ "agencyId={}", agencyId);
	   	    return null;
	   	}
	   	
	    String resultStr;
		try {
			resultStr = serverStatusInterface.monitor();
		    return resultStr;
		} catch (RemoteException e) {
			logger.error("Exception when trying to monitor agencyId={}. {}", 
					agencyId, e.getMessage());
			return null;
		}
	}
}
