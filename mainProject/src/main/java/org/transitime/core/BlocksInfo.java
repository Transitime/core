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
	 * of ones that will be or currently are active.
	 * 
	 * @param beforeStartTimeSecs
	 *            Specifies in seconds how much before the block start time that
	 *            the block is considered to be active.
	 * @return blocks that will be or are currently active. Can be empty list
	 *         but will not be null.
	 */
	public static List<Block> getCurrentlyActiveBlocks(int beforeStartTimeSecs) {
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
				if (block.isBeforeStartTime(now, beforeStartTimeSecs))
					activeBlocks.add(block);
			}
		}
		
		// Done!
		return activeBlocks;
	}

}
