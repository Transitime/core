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

package org.transitime.custom.sfmta.delayTimes;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.structs.Location;
import org.transitime.db.structs.Vector;
import org.transitime.utils.Geo;

/**
 * Defines the two vectors that define an intersection. The vector segment
 * should be the block before the intersection and the second one should be the
 * block from the intersection to the end of the block after the intersection.
 * 
 * @author SkiBu Smith
 *
 */
public class Intersection {

	public String name;
	public double lat1, lon1, latStop, lonStop, lat2, lon2;
	
	// Must be within 40m of path to match
	private final static double ALLOWABLE_DISTANCE = 40.0;
	
	private static final Logger logger = LoggerFactory
			.getLogger(Intersection.class);

	/********************** Member Functions **************************/

	/**
	 * Returns distance from lat,lon to the first path segment. Returns NaN if
	 * the match is to before or after the vector or if the distance from the
	 * vector is greater than allowableDistance.
	 * 
	 * @param loc
	 * @return
	 */
	public double matchToV1(Loc loc) {
		Location location = new Location(loc.lat, loc.lon);
		Location l1 = new Location(lat1, lon1);
		Location lStop = new Location(latStop, lonStop);
		Vector v1 = new Vector(l1, lStop);
		double distanceToV1 = Geo.distanceIfMatch(location, v1);
		if (!Double.isNaN(distanceToV1) && distanceToV1 < ALLOWABLE_DISTANCE) {
			// Matches to v1
			return Geo.matchDistanceAlongVector(location, v1);
		} else {
			// Didn't match
			return Double.NaN;
		}
	}
	
	/**
	 * Returns distance from lat,lon to the second path segment. Returns NaN if
	 * the match is to before or after the vector or if the distance from the
	 * vector is greater than allowableDistance.
	 * 
	 * @param loc
	 * @return
	 */
	public double matchToV2(Loc loc) {
		Location location = new Location(loc.lat, loc.lon);
		Location lStop = new Location(latStop, lonStop);
		Location l2 = new Location(lat2, lon2);
		Vector v2 = new Vector(lStop, l2);
		double distanceToV2 = Geo.distanceIfMatch(location, v2);
		if (!Double.isNaN(distanceToV2) && distanceToV2 < ALLOWABLE_DISTANCE) {
			// Matches to v1
			return Geo.matchDistanceAlongVector(location, v2);
		} else {
			// Didn't match
			return Double.NaN;
		}
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
	
	private static Intersection getIntersection(CSVRecord record) {
		Intersection i = new Intersection();
		i.name = getValue(record, "name");
		i.lat1 = Double.parseDouble(getValue(record, "lat1"));
		i.lon1 = Double.parseDouble(getValue(record, "lon1"));
		i.latStop = Double.parseDouble(getValue(record, "latStop"));
		i.lonStop = Double.parseDouble(getValue(record, "lonStop"));
		i.lat2 = Double.parseDouble(getValue(record, "lat2"));
		i.lon2 = Double.parseDouble(getValue(record, "lon2"));
		
		return i;
	}

	public static List<Intersection> readIntersections(String fileName) {
		List<Intersection> intersections = new ArrayList<Intersection>();
		
		try {
			Reader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileName), "UTF-8"));
			CSVFormat formatter = CSVFormat.DEFAULT.withHeader().
					withCommentMarker('-');

			// Parse the file
			Iterable<CSVRecord> records = formatter.parse(in);
			Iterator<CSVRecord> iterator = records.iterator();
			while (iterator.hasNext()) {
				// Determine the record to process
				CSVRecord record = iterator.next();
				Intersection i = getIntersection(record);
				intersections.add(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return intersections;
	}
	
}
