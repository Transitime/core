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

/**
 * A GTFS trip object.
 * 
 * @author SkiBu Smith
 *
 */
public class GtfsTrip extends GtfsBase {

	private final String routeId;
	private final String serviceId;
	private final String tripId;
	private final String tripHeadsign;
	private final String tripShortName;
	private final String directionId;
	private final String blockId;
	private final String shapeId;
	private final Integer wheelchairAccessible;
	private final Integer bikesAllowed;
		
	/********************** Member Functions **************************/

	/**
	 * Creates a GtfsTrip object by reading the data
	 * from the CSVRecord.
	 * @param record
	 * @param supplemental
	 * @param fileName for logging errors
	 */
	public GtfsTrip(CSVRecord record, boolean supplemental, String fileName) {
		super(record, supplemental, fileName);
		
		routeId = getRequiredValue(record, "route_id");
		serviceId = getRequiredValue(record, "service_id");
		tripId = getRequiredValue(record, "trip_id");
		tripHeadsign = getOptionalValue(record, "trip_headsign");
		tripShortName = getOptionalValue(record, "trip_short_name");
		directionId = getOptionalValue(record, "direction_id");
		blockId = getOptionalValue(record, "block_id");
		shapeId = getOptionalValue(record, "shape_id");
		String wheelchairAccessibleStr = getOptionalValue(record, "wheelchair_accessible");
		wheelchairAccessible = wheelchairAccessibleStr == null ? 
				null : Integer.parseInt(wheelchairAccessibleStr);
		String bikesAllowedStr = getOptionalValue(record, "bikes_allowed");
		bikesAllowed = bikesAllowedStr == null ? 
				null : Integer.parseInt(bikesAllowedStr);
	}
	
	public String getRouteId() {
		return routeId;
	}
	public String getServiceId() {
		return serviceId;
	}
	public String getTripId() {
		return tripId;
	}
	/**
	 * @return trip_headsign from trips.txt. This element is optional
	 * so can return null.
	 */
	public String getTripHeadsign() {
		return tripHeadsign;
	}
	public String getTripShortName() {
		return tripShortName;
	}
	public String getDirectionId() {
		return directionId;
	}
	public String getBlockId() {
		return blockId;
	}
	public String getShapeId() {
		return shapeId;
	}
	public Integer getWheelchairAccessible() {
		return wheelchairAccessible;
	}
	
	public Integer getBikesAllowed() {
		return bikesAllowed;
	}
	
	@Override
	public String toString() {
		return "GtfsTrip ["
				+ "lineNumber=" + lineNumber + ", "
				+ (routeId != null ? "routeId=" + routeId + ", " : "")
				+ (serviceId != null ? "serviceId=" + serviceId + ", " : "")
				+ (tripId != null ? "tripId=" + tripId + ", " : "")
				+ (tripHeadsign != null ? "tripHeadsign=" + tripHeadsign
						+ ", " : "")
				+ (tripShortName != null ? "tripShortName=" + tripShortName
						+ ", " : "")
				+ (directionId != null ? "directionId=" + directionId + ", "
						: "")
				+ (blockId != null ? "blockId=" + blockId + ", " : "")
				+ (shapeId != null ? "shapeId=" + shapeId + ", " : "")
				+ (wheelchairAccessible != null ? 
						"wheelchairAccessible="	+ wheelchairAccessible : "") 
				+ (bikesAllowed != null ? 
						"bikesAllowed="	+ bikesAllowed : "") 
				+ "]";
	}
	
}
