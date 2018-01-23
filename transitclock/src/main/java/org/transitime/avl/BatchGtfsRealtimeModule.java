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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.StringListConfigValue;
import org.transitime.core.AvlProcessor;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.Location;
import org.transitime.feed.gtfsRt.GtfsRtVehiclePositionsReader;
import org.transitime.modules.Module;
import org.transitime.utils.Time;

/**
 * For reading in a batch of GTFS-realtime data and processing it. It only
 * reads a single batch of data, unlike the usual AVL modules that continuously
 * read data. This module was created for the World Bank project so that 
 * could determine actual arrival times based on batched GPS data and then
 * output more accurate schedule times for the GTFS stop_times.txt file.
 * <p>
 * The AVL data is processed directly by this class by it calling
 * AvlProcessor.processAvlReport(avlReport). The messages do not go through
 * the JMS server and JMS server does not need to be running.
 * <p>
 * Note: the URL for the GTFS-realtime feed is obtained in GtfsRealtimeModule
 * from CoreConfig.getGtfsRealtimeURI(). This means it can be set in the
 * config file or as a Java property on the command line.
 *  
 * @author SkiBu Smith
 *
 */
public class BatchGtfsRealtimeModule extends Module {

	/*********** Logging *******************************************/
	
	private static final Logger logger = 
			LoggerFactory.getLogger(BatchGtfsRealtimeModule.class);

	/*********** Configurable Parameters for this module ***********/
	
	public static List<String> getGtfsRealtimeURIs() {
		return gtfsRealtimeURIs.getValue();
	}
	private static StringListConfigValue gtfsRealtimeURIs =
			new StringListConfigValue("transitime.avl.gtfsRealtimeFeedURIs",
					null,
					"Semicolon separated list of URIs of the GTFS-realtime data to read in");
	
	/********************** Member Functions **************************/

	/**
	 * @param agencyId
	 */
	public BatchGtfsRealtimeModule(String agencyId) {
		super(agencyId);
	}

	/**
	 * Zhengzhou had trouble providing valid GTFS-RT data. So this method can be
	 * used for Zhengzhou to add logging info so that can see how many valid
	 * reports there are. Also filters out AVL reports that are not within 15km
	 * of downtown Zhengzhou. It is only for debugging.
	 */
	private void debugZhengzhou() {
		for (String uri : getGtfsRealtimeURIs()) {
			Collection<AvlReport> avlReports =
					GtfsRtVehiclePositionsReader.getAvlReports(uri);
	
			// Location of downtown Zhengzhou
			Location downtown = new Location(34.75, 113.65);
			
			//logger.info("The following AVL reports are within 15km of Zhengzhou");
			List<AvlReport> zhengzhouAvlReports = new ArrayList<AvlReport>();
			Set<String> zhengzhouVehicles = new HashSet<String>();
			Set<String> zhengzhouRoutes = new HashSet<String>();
			long earliestTime = Long.MAX_VALUE;
			long latestTime = Long.MIN_VALUE;
			for (AvlReport avlReport : avlReports) {
				if (avlReport.getLocation().distance(downtown) < 15000) {
					//logger.info("Zhengzhou avlReport={}", avlReport);
					zhengzhouAvlReports.add(avlReport);
					
					zhengzhouVehicles.add(avlReport.getVehicleId());
					zhengzhouRoutes.add(avlReport.getAssignmentId());
					
					if (avlReport.getTime() < earliestTime)
						earliestTime = avlReport.getTime();
					if (avlReport.getTime() > latestTime)
						latestTime = avlReport.getTime();
				}
			}
			logger.info("For Zhengzhou got {} AVl reports out of total of {}.",
					zhengzhouAvlReports.size(), avlReports.size());
			logger.info("For Zhengzhou found {} vehicles={}", 
					zhengzhouVehicles.size(), zhengzhouVehicles);
			logger.info("For Zhengzhou found {} routes={}", 
					zhengzhouRoutes.size(), zhengzhouRoutes);
			logger.info("Earliest AVL time was {} and latest was {}",
					new Date(earliestTime), new Date(latestTime));
		}

	}
	
	/* 
	 * Reads in AVL reports from GTFS-realtime file and processes them.
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// Zhengzhou had trouble providing valid GPS reports so this method
		// can be used to log debugging info and to filter out reports that
		// are not actually in Zhengzhou
		if (agencyId.equals("zhengzhouXX")) {
			debugZhengzhou();
		}
		
		// Process the VehiclePosition reports from the GTFS-realtime files
		for (String uri : getGtfsRealtimeURIs()) {
			Collection<AvlReport> avlReports =
					GtfsRtVehiclePositionsReader.getAvlReports(uri);
			for (AvlReport avlReport : avlReports) {
				// Update the Core SystemTime to use this AVL time
				Core.getInstance().setSystemTime(avlReport.getTime());

				// Actually process the AvlReport
				AvlProcessor.getInstance().processAvlReport(avlReport);
			}
		}
		
		// Done processing the batch data. Wait a bit more to make sure system
		// has chance to log all data to the database. Then exit.
		Time.sleep(5000);
		System.exit(0);
	}

}
