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

package org.transitime.core.travelTimes;

import java.util.List;

import org.transitime.db.structs.Trip;
import org.transitime.utils.Geo;

/**
 * For holding the GPS based travel times for each trip/stop path. Contains
 * the trip info so can determine things such as the trip pattern and the
 * trip start time so that can find best match for when GPS data is not
 * available for a particular trip.
 *
 * @author SkiBu Smith
 *
 */
public class TravelTimeInfo {

	private final Trip trip;
	private final int stopPathIndex;
	private final List<Integer> travelTimes;
	private final int stopTime;
	private final double travelTimeSegLength;
	
	public static final int STOP_TIME_NOT_VALID = -1;
		
	/********************** Member Functions **************************/

	public TravelTimeInfo(Trip trip, int stopPathIndex, int stopTime,
			List<Integer> travelTimes, double travelTimeSegLength) {
		this.trip = trip;
		this.stopPathIndex = stopPathIndex;
		this.stopTime = stopTime;
		this.travelTimes = travelTimes;
		this.travelTimeSegLength = travelTimeSegLength;
	}
	
	public TravelTimeInfo(TravelTimeInfo toCopy) {
		this.trip = toCopy.trip;
		this.stopPathIndex = toCopy.stopPathIndex;
		this.stopTime = toCopy.stopTime;
		this.travelTimes = toCopy.travelTimes;
		this.travelTimeSegLength = toCopy.travelTimeSegLength;
	}
	
	@Override
	public String toString() {
		return "TravelTimeInfo [" 
				+ "trip=" + trip 
				+ ", stopPathIndex=" + stopPathIndex 
				+ ", travelTimes=" + travelTimes
				+ ", stopTime=" + stopTime 
				+ ", travelTimeSegLength=" + 
					Geo.distanceFormat(travelTimeSegLength) 
				+ "]";
	}

	public Trip getTrip() {
		return trip;
	}

	public int getStopPathIndex() {
		return stopPathIndex;
	}
	
	public List<Integer> getTravelTimes() {
		return travelTimes;
	}

	public int getStopTime() {
		return stopTime;
	}

	public boolean isStopTimeValid() {
		return stopTime != STOP_TIME_NOT_VALID && stopTime >= 0;
	}
	
	public boolean areTravelTimesValid() {
		if (travelTimes == null || travelTimes.isEmpty())
			return false;
		for (int time : travelTimes)
			if (time < 0)
				return false;
		return true;
	}
	
	public double getTravelTimeSegLength() {
		return travelTimeSegLength;
	}
}
