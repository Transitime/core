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
package org.transitime.gtfs.writers;

import java.io.IOException;
import org.transitime.gtfs.gtfsStructs.GtfsExtendedStopTime;

/**
 * For writing out extended GTFS stop_times.txt file that contains additional
 * info beyond the usual stop_times.txt file such as statistical info determined
 * when creating the schedule.
 * 
 * @author SkiBu Smith
 * 
 */
public class GtfsExtendedStopTimesWriter extends GtfsStopTimesWriter {

	/********************** Member Functions **************************/
	
	/**
	 * Creates file writer and writes the header. 
	 * @param fileName
	 */
	public GtfsExtendedStopTimesWriter(String fileName) {
		super(fileName);
	}
	
	/**
	 * Writes the header to the file.
	 * 
	 * @throws IOException
	 */
	protected void writeHeader() throws IOException {
	    writer.append("trip_id,arrival_time,departure_time,stop_id," +
	    		"stop_sequence,stop_headsign,pickup_type,drop_off_type," +
	    		"shape_dist_traveled," + 
	    		"original_arrival_time,min_arrival_time,max_arrival_time," +
	    		"arrivals_std_dev,number_arrivals," +
	    		"original_departure_time,min_departure_time,max_departure_time," +
	    		"departures_std_dev,number_departures" +
	    		"\n");
	}
	
	/**
	 * Writes a GtfsExtendedStopTimes to the file.
	 * 
	 * @param fileName
	 * @param stopTimes
	 */
	public void write(GtfsExtendedStopTime stopTime) {
		try	{
	    	// Write out the normal GTFS stop times data
	    	append(stopTime.getTripId()).append(',');
	    	appendTime(stopTime.getArrivalTimeSecs()).append(',');
	    	appendTime(stopTime.getDepartureTimeSecs()).append(',');
	    	append(stopTime.getStopId()).append(',');
	    	append(stopTime.getStopSequence()).append(',');
	    	append(stopTime.getStopHeadsign()).append(',');
	    	append(stopTime.getPickupType()).append(',');
	    	append(stopTime.getDropOffType()).append(',');
	    	append(stopTime.getShapeDistTraveled()).append(',');
	    	
	    	// Write out the extended stop times data.
	    	// Add a space as a visual separator before outputting arrival data
	    	writer.append(' ');
	    	appendTime(stopTime.getArrivalOrigTimeSecs()).append(',');
	    	appendTime(stopTime.getArrivalMinTimeSecs()).append(',');
	    	appendTime(stopTime.getArrivalMaxTimeSecs()).append(',');
	    	append(stopTime.getArrivalStdDev()).append(',');
	    	append(stopTime.getArrivalNumberDatapoints()).append(',');

	    	// Add a space as a visual separator before outputting departure data
	    	writer.append(' ');
	    	appendTime(stopTime.getDepartureOrigTimeSecs()).append(',');
	    	appendTime(stopTime.getDepartureMinTimeSecs()).append(',');
	    	appendTime(stopTime.getDepartureMaxTimeSecs()).append(',');
	    	append(stopTime.getDepartureStdDev()).append(',');
	    	append(stopTime.getDepartureNumberDatapoints()).append('\n');
		} catch(IOException e) {
			// Only expect to run this in batch mode so don't really
			// need to log an error using regular logging. Printing
			// stack trace should suffice.
		     e.printStackTrace();
		} 
	}

}
