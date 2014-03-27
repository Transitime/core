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
package org.transitime.db.structs;

import java.util.Date;

import javax.persistence.Entity;

/**
 * For persisting an Arrival time.
 * 
 * @author SkiBu Smith
 *
 */
@Entity
public class Arrival extends ArrivalDeparture {

	// Needed because Hibernate requires objects to be serializable
	private static final long serialVersionUID = 5438246244164457207L;

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
	 */
	public Arrival(String vehicleId, Date time, Date avlTime, Block block,
			int tripIndex, int pathIndex) {
		super(vehicleId, time, avlTime, block, tripIndex, pathIndex, 
				true); // isArrival
	}

	/**
	 * Hibernate always wants a no-arg constructor. Made private since 
	 * it shouldn't normally be used.
	 */
	@SuppressWarnings("unused")
	private Arrival() {
		super();
	}
}
