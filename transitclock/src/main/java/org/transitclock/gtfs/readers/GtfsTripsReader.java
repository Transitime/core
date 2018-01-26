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
package org.transitclock.gtfs.readers;

import java.text.ParseException;

import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.gtfs.gtfsStructs.GtfsTrip;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * GTFS reader for the trips.txt file
 *
 * @author SkiBu Smith
 *
 */
public class GtfsTripsReader extends CsvBaseReader<GtfsTrip> {

	public GtfsTripsReader(String dirName) {
		super(dirName, "trips.txt", true, false);
	}
	
	@Override
	public GtfsTrip handleRecord(CSVRecord record, boolean supplemental) 
			throws ParseException {
		if (GtfsData.tripNotFiltered(record.get("trip_id")) 
				&& GtfsData.routeNotFiltered(record.get("route_id")))
			return new GtfsTrip(record, supplemental, getFileName());
		else
			return null;
	}

}
