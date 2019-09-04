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

import java.util.HashMap;
import java.util.Map;

import org.transitclock.ipc.interfaces.CacheQueryInterface;
import org.transitclock.ipc.interfaces.CommandsInterface;
import org.transitclock.ipc.interfaces.HoldingTimeInterface;
import org.transitclock.ipc.rmi.ClientFactory;

/**
 * Provides a HoldingTimeInterface client that can be sent holding time queries.
 * 
 * @author Sean Og Crudden
 * 
 */
public class HoldingTimeInterfaceFactory {

	// Keyed by agencyId
	private static Map<String, HoldingTimeInterface> holdingtimeInterfaceMap =
			new HashMap<String, HoldingTimeInterface>();

	/********************** Member Functions **************************/

	/**
	 * Gets the singleton instance.
	 * 
	 * @param agencyId
	 * @return
	 */
	public static HoldingTimeInterface get(String agencyId) {
		HoldingTimeInterface holdingTimeInterface =
				holdingtimeInterfaceMap.get(agencyId);
		if (holdingTimeInterface == null) {
			
			holdingTimeInterface = ClientFactory.getInstance(agencyId, HoldingTimeInterface.class);
			
			holdingtimeInterfaceMap.put(agencyId, holdingTimeInterface);
			
		}

		return holdingTimeInterface;
	}

}
