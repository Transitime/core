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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.transitime.configData.AvlConfig;
import org.transitime.configData.CoreConfig;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.Route;
import org.transitime.db.structs.Trip;
import org.transitime.db.structs.VectorWithHeading;
import org.transitime.utils.Geo;
import org.transitime.utils.Time;

/**
 * For determining possible spatial matches. A spatial match is when the AVL
 * report location is within allowable distance of the path segment and the
 * heading is OK and the distance to the segment is a local minimum (it is a
 * best match).
 * 
 * @author SkiBu Smith
 * 
 */
public class SpatialMatcher {

	// So that know where to start searching from
	private SpatialMatch startSearchSpatialMatch = null;
	
	// The array to be returned that will contain the best spatial matches.
	private List<SpatialMatch> spatialMatches = new ArrayList<SpatialMatch>();

	// For keeping track of whether getting closer or further away
	private double previousDistanceToSegment = Double.MAX_VALUE;

	// For keeping track of potential matches where heading and
	// distance to segment are acceptable.
	private SpatialMatch previousPotentialSpatialMatch = null;

	// For keeping track of match with best distance. This is useful for
	// logging in case something goes wrong. It lets one determine if
	// need to make the system more lenient.
	private SpatialMatch smallestDistanceSpatialMatch = null;

	private static final Logger logger = LoggerFactory
			.getLogger(SpatialMatcher.class);

	/********************** Member Functions **************************/

	/**
	 * Declared private because only the public static members should be
	 * creating a SpatialMatcher. This is because need a new SpatialMatcher
	 * every time doing a match. Only the public static members can enforce this
	 * requirement.
	 */
	private SpatialMatcher() {
	}

	/**
	 * Specifies where to start when searching for spatial matches. This
	 * is important because due to the noise of GPS and also the flexibility
	 * of layovers a best spatial match might be determined to be before the
	 * previous match. But that would be a problem when determining arrivals/
	 * departures and such. Therefore need to only search starting from
	 * previous match.
	 * 
	 * @param startSearchSpatialMatch
	 */
	private void setStartOfSearch(SpatialMatch startSearchSpatialMatch) {
		this.startSearchSpatialMatch = startSearchSpatialMatch;
	}

	/**
	 * Goes through entire TripPattern for specified Trip and determines spatial
	 * matches.
	 * 
	 * @param avlReport
	 * @param block
	 * @param trip
	 * @return
	 */
	private List<SpatialMatch> getSpatialMatchesForTrip(AvlReport avlReport,
			Block block, Trip trip) {
		// Start looking for matches at the beginning of the trip.
		Indices indices = new Indices(block, block.getTripIndex(trip), 
				0, // stopPathIndex
				0); // segmentIndex

		// Loop through stopPaths and segments until reach end of trip.
		do {
			processPossiblePotentialMatch(avlReport, indices);

			// For next iteration through while loop
			indices.increment();
		} while (!indices.atBeginningOfTrip());

		// Need to handle boundary condition. Done looking ahead but
		// the end match might be a potential one even if was continuing
		// to improve the match. Therefore if there was a potential
		// match then should store it.
		if (previousPotentialSpatialMatch != null) {
			// There was a potential match and now things are getting
			// worse so that was a local minimum. Therefore this is
			// one of the spatial matches to be returned.
			spatialMatches.add(previousPotentialSpatialMatch);
		}

		// Return the list of local matches
		return spatialMatches;
	}

