/*
 * This file is part of transitclock.org
 * 
 * transitclock.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * transitclock.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitclock.custom.mbta;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.avl.AvlModule;
import org.transitclock.avl.PollUrlAvlModule;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.AvlReport.AssignmentType;
import org.transitclock.modules.Module;

/**
 * AVL module for reading AVL data from MBTA BusLoc feed.
 * 
 * @author Sean Óg Crudden
 *
 */
public class BusLocAvlModule extends PollUrlAvlModule {

	private static StringConfigValue mbtaBusLocFeedUrl = 
			new StringConfigValue("transitclock.avl.mbtaBusLocFeedUrl", "https://s3.amazonaws.com/mbta-busloc-s3/VehiclePositions_enhanced.json",
					"The URL of the BusLoc json feed to use.");

	
	private static StringConfigValue mbtaTestRoute = 
			new StringConfigValue("transitclock.avl.testroute", "77",
					"Route to test against.");

	
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
	public BusLocAvlModule(String agencyId) {
		super(agencyId);
	}

	/**
	 * Returns URL to use
	 */
	@Override
	protected String getUrl() {
		return mbtaBusLocFeedUrl.getValue();
	}

	/**
	 * Called when AVL data is read from URL. Processes the JSON data and calls
	 * processAvlReport() for each AVL report.
	 */
	@Override
	protected Collection<AvlReport> processData(InputStream in) throws Exception {
		// Get the JSON string containing the AVL data
		String jsonStr = getJsonString(in);
		Collection<AvlReport> avlReportsReadIn = new ArrayList<AvlReport>();
		try {
			// Convert JSON string to a JSON object
			JSONObject jsonObj = new JSONObject(jsonStr);

			JSONArray entities=(JSONArray) jsonObj.get("entity");
			
			//JSONArray vehicles = entityObj.getJSONArray("vehicle");
			
			for(int i=0;i<entities.length();i++)
			{
				JSONObject entity=entities.getJSONObject(i);
				JSONObject vehicle=entity.getJSONObject("vehicle");
				JSONObject vehicleIdentity=vehicle.getJSONObject("vehicle");
				JSONObject vehiclePosition=vehicle.getJSONObject("position");
				JSONObject vehicleTrip=vehicle.getJSONObject("trip");
				
				if(vehicleTrip.has("route_id") && (mbtaTestRoute==null || vehicleTrip.getString("route_id").equals(mbtaTestRoute.getValue())))
				{
					String blockid=vehicle.getString("block_id");
					long timestamp=vehicle.getLong("timestamp");
										
					// Create the AvlReport
					AvlReport avlReport =
							new AvlReport(vehicleIdentity.getString("id"), timestamp*1000, vehiclePosition.getDouble("latitude"), vehiclePosition.getDouble("longitude"), Float.NaN,
									(float) vehiclePosition.getInt("bearing"), "BusLoc");	
					
					// Actually set the assignment
					avlReport.setAssignment(blockid,
							AssignmentType.BLOCK_ID);
		
					logger.debug("From KeolisAvlModule {}", avlReport);
					
					if (shouldProcessAvl) {
						avlReportsReadIn.add(avlReport);
					}
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
		
		// Create a BusLocAvlModule for testing
		Module.start("org.transitclock.custom.mbta.BusLocAvlModule");
	}

}
