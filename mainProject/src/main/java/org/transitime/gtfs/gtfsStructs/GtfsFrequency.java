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
import org.transitime.utils.Time;

/**
 * A GTFS frequencies object. 
 * @author SkiBu Smith
 *
 */
public class GtfsFrequency extends GtfsBase {

	private final String tripId;
	private final int startTime;
	private final int endTime;
	private final int headwaySecs;
	private final Boolean exactTimes;
	
	/********************** Member Functions **************************/

	/**
	 * Creates a GtfsFrequency object by reading the data
	 * from the CSVRecord.
	 * @param record
	 * @param supplemental
	 * @param fileName for logging errors
	 */
	public GtfsFrequency(CSVRecord record, boolean supplemental, String fileName) {
		super(record, supplemental, fileName);

		tripId = getRequiredValue(record, "trip_id");
		startTime = Time.parseTimeOfDay(getRequiredValue(record, "start_time"));
		endTime = Time.parseTimeOfDay(getRequiredValue(record, "end_time"));
		headwaySecs = Integer.parseInt(getRequiredValue(record, "headway_secs"));
		exactTimes = getOptionalBooleanValue(record, "exact_times");
	}

	public String getTripId() {
		return tripId;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	/**
	 * Value of 0 means that there is no predetermined frequency. The vehicles
	 * will simply run when they run.
	 * @return
	 */
	public int getHeadwaySecs() {
		return headwaySecs;
	}

	public Boolean getExactTimes() {
		return exactTimes;
	}

	@Override
	public String toString() {
		return "GtfsFrequency ["
				+ "lineNumber=" + lineNumber 
				+ ", tripId=" + tripId 
				+ ", startTime="	+ startTime 
				+ ", endTime=" + endTime 
				+ ", headwaySecs=" + headwaySecs 
				+ ", exactTimes=" + exactTimes 
				+ "]";
	}
	
	
	
	
}
