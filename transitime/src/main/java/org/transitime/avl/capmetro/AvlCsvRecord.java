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

import org.apache.commons.csv.CSVRecord;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.AvlReport.AssignmentType;
import org.transitime.utils.Time;
import org.transitime.utils.csv.CsvBase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Represents a single record in a CSV file containing AVL data from CapMetrics.
 * 
 * https://github.com/scascketta/CapMetrics
 *
 * @author SkiBu Smith
 *
 */
public class AvlCsvRecord extends CsvBase {

	/********************** Member Functions **************************/

	private AvlCsvRecord(CSVRecord record, String fileName) {
		super(record, false, fileName);
	}
	
	/**
	 * Returns AvlReport that the line in the CSV file represents.	
	 * 
	 * @param record
	 * @param fileName
	 * @return AvlReport, or null if could not be parsed.
	 */
	public static AvlReport getAvlReport(CSVRecord record, String fileName)
			throws ParseException {
		AvlCsvRecord avlCsvRecord = new AvlCsvRecord(record, fileName);

		// Obtain the required values
		String vehicleId = avlCsvRecord.getRequiredValue(record, "vehicle_id");

		String timeStr = avlCsvRecord.getRequiredValue(record, "timestamp");	
		
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
				
		// Process time
		long time = 0L;
		
		time=dateFormatter.parse(timeStr).getTime();		
										
		String latStr = avlCsvRecord.getRequiredValue(record, "latitude");
		
		double lat=Double.NaN;
		try {
			lat = Double.parseDouble(latStr);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String lonStr = avlCsvRecord.getRequiredValue(record, "longitude");
		double lon = Double.parseDouble(lonStr);

		String speedStr = avlCsvRecord.getOptionalValue(record, "speed");
		float speed = speedStr == null ? Float.NaN : Float.parseFloat(speedStr);

		String headingStr = avlCsvRecord.getOptionalValue(record, "heading");
		float heading = headingStr == null ? 
				Float.NaN : Float.parseFloat(headingStr);
		
		// Obtain the optional values
		String leadVehicleId = avlCsvRecord.getOptionalValue(record,
				"leadVehicleId");
		String driverId = 
				avlCsvRecord.getOptionalValue(record, "driverId");
		String licensePlate = avlCsvRecord.getOptionalValue(record,
				"licensePlate");

		String passengerFullnessStr =
				avlCsvRecord.getOptionalValue(record, "passengerFullness");
		float passengerFullness = passengerFullnessStr==null ? 
				Float.NaN : Float.parseFloat(passengerFullnessStr);

		String passengerCountStr = 
				avlCsvRecord.getOptionalValue(record, "passengerCount");
		Integer passengerCount = passengerCountStr==null ?
				null : Integer.parseInt(passengerCountStr);
		
		// Create the avlReport
		AvlReport avlReport =
				new AvlReport(vehicleId, time, lat, lon, speed, heading, "CSV",
						leadVehicleId, driverId, licensePlate, passengerCount,
						passengerFullness);

		// Assignment info
		String assignmentId = 
				avlCsvRecord.getOptionalValue(record, "trip_id");
		
		AssignmentType assignmentType = AssignmentType.TRIP_ID;;
		
		avlReport.setAssignment(assignmentId, assignmentType);			
		
		
		return avlReport;
	}
	
}
