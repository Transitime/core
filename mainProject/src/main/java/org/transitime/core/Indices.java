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
package org.transitime.core;

import org.transitime.applications.Core;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.StopPath;
import org.transitime.db.structs.Route;
import org.transitime.db.structs.ScheduleTime;
import org.transitime.db.structs.Trip;
import org.transitime.db.structs.TripPattern;
import org.transitime.db.structs.VectorWithHeading;

/**
 * This private class is for keeping track of the trip, path, and
 * segment indices that specify where in a block a vehicle is matched
 * to. 
 * 
 * @author SkiBu Smith
 * 
 */
class Indices {
	private Block block;
	private int tripIndex;
	private int stopPathIndex;
	private int segmentIndex;
	
	/********************** Member Functions **************************/
	
	/**
	 * Simple constructor
	 * @param block
	 * @param tripIndex
	 * @param stopPathIndex
	 * @param segmentIndex
	 */
	public Indices(Block block, int tripIndex, int stopPathIndex, int segmentIndex) {
		this.block = block;
		this.tripIndex = tripIndex;
		this.stopPathIndex = stopPathIndex;
		this.segmentIndex = segmentIndex;
	}
	
	/**
	 * For constructing Indices object using a SpatialMatch
	 * @param spatialMatch
	 */
	public Indices(SpatialMatch spatialMatch) {
		this.block = spatialMatch.getBlock();
		this.tripIndex = spatialMatch.getTripIndex();
		this.stopPathIndex = spatialMatch.getStopPathIndex();
		this.segmentIndex = spatialMatch.getSegmentIndex();
	}
	
	/**
	 * Creates a copy of the Indices parameter. Useful if need to increment()
	 * or decrement() but don't want to affect the original object. 
	 * @param indices The object to clone
	 * @return
	 */
	@Override
	public Indices clone() {
		return new Indices(block, tripIndex, stopPathIndex, segmentIndex);
	}
	
	/**
	 * @param indices what to compare to
	 * @return true if this indices indicate a match before that specified
	 * by the indices passed in
	 */
	public boolean lessThan(Indices indices) {
		if (tripIndex > indices.tripIndex)
			return false;
		if (tripIndex < indices.tripIndex)
			return true;
		
		// tripIndex == indices.tripIndex
		if (stopPathIndex > indices.stopPathIndex)
			return false;
		if (stopPathIndex < indices.stopPathIndex)
			return true;
		
		// stopPathIndex == indices.pathIndex
		return segmentIndex < indices.segmentIndex;
	}
	
	/**
	 * Is this path earlier than the one passed in?
	 * 
	 * @param indices
	 * @return
	 */
	public boolean earlierStopPathThan(Indices indices) {
		if (tripIndex > indices.tripIndex)
			return false;
		if (tripIndex < indices.tripIndex)
			return true;
		
		// tripIndex == indices.tripIndex
		return stopPathIndex < indices.stopPathIndex;
	}

	/**
	 * Increments the indices to the next segment. If reached end of path
	 * then will increment the path index. And if reach end of trip then
	 * will increment the trip index.
	 * 
	 * @return The resulting Indices object
	 */
	public Indices increment() {
		++segmentIndex;
		if (segmentIndex >= block.numSegments(tripIndex, stopPathIndex)) {
			segmentIndex = 0;
			++stopPathIndex;
			if (stopPathIndex >= block.numStopPaths(tripIndex)) {
				stopPathIndex = 0;
				++tripIndex;
			}
		}
		
		return this;
	}

	/**
	 * Increments indices so it points to the next path.
	 * 
	 * @return
	 */
	public Indices incrementStopPath() {
		++stopPathIndex;
		if (stopPathIndex >= block.numStopPaths(tripIndex)) {
			stopPathIndex = 0;
			++tripIndex;
		}

		// Reset the segment index so that it is always valid (doesn't
		// go beyond end of the segment array.
		segmentIndex = 0;
		
		return this;
	}
	
	/**
	 * Decrements the indices to the previous segment. If going past
	 * beginning of path then path index is decremented. And if go
	 * past beginning of trip then the trip index is decremented.
	 * 
	 * @return The resulting Indices object
	 */
	public Indices decrement() {
		--segmentIndex;
		if (segmentIndex < 0) {
			--stopPathIndex;
			if (stopPathIndex < 0) {
				--tripIndex;
				// If trip index valid (not negative) then set path index to 
				// last one for the new trip
				if (tripIndex >= 0) {
					stopPathIndex = block.numStopPaths(tripIndex)-1;
				}
			}
			
			// Now that stopPathIndex properly updated, if trip index valid (not 
			// negative) then set path index to last one for the new path.			
			if (tripIndex >= 0) {
				segmentIndex = block.numSegments(tripIndex, stopPathIndex)-1;
			}
		}
		
		return this;
	}

	/**
	 * Decrements indices so it points to the previous path. Updates the
	 * trip and path indices. Also, updates segmentIndex so that it points
	 * to last segment on the new path.
	 * 
	 * @return
	 */
	public Indices decrementStopPath() {
		--stopPathIndex;
		if (stopPathIndex < 0) {
			--tripIndex;
			// If trip index valid (not negative) then set path index to 
			// last one for the new trip
			if (tripIndex >= 0) {
				stopPathIndex = block.numStopPaths(tripIndex)-1;
			}
		}
		
		// Reset the segment index so it points to last segment in path
		segmentIndex = block.numSegments(tripIndex, stopPathIndex)-1;
		
		return this;
	}
	
