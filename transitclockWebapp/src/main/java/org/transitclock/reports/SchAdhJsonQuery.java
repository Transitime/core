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
package org.transitclock.reports;

import org.apache.commons.lang3.StringUtils;
import org.transitclock.applications.Core;
import org.transitclock.utils.Time;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Does a query of vehicle states data and returns result in JSON format.
 * 
 * @author Rebecca Brown
 *
 */
public class SchAdhJsonQuery {
	// Maximum number of rows that can be retrieved by a query
	private static final int MAX_ROWS = 50000;
	
	/**
	 * Queries agency for vehicle state data and returns result as a JSON string. Limited
	 * to returning MAX_ROWS (50,000) data points.
	 * 
	 * @param agencyId

	 * @param vehicleId
	 * 			  selected vehicle Id
	 * @return data in JSON format. Can be empty JSON array if no data
	 *         meets criteria.
	 */
	public static String getJson(String agencyId, String vehicleId) {

		String sql = "SELECT v.vehicleId, v.avlTime, v.blockId, v.routeShortName, -(v.schedAdhMsec / 60000) as schedAdh, v.avlTime "
				+ "FROM VehicleStates v "
				+ "JOIN "
				+ "(select vehicleId, max(avlTime) as avlTime from VehicleStates group by vehicleId) maxv "
				+ "ON maxv.vehicleId = v.vehicleId and maxv.avlTime = v.avlTime "
				+ "WHERE v.schedAdh is not null ";

		if (vehicleId != null && !vehicleId.trim().isEmpty()) {
			sql += " AND v.vehicleId = '" + vehicleId + "' ";
		}

		sql += " ORDER BY v.schedAdhMsec, v.vehicleId";

		String json = GenericJsonQuery.getJsonString(agencyId, sql);

		return json;

	}
	
}
