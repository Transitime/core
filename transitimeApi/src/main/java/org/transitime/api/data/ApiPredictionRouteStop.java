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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitime.ipc.data.IpcPredictionsForRouteStopDest;
import org.transitime.utils.MathUtils;

/**
 * List of ApiPredictionDestination objects along with supporting information.
 * Used to output predictions for a particular stop where the predictions are
 * grouped by headsign.
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement
public class ApiPredictionRouteStop {

	@XmlAttribute
	private String routeShortName;

	@XmlAttribute
	private String routeName;

	@XmlAttribute
	private String routeId;

	@XmlAttribute
	private String stopId;

	@XmlAttribute
	private String stopName;

	@XmlAttribute
	private Integer stopCode;

	// Using String so that it will not be output if not showing predictions
	// by location because then this value will be null. Also, can this way
	// format it to desired number of digits of precision.
	@XmlAttribute
	private Double distanceToStop;

	@XmlElement(name = "dest")
	private List<ApiPredictionDestination> destinations;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiPredictionRouteStop() {
	}

	public ApiPredictionRouteStop(
			List<IpcPredictionsForRouteStopDest> predictionsForRouteStop) {
		if (predictionsForRouteStop == null
				|| predictionsForRouteStop.isEmpty())
			return;

		IpcPredictionsForRouteStopDest routeStopInfo =
				predictionsForRouteStop.get(0);
		routeShortName = routeStopInfo.getRouteShortName();
		routeName = routeStopInfo.getRouteName();
		routeId = routeStopInfo.getRouteId();
		stopId = routeStopInfo.getStopId();
		stopName = routeStopInfo.getStopName();
		stopCode = routeStopInfo.getStopCode();
		distanceToStop =
				Double.isNaN(routeStopInfo.getDistanceToStop()) ? null
						: MathUtils.round(routeStopInfo.getDistanceToStop(), 1);

		destinations = new ArrayList<ApiPredictionDestination>();
		for (IpcPredictionsForRouteStopDest destinationInfo : predictionsForRouteStop) {
			destinations.add(new ApiPredictionDestination(destinationInfo));
		}
	}

}
