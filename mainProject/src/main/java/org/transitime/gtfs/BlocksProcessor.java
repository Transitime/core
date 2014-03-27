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
package org.transitime.gtfs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.Frequency;
import org.transitime.db.structs.Trip;
import org.transitime.db.structs.TripPattern;
import org.transitime.gtfs.gtfsStructs.GtfsRoute;

/**
 * Goes through all the Trip objects and creates corresponding Block
 * assignments that use them.
 * 
 * @author SkiBu Smith
 */
public class BlocksProcessor {

	private final GtfsData gtfsData;
	
	private static final Logger logger = LoggerFactory
			.getLogger(BlocksProcessor.class);

	/********************** Member Functions **************************/

	public BlocksProcessor(GtfsData gtfsData) {
		this.gtfsData = gtfsData;

		// Make sure needed data is already read in. This method uses
		// trips and trip patterns from the stop_time.txt file. Therefore
		// making sure the stop times read in.
		if (gtfsData.getTripPatternMap() == null || gtfsData.getTripPatternMap().isEmpty()) {
			logger.error("processStopTimesData() must be called before " + 
					"BlocksProcessor() is. Exiting.");
			System.exit(-1);
		}
	}
	
	/**
	 * For some routes want "unscheduled" assignments that use the longest trip
	 * pattern for each direction but have no schedule time associated. Useful
	 * for special situations such as baseball game events where transit agency
	 * needs to add additional unscheduled service.
	 * 
	 * TODO need to fix this code so that special trip patterns are used that
	 * have an unscheduled stop at beginning so that system knows not to make 
	 * predictions until vehicle has left the terminal.
	 * 
	 * @param blocks List of blocks that the unscheduled assignments should be added
	 * @param serviceIdsUsed The service IDs that the unscheduled blocks should 
	 * be created for.
	 */
	private void addUnscheduledBlocks(List<Block> blocks, Set<String> serviceIdsUsed) {
		// Create unscheduled blocks for the routes that have 
		// create_unschedule_block set in GTFS route.txt file
		Collection<GtfsRoute> gtfsRoutes = gtfsData.getGtfsRoutesMap().values();
		for (GtfsRoute gtfsRoute : gtfsRoutes) {
			if (gtfsRoute.shouldCreateUnscheduledBlock()) {
				// Should create unscheduled block for route so do so.
				// Find the longest trip patterns for the two directions
				// for creating the block. Figure out the parameters
				// for the unscheduled block.
				String blockId = (gtfsRoute.getRouteShortName() != null ? 
						gtfsRoute.getRouteShortName() : gtfsRoute.getRouteId()) + 
						gtfsRoute.getUnscheduledBlockSuffix();
				int startTimeForBlock = 0;      // start at midnight
				int endTimeForBlock = 24*60*60; // end at midnight
				int headwaySecs = 0;
				
				// find the two trips to make up the block. Use longest trip for
				// each direction.
				List<TripPattern> tripPatternsForRoute = 
						gtfsData.getTripPatterns(gtfsData.getProperIdOfRoute(gtfsRoute.getRouteId()));
				TripPattern longestTripPatternForDirection0 = null;
				int maxStopsForDirection0 = 0;
				TripPattern longestTripPatternForDirection1 = null;
				int maxStopsForDirection1 = 0;
				for (TripPattern tripPattern : tripPatternsForRoute) {
					// Find longest tripPattern for each direction. 
					// According to the GTFS spec the direction_id in the
					// trips.txt file is either a "0" or a "1". So just look
					// for those values.
					if ("0".equals(tripPattern.getDirectionId())) {
						// It is direction "0"
						if (tripPattern.getStopPaths().size() > maxStopsForDirection0) {
							maxStopsForDirection0 = tripPattern.getStopPaths().size();
							longestTripPatternForDirection0 = tripPattern;
						}
					} else if ("1".equals(tripPattern.getDirectionId())) {
						// It is direction "1"
						if (tripPattern.getStopPaths().size() > maxStopsForDirection1) {
							maxStopsForDirection1 = tripPattern.getStopPaths().size();
							longestTripPatternForDirection1 = tripPattern;
						}
					}
				}				
				// Add a trip from the longest of each direction. Of course can only do
				// this if found a trip for both direction "0" and "1".
				if (longestTripPatternForDirection0 != null && longestTripPatternForDirection1 != null) {
					List<Trip> tripsListForBlock = new ArrayList<Trip>();
					// Note: it is OK to use getTrips() because only deprecated because 
					// can only be used when processing in GTFS data, which is what we are
					// doing here.
					tripsListForBlock.add(longestTripPatternForDirection0.getTrips().get(0));
					tripsListForBlock.add(longestTripPatternForDirection1.getTrips().get(0));
					
					// Create the Blocks. Create one for each service ID. The idea is
					// that this is all about special unscheduled service so even though
					// a route is defined for weekdays want the unscheduled blocks to be
					// available for any day.
					for (String serviceId : serviceIdsUsed) {
						Block block = new Block(blockId,
									serviceId,
									startTimeForBlock, endTimeForBlock, 
									tripsListForBlock, headwaySecs);
						
						// Add the new block to the list of blocks
						blocks.add(block);					
					}
				}
			}
		}
	}
	
