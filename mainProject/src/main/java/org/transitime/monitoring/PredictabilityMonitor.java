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

package org.transitime.monitoring;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.DoubleConfigValue;
import org.transitime.config.IntegerConfigValue;
import org.transitime.core.BlocksInfo;
import org.transitime.core.dataCache.VehicleDataCache;
import org.transitime.db.structs.Block;
import org.transitime.ipc.data.IpcCompleteVehicle;
import org.transitime.utils.EmailSender;
import org.transitime.utils.StringUtils;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class PredictabilityMonitor extends MonitorBase {

	private static DoubleConfigValue minimumPredictableBlocks =
			new DoubleConfigValue(
					"transitime.monitoring.minimumPredictableBlocks", 
					0.7, 
					"The minimum fraction of currently active blocks that "
					+ "should have a predictable vehicle");
	
	private static IntegerConfigValue minimumPredictableVehicles =
			new IntegerConfigValue(
					"transitime.monitoring.minimumPredictableVehicles", 
					3, 
					"When looking at small number of vehicles it is too easy "
					+ "to get below minimumPredictableBlocks. So number of "
					+ "predictable vehicles is increased to this amount if "
					+ "below when determining the fraction.");
	
	private static final Logger logger = LoggerFactory
			.getLogger(PredictabilityMonitor.class);

	/********************** Member Functions **************************/

	/**
	 * Simple constructor
	 * 
	 * @param emailSender
	 * @param agencyId
	 */
	public PredictabilityMonitor(EmailSender emailSender, String agencyId) {
		super(emailSender, agencyId);
	}

	private double percentageBlocksPredictable() {
		// Determine number of currently active blocks.
		// If there are no currently active blocks then don't need to be
		// getting AVL data so return 0
		List<Block> activeBlocks = BlocksInfo.getCurrentlyActiveBlocks();
		if (activeBlocks.size() == 0) {
			logger.debug("No currently active blocks so predictability "
					+ "considered to be OK");
			return 1.0;
		}

		// Determine number of currently active vehicles
		Collection<IpcCompleteVehicle> vehicles = 
				VehicleDataCache.getInstance().getVehicles();
		
		int predictableVehicleCount = 0;
		for (IpcCompleteVehicle vehicle : vehicles) {
			if (vehicle.isPredictable())
				++predictableVehicleCount;
		}
		
		// Determine fraction of active blocks that have a predictable vehicle 
		double fraction = ((double) Math.max(predictableVehicleCount,
				minimumPredictableVehicles.getValue())) / activeBlocks.size();
		
		// Provide simple message explaining the situation
		String message = "Predictable blocks fraction=" 
				+ StringUtils.twoDigitFormat(fraction) 
				+ ", minimum allowed fraction=" 
				+ StringUtils.twoDigitFormat(minimumPredictableBlocks.getValue())
				+ ", active blocks=" + activeBlocks.size()
				+ ", predictable vehicles=" + predictableVehicleCount
				+ ", predictable vehicles using max=" 
				+ Math.max(predictableVehicleCount,
						minimumPredictableVehicles.getValue());
		setMessage(message);
		
		// Return fraction of blocks that have a predictable vehicle
		return fraction;
	}
	
	/* (non-Javadoc)
	 * @see org.transitime.monitoring.MonitorBase#triggered()
	 */
	@Override
	protected boolean triggered() {
		double percentage = percentageBlocksPredictable();
		return percentage < minimumPredictableBlocks.getValue();
	}

	/* (non-Javadoc)
	 * @see org.transitime.monitoring.MonitorBase#type()
	 */
	@Override
	protected String type() {
		return "Predictability";
	}

}
