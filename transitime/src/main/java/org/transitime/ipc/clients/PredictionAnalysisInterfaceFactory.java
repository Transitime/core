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

import org.transitime.ipc.interfaces.PredictionAnalysisInterface;
import org.transitime.ipc.interfaces.PredictionsInterface;
import org.transitime.ipc.rmi.ClientFactory;

/**
 * Provides a PredictionsInterface client that can be queried for 
 * predictions.
 * 
 * @author Sean Og Crudden
 * 
 */
public class PredictionAnalysisInterfaceFactory {

	// Keyed by agencyId
	private static Map<String, PredictionAnalysisInterface> predictionAnalysisInterfaceMap =
			new HashMap<String, PredictionAnalysisInterface>();

	/********************** Member Functions **************************/

	/**
	 * Gets the PredictionAnalysisInterface for the specified projectId. There is one
	 * interface per agencyId.
	 * 
	 * @param agencyId
	 * @return
	 */
	public static PredictionAnalysisInterface get(String agencyId) {
		PredictionAnalysisInterface predictionAnalysisInterface =
				predictionAnalysisInterfaceMap.get(agencyId);
		if (predictionAnalysisInterface == null) {
			predictionAnalysisInterface = 
					ClientFactory.getInstance(agencyId, PredictionAnalysisInterface.class);
			predictionAnalysisInterfaceMap.put(agencyId, predictionAnalysisInterface);
		}
		return predictionAnalysisInterface;
	}

}
