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
import org.transitime.db.structs.Vector;

/**
 * Represents a shape for Inter Process Communication (IPC)
 *
 * @author SkiBu Smith
 *
 */
public class IpcShape implements Serializable {

	private String tripPatternId;
	private String headsign;
	private List<Location> locations;
	private boolean isUiShape;
	
	// For determining if segment from previous stop path can be
	// combined with segment from new stop path.
	private static final double MAX_VERTEX_DISTANCE = 3.0;
	
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

	/**
	 * Adds list of locations for a stop path to the location member. Tries to
	 * reduce number of segments by combining segment for previous stop path
	 * with segment of new stop path if they are going in straight line within
	 * MAX_VERTEX_DISTANCE.
	 * 
	 * @param locs
	 *            List of locations for a stop path to be added
	 */
	public void add(List<Location> locs) {
		// If already have a location then don't need to add the first
		// location since it will have already been added as part of the 
		// previous stop path. So only add first location if dealing
		// with first stop path for the shape.
		if (locations.isEmpty()) {
			locations.add(locs.get(0));
		} 
		
		// If path is for a straight line then want to combine the segments so
		// that get fewer segments, which will be better for drawing on
		// maps and such.
		if (locations.size() >= 2) {
			Vector possibleNewVector = new Vector(locations.get(locations.size()-2), locs.get(1));
			Location vertex = locs.get(0);
			double distanceOfVertexToNewVector = possibleNewVector.distance(vertex);
			if (distanceOfVertexToNewVector < MAX_VERTEX_DISTANCE) {
				locations.remove(locations.size()-1);
			}

		}
		
		// Add the other locations
		for (int i=1; i<locs.size(); ++i)
			locations.add(locs.get(i));
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
