package org.transitclock.avl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Collection;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.AvlReport;

import com.esri.core.geometry.Point;

public class NextBusBarefootAvlModule extends NextBusAvlModule {
	private static final Logger logger = LoggerFactory
			.getLogger(NextBusBarefootAvlModule.class);
	public NextBusBarefootAvlModule(String agencyId) {
		super(agencyId);		
	}

	@Override
	protected void processAvlReports(Collection<AvlReport> avlReports) {
		forwardAvlReports(avlReports);
	}
	
	
	protected void forwardAvlReports(Collection<AvlReport> avlReportsReadIn) {
		for(AvlReport avlReport:avlReportsReadIn)
		{
			sendUpdate(avlReport);
		}
	}
	
	public void sendUpdate(AvlReport avlReport) {
		try {
			JSONObject report = new JSONObject();

			InetAddress host = InetAddress.getLocalHost();

			int port = 1234;

			report.put("id", avlReport.getVehicleId());

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

			report.put("time", df.format(avlReport.getDate()));

			Point point = new Point();
			point.setX(avlReport.getLon());
			point.setY(avlReport.getLat());

			report.put("point", "POINT(" + avlReport.getLon() + " " + avlReport.getLat() + ")");
			// report.put("point", GeometryEngine.geometryToGeoJson(point));

			sendBareFootSample(host, port, report);

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
			throw new Exception("Barefoot server did not respond with SUCCESS. Code="+code);
		}
	}
}
