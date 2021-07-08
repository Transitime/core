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

/**
 * For SIRI MonitoredVehicleJourney element
 *
 * @author SkiBu Smith
 *
 */
public class SiriMonitoredVehicleJourney {

	// Vehicle Id
	@XmlElement(name = "VehicleRef")
	private String vehicleRef;

	// Location of vehicle
	@XmlElement(name = "VehicleLocation")
	private SiriLocation vehicleLocation;

	// Vehicle bearing: 0 is East, increments counter-clockwise.
	// This of course is different from heading, where 0 is north
	// and it goes clockwise.
	@XmlElement(name = "Bearing")
	private String bearingStr;

	// Block ID
	@XmlElement(name = "BlockRef")
	private String blockRef;

	// The route name
	@XmlElement(name = "LineRef")
	private String lineRef;

	// The GTFS direction
	@XmlElement(name = "DirectionRef")
	private String directionRef;

	// Describes the trip
	@XmlElement(name = "FramedVehicleJourneyRef")
	private SiriFramedVehicleJourneyRef framedVehicleJourneyRef;

	// Name of route. Using short name since that is available and is
	// more relevant.
	@XmlElement(name = "PublishedLineName")
	private String publishedLineName;

	// Name of agency
	@XmlElement(name = "OperatorRef")
	private String operatorRef;

	@XmlElement(name = "OriginRef")
	private String originRef;

	@XmlElement(name = "DestinationRef")
	private String destinationRef;

	@XmlElement(name = "DestinationName")
	private String destinationName;

	@XmlElement(name = "OriginAimedDepartureTime")
	private String originAimedDepartureTime;

	// Whether vehicle tracked
	@XmlElement(name = "Monitored")
	private String monitored;

	// Indicator of whether the bus is making progress (i.e. moving, generally)
	// or not (with value noProgress).
	@XmlElement(name = "ProgressRate")
	private String progressRate;

	@XmlElement(name = "ProgressStatus")
	private String progressStatus;

	@XmlElement(name = "MonitoredCall")
	private SiriMonitoredCall monitoredCall;

	@XmlElement(name = "OnwardCalls")
	private String onwardCalls;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey for JSON. Otherwise get really
	 * obtuse "MessageBodyWriter not found for media type=application/json"
	 * exception.
	 */
	protected SiriMonitoredVehicleJourney() {
	}

	/**
	 * Constructs that massive MonitoredVehicleJourney element.
	 * 
	 * @param ipcCompleteVehicle
	 * @param prediction
	 *            For when doing stop monitoring. If doing vehicle monitoring
	 *            then should be set to null.
	 * @param agencyId
	 * @param timeFormatter
	 *            For converting epoch time into a Siri time string
	 * @param dateFormatter
	 *            For converting epoch time into a Siri date string
	 */
	public SiriMonitoredVehicleJourney(IpcVehicleComplete ipcCompleteVehicle,
			IpcPrediction prediction, String agencyId,
			DateFormat timeFormatter, DateFormat dateFormatter) {
		vehicleRef = ipcCompleteVehicle.getId();
		vehicleLocation =
				new SiriLocation(ipcCompleteVehicle.getLatitude(),
						ipcCompleteVehicle.getLongitude());
		double bearing = 90 - ipcCompleteVehicle.getHeading();
		if (bearing < 0)
			bearing += 360.0;
		bearingStr = Double.toString(bearing);
		blockRef = ipcCompleteVehicle.getBlockId();
		lineRef = ipcCompleteVehicle.getRouteShortName();
		directionRef = ipcCompleteVehicle.getDirectionId();
		framedVehicleJourneyRef =
				new SiriFramedVehicleJourneyRef(ipcCompleteVehicle,
						dateFormatter);
		publishedLineName = ipcCompleteVehicle.getRouteName();
		operatorRef = agencyId;
		originRef = ipcCompleteVehicle.getOriginStopId();
		destinationRef = ipcCompleteVehicle.getDestinationId();
		destinationName = ipcCompleteVehicle.getHeadsign();
		originAimedDepartureTime =
				timeFormatter.format(new Date(ipcCompleteVehicle
						.getTripStartEpochTime()));
		monitored = "true";
		progressRate = "normalProgress";
		progressStatus = ipcCompleteVehicle.isLayover() ? "true" : null;

		monitoredCall = new SiriMonitoredCall(ipcCompleteVehicle, prediction, timeFormatter);

		// Not currently implemented but outputting it for completeness
		onwardCalls = "";
	}
}
