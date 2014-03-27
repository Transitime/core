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

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.transitime.db.structs.Block;
import org.transitime.db.structs.StopPath;
import org.transitime.db.structs.ScheduleTime;
import org.transitime.db.structs.Trip;
import org.transitime.db.structs.Vector;
import org.transitime.utils.Geo;

/**
 * Describes where an AVL report matches to an assignment spatially.
 * 
 * @author SkiBu Smith
 * 
 */
public class SpatialMatch {

	protected final String vehicleId;
	protected final Block block;
	protected final int tripIndex;
	protected final int stopPathIndex;
	protected final int segmentIndex;
	protected final double distanceToSegment;
	protected final double distanceAlongSegment;
	protected final VehicleAtStopInfo atStop;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(SpatialMatch.class);


	/********************** Member Functions **************************/

	public SpatialMatch(String vehicleId, Block block, int tripIndex,
			int pathIndex, int segmentIndex, double distanceToSegment,
			double distanceAlongSegment) {
		this.vehicleId = vehicleId;
		this.block = block;
		this.tripIndex = tripIndex;
		this.stopPathIndex = pathIndex;
		this.segmentIndex = segmentIndex;
		this.distanceToSegment = distanceToSegment;
		this.distanceAlongSegment = distanceAlongSegment;
		
		// Determine whether at stop
		this.atStop = atStop();
	}

	/**
	 * For making a copy of the SpatialMatch but for a new trip. This is 
	 * useful when doing things by TripPattern, such as spatial matching. 
	 * Can then make copies for the other trips that use that
	 * TripPattern.
	 * 
	 * @param toCopy The SpatialMatch to copy (except for the trip info)
	 * @param newTrip The new trip to use for the copy
	 */
	public SpatialMatch(SpatialMatch toCopy, Trip newTrip) {
		this.vehicleId = toCopy.vehicleId;
		this.block = toCopy.block;
		this.tripIndex = toCopy.block.getTripIndex(newTrip.getId());
		this.stopPathIndex = toCopy.stopPathIndex;
		this.segmentIndex = toCopy.segmentIndex;
		this.distanceToSegment = toCopy.distanceToSegment;
		this.distanceAlongSegment = toCopy.distanceAlongSegment;
		// Make a copy of the atStop. Can't simply use the toCopy
		// atStop because it will be for the wrong tripIndex. Therefore
		// need to create a new object.
		if (toCopy.atStop() == null) {
			this.atStop = null;
		} else {
			this.atStop = new VehicleAtStopInfo(toCopy.getAtStop().getBlock(),
					this.tripIndex - 
						(toCopy.tripIndex - toCopy.getAtStop().getTripIndex()), 
					toCopy.atStop().getStopPathIndex());
		}
	}
	
	/**
	 * Constructs a new SpatialMatch but at the new indices specified. Useful
	 * for create a match that is just before or after a stop, which is useful
	 * for determining travel time from stop to the next match.
	 * 
	 * @param toCopy
	 * @param newIndices
	 * @param distanceAlongSegment
	 */
	public SpatialMatch(SpatialMatch toCopy, Indices newIndices, 
			double distanceAlongSegment) {
		this.vehicleId = toCopy.vehicleId;
		this.block = toCopy.block;
		this.tripIndex = newIndices.getTripIndex();
		this.stopPathIndex = newIndices.getStopPathIndex();
		this.segmentIndex = newIndices.getSegmentIndex();
		this.distanceToSegment = toCopy.distanceToSegment;
		this.distanceAlongSegment = distanceAlongSegment;
		this.atStop = toCopy.atStop;
	}

	/**
	 * For subclasses to create an object using this superclass.
	 * 
	 * @param toCopy
	 */
	protected SpatialMatch(SpatialMatch toCopy) {
		this.vehicleId = toCopy.vehicleId;
		this.block = toCopy.block;
		this.tripIndex = toCopy.tripIndex;
		this.stopPathIndex = toCopy.stopPathIndex;
		this.segmentIndex = toCopy.segmentIndex;
		this.distanceToSegment = toCopy.distanceToSegment;
		this.distanceAlongSegment = toCopy.distanceAlongSegment;
		this.atStop = toCopy.atStop;
	}
	
