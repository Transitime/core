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

import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.StringConfigValue;
import org.transitclock.core.blockAssigner.BlockAssigner;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Trip;
import org.transitclock.feed.gtfsRt.GtfsRtVehiclePositionsReader;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.modules.Module;
import org.transitclock.monitoring.MonitoringService;

import java.util.Collection;

/**
 * For reading in feed of GTFS-realtime AVL data. Is used for both realtime
 * feeds and for when reading in a giant batch of data.
 * 
 * @author SkiBu Smith
 * 
 */
public class GtfsRealtimeModule extends PollUrlAvlModule {



	// If debugging feed and want to not actually process
	// AVL reports to generate predictions and such then
	// set shouldProcessAvl to false;
	protected static boolean shouldProcessAvl = true;


  private static final Logger logger = LoggerFactory
      .getLogger(GtfsRealtimeModule.class);
  

	/*********** Configurable Parameters for this module ***********/
	public static String getGtfsRealtimeURI() {
		return gtfsRealtimeURI.getValue();
	}
	private static StringConfigValue gtfsRealtimeURI =
			new StringConfigValue("transitclock.avl.gtfsRealtimeFeedURI", 
					"file:///C:/Users/Mike/gtfsRealtimeData",
					"The URI of the GTFS-realtime feed to use.");

	/********************** Member Functions **************************/

	/**
	 * @param projectId
	 */
	public GtfsRealtimeModule(String projectId) {
		super(projectId);
    // GTFS-realtime is already binary so don't want to get compressed
    // version since that would just be a waste.
    useCompression = false;
	}

	/**
	 * Reads and processes the data. Called by AvlModule.run().
	 * Reading GTFS-realtime doesn't use InputSteram so overriding
	 * getAndProcessData().
	 */
	@Override
	protected void getAndProcessData() {
	  
	  String[] urls = getGtfsRealtimeURI().split(",");

		int assignments = 0;
		int records = 0;
	  for (String urlStr : urls) {
  	  try {
    	  logger.info("reading {}", urlStr);
    		List<AvlReport> avlReports = GtfsRtVehiclePositionsReader
    				.getAvlReports(urlStr);
    		logger.info("read complete");
    		for (AvlReport avlReport : avlReports) {
    			initalizeAvlReport(avlReport);
    			processAvlReport(avlReport);
    			records++;
    			if (avlReport.getAssignmentId() != null)
						assignments++;
    		}
    		logger.info("processed {} reports for feed {}", avlReports.size(), urlStr);
  	  } catch (Exception any) {
  	    logger.error("issues processing feed {}:{}", urlStr, any, any);
  	  }
  		
	  }
	  MonitoringService.getInstance().averageMetric("PredictionAvlInputRecords", records);
		MonitoringService.getInstance().averageMetric("PredictionAvlInputAssignments", assignments);
		
	}

	// ensure hibernate lazy loading is complete before moving object to another thread
	private void initalizeAvlReport(AvlReport avlReport) {
		AvlReport.AssignmentType assignmentType = avlReport.getAssignmentType();
		if (assignmentType == AvlReport.AssignmentType.TRIP_ID) {
			DbConfig dbConfig = Core.getInstance().getDbConfig();
			Trip trip = getTrip(dbConfig, avlReport.getAssignmentId());
							getTripWithServiceIdSuffix(dbConfig, avlReport.getAssignmentId());
			if (trip == null) return;
			Block block = trip.getBlock();
			if (block != null)
				block.initialize();
		}
	}

	private Trip getTrip(DbConfig config, String assignmentId) {
		if (config.getServiceIdSuffix()) {
			return getTripWithServiceIdSuffix(config, assignmentId);
		}
		return config.getTrip(assignmentId);
	}

	private Trip getTripWithServiceIdSuffix(DbConfig config, String assignmentId) {
		return BlockAssigner.getInstance().getTripWithServiceIdSuffix(config, assignmentId);
	}

	/* (non-Javadoc)
	 * @see org.transitclock.avl.AvlModule#processData(java.io.InputStream)
	 */
	@Override
	protected Collection<AvlReport> processData(InputStream inputStream)
			throws Exception {
	  return null; // we've overriden getAndProcessData so this need not do anything
	}

	/**
	 * Just for debugging
	 */
	public static void main(String[] args) {
		// Create a GtfsRealtimeModule for testing
		Module.start("org.transitclock.avl.GtfsRealtimeModule");
	}

}
