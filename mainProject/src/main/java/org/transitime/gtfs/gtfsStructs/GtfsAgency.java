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
 *
 * @author SkiBu Smith
 *
 */
public class GtfsAgency extends GtfsBase {

	private final String agencyId;
	private final String agencyName;
	private final String agencyUrl;
	// Valid timezone format is at http://en.wikipedia.org/wiki/List_of_tz_zones
	private final String agencyTimezone;
	private final String agencyLang;
	private final String agencyPhone;
	private final String agencyFareUrl;
	
	/********************** Member Functions **************************/

	/**
	 * 
	 * @param record
	 * @param supplemental
	 * @param fileName for logging errors

	 */
	public GtfsAgency(CSVRecord record, boolean supplemental, String fileName) {
		super(record, supplemental, fileName);
		
		agencyId = getOptionalValue(record, "agency_id");
		agencyName = getRequiredUnlessSupplementalValue(record, "agency_name");
		agencyUrl = getRequiredUnlessSupplementalValue(record, "agency_url");
		agencyTimezone = getRequiredUnlessSupplementalValue(record, "agency_timezone");
		agencyLang = getOptionalValue(record, "agency_lang");
		agencyPhone = getOptionalValue(record, "agency_phone");
		agencyFareUrl = getOptionalValue(record, "agency_fare_url");
		
		createDateFormatter(agencyTimezone);
	}

	public String getAgencyId() {
		return agencyId;
	}

	public String getAgencyName() {
		return agencyName;
	}

	public String getAgencyUrl() {
		return agencyUrl;
	}

	/**
	 * Valid timezone format is at http://en.wikipedia.org/wiki/List_of_tz_zones
	 * 
	 * @return
	 */
	public String getAgencyTimezone() {
		return agencyTimezone;
	}

	public String getAgencyLang() {
		return agencyLang;
	}

	public String getAgencyPhone() {
		return agencyPhone;
	}

	public String getAgencyFareUrl() {
		return agencyFareUrl;
	}

	@Override
	public String toString() {
		return "GtfsAgency [lineNumber=" + lineNumber + ", agencyId="
				+ agencyId + ", agencyName=" + agencyName + ", agencyUrl="
				+ agencyUrl + ", agencyTimezone=" + agencyTimezone
				+ ", agencyLang=" + agencyLang + ", agencyPhone="
				+ agencyPhone + ", agencyFareUrl=" + agencyFareUrl + "]";
	}
	
	
}
