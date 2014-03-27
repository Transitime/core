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

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.core.DataProcessor;
import org.transitime.db.structs.AvlReport;

/**
 * Receives data from the AvlJmsClient and processes it.
 * Can use multiple threads to handle data.
 * 
 * @author SkiBu Smith
 */
public class AvlClient implements Runnable {

	// The AVL report being processed
	private final AvlReport avlReport;

	// List of current AVL reports by vehicle. Useful for determining last
	// report so can filter out new report if the same as the old one.
	// Keyed on vehicle ID.
	private static HashMap<String, AvlReport> avlReports =
			new HashMap<String, AvlReport>();
	
	private static final Logger logger= 
			LoggerFactory.getLogger(AvlClient.class);	

	/********************** Member Functions **************************/

	/**
	 * Constructor
	 * @param avlReport
	 */
	public AvlClient(AvlReport avlReport) {
		this.avlReport = avlReport;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// If the data is bad throw it out
		String errorMsg = avlReport.validateData();
		if (errorMsg != null) {
			logger.error("Throwing away avlReport {} because {}", 
					avlReport, errorMsg);
			return;
		}
		
		// If report the same then don't need to process it
		synchronized (avlReports) {			
			AvlReport previousReportForVehicle = 
					avlReports.get(avlReport.getVehicleId());
			if (previousReportForVehicle != null &&
					avlReport.getTime() <= previousReportForVehicle.getTime()) {
				logger.warn("Throwing away AVL report because it is older " +
						"than the previous AVL report for the vehicle. New " +
						"AVL report is {}. Old AVL report is{}", 
						avlReport, previousReportForVehicle);
				return;
			}
			avlReports.put(avlReport.getVehicleId(), avlReport);
		}
		
		// Record when the AvlReport was actually processed. This is done here so
		// that the value will be set when the avlReport is stored in the database
		// using the DbLogger.
		avlReport.setTimeProcessed();

		// Store the AVL report into the database
		Core.getInstance().getDbLogger().add(avlReport);
		
		// Process the report
		logger.info("Thread={} AvlClient processing AVL data {}", 
				Thread.currentThread().getName(), avlReport);		
		DataProcessor.getInstance().processAvlReport(avlReport);
	}
	
}
