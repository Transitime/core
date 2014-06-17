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
package org.transitime.gtfs.readers;

import java.text.ParseException;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.transitime.gtfs.gtfsStructs.GtfsAgency;
import org.transitime.utils.csv.CsvBaseReader;


/**
 * GTFS reader for the agency.txt file
 * 
 * @author SkiBu Smith
 *
 */
public class GtfsAgencyReader extends CsvBaseReader<GtfsAgency> {

	public GtfsAgencyReader(String dirName) {
		super(dirName, "agency.txt", true, false);
	}
	
	@Override
	public GtfsAgency handleRecord(CSVRecord record, boolean supplemental) 
		throws ParseException {
		return new GtfsAgency(record, supplemental, getFileName());
	}

	/**
	 * A convenience method for reading the timezone from the GTFS agency.txt
	 * file. If there are multiple agencies the timezone from the first agency
	 * is used. The agency.txt file is read every time this method is called.
	 * 
	 * @param gtfsDirectoryName
	 * @return The timezone for the agency, or null if not available
	 */
	public static String readTimezoneString(String gtfsDirectoryName) {
		GtfsAgencyReader agencyReader = new GtfsAgencyReader(gtfsDirectoryName);
		List<GtfsAgency> gtfsAgencies = agencyReader.get();
		if (gtfsAgencies == null || gtfsAgencies.isEmpty())
			return null;
		GtfsAgency firstAgency = gtfsAgencies.get(0);
		return firstAgency.getAgencyTimezone();
	}
}
