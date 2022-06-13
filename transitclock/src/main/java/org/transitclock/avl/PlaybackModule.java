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
package org.transitclock.avl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.core.AvlProcessor;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.modules.Module;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.Time;

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
	protected long dbReadBeginTime;
		
	/*********** Configurable Parameters for this module ***********/
	private static String getPlaybackVehicleId() {
		return playbackVehicleId.getValue();
	}
	private static StringConfigValue playbackVehicleId =
			new StringConfigValue("transitclock.avl.playbackVehicleId", 
					"",
					"ID of vehicle to playback.");

	private static String getPlaybackStartTimeStr() {
		return playbackStartTimeStr.getValue();
	}
	protected static StringConfigValue playbackStartTimeStr =
			new StringConfigValue("transitclock.avl.playbackStartTime", 
					"",
					"Date and time of when to start the playback.");
	
	protected static StringConfigValue playbackEndTimeStr =
			new StringConfigValue("transitclock.avl.playbackEndTime", 
					"",
					"Date and time of when to end the playback.");

	protected static BooleanConfigValue playbackRealtime =
			new BooleanConfigValue("transitclock.avl.playbackRealtime", 
					false,
					"Playback at normal time speed rather than as fast as possible.");
	
	protected static IntegerConfigValue playbackSkipIntervalMinutes =
			new IntegerConfigValue("transitclock.avl.playbackSkipIntervalMinutes", 
					120,
					"If no data for this amount of minutes skip forward in time.");
	
	protected static IntegerConfigValue playbackStartDelayMinutes=
			new IntegerConfigValue("transitclock.avl.playbackStartDelayMinutes", 
					3,
					"Time to sleep before starting. Gives some time to connect remote debugger.");
	
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
		if (getPlaybackStartTimeStr() == null
				|| getPlaybackStartTimeStr().isEmpty()) {
			logger.warn("Parameters not set. See log file for details.");			
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
						"transitclock.avl.playbackStartTime parameter is in " +
						"the future and therefore invalid!",
						playbackStartTimeStr);
				System.exit(-1);					
			}
				
			return playbackStartTime;
		} catch (java.text.ParseException e) {
			logger.error("Paramater -t \"{}\" specified by " +
					"transitclock.avl.playbackStartTime parameter could not " +
					"be parsed. Format must be \"MM-dd-yyyy HH:mm:ss\"",
					playbackStartTimeStr);
			System.exit(-1);
			
			// Will never be reached because the above state exits program but
			// needed so compiler doesn't complain.
			return -1;
		}
	}
	protected static long parsePlaybackEndTime(String playbackEndTimeStr) {
		try {
			long playbackEndTime = Time.parse(playbackEndTimeStr).getTime();
			
			// If specified time is in the future then reject.
			if (playbackEndTime > System.currentTimeMillis()) {
				logger.error("Playback end time \"{}\" specified by " +
						"transitclock.avl.playbackEndTime parameter is in " +
						"the future and therefore invalid!",
						playbackEndTimeStr);
				System.exit(-1);					
			}
				
			return playbackEndTime;
		} catch (java.text.ParseException e) {
			logger.error("Paramater -t \"{}\" specified by " +
					"transitclock.avl.playbackEndTime parameter could not " +
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
	protected List<AvlReport> getBatchOfAvlReportsFromDb() {
		// The times that should be reading data for
		long start = dbReadBeginTime;
		long end = dbReadBeginTime + DB_POLLING_TIME_MSEC;

		List<AvlReport> avlReports = null;
		if (playbackVehicleId.getValue() == null
				|| playbackVehicleId.getValue().length() == 0) {
			logger.info("PlaybackModule getting batch of AVLReports for " +
											"between beginTime={} and endTime={} " +
											"and vehicleId={}",
							Time.dateTimeStr(start),
							Time.dateTimeStr(end),
							playbackVehicleId);
			avlReports =
							AvlReport.getAvlReportsFromDb(
											new Date(start),
											new Date(end),
											getPlaybackVehicleId(),
											"ORDER BY time");

		}	else {
			logger.info("PlaybackModule getting batch of AVLReports for " +
											"between beginTime={} and endTime={} " +
											"and no vehicle specified",
							Time.dateTimeStr(start),
							Time.dateTimeStr(end),
							playbackVehicleId);
			avlReports = AvlReport.getAvlReportsFromDb(new Date(start),
							new Date(end),
							null,
							"ORDER BY time");
		}


		if (avlReports == null) {
			logger.error("No AVLReports were read -- something went wrong");
		} else {
			logger.info("PlaybackModule read {} AVLReports.", avlReports.size());
		}

		// For next time this method is called.
		dbReadBeginTime = end;
		
		// Return results
		return avlReports;
	}
	
	void sleepInIncrements(long clocktime, long sleep, long increment) throws InterruptedException
	{
		long counter=0;
		
		while(counter<sleep)
		{
			Thread.sleep(increment);
			counter=counter+increment;
			clocktime=clocktime+increment;
			Core.getInstance().setSystemTime(clocktime);
		}
	}
	
	/* Reads AVL data from db and processes it
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		try {
			Thread.sleep(playbackStartDelayMinutes.getValue()*Time.MS_PER_MIN);
		} catch (InterruptedException e) {
		
		}
		
		IntervalTimer timer = new IntervalTimer();
		// Keep running as long as not trying to access in the future.
		long last_avl_time=-1;	
		while (dbReadBeginTime < System.currentTimeMillis() && (playbackEndTimeStr.getValue().length()==0 || dbReadBeginTime<parsePlaybackEndTime(playbackEndTimeStr.getValue()))) {
			List<AvlReport> avlReports = getBatchOfAvlReportsFromDb();
			
			// Process the AVL Reports read in.			
			for (AvlReport avlReport : avlReports) {
																						        
				if(playbackRealtime.getValue()==true)
				{
					if(last_avl_time>-1)
					{
						try {
							// only sleep if values less than playbackSkipIntervalMinutes minutes. This is to allow it skip days/hours of missing data.
							if((avlReport.getTime()-last_avl_time) < (playbackSkipIntervalMinutes.getValue()*Time.MS_PER_MIN))
							{								
								//Thread.sleep(avlReport.getTime()-last_avl_time);
								sleepInIncrements(Core.getInstance().getSystemTime(), avlReport.getTime()-last_avl_time, 10*Time.MS_PER_SEC);
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
				logger.info("Processing avlReport={}", avlReport);
				
				// Update the Core SystemTime to use this AVL time
				Core.getInstance().setSystemTime(avlReport.getTime());
				
				// Do the actual processing of the AVL data
				AvlProcessor.getInstance().processAvlReport(avlReport);						
			
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
	
}
