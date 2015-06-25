/*
 * This file is part of Transitime.org
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

package org.transitime.api.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitime.ipc.data.IpcPredictionsForRouteStopDest;

/**
 * Contains predictions for multiple stops.
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement(name = "preds")
public class ApiPredictions {

	@XmlElement(name = "routeStop")
	private List<ApiPredictionRouteStop> predictionsForRouteStop;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	public ApiPredictions() {
	}

	/**
	 * For constructing a ApiPredictions object from a List of
	 * IpcPredictionsForRouteStopDest objects.
	 * 
	 * @param vehicles
	 * 
	 */
	public ApiPredictions(
			List<IpcPredictionsForRouteStopDest> predsForRouteStopDestinations) {
		predictionsForRouteStop = new ArrayList<ApiPredictionRouteStop>();

		// Get all the PredictionsForRouteStopDest that are for the same
		// route/stop and create a PredictionsRouteStopData object for each
		// route/stop.
		List<IpcPredictionsForRouteStopDest> predsForRouteStop = null;
		String previousRouteStopStr = "";
		for (IpcPredictionsForRouteStopDest predsForRouteStopDest : predsForRouteStopDestinations) {
			// If this is a new route/stop...
			String currentRouteStopStr =
					predsForRouteStopDest.getRouteId()
							+ predsForRouteStopDest.getStopId();
			if (!currentRouteStopStr.equals(previousRouteStopStr)) {
				// This is a new route/stop
				if (predsForRouteStop != null && !predsForRouteStop.isEmpty()) {
					// create PredictionsRouteStopData object for this
					// route/stop
					ApiPredictionRouteStop predictionsForRouteStopData =
							new ApiPredictionRouteStop(predsForRouteStop);
					predictionsForRouteStop.add(predictionsForRouteStopData);
				}
				predsForRouteStop =
						new ArrayList<IpcPredictionsForRouteStopDest>();
				previousRouteStopStr = currentRouteStopStr;
			}
			predsForRouteStop.add(predsForRouteStopDest);
		}

		// Add the last set of route/stop data
		ApiPredictionRouteStop predictionsForRouteStopData =
				new ApiPredictionRouteStop(predsForRouteStop);
		predictionsForRouteStop.add(predictionsForRouteStopData);
	}

}
