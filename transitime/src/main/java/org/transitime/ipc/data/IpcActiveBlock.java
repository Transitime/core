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

package org.transitime.ipc.data;

import java.io.Serializable;
import java.util.Collection;

/**
 * For IPC for obtaining currently active blocks. Contains both block
 * information plus info on vehicle assigned to the block.
 *
 * @author SkiBu Smith
 *
 */
public class IpcActiveBlock implements Serializable {
	private final IpcBlock block;
	private final int activeTripIndex;
	private final Collection<IpcVehicle> vehicles;
	
	private static final long serialVersionUID = -921793252731257869L;
	
	/********************** Member Functions **************************/

	/**
	 * Constructor
	 * 
	 * @param ipcBlock
	 * @param activeTripIndex
	 * @param ipcVehicles
	 */
	public IpcActiveBlock(IpcBlock ipcBlock, int activeTripIndex,
			Collection<IpcVehicle> ipcVehicles) {
		this.block = ipcBlock;
		this.activeTripIndex = activeTripIndex;
		this.vehicles = ipcVehicles;
	}

	@Override
	public String toString() {
		return "IpcBlockAndVehicle [" 
				+ "block=" + block 
				+ ", activeTripIndex=" + activeTripIndex
				+ ", vehicles=" + vehicles
				+ "]";
	}

	public IpcBlock getBlock() {
		return block;
	}

	public int getActiveTripIndex() {
		return activeTripIndex;
	}
	
	public Collection<IpcVehicle> getVehicles() {
		return vehicles;
	}
}
