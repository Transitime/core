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
import java.util.Collection;
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
			new StringListConfigValue("transitime.avl.gtfsRealtimeFeedURIs",
					null,
					"Semicolon separated list of URIs of the GTFS-realtime data to read in");
	
	private static String getCsvFileOutputDir() {
		return csvFileOutputDir.getValue();
	}
	private static StringConfigValue csvFileOutputDir =
			new StringConfigValue("transitime.avl.csvFileOutputDir", 
					"/Users/Mike/gtfsRealtimeData/csv",
					"Name of directory where output will be written");

	private static String getTimeZoneStr() {
		if (timeZoneStr.getValue().isEmpty())
			return null;
		else
			return timeZoneStr.getValue();
	}
	private static StringConfigValue timeZoneStr =
			new StringConfigValue("transitime.avl.timeZoneStr", 
					"",
					"The timezone for the agency. In the form " +
					"\"America/New_York\"");

	private static boolean shouldOffsetForMapOfChina() {
		return offsetForMapOfChina.getValue();
	}
	private static BooleanConfigValue offsetForMapOfChina = 
			new BooleanConfigValue("transitime.avl.offsetForMapOfChina", 
					false,
					"If set to true then the latitudes/longitudes stored " +
					"will be adjusted for the China map offset so that the " +
					"points will be displayed properly on a street map.");
	
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
			// This application can read in a great deal of data from multiple
			// GTFS-realtime files. The protobuffer tools require the entire
			// file to be read in as a FeedMessage and only then converted to
			// AvlReports. This means that have two copies of the data in memory
			// at once. If multiple files are read in and there is not enough
			// heap space allocated then found that the memory for the previous
			// GTFS-realtime file is sometimes not freed up before the next file
			// is read in. And because the system is bogged down reading and 
			// parsing files it means that the garbage collector might not run
			// until system is already bogged down. To deal with this need to
			// call the garbage collector manually between dealing with each
			// GTFS-realtime file. 
			System.gc();
			
			// For keeping track of all the AVL reports. Keyed on vehicleId
			Map<String, List<AvlReport>> avlReportsByVehicle =
					new HashMap<String, List<AvlReport>>();
					
			// Read in all the AvlReports
			Collection<AvlReport> avlReports = 
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
