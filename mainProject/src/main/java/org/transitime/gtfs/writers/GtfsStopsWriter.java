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
import java.text.DecimalFormat;

import org.transitime.gtfs.gtfsStructs.GtfsStop;


/**
 * For writing out the stops.txt GTFS file.
 * 
 * @author SkiBu Smith
 *
 */
public class GtfsStopsWriter extends GtfsWriterBase {

	private static DecimalFormat sixDigitFormatter = 
			new DecimalFormat("0.000000");

	/********************** Member Functions **************************/

	/**
	 * Creates file writer and writes the header. 
	 * 
	 * @param fileName
	 */
	public GtfsStopsWriter(String fileName) {
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
	    writer.append("stop_id,stop_code,stop_name,stop_desc,stop_lat," +
	    		"stop_lon,zone_id,stop_url,location_type,parent_station," +
	    		"stop_timezone,wheelchair_boarding\n");
	}
	
	/**
	 * Writes a GtfsStop to the file
	 * 
	 * @param gtfsStop
	 */
	public void write(GtfsStop gtfsStop) {
		try {
			// Write out the gtfsStop
			append(gtfsStop.getStopId()).append(',');
			append(gtfsStop.getStopCode()).append(',');
			append(gtfsStop.getStopName()).append(',');
			append(gtfsStop.getStopDesc()).append(',');
			append(sixDigitFormatter.format(gtfsStop.getStopLat())).append(',');
			append(sixDigitFormatter.format(gtfsStop.getStopLon())).append(',');
			append(gtfsStop.getZoneId()).append(',');
			append(gtfsStop.getStopUrl()).append(',');
			append(gtfsStop.getLocationType()).append(',');
			append(gtfsStop.getParentStation()).append(',');
			append(gtfsStop.getStopTimezone()).append(',');
			append(gtfsStop.getWheelchairBoarding()).append('\n');
		} catch (IOException e) {
			// Only expect to run this in batch mode so don't really
			// need to log an error using regular logging. Printing
			// stack trace should suffice.
		     e.printStackTrace();
		}
	}

}
