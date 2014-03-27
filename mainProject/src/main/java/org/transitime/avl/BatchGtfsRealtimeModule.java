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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.core.DataProcessor;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.Location;
import org.transitime.modules.Module;
import org.transitime.utils.Time;

/**
 * For reading in a batch of GTFS-realtime data and processing it. It only
 * reads a single batch of data, unlike the usual AVL modules that continuously
 * read data. This module was created for the World Bank project so that 
 * could determine actual arrival times based on batched GPS data and then
 * output more accurate schedule times for the GTFS stop_times.txt file.
 * <p>
 * Note: the URL for the GTFS-realtime feed is obtained in GtfsRealtimeModule
 * from CoreConfig.getGtfsRealtimeURI(). This means it can be set in the
 * config file or as a Java property on the command line.
 *  
 * @author SkiBu Smith
 *
 */
public class BatchGtfsRealtimeModule extends Module {

	private static final Logger logger = 
			LoggerFactory.getLogger(BatchGtfsRealtimeModule.class);

	/********************** Member Functions **************************/

	/**
	 * @param projectId
	 */
	public BatchGtfsRealtimeModule(String projectId) {
		super(projectId);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		List<AvlReport> avlReports = GtfsRealtimeModule.getAvlReports();
		
		// FIXME just for debugging  bad data for Zhengzhou
		logger.info("The following AVL reports are for within 15km of Zhengzhou");
		List<AvlReport> zhengzhouAvlReports = new ArrayList<AvlReport>();
		Set<String> zhengzhouVehicles = new HashSet<String>();
		Set<String> zhengzhouRoutes = new HashSet<String>();
		for (AvlReport avlReport : avlReports) {
			if (avlReport.getLocation().distance(new Location(34.75, 113.65)) < 15000) {
				logger.info("Zhengzhou avlReport={}", avlReport);
				zhengzhouAvlReports.add(avlReport);
				
				zhengzhouVehicles.add(avlReport.getVehicleId());
				zhengzhouRoutes.add(avlReport.getAssignmentId());
			}
		}
		logger.info("For Zhengzhou got {} AVl reports out of total of {}.",
				zhengzhouAvlReports.size(), avlReports.size());
		logger.info("For Zhengzhou found {} vehicles={}", 
				zhengzhouVehicles.size(), zhengzhouVehicles);
		logger.info("For Zhengzhou found {} routes={}", 
				zhengzhouRoutes.size(), zhengzhouRoutes);
		avlReports = zhengzhouAvlReports;
		
		// Process the AVL Reports read in.
		for (AvlReport avlReport : avlReports) {
			logger.debug("Processing avlReport={}", avlReport);
			DataProcessor.getInstance().processAvlReport(avlReport);
		}
		
		// Done processing the batch data. Wait a bit more to make sure system
		// has chance to log all data to the database. Then exit.
		Time.sleep(5000);
		System.exit(0);
	}

}
