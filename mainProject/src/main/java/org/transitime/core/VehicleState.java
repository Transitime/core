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

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.transitime.configData.CoreConfig;
import org.transitime.db.structs.Arrival;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.AvlReport.AssignmentType;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.Location;
import org.transitime.db.structs.StopPath;
import org.transitime.db.structs.Trip;
import org.transitime.ipc.data.Prediction;
import org.transitime.utils.StringUtils;
import org.transitime.utils.Time;

/**
 * Keeps track of vehicle state including its block assignment, where it
 * last matched to its assignment, and AVL reports.
 * 
 * @author SkiBu Smith
 *
 */
public class VehicleState {

	private final String vehicleId;
	private Block  block;
	private BlockAssignmentMethod assignmentMethod;
	private Date assignmentTime;
	private boolean predictable;
	// First is most recent
	private LinkedList<TemporalMatch> temporalMatchHistory = 
			new LinkedList<TemporalMatch>();
	// First is most recent
	private LinkedList<AvlReport> avlReportHistory =
			new LinkedList<AvlReport>();
	private List<Prediction> predictions;
	private TemporalDifference realTimeSchedAdh;
	
	// For keeping track of how many bad matches have been encountered.
	// This way can ignore bad matches if only get a couple
	private int numberOfBadMatches = 0;
	
	// So can make sure that departure time is after the arrival time
	private Arrival arrivalToStoreToDb;
	private long lastArrivalTime = 0;
	
	private static int MATCH_HISTORY_MAX_SIZE = 6;
	private static int AVL_HISTORY_MAX_SIZE = 6;
	
	/********************** Member Functions **************************/

	public VehicleState(String vehicleId) {
		this.vehicleId = vehicleId;
	}
	
	/**
	 * Sets the block assignment for vehicle. Also, this is how it is specified
	 * whether a vehicle is predictable or not.
	 * 
	 * @param block
	 *            The current block assignment for the vehicle. Set to null if
	 *            vehicle not assigned.
	 * @param assignmentMethod
	 *            How vehicle was assigned (AVL feed, auto assigner, etc)
	 * @param predictable
	 *            Whether vehicle is predictable
	 */
	public void setBlock(Block block, BlockAssignmentMethod assignmentMethod, 
			boolean predictable) {
		this.block = block;
		this.assignmentMethod = assignmentMethod;
		this.predictable = predictable;
		this.assignmentTime = new Date(System.currentTimeMillis());
	}
	
	/**
	 * Sets the match for the vehicle into the history. If set to null then
	 * VehicleState.predictable is set to false. Also resets numberOfBadMatches
	 * to 0.
	 * 
	 * @param match
	 */
	public void setMatch(TemporalMatch match) {
		// Add match to history
		temporalMatchHistory.addFirst(match);
		
		// Set predictability
		if (match == null) {
			predictable = false;
			
			// Make sure that the arrival time buffer is cleared so that
			// when get a new assignment won't try to use it since something
			// peculiar might have happened.
			setArrivalToStoreToDb(null);
		}
		
		// Reset numberOfBadMatches
		numberOfBadMatches = 0;
		
		// Truncate list if it has gotten too long
		while (temporalMatchHistory.size() > MATCH_HISTORY_MAX_SIZE) {
			temporalMatchHistory.removeLast();
		}
	}
	