	/**
	 * Actually processes the trips into block assignments. Includes "unscheduled"
	 * block assignments for the routes that have been configured for such.
	 * 
	 * @return List of Block assignments.
	 */
	public List<Block> process() {
		// Create list for blocks
		List<Block> blocks = new ArrayList<Block>();
		
		// Go through trips map, which was created using data from stop_times.txt
		// GTFS file. Then can go through each Trip and construct the
		// blocks. The trips are not necessarily grouped or ordered by block. 
		// Therefore need to read them all in to a map keyed by serviceId that 
		// contains maps keyed by blockId and has list of Trip objects. Then 
		// can put them in correct order based on their times.
		Map<String, HashMap<String, List<Trip>>> tripListByBlocksByServiceMap = 
				new HashMap<String, HashMap<String, List<Trip>>>();
		for (Trip trip : gtfsData.getTrips()) {
			String serviceId = trip.getServiceId();
			String blockId = trip.getBlockId();
			
			// Get the map for the specified service ID
			HashMap<String, List<Trip>> tripListForBlocksMap = 
					tripListByBlocksByServiceMap.get(serviceId);
			if (tripListForBlocksMap == null) {
				tripListForBlocksMap = new HashMap<String, List<Trip>>();
				tripListByBlocksByServiceMap.put(serviceId, tripListForBlocksMap);
			}
			
			// Determine trip list for the block
			List<Trip> tripListForBlock = tripListForBlocksMap.get(blockId);
			if (tripListForBlock == null) {
				tripListForBlock = new ArrayList<Trip>();
				tripListForBlocksMap.put(blockId, tripListForBlock);
			}
			
			// Add this trip to the trip list for the block
			tripListForBlock.add(trip);
		}
		
		// Now have access to trip list for each block. For each service ID
		// and block ID create the Block object.
		for (String serviceId : tripListByBlocksByServiceMap.keySet()) {
			HashMap<String, List<Trip>> tripListForBlocksMap = 
					tripListByBlocksByServiceMap.get(serviceId);
			
			// For each block ID for the service ID...
			for (String blockId : tripListForBlocksMap.keySet()) {
				// Determine list of trips for the current block
				List<Trip> tripsListForBlock = tripListForBlocksMap.get(blockId);
				
				
				// The following parameters need to be determined to create the Block.
				// The differ depending on whether the block is supposed to be
				// schedule based or not. It is schedule based if the trips are not
				// listed in the frequencies.txt file.
				int startTimeForBlock;
				int endTimeForBlock;
				int headwaySecs;
				
				// If schedule based block then handle normally. A block is schedule
				// based if its trips are listed in the frequencies.txt file. 
				// Using the frequencies file is a bit vague though. It appears to be
				// for individual trips but actually need a block to consist of multiple
				// trips so that can predict through to the next trip. Only thing can
				// do for now is to get the tripId for the first trip for the block
				// (as defined in the stop_times.txt file) and see if it is in the
				// frequency.txt file.
				String firstTripId = tripsListForBlock.get(0).getId();
				List<Frequency> frequencyList = gtfsData.getFrequencyList(firstTripId);
				if (frequencyList == null || frequencyList.get(0).getExactTimes()) {
					// It is schedule based block so mark headway as -1
					headwaySecs = -1;
					
					// Sort the List of Trips chronologically since they might be
					// listed in the stop_times.txt file in any order.
					Collections.sort(tripsListForBlock, 
							new Comparator<Trip>() {
								@Override
								public int compare(Trip arg0, Trip arg1) {
									return arg0.getStartTime().compareTo(arg1.getStartTime());
								}
							} );
								
					// Determine start time for block from the first trip.
					Trip firstTripForBlock = tripsListForBlock.get(0);
					startTimeForBlock = firstTripForBlock.getStartTime();
					
					// Determine end time for block from the last trip.
					Trip lastTripForBlock = tripsListForBlock.get(tripsListForBlock.size()-1);
					endTimeForBlock = lastTripForBlock.getEndTime();
				} else {
					Frequency frequency = frequencyList.get(0);
					
					// It is a frequency based block so use proper headway from file
					headwaySecs = frequency.getHeadwaySecs();
					
					// Start time for unscheduled block is from the frequencies.txt file
					startTimeForBlock = frequency.getStartTime();
	
					// End time for unscheduled block is from the frequencies.txt file
					endTimeForBlock = frequency.getEndTime();
				}
				
				// Create the Block
				Block block = new Block(blockId,
							serviceId,
							startTimeForBlock, endTimeForBlock, 
							tripsListForBlock, headwaySecs);
				
				// Add the new block to the list of blocks
				blocks.add(block);
			}
		}
		
		// Determine the service IDs that are used and then add
		// the unscheduled blocks for routes that have been configured
		// such that they should be generated.
		Set<String> serviceIdsUsed = tripListByBlocksByServiceMap.keySet();
		addUnscheduledBlocks(blocks, serviceIdsUsed);

		// Return the results
		return blocks;
	}
}
