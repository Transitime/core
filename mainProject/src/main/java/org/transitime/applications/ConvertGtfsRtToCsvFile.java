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
import org.transitime.config.BooleanConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.config.StringListConfigValue;
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

	private static final Logger logger = 
			LoggerFactory.getLogger(ConvertGtfsRtToCsvFile.class);

	/*********** Configurable Parameters for this module ***********/
	private static List<String> getGtfsRealtimeURIs() {
		return gtfsRealtimeURIs.getValue();
	}
	private static StringListConfigValue gtfsRealtimeURIs =
			new StringListConfigValue("transitime.avl.gtfsRealtimeFeedURIs");
	
	private static String getCsvFileOutputDir() {
		return csvFileOutputDir.getValue();
	}
	private static StringConfigValue csvFileOutputDir =
			new StringConfigValue("transitime.avl.csvFileOutputDir", 
					"/Users/Mike/gtfsRealtimeData/csv");

	private static String getTimeZoneStr() {
		if (timeZoneStr.getValue().isEmpty())
			return null;
		else
			return timeZoneStr.getValue();
	}
	private static StringConfigValue timeZoneStr =
			new StringConfigValue("transitime.avl.timeZoneStr", 
					"");

	private static boolean shouldOffsetForMapOfChina() {
		return offsetForMapOfChina.getValue();
	}
	private static BooleanConfigValue offsetForMapOfChina = 
			new BooleanConfigValue("transitime.avl.offsetForMapOfChina", false);
	
	/********************** Member Functions **************************/

	/**
	 * Adds avlReport to avlReportsByVehicle map.
	 * 
	 * @param avlReport
	 * @param avlReportsByVehicle
	 */
	private static void processReport(AvlReport avlReport,
			Map<String, List<AvlReport>> avlReportsByVehicle) {
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
	 * 
	 * @param avlReportsByVehicle
	 */
	private static void writeCsvFiles(
			Map<String, List<AvlReport>> avlReportsByVehicle) {
		String directory = getCsvFileOutputDir();
		logger.info("Writing out CSV files to directory {}", directory);
		
		Set<String> vehicleIds = avlReportsByVehicle.keySet();
		for (String vehicleId : vehicleIds) {
			String fileName = directory + "/" + vehicleId + ".csv";
			AvlCsvWriter writer = new AvlCsvWriter(fileName, getTimeZoneStr());

			List<AvlReport> avlReportsForVehicle = 
					avlReportsByVehicle.get(vehicleId);
			for (AvlReport avlReport : avlReportsForVehicle) {
				writer.write(avlReport, shouldOffsetForMapOfChina());
			}
			writer.close();
		}
	}
	
	/**
	 * Reads in a GTFS-realtime file for a Vehicle Positions feed and converts
	 * it to a CSV file containing the AVL reports.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		List<String> uris = getGtfsRealtimeURIs();
		
		// Read in GTFS-realtime data
		for (String uri : uris) {
			// For keeping track of all the AVL reports. Keyed on vehicleId
			Map<String, List<AvlReport>> avlReportsByVehicle =
					new HashMap<String, List<AvlReport>>();
					
			// Read in all the AvlReports
			List<AvlReport> avlReports = 
					GtfsRtVehiclePositionsReader.getAvlReports(uri);
			
			// Load all AvlReports into map by vehicle ID
			for (AvlReport avlReport : avlReports) {
				processReport(avlReport, avlReportsByVehicle);
			}
			
			// Write out the CSV files, one per vehicle
			writeCsvFiles(avlReportsByVehicle);
		}
		
		logger.info("Done writing out CSV files!");
	}


}
