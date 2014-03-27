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
 * A GTFS stops object.
 * 
 * Contains two extension columns beyond the GTFS spec, hidden
 * and adherenc.stop. They are useful for the user interface.
 * They will usually be set using a supplemental stops file.
 *
 * @author SkiBu Smith
 *
 */
public class GtfsStop extends GtfsBase {

	private final String stopId;
	private final Integer stopCode;
	private final String stopName;
	private final String stopDesc;
	private final double stopLat;
	private final double stopLon;
	private final String zoneId;
	private final String stopUrl;
	private final String locationType;
	private final String parentStation;
	private final String stopTimezone;
	private final String wheelchairBoarding;
	
	// Extensions to GTFS spec:

	// If should generate special ScheduleAdherence data for this stop
	private final Boolean adherenceStop;
	// Indicates that vehicle can leave route path before departing this stop
	// since the driver is taking a break.
	private final Boolean layoverStop;
	// Indicates that vehicle is not supposed to depart the stop until the
	// scheduled departure time.
	private final Boolean waitStop;
	// Indicates if stop should be hidden from public
	private final Boolean hidden;

	/********************** Member Functions **************************/

	/**
	 * Creates a GtfsRoute object by reading the data
	 * from the CSVRecord.
	 * @param record
	 * @param supplemental
	 * @param fileName for logging errors
	 */
	public GtfsStop(CSVRecord record, boolean supplemental, String fileName) {
		super(record, supplemental, fileName);
		
		stopId = getRequiredValue(record, "stop_id");
		String stopCodeStr = getOptionalValue(record, "stop_code");
		if (stopCodeStr != null)
			stopCode = Integer.parseInt(stopCodeStr);
		else
			stopCode = null;
		stopName = getRequiredUnlessSupplementalValue(record, "stop_name");
		stopDesc = getOptionalValue(record, "stop_desc");
		String stopLatStr = getRequiredUnlessSupplementalValue(record, "stop_lat");
		stopLat = stopLatStr!=null ? Double.parseDouble(stopLatStr) : Double.NaN;
		String stopLonStr = getRequiredUnlessSupplementalValue(record, "stop_lon");
		stopLon = stopLonStr!=null ? Double.parseDouble(stopLonStr) : Double.NaN;
		zoneId = getOptionalValue(record, "zone_id");
		stopUrl = getOptionalValue(record, "stop_url");
		locationType = getOptionalValue(record, "location_type");
		parentStation = getOptionalValue(record, "parent_station");
		stopTimezone = getOptionalValue(record, "stop_timezone");
		wheelchairBoarding = getOptionalValue(record, "wheelchair_boarding");
		
		// adherenc.stop is an extra column not defined in GTFS spec.
		// Useful for supplemental files because allows one to specify
		// which stops should show schedule adherence in reports for.
		adherenceStop = getOptionalBooleanValue(record, "adhrence_stop");

		// layove.stop indicates that the driver is supposed to wait 
		// until the scheduled departure time before continuing and
		// can go off of the path since it is a layover.
		layoverStop = getOptionalBooleanValue(record, "layover_stop");
		
		// A wai.stop is when driver is supposed to wait until the
		// scheduled time before proceeding. Vehicle is expected
		// to stay on path.
		waitStop = getOptionalBooleanValue(record, "wait_stop");
		
		// hidden is also an extra column not defined in GTFS spec.
		// Useful for supplemental files because allows one to hide
		// a particular stop from the public.
		hidden = getOptionalBooleanValue(record, "hidden");
	}
	
	/**
	 * Creates a copy of the GtfsStop but updates the latitude and longitude.
	 * Useful for transforming coordinates in China so that locations are
	 * properly displayed in maps.
	 * 
	 * @param original
	 * @param shapePtLat
	 * @param shapePtLon
	 */
	public GtfsStop(GtfsStop original, double lat, double lon) {
		// Copy the values from the original passed in
		super(original);

		this.stopId = original.stopId;
		this.stopCode = original.stopCode;
		this.stopName = original.stopName;
		this.stopDesc = original.stopDesc;
		this.zoneId = original.zoneId;
		this.stopUrl = original.stopUrl;
		this.locationType = original.locationType;
		this.parentStation = original.parentStation;
		this.stopTimezone = original.stopTimezone;
		this.wheelchairBoarding = original.wheelchairBoarding;
		// Extensions to GTFS spec:
		this.adherenceStop = original.adherenceStop;
		this.layoverStop = original.layoverStop;
		this.waitStop = original.waitStop;
		this.hidden = original.hidden;
		
		// Set the new location
		this.stopLat = lat;
		this.stopLon = lon;
	}
	
