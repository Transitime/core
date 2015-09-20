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
package org.transitime.avl.amigocloud;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.logging.Markers;

import java.net.URI;
import java.util.Random;

/**
 * 
 * @author AmigoCloud and Michael Smith Credits to the websockets library:
 *         https://github.com/TooTallNate/Java-WebSocket
 *
 */
public class AmigoWebsockets {
	private WebSocketClient mWs = null;
	private String websocket_token = null;
	private String userID = "";
	private String datasetID = "";

	private boolean connected = false;
	private boolean realtime = false;

	private AmigoWebsocketListener listener = null;

	private static final String BASE_URL = "www.amigocloud.com";
	private static final String END_POINT = "/amigosocket";

	private static final Logger logger = LoggerFactory
			.getLogger(AmigoWebsockets.class);

	/********************** Member Functions **************************/
	
	/**
	 * Constructor for a real-time data feed. Determines the websocket_token
	 * that is used in connect().
	 * 
	 * @param userID
	 * @param projectID
	 * @param datasetID
	 * @param apiToken
	 * @param listener
	 * @throws JSONException
	 */
	public AmigoWebsockets(long userID, long projectID, long datasetID,
			String apiToken, AmigoWebsocketListener listener)
			throws JSONException {
		this.userID = Long.toString(userID);
		this.datasetID = Long.toString(datasetID);
		this.listener = listener;

		this.realtime = true;

		AmigoRest client = new AmigoRest(apiToken);
		String uri =
				"https://" + BASE_URL + "/api/v1/users/" + userID
						+ "/projects/" + projectID + "/datasets/" + datasetID
						+ "/start_websocket_session";
		logger.info("Getting amigocloud websocket session using uri {}", uri);
		String result = client.get(uri);
		
		// If there was a problem getting websocket session then give up.
		// websocket_token will be null for this situation
		if (result == null) {
			logger.error(Markers.email(), 
					"Could not determine websocket session");
			return;
		}
		
		JSONObject obj = null;
		try {
			obj = new JSONObject(result);
			websocket_token = obj.getString("websocket_session");
			logger.info("Using amigocloud websocket_session={}",
					websocket_token);
		} catch (JSONException e) {
			logger.error("{}", e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * Constructor for non real-time data feeds
	 * 
	 * @param userID
	 * @param apiToken
	 * @param listener
	 */
	public AmigoWebsockets(long userID, String apiToken,
			AmigoWebsocketListener listener) {
		this.userID = Long.toString(userID);
		this.listener = listener;
		AmigoRest client = new AmigoRest(apiToken);
		String result =
				client.get("https://" + BASE_URL
						+ "/api/v1/me/start_websocket_session");
		JSONObject obj = null;
		try {
			obj = new JSONObject(result);
			websocket_token = obj.getString("websocket_session");
			logger.info("Using amigocloud websocket_session={}",
					websocket_token);
		} catch (JSONException e) {
			logger.error("{}", e.getMessage(), e);
		}
	}

	/**
	 * @return true if already successfully connected
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Initiate connection to real-time feed
	 * 
	 * @return true if successful
	 */
	public boolean connect() {
		if (websocket_token == null)
			return false;

		try {
			String uri = "ws://" + BASE_URL + ":5005/socket.io/1/websocket/";
			Random rand = new Random();
			uri += Long.toString(rand.nextLong());
			logger.info("Connecting to amigocloud feed using uri {}", uri);
			mWs = new WebSocketClient(new URI(uri), new Draft_10()) {
				@Override
				public void onMessage(String message) {
					parseMessage(message);
				}

				@Override
				public void onOpen(ServerHandshake handshake) {
					logger.info("Opened websockets connection");
				}

				@Override
				public void onClose(int code, String reason, boolean remote) {
					logger.info("closed connection, remote:" + remote
							+ ", code(" + Integer.toString(code)
							+ "), reason: " + reason);
					
					// Let listener know that connection closing so can restart it
					listener.onClose(code, reason);
				}

				@Override
				public void onError(Exception ex) {
					logger.error("Error with amigocloud websocket: {}",
							ex.getMessage(), ex);
				}

			};

			// open websocket
			mWs.connect();

			// Wait for connection
			int counter = 100;
			while (!isConnected() && counter > 0) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.error("Error with amigocloud websocket: {}",
							e.getMessage(), e);
				}
				counter--;
			}

			if (counter > 1) {
				return emit();
			}

		} catch (Exception e) {
			logger.error("Error with amigocloud websocket: {}", e.getMessage(),
					e);

		}
		return false;
	}

	public boolean emit() {
		String emitargs;
		if (realtime) {
			emitargs =
					"{\"userid\":" + userID + ",\"datasetid\":" + datasetID
							+ ",\"websocket_session\":\"" + websocket_token
							+ "\"}";
		} else {
			emitargs =
					"{\"userid\":" + userID + ",\"websocket_session\":\""
							+ websocket_token + "\"}";
		}
		String msg = "{\"name\":\"authenticate\",\"args\":[" + emitargs + "]}";
		send(5, msg);
		return true;
	}

	public void send(int type, String msg) {
		String message;
		message = Integer.toString(type) + "::" + END_POINT + ":" + msg;
		mWs.send(message);
	}

	public void parseMessage(String message) {
		String[] msg = message.split("::");
		if (msg.length == 0)
			return;
		if (msg[0].contentEquals("1")) {
			if (msg.length == 1) {
				send(1, ""); // Emit reply
			} else {
				connected = true;
			}
			return;
		}
		if (msg[0].contentEquals("5") && msg.length > 1) {
			String msg5 = msg[1].replace(END_POINT + ":", "");
			;
			if (listener != null) {
				listener.onMessage(msg5);
			}
		}

		if (msg[0].contentEquals("2")) {
			logger.info("Keep alive message: {}", message);
		}

	}
}
