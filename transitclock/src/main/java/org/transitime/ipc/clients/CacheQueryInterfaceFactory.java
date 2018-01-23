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
package org.transitime.ipc.clients;

import java.util.HashMap;
import java.util.Map;

import org.transitime.ipc.interfaces.CacheQueryInterface;
import org.transitime.ipc.interfaces.CommandsInterface;
import org.transitime.ipc.rmi.ClientFactory;

/**
 * Provides a CacheQueryInterface client that can be sent cache queries.
 * 
 * @author Sean Og Crudden
 * 
 */
public class CacheQueryInterfaceFactory {

	// Keyed by agencyId
	private static Map<String, CacheQueryInterface> cachequeryInterfaceMap =
			new HashMap<String, CacheQueryInterface>();

	/********************** Member Functions **************************/

	/**
	 * Gets the singleton instance.
	 * 
	 * @param agencyId
	 * @return
	 */
	public static CacheQueryInterface get(String agencyId) {
		CacheQueryInterface cachequeryInterface =
				cachequeryInterfaceMap.get(agencyId);
		if (cachequeryInterface == null) {
			cachequeryInterface = 
					ClientFactory.getInstance(agencyId, CacheQueryInterface.class);
			cachequeryInterfaceMap.put(agencyId, cachequeryInterface);
		}

		return cachequeryInterface;
	}

}
