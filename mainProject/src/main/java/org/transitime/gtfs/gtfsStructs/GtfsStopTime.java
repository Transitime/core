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
package org.transitime.gtfs.gtfsStructs;

import org.apache.commons.csv.CSVRecord;
import org.transitime.utils.Time;

/**
 * A GTFS stop_times object
 * 
 * @author SkiBu Smith
 * 
 */
public class GtfsStopTime extends GtfsBase implements Comparable<GtfsStopTime> {

	private final String tripId;
	// arrivalTimeSecs is in seconds into day. Can be null
	private final Integer arrivalTimeSecs;
	// departureTimeSecs is in seconds into day. Can be null
	private final Integer departureTimeSecs;
	private final String stopId;
	private final int stopSequence;
	private final String stopHeadsign;
	private final String pickupType;
	private final String dropOffType;
	// Can be null
	private final Double shapeDistTraveled; 

	/********************** Member Functions **************************/

	/**
	 * @param record
	 * @param supplemental
	 * @param fileName
	 *            for logging errors
	 */
	public GtfsStopTime(CSVRecord record, boolean supplemental, String fileName)
			throws NumberFormatException {
		super(record, supplemental, fileName);

		tripId = getRequiredValue(record, "trip_id");

		// Convert arrival_time to seconds in day
		String arrivalTimeStr = getRequiredValue(record, "arrival_time");
		if (arrivalTimeStr != null)
			arrivalTimeSecs = Time.parseTimeOfDay(arrivalTimeStr);
		else
			arrivalTimeSecs = null;

		// Convert departure_time to seconds in day
		String deparatureTimeStr = getRequiredValue(record, "departure_time");
		if (deparatureTimeStr != null)
			departureTimeSecs = Time.parseTimeOfDay(deparatureTimeStr);
		else
			departureTimeSecs = null;

		stopId = getRequiredValue(record, "stop_id");

		String stopSequenceStr = getRequiredValue(record, "stop_sequence");
		stopSequence = Integer.parseInt(stopSequenceStr);

		stopHeadsign = getOptionalValue(record, "stop_headsign");
		pickupType = getOptionalValue(record, "pickup_type");
		dropOffType = getOptionalValue(record, "drop_off_type");
		
		String shapeDistTraveledStr = getOptionalValue(record, "shape_dist_traveled");
		shapeDistTraveled = shapeDistTraveledStr == null ? 
				null : Double.parseDouble(shapeDistTraveledStr);
	}

	/**
	 * For when need to convert a GtfsStopTime to a subclass. Copies the
	 * originalValues but uses newArrivalTime and newDepartureTime if they are
	 * not null. If they are null then will use the arrivalTimeSecs and
	 * departureTimeSecs from originalValues.
	 * 
	 * @param originalValues
	 *            The original values from the stop_times.txt file
	 * @param newArrivalTime
	 *            The new arrival time. If null passed in then the original time
	 *            is used.
	 * @param newDepartureTime
	 *            The new departure time. If null passed in then the original
	 *            time is used.
	 */
	protected GtfsStopTime(GtfsStopTime originalValues, Integer newArrivalTime,
			Integer newDepartureTime) {
		super(originalValues);

		tripId = originalValues.tripId;
		stopId = originalValues.stopId;
		stopSequence = originalValues.stopSequence;
		stopHeadsign = originalValues.stopHeadsign;
		pickupType = originalValues.pickupType;
		dropOffType = originalValues.dropOffType;
		shapeDistTraveled = originalValues.shapeDistTraveled;

		// Set the appropriate arrival/departure times. If null passed in
		// then use the original values.
		Integer newArrivalTimeSecs = newArrivalTime!=null ? 
				newArrivalTime : originalValues.arrivalTimeSecs;
		Integer newDepartureTimeSecs = newDepartureTime!=null ? 
				newDepartureTime : originalValues.departureTimeSecs;
		// Make sure that the arrivalTime is not after the departure time.
		// This can happen when have weird data. Since it looks really
		// peculiar in a schedule and doesn't make any sense use the 
		// average arrival/departure time for this situation.
		if (newArrivalTimeSecs > newDepartureTimeSecs) {
			newArrivalTimeSecs = newDepartureTimeSecs = 
					(newArrivalTimeSecs + newDepartureTimeSecs)/2;
		}
		arrivalTimeSecs = newArrivalTimeSecs;
		departureTimeSecs = newDepartureTimeSecs;
	}

	public String getTripId() {
		return tripId;
	}

	/**
	 * @return arrival time in seconds into day
	 */
	public Integer getArrivalTimeSecs() {
		return arrivalTimeSecs;
	}

	/**
	 * @return departure time in seconds into day
	 */
	public Integer getDepartureTimeSecs() {
		return departureTimeSecs;
	}

	public String getStopId() {
		return stopId;
	}

	public int getStopSequence() {
		return stopSequence;
	}

	public String getStopHeadsign() {
		return stopHeadsign;
	}

	public String getPickupType() {
		return pickupType;
	}

	public String getDropOffType() {
		return dropOffType;
	}

	/**
	 * 
	 * @return shape_dist_traveled or null if not set
	 */
	public Double getShapeDistTraveled() {
		return shapeDistTraveled;
	}

	@Override
	public String toString() {
		return "GtfsStopTime ["
				+ "lineNumber="	+ lineNumber + ", "
				+ (tripId != null ? "tripId=" + tripId + ", " : "")
				+ (arrivalTimeSecs != null ? "arrivalTime="
						+ Time.timeOfDayStr(arrivalTimeSecs) + ", " : "")
				+ (departureTimeSecs != null ? "departureTime="
						+ Time.timeOfDayStr(departureTimeSecs) + ", " : "")
				+ (stopId != null ? "stopId=" + stopId + ", " : "")
				+ "stopSequence=" + stopSequence + ", "
				+ (stopHeadsign != null ? "stopHeadsign=" + stopHeadsign
						+ ", " : "")
				+ (pickupType != null ? "pickupType=" + pickupType + ", " : "")
				+ (dropOffType != null ? "dropOffType=" + dropOffType + ", " : "")
				+ (shapeDistTraveled != null ? "shapeDistTraveled="
						+ shapeDistTraveled : "") 
				+ "]";
	}

	/**
	 * So can use Collections.sort() to sort an Array of GtfsStopTime objects by 
	 * stop sequence.
	 * 
	 * @param arg0
	 * @return
	 */
	@Override
	public int compareTo(GtfsStopTime arg0) {
		return getStopSequence() - arg0.getStopSequence();
	}

}
