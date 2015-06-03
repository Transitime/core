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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

/**
 * Reads AVL data from a Transloc AVL feed and processes each AVL report.
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
	 * Converts the input stream into a JSONObject
	 * 
	 * @param in
	 * @return the JSONObject
	 * @throws IOException
	 * @throws JSONException
	 */
	private JSONObject getJsonObject(InputStream in) throws IOException,
			JSONException {
		BufferedReader streamReader =
				new BufferedReader(new InputStreamReader(in, "UTF-8"));
		StringBuilder responseStrBuilder = new StringBuilder();

		String inputStr;
		while ((inputStr = streamReader.readLine()) != null)
			responseStrBuilder.append(inputStr);

		JSONObject jsonObject = new JSONObject(responseStrBuilder.toString());
		return jsonObject;
	}
	
	/**
	 * Reads in the JSON data from the InputStream and creates and then
	 * processes an AvlReport.
	 */
	@Override
	protected void processData(InputStream in) throws Exception {
		JSONObject jsonObj = getJsonObject(in);
		JSONObject dataObj = jsonObj.getJSONObject("data");
		JSONArray jsonArray = dataObj.getJSONArray(feedAgencyId.getValue());
		for (int i=0; i<jsonArray.length(); ++i) {
			JSONObject vehicleData = jsonArray.getJSONObject(i);
			String vehicleId = vehicleData.getString("vehicle_id");
			
			JSONObject location = vehicleData.getJSONObject("location");
			String latStr = location.getString("lat");
			double lat = Double.parseDouble(latStr);
			String lonStr = location.getString("lng");
			double lon = Double.parseDouble(lonStr);
			
			String headingStr = vehicleData.getString("heading");
			float heading = Float.parseFloat(headingStr);

			// It appears that speed for Transloc API is in mph. Was
			// getting a value of 66.3 which if in m/s would be about
			// 120mph which wouldn't make sense. Unfortunately could
			// not find documentation on the units for speed.
			String speedStr = vehicleData.getString("speed");
			float speed = Float.parseFloat(speedStr) * Geo.MPH_TO_MPS;

			String gpsTimeStr = vehicleData.getString("last_updated_on");
			Date gpsTime = translocTimeFormat.parse(gpsTimeStr);
			
			if (logger.isDebugEnabled()) {
				// Following elements not actually needed but are parsed in 
				// case needed for debugging
				String callName = vehicleData.getString("call_name");
				String routeId = vehicleData.getString("route_id");
				
				logger.debug("vehicleId={} lat={} lon={} heading={} speed={} "
						+ "last_updated_on={} call_name={} route_id={}",
						vehicleId, latStr, lonStr, headingStr, speedStr,
						gpsTimeStr, callName, routeId);
			}
			
			// Process AVL report
			AvlReport avlReport =
					new AvlReport(vehicleId, gpsTime.getTime(), lat, lon,
							speed, heading);
			processAvlReport(avlReport);
		}
	}

	/**
	 * Just for debugging
	 */
	public static void main(String[] args) {
		Module.start("org.transitime.avl.TranslocAvlModule");
	}

}
