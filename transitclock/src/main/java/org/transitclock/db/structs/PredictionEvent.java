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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import net.jcip.annotations.Immutable;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.TemporalMatch;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.Time;

/**
 * For storing events associated with predictions  into log file and into database.
 * The resulting information can be mapped out for finding
 * problems in prediction methods. Based on VehicleEvents class.
 * 
 * @author SkiBu Smith, Sean Óg Crudden
 * 
 */
@Immutable // From jcip.annoations
@Entity @DynamicUpdate 
@Table(name="PredictionEvents",
       indexes = { @Index(name="PredictionEventsTimeIndex", 
                   columnList="time" ) } )
public class PredictionEvent implements Serializable {

	// System time of the event. 
	@Id
	@Column	
	@Temporal(TemporalType.TIMESTAMP)
	private final Date time;

	// Important for understanding context of issue
	@Id
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String vehicleId;
	
	// Short descriptor of event. Not using an enumerator because don't
	// want to have to change this code every time a new event type is
	// created. It is an @Id because several events for a vehicle might
	// happen with the same exact timestamp.
	@Id
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String eventType;
	
	// AVL time of the event. Should correspond to last AVL report time so that
	// can join with AVL report to get more info if necessary.
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private final Date avlTime;
	
	// A more verbose textual description of the event
	private final static int MAX_DESCRIPTION_LENGTH = 500;
	@Column(length=MAX_DESCRIPTION_LENGTH)
	private final String description;

			
	// Latitude/longitude of vehicle when event occurred. Though this could
	// be obtained by joining with corresponding AvlReport from db having
	// the lat/lon here makes it much easier to do things like display
	// events on a map using only a simple query.
	@Embedded
	private final Location location;
	
	// Nice for providing context. Allows for query so can see all events
	// for a route.
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String routeId;

	// Nice for providing context. 
	// routeShortName is included because for some agencies the
	// route_id changes when there are schedule updates. But the
	// routeShortName is more likely to stay consistent. Therefore
	// it is better for when querying for arrival/departure data
	// over a timespan.
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String routeShortName;

	// Nice for providing context. 
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String blockId;

	// Nice for providing context. 
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String serviceId;

	// Nice for providing context. 
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String tripId;

	// Nice for providing context. 
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String stopId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String arrivalstopid;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String departurestopid;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String referenceVehicleId;
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private final Date arrivalTime;
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private final Date departureTime;
	
	// Some standard prediciton event types
	public static final String PREDICTION_VARIATION = "Prediction variation";
	public static final String TRAVELTIME_EXCEPTION = "Travel time exception";
	
	// Hibernate requires class to be Serializable
	private static final long serialVersionUID = -763445348557811925L;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(PredictionEvent.class);



	/********************** Member Functions **************************/

	/**
	 * Simple constructor. Declared private because should be only accessed by
	 * the create() method so that can make sure that do things like log each
	 * creation of a predictionEvent.
	 * 
	 * @param time
	 * @param avlTime
	 * @param vehicleId
	 * @param eventType
	 * @param description
	 * @param predictable
	 * @param becameUnpredictable 
	 * @param supervisor
	 * @param location
	 * @param routeId
	 * @param routeShortName
	 * @param blockId
	 * @param serviceId
	 * @param tripId
	 * @param stopId
	 */
	private PredictionEvent(Date time, Date avlTime, String vehicleId,
			String eventType, String description,
			Location location,
			String routeId, String routeShortName, String blockId,
			String serviceId, String tripId, String stopId, String  arrivalStopId, String departureStopId, String referenceVehicleId, Date arrivalTime, Date departureTime) {
		super();
		this.time = time;
		this.avlTime = avlTime;
		this.vehicleId = vehicleId;
		this.eventType = eventType;
		this.description = description.length() <= MAX_DESCRIPTION_LENGTH ?
				description : description.substring(0, MAX_DESCRIPTION_LENGTH);	
		this.location = location;
		this.routeId = routeId;
		this.routeShortName = routeShortName;
		this.blockId = blockId;
		this.serviceId = serviceId;
		this.tripId = tripId;
		this.stopId = stopId;
		this.arrivalstopid = arrivalStopId;
		this.departurestopid = departureStopId;
		this.referenceVehicleId = referenceVehicleId;
		this.arrivalTime = arrivalTime;
		this.departureTime= departureTime;
		
	}

