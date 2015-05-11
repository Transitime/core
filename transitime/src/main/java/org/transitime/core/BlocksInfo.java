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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.transitime.applications.Core;
import org.transitime.db.structs.Block;
import org.transitime.gtfs.DbConfig;

/**
 * Contains information on Blocks as a whole, such as which blocks are currently
 * active.
 *
 * @author SkiBu Smith
 *
 */
public class BlocksInfo {

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
		List<String> currentServiceIds = 
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
		return getCurrentlyActiveBlocks(null, 0);
	}
	
	/**
	 * Returns list of blocks that are currently active for the specified
	 * routes.
	 * 
	 * @param routeIds
	 *            Collection of routes IDs that want blocks for. Use null
	 *            to indicate all routes.
	 * @param allowableBeforeTimeSecs
	 *            How much before the block time the block is considered to be
	 *            active
	 * @return List of currently active blocks. Will not be null.
	 */
	public static List<Block> getCurrentlyActiveBlocks(
			Collection<String> routeIds, int allowableBeforeTimeSecs) {
		// The list to be returned
		List<Block> activeBlocks = new ArrayList<Block>(1000);
		
		Core core = Core.getInstance();
		if (core == null)
			return activeBlocks;
		
		// Determine which service IDs are currently active.
		// Yes, there can be multiple ones active at once.
		Date now = core.getSystemDate();
		List<String> currentServiceIds = 
				core.getServiceUtils().getServiceIds(now);

		// For each service ID ...
		for (String serviceId : currentServiceIds) {
			DbConfig dbConfig = core.getDbConfig();
			Collection<Block> blocks = dbConfig.getBlocks(serviceId);
			
			// If the block is about to be or currently active then
			// add it to the list to be returned
			for (Block block : blocks) {
				// Determine if block is for specified route
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
				if (block.isActive(now) && forSpecifiedRoute)
					activeBlocks.add(block);
			}
		}
		
		// Done!
		return activeBlocks;
	}
}
