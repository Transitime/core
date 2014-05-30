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

package org.transitime.applications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.avl.AvlCsvWriter;
import org.transitime.config.StringConfigValue;
import org.transitime.db.structs.AvlReport;
import org.transitime.feed.gtfsRt.GtfsRtVehiclePositionsReader;

/**
 * Reads in a GTFS-realtime file for a Vehicle Positions feed and converts it
 * to a CSV file containing the AVL reports.
 *
 * @author SkiBu Smith
 *
 */
public class ConvertGtfsRtToCsvFile {

	// Keyed on vehicleId
	private static Map<String, List<AvlReport>> avlReportsByVehicle =
			new HashMap<String, List<AvlReport>>();
			
	private static final Logger logger = LoggerFactory
			.getLogger(ConvertGtfsRtToCsvFile.class);

	/*********** Configurable Parameters for this module ***********/
	public static String getGtfsRealtimeURI() {
		return gtfsRealtimeURI.getValue();
	}
	private static StringConfigValue gtfsRealtimeURI =
			new StringConfigValue("transitime.avl.gtfsRealtimeFeedURI", 
					"file:///C:/Users/Mike/gtfsRealtimeData");
	
	public static String getCsvFileDir() {
		return csvFileDir.getValue();
	}
	private static StringConfigValue csvFileDir =
			new StringConfigValue("transitime.avl.csvFileDir", 
					"/Users/Mike/gtfsRealtimeData/csv");

	public static String getTimeZoneStr() {
		if (timeZoneStr.getValue().isEmpty())
			return null;
		else
			return timeZoneStr.getValue();
	}
	private static StringConfigValue timeZoneStr =
			new StringConfigValue("transitime.avl.timeZoneStr", 
					"");

	/********************** Member Functions **************************/

	/**
	 * Adds avlReport to avlReportsByVehicle map.
	 * 
	 * @param avlReport
	 */
	private static void processReport(AvlReport avlReport) {
		String vehicleId = avlReport.getVehicleId();
		List<AvlReport> avlReportsForVehicle =
				avlReportsByVehicle.get(vehicleId);
		if (avlReportsForVehicle == null) {
			avlReportsForVehicle = new ArrayList<AvlReport>();
			avlReportsByVehicle.put(vehicleId, avlReportsForVehicle);
		}
		
		avlReportsForVehicle.add(avlReport);
	}
	
	/**
	 * Writes out separate CSV file of AvlReports for each vehicle.
	 */
	private static void writeCsvFiles() {
		String directory = getCsvFileDir();
		logger.info("Writing out CSV files to directory {}", directory);
		
		Set<String> vehicleIds = avlReportsByVehicle.keySet();
		for (String vehicleId : vehicleIds) {
			String fileName = directory + "/" + vehicleId + ".csv";
			AvlCsvWriter writer = new AvlCsvWriter(fileName, getTimeZoneStr());

			List<AvlReport> avlReportsForVehicle = 
					avlReportsByVehicle.get(vehicleId);
			for (AvlReport avlReport : avlReportsForVehicle) {
				writer.write(avlReport);
			}
			writer.close();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Read in GTFS-realtime data
		List<AvlReport> avlReports = GtfsRtVehiclePositionsReader
				.getAvlReports(getGtfsRealtimeURI());
		
		// Load all AvlReports into map by vehicle ID
		for (AvlReport avlReport : avlReports) {
			processReport(avlReport);
		}
		
		// Write out the CSV files, one per vehicle
		writeCsvFiles();
		
		logger.info("Done writing out CSV files!");
	}


}