	/**
	 * When combining a regular stop with a supplemental stop need to
	 * create a whole new object since this class is Immutable to make
	 * it safer to use.
	 * @param originalStop
	 * @param supplementStop
	 */
	public GtfsStop(GtfsStop originalStop, GtfsStop supplementStop) {
		super(originalStop);
		
		// Use short variable names
		GtfsStop o = originalStop;
		GtfsStop s = supplementStop;
		
		stopId = originalStop.stopId;
		stopCode = s.stopCode == null ? o.stopCode : s.stopCode;
		stopName = s.stopName == null ? o.stopName : s.stopName;
		stopDesc = s.stopDesc == null ? o.stopDesc : s.stopDesc;
		stopLat = o.stopLat; // Not optional so for now can't be null and can't be overriden
		stopLon = o.stopLon; // Not optional so for now can't be null and can't be overriden
		zoneId = s.zoneId == null ? o.zoneId : s.zoneId;
		stopUrl = s.stopUrl == null ? o.stopUrl : s.stopUrl;
		locationType = s.locationType == null ? o.locationType : s.locationType;
		parentStation = s.parentStation == null ? o.parentStation : s.parentStation;
		stopTimezone = s.stopTimezone == null ? o.stopTimezone : s.stopTimezone;
		wheelchairBoarding = s.wheelchairBoarding == null ? o.wheelchairBoarding : s.wheelchairBoarding;
		adherenceStop = s.adherenceStop == null ? o.adherenceStop : s.adherenceStop;
		layoverStop = s.layoverStop == null ? o.layoverStop : s.layoverStop;
		waitStop = s.waitStop == null ? o.waitStop : s.waitStop;
		hidden = s.hidden == null ? o.hidden : s.hidden;
	}

	public String getStopId() {
		return stopId;
	}
	
	public Integer getStopCode() {
		return stopCode;
	}
	
	public String getStopName() {
		return stopName;
	}
	
	public String getStopDesc() {
		return stopDesc;
	}
	
	public double getStopLat() {
		return stopLat;
	}
	
	public double getStopLon() {
		return stopLon;
	}
	
	public String getZoneId() {
		return zoneId;
	}
	
	public String getStopUrl() {
		return stopUrl;
	}
	
	public String getLocationType() {
		return locationType;
	}
	
	public String getParentStation() {
		return parentStation;
	}
	
	public String getStopTimezone() {
		return stopTimezone;
	}
	
	public String getWheelchairBoarding() {
		return wheelchairBoarding;
	}	
	
	/**
	 * Specifies if system should determine schedule adherence times
	 * for this stop. Some agencies configure all stops to have times but
	 * determining schedule adherence for every single stop would
	 * clutter things up. Only want to show adherence for a subset
	 * of stops.
	 * @return null if not set in config files
	 */
	public Boolean getAdherenceStop() {
		return adherenceStop;
	}

	/**
	 * Indicates that vehicle can leave route path before departing this stop
	 * since the driver is taking a break.
	 *
	 * @return null if not set in config files
	 */
	public Boolean getlayoverStop() {
		return layoverStop;
	}

	/**
	 * Specifies if stop is a wait stop where driver is
	 * not supposed to continue until the scheduled departure time.
	 *
	 * @return null if not set in config files
	 */
	public Boolean getWaitStop() {
		return waitStop;
	}
	
	/**
	 * Specifies if stop should be hidden from the public user interface.
	 * 
	 *@return null if not set in config files
	 */
	public Boolean getHidden() {
		return hidden;
	}

	@Override
	public String toString() {
		return "GtfsStop ["
				+ "lineNumber="	+ lineNumber
				+ ", stopId=" + stopId + ", "
				+ (stopCode != -1 ? ", stopCode=" + stopCode : "")
				+ ", stopName=" + stopName + ", "
				+ (stopDesc != null ? ", stopDesc=" + stopDesc : "")
				+ ", stopLat=" + stopLat
				+ ", stopLon="	+ stopLon
				+ (zoneId != null ? ", zoneId=" + zoneId : "")
				+ (stopUrl != null ? ", stopUrl=" + stopUrl : "")
				+ (locationType != null ? ", locationType=" + locationType : "")
				+ (parentStation != null ? ", parentStation=" + parentStation : "")
				+ (stopTimezone != null ? ", stopTimezone=" + stopTimezone : "")
				+ (wheelchairBoarding != null ? ", wheelchairBoarding=" + wheelchairBoarding : "")
				+ ", adherenceStop=" + adherenceStop
				+ ", layoverStop=" + layoverStop
				+ ", waitStop=" + waitStop
				+ ", hidden=" + hidden
				+ "]";
	}
	
}
