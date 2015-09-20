/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License (GPL) as published by the
 * Free Software Foundation, either version 3 of the License, or any later
 * version.
 * 
 * Transitime.org is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Transitime.org . If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitime.avl;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.avl.amigocloud.AmigoWebsocketListener;
import org.transitime.avl.amigocloud.AmigoWebsockets;
import org.transitime.config.LongConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.db.structs.AvlReport;
import org.transitime.logging.Markers;
import org.transitime.modules.Module;
import org.transitime.utils.Geo;
import org.transitime.utils.Time;

/**
 * AVL module for websocket based feed from AmigoCloud.
 * 
 * @author SkiBu Smith
 *
 */
public class AmigoCloudAvlModule extends AvlModule {
	private static StringConfigValue feedUrl = new StringConfigValue(
			"transitime.avl.amigocloud.apiToken",
			"R:0pKSjytdXHfGMfWdRjY5xGGUU3FgaamySwDK0u",
			"The API token obtained from AmigoCloud via "
					+ "https://www.amigocloud.com/accounts/tokens that allows "
					+ "access to their feed.");

	private static LongConfigValue userId =
			new LongConfigValue(
					"transitime.avl.amigocloud.userId",
					1194L,
					"The ID of the client. Obtained using the amigocloud api "
							+ "token via \"curl https://www.amigocloud.com/api/v1/me?token=API_TOKEN\"");

	private static LongConfigValue projectId = new LongConfigValue(
			"transitime.avl.amigocloud.projectId", 661L,
			"The amigocloud ID of the agency that getting AVL data " + "from.");

	private static LongConfigValue datasetId = new LongConfigValue(
			"transitime.avl.amigocloud.datasetId", 12556L,
			"The amigocloud ID of the dataset for the agency that "
					+ "getting AVL data from.");

	private static final Logger logger = LoggerFactory
			.getLogger(AmigoCloudAvlModule.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor. Declared public so can work via Module.start() using
	 * reflection
	 * 
	 * @param agencyId
	 */
	public AmigoCloudAvlModule(String agencyId) {
		super(agencyId);
	}

	/**
	 * Inner listener class. onMessage() is called for each AVL report from the
	 * websocket feed
	 */
	private static class MyAmigoWebsocketListener implements
			AmigoWebsocketListener {
		private AmigoCloudAvlModule avlModule;
		
		/**
		 * Constructor. So can store the AmigoCloudAvlModule so that it
		 * can be accessed to call processAvlReport().
		 * 
		 * @param avlModule
		 */
		private MyAmigoWebsocketListener(AmigoCloudAvlModule avlModule) {
			this.avlModule = avlModule;
		}
		
		/**
		 * Called by WebSocketClient.onClose() to indicate connection closed and
		 * there is a problem. Restarts the connection after a few seconds. The
		 * delay might be important to make sure that don't keep repeatedly
		 * reconnecting. Plus the old connection might not be completely closed
		 * when onClose() is called since can't tell from the documentation on
		 * WebSocket.
		 * 
		 * @param code
		 * @param reason
		 */
		@Override
		public void onClose(int code, String reason) {
			logger.error("AmigoCloudAvlModule.onClose() called so will restart "
					+ "the connection after 10 seconds...");
			
			// Restart the connection after a few seconds. Use a TimerTask so that
			// it happens in a separate thread to allow the old web socket to 
			// first be completely closed.
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					avlModule.startRealtimeWebsockets();
				}
			};
			new Timer().schedule(task, 10 * Time.SEC_IN_MSECS);
		};
		
