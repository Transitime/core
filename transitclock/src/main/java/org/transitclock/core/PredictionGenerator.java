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

import org.transitclock.ipc.data.IpcPrediction;

import java.util.List;

/**
 * Defines the interface for generating predictions. To create predictions using
 * an alternate method simply implement this interface and configure
 * PredictionGeneratorFactory to instantiate the new class when a
 * PredictionGenerator is needed.
 *
 * @author SkiBu Smith
 *
 */
public interface PredictionGenerator {
	
	/**
	 * Generates and returns the predictions for the vehicle. 
	 * 
	 * @param vehicleState
	 *            Contains the new match for the vehicle that the predictions
	 *            are to be based on.
	 */
	public List<IpcPrediction> generate(VehicleState vehicleState);
	
}
