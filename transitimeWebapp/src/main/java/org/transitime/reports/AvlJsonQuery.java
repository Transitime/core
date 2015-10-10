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
	 * Queries agency for AVL data and returns result as a JSON string. Limited
	 * to returning 5,000 data points.
	 * 
	 * @param agencyId
	 * @param vehicleId
	 *            Which vehicle to get data for. Set to null or empty string to
	 *            get data for all vehicles
	 * @param beginDate
	 *            date to start query
	 * @param numdays
	 *            of days to collect data for
	 * @param beginTime
	 *            optional time of day during the date range
	 * @param endTime
	 *            optional time of day during the date range
	 * @return AVL reports in JSON format. Can be empty JSON array if no data
	 *         meets criteria.
	 */
	public static String getAvlJson(String agencyId, String vehicleId,
			String beginDate, String numdays, String beginTime, String endTime) {
		//Determine the time portion of the SQL
		String timeSql = "";
		// If beginTime or endTime set but not both then use default values
		if ((beginTime != null && !beginTime.isEmpty())
				|| (endTime != null && !endTime.isEmpty())) {
			if (beginTime == null || beginTime.isEmpty())
				beginTime = "00:00";
			if (endTime == null || endTime.isEmpty())
				endTime = "24:00";
		}
		if (beginTime != null && !beginTime.isEmpty() 
				&& endTime != null && !endTime.isEmpty()) {
			timeSql = " AND time::time BETWEEN '" 
				+ beginTime + "' AND '" + endTime + "' ";
		}

		String sql = "SELECT vehicleId, time, assignmentId, lat, lon, speed, "
				+ "heading, timeProcessed "
				+ "FROM avlreports "
				+ "WHERE time BETWEEN '" + beginDate 
				+ "' AND TIMESTAMP '" + beginDate + "' + INTERVAL '" + numdays + " day' "
				+ timeSql;

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
	 * Queries agency for AVL data and corresponding Match data. By combining in
	 * Match data can see what the block and trip IDs, and possibly other
	 * information, are associated with each AVL report. Returns result as a
	 * JSON string. Limited to returning 5,000 data points.
	 * 
	 * @param agencyId
	 * @param vehicleId
	 *            Which vehicle to get data for. Set to null or empty string to
	 *            get data for all vehicles
	 * @param beginDate
	 *            date to start query
	 * @param numdays
	 *            of days to collect data for
	 * @param beginTime
	 *            optional time of day during the date range
	 * @param endTime
	 *            optional time of day during the date range
	 * @return AVL reports in JSON format. Can be empty JSON array if no data
	 *         meets criteria.
	 */
	public static String getAvlWithMatchesJson(String agencyId, String vehicleId,
			String beginDate, String numdays, String beginTime, String endTime) {
		//Determine the time portion of the SQL
		String timeSql = "";
		// If beginTime or endTime set but not both then use default values
		if ((beginTime != null && !beginTime.isEmpty())
				|| (endTime != null && !endTime.isEmpty())) {
			if (beginTime == null || beginTime.isEmpty())
				beginTime = "00:00";
			if (endTime == null || endTime.isEmpty())
				endTime = "24:00";
		}
		if (beginTime != null && !beginTime.isEmpty() 
				&& endTime != null && !endTime.isEmpty()) {
			timeSql = " AND time::time BETWEEN '" 
				+ beginTime + "' AND '" + endTime + "' ";
		}

		String sql = "SELECT a.vehicleId, a.time, a.assignmentId, a.lat, a.lon, "
				+ "a.speed, a.heading, a.timeProcessed, m.blockId, m.tripId  "
				+ "FROM avlreports a "
				+ "LEFT JOIN matches m "
                + "ON a.vehicleId = m.vehicleId "
                + "AND a.time = m.avltime "
				+ "WHERE a.time BETWEEN '" + beginDate 
				+ "' AND TIMESTAMP '" + beginDate + "' + INTERVAL '" + numdays + " day' "
				+ timeSql;

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
	
}
