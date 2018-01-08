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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.BooleanConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.core.AvlProcessor;
import org.transitime.db.structs.AvlReport;
import org.transitime.modules.Module;
import org.transitime.utils.Time;

import java.util.List;

/**
 * For reading in a batch of AVL data in CSV format and processing it. It only
 * reads a single batch of data, unlike the usual AVL modules that continuously
 * read data. This module is useful for debugging because can relatively easily
 * create a plain text CSV file of AVL data and see what the code does.
 * <p>
 * CSV columns include vehicleId, time (in epoch msec or as date string as in
 * "9-14-2015 12:53:01"), latitude, longitude, speed (optional), heading
 * (optional), assignmentId, and assignmentType (optional, but can be BLOCK_ID,
 * ROUTE_ID, TRIP_ID, or TRIP_SHORT_NAME).
 *
 * @author SkiBu Smith
 *
 */
public class BatchCsvAvlFeedModule extends Module {

	// For running in real time
	private long lastAvlReportTimestamp = -1;
			
	/*********** Configurable Parameters for this module ***********/
	private static String getCsvAvlFeedFileName() {
		return csvAvlFeedFileName.getValue();
	}
	private static StringConfigValue csvAvlFeedFileName =
			new StringConfigValue("transitime.avl.csvAvlFeedFileName", 
					"/Users/Mike/cvsAvlData/testAvlData.csv",
					"The name of the CSV file containing AVL data to process.");
	
	
	private static BooleanConfigValue processInRealTime =
			new BooleanConfigValue("transitime.avl.processInRealTime",
					false,
					"For when getting batch of AVL data from a CSV file. "
					+ "When true then when reading in do at the same speed as "
					+ "when the AVL was created. Set to false it you just want "
					+ "to read in as fast as possible.");

	/****************** Logging **************************************/
	
	private static final Logger logger = LoggerFactory
			.getLogger(BatchCsvAvlFeedModule.class);

	/********************** Member Functions **************************/

	/**
	 * @param projectId
	 */
	public BatchCsvAvlFeedModule(String projectId) {
		super(projectId);
	}

	/**
	 * If configured to process data in real time them delay the appropriate
	 * amount of time
	 * 
	 * @param avlReport
	 */
	private void delayIfRunningInRealTime(AvlReport avlReport) {
		if (processInRealTime.getValue()) {
			long delayLength = 0;

			if (lastAvlReportTimestamp > 0) {
				delayLength = avlReport.getTime() - lastAvlReportTimestamp;
				lastAvlReportTimestamp = avlReport.getTime();
			} else {
				lastAvlReportTimestamp = avlReport.getTime();
			}

			if (delayLength > 0)
				Time.sleep(delayLength);
		}		
	}
	
	/* 
	 * Reads in AVL reports from CSV file and processes them.
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		List<AvlReport> avlReports = 
				(new AvlCsvReader(getCsvAvlFeedFileName())).get();
		
		// Process the AVL Reports read in.
		for (AvlReport avlReport : avlReports) {
			
			logger.info("Processing avlReport={}", avlReport);
			
			// If configured to process data in real time them delay
			// the appropriate amount of time
			delayIfRunningInRealTime(avlReport);
			
			// Use the AVL report time as the current system time
			Core.getInstance().setSystemTime(avlReport.getTime());			

			// Actually process the AVL report
			AvlProcessor.getInstance().processAvlReport(avlReport);
			
			// Post process if neccessary
			if (avlPostProcessor != null)
				avlPostProcessor.postProcess(avlReport);
		}

		// Kill off the whole program because done processing the AVL data
        String integrationTest = System.getProperty("transitime.core.integrationTest");
        if(integrationTest != null){
            System.setProperty("transitime.core.csvImported","true");
        }else{
            System.exit(0);
        }
	}

	private AvlPostProcessor avlPostProcessor = null;
	
	public void setAvlPostProcessor(AvlPostProcessor avlPostProcessor) {
		this.avlPostProcessor = avlPostProcessor;
	}
	
	public interface AvlPostProcessor {
		void postProcess(AvlReport avlReport);
	}
}
