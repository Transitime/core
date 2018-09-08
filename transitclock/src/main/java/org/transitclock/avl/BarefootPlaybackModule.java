package org.transitclock.avl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.Time;

import com.esri.core.geometry.Point;

public class BarefootPlaybackModule extends PlaybackModule {
	private static final Logger logger = 
			LoggerFactory.getLogger(PlaybackModule.class);

	public BarefootPlaybackModule(String agencyId) {
		super(agencyId);
	
	}

	@Override
	public void run() {

		IntervalTimer timer = new IntervalTimer();
		// Keep running as long as not trying to access in the future.
				
		while (dbReadBeginTime < System.currentTimeMillis() && (playbackEndTimeStr.getValue().length()==0 || dbReadBeginTime<parsePlaybackEndTime(playbackEndTimeStr.getValue()))) {
			List<AvlReport> avlReports = getBatchOfAvlReportsFromDb();
			
			// Process the AVL Reports read in.
			long last_avl_time=-1;
			for (AvlReport avlReport : avlReports) {
				logger.info("Processing avlReport={}", avlReport);
				
				// Update the Core SystemTime to use this AVL time
				Core.getInstance().setSystemTime(avlReport.getTime());
				
				DateFormat estFormat = new SimpleDateFormat("yyyyMMddHHmmss");				
		        TimeZone estTime = TimeZone.getTimeZone("EST");
		        estFormat.setTimeZone(estTime);
		        
		        TimeZone gmtTime = TimeZone.getTimeZone("GMT");		        
		        DateFormat gmtFormat =  new SimpleDateFormat("yyyyMMddHHmmss");
		        gmtFormat.setTimeZone(gmtTime);
		        		    		        		       
		        String estDate=estFormat.format(avlReport.getTime());
		        String gmtDate=gmtFormat.format(avlReport.getTime());		        
		        		     
		        
				if(playbackRealtime.getValue()==true)
				{
					if(last_avl_time>-1)
					{
						try {
							// only sleep if values less than 10 minutes. This is to allow it skip days/hours of missing data.
							if((avlReport.getTime()-last_avl_time)<600000)
							{								
								Thread.sleep(avlReport.getTime()-last_avl_time);
							}
							last_avl_time=avlReport.getTime();
						} catch (InterruptedException e) {							
							e.printStackTrace();
						}
					}else
					{
						last_avl_time=avlReport.getTime();
					}
				}				
				// Send avl to barefoot server.
				sendUpdate(avlReport);						
			
			}
		}
		// logging here as the rest is database access dependent.
		logger.info("Processed AVL from playbackStartTimeStr:{} to playbackEndTimeStr:{} in {} secs.",playbackStartTimeStr,playbackEndTimeStr,  Time.secondsStr(timer.elapsedMsec()));
		
		
		// Wait for database queue to be emptied before exiting.
		while(Core.getInstance().getDbLogger().queueSize()>0)
		{
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			
			}
		}
		
		
 		logger.info("Read in AVL in playback mode all the way up to current " +
				"time so done. Exiting.");
		
		System.exit(0);	
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
