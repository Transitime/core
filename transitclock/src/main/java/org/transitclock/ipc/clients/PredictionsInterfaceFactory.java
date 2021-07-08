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

import org.transitclock.ipc.interfaces.PredictionsInterface;
import org.transitclock.ipc.rmi.ClientFactory;

/**
 * Provides a PredictionsInterface client that can be queried for 
 * predictions.
 * 
 * @author SkiBu Smith
 * 
 */
public class PredictionsInterfaceFactory {

	// Keyed by agencyId
	private static Map<String, PredictionsInterface> predictionsInterfaceMap =
			new HashMap<String, PredictionsInterface>();

	/********************** Member Functions **************************/

	/**
	 * Gets the PredictionsInterface for the specified projectId. There is one
	 * interface per agencyId.
	 * 
	 * @param agencyId
	 * @return
	 */
	public static PredictionsInterface get(String agencyId) {
		PredictionsInterface predictionsInterface =
				predictionsInterfaceMap.get(agencyId);
		if (predictionsInterface == null) {
			predictionsInterface = 
					ClientFactory.getInstance(agencyId, PredictionsInterface.class);
			predictionsInterfaceMap.put(agencyId, predictionsInterface);
		}

		return predictionsInterface;
	}

}
