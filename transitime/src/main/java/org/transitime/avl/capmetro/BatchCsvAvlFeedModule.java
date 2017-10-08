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

package org.transitime.avl.capmetro;

import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.BooleanConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.core.AvlProcessor;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.Trip;
import org.transitime.gtfs.DbConfig;
import org.transitime.modules.Module;
import org.transitime.utils.Time;

/**
 * For reading in a batch of AVL data in CSV format and processing it. It only
 * reads a single batch of data, unlike the usual AVL modules that continuously
 * read data. This module is useful for debugging because can relatively easily
 * create a plain text CSV file of AVL data and see what the code does.
 * 
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

	private static StringConfigValue csvAvlFeedFileName = new StringConfigValue("transitime.avl.csvAvlFeedFileName",
			"https://github.com/scascketta/CapMetrics/blob/master/data/vehicle_positions/2017-10-04.csv?raw=true",
			"The name of the CSV file containing AVL data to process.");

	private static BooleanConfigValue processInRealTime = new BooleanConfigValue("transitime.avl.processInRealTime",
			false,
			"For when getting batch of AVL data from a CSV file. "
					+ "When true then when reading in do at the same speed as "
					+ "when the AVL was created. Set to false it you just want " + "to read in as fast as possible.");

	/****************** Logging **************************************/
	private static StringConfigValue routeIdFilterRegEx = new StringConfigValue("transitime.gtfs.routeIdFilterRegEx",
			null, // Default of null means don't do any filtering
			"Route is included only if route_id matches the this regular "
					+ "expression. If only want routes with \"SPECIAL\" in the id then "
					+ "would use \".*SPECIAL.*\". If want to filter out such trips "
					+ "would instead use the complicated \"^((?!SPECIAL).)*$\" or "
					+ "\"^((?!(SPECIAL1|SPECIAL2)).)*$\" " + "if want to filter out two names. The default value "
					+ "of null causes all routes to be included.");
	private static Pattern routeIdFilterRegExPattern = null;

	private static StringConfigValue startTimeOfDay = new StringConfigValue("transitime.avl.startTimeOfDay", "10",
			"The time of day to start processing AVL.");;

	private static StringConfigValue endTimeOfDay = new StringConfigValue("transitime.avl.endTimeOfDay", "14",
			"The time of day to end processing AVL.");

	private static final Logger logger = LoggerFactory.getLogger(BatchCsvAvlFeedModule.class);

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
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		List<AvlReport> avlReports = (new AvlCsvReader(getCsvAvlFeedFileName())).get();

		// Process the AVL Reports read in.
		for (AvlReport avlReport : avlReports) {

			logger.info("Processing avlReport={}", avlReport);

			// If configured to process data in real time them delay
			// the appropriate amount of time
			delayIfRunningInRealTime(avlReport);

			// Use the AVL report time as the current system time
			Core.getInstance().setSystemTime(avlReport.getTime());

			Calendar cal = Calendar.getInstance();
			cal.setTime(avlReport.getDate());
			int hour = cal.get(Calendar.HOUR_OF_DAY);

			if (routeIdFilterRegEx != null) {
				// Create pattern if haven't done so yet, but only do so once.
				if (routeIdFilterRegExPattern == null)
					routeIdFilterRegExPattern = Pattern.compile(routeIdFilterRegEx.getValue());

				DbConfig dbConfig = Core.getInstance().getDbConfig();

				Trip trip = dbConfig.getTrip(avlReport.getAssignmentId());
				
				if(trip!=null)
				{
					boolean matches = routeIdFilterRegExPattern.matcher(trip.getRouteId().trim()).matches();
	
					if (matches) {
						if (endTimeOfDay.getValue() != null && startTimeOfDay.getValue() != null) {
							if (hour >= new Integer(startTimeOfDay.getValue())
									&& hour < new Integer(endTimeOfDay.getValue())) 
							{
								AvlProcessor.getInstance().processAvlReport(avlReport);
							}
							
						} else {
							AvlProcessor.getInstance().processAvlReport(avlReport);
						}							
					}
				}else
				{
					logger.info("No trup with id {} in GTFS", avlReport.getAssignmentId());
				}
			} else {
				if (endTimeOfDay.getValue() != null && startTimeOfDay.getValue() != null) {
					if (hour >= new Integer(startTimeOfDay.getValue()) && hour < new Integer(endTimeOfDay.getValue())) {
						AvlProcessor.getInstance().processAvlReport(avlReport);
					}
				} 
				else 
				{
					AvlProcessor.getInstance().processAvlReport(avlReport);
				}
			}

		}
		// Wait for database queue to be emptied before exiting.
		while (Core.getInstance().getDbLogger().queueSize() > 0) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {

			}
		}
		// Kill off the whole program because done processing the AVL data
		System.exit(0);
	}
}
