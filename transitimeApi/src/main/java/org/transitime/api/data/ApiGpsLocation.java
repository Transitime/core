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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.transitime.ipc.data.IpcVehicle;
import org.transitime.utils.MathUtils;
import org.transitime.utils.Time;

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

	// A Double so if null then won't show up in output
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
	 * @param lat
	 * @param lon
	 */
	public ApiGpsLocation(IpcVehicle vehicle) {
		super(vehicle.getLatitude(), vehicle.getLongitude());

		this.time = vehicle.getGpsTime() / Time.MS_PER_SEC;
		// Output only 1 digit past decimal point
		this.speed = Float.isNaN(vehicle.getSpeed()) ? 
				null : MathUtils.round(vehicle.getSpeed(), 1);
		// Output only 1 digit past decimal point
		this.heading = Float.isNaN(vehicle.getHeading()) ? 
				null : MathUtils.round(vehicle.getHeading(), 1);

	}

}
