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

import org.transitime.db.structs.ArrivalDeparture;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public interface ArrivalDepartureGenerator {
	/**
	 * Generates List of ArrivalDeparture objects such that they can be stored.
	 * Also should call vehicleState.setVehicleAtStopInfo(newVehicleAtStopInfo)
	 * to specify if vehicle at a stop. This is done in
	 * ArrivalDepartureGenerator.generate() since that is where this info is
	 * easily determined.
	 * 
	 * @param vehicleState
	 * @return List of ArrivalDeparture objects generated
	 */
	public List<ArrivalDeparture> generate(VehicleState vehicleState);

}
