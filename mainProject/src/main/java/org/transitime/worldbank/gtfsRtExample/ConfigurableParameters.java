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
package org.transitime.worldbank.gtfsRtExample;


/**
 *
 * Parameters that need to be configured for the individual transit agency.
 * 
 * @author SkiBu Smith
 */
public class ConfigurableParameters {

	// File name where to write the GTFS-realtime data
	public static String OUTPUT_FILE_NAME = "/Users/Mike/gtfsRealtimeData";

	// Specify whether using Microsoft Windows or Linux
	public static final boolean MICROSOFT_WINDOWS = true;

	// Parameters that specify how the database is accessed
	public static final String DB_USER = "root";
	public static final String DB_PASSWORD = "transitime";
	public static final String DB_CONNECTION_URL = "jdbc:mysql://localhost/sf-muni";

	// The database queries used. The two queries provide data for different
	// time ranges. DATABASE_QUERY should return data writing large amount of
	// data to a file. DATABASE_POLLING_QUERY should return data for the 
	// polling period, such as "WHERE time > NOW() - INTERVAL 30 SECOND ".
	// If a license_plate column not available in database then use null.
	// It is important that the SQL results have the proper column names.
	// Therefore each column is described using a "AS" clause. For example,
	// if the route ID column in the database is called "route_description"
	// then the SQL should be "route_description AS route_id"so that the 
	// result will have the desired "route_id" name. If data such as 
	// license plate or route ID is not available then still need to set
	// it to null so that the results of the query are completed. It is important
	// to order the results by GPS time so that it can be processed in the 
	// proper order.
	public static final String DATABASE_QUERY =
			"SELECT vehicleid AS vehicle_id, \n" +
	                "null AS license_plate, \n" +
					"lat AS latitude, \n" +
	                "lon AS longitude, \n" +
					"time AS time, \n" +
	                "null AS route_id, \n" +
					"speed AS speed, \n" +
	                "heading AS heading \n" +
					"FROM avlreports \n" +
	                "WHERE time BETWEEN CURDATE() - INTERVAL 2 DAY AND CURDATE()\n" +
					"ORDER BY time";
	
	public static final String DATABASE_POLLING_QUERY =
			"SELECT vehicleid AS vehicle_id, \n" +
	                "null AS license_plate, \n" +
					"latitude AS latitude, \n" +
	                "longitude AS longitude, \n" +
					"time AS time, \n" +
	                "null AS route_id, \n" +
					"speed AS speed, \n" +
	                "heading AS heading \n" +
					"FROM avlreports \n" +
	                "WHERE time > NOW() - INTERVAL 32 SECOND \n" +
					"ORDER BY time";
	
}
