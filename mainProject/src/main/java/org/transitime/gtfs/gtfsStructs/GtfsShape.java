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
package org.transitime.gtfs.gtfsStructs;

import org.apache.commons.csv.CSVRecord;
import org.transitime.db.structs.Location;


/**
 * A GTFS shapes object.
 * 
 * @author SkiBu Smith
 *
 */
public class GtfsShape extends GtfsBase implements Comparable<GtfsShape> {

	private final String shapeId;
	private final double shapePtLat;
	private final double shapePtLon;
	private final int shapePtSequence;
	private final Double shapeDistTraveled;
	
	/********************** Member Functions **************************/

	/**
	 * Creates a GtfsShape object by reading the data
	 * from the CSVRecord.
	 * @param record
	 * @param supplemental
	 * @param fileName for logging errors
	 */
	public GtfsShape(CSVRecord record, boolean supplemental, String fileName) 
		throws NumberFormatException {
		super(record, supplemental, fileName);

		shapeId = getRequiredValue(record, "shape_id");
		shapePtLat = Double.parseDouble(getRequiredValue(record, "shape_pt_lat"));
		shapePtLon = Double.parseDouble(getRequiredValue(record, "shape_pt_lon"));
		shapePtSequence = Integer.parseInt(getRequiredValue(record, "shape_pt_sequence"));
		
		String distStr = getOptionalValue(record, "shape_dist_traveled");
		if (distStr != null)
			shapeDistTraveled = Double.parseDouble(distStr);
		else
			shapeDistTraveled = null;
	}
	
	/**
	 * Creates a copy of the GtfsShape but updates the latitude and longitude.
	 * Useful for transforming coordinates in China so that locations are
	 * properly displayed in maps.
	 * 
	 * @param original
	 * @param shapePtLat
	 * @param shapePtLon
	 */
	public GtfsShape(GtfsShape original, double shapePtLat, double shapePtLon) {
		// Copy the values from the original passed in
		super(original);
		this.shapeId = original.shapeId;
		this.shapePtSequence = original.shapePtSequence;
		this.shapeDistTraveled = original.shapeDistTraveled;
		
		// Set the new location
		this.shapePtLat = shapePtLat;
		this.shapePtLon = shapePtLon;
	}

	public String getShapeId() {
		return shapeId;
	}

	public double getShapePtLat() {
		return shapePtLat;
	}

	public double getShapePtLon() {
		return shapePtLon;
	}
	
	public Location getLocation() {
		return new Location(shapePtLat, shapePtLon);
	}

	public int getShapePtSequence() {
		return shapePtSequence;
	}

	public Double getShapeDistTraveled() {
		return shapeDistTraveled;
	}

	/**
	 * So can use Collections.sort() to sort an Array of GtfsStopTime objects by 
	 * stop sequence.
	 * 
	 * @param arg0
	 * @return
	 */
	@Override
	public int compareTo(GtfsShape arg0) {
		return getShapePtSequence() - arg0.getShapePtSequence();
	}

	@Override
	public String toString() {
		return "GtfsShape ["
				+ "lineNumber=" + lineNumber
				+ ", shapeId=" + shapeId 
				+ ", shapePtLat=" + shapePtLat 
				+ ", shapePtLon=" + shapePtLon
				+ ", shapePtSequence=" + shapePtSequence
				+ ", shapeDistTraveled=" + shapeDistTraveled + "]";
	}
	
}