	/**
	 * Constructs a predictionEvent event and logs it and queues it to be stored in
	 * database.
	 * 
	 * @param time
	 * @param avlTime
	 * @param vehicleId
	 * @param eventType
	 * @param description
	 * @param predictable
	 * @param becameUnpredictable 
	 * @param supervisor
	 * @param location
	 * @param routeId
	 * @param routeShortName
	 * @param blockId
	 * @param serviceId
	 * @param tripId
	 * @param stopId
	 * @return The predictionEvent constructed
	 */
	public static PredictionEvent create(Date time, Date avlTime,
			String vehicleId, String eventType, String description,
			Location location, String routeId,
			String routeShortName, String blockId, String serviceId,
			String tripId, String stopId,  String  arrivalStopId, String departureStopId, String referenceVehicleId, Date arrivalTime, Date departureTime) {
		PredictionEvent predictionEvent =
				new PredictionEvent(time, avlTime, vehicleId, eventType,
						description, location, routeId, routeShortName, blockId,
						serviceId, tripId, stopId, arrivalStopId, departureStopId,  referenceVehicleId, arrivalTime, departureTime);

		// Log predictionEvent in log file
		logger.info(predictionEvent.toString());

		// Queue to write object to database
		Core.getInstance().getDbLogger().add(predictionEvent);

		// Return new predictionEvent
		return predictionEvent;
	}

	/**
	 * A simpler way to create a VehicleEvent that gets a lot of its info from
	 * the avlReport and match params. This also logs it and queues it to be
	 * stored in database. The match param can be null.
	 * 
	 * @param avlReport
	 * @param match
	 * @param eventType
	 * @param description
	 * @param predictable
	 * @param becameUnpredictable
	 * @param supervisor
	 * @return The VehicleEvent constructed
	 */
	public static PredictionEvent create(AvlReport avlReport, TemporalMatch match,
			String eventType, String description,  String  arrivalStopId, String departureStopId , String referenceVehicleId, Date arrivalTime, Date departureTime) {
		// Get a log of the info from the possibly null match param
		String routeId = match==null ? null : match.getTrip().getRouteId();
		String routeShortName = 
				match==null ? null : match.getTrip().getRouteShortName();
		String blockId = match==null ? null : match.getBlock().getId();
		String serviceId = match==null ? null : match.getBlock().getServiceId();
		String tripId = match==null ? null : match.getTrip().getId();
		String stopId = match==null ? null : match.getStopPath().getStopId();
		
		// Create and return the VehicleEvent
		return create(Core.getInstance().getSystemDate(), avlReport.getDate(),
				avlReport.getVehicleId(), eventType, description, avlReport.getLocation(),
				routeId, routeShortName, blockId, serviceId, tripId, stopId, arrivalStopId, departureStopId, referenceVehicleId, arrivalTime, departureTime);
	}
	
	/**
	 * Hibernate requires a no-args constructor for reading data.
	 * So this is an experiment to see what can be done to satisfy
	 * Hibernate but still have an object be immutable. Since
	 * this constructor is only intended to be used by Hibernate
	 * is is declared protected, since that still works. That way
	 * others won't accidentally use this inappropriate constructor.
	 * And yes, it is peculiar that even though the members in this
	 * class are declared final that Hibernate can still create an
	 * object using this no-args constructor and then set the fields.
	 * Not quite as "final" as one might think. But at least it works.
	 */
	protected PredictionEvent() {
		this.time = null;
		this.avlTime = null;
		this.vehicleId = null;
		this.eventType = null;
		this.description = null;		
		this.location = null;
		this.routeId = null;
		this.routeShortName = null;
		this.blockId = null;
		this.serviceId = null;
		this.tripId = null;
		this.stopId = null;
		this.arrivalstopid = null;
		this.departurestopid = null;
		this.referenceVehicleId = null;
		this.departureTime = null;
		this.arrivalTime = null;
	}

	public String getArrivalstopid() {
		return arrivalstopid;
	}