	/**
	 * Goes through the Block assignment data and determines the closest spatial
	 * matches. For first matching a vehicle to a block assignment.
	 * 
	 * @param avlReport
	 * @param tripPatternsToInvestigate
	 * @param block
	 *            So can get block ID for logging
	 * @return
	 */
	public static List<SpatialMatch> getSpatialMatches(AvlReport avlReport,
			List<Trip> tripsToInvestigate, Block block) {
		List<SpatialMatch> spatialMatchesForAllTrips = new ArrayList<SpatialMatch>();

		// So can reuse spatial matches if looking at same trip pattern
		Set<String> tripPatternIdsCovered = new HashSet<String>();

		for (Trip trip : tripsToInvestigate) {
			if (tripPatternIdsCovered.contains(trip.getTripPattern().getId())) {
				// Already found spatial matches for this trip pattern
				// so use them instead of going through the whole trip
				// pattern again.
				boolean foundTripPattern = false;
				// So can determine when to stop copying. Need to stop
				// when starting to look at another trip.
				String tripIdThatFoundTripPatternFor = null;
				List<SpatialMatch> matchListForIteration = new ArrayList<SpatialMatch>(
						spatialMatchesForAllTrips);
				for (SpatialMatch spatialMatch : matchListForIteration) {
					String spatialMatchTripPatternId = spatialMatch.getTrip()
							.getTripPattern().getId();
					String currentTripPatternId = trip.getTripPattern().getId();
					if (spatialMatchTripPatternId.equals(currentTripPatternId)
							&& (tripIdThatFoundTripPatternFor == null || tripIdThatFoundTripPatternFor
									.equals(spatialMatch.getTrip().getId()))) {
						foundTripPattern = true;
						SpatialMatch spatialMatchCopy = new SpatialMatch(
								spatialMatch, trip);
						spatialMatchesForAllTrips.add(spatialMatchCopy);
						tripIdThatFoundTripPatternFor = spatialMatch.getTrip()
								.getId();
					} else {
						// If trip pattern or trip ID for spatial matches is now
						// different
						// then have finished copying all the spatial matches
						// for
						// this trip pattern. Therefore done with the for loop.
						if (foundTripPattern)
							break;
					}
				}
			} else {
				// Haven't already examined this trip pattern for spatial
				// matches so do so now.
				List<SpatialMatch> spatialMatchesForTrip = (new SpatialMatcher())
						.getSpatialMatchesForTrip(avlReport, block, trip);
				spatialMatchesForAllTrips.addAll(spatialMatchesForTrip);
				tripPatternIdsCovered.add(trip.getTripPattern().getId());
			}
		}

		logger.debug("Finished determining spatial matches for vehicleId={} "
				+ "location={} and blockId={}. The list of spatial "
				+ "matches is {}", avlReport.getVehicleId(),
				avlReport.getLocation(), block.getId(),
				spatialMatchesForAllTrips);

		return spatialMatchesForAllTrips;
	}

	/**
	 * Returns the max distance that an AVL report can be from the segment.
	 * Currently uses the max distance for the route if it is set. If max
	 * distance for route is not set then uses the global 
	 * CoreConfig.getMaxDistanceFromSegment().
	 * 
	 * @param indices
	 * @return
	 */
	private double getMaxDistanceFromSegment(Indices indices) {
		Route route = indices.getRoute();
		double maxDistance = route.getMaxDistanceFromSegment();
		if (Double.isNaN(maxDistance))
			maxDistance = CoreConfig.getMaxDistanceFromSegment();
		return maxDistance;
	}
	
