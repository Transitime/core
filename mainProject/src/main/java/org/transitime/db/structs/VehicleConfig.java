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

package org.transitime.db.structs;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.transitime.db.hibernate.HibernateUtils;

/**
 * For storing static configuration information for a vehicle.
 *
 * @author SkiBu Smith
 *
 */
@Entity @DynamicUpdate @Table(name="VehicleConfigs")
public class VehicleConfig {

	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	@Id
	private final String id;
	
	// Same as vehicle type in GTFS
	@Column
	private final Integer type;
	
	@Column
	private final String description;
	
	@Column
	private final Integer capacity;
	
	@Column 
	private final Integer crushCapacity;
	
	@Column
	private final Boolean passengerVehicle;
	
	/********************** Member Functions **************************/

	/**
	 * Constructor for when new vehicle encountered
	 * 
	 * @param id
	 */
	public VehicleConfig(String id) {
		this.id = id;
		type = null;
		description = null;
		capacity = null;
		crushCapacity = null;
		passengerVehicle = null;
	}

	/**
	 * Needed because Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private VehicleConfig() {
		id = null;
		type = null;
		description = null;
		capacity = null;
		crushCapacity = null;
		passengerVehicle = null;
	}

	/**
	 * Reads List of VehicleConfig objects from database
	 * 
	 * @param session
	 * @return List of VehicleConfig objects
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<VehicleConfig> getVehicleConfigs(Session session) 
			throws HibernateException {
		String hql = "FROM VehicleConfig";
		Query query = session.createQuery(hql);
		return query.list();
	}
	
	@Override
	public String toString() {
		return "VehicleConfig [" 
				+ "id=" + id 
				+ ", type=" + type 
				+ ", description=" + description 
				+ ", capacity=" + capacity
				+ ", crushCapacity=" + crushCapacity
				+ ", passengerVehicle=" + passengerVehicle 
				+ "]";
	}

	/************* Getter Methods ****************************/
	
	/**
	 * @return the vehicle ID
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return The type of the vehicle. Should be same as GTFS route type:
     * 0 - Tram, Streetcar, Light rail. Any light rail or street level system within a metropolitan area.
     * 1 - Subway, Metro. Any underground rail system within a metropolitan area.
     * 2 - Rail. Used for intercity or long-distance travel.
     * 3 - Bus. Used for short- and long-distance bus routes.
     * 4 - Ferry. Used for short- and long-distance boat service.
     * 5 - Cable car. Used for street-level cable cars where the cable runs beneath the car.
     * 6 - Gondola, Suspended cable car. Typically used for aerial cable cars where the car is suspended from the cable.
     * 7 - Funicular. Any rail system designed for steep inclines.
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * @return A description of the vehicle such as "New Flyer"
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return Passenger capacity of vehicle. Typically number of seats.
	 */
	public Integer getCapacity() {
		return capacity;
	}

	/**
	 * @return Passenger capacity of vehicle including standees. For when cannot
	 *         pick up any more passengers.
	 */
	public Integer getCrushCapacity() {
		return crushCapacity;
	}
	
	/**
	 * @return True if a revenue passenger vehicle as opposed to some type of
	 *         service vehicle.
	 */
	public Boolean getPassengerVehicle() {
		return passengerVehicle;
	}
	
}
