/**
 * 
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
 * A GTFS fare_rules object.
 * 
 * @author SkiBu Smith
 *
 */
public class GtfsFareRule extends GtfsBase {

	private final String fareId;
	private final String routeId;
	private final String originId;
	private final String destinationId;
	private final String containsId;

	/********************** Member Functions **************************/

	/**
	 * Creates a GtfsFareRule object by reading the data
	 * from the CSVRecord.
	 * @param record
	 * @param supplemental
	 * @param fileName for logging errors
	 */
	public GtfsFareRule(CSVRecord record, boolean supplemental, String fileName) {
		super(record, supplemental, fileName);

		fareId = getRequiredValue(record, "fare_id");
		routeId = getOptionalValue(record, "route_id");
		originId = getOptionalValue(record, "origin_id");
		destinationId = getOptionalValue(record, "destination_id");
		containsId = getOptionalValue(record, "contains_id");
	}
	
	public String getFareId() {
		return fareId;
	}

	public String getRouteId() {
		return routeId;
	}

	public String getOriginId() {
		return originId;
	}

	public String getDestinationId() {
		return destinationId;
	}

	public String getContainsId() {
		return containsId;
	}

	@Override
	public String toString() {
		return "GtfsFareRule ["
				+ "lineNumber=" + lineNumber + ", "
				+ (fareId != null ? "_fareId=" + fareId + ", " : "")
				+ (routeId != null ? "routeId=" + routeId + ", " : "")
				+ (originId != null ? "_originId=" + originId + ", " : "")
				+ (destinationId != null ? "_destinationId=" + destinationId
						+ ", " : "")
				+ (containsId != null ? "_containsId=" + containsId : "")
				+ "]";
	}


}
