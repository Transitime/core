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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.BooleanConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.core.AvlProcessor;
import org.transitime.db.structs.AvlReport;
import org.transitime.modules.Module;
import org.transitime.utils.Time;

/**
 * For running the system in "playback mode" where AVL data is read from the
 * database instead of from a realtime AVL feed. Useful for debugging the system
 * software because can easily debug what is happening for a particular vehicle
 * at a particular time.
 * 
 * @author SkiBu Smith
 *
 */
public class PlaybackModule extends Module {

	// Get 5 minutes worth of AVL data at a time
	private final static long DB_POLLING_TIME_MSEC = 5 * Time.MS_PER_MIN;
	
	// For keeping track of beginning of timespan for doing query
	private long dbReadBeginTime;
		
	/*********** Configurable Parameters for this module ***********/
	private static String getPlaybackVehicleId() {
		return playbackVehicleId.getValue();
	}
	private static StringConfigValue playbackVehicleId =
			new StringConfigValue("transitime.avl.playbackVehicleId", 
					"",
					"ID of vehicle to playback.");

	private static String getPlaybackStartTimeStr() {
		return playbackStartTimeStr.getValue();
	}
	private static StringConfigValue playbackStartTimeStr =
			new StringConfigValue("transitime.avl.playbackStartTime", 
					"",
					"Date and time of when to start the playback.");
	
	private static StringConfigValue playbackEndTimeStr =
			new StringConfigValue("transitime.avl.playbackEndTime", 
					"",
					"Date and time of when to end the playback.");

	private static BooleanConfigValue playbackRealtime =
			new BooleanConfigValue("transitime.avl.playbackRealtime", 
					false,
					"Playback at normal time speed rather than as fast as possible.");
	/********************* Logging **************************/
	private static final Logger logger = 
			LoggerFactory.getLogger(PlaybackModule.class);


	/********************** Member Functions **************************/

	/**
	 * @param agencyId
	 */
	public PlaybackModule(String agencyId) {
		super(agencyId);
		
		// Make sure params are set
		if (getPlaybackVehicleId() == null 
				|| getPlaybackVehicleId().isEmpty()
				|| getPlaybackStartTimeStr() == null
				|| getPlaybackStartTimeStr().isEmpty()) {
			logger.warn("Parameters not set. See log file for details. Exiting.");			
		}
		
		// Initialize the dbReadBeingTime member
		this.dbReadBeginTime = parsePlaybackStartTime(getPlaybackStartTimeStr());
	}
	
	private static long parsePlaybackStartTime(String playbackStartTimeStr) {
		try {
			long playbackStartTime = Time.parse(playbackStartTimeStr).getTime();
			
			// If specified time is in the future then reject.
			if (playbackStartTime > System.currentTimeMillis()) {
				logger.error("Playback start time \"{}\" specified by " +
						"transitime.avl.playbackStartTime parameter is in " +
						"the future and therefore invalid!",
						playbackStartTimeStr);
				System.exit(-1);					
			}
				
			return playbackStartTime;
		} catch (java.text.ParseException e) {
			logger.error("Paramater -t \"{}\" specified by " +
					"transitime.avl.playbackStartTime parameter could not " +
					"be parsed. Format must be \"MM-dd-yyyy HH:mm:ss\"",
					playbackStartTimeStr);
			System.exit(-1);
			
			// Will never be reached because the above state exits program but
			// needed so compiler doesn't complain.
			return -1;
		}
	}
	private static long parsePlaybackEndTime(String playbackEndTimeStr) {
		try {
			long playbackEndTime = Time.parse(playbackEndTimeStr).getTime();
			
			// If specified time is in the future then reject.
			if (playbackEndTime > System.currentTimeMillis()) {
				logger.error("Playback end time \"{}\" specified by " +
						"transitime.avl.playbackEndTime parameter is in " +
						"the future and therefore invalid!",
						playbackEndTimeStr);
				System.exit(-1);					
			}
				
			return playbackEndTime;
		} catch (java.text.ParseException e) {
			logger.error("Paramater -t \"{}\" specified by " +
					"transitime.avl.playbackEndTime parameter could not " +
					"be parsed. Format must be \"MM-dd-yyyy HH:mm:ss\"",
					playbackEndTimeStr);
			System.exit(-1);
			
			// Will never be reached because the above state exits program but
			// needed so compiler doesn't complain.
			return -1;
		}
	}
	/**
	 * Gets a batch of AVl data from the database
	 * @return
	 */
	private List<AvlReport> getBatchOfAvlReportsFromDb() {
		// The times that should be reading data for
		long start = dbReadBeginTime;
		long end = dbReadBeginTime + DB_POLLING_TIME_MSEC;
				
		logger.info("PlaybackModule getting batch of AVLReports for " +
				"between beginTime={} and endTime={} " +
				"and vehicleId={}", 
				Time.dateTimeStr(start),
				Time.dateTimeStr(end),
				playbackVehicleId);
		
		List<AvlReport> avlReports = 
				AvlReport.getAvlReportsFromDb(
						new Date(start), 
						new Date(end), 
						getPlaybackVehicleId(),
						"ORDER BY time");
		
		logger.info("PlaybackModule read {} AVLReports.", avlReports.size());

		// For next time this method is called.
		dbReadBeginTime = end;
		
		// Return results
		return avlReports;
	}
	
	/* Reads AVL data from db and processes it
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
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
				// Do the actual processing of the AVL data
				AvlProcessor.getInstance().processAvlReport(avlReport);						
			
			}
		}
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
	
}