	/**
	 * Returns the last temporal match. Returns null if there isn't one.
	 * @return
	 */
	public TemporalMatch getMatch() {
		try {
			return temporalMatchHistory.getFirst();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	/**
	 * To be called when predictable vehicle has no valid spatial/temporal
	 * match. Only allowed so many of these before vehicle is made
	 * unpredictable.
	 */
	public void incrementNumberOfBadMatches() {
		++numberOfBadMatches;
	}
	
	/**
	 * Returns if have exceeded the number of allowed bad matches. If so
	 * then vehicle should be made unpredictable.
	 * 
	 * @return
	 */
	public boolean overLimitOfBadMatches() {
		return numberOfBadMatches > CoreConfig.getAllowableNumberOfBadMatches();
	}
	
	/**
	 * Returns the number of sequential bad spatial/temporal matches that
	 * occurred while vehicle was predictable.
	 * 
	 * @return current number of bad matches
	 */
	public int numberOfBadMatches() {
		return numberOfBadMatches;
	}
	
	/**
	 * Returns true if the last AVL report was successfully matched to the
	 * assignment indicating that can generate predictions and arrival/departure
	 * times etc.
	 * 
	 * @return True if last match is valid
	 */
	public boolean lastMatchIsValid() {
		return numberOfBadMatches == 0;
	}
	
	/**
	 * Stores the specified avlReport into the history for the vehicle.
	 * Makes sure that the AVL history doesn't exceed maximum size.
	 * 
	 * @param avlReport
	 */
	public void setAvlReport(AvlReport avlReport) {
		// Add AVL report to history
		avlReportHistory.addFirst(avlReport);
		
		// Truncate list if it is too long or data in it is too old
		while (avlReportHistory.size() > AVL_HISTORY_MAX_SIZE) {
			avlReportHistory.removeLast();
		}
	}
	
	/**
	 * Returns the current Trip for the vehicle. Returns null if there is not
	 * current trip.
	 * 
	 * @return Trip or null.
	 */
	public Trip getTrip() {
		TemporalMatch lastMatch = getMatch();
		return lastMatch!=null ? lastMatch.getTrip() : null;
	}
	
	/**
	 * Returns the current route ID for the vehicle. Returns null if not
	 * currently associated with a trip.
	 * 
	 * @return Route ID or null.
	 */
	public String getRouteId() {
		Trip trip = getTrip();
		return trip!=null ? trip.getRouteId() : null;
	}
	
	/**
	 * Returns the current route short name for the vehicle. Returns null if not
	 * currently associated with a trip.
	 * 
	 * @return route short name or null
	 */
	public String getRouteShortName() {
		Trip trip = getTrip();
		return trip!=null ? trip.getRouteShortName() : null;
	}
	
	/**
	 * Returns true if last temporal match for vehicle indicates that it is at a
	 * layover. A layover stop is when a vehicle can leave route path before
	 * departing this stop since the driver is taking a break.
	 * 
	 * @return true if at a layover
	 */
	public boolean atLayover() {
		TemporalMatch temporalMatch = getMatch();
		if (temporalMatch == null)
			return false;
		else
			return temporalMatch.isLayover();
	}
	
	/**
	 * Returns the next to last temporal match. Returns null if there isn't
	 * one. Useful for when need to compare the previous to last match with
	 * the last one, such as for determining if vehicle has crossed any
	 * stops.
	 * 
	 * @return
	 */
	public TemporalMatch getPreviousMatch() {
		if (temporalMatchHistory.size() >= 2)
			return temporalMatchHistory.get(1);
		else
			return null;
	}

	/**
	 * Returns the current AvlReport. Returns null if there isn't one.
	 * @return
	 */
	public AvlReport getAvlReport() {
		try {
			return avlReportHistory.getFirst();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	/**
	 * Looks in the AvlReport history for the most recent AvlReport that is
	 * at least minDistanceFromCurrentReport from the current AvlReport.
	 * Also makes sure that previous report isn't too old.
	 * 
	 * @param minDistanceFromCurrentReport
	 * @return The previous AvlReport, or null if there isn't one that far away
	 */
	public AvlReport getPreviousAvlReport(double minDistanceFromCurrentReport) {
		// Go through history of AvlReports to find first one that is specified
		// distance away from the current AVL location.
		long currentTime = getAvlReport().getTime();
		Location currentLoc = getAvlReport().getLocation();
		for (AvlReport previousAvlReport : avlReportHistory) {
			// If the previous report is too old then return null
			if (currentTime - previousAvlReport.getTime() > 20 * Time.MS_PER_MIN)
				return null;
			
			// If previous location far enough away from current location
			// then return the previous AVL report.
			Location previousLoc = previousAvlReport.getLocation();
			if (previousLoc.distance(currentLoc) > minDistanceFromCurrentReport) {
				return previousAvlReport;
			}
		}
		
		// Didn't find a previous AvlReport in history that was far enough away
		// so return null
		return null;
	}

	/**
	 * Returns the next to last AvlReport where successfully matched the
	 * vehicle. This isn't necessarily simply the previous AvlReport since that
	 * report might not have been successfully matched. It is important to use
	 * the proper AvlReport when matching a vehicle or such because otherwise
	 * the elapsed time between the last successful match and the current match
	 * would be wrong.
	 * 
	 * @return The last successfully matched AvlReport, or null if no such
	 *         report is available
	 */
	public AvlReport getPreviousAvlReportFromSuccessfulMatch() {
		if (avlReportHistory.size() >= 2+numberOfBadMatches) 
			return avlReportHistory.get(1+numberOfBadMatches);
		else
			return null;
	}

	/**
	 * Returns true if the AVL report has a different block assignment
	 * than what is in the VehicleState. For when reassigning a vehicle
	 * via the AVL feed.
	 * @param avlReport
	 * @return
	 */
	public boolean hasNewBlockAssignment(AvlReport avlReport) {		
		// Return true if assignment being changed
		String previousBlockId = getBlock()==null ? null : getBlock().getId();
		return avlReport.getAssignmentId() != null &&
				 avlReport.getAssignmentType() == AssignmentType.BLOCK_ID &&
				 (previousBlockId == null || !avlReport.getAssignmentId().equals(previousBlockId));
	}
	
	/********************** Getter methods ************************/
	/**
	 * Returns an unmodifiable list of the match history. The most recent
	 * one is first. The size of the list will be not greater than
	 * MATCH_HISTORY_SIZE.
	 * 
	 * @return the match history
	 */
	public List<TemporalMatch> getMatches() {
		return Collections.unmodifiableList(temporalMatchHistory);
	}
	
	/**
	 * The current block assignment. But will be null if vehicle not currently
	 * assigned.
	 * 
	 * @return
	 */
	public Block getBlock() {
		return block;
	}

	/**
	 * Indicates how the vehicle was assigned (via block assignment, route
	 * assignment, auto assignment, etc).
	 * 
	 * @return
	 */
	public BlockAssignmentMethod getAssignmentMethod() {
		return assignmentMethod;
	}

	public Date getAssignmentTime() {
		return assignmentTime;
	}

	public String getVehicleId() {
		return vehicleId;
	}
	
	public boolean isPredictable() {
		return predictable;
	}
	
	/**
	 * Records the specified arrival as one that still needs to be stored to the
	 * db. This is important because can generate arrivals into the future but
	 * need to make sure that the arrival is before the subsequent departure and
	 * can't do so until get additional AVL reports.
	 * 
	 * @param arrival
	 */
	public void setArrivalToStoreToDb(Arrival arrival) {
		this.arrivalToStoreToDb = arrival;
	}
	
	public Arrival getArrivalToStoreToDb() {
		return arrivalToStoreToDb;
	}
	
	/**
	 * Sets the current list of predictions for the vehicle to the
	 * predictions parameter.
	 * 
	 * @param predictions
	 */
	public void setPredictions(List<Prediction> predictions) {
		this.predictions = predictions;
	}
	
	/**
	 * Gets the current list of predictions for the vehicle. Can be null.
	 * @return
	 */
	public List<Prediction> getPredictions() {
		return predictions;
	}
	
	/**
	 * Stores the real-time schedule adherence for the vehicle.
	 * 
	 * @param realTimeSchedAdh
	 */
	public void setRealTimeSchedAdh(TemporalDifference realTimeSchedAdh) {
		this.realTimeSchedAdh = realTimeSchedAdh;
	}
	
	/**
	 * Returns the current real-time schedule adherence for the vehicle,
	 * or null if schedule adherence not currently valid (vehicle is
	 * not predictable or running a non-schedule based assignment).
	 * 
	 * @return
	 */
	public TemporalDifference getRealTimeSchedAdh() {
		if (isPredictable())
			return realTimeSchedAdh;
		else
			return null;
	}
	
	/**
	 * Determines the heading of the vector that defines the stop path segment
	 * that the vehicle is currently on.
	 * 
	 * @return Heading of vehicle according to path segment. NaN if not
	 *         currently matched or there is no heading for that segment.
	 */
	public float getPathHeading() {
		SpatialMatch match = getMatch();
		if (match == null)
			return Float.NaN;
		
		StopPath stopPath = getTrip().getStopPath(match.getStopPathIndex());
		return stopPath.getSegmentVector(match.getSegmentIndex()).getHeading();
	}
	
	/**
	 * Normally uses the heading from getPathHeading(). But if that returns NaN
	 * then uses heading from last AVL report, though that might be NaN as well.
	 * This can be better then always using heading from AVL report since that 
	 * often won't line up with the path.
	 * 
	 * @return The best heading for a vehicle
	 */
	public float getHeading() {
		float heading = getPathHeading();
		if (Float.isNaN(heading))
			return getAvlReport().getHeading();
		else
			return heading;
	}
	
	@Override
	public String toString() {
		return "VehicleState [" 
				+ "vehicleId=" + vehicleId 
				+ ", blockId=" + (block==null? null : block.getId())
				+ ", assignmentMethod=" + assignmentMethod
				+ ", assignmentTime=" + assignmentTime 
				+ ", predictable=" + predictable 
				+ ", realTimeSchedAdh=" + realTimeSchedAdh
				+ ", pathHeading=" + StringUtils.twoDigitFormat(getHeading())
				+ ", getMatch()=" + getMatch()
				+ ", getAvlReport()=" + getAvlReport()
				+ (arrivalToStoreToDb != null ? "\n  arrivalToStoreToDb=" + arrivalToStoreToDb : "")
				//+ ",\n  block=" + block // Block info too verbose so commented out
				//+ ",\n  temporalMatchHistory=" + temporalMatchHistory 
				//+ ",\n  avlReportHistory=" + avlReportHistory 
				+ "]";
	}

	public String toStringVerbose() {
		return "VehicleState [" 
				+ "vehicleId=" + vehicleId 
				+ ", blockId=" + (block==null? null : block.getId())
				+ ", assignmentMethod=" + assignmentMethod
				+ ", assignmentTime=" + assignmentTime 
				+ ", predictable=" + predictable 
				+ ", realTimeSchedAdh=" + realTimeSchedAdh
				+ ", pathHeading=" + StringUtils.twoDigitFormat(getHeading())
				+ ", getMatch()=" + getMatch()
				+ ", getAvlReport()=" + getAvlReport()
				//+ ", \nblock=" + block // Block info too verbose so commented out
				+ ",\n  temporalMatchHistory=" + temporalMatchHistory 
				+ ",\n  avlReportHistory=" + avlReportHistory 
				+ (arrivalToStoreToDb != null ? "\n  arrivalToStoreToDb=" + arrivalToStoreToDb : "")
				+ "]";
	}

	/**
	 * Stores the last arrival time so that can make sure that departure
	 * times are after the arrival times.
	 * @param arrivalTime
	 */
	public void setLastArrivalTime(long arrivalTime) {
		lastArrivalTime = arrivalTime;
	}

	/**
	 * Returns the last stored arrival time so can make sure that departure
	 * times are after the arrival times.
	 * 
	 * @return
	 */
	public long getLastArrivalTime() {
		return lastArrivalTime;
	}
}
