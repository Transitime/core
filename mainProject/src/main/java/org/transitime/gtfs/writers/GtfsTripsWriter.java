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

import org.transitime.gtfs.gtfsStructs.GtfsTrip;
import org.transitime.utils.csv.CsvWriterBase;

/**
 * For writing out the trips.txt GTFS file. Useful for when need to modify the
 * file, such as for programmatically adding block info.
 *
 * @author SkiBu Smith
 *
 */
public class GtfsTripsWriter extends CsvWriterBase {

	/********************** Member Functions **************************/

	/**
	 * Creates file writer and writes the header. 
	 * 
	 * @param fileName
	 */
	public GtfsTripsWriter(String fileName) {
		super(fileName, false);
	}

	/**
	 * Writes the header to the file.
	 * 
	 * @throws IOException
	 */
	@Override
	protected void writeHeader() throws IOException {
	    // Write the header
	    writer.append("route_id,service_id,trip_id,trip_headsign,"
	    		+ "trip_short_name,direction_id,block_id,shape_id,"
	    		+ "wheelchair_accessible,bikes_allowed\n");
	}
	
	/**
	 * Writes a GtfsTrip to the file
	 * 
	 * @param gtfsStop
	 */
	public void write(GtfsTrip gtfsTrip) {
		try {
			// Write out the gtfsStop
			append(gtfsTrip.getRouteId()).append(',');
			append(gtfsTrip.getServiceId()).append(',');
			append(gtfsTrip.getTripId()).append(',');
			append(gtfsTrip.getTripHeadsign()).append(',');
			append(gtfsTrip.getTripShortName()).append(',');
			append(gtfsTrip.getDirectionId()).append(',');
			append(gtfsTrip.getBlockId()).append(',');
			append(gtfsTrip.getShapeId()).append(',');
			append(gtfsTrip.getWheelchairAccessible()).append(',');
			append(gtfsTrip.getBikesAllowed()).append('\n');
		} catch (IOException e) {
			// Only expect to run this in batch mode so don't really
			// need to log an error using regular logging. Printing
			// stack trace should suffice.
		     e.printStackTrace();
		}
	}

}
