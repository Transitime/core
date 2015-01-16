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
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.webstructs.WebAgency;
import org.transitime.ipc.interfaces.ConfigInterface;
import org.transitime.ipc.interfaces.ServerStatusInterface;

/**
 * Makes the ServerStatusInterface.monitor() RMI call easy to access.
 * Intended to be used on client, such as a web page on a web server.
 *
 * @author SkiBu Smith
 *
 */
public class AgencyMonitorClient {

	private static final Logger logger = LoggerFactory
			.getLogger(AgencyMonitorClient.class);

	/********************** Member Functions **************************/

	public static String pingAgency(String agencyId) {
		String msg = null;

		ConfigInterface configInterface = ConfigInterfaceFactory.get(agencyId);
		if (configInterface == null) {
			msg = "Could not create ConfigInterface for RMI";
			logger.error(msg);
		} else {
			try {
				// Do the RMI call to make sure agency is running and can
				// communicate with it.
				configInterface.getAgencies();
			} catch (RemoteException e) {
				msg = "Could not connect via RMI. " + e.getMessage();
				logger.error(msg);
			}
		}
		
		return msg;
	}
	
	/**
	 * Does a simple ping to each agency to make sure that the core for that
	 * agency is running and can communicate with it via IPC.
	 * 
	 * @return An error message if there is a problem, otherwise null
	 */
	public static String pingAllAgencies() {
		String errorMessageForAllAgencies = "";
		Collection<WebAgency> webAgencies = WebAgency.getWebAgencies();
		for (WebAgency webAgency : webAgencies) {
			if (webAgency.isActive()) {
				// Actually do the low-level monitoring on the core system
				String errorMessage = pingAgency(webAgency.getAgencyId());
				if (errorMessage != null)
					errorMessageForAllAgencies += "AgencyId "
							+ webAgency.getAgencyId() + ": " + errorMessage 
							+ "; ";
			}
		}
		
		// Return error message if there is one. Otherwise return null.
		if (errorMessageForAllAgencies.length() > 0)
			return errorMessageForAllAgencies;
		else
			return null;
	}
	
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
			WebAgency webAgency = WebAgency.getCachedWebAgency(agencyId);
			logger.error("Exception when trying to monitor agency {}. {}", 
					webAgency, e.getMessage());
			return "Could not connect via RMI for " + webAgency;
		}
	}

	/**
	 * Goes through all agencies in the WebAgency database table that are
	 * marked as active and monitor them to see if there are any problems.
	 * The monitoring on the server side will automatically send out e-mail
	 * notifications if there is a problems. This method sends out e-mails if
	 * there is a problem connecting to an agency.
	 * <p>
	 * Probably not needed if using MonitoringModule which already monitors
	 * an agency repeatedly.
	 * 
	 * @return
	 */
	public static String monitorAllAgencies() {
		String errorMessageForAllAgencies = "";
		Collection<WebAgency> webAgencies = WebAgency.getWebAgencies();
		for (WebAgency webAgency : webAgencies) {
			if (webAgency.isActive()) {
				// Actually do the low-level monitoring on the core system
				String errorMessage = monitor(webAgency.getAgencyId());
				if (errorMessage != null)
					errorMessageForAllAgencies += "AgencyId "
							+ webAgency.getAgencyId() + ": " + errorMessage 
							+ "; ";
			}
		}
		
		// Return error message if there is one. Otherwise return null.
		if (errorMessageForAllAgencies.length() > 0)
			return errorMessageForAllAgencies;
		else
			return null;
	}
	
}
