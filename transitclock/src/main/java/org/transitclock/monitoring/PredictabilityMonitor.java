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

package org.transitclock.monitoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.transitclock.config.DoubleConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.BlocksInfo;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.db.structs.Block;
import org.transitclock.utils.EmailSender;
import org.transitclock.utils.StringUtils;

/**
 * Monitors how many vehicles are predictable compared to how many active blocks
 * there currently are.
 *
 * @author SkiBu Smith
 *
 */
public class PredictabilityMonitor extends MonitorBase {

    private MonitoringService monitoringService;

	private static DoubleConfigValue minPredictableBlocks =
			new DoubleConfigValue(
					"transitclock.monitoring.minPredictableBlocks", 
					0.50, 
					"The minimum fraction of currently active blocks that "
					+ "should have a predictable vehicle");

	private static DoubleConfigValue minPredictableBlocksGap =
			new DoubleConfigValue(
					"transitclock.monitoring.minPredictableBlocksGap", 
					0.25, 
					"When transitioning from triggered to untriggered don't "
					+ "want to send out an e-mail right away if actually "
					+ "dithering. Therefore will only send out OK e-mail if the "
					+ "value is now above minPredictableBlocks + "
					+ "minPredictableBlocksGap ");

	private static IntegerConfigValue minimumPredictableVehicles =
			new IntegerConfigValue(
					"transitclock.monitoring.minimumPredictableVehicles", 
					3, 
					"When looking at small number of vehicles it is too easy "
					+ "to get below minimumPredictableBlocks. So number of "
					+ "predictable vehicles is increased to this amount if "
					+ "below when determining the fraction.");
	
	/********************** Member Functions **************************/

	/**
	 * Simple constructor
	 * 
	 * @param emailSender
	 * @param agencyId
	 */
	public PredictabilityMonitor(MonitoringService monitoringService, EmailSender emailSender, String agencyId) {
		super(emailSender, agencyId);
        this.monitoringService = monitoringService;
	}

	/**
	 * Returns the fraction (0.0 - 1.0) of the blocks that currently have a
	 * predictable vehicle associated.
	 * 
	 * @param threshold
	 *            So can provide a more complete message if below the threshold
	 * @return Fraction of blocks that have a predictable vehicle
	 */
	private double fractionBlocksPredictable(double threshold) {
		// For creating message
		List<Block> activeBlocksWithoutVehicle = new ArrayList<Block>();
		// Determine number of currently active blocks.
		// If there are no currently active blocks then don't need to be
		// getting AVL data so return 0
		List<Block> activeBlocks = BlocksInfo.getCurrentlyActiveBlocks();
		if (activeBlocks.size() == 0) {
			setMessage("No currently active blocks so predictability "
					+ "considered to be OK.");
            monitoringService.averageMetric("PredictionPredictablePercentageOfBlocks", 1d);
			return 1.0;
		}

		// Determine number of currently active vehicles
		int predictableVehicleCount = 0;
		for (Block block : activeBlocks) {
			// Determine vehicles associated with the block if there are any
			Collection<String> vehicleIdsForBlock = VehicleDataCache
					.getInstance().getVehiclesByBlockId(block.getId());
			predictableVehicleCount += vehicleIdsForBlock.size();
			
			// Keep track of active blocks without vehicles
			if (vehicleIdsForBlock.size() == 0)
				activeBlocksWithoutVehicle.add(block);
		}
		
		// Determine fraction of active blocks that have a predictable vehicle 
		double fraction = ((double) Math.max(predictableVehicleCount,
		minimumPredictableVehicles.getValue())) / activeBlocks.size();
		monitoringService.averageMetric("PredictionActiveBlockCount", (double)Math.max(predictableVehicleCount,
		    minimumPredictableVehicles.getValue()));
		monitoringService.averageMetric("PredictionTotalBlockCount", (double)activeBlocks.size());
    monitoringService.averageMetric("PredictablePercentageOfBlocks", fraction);

		// Provide simple message explaining the situation
		String message = "Predictable blocks fraction=" 
				+ StringUtils.twoDigitFormat(fraction) 
				+ ", minimum allowed fraction=" 
				+ StringUtils.twoDigitFormat(minPredictableBlocks.getValue())
				+ ", active blocks=" + activeBlocks.size()
				+ ", predictable vehicles=" + predictableVehicleCount
				+ " (minimumPredictableVehicles=" 
				+ Math.max(predictableVehicleCount,
						minimumPredictableVehicles.getValue())
				+ ").";
		
		// If below the threshold then add all the active block IDs to the
		// message so can more easily see 
		if (fraction < threshold) {
			StringBuilder sb = new StringBuilder();
			sb.append(" Currently active blocks without vehicles: ");
			for (Block block : activeBlocksWithoutVehicle) {
	      sb.append("block=")
        .append(block.getId())
        .append(", serviceId=")
        .append(block.getServiceId())
        .append("; ");
			}
			message += sb.toString();
		}
		

		setMessage(message, fraction);
		
		// Return fraction of blocks that have a predictable vehicle
		return fraction;
	}
	
	/* (non-Javadoc)
	 * @see org.transitclock.monitoring.MonitorBase#triggered()
	 */
	@Override
	protected boolean triggered() {
		// Determine the threshold for triggering. If already triggered
		// then raise the threshold by minPredictableBlocksGap in order
		// to prevent lots of e-mail being sent out if the value is
		// dithering around minPredictableBlocks.
		double threshold = minPredictableBlocks.getValue();
		if (wasTriggered())
			threshold += minPredictableBlocksGap.getValue();
		
		double fraction = fractionBlocksPredictable(threshold);
		
		return fraction < threshold;
	}

	/* (non-Javadoc)
	 * @see org.transitclock.monitoring.MonitorBase#type()
	 */
	@Override
	protected String type() {
		return "Predictability";
	}

}
