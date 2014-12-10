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
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

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
					"https://sfmta-telemetry.rideintegrity.net/api/1/data/",
					"The telemetry URL for the SFMTA API");

	private static StringConfigValue stopsUrl = 
			new StringConfigValue("transitime.sfmta.telemetryUrl", 
					"https://sfmta-vehicle.rideintegrity.net/api/1/stops/",
					"The stops URL for the SFMTA API");

	private static StringConfigValue key = 
			new StringConfigValue("transitime.sfmta.key", 
					"Z2pUMGM1K1VCcHBLbklBUFRYWlpFdWtmMzVRQm4zZlRWaDNzUFkxc1RpZ1U2RFpVSjRxSkZQL3JyeFVPMlkyUw",
					"The key for the SFMTA API");

	private static StringConfigValue providerId = 
			new StringConfigValue("transitime.sfmta.providerId", 
					"19",
					"The provider ID for the SFMTA API");

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
	
	// For format: 2014-11-05T08:15:30-08:00
	private static SimpleDateFormat dateFormat = 
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-08:00");
	
	// For batching the AVL reports
	private static List<AvlReport> avlReports = new ArrayList<AvlReport>();
	
	private static final Logger logger = LoggerFactory
			.getLogger(SfmtaApiCaller.class);

	/********************** Member class *******************************/
	
	private static class StopInfo {
		String vehicleId;
		int stopId;
		double lat;
		double lon;
		Date arrivalTime;
		Date departureTime;
	}
	
	/********************** Member Functions  **************************/

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
		sb.append("[");
		
		boolean firstAvlReport = true;
		for (AvlReport avlReport : avlReports) {
			if (!firstAvlReport)
				sb.append(",\n");
			firstAvlReport = false;
					
			sb.append("{\n");
			sb.append("\"ProviderId\": " + providerId.getValue() + ",\n");
			sb.append("\"VehicleId\": \"" + avlReport.getVehicleId() + "\",\n");
			sb.append("\"LocationLatitude\": " + avlReport.getLat() + ",\n");
			sb.append("\"LocationLongitude\": " + avlReport.getLon() + ",\n");
			sb.append("\"TimeStampLocal\": \"" + formattedTime(avlReport.getDate()) + "\"\n"); 
			sb.append("}");
		}
		sb.append("]");
		
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
		sb.append("[");
		
		boolean firstStopReport = true;
		for (StopInfo stopInfo : stopInfos) {
			if (!firstStopReport)
				sb.append(",\n");
			firstStopReport = false;
					
			sb.append("{\n");
			sb.append("\"ProviderId\": " + providerId.getValue() + ",\n");
			sb.append("\"VehicleId\": \"" + stopInfo.vehicleId + "\",\n");
			sb.append("\"StopId\": " + stopInfo.stopId + ",\n");
			sb.append("\"StopLocationLatitude\": " + stopInfo.lat + ",\n");
			sb.append("\"StopLocationLongitude\": " + stopInfo.lon + ",\n");
			sb.append("\"StopTimeStart\": \"" + formattedTime(stopInfo.arrivalTime) + "\",\n"); 
			sb.append("\"StopTimeEnd\": \"" + formattedTime(stopInfo.departureTime) + "\"\n"); 
			sb.append("}");
		}
		sb.append("]");
		
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
		String fullUrl = baseUrl + "?" + "key=" + key.getValue()
				/* + "&tname=geo-bus" */; // FIXME tname doesn't seem to be needed. Should confirm

		try {
			// Create the connection
			URL url = new URL(fullUrl);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

			// Set parameters for the connection
			con.setRequestMethod("POST"); 
			con.setRequestProperty("content-type", "application/json");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches (false);
			
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
			if (responseCode != 202 && responseCode != 204) {
				// IVS returns a special error code sometimes
				String ivsErrorCode = con.getHeaderField("Ivs-Error-Code");
				String ivsMessageId = con.getHeaderField("Ivs-Message-Id");
				
				String responseStr = "";
				if (responseCode != 500) {
					// Response code indicates there was a problem so get the 
					// reply in case API returned useful error message
					BufferedReader in = new BufferedReader(
					        new InputStreamReader(con.getErrorStream()));
					String inputLine;
					StringBuffer response = new StringBuffer();
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
					responseStr = response.toString();
				}

				// Lot that response code indicates there was a problem
				logger.error("Bad HTTP response {} when writing data to SFMTA "
						+ "API. Ivs-Error-Code: {} Ivs-Message-Id: {} "
						+ "Response text=\"{}\" URL={} json=\n{}",
						responseCode, ivsErrorCode, ivsMessageId, responseStr,
						fullUrl, jsonStr);
			}

			// Done so disconnect just for the heck of it
			con.disconnect();
			
			// Return whether was successful
			return responseCode == 202 || responseCode == 204;
		} catch (IOException e) {
			logger.error("Exception when writing data to SFMTA API: \"{}\" "
					+ "URL={} json=\n{}", 
					e.getMessage(), fullUrl, jsonStr);
			
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
		// Convert AVL data to a JSON string
		String jsonStr = getTelemetryJson(avlReports);
		
		// Post data to the API
		if (post(telemetryUrl.getValue(), jsonStr)) {
			logger.info("Successfully pushed out {} telemetry data points to "
					+ "API", avlReports.size());
			logger.debug("Successfully pushed telemetry data json=\n{}", 
					jsonStr);
		};
	}
	
	/**
	 * Posts list of stop data to SFMTA API
	 * 
	 * @param avlReports
	 */
	private static void postStopReports(List<StopInfo> stopInfos) {
		// Convert AVL data to a JSON string
		String jsonStr = getStopJson(stopInfos);
		
		// Post data to the API
		if (post(stopsUrl.getValue(), jsonStr)) {
			logger.info("Successfully pushed out stop data to API");
			logger.debug("Successfully pushed stop data json=\n{}", 
					jsonStr);
		};
	}
	
	/**
	 * To be called when system has a new AVL report. Batches the reports
	 * together. If have enough reports (as specified by avlReportsBatchSize)
	 * then actually writes them to the API.
	 * 
	 * @param avlReport
	 */
	public synchronized static void postAvlReportWhenAppropriate(AvlReport avlReport) {
		avlReports.add(avlReport);
		if (avlReports.size() >= avlReportsBatchSize.getValue()) {
			postAvlReports(avlReports);
			avlReports.clear();
		}
	}
	
	public static void main(String args[]) {
		AvlReport avlReport1 = new AvlReport("vehicleId1", System.currentTimeMillis(), 37.12345, -122.4567);
		AvlReport avlReport2 = new AvlReport("vehicleId2", System.currentTimeMillis(), 37.12345, -122.4567);
		List<AvlReport> avlReports = new ArrayList<AvlReport>();
		avlReports.add(avlReport1);
		avlReports.add(avlReport2);
		
		postAvlReports(avlReports);
		
		StopInfo s1 = new StopInfo();
		s1.vehicleId = "vehicleId1";
		s1.stopId = 1324;
		s1.lat = 37.12345;
		s1.lon = -122.4567;
		s1.arrivalTime = new Date();
		s1.departureTime = new Date();
		StopInfo s2 = new StopInfo();
		s2.vehicleId = "vehicleId2";
		s2.stopId = 4321;
		s2.lat = 37.12345;
		s2.lon = -122.4567;
		s2.arrivalTime = new Date();
		s2.departureTime = new Date();
		List<StopInfo> stopInfos = new ArrayList<StopInfo>();
		stopInfos.add(s1);
		stopInfos.add(s2);
		
		postStopReports(stopInfos);
		
	}
}
