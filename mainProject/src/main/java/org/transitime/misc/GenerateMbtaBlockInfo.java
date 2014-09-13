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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.structs.AvlReport;
import org.transitime.utils.Time;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class GenerateMbtaBlockInfo {

	private static Time time = new Time("America/New_York");

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
	 * @param args
	 */
	public static void main(String[] args) {
		// Need to set timezone so that when dates are read in they
		// will be correct.
		TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
		
		// Determine the parameters
		Date beginDate = getDate(args[0]);
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
			
			String previousTripId = "";
			String previousBlockId = "";
			
			for (AvlReport avlReport : avlReportsForVehicle) {
				String tripId = avlReport.getAssignmentId();
				String blockId = avlReport.getField1Value();
				
				// If no change in trip or block ID then don't need to output it
				if (tripId.equals(previousTripId) && blockId.equals(previousBlockId))
					continue;
				previousTripId = tripId;
				previousBlockId = blockId;
				
				// Output the new trip & block info
				System.out.println("  " 
						+ time.dateTimeStrMsecForTimezone(avlReport.getTime()) 
						+ " tripId=" + tripId + " blockId=" + blockId 
						+ " lat=" + avlReport.getLat() + " lon=" + avlReport.getLon());				
			}
		}
	}

}
