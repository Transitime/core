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

package org.transitclock.db.structs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.db.hibernate.HibernateUtils;

/**
 * For storing static configuration information for a vehicle.
 *
 * This class was originally for external vehicle configuration,
 * but was later repurposed to GTFS vehicle extension as both have
 * the same intention
 *
 * @author SkiBu Smith
 * @author shelondabrown
 *
 */
@Entity @DynamicUpdate @Table(name="VehicleConfigs")
public class VehicleConfig {

	// ID of vehicle
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	@Id
	private final String id;
	
	// Same as vehicle type in GTFS
	@Column
	private final Integer type;
	
	// A more verbose description of the vehicle.
	@Column
	private final String description;
	
	// Useful for when getting a GPS feed that has a tracker ID, like an IMEI 
	// or phone #, instead of a vehicle ID. Allows the corresponding vehicleId 
	// to be determined from the VehicleConfig object.
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) 
	private final String trackerId;
	
	// Typical capacity of vehicle 
	@Column
	private final Integer capacity;
	
	// Absolute crush capacity of vehicle. Number of people who can be 
	// squeezed in.
	@Column 
	private final Integer crushCapacity;
	
	// If true then a non-revenue vehicle.
	@Column
	private final Boolean nonPassengerVehicle;

	@Column
	private final Integer doorCount;

	@Column
	private final String doorWidth;

	@Column
	private final Integer lowFloor;

	@Column
	private final Integer bikeCapacity;

	@Column
	private final String wheelchairAccess;
	
	/********************** Member Functions **************************/

	/**
	 * Constructor for when new vehicle encountered and automatically adding it
	 * to the db.
	 * 
	 * @param id vehicle ID
	 */
	public VehicleConfig(String id) {
		this.id = id;
		type = null;
		description = null;
		trackerId = null;
		capacity = null;
		crushCapacity = null;
		nonPassengerVehicle = null;
		doorCount = null;
		doorWidth = null;
		lowFloor = null;
		bikeCapacity = null;
		wheelchairAccess = null;
	}

	/**
	 * GTFS Vehicle extension constructor.
	 */
	public VehicleConfig(String vehicleId,
											 String vehicleDescription,
											 Integer seatedCapacity,
											 Integer standingCapacity,
											 Integer doorCount,
											 String doorWidth,
											 Integer lowFloor,
											 Integer bikeCapacity,
											 String wheelchairAccess) {
		this.id = vehicleId;
		this.type = 3; // TODO we assume bus here
		this.description = vehicleDescription;
		this.trackerId = null;
		this.capacity = seatedCapacity;
		this.crushCapacity = standingCapacity;
		this.nonPassengerVehicle = null;
		this.doorCount = doorCount;
		this. doorWidth = doorWidth;
		this.lowFloor = lowFloor;
		this.bikeCapacity = bikeCapacity;
		this.wheelchairAccess = wheelchairAccess;

	}

	/**
	 * Needed because Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private VehicleConfig() {
		id = null;
		type = null;
		description = null;
		trackerId = null;
		capacity = null;
		crushCapacity = null;
		nonPassengerVehicle = null;
		doorCount = null;
		doorWidth = null;
		lowFloor = null;
		bikeCapacity = null;
		wheelchairAccess = null;
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

	public static Map<String, VehicleConfig> buildConfigMap(Session session) {
		Map<String, VehicleConfig> map = new HashMap<>();
		List<VehicleConfig> configs = getVehicleConfigs(session);
		if (configs == null) return map;
		for (VehicleConfig vc : configs) {
			map.put(vc.getId(), vc);
		}
		return map;
	}

	@Override
	public String toString() {
		return "VehicleConfig [" 
				+ "id=" + id 
				+ ", type=" + type 
				+ ", description=" + description
				+ ", trackerId=" + trackerId
				+ ", capacity=" + capacity
				+ ", crushCapacity=" + crushCapacity
				+ ", nonPassengerVehicle=" + nonPassengerVehicle 
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
	 * @return The tracker ID used for when getting a direct GPS report that
	 *         doesn't include vehicleID.
	 */
	public String getTrackerId() {
		return trackerId;
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
	public Boolean isNonPassengerVehicle() {
		return nonPassengerVehicle;
	}

	/**
	 * number of doors for exit purposes.
	 * @return
	 */
	public Integer getDoorCount() { return doorCount; }

	/**
	 * Door width:  normal or wide.
	 * @return
	 */
	public String getDoorWidth() { return doorWidth; }

	/**
	 * flag for lowFloor.
	 * @return
	 */
	public Integer getLowFloor() { return lowFloor; }

	/**
	 * static configuration for bicycles.
	 * @return
	 */
	public Integer getBikeCapacity() { return bikeCapacity; }

	/**
	 * type of configuration for wheelchair access.
	 * @return
	 */
	public String getWheelchairAccess() { return wheelchairAccess; }

}
