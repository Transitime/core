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
package org.transitime.worldbank.gtfsRtExample;

import java.util.Date;

/**
 * 
 * For keeping data from the database.
 *
 * @author SkiBu Smith
 */
public class VehicleData {
	private String vehicleId;
	private String licensePlate;
	private float latitude;
	private float longitude;
	private String speed; // km/hr
	private String heading;
	private Date gpsTime;
	private String routeId;
	
	/**
	 * @param vehicleId
	 * @param licensePlate
	 * @param latitude2
	 * @param longitude2
	 * @param speed2
	 * @param heading2
	 * @param gpsTime2
	 * @param routeId
	 */
	public VehicleData(String vehicleId, String licensePlate, float latitude, 
			float longitude, String speed, String heading, Date gpsTime, String routeId) {
		this.vehicleId = vehicleId;
		this.licensePlate = licensePlate;
		this.latitude = latitude;
		this.longitude = longitude;
		this.speed = speed;
		this.heading = heading;
		this.gpsTime = gpsTime;
		this.routeId = routeId;
	}

	/**
	 * For creating error messages
	 */
	@Override
	public String toString() {
		return "VehicleData [" 
				+ "vehicleId=" + vehicleId 
				+ ", licensePlate="	+ licensePlate 
				+ ", latitude=" + latitude 
				+ ", longitude=" + longitude 
				+ ", speed=" + speed 
				+ ", heading=" + heading
				+ ", gpsTime=" + gpsTime 
				+ ", routeId=" + routeId 
				+ "]";
	}


	public String getVehicleId() {
		return vehicleId;
	}

	public String getLicensePlate() {
		return licensePlate;
	}
	
	public float getLatitude() {
		return latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	/**
	 * @return speed in kilometers/hour
	 */
	public String getSpeed() {
		return speed; 
	}

	public String getHeading() {
		return heading;
	}

	public Date getGpsTime() {
		return gpsTime;
	}

	public String getRouteId() {
		return routeId;
	}
	
}
