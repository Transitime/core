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

package org.transitclock.api.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.transitclock.api.utils.MathUtils;
import org.transitclock.api.utils.NumberFormatter;
import org.transitclock.ipc.data.IpcVehicle;
import org.transitclock.utils.Time;

/**
 * Extends a location by including GPS information including time, speed,
 * heading, and pathHeading.
 * 
 *
 * @author SkiBu Smith
 *
 */
@XmlType(propOrder = { "lat", "lon", "time", "speed", "heading" })
public class ApiGpsLocation extends ApiTransientLocation {

	// Epoch time in seconds (not msec, so that shorter)
	@XmlAttribute
	private long time;

	// Speed in m/s. A Double so if null then won't show up in output
	@XmlAttribute
	private Double speed;

	// A Double so if null then won't show up in output
	@XmlAttribute
	private Double heading;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiGpsLocation() {
	}

	/**
	 *
	 * @param vehicle
	 */
	public ApiGpsLocation(IpcVehicle vehicle, SpeedFormat speedFormat) {
		super(vehicle.getLatitude(), vehicle.getLongitude());

		this.time = vehicle.getGpsTime() / Time.MS_PER_SEC;
		// Output only 1 digit past decimal point
		this.speed = NumberFormatter.getRoundedValueAsDouble(MathUtils.convertSpeed(vehicle.getSpeed(), speedFormat), 1);

		// Output only 1 digit past decimal point
		this.heading = NumberFormatter.getRoundedValueAsDouble(vehicle.getHeading(), 1);

	}

}
