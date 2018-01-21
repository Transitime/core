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
package org.transitime.core;

import org.transitime.applications.Core;
import org.transitime.configData.CoreConfig;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.Trip;

/**
 * So that VehicleState can keep track of whether vehicle is matched to
 * a stop.
 * 
 * @author SkiBu Smith
 *
 */
public class VehicleAtStopInfo extends Indices {
	
	/********************** Member Functions **************************/

	/**
	 * @param block
	 * @param tripIndex
	 * @param stopPathIndex
	 */
	public VehicleAtStopInfo(Block block, int tripIndex, int stopPathIndex) {
		super(block, tripIndex, stopPathIndex, 
				0); // segment index
	}
		
	/**
	 * 
	 * @param indices
	 */
	public VehicleAtStopInfo(Indices indices) {
		super(indices.getBlock(), 
				indices.getTripIndex(), 
				indices.getStopPathIndex(),
				0); // segment index
	}
	
	/**
	 * Returns the stop ID of the stop
	 * 
	 * @return
	 */
	public String getStopId() { 
		return getStopPath().getStopId();
	}
	
	@Override
	public String toString() {
		return "Indices [" 
				+ "blockId=" + getBlock().getId() 
				+ ", tripIndex=" + getTripIndex()
				+ ", stopPathIndex=" + getStopPathIndex() 
				+ ", stopId=" + getStopId()
				+ "]";
	}

	/**
	 * For schedule based assignment returns true if tripIndex and stopPathIndex
	 * are for the very last trip, path, segment for the block assignment. Had
	 * to override Indices.atEndOfBlock() because this class doesn't use the
	 * segment index while Indices does.
	 * <p>
	 * But for no schedule based assignment then can't just look to see if at
	 * last stop because since the vehicle will loop it will often be at the
	 * last stop. Therefore for no schedule assignments this method looks at the
	 * current time to see if the block is still active.
	 * 
	 * @return true if vehicle at end of block
	 */
	@Override
  public boolean atEndOfBlock() {
		Block block = getBlock();
		if (block.isNoSchedule()) {
		  // frequency based blocks last until the last trip completes
		  Trip trip = getTrip();
		  int tripDuration = trip.getEndTime() - trip.getStartTime();
		  int blockDuration = block.getEndTime() - block.getStartTime();
		  int secondsBeforeTrip = CoreConfig.getAllowableEarlySeconds();
		  int secondsAfterTrip = CoreConfig.getAllowableLateSeconds();
		  return !block.isActive(Core.getInstance().getSystemDate(), secondsBeforeTrip, blockDuration + tripDuration + secondsAfterTrip); 
		} else {
		return getTripIndex() == block.numTrips() - 1
				&& getStopPathIndex() == 
						block.numStopPaths(getTripIndex()) - 1;
		}
	}

}
