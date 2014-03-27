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

import org.transitime.statistics.ScheduleStatistics;

/**
 * For when generating more accurate schedule times using AVL data.
 * Includes the more accurate arrival and departure times plus
 * some additional information such as the highest values, lowest
 * values, and how many data points used.
 * 
 * @author SkiBu Smith
 * 
 */
public class GtfsExtendedStopTime extends GtfsStopTime {

	private final Integer arrivalOrigTimeSecs;
	private final Integer arrivalMinTimeSecs;
	private final Integer arrivalMaxTimeSecs;
	private final float arrivalStandardDeviation;
	private final int arrivalNumberDatapoints;
	
	private final Integer departureOriginalTimeSecs;
	private final Integer departureMinTimeSecs;
	private final Integer departureMaxTimeSecs;
	private final float departureStandardDeviation;
	private final int departureNumberDatapoints;
	
	/********************** Member Functions **************************/

	/**
	 * 
	 * @param originalValues
	 * @param useOriginalTimes
	 * @param arrivalResults
	 *            Contains statistical info including best time to use. If null
	 *            then the original schedule time is used.
	 * @param departureResults
	 *            Contains statistical info including best time to use. If null
	 *            then the original schedule time is used.
	 */
	public GtfsExtendedStopTime(GtfsStopTime originalValues,
			boolean useOriginalTimes,
			ScheduleStatistics.Stats arrivalResults, 
			ScheduleStatistics.Stats departureResults) {
		// Initialize the GtfsStopTime superclass using the results of 
		// the AVL data.
		super(originalValues, 
				arrivalResults == null  || useOriginalTimes ? 
						null : arrivalResults.bestValue, 
				departureResults == null  || useOriginalTimes ? 
						null : departureResults.bestValue);
		
		this.arrivalOrigTimeSecs = originalValues.getArrivalTimeSecs();
		if (arrivalResults == null) {
			this.arrivalMinTimeSecs = null;
			this.arrivalMaxTimeSecs = null;
			this.arrivalStandardDeviation = Float.NaN;
			this.arrivalNumberDatapoints = 0;			
		} else {
			this.arrivalMinTimeSecs = arrivalResults.min;
			this.arrivalMaxTimeSecs = arrivalResults.max;
			this.arrivalStandardDeviation = arrivalResults.standardDeviation;
			this.arrivalNumberDatapoints = arrivalResults.filteredTimesArray.length;
		}
		
		this.departureOriginalTimeSecs = originalValues.getDepartureTimeSecs();
		if (departureResults == null) {
			this.departureMinTimeSecs = null;
			this.departureMaxTimeSecs = null;
			this.departureStandardDeviation = Float.NaN;
			this.departureNumberDatapoints = 0;
		} else {
			this.departureMinTimeSecs = departureResults.min;
			this.departureMaxTimeSecs = departureResults.max;
			this.departureStandardDeviation = departureResults.standardDeviation;
			this.departureNumberDatapoints = departureResults.filteredTimesArray.length;
		}
	}

	public Integer getArrivalOrigTimeSecs() {
		return arrivalOrigTimeSecs;
	}

	public Integer getArrivalMinTimeSecs() {
		return arrivalMinTimeSecs;
	}

	public Integer getArrivalMaxTimeSecs() {
		return arrivalMaxTimeSecs;
	}

	public double getArrivalStdDev() {
		return arrivalStandardDeviation;
	}
	
	public int getArrivalNumberDatapoints() {
		return arrivalNumberDatapoints;
	}

	public Integer getDepartureOrigTimeSecs() {
		return departureOriginalTimeSecs;
	}

	public Integer getDepartureMinTimeSecs() {
		return departureMinTimeSecs;
	}

	public Integer getDepartureMaxTimeSecs() {
		return departureMaxTimeSecs;
	}

	public double getDepartureStdDev() {
		return departureStandardDeviation;
	}

	public int getDepartureNumberDatapoints() {
		return departureNumberDatapoints;
	}
}
