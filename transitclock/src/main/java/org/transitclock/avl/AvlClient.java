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

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.configData.AvlConfig;
import org.transitclock.configData.CoreConfig;
import org.transitclock.core.AvlProcessor;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.logging.Markers;
import org.transitclock.utils.Time;

/**
 * Receives AVL data from the AvlExecutor or JMS, determines if AVL should be
 * filtered, and processes data that doesn't need to be filtered. Can use
 * multiple threads to process data.
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
	
	/**
	 * Returns the AVL report associated with this AvlClient
	 * 
	 * @return the AVL report
	 */
	public AvlReport getAvlReport() {
		return avlReport;
	}
	
	/**
	 * Filters out problematic AVL reports (such as for having invalid data,
	 * being in the past, or too recent) and processes the ones that are good.
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// Put a try/catch around everything so that if unexpected exception 
		// occurs an e-mail is sent and the avl client thread isn't killed.
		try {
			// If the data is bad throw it out
			String errorMsg = avlReport.validateData();
			if (errorMsg != null) {
				logger.error("Throwing away avlReport {} because {}",
						avlReport, errorMsg);
				return;
			}

			// See if should filter out report
			synchronized (avlReports) {
				AvlReport previousReportForVehicle =
						avlReports.get(avlReport.getVehicleId());

				// If report the same time or older then don't need to process
				// it
				if (previousReportForVehicle != null
						&& avlReport.getTime() <= previousReportForVehicle
								.getTime()) {
					if (avlReport.hasValidAssignment() && !previousReportForVehicle.hasValidAssignment()) {
						// assignment records have higher priority then just lat/lon updates
						logger.info("keeping older AVL report because it contains an assignment "
								+ "not present in cache {}", avlReport);
					} else {
						logger.warn("Throwing away AVL report because it is same time "
										+ "or older than the previous AVL report for the "
										+ "vehicle. New AVL report is {}. Previous valid AVL "
										+ "report is {}", avlReport,
								previousReportForVehicle);
						return;
					}
				}

				// If previous report happened too recently then don't want to
				// process it. This is important for when get AVL data for a
				// vehicle
				// more frequently than is worthwhile, like every couple of
				// seconds.
				if (previousReportForVehicle != null) {
					long timeBetweenReportsSecs =
							(avlReport.getTime() - previousReportForVehicle
									.getTime()) / Time.MS_PER_SEC;
					if (timeBetweenReportsSecs < AvlConfig
							.getMinTimeBetweenAvlReportsSecs()) {
						// Log this but. Since this can happen very frequently
						// (VTA has hundreds of vehicles reporting every
						// second!)
						// separated the logging into two statements in case
						// want
						// to make the first shorter one a warn message but keep
						// the
						// second more verbose one a debug statement.
						logger.debug("AVL report for vehicleId={} for time {} is "
								+ "only {} seconds old which is too recent to "
								+ "previous report so not fully processing it. "
								+ "Just updating the vehicle's location in cache.",
								avlReport.getVehicleId(), avlReport.getTime(),
								timeBetweenReportsSecs);
						logger.debug("Not processing AVL report because the new "
								+ "report is too close in time to the previous AVL "
								+ "report for the vehicle. "
								+ "transitclock.avl.minTimeBetweenAvlReportsSecs={} "
								+ "secs. New AVL report is {}. Previous valid AVL "
								+ "report is {}",
								AvlConfig.getMinTimeBetweenAvlReportsSecs(),
								avlReport, previousReportForVehicle);

						// But still want to update the vehicle cache with the
						// latest report because doing so is cheap and it allows
						// vehicles to move on map smoothly
						if (avlReport.hasValidAssignment() && !previousReportForVehicle.hasValidAssignment()) {
							logger.info("using discarded AVL report due to its assignment {}", avlReport);
						} else {
							AvlProcessor.getInstance()
									.cacheAvlReportWithoutProcessing(avlReport);
							// Done here since not processing this AVL report
							return;

						}
					}
				}

				// Should handle the AVL report. Remember it so can possibly
				// filter
				// the next one
				avlReports.put(avlReport.getVehicleId(), avlReport);
			}

			// Process the report
			logger.info("Thread={} AvlClient processing AVL data {}", 
					Thread.currentThread().getName(), avlReport);
			AvlProcessor.getInstance().processAvlReport(avlReport);
		} catch (Exception e) {
			e.printStackTrace();
			// Catch unexpected exceptions so that can continue to use the same
			// AVL thread even if there is an unexpected problem. Only let
			// Errors, such as OutOfMemory errors, through.
			logger.error(Markers.email(),
					"For agencyId={} Exception {} for avlReport={}.", 
					AgencyConfig.getAgencyId(), e.getMessage(), avlReport, e);
		}
	}
	
}
