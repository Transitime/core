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

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.core.DataProcessor;
import org.transitime.db.structs.AvlReport;
import org.transitime.modules.Module;
import org.transitime.utils.SettableSystemTime;
import org.transitime.utils.SystemTime;
import org.transitime.utils.Time;

/**
 * For running the system in "playback mode" where AVL data is read from
 * the database instead of from a realtime AVL feed. Useful for debugging
 * the system software because can easily debug what is happening for a 
 * particular vehicle at a particular time.
 * <p>
 * This module is different from others because it is not intended to be
 * configured to run as an optional module via CoreConfig.getOptionalModules().
 * Instead, it is called explicitly. This is necessary since the constructor
 * has different parameters from most modules, which just have a projectId
 * parameter.
 * 
 * @author SkiBu Smith
 *
 */
public class PlaybackModule extends Module {

	// Get 5 minutes worth of AVL data at a time
	private final static long DB_POLLING_TIME_MSEC = 5 * Time.MS_PER_MIN;
	
	// For keeping track of beginning of timespan for doing query
	private long dbReadBeginTime;
	
	// Time when to start getting AVL data
	private final long playbackStartTime;
	
	// Vehicle to get AVL data data for. If null then get data for all vehicles.
	private final String playbackVehicleId;
	
	private final SettableSystemTime playbackSystemTime = 
			new SettableSystemTime();
	
	private static final Logger logger = 
			LoggerFactory.getLogger(PlaybackModule.class);


	/********************** Member Functions **************************/

	/**
	 * @param projectId
	 */
	public PlaybackModule(String projectId, 
			Date playbackStartTime, 
			String playbackVehicleId) {
		super(projectId);
		
		this.playbackStartTime = playbackStartTime.getTime();
		this.playbackVehicleId = playbackVehicleId;
		
		this.dbReadBeginTime = this.playbackStartTime;
	}
	
	/**
	 * Returns SystemTime object that uses the time of the last AVL report.
	 * @return
	 */
	public SystemTime getSystemTime() {
		return playbackSystemTime;
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
				AvlReport.getAvlReportsFromDb(getProjectId(),
						new Date(start), 
						new Date(end), 
						playbackVehicleId);
		
		logger.info("PlaybackModule read {} AVLReports.", avlReports.size());

		// For next time this method is called
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
		while (dbReadBeginTime < System.currentTimeMillis()) {
			List<AvlReport> avlReports = getBatchOfAvlReportsFromDb();
			
			// Process the AVL Reports read in.
			for (AvlReport avlReport : avlReports) {
				logger.debug("Processing avlReport={}", avlReport);
				
				// Update the SystemTime object to use this AVL time
				playbackSystemTime.set(avlReport.getTime());
				
				// Do the actual processing of the AVL data
				DataProcessor.getInstance().processAvlReport(avlReport);
			}
		}
		
		logger.info("Read in AVL in playback mode all the way up to current " +
				"time so done. Exiting.");
		System.exit(0);
		
	}
	
}