		/**
		 * Called for every AVL report
		 */
		@Override
		public void onMessage(String message) {
			try {
				JSONObject obj = new JSONObject(message);
				JSONArray argsArray = obj.getJSONArray("args");
				for (int i=0; i<argsArray.length(); ++i) {
					JSONObject arg = argsArray.getJSONObject(i);
					JSONArray dataArray = arg.getJSONArray("data");
					for (int j=0; j<dataArray.length(); ++j) {
						JSONObject data = dataArray.getJSONObject(j);
						
						// Read in AVL report data from JSON
						String objectId = data.getString("object_id");
						String vehicleId =
								objectId.substring(0, objectId.indexOf('-'))
										.trim();
						long timestamp =
								Long.parseLong(data.getString("timestamp")) 
									* Time.MS_PER_SEC;
						double latitude =
								Double.parseDouble(data.getString("latitude"));
						double longitude =
								Double.parseDouble(data.getString("longitude"));
						
						// Heading is a special case. It is set to 0.0000 when
						// it is actually not available. For this case set it
						// to Float.NaN
						float heading =
								Float.parseFloat(data.getString("track"));
						if (heading == 0.0)
							heading = Float.NaN;
						
						// By looking at GPS reports for speed and time and 
						// measuring distance traveled using a map found that
						// units appears to be mph.
						float speed =
								Float.parseFloat(data.getString("speed"))
										* Geo.MPH_TO_MPS;
						
						// Read in some other info that probably isn't useful 
						// but what the heck
						String satelliteFix = data.getString("satellite_fix");
						String satellites = data.getString("satellites");
						String altitude = data.getString("altitude");
						
						// Get a lot of data so this probably should be debug logging
						logger.debug("AmigoCloud AVL report v={} t={} {} lat={} "
								+ "lon={} heading={} speed={} satelliteFix={} "
								+ "satellites={} altitude={}", 
								vehicleId, timestamp, new Date(timestamp), latitude, longitude, 
								heading, speed, satelliteFix, satellites, 
								altitude);
						
						// Actually process the report
						AvlReport avlReport =
								new AvlReport(vehicleId, timestamp, latitude,
										longitude, speed, heading, "AmigoCloud");
						avlModule.processAvlReport(avlReport);
					}
				}
			} catch (JSONException | NumberFormatException e) {
				logger.error("Exception {} parsing from amigocloud AVL feed {}", 
						e.getMessage(), e);
			}
		}
	}

	/**
	 * For in case want to test a non-realtime amigocloud feed
	 */
	public void startNonRealtimeWebsockets() {
		AmigoWebsockets socket =
				new AmigoWebsockets(userId.getValue(), feedUrl.toString(),
						new MyAmigoWebsocketListener(this));
		socket.connect();
	}

	/**
	 * Initiates a real-time amigocloud feed. If there is a JSONException 
	 * while starting connection then will try again every 10 seconds
	 * until successful. 
	 */
	public void startRealtimeWebsockets() {
		logger.info("Starting amigocloud AVL feed");

		int numberOfExceptions = 0;
		boolean exceptionOccurred = false;
		do {
			try {
				// Actually make the connection
				AmigoWebsockets socket =
						new AmigoWebsockets(userId.getValue(),
								projectId.getValue(), datasetId.getValue(),
								feedUrl.toString(),
								new MyAmigoWebsocketListener(this));
				socket.connect();
				exceptionOccurred = false;
			} catch (JSONException e) {
				++numberOfExceptions;
				exceptionOccurred = true;
				
				// If exception has occurred several times then send e-mail to 
				// indicate there is an ongoing problem
				if (numberOfExceptions == 3) {
					logger.error(
							Markers.email(),
							"Exception when starting up AmigoCloudAvlModule. "
							+ "{}. numberOfExceptions={}",
							e.getMessage(), numberOfExceptions, e);
				}
				
				// Sleep 10 seconds before trying again
				Time.sleep(10 * Time.MS_PER_SEC);
			}
		} while (exceptionOccurred);
	}

	/**
	 * Called in separate thread when module is started up
	 */
	@Override
	public void run() {
		// For testing VTA system
		startRealtimeWebsockets();
	}

	/**
	 * For debugging
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Module.start("org.transitime.avl.AmigoCloudAvlModule");
	}
}