	/**
	 * Returns a SpatialMatch that corresponds to the the current match 
	 * but will be moved to after a stop if the current match is
	 * just before the stop. This way can pass in this modified match to
	 * the methods that determine travel time and the layover/stop time
	 * for the stop where the SpatialMatch is will not be included in the
	 * calculations. If the current object is not just before a stop then
	 * the original object is returned.
	 * @return
	 */
	public SpatialMatch getMatchAfterStop() {
		// If the spatialMatch is not just before a stop then return
		// spatialMatch. The spatialMatch is just before a stop if
		// atStop trip and path indices are the same as for the match.
		if (atStop == null || 
				atStop.getTripIndex() != tripIndex || 
				atStop.getStopPathIndex() != stopPathIndex)
			return this;
		
		// The spatialMatch is just before a stop so create a new spatial
		// match but use the beginning of the next path.
		Indices nextPathIndices = getIndices().incrementStopPath();
		return new SpatialMatch(this, nextPathIndices, 0.0);
	}

	/**
	 * Returns a SpatialMatch that corresponds to the current match 
	 * but will be moved to before a stop if the current match is
	 * just after the stop. This way can pass in this modified match to
	 * the methods that determine travel time and the layover/stop time
	 * for the stop where the SpatialMatch is will not be included in the
	 * calculations. If the current object is not just after a stop then
	 * the original object is returned.
	 * @return
	 */
	public SpatialMatch getMatchBeforeStop() {
		// If the spatialMatch is not just after a stop then return
		// spatialMatch. The spatialMatch is after before a stop if
		// atStop trip and path indices are different than for the match.
		if (atStop == null || 
				(atStop.getTripIndex() == tripIndex && 
				 atStop.getStopPathIndex() == stopPathIndex))
			return this;
		
		// The spatialMatch is just after a stop so create a new spatial
		// match but use the end of the previous path.
		Indices previousPathIndices = getIndices().decrementStopPath();
		return new SpatialMatch(this, previousPathIndices, 
				previousPathIndices.getSegment().length());
	}

	/**
	 * Returns a SpatialMatch that corresponds to the next stop. Useful if
	 * need to determine travel time to next stop. 
	 * @return
	 */
	public SpatialMatch getMatchAtNextStop() {
		// First need to get on the proper path. If just before a stop
		// then need to get a match just after that stop.
		SpatialMatch m = getMatchAfterStop();
		
		int segmentIndex = block.numSegments(m.getTripIndex(), m.getStopPathIndex())-1;
		double segmentLength =
				block.getSegmentVector(m.getTripIndex(), m.getStopPathIndex(), segmentIndex).length();
		
		// Return a match that is at the end of the path
		return new SpatialMatch(vehicleId,
				block, 
				m.getTripIndex(),
				m.getStopPathIndex(),
				segmentIndex, 
				Double.NaN, // distanceToSegment not set to a valid value
				segmentLength);
	}
	
	/**
	 * Returns a SpatialMatch that corresponds to the previous stop. The 
	 * returned match will be just before the previous stop. Useful
	 * if need to determine travel time from previous stop.
	 * @return
	 */
	public SpatialMatch getMatchAtPreviousStop() {
		// First need to get on the proper path. If just before a stop
		// then need to get a match just after that stop.
		Indices indices = getMatchBeforeStop().getIndices().decrementStopPath();
		
		// Return a match that is at the end of the path
		Vector vector = indices.getBlock().getSegmentVector(
				indices.getTripIndex(), indices.getStopPathIndex(),
				indices.getSegmentIndex());
		return new SpatialMatch(vehicleId,
				block, 
				indices.getTripIndex(),
				indices.getStopPathIndex(),
				indices.getSegmentIndex(),
				Double.NaN,       // distanceToSegment not set to a valid value
				vector.length()); // segmentLength
	}
	
	/**
	 * Determines if the match means that the vehicle is considered to be at a
	 * stop. Looks forward and back from the match to see if it is within the
	 * allowable distance of the stop.
	 * 
	 * @return VehicleAtStopInfo object containing trip and path index of stop
	 *         that match is nearby. Returns null if match is not near a stop.
	 */
	private VehicleAtStopInfo atStop() {
		// Determine if just before a stop
		StopPath path = block.getStopPath(tripIndex, stopPathIndex);		
		if (getDistanceRemainingInStopPath() < path.getBeforeStopDistance()) {
			// Indeed just before the stop so return the current stop/path index
			return new VehicleAtStopInfo(block, tripIndex, stopPathIndex);
		} 
		
		// Not just before a stop so see if just after a stop
		Indices previousPathIndices = getIndices().decrementStopPath();
		if (!previousPathIndices.beforeBeginningOfBlock()) {
			StopPath previousPath = previousPathIndices.getStopPath();
			if (previousPath != null &&
					getDistanceAlongStopPath() < previousPath.getAfterStopDistance())
				return new VehicleAtStopInfo(block,
						previousPathIndices.getTripIndex(), 
						previousPathIndices.getStopPathIndex());
		}
		
		// Not at a stop so return null to indicate such
		return null;
	}
	
