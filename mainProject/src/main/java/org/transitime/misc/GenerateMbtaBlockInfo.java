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

package org.transitime.misc;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.structs.AvlReport;
import org.transitime.gtfs.gtfsStructs.GtfsStopTime;
import org.transitime.gtfs.gtfsStructs.GtfsTrip;
import org.transitime.gtfs.readers.GtfsStopTimesReader;
import org.transitime.gtfs.readers.GtfsTripsReader;
import org.transitime.gtfs.writers.GtfsTripsWriter;
import org.transitime.utils.Time;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class GenerateMbtaBlockInfo {

	private static String timeZoneStr = "America/New_York";

	private static Time time = new Time(timeZoneStr);

	// Keyed on tripId, value is blockId
	private static Map<String, String> tripToBlockMap = 
			new HashMap<String, String>();
	
	private static final Logger logger = LoggerFactory
			.getLogger(GenerateMbtaBlockInfo.class);

	/********************** Member Functions **************************/

	/**
	 * Converts dateStr to a Date using the "America/New_York" timezone.
	 * 
	 * @param dateStr
	 * @return
	 */
	private static Date getDate(String dateStr) {
		try {
			return time.parseUsingTimezone(dateStr);
		} catch (ParseException e) {
			logger.error("Could not parse date \"{}\"", dateStr);
			System.exit(-1);
			return null;
		}		
	}
	
	/**
	 * Writes the supplemental trips file that includes block IDs.
	 * 
	 * @param supplementTripsFileName
	 */
	private static void writeSupplementalTripsFile(
			String supplementTripsFileName) {
		GtfsTripsWriter writer = new GtfsTripsWriter(supplementTripsFileName);
		
		Collection<String> tripIds = tripToBlockMap.keySet();
		for (String tripId : tripIds) {
			String blockIdForTrip = tripToBlockMap.get(tripId);
			GtfsTrip gtfsTrip = new GtfsTrip(tripId, blockIdForTrip);
			writer.write(gtfsTrip);
		}
		
		writer.close();		
	}
	
	/**
	 * Processes days worth of AVL data to determine the block assignments
	 * for each trip. Updates the static tripToBlockMap with the block 
	 * assignment for each trip.
	 * 
	 * @param beginDate
	 */
	private static void processDayOfData(Date beginDate) {
		Date endDate = new Date(beginDate.getTime() + Time.MS_PER_DAY);
		System.out.println("Processing data for beginDate=" 
				+ time.dateTimeStrMsecForTimezone(beginDate.getTime())
				+ " endDate=" 
				+ time.dateTimeStrMsecForTimezone(endDate.getTime()));
		
		// Get days worth of data from db
		List<AvlReport> avlReports = AvlReport.getAvlReportsFromDb("mbta",
				beginDate, endDate, null, "ORDER BY time");
		System.out.println("Read in " + avlReports.size() 
				+ " AVL reports for " 
				+ time.dateTimeStrMsecForTimezone(beginDate.getTime()));
		
		// Put data into a list for each vehicle
		Map<String, List<AvlReport>> reportsByVehicle = 
				new HashMap<String, List<AvlReport>>(avlReports.size());
		for (AvlReport avlReport : avlReports) {
			// If avlReport is for a bad trip then simply ignore it.
			// Keep trip 9999 for now since it seems to indicate an end of 
			// a trip
			String tripId = avlReport.getAssignmentId();
			if (tripId.equals("000"))
				continue;
			
			String vehicleId = avlReport.getVehicleId();
			List<AvlReport> reportsForVehicle = reportsByVehicle.get(vehicleId);
			if (reportsForVehicle == null) {
				reportsForVehicle = new ArrayList<AvlReport>();
				reportsByVehicle.put(vehicleId, reportsForVehicle);				
			}
			reportsForVehicle.add(avlReport);
		}
		
		// For each vehicle...
		for (String vehicleId : reportsByVehicle.keySet()) {
			System.out.println("\nFor vehicleId=" + vehicleId);
			List<AvlReport> avlReportsForVehicle = reportsByVehicle.get(vehicleId);
			
			String previousTripShortName = "";
			String previousBlockId = "";
			
			for (AvlReport avlReport : avlReportsForVehicle) {
				String tripShortName = avlReport.getAssignmentId();
				String blockId = avlReport.getField1Value();
				
				// If no change in trip or block ID then don't need to output it
				if (tripShortName.equals(previousTripShortName) && blockId.equals(previousBlockId))
					continue;
				previousTripShortName = tripShortName;
				previousBlockId = blockId;
				
				Integer start = tripStartTimeMap.get(tripShortName);
				String tripStartStr = start!=null ? Time.timeOfDayStr(start) : "--:--:--";
				
				Integer end = tripEndTimeMap.get(tripShortName);
				String tripEndStr = end!=null ? Time.timeOfDayStr(end) : "--:--:--";
				
				// Output the new trip & block info
				System.out.println("  " 
						+ " avlTime=" + time.dateTimeStrMsecForTimezone(avlReport.getTime())
						+ " tripShortName(pattern)=" + tripShortName + " blockId(workpiece)=" + blockId 
						+ " tripStart=" + tripStartStr
						+ " tripEnd=" + tripEndStr
						+ " lat=" + avlReport.getLat() + " lon=" + avlReport.getLon());	
				
				
				time.getSecondsIntoDay(avlReport.getTime());
				
				// Add trip/block relationship to map. But only do so if not a special
				// trip 9999 or block 000.
				if (tripShortName.equals("9999") || blockId.equals("000"))
					continue;
				String previousBlockForTrip = tripToBlockMap.get(tripShortName);
				if (previousBlockForTrip != null 
						&& !previousBlockForTrip.equals(blockId)) {
					System.out.println("Got missmatch between block assignment "
							+ "for tripId="	+ tripShortName 
							+ ". Block for that trip was " + previousBlockForTrip 
							+ " but for begin date " + beginDate + " it is " 
							+ blockId);
				} else {
					// The block ID does not contradict a previous one for this 
					// trip ID. Therefore remember this block assignment for 
					// the trip.
					tripToBlockMap.put(tripShortName, blockId);
				}
					
			} // End of for each AVL report for the vehicle
		} // End of for each vehicle
		
	}
	
	// Keyed on trip ID
	private static Map<String, String> tripIdToTripShortNameMap = new HashMap<String, String>();
	
	// Keyed on trip short name
	private static Map<String, Integer> tripStartTimeMap = new HashMap<String, Integer>();
	private static Map<String, Integer> tripEndTimeMap = new HashMap<String, Integer>();
	
	/**
	 * Determines start and end times of trips using GTFS stop_times.txt data and puts them
	 * into tripStartTimeMap and tripEndTimeMap.
	 * 
	 * @param gtfsDir
	 */
	private static void readTripTimes(String gtfsDir) {
		GtfsTripsReader gtfsTripsReader = new GtfsTripsReader(gtfsDir);
		for (GtfsTrip gtfsTrip : gtfsTripsReader.get()) {
			tripIdToTripShortNameMap.put(gtfsTrip.getTripId(), gtfsTrip.getTripShortName());
		}
		
		GtfsStopTimesReader gtfsStopTimesReader = new GtfsStopTimesReader(gtfsDir);
		for (GtfsStopTime gtfsStopTime : gtfsStopTimesReader.get()) {
			String tripId = gtfsStopTime.getTripId();
			String tripShortName = tripIdToTripShortNameMap.get(tripId);
			Integer arrivalTimeForTrip = gtfsStopTime.getArrivalTimeSecs();
			
			Integer previousStartForTrip = tripStartTimeMap.get(tripShortName);
			if (previousStartForTrip == null || arrivalTimeForTrip < previousStartForTrip)
				tripStartTimeMap.put(tripShortName, arrivalTimeForTrip);

			Integer previousEndForTrip = tripEndTimeMap.get(tripShortName);
			if (previousEndForTrip == null || arrivalTimeForTrip > previousEndForTrip)
				tripEndTimeMap.put(tripShortName, arrivalTimeForTrip);
		}
	}
	
	/**
	 * @param args
	 *            args[0] is the date to run program for, args[1] is number of
	 *            days of data to use, args[2] is the directory name where
	 *            the GTFS files reside and where the supplement/trips.txt file
	 *            is written.
	 */
	public static void main(String[] args) {
		// Need to set timezone so that when dates are read in they
		// will be correct.
		TimeZone.setDefault(TimeZone.getTimeZone(timeZoneStr));
		
		// Determine the parameters
		Date beginDate = getDate(args[0]);
		int numberOfDays = Integer.parseInt(args[1]);
		String gtfsDir = args[2];
		
		// Initialize trip times maps
		readTripTimes(gtfsDir);
		
		// Process the AVL data to determine block assignment associated with
		// each trip
		for (int day=0; day < numberOfDays; ++day) {
			Date date = new Date(beginDate.getTime() + day*Time.MS_PER_DAY);
			processDayOfData(date);
		}
		
		// Write out the supplemental trips file
		writeSupplementalTripsFile(gtfsDir + "/supplement/trips.txt");
	}

}
