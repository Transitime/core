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
package org.transitclock.custom.traccar;

import java.util.Collection;

import org.transitclock.db.structs.AvlReport;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import com.esri.core.geometry.Point;
import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;

/**
 * @author Sean Óg Crudden
 * 
 *         This module takes data from traccar and forwards it to a barefoot
 *         server instances to have it map matched to a GTFS route.
 * 
 *         The BarefootAVLModule should be configured with this module to read
 *         the adjusted AVL from the barefoot server for processing by
 *         TheTransitClock.
 */
public class TraccarBarefootAVLModule extends TraccarAVLModule {
	private static final Logger logger = LoggerFactory.getLogger(TraccarBarefootAVLModule.class);

	private static IntegerConfigValue barefootPort = new IntegerConfigValue("transitclock.avl.barefoot.port", 1235,
			"This is the port of the barefoot server to send samples to.");

	private static StringConfigValue barefootServer = new StringConfigValue("transitclock.avl.barefoot.server",
			"127.0.0.1", "This is the server that is running the barefoot service.");

	public TraccarBarefootAVLModule(String agencyId) throws Throwable {
		super(agencyId);
	}
	@Override	
	protected void forwardAvlReports(Collection<AvlReport> avlReportsReadIn) {
		for(AvlReport avlReport:avlReportsReadIn)
		{
			sendUpdate(avlReport);
		}
	}

	public void sendUpdate(AvlReport avlReport) {
		try {
			JSONObject report = new JSONObject();

			InetAddress host = InetAddress.getByName(barefootServer.getValue());

			report.put("id", avlReport.getVehicleId());

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

			report.put("time", df.format(avlReport.getDate()));

			Point point = new Point();
			point.setX(avlReport.getLon());
			point.setY(avlReport.getLat());

			report.put("point", "POINT(" + avlReport.getLon() + " " + avlReport.getLat() + ")");
			// report.put("point", GeometryEngine.geometryToGeoJson(point));

			sendBareFootSample(host, barefootPort.getValue(), report);

		} catch (Exception e) {
			logger.error("Problem when sending samples to barefoot.", e);
		}
	}

	private void sendBareFootSample(InetAddress host, int port, JSONObject sample) throws Exception {
		int trials = 120;
		int timeout = 500;
		Socket client = null;
		// TODO Will need to leave socket open.
		while (client == null || !client.isConnected()) {
			try {
				client = new Socket(host, port);
			} catch (IOException e) {
				Thread.sleep(timeout);

				if (trials == 0) {
					logger.error(e.getMessage());
					client.close();
					throw new IOException();
				} else {
					trials -= 1;
				}
			}
		}
		PrintWriter writer = new PrintWriter(client.getOutputStream());
		BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		writer.println(sample.toString());
		writer.flush();

		String code = reader.readLine();
		if (!code.equals("SUCCESS")) {
			throw new Exception("Barefoot server did not respond with SUCCESS");
		}
	}
}
