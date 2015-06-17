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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.BooleanConfigValue;
import org.transitime.config.IntegerConfigValue;
import org.transitime.core.AvlProcessor;
import org.transitime.core.BlocksInfo;
import org.transitime.core.dataCache.VehicleDataCache;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.Location;
import org.transitime.db.structs.AvlReport.AssignmentType;
import org.transitime.ipc.data.IpcVehicle;
import org.transitime.ipc.data.IpcVehicleComplete;
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
 * the block or the schedule based vehicle is timed out via TimeoutHandlerModule
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

	private static final BooleanConfigValue processImmediatelyAtStartup =
			new BooleanConfigValue("transitime.schedBasedPreds.processImmediatelyAtStartup", 
					false,
					"Whether should start creating schedule based predictions "
					+ "right at startup. Usually want to give AVL data a "
					+ "polling cycle to generate AVL based predictions so the "
					+ "default is false. But for a purely schedule based "
					+ "system want to set this to true so get the predictions "
					+ "immediately.");
	
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
		// Determine all the block IDs already in use so that can skip these
		// when doing the somewhat expensive searching for currently active
		// blocks.
		Set<String> blockIdsAlreadyAssigned = new HashSet<String>();
		Collection<IpcVehicleComplete> vehicles =
				VehicleDataCache.getInstance()
						.getVehiclesUncludingSchedBasedOnes();
		for (IpcVehicle vehicle : vehicles) {
			String blockId = vehicle.getBlockId();
			if (blockId != null)
				blockIdsAlreadyAssigned.add(blockId);
		}
		
		// Determine which blocks are coming up or currently active
		List<Block> activeBlocks =
				BlocksInfo.getCurrentlyActiveBlocks(null,
						blockIdsAlreadyAssigned, getBeforeStartTimeMins()
								* Time.SEC_PER_MIN);
		
		// For each block about to start see if no associated vehicle
		for (Block block : activeBlocks) {
			// Is there a vehicle associated with the block?
			Collection<String> vehiclesForBlock = VehicleDataCache.getInstance()
					.getVehiclesByBlockId(block.getId());
			if (vehiclesForBlock == null || vehiclesForBlock.isEmpty()) {
				// No vehicle associated with the active block so create a
				// schedule based one. First create a fake AVL report that
				// corresponds to the first stop of the block.
				String vehicleId = 
						"block_" + block.getId() + "_schedBasedVehicle"; 
				long referenceTime = Core.getInstance().getSystemTime();
				long blockStartEpochTime =
						Core.getInstance()
								.getTime()
								.getEpochTime(block.getStartTime(),
										referenceTime);
				Location location = block.getStartLoc();
				AvlReport avlReport =
						new AvlReport(vehicleId, blockStartEpochTime, location);
				
				// Set the block assignment for the AVL report and indicate 
				// that it is for creating scheduled based predictions
				avlReport.setAssignment(block.getId(), 
						AssignmentType.BLOCK_FOR_SCHED_BASED_PREDS);

				logger.info("Creating a schedule based vehicle for blockId={}. "
						+ "The fake AVL report is {}. The block is {}",
						block.getId(), avlReport, block.toShortString());

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
		// vehicles to blocks yet. So sleep a bit first, unless specified to
		// run immediately by the processImmediatelyAtStartup configuration 
		// parameter.
		if (!processImmediatelyAtStartup.getValue())
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
