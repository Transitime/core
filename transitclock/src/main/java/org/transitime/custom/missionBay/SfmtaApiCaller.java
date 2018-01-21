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

package org.transitime.custom.missionBay;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.IntegerConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.db.structs.AvlReport;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class SfmtaApiCaller {

	private static StringConfigValue telemetryUrl = 
			new StringConfigValue("transitime.sfmta.telemetryUrl", 
					"https://services.sfmta.com/shuttle/api/Telemetries/",
					"The telemetry URL for the SFMTA API");

	private static StringConfigValue stopsUrl = 
			new StringConfigValue("transitime.sfmta.telemetryUrl", 
					"https://services.sfmta.com/shuttle/api/StopEvents/",
					"The stops URL for the SFMTA API");

	private static StringConfigValue login =
			new StringConfigValue("transitime.sfmta.login",
					"TransitTimeApiUser",
					"Login for basic authentication.");
	
	private static StringConfigValue password =
			new StringConfigValue("transitime.sfmta.password",
					"SFMTA$hutt1e$3G",
					"Password for basic authentication.");
	
	private static StringConfigValue techProviderId = 
			new StringConfigValue("transitime.sfmta.techProviderId", 
					"248",
					"The TechProviderId for the SFMTA API");

	private static StringConfigValue shuttleCompanyId = 
			new StringConfigValue("transitime.sfmta.shuttleCompanyId", 
					"11",
					"The ShuttleCompanyId for the SFMTA API");

	private static IntegerConfigValue timeout =
			new IntegerConfigValue("transitime.sfmta.timeout",
					10000,
					"Timeout in msec for API calls. A timeout of 0 is "
					+ "interpreted as an infinit timeout.");
	
	private static IntegerConfigValue avlReportsBatchSize =
			new IntegerConfigValue("transitime.sfmta.avlReportsBatchSize",
					10,
					"Won't actually post AVL reports to SFMTA API until this "
					+ "many have been received.");
	
	private static class VInfo {
		VInfo(String vehiclePlacardNum, String licensePlateNum) {
			this.vehiclePlacardNum = vehiclePlacardNum;
			this.licensePlateNum = licensePlateNum;
		}
		private String vehiclePlacardNum;
		private String licensePlateNum;
	}
	
	private static Map<String, VInfo> vehicleInfo = null;
	
	// For format: 2014-11-05T08:15:30-08:00
	private static SimpleDateFormat dateFormat = 
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-08:00");
	
	// For batching the AVL reports
	private static List<AvlReport> avlReports = new ArrayList<AvlReport>();
	
	private static final Logger logger = LoggerFactory
			.getLogger(SfmtaApiCaller.class);

	/********************** Member classes *******************************/
	
	/**
	 * Contains the data for a SFMTA API stop report
	 */
	private static class StopInfo {
		private String vehicleId;
		private int stopId;
		private double stopLat;
		private double stopLon;
		private Date arrivalTime;
		private Date departureTime;
		
		/**
		 * Constructor
		 * 
		 * @param vehicleId
		 * @param stopId Numeric SFMTA stop ID
		 * @param stopLat
		 * @param stopLon
		 * @param arrivalTime
		 * @param departureTime
		 */
		public StopInfo(String vehicleId, int stopId, double stopLat,
			double stopLon, Date arrivalTime, Date departureTime) {
			this.vehicleId = vehicleId;
			this.stopId = stopId;
			this.stopLat = stopLat;
			this.stopLon = stopLon;
			this.arrivalTime = arrivalTime;
			this.departureTime = departureTime;
		}
	}
	
	/********************** Member Functions  **************************/

	private static void init() {
		if (vehicleInfo != null)
			return;
		
		vehicleInfo = new HashMap<String, VInfo>();
		
		vehicleInfo.put("1207", new VInfo("11-004", "8JO8212"));
		vehicleInfo.put("1209", new VInfo("11-006", "8JO8211"));
		vehicleInfo.put("N-1210", new VInfo("11-003", "8JO8206"));
		vehicleInfo.put("1369", new VInfo("11-002", "96458B1"));
		vehicleInfo.put("1370", new VInfo("11-001", "06459B1"));
		vehicleInfo.put("947", new VInfo("11-007", "07276B1"));
		vehicleInfo.put("1552", new VInfo("11-008", "83093S1"));
		vehicleInfo.put("1553", new VInfo("11-009", "83092S1"));
		vehicleInfo.put("1285", new VInfo("11-005", "8U13888"));

		// These buses seem to have existed at some point but are supposedly now not active 
		vehicleInfo.put("693", new VInfo("693", "693"));
		vehicleInfo.put("694", new VInfo("694", "694"));
	}
	
	/**
	 * Returns date/time in format 2014-11-05T08:15:30-08:00
	 * 
	 * @param date
	 * @return
	 */
	private static String formattedTime(Date date) {
		return dateFormat.format(date);
	}
	
	/**
	 * Returns JSON for telemetry API call
	 * 
	 * @param avlReports
	 * @return
	 */
	private static String getTelemetryJson(List<AvlReport> avlReports) {
		StringBuilder sb = new StringBuilder();

		sb.append("{ \n");
		sb.append("  \"TechProviderId\": " + techProviderId.getValue() + ", \n");
		sb.append("  \"ShuttleCompanyId\": \"" + shuttleCompanyId.getValue() + "\", \n");
		
		sb.append("  \"Telemetries\": { \n");
		sb.append("    \"Telemetry\": [ \n");
		
		boolean firstAvlReport = true;
		for (AvlReport avlReport : avlReports) {
			if (!firstAvlReport)
				sb.append("    ,\n");
			firstAvlReport = false;
					
			VInfo vinfo = vehicleInfo.get(avlReport.getVehicleId());
			if (vinfo == null) {
				logger.error("No info for vehicleId={}", avlReport.getVehicleId());
				continue;
			}

			sb.append("    { \n");
			sb.append("      \"VehiclePlacardNum\": \"" + vinfo.vehiclePlacardNum + "\",\n");
			sb.append("      \"LicensePlateNum\": \"" + vinfo.licensePlateNum + "\",\n");
			
			// Stop info is not know but desired by the API??!?
			sb.append("      \"VehicleStatus\": " + 1 + ",\n");
//			sb.append("      \"StopId\": " + 0 + ",\n");
//			sb.append("      \"StopLocationLatitude\": " + 0.0 + ",\n");
//			sb.append("      \"StopLocationLongitude\": " + 0.0 + ",\n");

			
			sb.append("      \"LocationLatitude\": " + avlReport.getLat() + ",\n");
			sb.append("      \"LocationLongitude\": " + avlReport.getLon() + ",\n");
			sb.append("      \"TimeStampLocal\": \"" + formattedTime(avlReport.getDate()) + "\"\n"); 
			sb.append("    } \n");
		}
		sb.append("    ] \n");
		sb.append("  } \n");
		sb.append("} \n");
		
		return sb.toString();
	}
	
	/**
	 * Returns JSON for stop API call
	 * 
	 * @param stopInfos
	 * @return
	 */
	private static String getStopJson(List<StopInfo> stopInfos) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{ \n");
		sb.append("  \"TechProviderId\": " + techProviderId.getValue() + ", \n");
		sb.append("  \"ShuttleCompanyId\": \"" + shuttleCompanyId.getValue() + "\", \n");
		
		sb.append("  \"StopEvents\": { \n");
		sb.append("    \"StopEvent\": [ \n");
		
		boolean firstStopReport = true;
		for (StopInfo stopInfo : stopInfos) {
			if (!firstStopReport)
				sb.append("    ,\n");
			firstStopReport = false;
					
			VInfo vinfo = vehicleInfo.get(stopInfo.vehicleId);
			if (vinfo == null) {
				logger.error("No info for vehicleId={}", stopInfo.vehicleId);
				continue;
			}
			sb.append("    { \n");
			sb.append("      \"VehiclePlacardNum\": \"" + vinfo.vehiclePlacardNum + "\",\n");
			sb.append("      \"LicensePlateNum\": \"" + vinfo.licensePlateNum + "\",\n");
			sb.append("      \"StopId\": " + stopInfo.stopId + ",\n");
			sb.append("      \"StopTimeStart\": \"" + formattedTime(stopInfo.arrivalTime) + "\",\n"); 
			sb.append("      \"StopTimeEnd\": \"" + formattedTime(stopInfo.departureTime) + "\"\n"); 
			sb.append("      \"StopLocationLatitude\": " + stopInfo.stopLat + ",\n");
			sb.append("      \"StopLocationLongitude\": " + stopInfo.stopLon + ",\n");
			sb.append("    } \n");
		}
		sb.append("    ] \n");
		sb.append("  } \n");
		sb.append("} \n");
		
		return sb.toString();
	}
	
	/**
	 * Posts the JSON string to the URL. For either the telemetry or the stop
	 * command.
	 * 
	 * @param baseUrl
	 * @param jsonStr
	 * @return True if successfully posted the data
	 */
	private static boolean post(String baseUrl, String jsonStr) {
		try {
			// Create the connection
			URL url = new URL(baseUrl);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

			// Set parameters for the connection
			con.setRequestMethod("POST"); 
			con.setRequestProperty("content-type", "application/json");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches (false);
			
			// API now uses basic authentication
			String authString = login.getValue() + ":" + password.getValue();
			byte[] authEncBytes =
					Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			con.setRequestProperty("Authorization", "Basic "
					+ authStringEnc);

			// Set the timeout so don't wait forever (unless timeout is set to 0)
			int timeoutMsec = timeout.getValue();
			con.setConnectTimeout(timeoutMsec);
			con.setReadTimeout(timeoutMsec);

			// Write the json data to the connection
			DataOutputStream wr = new DataOutputStream(con.getOutputStream ());
			wr.writeBytes(jsonStr);
			wr.flush();
			wr.close();
			
			// Get the response
			int responseCode = con.getResponseCode();
			
			// If wasn't successful then log the response so can debug
			if (responseCode != 200) {			
				String responseStr = "";
				if (responseCode != 500) {
					// Response code indicates there was a problem so get the
					// reply in case API returned useful error message
					InputStream inputStream = con.getErrorStream();
					if (inputStream != null) {
						BufferedReader in =
								new BufferedReader(new InputStreamReader(
										inputStream));
						String inputLine;
						StringBuffer response = new StringBuffer();
						while ((inputLine = in.readLine()) != null) {
							response.append(inputLine);
						}
						in.close();
						responseStr = response.toString();
					}
				}

				// Lot that response code indicates there was a problem
				logger.error("Bad HTTP response {} when writing data to SFMTA "
						+ "API. Response text=\"{}\" URL={} json=\n{}",
						responseCode, responseStr, baseUrl, jsonStr);
			}

			// Done so disconnect just for the heck of it
			con.disconnect();
			
			// Return whether was successful
			return responseCode == 200;
		} catch (IOException e) {
			logger.error("Exception when writing data to SFMTA API: \"{}\" "
					+ "URL={} json=\n{}", 
					e.getMessage(), baseUrl, jsonStr);
			
			// Return that was unsuccessful
			return false;
		}
		
	}
	
	/**
	 * Posts list of telemetry data to SFMTA API
	 * 
	 * @param avlReports
	 */
	private static void postAvlReports(List<AvlReport> avlReports) {
		init();
		
		// Convert AVL data to a JSON string
		String jsonStr = getTelemetryJson(avlReports);
		
		// Post data to the API
		if (post(telemetryUrl.getValue(), jsonStr)) {
			logger.info("Successfully pushed out {} telemetry data points to "
					+ "SFMTA API", avlReports.size());
			logger.debug("Successfully pushed telemetry data json=\n{}", 
					jsonStr);
		};
	}
	
	/**
	 * Posts list of stop data to SFMTA API
	 * 
	 * @param avlReports
	 */
	public static void postStopReports(List<StopInfo> stopInfos) {
		init();

		// Convert AVL data to a JSON string
		String jsonStr = getStopJson(stopInfos);
		
		// Post data to the API
		if (post(stopsUrl.getValue(), jsonStr)) {
			logger.info("Successfully pushed out stop data to SFMTA API");
			logger.debug("Successfully pushed stop data json=\n{}", 
					jsonStr);
		};
	}
	
	/**
	 * Posts a single stop report to the SFMTA API.
	 * 
	 * @param vehicleId
	 * @param stopId
	 * @param lat
	 * @param lon
	 * @param arrivalTime
	 * @param departureTime
	 */
	public static void postStopReport(String vehicleId, int stopId, double lat,
			double lon, Date arrivalTime, Date departureTime) {
		init();
		
		StopInfo stopInfo = new StopInfo(vehicleId, stopId, lat, lon,
				arrivalTime, departureTime);
 
		// Put the StopInfo into a list so can call postStopReports()
		List<StopInfo> stopInfos = new ArrayList<StopInfo>();
		stopInfos.add(stopInfo);
		
		// Actually post the data
		postStopReports(stopInfos);
	}
	
	/**
	 * To be called when system has a new AVL report. Batches the reports
	 * together. If have enough reports (as specified by avlReportsBatchSize)
	 * then actually writes them to the API.
	 * <p>
	 * Synchronized in case multiple threads used when accessing the avlReports
	 * cache
	 * 
	 * @param avlReport
	 */
	public synchronized static void postAvlReportWhenAppropriate(
			AvlReport avlReport) {
		avlReports.add(avlReport);
		if (avlReports.size() >= avlReportsBatchSize.getValue()) {
			postAvlReports(avlReports);
			avlReports.clear();
		}
	}
	
	/**
	 * For debugging.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		AvlReport avlReport1 = new AvlReport("693",
				System.currentTimeMillis(), 37.12345, -122.4567, null);
		AvlReport avlReport2 = new AvlReport("694",
				System.currentTimeMillis(), 37.12345, -122.4567, null);
		List<AvlReport> avlReports = new ArrayList<AvlReport>();
		avlReports.add(avlReport1);
		avlReports.add(avlReport2);

		postAvlReports(avlReports);

		StopInfo s1 = new StopInfo("693", 3124, 37.12345, -122.4567,
				new Date(), new Date());
		StopInfo s2 = new StopInfo("694", 4321, 37.12346, -122.4667,
				new Date(), new Date());

		List<StopInfo> stopInfos = new ArrayList<StopInfo>();
		stopInfos.add(s1);
		stopInfos.add(s2);
		
		postStopReports(stopInfos);		
	}
}