	public String getDeparturestopid() {
		return departurestopid;
	}

	public Date getTime() {
		return time;
	}

	public String getVehicleId() {
		return vehicleId;
	}

	public String getEventType() {
		return eventType;
	}

	public Date getAvlTime() {
		return avlTime;
	}

	public String getDescription() {
		return description;
	}

	public Location getLocation() {
		return location;
	}

	public String getRouteId() {
		return routeId;
	}

	public String getRouteShortName() {
		return routeShortName;
	}

	public String getBlockId() {
		return blockId;
	}

	public String getServiceId() {
		return serviceId;
	}

	public String getTripId() {
		return tripId;
	}

	public String getStopId() {
		return stopId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arrivalTime == null) ? 0 : arrivalTime.hashCode());
		result = prime * result + ((arrivalstopid == null) ? 0 : arrivalstopid.hashCode());
		result = prime * result + ((avlTime == null) ? 0 : avlTime.hashCode());
		result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
		result = prime * result + ((departureTime == null) ? 0 : departureTime.hashCode());
		result = prime * result + ((departurestopid == null) ? 0 : departurestopid.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((referenceVehicleId == null) ? 0 : referenceVehicleId.hashCode());
		result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
		result = prime * result + ((routeShortName == null) ? 0 : routeShortName.hashCode());
		result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
		result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
		result = prime * result + ((vehicleId == null) ? 0 : vehicleId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PredictionEvent other = (PredictionEvent) obj;
		if (arrivalTime == null) {
			if (other.arrivalTime != null)
				return false;
		} else if (!arrivalTime.equals(other.arrivalTime))
			return false;
		if (arrivalstopid == null) {
			if (other.arrivalstopid != null)
				return false;
		} else if (!arrivalstopid.equals(other.arrivalstopid))
			return false;
		if (avlTime == null) {
			if (other.avlTime != null)
				return false;
		} else if (!avlTime.equals(other.avlTime))
			return false;
		if (blockId == null) {
			if (other.blockId != null)
				return false;
		} else if (!blockId.equals(other.blockId))
			return false;
		if (departureTime == null) {
			if (other.departureTime != null)
				return false;
		} else if (!departureTime.equals(other.departureTime))
			return false;
		if (departurestopid == null) {
			if (other.departurestopid != null)
				return false;
		} else if (!departurestopid.equals(other.departurestopid))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (eventType == null) {
			if (other.eventType != null)
				return false;
		} else if (!eventType.equals(other.eventType))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (referenceVehicleId == null) {
			if (other.referenceVehicleId != null)
				return false;
		} else if (!referenceVehicleId.equals(other.referenceVehicleId))
			return false;
		if (routeId == null) {
			if (other.routeId != null)
				return false;
		} else if (!routeId.equals(other.routeId))
			return false;
		if (routeShortName == null) {
			if (other.routeShortName != null)
				return false;
		} else if (!routeShortName.equals(other.routeShortName))
			return false;
		if (serviceId == null) {
			if (other.serviceId != null)
				return false;
		} else if (!serviceId.equals(other.serviceId))
			return false;
		if (stopId == null) {
			if (other.stopId != null)
				return false;
		} else if (!stopId.equals(other.stopId))
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		if (tripId == null) {
			if (other.tripId != null)
				return false;
		} else if (!tripId.equals(other.tripId))
			return false;
		if (vehicleId == null) {
			if (other.vehicleId != null)
				return false;
		} else if (!vehicleId.equals(other.vehicleId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PredictionEvent [time=" + time + ", vehicleId=" + vehicleId + ", eventType=" + eventType + ", avlTime="
				+ avlTime + ", description=" + description + ", location=" + location + ", routeId=" + routeId
				+ ", routeShortName=" + routeShortName + ", blockId=" + blockId + ", serviceId=" + serviceId
				+ ", tripId=" + tripId + ", stopId=" + stopId + ", arrivalstopid=" + arrivalstopid
				+ ", departurestopid=" + departurestopid + ", referenceVehicleId=" + referenceVehicleId
				+ ", arrivalTime=" + arrivalTime + ", departureTime=" + departureTime + "]";
	}


	
}
