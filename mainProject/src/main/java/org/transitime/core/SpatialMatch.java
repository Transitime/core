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
			int stopPathIndex, int segmentIndex, double distanceToSegment,
			double distanceAlongSegment) {
		this.vehicleId = vehicleId;
		this.block = block;
		this.tripIndex = tripIndex;
		this.stopPathIndex = stopPathIndex;
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
	 * Returns true if the match is to the stop at the end of the stop path.
	 * This can be important because atStop can also be set to the previous
	 * stop, which is something very different. If match is not for at a stop
	 * then null is returned.
	 * 
	 * @return True if match is for at stop at end of stop path
	 */
	public boolean atEndOfPathStop() {
		return atStop != null && atStop.getTripIndex() == tripIndex
				&& atStop.getStopPathIndex() == stopPathIndex;
	}
	
	/**
	 * Returns true if the match is to the stop for the previous stop path. This
	 * can be important because atStop can also be set to the stop for the
	 * current stop path, which is very different. If match is not for at a stop
	 * then null is returned.
	 * 
	 * @return True if match is for stop for the previous stop path
	 */
	public boolean atBeginningOfPathStop() {
		return atStop != null 
				&& atStop.getTripIndex() == tripIndex  
				&& atStop.getStopPathIndex() != stopPathIndex;		
	}
	
	/**
	 * For when need to determine arrival time when vehicle matches to a stop.
	 * Returns a SpatialMatch that corresponds to the end of the stop path for
	 * the stop indicated by the current SpatialMatch. If the match is for stop
	 * for the previous stop path (the vehicle is matching to just beyond the
	 * stop) the the previous stop path is used. Only to be called when vehicle
	 * is matched to a stop. Logs error and returns null if match is not for at
	 * a stop.
	 * 
	 * @return SpatialMatch adjusted so it is at end of path for the stop
	 */
	public SpatialMatch getMatchAdjustedToEndOfPath() {
		// If not at a stop then we have a problem
		if (atStop == null) {
			logger.error("For vehicleId={} wrongly called " +
					"getMatchAdjustedToEndOfPath() when vehicle is not at " +
					"a stop. This is not allowed.",
					vehicleId);
			return null;
		}
		
		// Determine Indices for the atStop for the Match. If match 
		// is for stop at the beginning of the path instead of the end
		// then decrement the indices so will point to proper stop path.
		Indices indices = getIndices();
		if (!atEndOfPathStop()) {
			indices.decrementStopPath();
		}

		// Determine and return spatial match that is at the end of
		// the StopPath indicates by indices
		StopPath stopPath = indices.getStopPath();
		int segmentIndex = stopPath.getNumberSegments()-1;
		Vector segmentVector = stopPath.getSegmentVector(segmentIndex);
		double distanceAlongSegment = segmentVector.length();
		Indices endOfStopPathIndices = new Indices(block,
				indices.getTripIndex(), indices.getStopPathIndex(),
				segmentIndex);
		return new SpatialMatch(this, endOfStopPathIndices,
				distanceAlongSegment);
	}
	
	/**
	 * For when need to determine departure time at a stop. Returns a
	 * SpatialMatch that corresponds to the beginning of the next stop path for
	 * the stop indicated by the current SpatialMatch. Only to be called when vehicle
	 * is matched to a stop. Logs error and returns null if match is not for at
	 * a stop.
	 * 
	 * @return SpatialMatch adjusted so it is at beginning of path for the stop
	 */
	public SpatialMatch getMatchAdjustedToBeginningOfPath() {
		// If not at a stop then we have a problem
		if (atStop == null) {
			logger.error("For vehicleId={} wrongly called " +
					"getMatchAdjustedToBeginningOfPath() when vehicle is " +
					"not at a stop. This is not allowed.",
					vehicleId);
			return null;
		}

		// Determine Indices for the atStop for the Match. If match 
		// is for stop at the beginning of the path instead of the end
		// then decrement the indices so will point to proper stop path.
		Indices indices = getIndices();
		if (!atBeginningOfPathStop()) {
			indices.incrementStopPath();
		}

		// Create a new SpatialMatch that is at the beginning of
		// stop path
		int segmentIndex = 0;
		double distanceAlongSegment = 0.0;
		Indices beginningOfStopPathIndices = new Indices(block,
				indices.getTripIndex(), indices.getStopPathIndex(),
				segmentIndex);
		return new SpatialMatch(this, beginningOfStopPathIndices,
				distanceAlongSegment);
	}
	
	/**
	 * Returns a SpatialMatch that corresponds to the current match but will be
	 * moved to after a stop if the current match is just before the stop. This
	 * way can pass in this modified match to the methods that determine travel
	 * time and the layover/stop time for the stop where the SpatialMatch is
	 * will not be included in the calculations. If the current object is not
	 * just before a stop then the original object is returned.
	 * 
	 * @return This match or one after the stop if at end of stop path
	 */
	private SpatialMatch getMatchAfterStopIfAtStop() {
		// If the spatialMatch is not just before a stop then return
		// spatialMatch. The spatialMatch is just before a stop if
		// atStop trip and path indices are the same as for the match.
		if (!atEndOfPathStop())
			return this;
		
		// The spatialMatch is just before a stop so create a new spatial
		// match but use the beginning of the next path.
		Indices nextPathIndices = getIndices().incrementStopPath();
		return new SpatialMatch(this, nextPathIndices, 0.0);
	}

	/**
	 * Returns a SpatialMatch that corresponds to just before the next stop.
	 * Useful if need to determine travel time to next stop.
	 * 
	 * @return
	 */
	public SpatialMatch getMatchAtJustBeforeNextStop() {
		// First need to get on the proper path. If just before a stop
		// then need to get a match just after that stop.
		SpatialMatch m = getMatchAfterStopIfAtStop();
		
		int segmentIndex = 
				block.numSegments(m.getTripIndex(), m.getStopPathIndex())-1;
		Vector segmentVector = block.getSegmentVector(m.getTripIndex(),
				m.getStopPathIndex(), segmentIndex);
		double segmentLength = segmentVector.length();
		
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
	 * Returns a SpatialMatch that corresponds to the current match but will be
	 * moved to before a stop if the current match is just after the stop. This
	 * way can pass in this modified match to the methods that determine travel
	 * time and the layover/stop time for the stop where the SpatialMatch is
	 * will not be included in the calculations. If the current object is not
	 * just after a stop then the original object is returned.
	 * 
	 * @return This match or one before the stop if at beginning of stop path
	 */
	private SpatialMatch getMatchBeforeStopIfAtStop() {
		// If the spatialMatch is not just after a stop then return
		// spatialMatch. 
		if (!atBeginningOfPathStop())
			return this;
		
		// The spatialMatch is just after a stop so create a new spatial
		// match but use the end of the previous path.
		Indices previousPathIndices = getIndices().decrementStopPath();
		return new SpatialMatch(this, previousPathIndices, 
				previousPathIndices.getSegment().length());
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
		Indices indices = 
				getMatchBeforeStopIfAtStop().getIndices().decrementStopPath();
		
		// Return a match that is at the end of the path
		Vector segmentVector = indices.getBlock().getSegmentVector(
				indices.getTripIndex(), indices.getStopPathIndex(),
				indices.getSegmentIndex());
		return new SpatialMatch(vehicleId,
				block, 
				indices.getTripIndex(),
				indices.getStopPathIndex(),
				indices.getSegmentIndex(),
				Double.NaN,       // distanceToSegment not set to a valid value
				segmentVector.length()); 
	}
	
	/**
	 * Determines if the match means that the vehicle is considered to be at a
	 * stop. Looks forward and back from the match to see if it is within the
	 * allowable distance of the stop. Intended to be called by constructor
	 * to set the atStop member variable so that only have to determine if
	 * SpatialMatch is at the stop once.
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
	 * A layover stop is when a vehicle can leave route path before departing
	 * this stop since the driver is taking a break.
	 * 
	 * @return true if this spatial match is for a layover
	 */
	public boolean isLayover() {
		return block.isLayover(tripIndex, stopPathIndex);
	}
	
	/**
	 * Wait stop is when a vehicle is not supposed to depart the stop until the
	 * scheduled departure time.
	 * 
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
	public int getScheduledWaitStopTime() { 
		try {
			ScheduleTime scheduleTime = block.getScheduleTime(tripIndex, stopPathIndex);
			return scheduleTime.getDepartureTime();
		} catch (Exception e) {
			logger.error("Tried to get wait stop time for a stop that didn't have one. {}", 
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
	
	/**
	 * Returns true if this is before the other SpatialMatch passed in.
	 * 
	 * @param other
	 *            The Spatial Match to compare to
	 * @return true if this is before the other SpatialMatch
	 */
	public boolean lessThan(SpatialMatch other) {
		if (tripIndex > other.tripIndex)
			return false;
		if (tripIndex < other.tripIndex)
			return true;
		
		// tripIndex == other.tripIndex
		if (stopPathIndex > other.stopPathIndex)
			return false;
		if (stopPathIndex < other.stopPathIndex)
			return true;
		
		// stopPathIndex == other.pathIndex
		if (segmentIndex > other.segmentIndex)
			return false;
		if (segmentIndex < other.segmentIndex)
			return true;
		
		// segmentIndex == other.segmentIndex
		return distanceAlongSegment < other.distanceAlongSegment;
	}

	/**
	 * Returns the total number of stops traversed between the two
	 * matches.
	 * 
	 * @param match1
	 * @param match2
	 * @return Number of stops
	 */
	public static int numberStopsBetweenMatches(SpatialMatch match1,
			SpatialMatch match2) {
		// Stop index for first trip
		int stopIdxInFirstTrip = match1.getStopPathIndex();

		// Stops for intermediate trips
		Block block = match2.getBlock();
		int numIntermediateTripsStops = 0;
		for (int tripIndex = match1.getTripIndex(); 
				tripIndex < match2.getTripIndex(); 
				++tripIndex) {
			numIntermediateTripsStops += 
					block.getTrip(tripIndex).getNumberStopPaths();
		}

		// Stops for last trip
		int stopIdxInLastTrip = match2.getStopPathIndex();

		// Return total number of stops traversed between matches
		int totalStopsBetweenMatches = stopIdxInLastTrip - stopIdxInFirstTrip
				+ numIntermediateTripsStops;
		return totalStopsBetweenMatches;
	}
	
	@Override
	public String toString() {
		return "SpatialMatch [" 
				+ "vehicleId=" + vehicleId
				// + ", block=" + block.toShortString() too verbose!
				+ ", blockId=" + block.getId()
				+ ", tripIndex=" + tripIndex
				+ ", gtfsStopSeq=" + getStopPath().getGtfsStopSeq()
				+ ", stopPathIndex=" + stopPathIndex
				+ ", segmentIndex=" + segmentIndex
				+ ", isLayover=" + isLayover()
				+ ", distanceToSegment=" + Geo.distanceFormat(distanceToSegment) 
				+ ", distanceAlongSegment=" + Geo.distanceFormat(distanceAlongSegment) 
				+ ", distanceAlongStopPath=" + Geo.distanceFormat(getDistanceAlongStopPath())
				+ ", atStop=" + atStop
				+ ", trip=" + getTrip().toShortString()
				+ "]";
	}

	/********************* Getter Methods *****************************/
	
	public String getVehicleId() {
		return vehicleId;
	}
	
	/**
	 * Returns copy of the indices of the match as an Indices object.
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
	 * Can be either at stop at beginning of the stop path or at the stop at the
	 * end of the stop path.
	 * 
	 * @return VehicleAtStopInfo object containing trip and path index of stop
	 *         that match is nearby. Returns null if match is not near a stop.
	 */
	public VehicleAtStopInfo getAtStop() {
		return atStop;
	}
	
	/**
	 * Returns the VehicleAtStopInfo if match indicates that vehicle is at the
	 * stop at the end of the path.
	 * 
	 * @return VehicleAtStopInfo if vehicle at stop at end of stop path.
	 *         Otherwise null.
	 */
	public VehicleAtStopInfo getAtEndStop() {
		if (atStop == null)
			return null;
		
		// At a stop so see if it is the end of path stop
		if (atStop.getStopPathIndex() == stopPathIndex) {
			// At end of path stop so return it
			return atStop;
		} else {
			// At a stop but it is the beginning stop so return null
			return null;
		}
	}
	
	/**
	 * Returns the VehicleAtStopInfo if match indicates that vehicle is at the
	 * stop at the beginning of the path.
	 * 
	 * @return VehicleAtStopInfo if vehicle at stop at beginning of stop path.
	 *         Otherwise null.
	 */
	public VehicleAtStopInfo getAtBeginningStop() {
		if (atStop == null)
			return null;
		
		// At a stop so see if it is the end of path stop
		if (atStop.getStopPathIndex() == stopPathIndex) {
			// At end of path stop so return null
			return null;
		} else {
			// At beginning stop so return it
			return atStop;
		}	
	}
	
	/**
	 * Returns true if vehicle is at or near a stop. Can be either at stop at
	 * beginning of the stop path or at the stop at the end of the stop path.
	 * 
	 * @return true if vehicle is at or near a stop.
	 */
	public boolean isAtStop() {
		return atStop != null;
	}
	
	/**
	 * Returns true if this match indicates that the vehicle is at the stop
	 * indicated by the tripIndex and stopPathIndex parameters.
	 * 
	 * @param tripIndex
	 * @param stopPathIndex
	 * @return True if at specified stop
	 */
	public boolean isAtStop(int tripIndex, int stopPathIndex) {
		return atStop != null
				&& atStop.equals(atStop.getBlock().getId(), tripIndex, 
						stopPathIndex);
	}
	
}

