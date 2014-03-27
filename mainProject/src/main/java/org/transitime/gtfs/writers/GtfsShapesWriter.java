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

import org.transitime.gtfs.gtfsStructs.GtfsShape;

/**
 * Writes out a GTFS shapes.txt file
 * 
 * @author SkiBu Smith
 *
 */
public class GtfsShapesWriter extends GtfsWriterBase {
	
	private static DecimalFormat sixDigitFormatter = 
			new DecimalFormat("0.000000");
	
	/********************** Member Functions **************************/
	
	/**
	 * Creates file writer and writes the header. 
	 * 
	 * @param fileName
	 */
	public GtfsShapesWriter(String fileName) {
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
	    writer.append("shape_id,shape_pt_lat,shape_pt_lon,shape_pt_sequence," +
	    		"shape_dist_traveled\n");
	}
	
	public void write(GtfsShape gtfsShape) {
		try {
			// Write out the GtfsShape
			append(gtfsShape.getShapeId()).append(',');
			append(sixDigitFormatter.format(gtfsShape.getLocation().getLat()));
			append(',');
			append(sixDigitFormatter.format(gtfsShape.getLocation().getLon()));
			append(',');
			append(gtfsShape.getShapePtSequence()).append(',');
			append(gtfsShape.getShapeDistTraveled()).append('\n');
		} catch (IOException e) {
			// Only expect to run this in batch mode so don't really
			// need to log an error using regular logging. Printing
			// stack trace should suffice.
		     e.printStackTrace();
		}
	}
}
