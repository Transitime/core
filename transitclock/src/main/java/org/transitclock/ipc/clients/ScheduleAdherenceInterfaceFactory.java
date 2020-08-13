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
package org.transitclock.ipc.clients;

import org.transitclock.ipc.interfaces.ReportingInterface;
import org.transitclock.ipc.rmi.ClientFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a ScheduleAdherenceInterface client
 * 
 * @author carabalb
 * 
 */
public class ScheduleAdherenceInterfaceFactory {

	// Keyed by agencyId
	private static Map<String, ReportingInterface> scheduleAdherenceInterfaceMap =
			new HashMap<String, ReportingInterface>();

	/********************** Member Functions **************************/

	/**
	 * Gets the singleton instance.
	 * 
	 * @param agencyId
	 * @return
	 */
	public static ReportingInterface get(String agencyId) {
		ReportingInterface reportingInterface =
				scheduleAdherenceInterfaceMap.get(agencyId);
		if (reportingInterface == null) {

			reportingInterface = ClientFactory.getInstance(agencyId, ReportingInterface.class);

			scheduleAdherenceInterfaceMap.put(agencyId, reportingInterface);
			
		}

		return reportingInterface;
	}

}
