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
import org.transitime.gtfs.gtfsStructs.GtfsRoute;

/**
 * GTFS reader for the routeSupplement.txt file
 * 
 * @author SkiBu Smith
 *
 */
public class GtfsRoutesSupplementReader extends GtfsBaseReader<GtfsRoute> {

	public GtfsRoutesSupplementReader(String dirName) {
		super(dirName, "routes.txt", false, true);
	}
	
	@Override
	public GtfsRoute handleRecord(CSVRecord record, boolean supplemental) 
			throws ParseException {
		return new GtfsRoute(record, supplemental, getFileName());
	}
	
}

