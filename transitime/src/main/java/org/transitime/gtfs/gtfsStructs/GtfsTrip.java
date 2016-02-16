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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVRecord;
import org.transitime.config.StringConfigValue;
import org.transitime.utils.csv.CsvBase;

/**
 * A GTFS trip object as read from the trips.txt GTFS file.
 * 
 * @author SkiBu Smith
 *
 */
public class GtfsTrip extends CsvBase {

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
		
	// For determining a trip_short_name from the trip_id if the 
	// trip_short_name is not specified in GTFS file.
	// Default of null means simply use trip_id without any modification.
	private static StringConfigValue tripShortNameRegEx = new StringConfigValue(
			"transitime.gtfs.tripShortNameRegEx", 
			null,
			"For agencies where trip short name not specified can use this "
			+ "regular expression to determine the short name from the trip "
			+ "ID by specifying a grouping. For example, to get name before "
			+ "a \"-\" would use something like \"(.*?)-\"");
	private static Pattern tripShortNameRegExPattern = null;
	
	// For determining proper block_id that corresponds to AVL feed
	// Default of null means simply use block_id without any modification.
	private static StringConfigValue blockIdRegEx = new StringConfigValue(
			"transitime.gtfs.blockIdRegEx", 
			null,
			"For agencies where block ID from GTFS datda needs to be modified "
			+ "to match that of the AVL feed. Can use this "
			+ "regular expression to determine the proper block ID "
			+ " by specifying a grouping. For example, to get name after "
			+ "a \"xx-\" would use something like \"xx-(.*)\"");
	private static Pattern blockIdRegExPattern = null;
	
	
	/********************** Member Functions **************************/

	/**
	 * Creates a GtfsTrip object from scratch
	 */
	public GtfsTrip(String routeId, String serviceId, String tripId,
			String tripHeadsign, String tripShortName, String directionId,
			String blockId, String shapeId) {
		this.routeId = routeId;
		this.serviceId = serviceId;
		this.tripId = tripId;
		this.tripHeadsign = tripHeadsign;
		this.tripShortName = tripShortName;
		this.directionId = directionId;
		this.blockId = blockId;
		this.shapeId = shapeId;

		this.wheelchairAccessible = null;
		this.bikesAllowed = null;
	}	

	/**
	 * Creates a GtfsTrip object by reading the data from the CSVRecord.
	 * 
	 * @param record
	 * @param supplemental
	 * @param fileName
	 *            for logging errors
	 */
	public GtfsTrip(CSVRecord record, boolean supplemental, String fileName) {
		super(record, supplemental, fileName);
		
		routeId = getRequiredUnlessSupplementalValue(record, "route_id");
		serviceId = getRequiredUnlessSupplementalValue(record, "service_id");
		tripId = getRequiredUnlessSupplementalValue(record, "trip_id");
		tripHeadsign = getOptionalValue(record, "trip_headsign");
		
		// Trip short name is a bit more complicated. For a regular 
		// non-supplemental trips.txt file want to use the trip_short_name
		// if it is specified but otherwise use the trip_name or use a 
		// regular expression on the trip_id to determine it. Hence,
		// getTripShortName() is called to determine the trip short name
		// for a non-supplemental file. But for a supplemental file only
		// want to use a trip_short_name if it is specified. This way won't
		// overwrite the trip short name from the regular trips.txt file
		// unless the trip_short_name is explicitly specified in the 
		// supplemental trips.txt file.
		tripShortName =
				supplemental ? getOptionalValue(record, "trip_short_name")
						: getTripShortName(
								getOptionalValue(record, "trip_short_name"),
								tripId);
		
		directionId = getOptionalValue(record, "direction_id");
		blockId = getBlockId(getOptionalValue(record, "block_id"));
		shapeId = getOptionalValue(record, "shape_id");
		String wheelchairAccessibleStr = getOptionalValue(record, "wheelchair_accessible");
		wheelchairAccessible = wheelchairAccessibleStr == null ? 
				null : Integer.parseInt(wheelchairAccessibleStr);
		String bikesAllowedStr = getOptionalValue(record, "bikes_allowed");
		bikesAllowed = bikesAllowedStr == null ? 
				null : Integer.parseInt(bikesAllowedStr);
	}	

	/**
	 * @param routeId
	 * @param serviceId
	 * @param tripId
	 * @param tripHeadsign
	 * @param tripShortName
	 * @param directionId
	 * @param blockId
	 * @param shapeId
	 * @param wheelchairAccessible
	 * @param bikesAllowed
	 */
	public GtfsTrip(String routeId, String serviceId, String tripId,
			String tripHeadsign, String tripShortName, String directionId,
			String blockId, String shapeId, Integer wheelchairAccessible,
			Integer bikesAllowed) {
		super();
		this.routeId = routeId;
		this.serviceId = serviceId;
		this.tripId = tripId;
		this.tripHeadsign = tripHeadsign;
		this.tripShortName = tripShortName;
		this.directionId = directionId;
		this.blockId = blockId;
		this.shapeId = shapeId;
		this.wheelchairAccessible = wheelchairAccessible;
		this.bikesAllowed = bikesAllowed;
	}
	
