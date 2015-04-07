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

package org.transitime.ipc.data;

import java.io.Serializable;

import org.transitime.db.structs.VehicleConfig;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class IpcVehicleConfig implements Serializable {

	private final String id;
	private final Integer type;
	private final String description;
	private final Integer capacity;
	private final Integer crushCapacity;
	private final Boolean passengerVehicle;

	private static final long serialVersionUID = 4172266751162647909L;
	
	/********************** Member Functions **************************/

	public IpcVehicleConfig(VehicleConfig vc) {
		this.id = vc.getId();
		this.type = vc.getType();
		this.description = vc.getDescription();
		this.capacity = vc.getCapacity();
		this.crushCapacity = vc.getCrushCapacity();
		this.passengerVehicle = vc.getPassengerVehicle();
	}

	public String getId() {
		return id;
	}

	public Integer getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public Integer getCrushCapacity() {
		return crushCapacity;
	}

	public Boolean getPassengerVehicle() {
		return passengerVehicle;
	}

	@Override
	public String toString() {
		return "IpcVehicleConfig [" 
				+ "id=" + id 
				+ ", type=" + type
				+ ", description=" + description 
				+ ", capacity=" + capacity
				+ ", crushCapacity=" + crushCapacity 
				+ ", passengerVehicle="	+ passengerVehicle 
				+ "]";
	}
}
