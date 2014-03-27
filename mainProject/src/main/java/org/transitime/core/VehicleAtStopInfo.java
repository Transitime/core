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

import java.util.Date;

import org.transitime.db.structs.Block;

/**
 * So that VehicleState can keep track of whether vehicle is matched to
 * a stop.
 * 
 * @author SkiBu Smith
 *
 */
public class VehicleAtStopInfo extends Indices {

	private Date arrivalTime;
	
	/********************** Member Functions **************************/

	/**
	 * @param block
	 * @param tripIndex
	 * @param stopPathIndex
	 */
	public VehicleAtStopInfo(Block block, int tripIndex, int pathIndex) {
		super(block, tripIndex, pathIndex, 
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
	
	public void setArrivalTime(Date arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	
	public Date getArrivalTime() {
		return arrivalTime;
	}
	
}
