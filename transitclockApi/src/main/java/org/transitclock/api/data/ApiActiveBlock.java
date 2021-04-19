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

package org.transitclock.api.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.transitclock.db.webstructs.WebAgency;
import org.transitclock.ipc.data.IpcActiveBlock;
import org.transitclock.ipc.data.IpcTrip;
import org.transitclock.ipc.data.IpcVehicle;
import org.transitclock.utils.Time;

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
	private ApiTripTerse apiTripSummary;
	
	@XmlElement(name = "vehicle")
	private Collection<ApiVehicleDetails> vehicles;

	/********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really 
     * obtuse "MessageBodyWriter not found for media type=application/json"
     * exception.
     */
	protected ApiActiveBlock() {}
	
	public ApiActiveBlock(IpcActiveBlock ipcActiveBlock, String agencyId, SpeedFormat speedFormat) throws IllegalAccessException, InvocationTargetException {
		id = ipcActiveBlock.getBlock().getId();
		serviceId = ipcActiveBlock.getBlock().getServiceId();
		startTime = Time.timeOfDayStr(ipcActiveBlock.getBlock().getStartTime());
		endTime = Time.timeOfDayStr(ipcActiveBlock.getBlock().getEndTime());
		
		List<IpcTrip> trips = ipcActiveBlock.getBlock().getTrips();
		IpcTrip ipcTrip = trips.get(ipcActiveBlock.getActiveTripIndex());
		apiTripSummary = new ApiTripTerse(ipcTrip);
		
		// Get Time object based on timezone for agency
		WebAgency webAgency = WebAgency.getCachedWebAgency(agencyId);
		Time timeForAgency = webAgency.getAgency().getTime();				

		vehicles = new ArrayList<ApiVehicleDetails>();
		for (IpcVehicle ipcVehicles : ipcActiveBlock.getVehicles()) {
			vehicles.add(new ApiVehicleDetails(ipcVehicles, timeForAgency, speedFormat));
		}
	}
	
	public String getId() {
		return id;
	}
	
	public ApiTripTerse getApiTripSummary() {
		return apiTripSummary;
	}
}