	/**
	 * For determining possible spatial matches. A spatial match is when the AVL
	 * report location is within allowable distance of the path segment and the
	 * heading is OK and the distance to the segment is a local minimum (it is a
	 * best match). To be called for a series of segments.
	 * 
	 * @param avlReport
	 *            The new AVL report
	 * @param potentialMatchIndices
	 *            where in block to start looking for matches
	 */
	private void processPossiblePotentialMatch(AvlReport avlReport,
			Indices potentialMatchIndices) {
		// Convenience variables
		VectorWithHeading segmentVector = potentialMatchIndices.getSegment();
		double distanceToSegment = 
				segmentVector.distance(avlReport.getLocation());
		double distanceAlongSegment = 
				segmentVector.matchDistanceAlongVector(avlReport.getLocation());
		boolean atLayover = potentialMatchIndices.isLayover();

		// Make sure only searching starting from previous spatial match. 
		// Otherwise would screw up determination of arrivals/departures etc.
		if (startSearchSpatialMatch != null) {
			// If looking at previous index then something is really wrong.
			// Don't need to see if this is a match.
			if (potentialMatchIndices.lessThan(startSearchSpatialMatch.getIndices())) {
				logger.error("For vehicleId={} looking at segment that is " +
						"before the segment of the previous match, which " +
						"should not happen. potentialMatchIndices={} " +
						"startSearchSpatialMatch={}",
						avlReport.getVehicleId(), 
						potentialMatchIndices, startSearchSpatialMatch);
				return;
			} else
			// If match is for before the previous one then need to adjust
			// it so that it is the same as the previous one. That way
			// can't end up with a match before the previous one, which
			// would screw up determination of arrivals/departures.
			if (potentialMatchIndices.equals(startSearchSpatialMatch.getIndices())
					&& distanceAlongSegment < 
						startSearchSpatialMatch.getDistanceAlongSegment()) {
				// The current match would be before the starting point so
				// adjust it.
				logger.debug("For vehicleId={} the spatial match was before " +
						"the starting previous match so will use the previous " +
						"match. original distanceAlongSegment={} and " +
						"startSearchSpatialMatch={}",
						avlReport.getVehicleId(), 
						Geo.distanceFormat(distanceAlongSegment), 
						startSearchSpatialMatch);
				distanceAlongSegment = 
						startSearchSpatialMatch.getDistanceAlongSegment();
				distanceToSegment = 
						startSearchSpatialMatch.getDistanceToSegment();
			}
		}
		
		// If layover then need to set distanceAlongSegment to the length of 
		// the path so that the match is with the actual stop.
		if (atLayover) {
			distanceAlongSegment = potentialMatchIndices.getSegment().length();
		}
		
		// Create the SpatialMatch object for the specified indices
		SpatialMatch spatialMatch = new SpatialMatch(
				avlReport.getVehicleId(), 
				potentialMatchIndices.getBlock(),
				potentialMatchIndices.getTripIndex(),
				potentialMatchIndices.getStopPathIndex(),
				potentialMatchIndices.getSegmentIndex(), 
				distanceToSegment,
				distanceAlongSegment);
		logger.debug("For vehicleId={} examining match to see if it should " +
				"be included in list of spatial matches. {}", 
				avlReport.getVehicleId(), spatialMatch);
		
		// If the match is better than the previous one then it trending 
		// towards a minimum so keep track of it if heading and distance are OK. 
		if (distanceToSegment <= previousDistanceToSegment) {
			boolean headingOK = 
					segmentVector.headingOK(avlReport.getHeading(), 
							CoreConfig.getMaxHeadingOffsetFromSegment());
			boolean distanceOK = 
					distanceToSegment < getMaxDistanceFromSegment(potentialMatchIndices);
			if (headingOK && distanceOK) {
				// Heading and distance OK so store this as a potential match
				previousPotentialSpatialMatch = spatialMatch;
				
				logger.debug("For vehicleId={} distanceToSegment={} is better " +
						"and because heading and distance are " +
						"OK keeping track of this spatial match as a potential " +
						"best spatial match", 
						avlReport.getVehicleId(), 
						Geo.distanceFormat(distanceToSegment));				
			} else {
				// Heading or distance not OK so don't store as potential match.
				// Simply log what is happening.
				logger.debug("For vehicleId={} distanceToSegment={} is better " +
						"than previousDistanceToSegment={} but headingOK={} " +
						"distanceOK={} so not keeping track of this match " +
						"as a potential best spatial match", 
						avlReport.getVehicleId(), 
						Geo.distanceFormat(distanceToSegment), 
						Geo.distanceFormat(previousDistanceToSegment), 
						headingOK, distanceOK);
			}
		} else {
			// This match is not as good as previous one which means that 
			// moving away from a minimum. If have a previous potential
			// match then add it to the list of spatial matches.
			if (previousPotentialSpatialMatch != null) {
				// Match is further away than for previous potential match then
				// have passed by a minimum so store the previous spatial match.
				spatialMatches.add(previousPotentialSpatialMatch);

				logger.debug("For vehicleId={} since there was a previous " +
						"good spatial match and distanceToSegment={} is " +
						"further away than previousDistanceToSegment={}, " +
						"adding the previous spatial match to the list. {}",
						avlReport.getVehicleId(), 
						Geo.distanceFormat(distanceToSegment), 
						Geo.distanceFormat(previousDistanceToSegment), 
						previousPotentialSpatialMatch);

				// Set previousPotentialSpatialMatch to null to indicate 
				// that have already added this match to list
				previousPotentialSpatialMatch = null;
			} else {
				// Moving away from minimum but if there was a valid minimum it
				// was already stored. Therefore simply log what is happening.
				logger.debug("For vehicleId={} distanceToSegment={} is worse " +
						"than previousDistanceToSegment={} but there was no " +
						"previousPotentialSpatialMatch meaning didn't just go " +
						"past a minimum. Therefore no previous match to add " +
						"to list of spatial matches",
						avlReport.getVehicleId(), 
						Geo.distanceFormat(distanceToSegment), 
						Geo.distanceFormat(previousDistanceToSegment));
			}
		}
		
		// Remember the distance to the segment for when checking the
		// next indices for spatial match.
		previousDistanceToSegment = distanceToSegment;
		
		// A layover is always a spatial match since the vehicle is allowed to
		// be off of the route there. So always add layovers to the list of
		// spatial matches.
		if (atLayover) {
			logger.debug("For vehicleId={} segment is at a layover so adding " +
					"it to list of spatial matches. {}",
					avlReport.getVehicleId(), spatialMatch);
			spatialMatches.add(spatialMatch);
		}

		// Keep track of best spatial match even if the distance from vehicle
		// to the match is greater than the allowable distance. This is
		// handy in case there is a problem since can log the best match
		// and see if there is a heading problem or need need make the
		// allowable distance more lenient.
		if (smallestDistanceSpatialMatch == null 
				|| distanceToSegment < 
					smallestDistanceSpatialMatch.getDistanceToSegment()) {
			smallestDistanceSpatialMatch = spatialMatch;
		}

	}
	
