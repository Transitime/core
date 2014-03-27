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

import org.apache.commons.csv.CSVRecord;
import org.transitime.gtfs.gtfsStructs.GtfsStop;


/**
 * GTFS reader for the stops.txt file
 * @author SkiBu Smith
 *
 */
public class GtfsStopsSupplementReader extends GtfsBaseReader<GtfsStop> {

	public GtfsStopsSupplementReader(String dirName) {
		super(dirName, "stops.txt", false, true);
	}
	
	@Override
	public GtfsStop handleRecord(CSVRecord record, boolean supplemental) 
			throws ParseException {
		return new GtfsStop(record, supplemental, getFileName());
	}

}

