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
package org.transitime.custom.mbta;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.avl.AvlModule;
import org.transitime.avl.PollUrlAvlModule;
import org.transitime.config.IntegerConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.AvlReport.AssignmentType;
import org.transitime.modules.Module;
import org.transitime.utils.Geo;
import org.transitime.utils.Time;

/**
 * AVL module for reading AVL data from Keolis feed.
 * 
 * @author Michael Smith (michael@transitime.org)
 *
 */
public class KeolisAvlModule extends PollUrlAvlModule {

	private static StringConfigValue mbtaCommuterRailFeedUrl = 
			new StringConfigValue("transitime.avl.mbtaCommuterRailFeedUrl", 
					"The URL of the Keolis feed to use.");

	private static IntegerConfigValue keolisFeedAvlTimeOffset =
			new IntegerConfigValue("transitime.mbta.keolisFeedAvlTimeOffset", 
					5, 
					"The GPS time from the Keolis feed has a strange 4 or 5 "
					+ "hour offset. It appears that it is 4 hours during "
					+ "saving time but 5 hours during normal winter time.");
	
	// If debugging feed and want to not actually process
	// AVL reports to generate predictions and such then
	// set shouldProcessAvl to false;
	private static boolean shouldProcessAvl = true;
	
	// For logging use AvlModule class so that will end up in the AVL log file
	private static final Logger logger = LoggerFactory
			.getLogger(AvlModule.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor
	 * 
	 * @param agencyId
	 */
	public KeolisAvlModule(String agencyId) {
		super(agencyId);
	}

	/**
	 * Returns URL to use
	 */
	@Override
	protected String getUrl() {
		return mbtaCommuterRailFeedUrl.getValue();
	}

	/**
	 * Called when AVL data is read from URL. Processes the JSON data and calls
	 * processAvlReport() for each AVL report.
	 */
	@Override
	protected Collection<AvlReport> processData(InputStream in) throws Exception {
		// Get the JSON string containing the AVL data
		String jsonStr = getJsonString(in);
		try {
			// Convert JSON string to a JSON object
			JSONObject jsonObj = new JSONObject(jsonStr);

			// The JSON feed is really odd. Instead of having an
			// array of vehicles there is a separate JSON object
			// for each vehicle, and the name of the object is the
			// trip ID. Therefore need to get names of all the objects
			// so that each one can be accessed by name.
			String tripNames[] = JSONObject.getNames(jsonObj);
			
			// The return value for the method
			Collection<AvlReport> avlReportsReadIn = new ArrayList<AvlReport>();

			// For each vehicle...
			for (String tripName : tripNames) {
				JSONObject v = jsonObj.getJSONObject(tripName);
				
				// Create the AvlReport from the JSON data
				String vehicleId = Integer.toString(v.getInt("vehicle_id"));
				Double lat = v.getDouble("vehicle_lat");
				Double lon = v.getDouble("vehicle_lon");
				// Guessing that the speed is in mph since getting values up to 58.
				// Therefore need to convert to m/s.
				float speed = (float) v.getDouble("vehicle_speed") * Geo.MPH_TO_MPS;
				// Not sure if bearing is the same as heading.
				float heading = (float) v.getDouble("vehicle_bearing");
				// The time is really strange. It is off by 4-5 hours for some
				// strange reason. It appears to be 4 hours off during daylight
				// savings time but 5 hours during normal winter hours. Yikes!
				// Therefore using the parameter keolisFeedAvlTimeOffset to
				// specify the offset.
				long gpsTime =
						v.getLong("vehicle_timestamp") * Time.MS_PER_SEC
								+ keolisFeedAvlTimeOffset.getValue()
								* Time.HOUR_IN_MSECS;

				// Create the AvlReport
				AvlReport avlReport =
						new AvlReport(vehicleId, gpsTime, lat, lon, speed,
								heading, "Keolis");

				// Need to set assignment info separately. Unfortunately the 
				// trip ID in the Keolis feed doesn't correspond exactly to the
				// trip IDs in the GTFS data. In Keolis feed it will be 
				// something like "CR-FRAMINGHAM-Weekday-515" but in GTFS it will
				// be "CR-Worcester-CR-Weekday-Worcester-Jun15-515". Therefore 
				// it is best to just use the trip short name, such as "515".
				String tripShortName =
						tripName.substring(tripName.lastIndexOf('-') + 1);
				
				// Actually set the assignment
				avlReport.setAssignment(tripShortName,
						AssignmentType.TRIP_SHORT_NAME);

				logger.debug("From KeolisAvlModule {}", avlReport);
				
				if (shouldProcessAvl) {
					avlReportsReadIn.add(avlReport);
				}
			}	
			
			// Return all the AVL reports read in
			return avlReportsReadIn;
		} catch (JSONException e) {
			logger.error("Error parsing JSON. {}. {}", e.getMessage(), jsonStr,
					e);
			return new ArrayList<AvlReport>();

		}

	}

	/**
	 * Just for debugging
	 */
	public static void main(String[] args) {
		// For debugging turn off the actual processing of the AVL data.
		// This way the AVL data is logged, but that is all.
		shouldProcessAvl = false;
		
		// Create a NextBusAvlModue for testing
		Module.start("org.transitime.custom.mbta.KeolisAvlModule");
	}

}
