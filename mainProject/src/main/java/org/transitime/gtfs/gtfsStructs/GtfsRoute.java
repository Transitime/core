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

import net.jcip.annotations.Immutable;

import org.apache.commons.csv.CSVRecord;


/**
 * A GTFS routes object. 
 * 
 * Contains two extension columns beyond the GTFS spec. These are
 * rout.order and hidden. They are useful for the user interface.
 * They will usually be set using a supplemental routes file.
 * 
 * @author SkiBu Smith
 * 
 */
@Immutable
public class GtfsRoute extends GtfsBase {

	private final String routeId;
	private final String agencyId;
	private final String routeShortName;
	private final String routeLongName;
	private final String routeDesc;
	private final String routeType;
	private final String routeURL;
	private final String routeColor;
	private final String routeTextColor;
	// Extensions to GTFS spec:
	// routeOrder is used to order the routes. It is optional. If set to below 
	// 1,000 then the route will be ordered by route order with with this route 
	// at the beginning. If greater than 1,000,000 then the route will be at
	// the end of the list.
	private final Integer routeOrder; 
	private final Boolean hidden;
	private final Boolean remove;
	private final String unscheduledBlockSuffix;
	private final String parentRouteId;
	private final Integer breakTime;
	private final Double maxDistance;
	
	/********************** Member Functions **************************/

	/**
	 * Creates a GtfsRoute object by reading the data
	 * from the CSVRecord.
	 * @param record
	 * @param supplemental
	 * @param fileName for logging errors
	 */
	public GtfsRoute(CSVRecord record, boolean supplementalFile, String fileName) {
		super(record, supplementalFile, fileName);
		
		routeId = getRequiredUnlessSupplementalValue(record, "route_id");
		agencyId = getRequiredUnlessSupplementalValue(record, "agency_id");
		routeShortName = getOptionalValue(record, "route_short_name");
		routeLongName = getOptionalValue(record, "route_long_name");
		routeDesc = getOptionalValue(record, "route_desc");
		routeType = getRequiredUnlessSupplementalValue(record, "route_type");
		routeURL = getOptionalValue(record, "route_url");
		routeColor = getOptionalValue(record, "route_color");
		routeTextColor = getOptionalValue(record, "route_text_color");
		
		// Note: route_order is an extra column not defined in GTFS spec.
		// It is useful for supplemental files because it enables the routes
		// to be ordered in a more user friendly way. Transit agencies often
		// don't order the routes in the routes.txt file.
		routeOrder = getOptionalIntegerValue(record, "route_order");
		
		// hidden is also an extra column not defined in GTFS spec.
		// Useful for supplemental files because allows one to hide
		// a particular stop from the public.
		hidden = getOptionalBooleanValue(record, "hidden");
		
		remove = getOptionalBooleanValue(record, "remove");
		
		unscheduledBlockSuffix = getOptionalValue(record, "unscheduled_block_suffix");
		
		parentRouteId = getOptionalValue(record, "parent_route_id");
		
		breakTime = getOptionalIntegerValue(record, "break_time");
		
		maxDistance = getOptionalDoubleValue(record, "max_distance");
	}
	
	/**
	 * When combining a regular route with a supplemental route need to
	 * create a whole new object since this class is Immutable to make
	 * it safer to use.
	 * @param originalRoute
	 * @param supplementRoute
	 */
	public GtfsRoute(GtfsRoute originalRoute, GtfsRoute supplementRoute) {
		super(originalRoute);
		
		// Use short variable names
		GtfsRoute o = originalRoute;
		GtfsRoute s = supplementRoute;
		
		routeId = originalRoute.routeId;
		agencyId = s.agencyId == null ? o.agencyId : s.agencyId;
		routeShortName = s.routeShortName == null ? o.routeShortName : s.routeShortName;
		routeLongName = s.routeLongName == null ? o.routeLongName : s.routeLongName;
		routeDesc = s.routeDesc == null ? o.routeDesc : s.routeDesc;
		routeType = s.routeType == null ? o.routeType : s.routeType;
		routeURL = s.routeURL == null ? o.routeURL : s.routeURL;
		routeColor = s.routeColor == null ? o.routeColor : s.routeColor;
		routeTextColor = s.routeTextColor == null ? o.routeTextColor : s.routeTextColor;
		routeOrder = s.routeOrder == null ? o.routeOrder : s.routeOrder;
		hidden = s.hidden == null ? o.hidden : s.hidden;
		remove = s.remove == null ? o.remove : s.remove;
		unscheduledBlockSuffix = s.unscheduledBlockSuffix == null ? 
				o.unscheduledBlockSuffix : s.unscheduledBlockSuffix;
		parentRouteId = s.parentRouteId == null ? o.parentRouteId : s.parentRouteId;
		breakTime = s.breakTime == null ? o.breakTime : s.breakTime;
		maxDistance = s.maxDistance == null ? o.maxDistance : s.maxDistance;
	}
	
