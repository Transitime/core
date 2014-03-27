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

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.csv.CSVRecord;
import org.transitime.utils.Time;


/**
 * A GTFS calendar_dates object.
 * @author SkiBu Smith
 *
 */
public class GtfsCalendarDate extends GtfsBase {

	private final String serviceId;
	private final Date date;
	private final String exceptionType;
	
	/********************** Member Functions **************************/

	/**
	 * Creates a GtfsRoute object by reading the data
	 * from the CSVRecord.
	 * @param record
	 * @param supplemental
	 * @param fileName for logging errors
	 */
	public GtfsCalendarDate(CSVRecord record, boolean supplemental, String fileName) 
			throws ParseException {
		super(record, supplemental, fileName);
		
		serviceId = getRequiredValue(record, "service_id");
		date = dateFormatter.parse(getRequiredValue(record, "date"));
		exceptionType = getRequiredValue(record, "exception_type");
	}

	public String getServiceId() {
		return serviceId;
	}

	public Date getDate() {
		return date;
	}

	public String getExceptionType() {
		return exceptionType;
	}

	@Override
	public String toString() {
		return "GtfsCalendarDates [lineNumber=" + lineNumber
				+ ", serviceId=" + serviceId 
				+ ", date=" + Time.dateStr(date) 
				+ ", exceptionType=" + exceptionType + "]";
	}
	
}
