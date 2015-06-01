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

package org.transitime.core.schedBasedPreds;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.IntegerConfigValue;
import org.transitime.core.AvlProcessor;
import org.transitime.core.BlocksInfo;
import org.transitime.core.dataCache.VehicleDataCache;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.Location;
import org.transitime.db.structs.AvlReport.AssignmentType;
import org.transitime.logging.Markers;
import org.transitime.modules.Module;
import org.transitime.utils.IntervalTimer;
import org.transitime.utils.Time;

/**
 * The schedule based predictions module runs in the background. Every few
 * minutes it looks for blocks that do not have an associated vehicle. For these
 * blocks the module creates a schedule based vehicle at the location of the
 * beginning of the block and generates predictions for the entire block that
 * are based on the scheduled departure time. The purpose of this module is to
 * generate predictions well in advance even if vehicles are assigned just a few
 * minutes before a vehicle is scheduled to start a block. This feature should
 * of course only be used if most of the time the blocks are actually run. It
 * should not be used for agencies such as SFMTA where blocks/trips are often
 * missed because would then be often providing predictions when no vehicle will
 * arrive.
 * <p>
 * Schedule based predictions are removed once a regular vehicle is assigned to
 * the block or the schedule based vehicle is timed out iva TimeoutHandlerModule
 * due to it being transitime.timeout.allowableNoAvlForSchedBasedPredictions
 * after the scheduled departure time for the assignment.
 * 
 * @author SkiBu Smith
 *
 */
public class SchedBasedPredsModule extends Module {

	private static final Logger logger = LoggerFactory
			.getLogger(SchedBasedPredsModule.class);

	/********************** Config Params **************************/

	private static final IntegerConfigValue timeBetweenPollingMsec = 
			new IntegerConfigValue(
					"transitime.schedBasedPreds.pollingRateMsec",
					4 * Time.MS_PER_MIN,
					"How frequently to look for blocks that do not have "
							+ "associated vehicle.");
	
	private static int getTimeBetweenPollingMsec() {
		return timeBetweenPollingMsec.getValue();
	}

	private static final IntegerConfigValue beforeStartTimeMins = 
			new IntegerConfigValue(
					"transitime.schedBasedPreds.beforeStartTimeMins", 60,
					"How much before a block start time should create a "
							+ "schedule based vehicle for that block.");
	
	private static int getBeforeStartTimeMins() {
		return beforeStartTimeMins.getValue();
	}

	/********************** Member Functions **************************/

	/**
	 * The constructor for the module. Called automatically if the module
	 * is configured.
	 * 
	 * @param agencyId
	 */
	public SchedBasedPredsModule(String agencyId) {
		super(agencyId);
	}
	
	/**
	 * Goes through all the blocks to find which ones don't have vehicles.
	 * For those blocks create a schedule based vehicle with associated
	 * predictions.
	 */
	private void createSchedBasedPredsAsNecessary() {
		// Determine which blocks are coming up or currently active
		List<Block> blocksAboutToStart = BlocksInfo.getBlocksAboutToStart(
				getBeforeStartTimeMins() * Time.SEC_PER_MIN);
		
		// For each block about to start see if no associated vehicle
		for (Block block : blocksAboutToStart) {
			// Is there a vehicle associated with the block?
			Collection<String> vehiclesForBlock = VehicleDataCache.getInstance()
					.getVehiclesByBlockId(block.getId());
			if (vehiclesForBlock == null || vehiclesForBlock.isEmpty()) {
				// No vehicle associated with the active block so create a
				// schedule based one. First create a fake AVL report that
				// corresponds to the first stop of the block.
				logger.info("Creating a schedule based vehicle for blockId={}",
						block.getId());
				String vehicleId = 
						"block_" + block.getId() + "_schedBasedVehicle"; 
				long time = Core.getInstance().getSystemTime();
				Location location = block.getStartLoc();
				AvlReport avlReport = new AvlReport(vehicleId, time, location);
				
				// Set the block assignment for the AVL report and indicate 
				// that it is for creating scheduled based predictions
				avlReport.setAssignment(block.getId(), 
						AssignmentType.BLOCK_FOR_SCHED_BASED_PREDS);
				
				// Process that AVL report to generate predictions and such
				AvlProcessor.getInstance().processAvlReport(avlReport);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// Log that module successfully started
		logger.info("Starting module {} for agencyId={}", 
				getClass().getName(), getAgencyId());
		
		// No need to run at startup since haven't yet had change to assign 
		// vehicles to blocks yet. So sleep a bit first.
		Time.sleep(getTimeBetweenPollingMsec());
		
		// Run forever
		while (true) {
			try {
				// For determining when to poll next
				IntervalTimer timer = new IntervalTimer();
								
				// Do the actual work
				createSchedBasedPredsAsNecessary();
				
				// Wait appropriate amount of time till poll again
				long sleepTime = 
						getTimeBetweenPollingMsec() - timer.elapsedMsec();
				if (sleepTime > 0)
					Time.sleep(sleepTime);
			} catch (Throwable e) {
				logger.error(Markers.email(),
						"Error with SchedBasedPredsModule", e);
			}
		}
	}

}
