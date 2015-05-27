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
import java.util.ArrayList;
import java.util.List;

import org.transitime.applications.Core;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.Route;
import org.transitime.db.structs.Trip;
import org.transitime.utils.Time;

/**
 * Configuration information for a Block for IPC.
 *
 * @author SkiBu Smith
 *
 */
public class IpcBlock implements Serializable {

	private final int configRev;
	private final String id;
	private final String serviceId;
	private final int startTime; // In seconds from midnight
	private final int endTime;   // In seconds from midnight

	private final List<IpcTrip> trips;
	private final List<IpcRouteSummary> routeSummaries;

	private static final long serialVersionUID = 5707936828040534137L;

	/********************** Member Functions **************************/

	public IpcBlock(Block dbBlock) {
		configRev = dbBlock.getConfigRev();
		id = dbBlock.getId();
		serviceId = dbBlock.getServiceId();
		startTime = dbBlock.getStartTime();
		endTime = dbBlock.getEndTime();
		
		trips = new ArrayList<IpcTrip>();
		for (Trip dbTrip : dbBlock.getTrips()) {
			trips.add(new IpcTrip(dbTrip));
		}
		
		routeSummaries = new ArrayList<IpcRouteSummary>();
		for (String routeId : dbBlock.getRouteIds()) {
			Route dbRoute = 
					Core.getInstance().getDbConfig().getRouteById(routeId);
			routeSummaries.add(new IpcRouteSummary(dbRoute));
		}
	}

	@Override
	public String toString() {
		return "IpcBlock [" 
				+ "configRev=" + configRev 
				+ ", id=" + id
				+ ", serviceId=" + serviceId 
				+ ", startTime=" + Time.timeOfDayStr(startTime)
				+ ", endTime=" + Time.timeOfDayStr(endTime) 
				+ ", trips=" + trips 
				+ ", routeSummaries=" + routeSummaries
				+ "]";
	}

	public int getConfigRev() {
		return configRev;
	}

	public String getId() {
		return id;
	}

	public String getServiceId() {
		return serviceId;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public List<IpcTrip> getTrips() {
		return trips;
	}

	public List<IpcRouteSummary> getRouteSummaries() {
		return routeSummaries;
	}
}