	/**
	 * @return true if this spatial match is for a layover
	 */
	public boolean isLayover() {
		return block.isLayover(tripIndex, stopPathIndex);
	}
	
	/**
	 * @return true if this spatial match is for a waitStop
	 */
	public boolean isWaitStop() {
		return block.isWaitStop(tripIndex, stopPathIndex);
	}
	
	/**
	 * Returns the scheduled time vehicle is to leave the stop.
	 * 
	 * @return the scheduled time vehicle is to leave the stop. Time
	 * is seconds into day.
	 */
	public int getScheduledLayoverTime() { 
		try {
			ScheduleTime scheduleTime = block.getScheduleTime(tripIndex, stopPathIndex);
			return scheduleTime.getDepartureTime();
		} catch (Exception e) {
			logger.error("Tried to get layover time for a stop that didn't have one. {}", 
					this);
			return -1; 
		}
	}
	
	/**
	 * Travel distance along current path.
	 * 
	 * @return
	 */
	public double getDistanceAlongStopPath() {
		double distance = 0.0;
		for (int segIndex=0; segIndex<segmentIndex; ++segIndex) {
			Vector v = block.getSegmentVector(tripIndex, stopPathIndex, segIndex);
			distance += v.length();
		}
		distance += distanceAlongSegment;
		return distance;
	}
	
	/**
	 * How far still to travel to end of path.
	 * 
	 * @return
	 */
	public double getDistanceRemainingInStopPath() {
		double distance = -distanceAlongSegment;
		StopPath stopPath = block.getStopPath(tripIndex, stopPathIndex);
		int numSegments = stopPath.getSegmentVectors().size();
		for (int segIndex=segmentIndex; segIndex<numSegments; ++segIndex) {
			distance += stopPath.getSegmentVector(segIndex).length();
		}
		return distance;
	}
	
	/**
	 * Determines the indices for the position the specified distance before
	 * this match. Useful for seeing if looking how far past a stop the match
	 * is.
	 * 
	 * @param distanceBeforeMatch
	 * @return
	 */
	public Indices getIndicesForDistanceBeforeMatch(double distanceBeforeMatch) {
		Indices indices = getIndices();
		double distanceExamined = distanceAlongSegment;
		while (distanceExamined < distanceBeforeMatch) {
			indices.decrement();
			if (indices.beforeBeginningOfBlock())
				return null;
			distanceExamined += indices.getSegment().length();			
		}
		
		return indices;
	}
	
	@Override
	public String toString() {
		return "SpatialMatch [" 
				+ "vehicleId=" + vehicleId
				// + ", block=" + block.toShortString() too verbose!
				+ ", blockId=" + block.getId()
				+ ", tripIndex=" + tripIndex
				+ ", stopPathIndex=" + stopPathIndex
				+ ", segmentIndex=" + segmentIndex
				+ ", atLayover=" + atLayover()
				+ ", distanceToSegment=" + Geo.distanceFormat(distanceToSegment) 
				+ ", distanceAlongSegment=" + Geo.distanceFormat(distanceAlongSegment) 
				+ ", atStop=" + atStop
				+ ", trip=" + getTrip().toShortString()
				+ "]";
	}


	/********************* Getter Methods *****************************/
	
	public String getVehicleId() {
		return vehicleId;
	}
	
	/**
	 * Returns the indices of the match as an Indices object.
	 * The Indices object is newly created and independent so
	 * it can be changed (incremented and decremented) at will.
	 * @return
	 */
	public Indices getIndices() {
		return new Indices(this);
	}
	
	public Block getBlock() {
		return block;
	}
	
	public Trip getTrip() {
		return block.getTrips().get(tripIndex);
	}

	/**
	 * The index of which trip this is within the block.
	 * 
	 * @return
	 */
	public int getTripIndex() {
		return tripIndex;
	}
	
	/**
	 * The index of which stop path this is within the trip.
	 * @return
	 */
	public int getStopPathIndex() {
		return stopPathIndex;
	}

	public StopPath getStopPath() {
		return getTrip().getStopPath(stopPathIndex);
	}
	
	public int getSegmentIndex() {
		return segmentIndex;
	}

	public double getDistanceToSegment() {
		return distanceToSegment;
	}
	
	public double getDistanceAlongSegment() {
		return distanceAlongSegment;
	}
	
	/**
	 * @return VehicleAtStopInfo object containing trip and path index of stop
	 *         that match is nearby. Returns null if match is not near a stop.
	 */
	public VehicleAtStopInfo getAtStop() {
		return atStop;
	}
	
	/**
	 * Returns true if the trip, path, and segment indices indicate
	 * that on last segment of a path that is a layover
	 * @return
	 */
	public boolean atLayover() {
		return getIndices().isLayover();
	}
}
