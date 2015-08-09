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
package org.transitime.reports;

/**
 * Does a query of AVL data and returns result in JSON format.
 * 
 * @author SkiBu Smith
 *
 */
public class AvlJsonQuery {
	
	/**
	 * Queries agency for AVL data and returns result as a JSON string.
	 * 
	 * @param agencyId
	 * @param vehicleId
	 *            Which vehicle to get data for. Set to null or empty string to
	 *            get data for all vehicles
	 * @param startTime
	 * @param endTime
	 * @return AVL reports in JSON format. Can be empty JSON array if no data
	 *         meets criteria.
	 */
	public static String getJson(String agencyId, String vehicleId,
			String startTime, String endTime) {
		String sql = "SELECT vehicleId, time, assignmentId, lat, lon, speed, "
				+ "heading, timeProcessed "
				+ "FROM avlreports "
				+ "WHERE time BETWEEN '" + startTime 
				+ "' AND '" + endTime + "' ";

		// If only want data for single vehicle then specify so in SQL
		if (vehicleId != null && !vehicleId.isEmpty())
			sql += "AND vehicleId='" + vehicleId + "' ";
		
		// Make sure data is ordered by vehicleId so that can draw lines 
		// connecting the AVL reports per vehicle properly. Also then need
		// to order by time to make sure they are in proper order. And
		// lastly, limit AVL reports to 5000 so that someone doesn't try
		// to view too much data at once.
		sql += "ORDER BY vehicleId, time LIMIT 5000";
		
		String json = GenericJsonQuery.getJsonString(agencyId, sql);
		return json;
	}
	
	/**
	 * For testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String agencyId = "sfmta";
		String vehicleId = null;
		String startTime = "2015-08-07 12:25:02.381";
		String endTime = "2015-08-07 12:25:12.381";
		String json = getJson(agencyId, vehicleId, startTime, endTime);
		System.out.println(json);
	}
	
}
