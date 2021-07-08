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

package org.transitclock.api.data.siri;

import java.text.DateFormat;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;

import org.transitclock.ipc.data.IpcPrediction;
import org.transitclock.ipc.data.IpcVehicleComplete;
import org.transitclock.utils.StringUtils;

/**
 * For SIRI MonitorCall element.
 *
 * @author SkiBu Smith
 *
 */
public class SiriMonitoredCall {

	@XmlElement(name = "StopPointRef")
	private String stopPointRef;

	@XmlElement(name = "VisitNumber")
	private int visitNumber;

	// The arrival/departure time elements were found in
	// http://user47094.vs.easily.co.uk/siri/schema/1.4/examples/exs_stopMonitoring_response.xml
	// Scheduled time not currently available via IPC so not available here.
	@XmlElement(name = "AimedArrivalTime")
	String aimedArrivalTime;

	// Predicted arrival time
	@XmlElement(name = "ExpectedArrivalTime")
	String expectedArrivalTime;

	// Scheduled time not currently available via IPC so not available here.
	@XmlElement(name = "AimedDepartureTime")
	String aimedDepartureTime;

	// Predicted departure time
	@XmlElement(name = "ExpectedDepartureTime")
	String expectedDepartureTime;

	// NYC MTA extensions
	@XmlElement(name = "Extensions")
	private Extensions extensions;

	/**
	 * The MTA Bus Time extensions to show distance of the vehicle from the stop
	 */
	public static class Extensions {
		@XmlElement(name = "Distances")
		private Distances distances;

		/**
		 * Need a no-arg constructor for Jersey for JSON. Otherwise get really
		 * obtuse "MessageBodyWriter not found for media type=application/json"
		 * exception.
		 */
		protected Extensions() {
		}

		public Extensions(IpcVehicleComplete ipcCompleteVehicle) {
			distances = new Distances(ipcCompleteVehicle);
		}
	}

	/**
	 * The MTA Bus Time extensions to show distance of the vehicle from the stop
	 */
	public static class Distances {
		// The distance of the stop from the beginning of the trip/route
		@XmlElement(name = "CallDistanceAlongRoute")
		private String callDistanceAlongRoute;

		// The distance from the vehicle to the stop along the route, in meters
		@XmlElement(name = "DistanceFromCall")
		private String distanceFromCall;

		/**
		 * Need a no-arg constructor for Jersey for JSON. Otherwise get really
		 * obtuse "MessageBodyWriter not found for media type=application/json"
		 * exception.
		 */
		protected Distances() {
		}

		public Distances(IpcVehicleComplete ipcCompleteVehicle) {
			callDistanceAlongRoute =
					StringUtils.oneDigitFormat(ipcCompleteVehicle
							.getDistanceOfNextStopFromTripStart());

			distanceFromCall =
					StringUtils.oneDigitFormat(ipcCompleteVehicle
							.getDistanceToNextStop());
		}
	}

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey for JSON. Otherwise get really
	 * obtuse "MessageBodyWriter not found for media type=application/json"
	 * exception.
	 */
	protected SiriMonitoredCall() {
	}

	/**
	 * Constructs a MonitoredCall element.
	 * 
	 * @param ipcCompleteVehicle
	 * @param prediction
	 *            The prediction for when doing stop monitoring. When doing
	 *            vehicle monitoring should be set to null.
	 * @param timeFormatter
	 *            For converting epoch time into a Siri time string
	 */
	public SiriMonitoredCall(IpcVehicleComplete ipcCompleteVehicle,
			IpcPrediction prediction, DateFormat timeFormatter) {
		stopPointRef = ipcCompleteVehicle.getNextStopId();
		// Always using value of 1 for now
		visitNumber = 1;

		// Deal with the predictions if StopMonitoring query.
		// Don't have schedule time available so can't provide it.
		if (prediction != null) {
			if (prediction.isArrival()) {
				expectedArrivalTime =
						timeFormatter.format(new Date(prediction.getPredictionTime()));
			} else {
				expectedDepartureTime =
						timeFormatter.format(new Date(prediction.getPredictionTime()));
			}
		}

		// Deal with NYC MTA extensions
		extensions = new Extensions(ipcCompleteVehicle);
	}
}
