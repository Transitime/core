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
package org.transitclock.db.structs;

import java.util.Date;

import javax.persistence.Entity;

/**
 * For persisting a Departure time.
 * @author SkiBu Smith
 *
 */
@Entity
public class Departure extends ArrivalDeparture {

	// Needed because Hibernate requires objects to be serializable
	private static final long serialVersionUID = 8489481047642753556L;

	/********************** Member Functions **************************/

	/**
	 * Simple constructor
	 * 
	 * @param vehicleId
	 * @param time
	 * @param avlTime
	 *            So can match arrival to the AVL report that generated it
	 * @param block
	 * @param tripIndex
	 * @param stopPathIndex
	 * @param freqStartTime
	 * @param dwellTime
	 */
	public Departure(String vehicleId, Date time, Date avlTime, Block block,
					 int tripIndex, int stopPathIndex, Date freqStartTime, Long dwellTime, String stopPathId,
					 boolean isScheduleAdherenceStop) {
		super(vehicleId, time, avlTime, block, tripIndex, stopPathIndex, 
				false, freqStartTime, dwellTime, stopPathId, isScheduleAdherenceStop);
	}
	public Departure(int configRev, String vehicleId, Date time, Date avlTime, Block block,
					 int tripIndex, int stopPathIndex, Date freqStartTime, Long dwellTime, String stopPathId,
					 boolean isScheduleAdherenceStop) {
		super(configRev, vehicleId, time, avlTime, block, tripIndex, stopPathIndex, 
				false, freqStartTime, dwellTime, stopPathId, isScheduleAdherenceStop);
	}
	/**
	 * Hibernate always wants a no-arg constructor. Made private since 
	 * it shouldn't normally be used.
	 */
	@SuppressWarnings("unused")
	private Departure() {
		super();
	}

	/**
	 * A copy constructor that creates a new Departure by copying this
	 * one but using the newTime. This method is needed to update a time given
	 * that the class has all final elements.
	 *
	 * @param newTime
	 * @return The newly constructed Arrival with the new time.
	 */
	public Departure withUpdatedTime(Date newTime) {
		return new Departure(getVehicleId(), newTime, getAvlTime(), getBlock(),
						getTripIndex(), getStopPathIndex(), getFreqStartTime(), getDwellTime(), getStopPathId(),
						isScheduleAdherenceStop());
	}

}
