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

package org.transitime.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.transitime.ipc.data.IpcActiveBlock;
import org.transitime.ipc.data.IpcTrip;
import org.transitime.ipc.data.IpcVehicle;
import org.transitime.utils.Time;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class ApiActiveBlock {

	@XmlAttribute
	private String id;

	@XmlAttribute
	private String serviceId;

	@XmlAttribute
	private String startTime; 

	@XmlAttribute
	private String endTime; 

	@XmlElement(name = "trip")
	private ApiTripSummary apiTripSummary;
	
	@XmlElement(name = "vehicle")
	private Collection<ApiVehicleDetails> vehicles;

	/********************** Member Functions **************************/

	protected ApiActiveBlock() {}
	
	public ApiActiveBlock(IpcActiveBlock ipcActiveBlock) {
		id = ipcActiveBlock.getBlock().getId();
		serviceId = ipcActiveBlock.getBlock().getServiceId();
		startTime = Time.timeOfDayStr(ipcActiveBlock.getBlock().getStartTime());
		endTime = Time.timeOfDayStr(ipcActiveBlock.getBlock().getEndTime());
		
		List<IpcTrip> trips = ipcActiveBlock.getBlock().getTrips();
		IpcTrip ipcTrip = trips.get(ipcActiveBlock.getActiveTripIndex());
		apiTripSummary = new ApiTripSummary(ipcTrip);
		
		vehicles = new ArrayList<ApiVehicleDetails>();
		for (IpcVehicle ipcVehicles : ipcActiveBlock.getVehicles()) {
			vehicles.add(new ApiVehicleDetails(ipcVehicles));
		}
	}
	
	public String getId() {
		return id;
	}
	
	public ApiTripSummary getApiTripSummary() {
		return apiTripSummary;
	}
}