	public String getRouteId() {
		return routeId;
	}
	
	public String getAgencyId() {
		return agencyId;
	}

	/**
	 * Returns the route short name. Can be null.
	 * 
	 * @return
	 */
	public String getRouteShortName() {
		return routeShortName;
	}

	public String getRouteLongName() {
		return routeLongName;
	}

	public String getRouteDesc() {
		return routeDesc;
	}

	public String getRouteType() {
		return routeType;
	}

	public String getRouteURL() {
		return routeURL;
	}

	public String getRouteColor() {
		return routeColor;
	}

	public String getRouteTextColor() {
		return routeTextColor;
	}

	/**
	 * Returns optional route_order or null if not set.
	 * @return
	 */
	public Integer getRouteOrder() {
		return routeOrder;
	}

	/**
	 * Some agencies create several entries for a single route. For example,
	 * they might have a 3 different trip patterns so they use 3 separate 
	 * routes, but they have the same title. In this case the routes should
	 * be combined.
	 * @return route_id of the parent route
	 */
	public String getParentRouteId() {
		return parentRouteId;
	}
	
	/**
	 * Returns if route should be hidden from user interface
	 * for the public.
	 * @return
	 */
	public boolean getHidden() {
		// hidden is optional so can be null. Therefore need
		// to handle specially
		return hidden!=null ? hidden : false;
	}
	
	/**
	 * Returns if route should be removed from configuration so it
	 * doesn't show up at all
	 * @return
	 */
	public boolean shouldRemove() {
		// remove is optional so can be null. Therefore need
		// to handle specially
		return remove!=null ? remove : false;

	}
	
	/**
	 * Returns true if should create unscheduled block assignments for this
	 * route.
	 * 
	 * @return
	 */
	public boolean shouldCreateUnscheduledBlock() { 
		return unscheduledBlockSuffix != null;
	}
	
	/**
	 * Returns suffix to use if should create unscheduled block assignments for
	 * this route.
	 * 
	 * @return The suffix to use for the block assignments
	 */
	public String getUnscheduledBlockSuffix() {
		return unscheduledBlockSuffix;
	}
	
	/**
	 * Returns the minimum time that drivers get as a break time for layovers
	 * for the route. Affects predictions.
	 * 
	 * @return The break time, or null if not set for route
	 */
	public Integer getBreakTime() {
		return breakTime;
	}
	
	/**
	 * For specifying on a per route basis how far AVL report can be from
	 * segment and still have it be considered a match.
	 * 
	 * @return
	 */
	public Double getMaxDistance() {
		return maxDistance;
	}
	
	@Override
	public String toString() {
		return "GtfsRoute ["
				+ "lineNumber=" + lineNumber 
				+ ", routeId="	+ routeId 
				+ ", agencyId=" + agencyId 
				+ ", routeShortName=" + routeShortName 
				+ ", routeLongName=" + routeLongName
				+ ", routeDesc=" + routeDesc 
				+ ", routeType=" + routeType
				+ ", routeURL=" + routeURL 
				+ ", routeColor=" + routeColor
				+ ", routeTextColor=" + routeTextColor 
				+ ", routeOrder=" + routeOrder 
				+ ", hidden=" + hidden
				+ ", unscheduledBlockSuffix=" + unscheduledBlockSuffix
				+ ", breakTime=" + breakTime
				+ ", maxDistance=" + maxDistance
				+ "]";
	}
	
	
}
