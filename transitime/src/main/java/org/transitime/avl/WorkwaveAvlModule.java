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
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.StringConfigValue;
import org.transitime.configData.AvlConfig;
import org.transitime.db.structs.AvlReport;
import org.transitime.modules.Module;

/**
 * AVL module for Workwave AVL feed. Polls JSON feed. First gets a valid
 * session ID to pass to the AVL requests.
 * 
 * @author Michael Smith
 *
 */
public class WorkwaveAvlModule extends PollUrlAvlModule {

	private final static String AVL_URL = 
			"http://gps.workwave.com/php/points.php";
	
	private final static String GET_SESSION_URL = 
			"http://gps.workwave.com/map/view-only?api-key=";
	
	private final static DateFormat workwaveTimeFormat =
			new SimpleDateFormat("MM/dd/yy hh:mm:ss a");

	private static StringConfigValue apiKey =
			new StringConfigValue("transitime.avl.workwaveApiKey",
					"The API key to use when getting session ID for Workwave "
					+ "AVL feed.");
			
	// The authorization cookie to be provided when requesting AVL data
	String sessionId = null;
	
	private static final Logger logger = LoggerFactory
			.getLogger(TranslocAvlModule.class);

	/********************** Member Functions **************************/

	public WorkwaveAvlModule(String agencyId) {
		super(agencyId);

		// Initialize the session ID
		setSessionId();
	}

	/**
	 * Access the page GET_SESSION_URL to get the cookie PHPSESSID and stores
	 * the resulting cookie in sessionId member. Then when getting AVL data
	 * can send the PHPSESSID cookie as authentication.
	 */
	private void setSessionId() {
		// Make sure the API key is configured
		if (apiKey.getValue() == null) {
			logger.error("API key not set for WorkwaveAvlModule. Needs to be "
					+ "set via Java property {} .",	apiKey.getID());
			return;
		}
		
		String fullUrl = null;
		try {
			// Create the connection
			fullUrl = GET_SESSION_URL + apiKey.getValue();
			URL url = new URL(fullUrl);
			URLConnection con = url.openConnection();
			
			// Set the timeout so don't wait forever
			int timeoutMsec = AvlConfig.getAvlFeedTimeoutInMSecs();
			con.setConnectTimeout(timeoutMsec);
			con.setReadTimeout(timeoutMsec);
			
			// Get the PHPSESSID cookie to use as the session ID
			Map<String, List<String>> headerFields = con.getHeaderFields();
			List<String> cookies = headerFields.get("Set-Cookie");
			for (String cookie : cookies) {
				if (cookie.contains("PHPSESSID")) {
					int equalSign = cookie.indexOf('=');
					int semicolon = cookie.indexOf(';');
					sessionId = cookie.substring(equalSign + 1, semicolon);
					
					logger.info("Session ID for Workwave AVL feed is "
							+ "PHPSESSID={}", sessionId);
				}
			}
		} catch (Exception e) {
			logger.error("Could not access URL {}.", fullUrl, e);
		}
	}
	
	/**
	 * Set the PHPSESSID as a cookie as authorization
	 */
	@Override
	protected void setRequestHeaders(URLConnection con) {
		if (sessionId == null)
			logger.error("PHPSESSID not set when trying to fetch AVL data.");
		
		con.setRequestProperty("Cookie", "PHPSESSID=" + sessionId);
	}

	@Override
	protected String getUrl() {
		return AVL_URL;
	}

	/**
	 * Reads in and processes JSON AVL data
	 */
	@Override
	protected Collection<AvlReport> processData(InputStream in) throws Exception {
		String jsonStr = getJsonString(in);
		try {
			JSONObject jsonObj = new JSONObject(jsonStr);
			
			// Make sure no problem getting data
			boolean success = jsonObj.getBoolean("success");
			if (!success) {
				String message = jsonObj.getString("message");
				logger.error("Error occurred when getting AVL data. URL={} and "
						+ "PHPSESSID={}. {}", 
						getUrl(), sessionId, message);
				
				// Since there was a problem try getting new session ID since
				// perhaps old one is invalid.
				setSessionId();
				return new ArrayList<AvlReport>();
			}
			
			// The return value for the method
			Collection<AvlReport> avlReportsReadIn = new ArrayList<AvlReport>();
			
			JSONObject points = jsonObj.getJSONObject("points");
			JSONArray features = points.getJSONArray("features");
			for (int i=0; i<features.length(); ++i) {
				JSONObject feature = features.getJSONObject(i);
				
				// Get latitude/longitude
				JSONObject geometry = feature.getJSONObject("geometry");
				JSONArray coordinates = geometry.getJSONArray("coordinates");
				double latitude = Double.parseDouble(coordinates.getString(1));
				double longitude = Double.parseDouble(coordinates.getString(0));
				
				// Get vehicleId, heading, velocity, and timestamp
				JSONObject properties = feature.getJSONObject("properties");
				String vehicleId = properties.getString("deviceId");
				float heading = 
						(float) Double.parseDouble(properties.getString("heading"));
				float speed = 
						(float) Double.parseDouble(properties.getString("velocity"));
				String gpsTimeStr = properties.getString("timestamp");
				Date gpsTime = workwaveTimeFormat.parse(gpsTimeStr);
				
				// Process AVL report
				AvlReport avlReport =
						new AvlReport(vehicleId, gpsTime.getTime(), latitude, longitude,
								speed, heading, "WorkWave");
				
				avlReportsReadIn.add(avlReport);
			}
			
			// Return all the AVL reports read in
			return avlReportsReadIn;
		} catch (JSONException e) {
			logger.error("Error parsing JSON. {}. {}", 
					e.getMessage(), jsonStr, e);
			return new ArrayList<AvlReport>();
		}

	}

	/**
	 * Just for debugging
	 */
	public static void main(String[] args) {
		Module.start("org.transitime.avl.WorkwaveAvlModule");
	}

} 
