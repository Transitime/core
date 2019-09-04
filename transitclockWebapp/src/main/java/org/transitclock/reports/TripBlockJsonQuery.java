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
import org.transitclock.utils.Time;
import org.transitclock.utils.web.WebUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Does a query of trip data and returns result in JSON format.
 * 
 * @author Rebecca Brown
 *
 */
public class TripBlockJsonQuery {
	// Maximum number of rows that can be retrieved by a query
	private static final int MAX_ROWS = 50000;
	
	/**
	 * Queries agency for trip data and returns result as a JSON string. Limited
	 * to returning MAX_ROWS (50,000) data points.
	 * 
	 * @param agencyId
	 * @param date
	 *            date to start query
	 * @param beginTime
	 *            optional time of day during the date range
	 * @param endTime
	 *            optional time of day during the date range
	 * @param routeId
	 * 			  selected route Id
	 * @return data in JSON format. Can be empty JSON array if no data
	 *         meets criteria.
	 */
	public static String getJson(String agencyId, String date, String beginTime, String endTime, String routeId) {

		//Determine the time portion of the SQL
		// If beginTime or endTime are not set then use default values
		if (beginTime == null || beginTime.isEmpty())
			beginTime = "00:00";
		if (endTime == null || endTime.isEmpty())
			endTime = "24:00";

		//convert into seconds from beginning of day
		int beginSecInDay = Time.parseTimeOfDay(beginTime);
		int endSecInDay = Time.parseTimeOfDay(endTime);

		//get date from params and then get service ids active on that date at noon
		Date d;
		try {
			d = Time.parseDate(date);
		} catch (ParseException e) {
			d = new Date();
		}
		d = getNoonOfDay(d);

		StringBuffer serviceIdsResponse;
		List<String> serviceIds;
        Map<String, String> properties = new HashMap<>();
        properties.put("day", "" + d.getTime());
		try {
			serviceIdsResponse = WebUtils.getApiRequest("/command/getserviceidsforday", properties);
			serviceIds = parseResponse(serviceIdsResponse.toString());
		} catch (IOException ioe) {
			return "Unable to retrieve service ids for day";
		}

		String serviceIdStr = "";
		if (serviceIds != null && serviceIds.size() > 0) {
			serviceIdStr = StringUtils.join(serviceIds, ',');
		}

		String sql = "SELECT t.tripShortName, t.startTime, t.endTime, t.routeShortName, j.Blocks_blockId as blockId "
				+ "FROM Trips t "
				+ "JOIN "
				+ "Block_to_Trip_joinTable j "
				+ "ON j.trips_TripId = t.tripId "
				+ "WHERE "
				+ "t.configRev = (select configRev from ActiveRevisions) "
				+ "AND j.trips_configRev = t.configRev "
				+ "AND j.Blocks_configRev = t.configRev "
				+ "AND t.startTime >= " + beginSecInDay + " AND t.startTime <= " + endSecInDay;

		if (routeId != null && !routeId.trim().isEmpty()) {
			sql += " AND t.routeShortName = '" + routeId + "' ";
		}

		if (serviceIdStr != null && !serviceIdStr.isEmpty()) {
			sql += " AND t.serviceId in (" + serviceIdStr + ") ";
		}


		// Make sure to order by time to make sure they are in proper order. And
		// lastly, limit to 5000 so that someone doesn't try
		// to view too much data at once.
		sql += "ORDER BY j.Blocks_blockId, t.startTime LIMIT " + MAX_ROWS;

		String json = GenericJsonQuery.getJsonString(agencyId, sql);

		return json;

	}

	public static Date getNoonOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 12);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}


	// todo use real json parser
	private static List<String> parseResponse(String json) {
		if (json == null) return null;
		if (json.indexOf(":") < 0) return null;
		json = json.split(":")[1];
		json = json.replaceAll("\\[", "")
				.replaceAll("\\]", "")
				.replaceAll("\\}", "");
		String[] elements = json.split(",");
		List<String> results = new ArrayList<>();
		for (String s : elements) {
			results.add("'" + s.replaceAll("\"", "") + "'");
		}
		return results;
	}
	
}
