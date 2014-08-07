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
package org.transitime.ipc.rmi;

import org.transitime.configData.CoreConfig;
import org.transitime.db.webstructs.WebAgency;

/**
 * Contains the info needed for creating an RMI stub.
 * This information exists on the client side.
 * 
 * @author SkiBu Smith
 *
 */
public class RmiStubInfo {

	final String agencyId;	
	final String className;
	
	/********************** Member Functions **************************/

	public RmiStubInfo(String agencyId, String className) {
		this.agencyId = agencyId;
		this.className = className;
	}
	
	public String getAgencyId() {
		return agencyId;
	}
	
	public String getClassName() {
		return className;
	}
	
	/**
	 * Returns the RMI hostname. Will use command line parameter
	 * -Dtransitime.core.rmiHost if it is set. If not set then looks in
	 * WebAgencies table in the web database. Returns null if not configured.
	 * 
	 * @return
	 */
	public String getHostName() {
		// If RMI host is configured in CoreConfig via command line
		// option then use it.
		String configuredRmiHost = CoreConfig.rmiHost();
		if (configuredRmiHost != null)
			return configuredRmiHost;
		
		// RMI host not configured via command line so use value
		// from database.
		WebAgency webAgency = WebAgency.getCachedWebAgency(agencyId);
		return webAgency!=null ? webAgency.getHostName() : null;
	}
}
