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
package org.transitime.avl;

import java.io.InputStream;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.StringConfigValue;
import org.transitime.db.structs.AvlReport;
import org.transitime.modules.Module;
import org.transitime.utils.Geo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Reads AVL data from a Transloc JSON AVL feed and processes each AVL report.
 * <p>
 * Documentation on the Transloc API is at
 * https://www.mashape.com/transloc/openapi-1-2#
 * 
 * @author SkiBu Smith
 *
 */
public class TranslocAvlModule extends PollUrlAvlModule {
	private static StringConfigValue feedUrl = 
			new StringConfigValue("transitime.avl.transloc.url", 
					"https://transloc-api-1-2.p.mashape.com/",
					"The URL of the NextBus feed to use.");

	private static StringConfigValue feedAgencyId =
			new StringConfigValue("transitime.avl.transloc.agencyId",
					"255",
					"The number Transloc agency ID obtained using the Transloc "
					+ "API agencies.json command.");
	
	private static StringConfigValue apiKey =
			new StringConfigValue("transitime.avl.transloc.apiKey", 
					"tXHlJmqevJmsh4q37xKuv3k2vfZ5p1VbAvPjsnwErq18jMCSmb",
					"The API key for the Transloc API.");
	
	private final static DateFormat translocTimeFormat =
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
	
	private static final Logger logger = LoggerFactory
			.getLogger(TranslocAvlModule.class);

	/********************** Member Functions **************************/
	
	public TranslocAvlModule(String agencyId) {
		super(agencyId);
	}

	@Override
	protected String getUrl() {
		return feedUrl.getValue() + "vehicles.json?agencies="
				+ feedAgencyId.getValue();
	}

	/**
	 * Set the API key and JSON format type
	 */
	@Override
	protected void setRequestHeaders(URLConnection con) {
		con.setRequestProperty("X-Mashape-Key", apiKey.getValue());
		con.setRequestProperty("Accept", "application/json");
	}

	/**
	 * Reads in the JSON data from the InputStream and creates and then
	 * processes an AvlReport.
	 * 
	 * @param in
	 */
	@Override
	protected void processData(InputStream in) throws Exception {
		String jsonStr = getJsonString(in);
		try {
			JSONObject jsonObj = new JSONObject(jsonStr);

			JSONObject dataObj = jsonObj.getJSONObject("data");
			JSONArray jsonArray = dataObj.getJSONArray(feedAgencyId.getValue());
			for (int i=0; i<jsonArray.length(); ++i) {
				JSONObject vehicleData = jsonArray.getJSONObject(i);
				String vehicleId = vehicleData.getString("vehicle_id");
				
				// Have found that the feed sometimes include a null location.
				// If so, ignore the data for this vehicle instead of generating
				// an error.
				if (vehicleData.isNull("location"))
					continue;
				
				// Get the location
				JSONObject location = vehicleData.getJSONObject("location");
				double lat = location.getDouble("lat");
				double lon = location.getDouble("lng");
				// For heading use optDouble() since it can be null
				float heading = (float) vehicleData.optDouble("heading");

				// It appears that speed for Transloc API is in kph. Was
				// getting a value of 74.6 which if in m/s would be about
				// 150mph which wouldn't make sense. And it is likely not
				// mph since that would still be too fast. Therefore it
				// is likely to be in kph. Unfortunately could
				// not find documentation on the units for speed.
				float speed = (float) vehicleData.getDouble("speed") * Geo.KPH_TO_MPS;

				String gpsTimeStr = vehicleData.getString("last_updated_on");
				Date gpsTime = translocTimeFormat.parse(gpsTimeStr);
				
				if (logger.isDebugEnabled()) {
					// Following elements not actually needed but are parsed in 
					// case needed for debugging
					String callName = vehicleData.getString("call_name");
					String routeId = vehicleData.getString("route_id");
					
					logger.debug("vehicleId={} lat={} lon={} heading={} speed={} "
							+ "last_updated_on={} call_name={} route_id={}",
							vehicleId, lat, lon, heading, speed,
							gpsTimeStr, callName, routeId);
				}
				
				// Process AVL report
				AvlReport avlReport =
						new AvlReport(vehicleId, gpsTime.getTime(), lat, lon,
								speed, heading, "Transloc");
				processAvlReport(avlReport);
			}
		} catch (JSONException e) {
			logger.error("Error parsing JSON. {}. {}", 
					e.getMessage(), jsonStr, e);
		}
	}

	/**
	 * Just for debugging
	 */
	public static void main(String[] args) {
		Module.start("org.transitime.avl.TranslocAvlModule");
	}

}