	/**
	 * Gets a previous path for the block assignment.
     *
	 * @param count
	 * @return The previous path, null if at beginning of block
	 */
	public StopPath getPreviousStopPath(int count) {
		// Determine the proper stopPathIndex and tripIndex
		int previousTripIndex = tripIndex;
		int previousStopPathIndex = stopPathIndex;
		for (int i=0; i<count; ++i) {
			--previousStopPathIndex;
			if (previousStopPathIndex < 0) {
				--previousTripIndex;
				// If went past beginning of block then gone to far
				if (previousTripIndex < 0)
					return null;
				
				TripPattern tripPattern = 
						block.getTrip(previousTripIndex).getTripPattern();
				previousStopPathIndex = tripPattern.getStopPaths().size()-1;
			}
		}
		// Determine and return the StopPath object
		Trip trip = block.getTrip(previousTripIndex);
		StopPath path = trip.getTripPattern().getStopPath(previousStopPathIndex);
		return path;
	}
	
	/**
	 * Gets the previous path for the block assignment.
	 * @return The previous path, null if at beginning of block
	 */
	public StopPath getPreviousStopPath() {
		return getPreviousStopPath(1);
	}
	
	/**
	 * Returns true if used increment() to go past end of block
	 * as indicated by the tripIndex being beyond its limit.
	 * @return
	 */
	public boolean pastEndOfBlock() {
		return tripIndex >= block.numTrips();
	}
	
	/**
	 * Returns true if tripIndex, stopPathIndex, and segmentIndex are for the
	 * very last trip, path, segment for the block assignment.
	 * @return
	 */
	public boolean atEndOfBlock() {
		return tripIndex == block.numTrips()-1 && 
				stopPathIndex == block.numStopPaths(tripIndex)-1 &&
				segmentIndex == block.numSegments(tripIndex, stopPathIndex)-1;
	}
	
	/**
	 * Returns true if used decrement() to go past beginning of block
	 * as indicated by the tripIndex being negative.
	 * @return
	 */
	public boolean beforeBeginningOfBlock() {
		return tripIndex < 0;
	}
	
	/**
	 * Returns true if at beginning of trip. Useful for determining
	 * if calling increment() caused Indices to end one trip and
	 * start the next one.
	 * @return
	 */
	public boolean atBeginningOfTrip() {
		return stopPathIndex == 0 && segmentIndex == 0;
	}
	
	/**
	 * Returns true if at last path/stop for a trip. Does not look at
	 * the segmentIndex, only the stopPathIndex.
	 * 
	 * @return
	 */
	public boolean atEndOfTrip() {
		return stopPathIndex == block.numStopPaths(tripIndex)-1;
	}
	
	/**
	 * @return true if at end of path indicating that at a stop and need
	 * to include the stop time as part of travel time
	 */
	public boolean atEndOfStopPath() {
		return segmentIndex == block.numSegments(tripIndex, stopPathIndex)-1;
	}
	
	/**
	 * Determines if indices indicate that at a layover. At a layover if on the
	 * last segment of the path for a stop that has a layover.
	 * 
	 * @return true if path is for a layover where need to include layover time
	 *         as part of travel time
	 */
	public boolean isLayover() {
		return atEndOfStopPath() && block.isLayover(tripIndex, stopPathIndex);
	}
	
	/**
	 * Indicates that vehicle is not supposed to depart the stop until the
	 * scheduled departure time.
	 * 
	 * @return true if a wait stop
	 */
	public boolean isWaitStop() {
		return block.isWaitStop(tripIndex, stopPathIndex);
	}
	
	/**
	 * Returns the schedule time for the trip and path indices specified
	 * 
	 * @param tripIndex
	 * @param stopPathIndex
	 * @return the schedule time for the specified stop. Returns null if no
	 * schedule time associated with stop
	 */
	public ScheduleTime getScheduleTime() {
		return block.getScheduleTime(tripIndex, stopPathIndex);
	}
	
	/**
	 * Returns the time in msec for how long expected to be at the stop
	 * at the end of the path.
	 * 
	 * @return
	 */
	public int getStopTimeForPath() {
		return getBlock().getPathStopTime(tripIndex, stopPathIndex);
	}
	
	/**
	 * Returns the travel time for the specified path. Does not include stop 
	 * times.
	 * 
	 * @return
	 */
	public int getTravelTimeForPath() {
		return getBlock().getStopPathTravelTime(tripIndex, stopPathIndex);
	}
	
	@Override
	public String toString() {
		return "Indices [" 
				+ "blockId=" + block.getId() 
				+ ", tripIndex=" + tripIndex
				+ ", stopPathIndex=" + stopPathIndex 
				+ ", segmentIndex="	+ segmentIndex 
				+ "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Indices other = (Indices) obj;
		if (block == null) {
			if (other.block != null)
				return false;
		} else if (!block.equals(other.block))
			return false;
		if (stopPathIndex != other.stopPathIndex)
			return false;
		if (segmentIndex != other.segmentIndex)
			return false;
		if (tripIndex != other.tripIndex)
			return false;
		return true;
	}

	/********************** Getter Methods ****************************/
	
	public Block getBlock() {
		return block;
	}

	public Trip getTrip() {
		return block.getTrip(tripIndex);
	}
	
	public int getTripIndex() {
		return tripIndex;
	}

	public StopPath getStopPath() {
		return getTrip().getTripPattern().getStopPath(stopPathIndex);
	}
	
	public int getStopPathIndex() {
		return stopPathIndex;
	}

	public VectorWithHeading getSegment() {
		return getStopPath().getSegmentVector(segmentIndex);
	}
	
	/**
	 * Returns the route associated with this Indices
	 * @return
	 */
	public Route getRoute() {
		String routeId = getTrip().getRouteId();
		return Core.getInstance().getDbConfig().getRoute(routeId);
	}
	
	public int getSegmentIndex() {
		return segmentIndex;
	}

}
