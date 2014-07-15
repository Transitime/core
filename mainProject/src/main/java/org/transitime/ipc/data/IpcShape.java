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
import java.util.ArrayList;
import java.util.List;

import org.transitime.db.structs.Location;
import org.transitime.db.structs.TripPattern;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class IpcShape implements Serializable {

	private String tripPatternId;
	private String headsign;
	private List<Location> locations;
	private boolean isUiShape;
	
	private static final long serialVersionUID = 4035471462057953970L;

	/********************** Member Functions **************************/

	IpcShape(TripPattern tripPattern, boolean isUiShape) {
		this.tripPatternId = tripPattern.getId();
		this.headsign = tripPattern.getHeadsign();
		this.locations = new ArrayList<Location>();
		this.isUiShape = isUiShape;
	}
	
	@Override
	public String toString() {
		return "IpcShape ["
				+ "tripPatternId=" + tripPatternId
				+ ", headsign=" + headsign
				+ ", locations=" + locations 
				+ ", isUiShape=" + isUiShape
				+ "]";
	}

	public void add(Location loc) {
		locations.add(loc);
	}
	
	public String getTripPatternId() {
		return tripPatternId;
	}
	
	public String getHeadsign() {
		return headsign;
	}
	
	public boolean isUiShape() {
		return isUiShape;
	}

	public List<Location> getLocations() {
		return locations;
	}

}
