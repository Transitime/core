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

package org.transitime.applications;

import org.transitime.db.webstructs.WebAgency;

public class CreateWebAgency {

	/**
	 * For storing a web agency in the web database
	 * 
	 * @param args
	 *            agencyId = args[0]; hostName = args[1]; dbName = args[2];
	 *            dbType = args[3]; dbHost = args[4]; dbUserName = args[5];
	 *            dbPassword = args[6];
	 */
	public static void main(String args[]) {
		// Determine all the params
		if (args.length <= 5) {
			System.err.println("Specify params for the WebAgency: agencyId = args[0]; hostName = args[1]; dbName = args[1]; dbType = args[2]; dbHost = args[3]; dbUserName = args[4]; dbPassword = args[5];");
			System.exit(-1);
		}
		String agencyId = args[0];
		String hostName = args[1];
		boolean active = true;
		String dbName = args[2];
		String dbType = args[3];
		String dbHost = args[4];
		String dbUserName = args[5];
		String dbPassword = args[6];
		// Name of database where to store the WebAgency object
		String webAgencyDbName = "web";
		
		// Create the WebAgency object
		WebAgency webAgency = new WebAgency(agencyId, hostName, active, dbName,
				dbType, dbHost, dbUserName, dbPassword);
		System.out.println("Storing " + webAgency);
		
		// Store the WebAgency
		webAgency.store(webAgencyDbName);
	}
}
