/* This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License (GPL) as published by the
 * Free Software Foundation, either version 3 of the License, or any later
 * version.
 * 
 * Transitime.org is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Transitime.org . If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitclock.ipc.data.IpcBlock;
import org.transitclock.ipc.data.IpcRouteSummary;
import org.transitclock.ipc.data.IpcTrip;
import org.transitclock.utils.Time;

/**
 * Describes a block in terse form, without schedule and trip pattern info
 * 
 * @author SkiBu Smith
 *
 */
@XmlRootElement(name = "block")
public class ApiBlockTerse {

	@XmlAttribute
	private int configRev;

	@XmlAttribute
	private String id;

	@XmlAttribute
	private String serviceId;

	@XmlAttribute
	private String startTime;

	@XmlAttribute
	private String endTime;

	@XmlElement
	private List<ApiTripTerse> trips;

	@XmlElement(name = "routes")
	private List<ApiRoute> routeSummaries;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiBlockTerse() {
	}

	public ApiBlockTerse(IpcBlock ipcBlock) {
		configRev = ipcBlock.getConfigRev();
		id = ipcBlock.getId();
		serviceId = ipcBlock.getServiceId();
		startTime = Time.timeOfDayStr(ipcBlock.getStartTime());
		endTime = Time.timeOfDayStr(ipcBlock.getEndTime());

		trips = new ArrayList<ApiTripTerse>();
		for (IpcTrip ipcTrip : ipcBlock.getTrips()) {
			trips.add(new ApiTripTerse(ipcTrip));
		}

		routeSummaries = new ArrayList<ApiRoute>();
		for (IpcRouteSummary ipcRouteSummary : ipcBlock.getRouteSummaries()) {
			routeSummaries.add(new ApiRoute(ipcRouteSummary));
		}
	}

}
