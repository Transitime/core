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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.AvlReport.AssignmentType;
import org.transitime.db.structs.Block;
import org.transitime.gtfs.DbConfig;

/**
 * Singleton class that handles block assignments from AVL feed. 
 * 
 * @author SkiBu Smith
 * 
 */
public class BlockAssigner {

	// Singleton class
	private static BlockAssigner singleton = new BlockAssigner();
	
	private static final Logger logger = 
			LoggerFactory.getLogger(BlockAssigner.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor private since singleton class
	 */
	private BlockAssigner() {}
	
	/**
	 * Returns the BlockAssigner singleton
	 * 
	 * @return
	 */
	public static BlockAssigner getInstance() {
		return singleton;
	}
	
	/**
	 * Gets the appropriate block associated with the AvlReport by getting the
	 * proper serviceId using the AVL timestamp and then determining the
	 * appropriate block using the serviceId and the blockId from the AVL
	 * report. If the blockId not specified in AVL data or the block could not
	 * be found for the serviceId then null will be returned
	 * 
	 * @param avlReport
	 * @return Block corresponding to the time and blockId from AVL report.
	 */
	public Block getBlock(AvlReport avlReport) {
		if (avlReport != null &&
				avlReport.getAssignmentId() != null && 
				avlReport.getAssignmentType()==AssignmentType.BLOCK_ID) {
			DbConfig config = Core.getInstance().getDbConfig();
			Service service = Core.getInstance().getService();
			List<String> serviceIds = service.getServiceIds(avlReport.getDate());
			boolean blockFoundForServiceId = false;
			for (String serviceId : serviceIds) {
				Block block = config.getBlock(serviceId, avlReport.getAssignmentId());
				if (block != null) {
					blockFoundForServiceId = true;
					logger.info("For vehicleId={} the block from the AVL feed is blockId={}", 
							avlReport.getVehicleId(), 
							block.getId());
					return block;
				}
			}
			if (!blockFoundForServiceId) {
				logger.error("For vehicleId={} AVL report specifies blockId={} " + 
						"but block is not valid for serviceIds={}",
						avlReport.getVehicleId(), 
						avlReport.getAssignmentId(), 
						serviceIds);
			}
		}

		// No valid block so return null
		return null;
	}
	
}
