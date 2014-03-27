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

import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.AvlReport.AssignmentType;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.Trip;
import org.transitime.ipc.data.Prediction;

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
	private LinkedList<TemporalMatch> temporalMatchHistory = 
			new LinkedList<TemporalMatch>();
	private LinkedList<AvlReport> avlReportHistory =
			new LinkedList<AvlReport>();
	private VehicleAtStopInfo vehicleAtStopInfo;
	private List<Prediction> predictions;
	
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
	 *            The current block assignment for the vehicle
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
	 * VehicleState.predictable is set to false.
	 * 
	 * @param match
	 */
	public void setMatch(TemporalMatch match) {
		// Add match to history
		temporalMatchHistory.addFirst(match);
		
		// Set predictability
		if (match == null) {
			predictable = false;
		}
		
		// Truncate list if it has gotten too long
		while (temporalMatchHistory.size() > MATCH_HISTORY_MAX_SIZE) {
			temporalMatchHistory.removeLast();
		}
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
	 * Returns the last temporal match. Returns null if there isn't one.
	 * @return
	 */
	public TemporalMatch getLastMatch() {
		try {
			return temporalMatchHistory.getFirst();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	/**
	 * Returns the current Trip for the vehicle. Returns null if there is not
	 * current trip.
	 * 
	 * @return Trip or null.
	 */
	public Trip getTrip() {
		TemporalMatch lastMatch = getLastMatch();
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
	 * Returns true if last temporal match for vehicle indicates that it
	 * is at a layover.
	 * @return true if at a layover
	 */
	public boolean atLayover() {
		TemporalMatch temporalMatch = getLastMatch();
		if (temporalMatch == null)
			return false;
		else
			return temporalMatch.atLayover();
	}
	
	/**
	 * Returns the next to last temporal match. Returns null if there isn't
	 * one. Useful for when need to compare the previous to last match with
	 * the last one, such as for determining if vehicle has transversed any
	 * stops.
	 * 
	 * @return
	 */
	public TemporalMatch getPreviousToLastMatch() {
		if (temporalMatchHistory.size() >= 2)
			return temporalMatchHistory.get(1);
		else
			return null;
	}

	/**
	 * Returns the last AvlReport. Returns null if there isn't one.
	 * @return
	 */
	public AvlReport getLastAvlReport() {
		try {
			return avlReportHistory.getFirst();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	/**
	 * Returns the next to last AvlReport. Returns null if there isn't
	 * one.
	 * 
	 * @return
	 */
	public AvlReport getPreviousToLastAvlReport() {
		if (avlReportHistory.size() >= 2) 
			return avlReportHistory.get(1);
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
	
	public Block getBlock() {
		return block;
	}

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

	public void setVehicleAtStopInfo(VehicleAtStopInfo vehicleAtStopInfo) {
		this.vehicleAtStopInfo = vehicleAtStopInfo;
	}

	/**
	 * Note: this is different from whether the SpatialMatch is currently at
	 * a stop. The SpatialMatch simply looks at the current stop indicated by
	 * the path index to determine if it is at that stop. But vehicleAtStopInfo
	 * keeps track of which stop the vehicle is on with respect to 
	 * arrivals/departures. It could match to a previous stop if the allowable
	 * distance after the stop is large, such as for a terminal.
	 * @return
	 */
	public VehicleAtStopInfo getVehicleAtStopInfo() {
		return vehicleAtStopInfo;
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
	
	@Override
	public String toString() {
		return "VehicleState [" 
				+ "vehicleId=" + vehicleId 
				+ ", blockId=" + (block==null? null : block.getId())
				+ ", assignmentMethod=" + assignmentMethod
				+ ", assignmentTime=" + assignmentTime 
				+ ", predictable=" + predictable 
				+ ", vehicleAtStopInfo=" + vehicleAtStopInfo
				+ ", getLastMatch()=" + getLastMatch()
				+ ", getLastAvlReport()=" + getLastAvlReport()
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
				+ ", vehicleAtStopInfo=" + vehicleAtStopInfo
				+ ", getLastMatch()=" + getLastMatch()
				+ ", getLastAvlReport()=" + getLastAvlReport()
				//+ ", \nblock=" + block // Block info too verbose so commented out
				+ ",\n  temporalMatchHistory=" + temporalMatchHistory 
				+ ",\n  avlReportHistory=" + avlReportHistory 
				+ "]";
	}
}
