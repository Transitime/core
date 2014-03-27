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

import org.transitime.ipc.interfaces.ConfigInterface;
import org.transitime.ipc.rmi.ClientFactory;

/**
 * Provides a ConfigInterface client that can be queried for 
 * configuration info.
 *
 * @author SkiBu Smith
 *
 */
public class ConfigInterfaceFactory {

	// Keyed by projectId
	private static Map<String, ConfigInterface> configInterfaceMap =
			new HashMap<String, ConfigInterface>();

	/********************** Member Functions **************************/

	/**
	 * Gets the singleton instance.
	 * 
	 * @param projectId
	 * @return
	 */
	public static ConfigInterface get(String projectId) {
		ConfigInterface configInterface =
				configInterfaceMap.get(projectId);
		if (configInterface == null) {
			configInterface = 
					ClientFactory.getInstance(projectId, ConfigInterface.class);
			configInterfaceMap.put(projectId, configInterface);
		}

		return configInterface;
	}

}
