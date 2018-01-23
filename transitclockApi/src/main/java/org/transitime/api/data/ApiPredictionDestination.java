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

import org.transitime.ipc.data.IpcPrediction;
import org.transitime.ipc.data.IpcPredictionsForRouteStopDest;

/**
 * Contains list of predictions for a particular headsign.
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement
public class ApiPredictionDestination {

	@XmlAttribute(name = "dir")
	private String directionId;

	@XmlAttribute
	private String headsign;

	@XmlElement(name = "pred")
	private List<ApiPrediction> predictions;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiPredictionDestination() {
	}

	public ApiPredictionDestination(
			IpcPredictionsForRouteStopDest predictionsForRouteStop) {
		directionId = predictionsForRouteStop.getDirectionId();
		headsign = predictionsForRouteStop.getHeadsign();

		predictions = new ArrayList<ApiPrediction>();
		for (IpcPrediction prediction : predictionsForRouteStop
				.getPredictionsForRouteStop()) {
			predictions.add(new ApiPrediction(prediction));
		}
	}
}
