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

package org.transitime.misc.sfmta;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.structs.Location;
import org.transitime.utils.StringUtils;

/**
 * A structure that represents a location datapoint
 *
 * @author SkiBu Smith
 *
 */
public class Loc {

	public String ipAddress;
	public double lat, lon, accuracy;
	public long epochTime;
	
	// Only use datapoints where accuracy supposed to be at least
	// 75m. For iPhone when outside the accuracy seems to always be
	// specified as 65m.
	private static final double MAX_ALLOWED_ACCURACY = 75.0;
	
	private static final Logger logger = LoggerFactory.getLogger(Loc.class);

	/********************** Member Functions **************************/

	public Location getLocation() {
		return new Location(lat, lon);
	}
	
	private static String getValue(CSVRecord record, String name) {
		if (!record.isSet(name)) {
			logger.error("Column {} not defined", name);	
			return null;
		}
		
		// Get the value. First trim whitespace so that
		// value will be consistent. 
		String value = record.get(name).trim();
		return value;
	}
	
	private static Loc getLoc(CSVRecord record) {
		Loc loc = new Loc();
		
		loc.ipAddress = getValue(record, "ip");
		loc.lat = Double.parseDouble(getValue(record, "lat"));
		loc.lon = Double.parseDouble(getValue(record, "lon"));
		loc.accuracy = Double.parseDouble(getValue(record, "accuracy"));
		loc.epochTime = Long.parseLong(getValue(record, "epoch"));
		
		return loc;
	}
	
	public static List<Loc> readLocs(String fileName) {
		List<Loc> locs = new ArrayList<Loc>();
		
		try {
			Reader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileName), "UTF-8"));
			CSVFormat formatter = 
					CSVFormat.DEFAULT.withHeader().withCommentStart('-');
			
			// Parse the file
			Iterable<CSVRecord> records = formatter.parse(in);
			Iterator<CSVRecord> iterator = records.iterator();
			while (iterator.hasNext()) {
				// Determine the record to process
				CSVRecord record = iterator.next();
				Loc loc = getLoc(record);
				if (loc.accuracy < MAX_ALLOWED_ACCURACY)
				locs.add(loc);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return locs;
	}

	@Override
	public String toString() {
		return "Loc [latLon=" + StringUtils.sixDigitFormat(lat) 
					+ ", " + StringUtils.sixDigitFormat(lon)
				+ ", time=" + new Date(epochTime)
				+ ", epochTime=" + epochTime
				+ ", accuracy=" + accuracy 
				+ ", ipAddress=" + ipAddress +
				"]";
	}
}
