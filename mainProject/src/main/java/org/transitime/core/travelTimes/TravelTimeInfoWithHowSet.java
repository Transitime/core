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
import org.transitime.db.structs.TravelTimesForStopPath.HowSet;

/**
 * Extends TravelTimeInfo class but adds how the travel time was set.
 * This way can see if travel time set via AVL data directly, is for
 * a different trip, or is for a different service class, etc.
 *
 * @author SkiBu Smith
 *
 */
public class TravelTimeInfoWithHowSet extends TravelTimeInfo {

	private final HowSet howSet;
	
	/********************** Member Functions **************************/

	/**
	 * Simple constructor.
	 * 
	 * @param trip
	 * @param stopPathIndex
	 * @param stopTime
	 * @param travelTimes
	 * @param travelTimeSegLength
	 * @param howSet
	 */
	public TravelTimeInfoWithHowSet(Trip trip, int stopPathIndex, int stopTime,
			List<Integer> travelTimes, double travelTimeSegLength, HowSet howSet) {
		super(trip, stopPathIndex, stopTime, travelTimes, travelTimeSegLength);
		this.howSet = howSet;
	}
	
	public TravelTimeInfoWithHowSet(TravelTimeInfo travelTimeInfo, HowSet howSet) {
		super(travelTimeInfo);
		this.howSet = howSet;
	}
	
	/**
	 * Returns how the travel time info was obtained
	 * @return
	 */
	public HowSet howSet() {
		return howSet;
	}
}
