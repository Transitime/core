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

package org.transitclock.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.db.structs.Block;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.utils.Time;

/**
 * Contains information on Blocks as a whole, such as which blocks are currently
 * active.
 *
 * @author SkiBu Smith
 *
 */
public class BlocksInfo {
	
	private static IntegerConfigValue blockactiveForTimeBeforeSecs=new IntegerConfigValue("transitclock.core.blockactiveForTimeBeforeSecs", new Integer(0), "Now many seconds before the start of a block it will be considered active.");
	private static IntegerConfigValue blockactiveForTimeAfterSecs=new IntegerConfigValue("transitclock.core.blockactiveForTimeAfterSecs", new Integer(-1), "Now many seconds after the end of a block it will be considered active.");
	

	/********************** Member Functions **************************/

	/**
	 * Looks at all blocks that are for the current service ID and returns list
	 * of ones that will start within beforeStartTimeSecs.
	 * 
	 * @param beforeStartTimeSecs
	 *            Specifies in seconds how much before the block start time that
	 *            the block is considered to be active.
	 * @return blocks that are about to start. Can be empty list but will not be
	 *         null.
	 */
	public static List<Block> getBlocksAboutToStart(int beforeStartTimeSecs) {
		// The list to be returned
		List<Block> aboutToStartBlocks = new ArrayList<Block>(1000);
		
		Core core = Core.getInstance();
		if (core == null)
			return aboutToStartBlocks;
		
		// Determine which service IDs are currently active.
		// Yes, there can be multiple ones active at once.
		Date now = core.getSystemDate();
		Collection<String> currentServiceIds = 
				core.getServiceUtils().getServiceIds(now);
	
		// For each service ID ...
		for (String serviceId : currentServiceIds) {
			DbConfig dbConfig = core.getDbConfig();
			Collection<Block> blocks = dbConfig.getBlocks(serviceId);
			
			// If the block is about to be or currently active then
			// add it to the list to be returned
			for (Block block : blocks) {
				if (block.isBeforeStartTime(now, beforeStartTimeSecs))
					aboutToStartBlocks.add(block);
			}
		}
		
		// Done!
		return aboutToStartBlocks;
	}

	/**
	 * Returns list of blocks that are currently active. 
	 * 
	 * @return List of currently active blocks. Will not be null.
	 */
	public static List<Block> getCurrentlyActiveBlocks() {
		return getCurrentlyActiveBlocks(null, null,blockactiveForTimeBeforeSecs.getValue(), blockactiveForTimeAfterSecs.getValue());
	}
	
	/**
	 * Returns list of blocks that are currently active for the specified
	 * routes.
	 * 
	 * @param routeIds
	 *            Collection of routes IDs that want blocks for. Use null to
	 *            indicate all routes.
	 * @param blockIdsToIgnore
	 *            Won't do the expensive lookup of the blocks in this set. This
	 *            way can filter out blocks already assigned or such, and speed
	 *            up the determination of active blocks. Set to null if simply
	 *            want all currently active blocks.
	 * @param allowableBeforeTimeSecs
	 *            How much before the block time the block is considered to be
	 *            active
	 * @param allowableAfterStartTimeSecs
	 *            If set to value greater than or equal to zero then block
	 *            considered active only if within this number of seconds after
	 *            the start time. If less then zero then block considered active
	 *            up to the block end time.
     * @return List of currently active blocks. Will not be null.
	 */
	public static List<Block> getCurrentlyActiveBlocks(
			Collection<String> routeIds, Set<String> blockIdsToIgnore,
			int allowableBeforeTimeSecs, int allowableAfterStartTimeSecs) {
		// The list to be returned
		List<Block> activeBlocks = new ArrayList<Block>(1000);
		
		Core core = Core.getInstance();
		if (core == null)
			return activeBlocks;
		
		// Determine which service IDs are currently active
		Set<String> serviceIds = new HashSet<String>();
		long now = core.getSystemTime();
		List<String> currentServiceIds = 
				core.getServiceUtils().getServiceIdsForDay(now);
		serviceIds.addAll(currentServiceIds);
		
		// If current time is just a couple of hours after midnight then need
		// to also look at service IDs for previous day as well since a block
		// from the previous day might still be running after midnight.
		int secsInDayForAvlReport = 
				Core.getInstance().getTime().getSecondsIntoDay(now);
		if (secsInDayForAvlReport < 4 * Time.HOUR_IN_SECS) {
			List<String> previousDayServiceIds =
					core.getServiceUtils().getServiceIdsForDay(
							now - Time.DAY_IN_MSECS);
			serviceIds.addAll(previousDayServiceIds);
		}

		// If current time is just before midnight then need to also look at
		// service IDs from the next day since a block might start soon after
		// midnight.
		if (secsInDayForAvlReport > Time.DAY_IN_SECS - allowableBeforeTimeSecs) {
			List<String> nextDayServiceIds =
					core.getServiceUtils().getServiceIdsForDay(
							now + Time.DAY_IN_MSECS);
			serviceIds.addAll(nextDayServiceIds);
		}
		
		// For each service ID ...
		for (String serviceId : serviceIds) {
			DbConfig dbConfig = core.getDbConfig();
			Collection<Block> blocks = dbConfig.getBlocks(serviceId);
			
			// If the block is about to be or currently active then
			// add it to the list to be returned
			for (Block block : blocks) {
				// If this is a block to ignore then simply continue to the 
				// next one
				if (blockIdsToIgnore != null
						&& blockIdsToIgnore.contains(block.getId()))
					continue;
				
				// Determine if block is for specified route. If routeIds is
				// null then interested in all routes
				boolean forSpecifiedRoute = true;
				if (routeIds != null && !routeIds.isEmpty()) {
					forSpecifiedRoute = false;
					for (String routeId : routeIds) {
						if (block.getRouteIds().contains(routeId)) {
							forSpecifiedRoute = true;
							break;
						}
					}
				}
				
				// If block currently active and is for specified route then
				// add it to the list
				if (block.isActive(now, allowableBeforeTimeSecs,
						allowableAfterStartTimeSecs) && forSpecifiedRoute)
					activeBlocks.add(block);
			}
		}
		
		// Done!
		return activeBlocks;
	}
}
