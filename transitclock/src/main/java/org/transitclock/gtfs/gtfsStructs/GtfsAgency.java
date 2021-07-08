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
package org.transitclock.gtfs.gtfsStructs;

import org.apache.commons.csv.CSVRecord;
import org.transitclock.utils.csv.CsvBase;

/**
 *
 * @author SkiBu Smith
 *
 */
public class GtfsAgency extends CsvBase {

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
	}

	/**
	 * When combining a regular agency with a supplemental agency need to create a
	 * whole new object since this class is Immutable to make it safer to use.
	 * 
	 * @param originalStop
	 * @param supplementStop
	 */
	public GtfsAgency(GtfsAgency originalAgency, GtfsAgency supplementAgency) {
		super(originalAgency);
		
		// Use short variable names
		GtfsAgency o = originalAgency;
		GtfsAgency s = supplementAgency;
		
		agencyId = originalAgency.agencyId;
		agencyName = s.agencyName == null ? o.agencyName : s.agencyName;
		agencyUrl = s.agencyUrl == null ? o.agencyUrl : s.agencyUrl;
		agencyTimezone = s.agencyTimezone == null ? o.agencyTimezone : s.agencyTimezone;
		agencyLang = s.agencyLang == null ? o.agencyLang : s.agencyLang;
		agencyPhone = s.agencyPhone == null ? o.agencyPhone : s.agencyPhone;
		agencyFareUrl = s.agencyFareUrl == null ? o.agencyFareUrl : s.agencyFareUrl;
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