	/**
	 * When combining a regular trip with a supplemental trip need to create a
	 * whole new object since this class is Immutable to make it safer to use.
	 * 
	 * @param originalTrip
	 * @param supplementTrip
	 */
	public GtfsTrip(GtfsTrip originalTrip, GtfsTrip supplementTrip) {
		super(originalTrip);
		
		// Use short variable names
		GtfsTrip o = originalTrip;
		GtfsTrip s = supplementTrip;
		
		tripId = s.tripId == null ? o.tripId : s.tripId;		
		routeId = s.routeId == null ? o.routeId : s.routeId;
		serviceId = s.serviceId == null ? o.serviceId : s.serviceId;
		tripHeadsign = s.tripHeadsign == null ? o.tripHeadsign : s.tripHeadsign;
		tripShortName = s.tripShortName == null ? o.tripShortName : s.tripShortName;
		directionId = s.directionId == null ? o.directionId : s.directionId;
		blockId = s.blockId == null ? o.blockId : s.blockId;
		shapeId = s.shapeId == null ? o.shapeId : s.shapeId;
		wheelchairAccessible = s.wheelchairAccessible == null ? o.wheelchairAccessible : s.wheelchairAccessible;
		bikesAllowed = s.bikesAllowed == null ? o.bikesAllowed : s.bikesAllowed;
	}
	
	/**
	 * Creates a GtfsTrip object but only with the tripShortName and blockId
	 * set. This is useful for creating a supplemental trips.txt file that
	 * contains only block ID information.
	 * 
	 * @param tripShortName
	 * @param blockId
	 */
	public GtfsTrip(String tripShortName, String blockId) {
		// Creating supplemental data so can call default constructor
		// since line number, filename, etc are not valid.
		super();
		
		this.routeId = null;
		this.serviceId = null;
		this.tripId = null;
		this.tripHeadsign = null;
		this.tripShortName = tripShortName;
		this.directionId = null;
		this.blockId = blockId;
		this.shapeId = null;
		this.wheelchairAccessible = null;
		this.bikesAllowed = null;
	}
	
	/**
	 * Many agencies don't specify a trip_short_name. For these use the trip_id
	 * or if the transitime.gtfs.tripShortNameRegEx is set to determine a group
	 * then use that group in the tripId. For example, if tripId is
	 * "345-long unneeded description" and the regex is set to "(.*?)-" then
	 * returned trip short name will be 345.
	 * 
	 * @param tripShortName
	 * @param tripId
	 * @return The tripShortName if it is not null. Other returns a the first
	 *         group specified by the regex on tripId, or the tripId if no regex
	 *         defined or if no match.
	 */
	private static String getTripShortName(String tripShortName, String tripId) {
		// If tripShortName provided then use it
		if (tripShortName != null)
			return tripShortName;
		
		// The tripShortName wasn't provided. If a regular expression specified
		// then use it. If regex not specified then return tripId.
		if (tripShortNameRegEx.getValue() == null)
			return tripId;
		
		// Initialize the pattern if need to, but do so just once
		if (tripShortNameRegExPattern == null)
			tripShortNameRegExPattern = Pattern.compile(tripShortNameRegEx.getValue());
		
		// Create the matcher
		Matcher m = tripShortNameRegExPattern.matcher(tripId);
		
		// If insufficient match then return tripId
		if (!m.find())
			return tripId;
		if (m.groupCount() < 1)
			return tripId;
		
		// Return the first group. Note: group #0 is the entire string. Need to 
		// use group #1 for the group match.
		return m.group(1);
	}
	
	/**
	 * In case block IDs from GTFS needs to be modified to match block IDs from
	 * AVL feed. Uses the property transitime.gtfs.blockIdRegEx if it is not
	 * null.
	 * 
	 * @param originalBlockId
	 * @return the processed block ID
	 */
	public static String getBlockId(String originalBlockId) {
		// If nothing to do then return original value
		if (originalBlockId == null)
			return originalBlockId;		
		if (blockIdRegEx.getValue() == null)
			return originalBlockId;
		
		// Initialize the pattern if need to, but do so just once
		if (blockIdRegExPattern == null)
			blockIdRegExPattern = Pattern.compile(blockIdRegEx.getValue());
		
		// Create the matcher
		Matcher m = blockIdRegExPattern.matcher(originalBlockId);
		
		// If insufficient match return original value
		if (!m.find() || m.groupCount() < 1)
			return originalBlockId;
		
		// Return the first group. Note: group #0 is the entire string. Need to 
		// use group #1 for the group match.
		return m.group(1);
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
