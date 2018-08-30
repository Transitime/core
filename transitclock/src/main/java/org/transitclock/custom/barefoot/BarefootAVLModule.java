/* 
 * This file is part of thetransitclock.org
 * 
 * thetransitclock.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * thetransitclock.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with thetransitclock.org .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitclock.custom.barefoot;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import org.transitclock.avl.NextBusBarefootAvlModule;
import org.transitclock.avl.PollUrlAvlModule;
import org.transitclock.config.DoubleConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.structs.AvlReport;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.Location;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.WktImportFlags;
import com.esri.core.geometry.Geometry.Type;

/**
 * @author Sean Óg Crudden
 * 
 *         This module subscribes to a barefoot server to get map matched GPS
 *         data.
 * 
 *
 */
public class BarefootAVLModule extends PollUrlAvlModule {

	private static IntegerConfigValue barefootPort = new IntegerConfigValue("transitclock.avl.barefoot.subscribe.port",
			1235, "This is the port of the barefoot server to subscribe to.");

	private static StringConfigValue barefootServer = new StringConfigValue(
			"transitclock.avl.barefoot.subscribe.server", "127.0.0.1",
			"This is the server that is running the barefoot map matching service.");

	private static DoubleConfigValue minProbability = new DoubleConfigValue(
			"transitclock.avl.barefoot.subscribe.minprobability", 0.1,
			"Only use values that have a probability of being correct higher than this.");

	private static final Logger logger = LoggerFactory.getLogger(BarefootAVLModule.class);

	private static HashMap<String, Point> locations = new HashMap<String, Point>();

	public BarefootAVLModule(String agencyId) {
		super(agencyId);

	}

	@Override
	protected void getAndProcessData() throws Exception {

		// Prepare our context and subscriber

		Context context = ZMQ.context(1);

		Socket subscriber = context.socket(ZMQ.SUB);

		subscriber.connect("tcp://" + barefootServer.getValue() + ":" + barefootPort.getValue());

		subscriber.subscribe("".getBytes());

		while (!Thread.currentThread().isInterrupted()) {

			String contents = subscriber.recvStr();
			logger.debug("Got content from barefoot : " + contents);

			JSONObject update = new JSONObject(contents);

			String vehicleId = update.getString("id");

			Long time = update.getLong("time");
			Location location = getAdjustedLocation(vehicleId, update);
			AvlReport avlReport = null;
			if (location != null) {
				avlReport = new AvlReport(vehicleId, time, location.getLat(), location.getLon(), "Barefoot");
				logger.debug("AVL from barefoot to be processed : " + avlReport);
				processAvlReport(avlReport);
			}
		}
		subscriber.close();
		context.term();
	}

	@Override
	protected Collection<AvlReport> processData(InputStream in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	private Location getAdjustedLocation(String vehicleId, JSONObject state) {

		Location result = null;
		try {

			JSONArray candidatesArray = state.getJSONArray("candidates");

			Double maxProbability = 0.0;

			for (int i = 0; i < candidatesArray.length(); i++) {
				double probabililty = candidatesArray.getJSONObject(i).getDouble("prob");

				String geoPoint = candidatesArray.getJSONObject(i).getString("point");

				Point point = (Point) GeometryEngine.geometryFromWkt(geoPoint, WktImportFlags.wktImportDefaults,
						Type.Point);

				// Only consider new or modified GPS values
				if ((locations.get(vehicleId) != null && !locations.get(vehicleId).equals(point))
						|| locations.get(vehicleId) == null) {
					if (probabililty > minProbability.getValue()) {

						maxProbability = probabililty;

						result = new Location(point.getY(), point.getX());

						locations.put(vehicleId, point);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Failed to adjust location with barefoot. Reason: " + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	protected double bearing(double lat1, double lon1, double lat2, double lon2) {
		double longitude1 = lon1;
		double longitude2 = lon2;
		double latitude1 = Math.toRadians(lat1);
		double latitude2 = Math.toRadians(lat2);
		double longDiff = Math.toRadians(longitude2 - longitude1);
		double y = Math.sin(longDiff) * Math.cos(latitude2);
		double x = Math.cos(latitude1) * Math.sin(latitude2)
				- Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);

		return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
	}
}
