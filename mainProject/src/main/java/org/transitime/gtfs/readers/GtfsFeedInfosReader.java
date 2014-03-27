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
import org.transitime.gtfs.gtfsStructs.GtfsFeedInfo;

/**
 * GTFS reader for the feed_info.txt file
 * 
 * @author SkiBu Smith
 *
 */
public class GtfsFeedInfosReader extends GtfsBaseReader<GtfsFeedInfo> {

	public GtfsFeedInfosReader(String dirName) {
		super(dirName, "feed_info.txt", false, false);
	}
	
	@Override
	public GtfsFeedInfo handleRecord(CSVRecord record, boolean supplemental) 
			throws ParseException {
		return new GtfsFeedInfo(record, supplemental, getFileName());
	}

}
