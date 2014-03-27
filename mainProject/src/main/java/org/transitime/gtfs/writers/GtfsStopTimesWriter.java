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
import java.io.Writer;

import org.transitime.gtfs.gtfsStructs.GtfsStopTime;
import org.transitime.utils.Time;

/**
 * For writing a GTFS stop_times.txt file. Since this is pretty simple
 * not using a general CSV class to do the writing.
 * 
 * @author SkiBu Smith
 *
 */
public class GtfsStopTimesWriter extends GtfsWriterBase {

	/********************** Member Functions **************************/
	
	/**
	 * Creates file writer and writes the header. 
	 * @param fileName
	 */
	public GtfsStopTimesWriter(String fileName) {
		super(fileName);
	}

	/**
	 * Writes the header to the file.
	 * 
	 * @throws IOException
	 */
	@Override
	protected void writeHeader() throws IOException {
	    // Write the header
	    writer.append("trip_id,arrival_time,departure_time,stop_id," +
	    		"stop_sequence,stop_headsign,pickup_type,drop_off_type," +
	    		"shape_dist_traveled\n");
	}
	
	/**
	 * For handling values which can be null.
	 * 
	 * @param o the object to be written
	 * @return the Writer
	 * @throws IOException 
	 */
	protected Writer append(Object o) 
			throws IOException {
		if (o != null)
			writer.append(o.toString());
		return writer;
	}
	
	/**
	 * Writing time values are is special case because need to convert the 
	 * timeOfDay in seconds to a time of day string such as 11:53:01.
	 * 
	 * @param timeOfDay the object to be written
	 * @return the Writer
	 * @throws IOException
	 */
	protected Writer appendTime(Integer timeOfDay) 
			throws IOException {
		if (timeOfDay != null)
			writer.append(Time.timeOfDayStr(timeOfDay));
		return writer;
	}
	
	/**
	 * Writes a GtfsStopTime to the file
	 * 
	 * @param stopTime
	 */
	public void write(GtfsStopTime stopTime) {
		try	{	 
		    // Write the data
			append(stopTime.getTripId()).append(',');
	    	appendTime(stopTime.getArrivalTimeSecs()).append(',');
	    	appendTime(stopTime.getDepartureTimeSecs()).append(',');
	    	append(stopTime.getStopId()).append(',');
	    	append(stopTime.getStopSequence()).append(',');
	    	append(stopTime.getStopHeadsign()).append(',');
	    	append(stopTime.getPickupType()).append(',');
	    	append(stopTime.getDropOffType()).append(',');
	    	append(stopTime.getShapeDistTraveled()).append('\n');
		}
		catch(IOException e) {
			// Only expect to run this in batch mode so don't really
			// need to log an error using regular logging. Printing
			// stack trace should suffice.
		     e.printStackTrace();
		} 
	}
}