	/**
	 * Starts at the previous match and goes from that point forward through the
	 * block assignment looking for the best spatial matches.
	 * 
	 * @param vehicleState
	 *            the previous vehicle state
	 * @return list of possible spatial matches
	 */
	public static List<SpatialMatch> getSpatialMatches(VehicleState vehicleState) {
		// Some convenience variables
		TemporalMatch previousMatch = vehicleState.getLastMatch();
		SpatialMatcher spatialMatcher = new SpatialMatcher();

		// Don't want to waste time search forward too far. So limit distance
		// such that vehicle would have traveled at 50% more than the max speed 
		// plus a couple hundred meters just to be safe.
		long timeBetweenFixesMsec = vehicleState.getLastAvlReport().getTime()
				- vehicleState.getPreviousToLastAvlReport().getTime();
		double distanceAlongPathToSearch = AvlConfig.getMaxAvlSpeed() * 1.5
				* timeBetweenFixesMsec / Time.MS_PER_SEC + 200.0;

		// Since already traveled some along segment should start
		// distanceSearched
		// with minus that distance so that it will be determined correctly.
		double distanceSearched = -previousMatch.getDistanceAlongSegment();

		// Start at the previous match and search along the block for best
		// spatial matches. Look ahead until distance spanned would mean
		// that vehicle would have had to travel too fast or that end of
		// block reached.
		Indices indices = new Indices(previousMatch);
		spatialMatcher.setStartOfSearch(previousMatch);
		while (!indices.pastEndOfBlock()
				&& distanceSearched < distanceAlongPathToSearch) {
			spatialMatcher.processPossiblePotentialMatch(
					vehicleState.getLastAvlReport(), indices);

			distanceSearched += indices.getSegment().length();

			// For next iteration through while loop
			indices.increment();
		}

		// Need to handle boundary condition. Done looking ahead but
		// the end match might be a potential one even if was continuing
		// to improve the match. Therefore if there was a potential
		// match then should store it.
		if (spatialMatcher.previousPotentialSpatialMatch != null) {
			// There was a potential match and now things are getting
			// worse so that was a local minimum. Therefore this is
			// one of the spatial matches to be returned.
			spatialMatcher.spatialMatches
					.add(spatialMatcher.previousPotentialSpatialMatch);
		}

		logger.debug(
				"For vehicleId={} the match with the best distance was {}",
				vehicleState.getVehicleId(),
				spatialMatcher.smallestDistanceSpatialMatch);

		// Return the list of local matches
		return spatialMatcher.spatialMatches;
	}

}
