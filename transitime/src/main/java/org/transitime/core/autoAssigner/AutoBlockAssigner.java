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

package org.transitime.core.autoAssigner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.BooleanConfigValue;
import org.transitime.config.DoubleConfigValue;
import org.transitime.config.IntegerConfigValue;
import org.transitime.configData.CoreConfig;
import org.transitime.core.BlocksInfo;
import org.transitime.core.SpatialMatch;
import org.transitime.core.SpatialMatcher;
import org.transitime.core.TemporalDifference;
import org.transitime.core.TemporalMatch;
import org.transitime.core.TemporalMatcher;
import org.transitime.core.TravelTimes;
import org.transitime.core.VehicleState;
import org.transitime.core.dataCache.VehicleDataCache;
import org.transitime.core.dataCache.VehicleStateManager;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.Trip;
import org.transitime.utils.Time;

/**
 * Singleton class for automatically assigning a vehicle to an available
 * block by determining both spatial and temporal match.
 *
 * @author SkiBu Smith
 *
 */
public class AutoBlockAssigner {

	/*********************** Config params *****************************/
	
	private static BooleanConfigValue autoAssignerEnabled =
			new BooleanConfigValue(
					"transitime.autoBlockAssigner.autoAssignerEnabled", 
					false, 
					"Set to true to enable the auto assignment feature where "
					+ "the system tries to assign vehicle to an available block");
	
	private static BooleanConfigValue ignoreAvlAssignments =
			new BooleanConfigValue(
					"transitime.autoBlockAssigner.ignoreAvlAssignments", 
					false, 
					"For when want to test automatic assignments. When set to "
					+ "true then system ignores assignments from AVL feed so "
					+ "vehicles need to be automatically assigned instead");
	public boolean ignoreAvlAssignments() {
		return ignoreAvlAssignments.getValue();
	}
	
	private static DoubleConfigValue minDistanceFromCurrentReport =
			new DoubleConfigValue(
					"transitime.autoBlockAssigner.minDistanceFromCurrentReport", 
					100.0, 
					"AutoBlockAssigner looks at two AVL reports to match "
					+ "vehicle. This parameter specifies how far away those "
					+ "AVL reports need to be sure that the vehicle really "
					+ "is moving and in service. If getting incorrect matches "
					+ "then this value should likely be increased.");
	
	private static IntegerConfigValue allowableEarlySeconds =
			new IntegerConfigValue(
					"transitime.autoBlockAssigner.allowableEarlySeconds",
					3*Time.SEC_PER_MIN,
					"How early a vehicle can be in seconds and still be auto "
					+ "assigned to a block");
	
	private static IntegerConfigValue allowableLateSeconds =
			new IntegerConfigValue(
					"transitime.autoBlockAssigner.allowableLateSeconds",
					5*Time.SEC_PER_MIN,
					"How late a vehicle can be in seconds and still be auto "
					+ "assigned to a block");
	
	/*********************** Singleton ********************************/
	
	private static final AutoBlockAssigner singleton = new AutoBlockAssigner();
	
	/*********************** Logging **********************************/
	
