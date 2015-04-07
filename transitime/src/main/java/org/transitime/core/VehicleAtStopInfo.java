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

import org.transitime.db.structs.Block;

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
	 * Returns true if tripIndex, stopPathIndex, and segmentIndex are for the
	 * very last trip, path, segment for the block assignment. Had to override
	 * Indices.atEndOfBlock() because this class doesn't use the segment index
	 * while Indices does.
	 * 
	 * @return
	 */
	@Override
	public boolean atEndOfBlock() {
		return getTripIndex() == getBlock().numTrips() - 1
				&& getStopPathIndex() == 
						getBlock().numStopPaths(getTripIndex()) - 1;
	}

}
