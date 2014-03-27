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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 *
 * For retrieving VehicleData from the database
 *
 */
public class DataFetcher {
	// For caching database connection
	private static Connection databaseConnection = null;
	
	
	/**
	 * Get the VehicleData from the database
	 * @return
	 */
	private static List<VehicleData> queryDatabase(Connection connection) {
		List<VehicleData> vehicleDataList = new ArrayList<VehicleData>();
		
		// If connection not valid then return empty list
		if (connection == null)
			return vehicleDataList;
		
		Statement statement = null;
		try {
			statement = connection.createStatement();
			String queryString = GtfsRealtimeExample.usingPolling() ?
					ConfigurableParameters.DATABASE_POLLING_QUERY :
					ConfigurableParameters.DATABASE_QUERY;
			ResultSet rs = statement.executeQuery(queryString);
			while (rs.next()) {
				String vehicleId = rs.getString("vehicle_id");
				String licensePlate = rs.getString("license_plate");
				float latitude = rs.getFloat("latitude");
				float longitude = rs.getFloat("longitude");
				Date gpsTime = rs.getTimestamp("time");
				String routeId = rs.getString("route_id");
				// Note: speed and heading are read in as Strings so that can
				// determine whether null or not. Cannot do so with getFloat().
				String speed = rs.getString("speed");
				String heading = rs.getString("heading");
				
				// Add the VehiclData to the List
				VehicleData vehicleData = 
						new VehicleData(vehicleId, licensePlate, latitude, longitude, 
								speed, heading, gpsTime, routeId);
				vehicleDataList.add(vehicleData);
			}
		} catch (SQLException e) {
			System.err.println("Exception when running query. " + e.getMessage() + 
					"\nSQL was:\n" + ConfigurableParameters.DATABASE_QUERY);
		} finally {
			// Make sure that the database statement is closed
			if (statement != null)
				try {
					statement.close();
				} catch (SQLException e) {
					System.err.println("Exception when closing database statement. " +
							e.getMessage());
				}
		}
		
		// Return the resulting list of VehicleData objects
		return vehicleDataList;
	}

	/**
	 * Returns database connection. Caches the connection so that new one
	 * not opened up every time.
	 * 
	 * @return the connection. Null if connection could not be created
	 */
	private static Connection getConnection() {
		// If have not created database connection yet, do so now
		if (databaseConnection == null) {			
			Properties connectionProps = new Properties();
			connectionProps.put("user", ConfigurableParameters.DB_USER);
			connectionProps.put("password", ConfigurableParameters.DB_PASSWORD);
			
			try {
				databaseConnection = 
						DriverManager.getConnection(ConfigurableParameters.DB_CONNECTION_URL, 
								connectionProps);
			} catch (SQLException e) {
				System.err.println("Could not create database connection for" 
						+ " DB_USER=" + ConfigurableParameters.DB_USER
						+ " DB_PASSWORD=" + ConfigurableParameters.DB_PASSWORD
						+ " DB_CONNECTION_URL=" + ConfigurableParameters.DB_CONNECTION_URL
						+ " . " + e.getMessage());
			}
		}
		return databaseConnection;
	}
	
	/**
	 * Gets a database connection, queries the database for the GPS data,
	 * and puts the resulting data into a List of VehicleData objects.
	 *  
	 * @return List of VehicleData read from database
	 */
	public static List<VehicleData> queryDatabase() {
		Connection connection = getConnection();
		List<VehicleData> vehicleDataList = queryDatabase(connection);
		return vehicleDataList;
	}
	
	/**
	 * Gets the VehicleData from the database but only returns the last
	 * record for each vehicle. This is useful for when doing polling 
	 * because then don't want or need multiple GPS reports for a vehicle.
	 * Only the most recent one is actually useful.
	 * 
	 * @return List of VehicleData, with only one entry per vehicle
	 */
	public static List<VehicleData> queryDatabaseFilteringDuplicates() {
		// Get the full data set from the database
		List<VehicleData> fullData = queryDatabase();
		
		// Go through list and use only the last GPS report for a vehicle.
		// Go backwards since queryDatabase() query should provide the 
		// data ordered by time. Using a LinkedList since going backwards
		// through data means need to insert into beginning of the list
		// and LinkedList does this efficiently.
		LinkedList<VehicleData> dataFilteredOfDuplicates = new LinkedList<VehicleData>();
		Set<String> vehicleIds = new HashSet<String>();
		for (int i=fullData.size()-1; i>=0; --i) {
			VehicleData vehicleData = fullData.get(i);
			
			// If already dealt with this vehicle then continue to next one
			if (vehicleIds.contains(vehicleData.getVehicleId()))
				continue;
			
			// Vehicle not already dealt with so add it to list
			dataFilteredOfDuplicates.addFirst(vehicleData);
			vehicleIds.add(vehicleData.getVehicleId());
		}
		
		// Return the filtered list
		return dataFilteredOfDuplicates;
	}
	
	/**
	 * Create test VehicleData without accessing database
	 * @return
	 */
	public static List<VehicleData> createTestData() {
		List<VehicleData> vehicleDataList = new ArrayList<VehicleData>();
		for (int i=0; i<100; ++i) {
			VehicleData vehicleData = 
					new VehicleData("你好世界vehicleId" + i, // vehicleId
							"你好世界licensePlate" + 1,      // license plate
							39.1234f,                // latitude 
							116.4567f,               // longitude 
							"3.4",                   // speed 
							"30.0",                  // heading 
							new Date(),              // gpsTime 
							"你好世界routeId" + i);          // routeId
			vehicleDataList.add(vehicleData);
		}
		
		// Return the results
		return vehicleDataList;
	}

}