	private static final Logger logger = LoggerFactory
			.getLogger(AutoBlockAssigner.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor declared private because singleton class
	 */
	private AutoBlockAssigner() {	
	}
	
	/**
	 * Get singleton object
	 * 
	 * @return the singleton object
	 */
	public static AutoBlockAssigner getInstance() {
		return singleton;
	}
	
	/**
	 * Determines if a block doesn't have a non-schedule based vehicle
	 * associated with it. This means that the block assignment is available for
	 * trying to automatically assign a vehicle to it. Schedule based vehicles
	 * don't count because even when have a schedule based vehicle still want to
	 * assign a real vehicle to that assignment when possible.
	 * 
	 * @param blockId
	 *            The block assignment to examine
	 * @return True if block is available to be assigned (doesn't have a regular
	 *         vehicle assigned to it.
	 */
	private boolean isBlockUnassigned(String blockId) {
		Collection<String> vehicleIdsForBlock = 
				VehicleDataCache.getInstance().getVehiclesByBlockId(blockId);
		// If no vehicles associated with the block then it is definitely
		// unassigned.
		if (vehicleIdsForBlock.isEmpty())
			return true;

		// There are vehicles assigned to the block but still need to see if
		// they are schedule based vehicles or not
		for (String vehicleId : vehicleIdsForBlock) {
			// If a regular vehicle instead of one for schedule based
			// predictions then the block has a vehicle assigned to it,
			// meaning it is not unassigned
			VehicleState vehiclestate = VehicleStateManager.getInstance()
					.getVehicleState(vehicleId);
			if (!vehiclestate.isForSchedBasedPreds())
				return false;
		}
		
		// Block doesn't have a non-schedule based vehicle so it is unassigned
		return true;
	}

	/**
	 * Determines which blocks are currently active and are not assigned to a
	 * vehicle, meaning that they are available for assignment.
	 * 
	 * @return List of blocks that are available for assignment. Can be empty
	 *         but not null
	 */
	private List<Block> unassignedBlocks() {
		List<Block> currentlyUnassignedBlocks = new ArrayList<Block>();
		List<Block> activeBlocks = BlocksInfo.getCurrentlyActiveBlocks();
		for (Block block : activeBlocks) {
			if (isBlockUnassigned(block.getId())) {
				// No vehicles assigned to this active block so should see
				// if the vehicle currently trying to assign can match to it
				currentlyUnassignedBlocks.add(block);
			}
		}

		return currentlyUnassignedBlocks;
	}
	
	/**
	 * Determines the best match by looking at both the current AVL report and
	 * the previous one. Only for block assignments that do not have a schedule.
	 * 
	 * @param avlReport
	 * @param previousAvlReport
	 * @param block
	 * @return The best adequate match, or null if there isn't an adequate match
	 */
	private TemporalMatch bestNoScheduleMatch(AvlReport avlReport,
			AvlReport previousAvlReport, Block block) {
		if (!block.isNoSchedule()) {
			logger.error("Called bestNoScheduleMatch() on block that has a "
					+ "schedule. {}", block);
			return null;
		}
		
		// Determine all potential spatial matches for the block that are 
		// not layovers. Won't be a layover match anyways since this method
		// is only for use with no schedule assignments.
		List<Trip> potentialTrips = block.getTripsCurrentlyActive(avlReport);
		List<SpatialMatch> spatialMatches = SpatialMatcher
				.getSpatialMatchesIgnoringLayovers(avlReport,
						potentialTrips, block);
		if (spatialMatches.isEmpty())
			return null;

		// Determine all possible spatial matches for the previous AVL report so
		// that can make sure that it too matches the assignment.
		List<SpatialMatch> prevSpatialMatches = SpatialMatcher
				.getSpatialMatchesIgnoringLayovers(previousAvlReport,
						potentialTrips, block);
		if (prevSpatialMatches.isEmpty())
			return null;
		
		// Determine params needed in the following for loop
		int timeOfDayInSecs =
				Core.getInstance().getTime()
						.getSecondsIntoDay(previousAvlReport.getTime());
		long avlTimeDifferenceMsec = 
				avlReport.getTime() - previousAvlReport.getTime();
		
		// Go through each spatial match for both the current AVL report and
		// the previous AVL report. Find one where the expected travel time
		// closely matches the time between the AVL reports.
		TemporalDifference bestTemporalDifference = null;
		SpatialMatch bestSpatialMatch = null;
		for (SpatialMatch prevSpatialMatch : prevSpatialMatches) {
			for (SpatialMatch spatialMatch : spatialMatches) {
				// Determine according to the historic travel times how long
				// it was expected to take to travel from the previous match
				// to the current one.
				int expectedTravelTimeMsec =
						TravelTimes.getInstance()
								.expectedTravelTimeBetweenMatches(
										avlReport.getVehicleId(),
										timeOfDayInSecs, prevSpatialMatch,
										spatialMatch);
				TemporalDifference differenceFromExpectedTime =
						new TemporalDifference(expectedTravelTimeMsec
								- avlTimeDifferenceMsec);

				// If the travel time is too far off from the time between the 
				// AVL reports then it is not a good match so continue on to 
				// check out the other spatial matches.
				if (!differenceFromExpectedTime.isWithinBounds(
						allowableEarlySeconds.getValue(),
						allowableLateSeconds.getValue()))
					continue;
				
				// If this match is the best one found temporally then remember it
				if (differenceFromExpectedTime.betterThan(bestTemporalDifference)) {
					bestTemporalDifference = differenceFromExpectedTime;
					bestSpatialMatch = spatialMatch;
				}
			}
		}
		
		// Return the best temporal match if an adequate one was found
		if (bestSpatialMatch == null)
			return null;
		else {
			TemporalMatch bestTemporalMatch =
					new TemporalMatch(bestSpatialMatch, bestTemporalDifference);
			return bestTemporalMatch;
		}
	}
	
	/**
	 * Determines best non-layover match for the AVL report to the specified
	 * block. First finds spatial matches and then finds one that best matches
	 * to the schedule. If the match is adequate spatial and temporally then 
	 * the match is returned. Intended for schedule based blocks only.
	 * 
	 * @param avlReport
	 * @param block
	 *            The block to match the AVL report to
	 * @return The best match if there is one. Null if there is not a valid
	 *         match
	 */
	private TemporalMatch bestTemporalMatch(AvlReport avlReport, Block block) {
		// Determine all potential spatial matches for the block that are 
		// not layovers. If match is to a layover can ignore it since 
		// layover matches are far too flexible to really be considered 
		// a spatial match
		List<Trip> potentialTrips = block.getTripsCurrentlyActive(avlReport);
		List<SpatialMatch> spatialMatches = SpatialMatcher
				.getSpatialMatchesIgnoringLayovers(avlReport,
						potentialTrips, block);

		TemporalMatch bestMatch = TemporalMatcher.getInstance()
				.getBestTemporalMatchComparedToSchedule(avlReport,
						spatialMatches);

		// Want to be pretty restrictive about matching to avoid false 
		// positives. At the same time, want to not have a temporal match
		// that matches but not all that well cause the auto matcher to
		// think that can't match the vehicle due to the matches being
		// ambiguous. Therefore want to be more restrictive temporally 
		// with respect to the temporal matches used. So throw out the
		// temporal match unless it is pretty close to the scheduled time.
		if (bestMatch != null) {
			TemporalDifference diff = bestMatch.getTemporalDifference();
			boolean isWithinBounds = diff.isWithinBounds(
					allowableEarlySeconds.getValue(),
					allowableLateSeconds.getValue());
			if (!isWithinBounds) {
				// Vehicle is too early or too late to auto match the block so
				// return null.
				logger.debug("For vehicleId={} for blockId={} the temporal "
						+ "difference was not within allowed bounds. {}",
						avlReport.getVehicleId(), block.getId(), diff);
				return null;
			}
		}
		
		// Return the temporal match, which could be null
		return bestMatch;	
	}

	/**
	 * Returns best schedule based match. Only for schedule based block
	 * assignments.
	 * 
	 * @param avlReport
	 * @param previousAvlReport
	 * @param block
	 * @return Best TemporalMatch to the block assignment, or null if no
	 *         adequate match
	 */
	private TemporalMatch bestScheduleMatch(AvlReport avlReport,
			AvlReport previousAvlReport, Block block) {
		if (block.isNoSchedule()) {
			logger.error("Called bestScheduleMatch() on block that does not "
					+ "have a schedule. {}", block.toShortString());
			return null;
		}		

		TemporalMatch bestMatch = bestTemporalMatch(avlReport, block);
		if (bestMatch != null) {
			// Found a valid match for the AVL report to the block
			logger.debug("For vehicleId={} and blockId={} and AVL "
					+ "report={} found bestMatch={}",
					avlReport.getVehicleId(), block.getId(), avlReport, 
					bestMatch);
			
			// Make sure that previous AVL report also matches and 
			// that it matches to block before the current AVL report
			TemporalMatch previousAvlReportBestMatch = 
					bestTemporalMatch(previousAvlReport, block);
			if (previousAvlReportBestMatch != null
					&& previousAvlReportBestMatch.lessThanOrEqualTo(bestMatch)) { 
				// Previous AVL report also matches appropriately. 
				// Therefore record this temporal match as appropriate one.
				logger.debug("For vehicleId={} also found appropriate "
						+ "match for previous AVL report {}. Previous "
						+ "match was {}", 
						avlReport.getVehicleId(), previousAvlReport, 
						previousAvlReportBestMatch);	
				return bestMatch;
			} else {
				logger.debug("For vehicleId={} did NOT get valid match for "
						+ "previous AVL report {}. Previous match was {} ", 
						avlReport.getVehicleId(), previousAvlReport, 
						previousAvlReportBestMatch);					
			}
		}

		// Did not find an adequate match
		return null;
	}
	
	/**
	 * Goes through all the currently active blocks and tries to match the AVL
	 * report to them. Returns list of valid temporal matches. Ignores layover
	 * matches since they are too lenient to indicate a valid match. Also
	 * requires a previous AVL report to match appropriately to make sure that
	 * vehicle really matches and isn't just sitting there and isn't going in
	 * other direction or crossing route and matching only momentarily.
	 * 
	 * @param vehicleState
	 *            Info on vehicle to match
	 * @return A non-null list of TemporalMatches. Will be empty if there are no
	 *         valid matches.
	 */
	private List<TemporalMatch> determineTemporalMatches(
			VehicleState vehicleState) {
		// The list of matches to return
		List<TemporalMatch> validMatches = new ArrayList<TemporalMatch>();
		
		AvlReport avlReport = vehicleState.getAvlReport();

		// Only want to match if there is also a previous AVL report
		// that is significantly away from the current report. This
		// way we avoid trying to match non-moving vehicles which are
		// not in service.
		double minDistance = minDistanceFromCurrentReport.getValue();
		AvlReport previousAvlReport =
				vehicleState.getPreviousAvlReport(minDistance);
		if (previousAvlReport == null) {
			// There was no previous AVL report far enough away from the 
			// current one so return empty list of matches
			logger.debug("In AutoBlockAssigner.bestMatch() could not find "
					+ "valid previous AVL report for vehicleId={} further away "
					+ "than {}m from current AVL report {}",
					vehicleState.getVehicleId(), minDistance,
					vehicleState.getAvlReport());
			return validMatches;
		}
		
		// Determine which blocks to examine. If agency configured such that
		// blocks are to be exclusive then only look at the ones currently
		// not used. But if not to be exclusive, such as for no schedule based
		// routes, then look at all active blocks.
		List<Block> blocksToExamine = CoreConfig.exclusiveBlockAssignments() ? 
				unassignedBlocks() : BlocksInfo.getCurrentlyActiveBlocks();
		
		if (blocksToExamine.isEmpty()) {
			logger.info("No currently active blocks to assign vehicleId={} to.",
					vehicleState.getVehicleId());
		}
		
		// For each active block that is currently unassigned...
		for (Block block : blocksToExamine) {
			if (logger.isDebugEnabled()) {
				logger.debug("For vehicleId={} examining block for match. {}", 
						vehicleState.getVehicleId(), block.toShortString());
			}

			// Determine best match for the block depending on whether the 
			// block is schedule based or not
			TemporalMatch bestMatch =
					block.isNoSchedule() ? bestNoScheduleMatch(avlReport,
							previousAvlReport, block) : bestScheduleMatch(
							avlReport, previousAvlReport, block);
			if (bestMatch != null)
				validMatches.add(bestMatch);			
		}

		// Return the valid matches that were found
		return validMatches;
	}
	
	/**
	 * For trying to match vehicle to a active but currently unused block and
	 * the auto assigner is enabled. If auto assigner is not enabled then
	 * returns null. Goes through all the currently active blocks and tries to
	 * match the AVL report to them. Returns a TemporalMatch if there is a
	 * single block that can be successfully matched to. Ignores layover matches
	 * since they are too lenient to indicate a valid match. Also requires a
	 * previous AVL report to match appropriately to make sure that vehicle
	 * really matches and isn't just sitting there and isn't going in other
	 * direction or crossing route and matching only momentarily.
	 * 
	 * @param vehicleState
	 *            Info on the vehicle to match
	 * @return A TemporalMatch if there is a single valid one, otherwise null
	 */
	public TemporalMatch autoAssignVehicleToBlockIfEnabled(
			VehicleState vehicleState) {
		// If the auto assigner is not enabled then simply return null for 
		// the match
		if (!autoAssignerEnabled.getValue())
			return null;
		
		String vehicleId = vehicleState.getVehicleId();
		logger.info("Determining auto assignment match for vehicleId={}", 
				vehicleId);
		
		// Determine all the valid matches
		List<TemporalMatch> matches = determineTemporalMatches(vehicleState);
		
		// If no matches then not successful
		if (matches.isEmpty()) {
			logger.debug("Found no valid matches for vehicleId={}", vehicleId);
			return null;
		}
		
		// If more than a single match then situation is ambiguous and we can't
		// consider that a match 
		if (matches.size() > 1) {
			logger.debug("Found multiple matches ({}) for vehicleId={}. {}", 
					matches.size(), vehicleId, matches);
			return null;
		}
		
		// Found a single match so return it
		logger.info("Found single valid match for vehicleId={}. {}", 
				vehicleId, matches.get(0));
		return matches.get(0);
	}
}
