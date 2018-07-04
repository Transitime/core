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

package org.transitclock.avl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.AvlReport.AssignmentType;
import org.transitclock.utils.ChinaGpsOffset;
import org.transitclock.utils.Geo;
import org.transitclock.utils.Time;
import org.transitclock.utils.ChinaGpsOffset.LatLon;
import org.transitclock.utils.csv.CsvWriterBase;

/**
 * For writing a CSV file containing AVL reports.
 *
 * @author SkiBu Smith
 *
 */
public class AvlCsvWriter extends CsvWriterBase {

	// Needed so can output times in proper timezone
	private final Time timeUsingTimeZone;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(AvlCsvWriter.class);


	/********************** Member Functions **************************/

	/**
	 * Simple constructor.
	 * 
	 * @param fileName
	 * @param timezoneStr
	 *            For outputting time in proper timezone. If null then will
	 *            output time in local time.
	 */
	public AvlCsvWriter(String fileName, String timezoneStr) {
		super(fileName, false);
		
		timeUsingTimeZone = new Time(timezoneStr);
	}


	/* (non-Javadoc)
	 * @see org.transitclock.utils.csv.CsvWriterBase#writeHeader()
	 */
	@Override
	protected void writeHeader() throws IOException {
	    // Write the header
	    writer.append("vehicleId,time,justTime,latitude,longitude,speed,heading," +
	    		"assignmentId,assignmentType\n");		
	}

	/**
	 * Appends an AvlReport to the CSV file.
	 * 
	 * @param avlReport
	 *            The AvlReport to be appended to the CSV file
	 * @param transformForChinaMap
	 *            If true then will convert lat/lon so that can be displayed on
	 *            map of China.
	 */
	public void write(AvlReport avlReport, boolean transformForChinaMap) {
		try {
			// Write out the GtfsShape
			append(avlReport.getVehicleId());
			append(',');
			
			append(timeUsingTimeZone.dateTimeStrMsecForTimezone(
					avlReport.getTime()));
			append(',');
			
			append(timeUsingTimeZone.timeStrForTimezone(avlReport.getTime()));
			append(',');
			
			// Determine lat/lon. Offset for use in map of China if necessary.
			double lat = avlReport.getLat();
			double lon = avlReport.getLon();
			if (transformForChinaMap) {
				LatLon offsetLatLon = ChinaGpsOffset.transform(lat, lon);
				lat = offsetLatLon.getLat();
				lon = offsetLatLon.getLon();				
			}
			append(Geo.format(lat));
			append(',');
			
			append(Geo.format(lon));
			append(',');
			
			if (!Float.isNaN(avlReport.getSpeed()))
				append(Geo.oneDigitFormat(avlReport.getSpeed()));
			append(',');
	
			if (!Float.isNaN(avlReport.getHeading()))
				append(Geo.oneDigitFormat(avlReport.getHeading()));
			append(',');

			if (avlReport.getAssignmentId() != null)
				append(avlReport.getAssignmentId());
			append(',');
			
			// Add the assignment type using the name of the enumeration
			AssignmentType assignmentType = avlReport.getAssignmentType();
			if (assignmentType != null) {
				append(assignmentType.name());
			}

			// Wrap up the record
			append('\n');
		} catch (IOException e) {
			logger.error("Error writing {}.", avlReport, e);
		}
	}

	/**
	 * Appends an AvlReport to the CSV file.
	 * 
	 * @param avlReport
	 *            The AvlReport to be appended to the CSV file
	 */
	public void write(AvlReport avlReport) {
		write(avlReport, false);
	}
}
