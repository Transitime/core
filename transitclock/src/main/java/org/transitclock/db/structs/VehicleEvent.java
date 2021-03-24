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
 * For storing events associated with vehicles into log file and into database.
 * Used for situations such as vehicles becoming predictable or unpredictable
 * and specifying why. The resulting information can be mapped out for finding
 * problems.
 * 
 * @author SkiBu Smith
 * 
 */
@Immutable // From jcip.annoations
@Entity @DynamicUpdate 
@Table(name="VehicleEvents",
       indexes = { @Index(name="VehicleEventsTimeIndex", 
                   columnList="time" ) } )
public class VehicleEvent implements Serializable {

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

	// The new state of the vehicle.
	// Using boolean instead of Boolean because it is a required element
	// and must therefore not be null.
	@Column
	private final boolean predictable;
	
	// Whether this event caused the vehicle to become unpredictable. This
	// is specifically to record transitions from predictable to 
	// unpredictable because a common use of VehicleEvents is to find
	// out specifically why a vehicle became unpredictable.
	// Using boolean instead of Boolean because it is a required element
	// and must therefore not be null.
	@Column
	private final boolean becameUnpredictable;
	
	// If event was initiated by a supervisor, such as logging out
	// a vehicle, then the login for the supervisor should also
	// be stored.
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String supervisor;
	
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

	
	// Some standard event types
	public static final String PREDICTABLE = "Predictable";
	public static final String TIMEOUT = "Timeout";
	public static final String NO_MATCH = "No match";
	public static final String NO_PROGRESS = "No progress";
	public static final String DELAYED = "Delayed";
	public static final String END_OF_BLOCK = "End of block";
	public static final String LEFT_TERMINAL_EARLY = "Left terminal early";
	public static final String LEFT_TERMINAL_LATE = "Left terminal late";
	public static final String NOT_LEAVING_TERMINAL = "Not leaving terminal";
	public static final String ASSIGNMENT_GRABBED = "Assignment Grabbed";
	public static final String ASSIGNMENT_CHANGED = "Assignment Changed";
	public static final String AVL_CONFLICT ="AVL Conflict";
	public static final String PREDICTION_VARIATION = "Prediction variation";
	public static final String UNMATCHED_ASSIGNMENT = "Unmatched Assignment";
	
	// Hibernate requires class to be Serializable
	private static final long serialVersionUID = -763445348557811925L;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(VehicleEvent.class);



	/********************** Member Functions **************************/

	/**
	 * Simple constructor. Declared private because should be only accessed by
	 * the create() method so that can make sure that do things like log each
	 * creation of a VehicleEvent.
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
	private VehicleEvent(Date time, Date avlTime, String vehicleId,
			String eventType, String description, boolean predictable,
			boolean becameUnpredictable, String supervisor, Location location,
			String routeId, String routeShortName, String blockId,
			String serviceId, String tripId, String stopId) {
		super();
		this.time = time;
		this.avlTime = avlTime;
		this.vehicleId = vehicleId;
		this.eventType = eventType;
		this.description = description.length() <= MAX_DESCRIPTION_LENGTH ?
				description : description.substring(0, MAX_DESCRIPTION_LENGTH);
		this.predictable = predictable;
		this.becameUnpredictable = becameUnpredictable;
		this.supervisor = supervisor;
		this.location = location;
		this.routeId = routeId;
		this.routeShortName = routeShortName;
		this.blockId = blockId;
		this.serviceId = serviceId;
		this.tripId = tripId;
		this.stopId = stopId;
	}

	/**
	 * Constructs a vehicle event and logs it and queues it to be stored in
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
	 * @return The VehicleEvent constructed
	 */
	public static VehicleEvent create(Date time, Date avlTime,
			String vehicleId, String eventType, String description,
			boolean predictable, boolean becameUnpredictable,
			String supervisor, Location location, String routeId,
			String routeShortName, String blockId, String serviceId,
			String tripId, String stopId) {
		VehicleEvent vehicleEvent =
				new VehicleEvent(time, avlTime, vehicleId, eventType,
						description, predictable, becameUnpredictable,
						supervisor, location, routeId, routeShortName, blockId,
						serviceId, tripId, stopId);

		// Log VehicleEvent in log file
		logger.info(vehicleEvent.toString());

		// Queue to write object to database
		Core.getInstance().getDbLogger().add(vehicleEvent);

		// Return new VehicleEvent
		return vehicleEvent;
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
	public static VehicleEvent create(AvlReport avlReport, TemporalMatch match,
			String eventType, String description, boolean predictable,
			boolean becameUnpredictable, String supervisor) {
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
				avlReport.getVehicleId(), eventType, description, predictable,
				becameUnpredictable, supervisor, avlReport.getLocation(),
				routeId, routeShortName, blockId, serviceId, tripId, stopId);
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
	protected VehicleEvent() {
		this.time = null;
		this.avlTime = null;
		this.vehicleId = null;
		this.eventType = null;
		this.description = null;
		this.predictable = false;
		this.becameUnpredictable = false;
		this.supervisor = null;
		this.location = null;
		this.routeId = null;
		this.routeShortName = null;
		this.blockId = null;
		this.serviceId = null;
		this.tripId = null;
		this.stopId = null;
	}

	/**
	 * Because using a composite Id Hibernate wants this method.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((avlTime == null) ? 0 : avlTime.hashCode());
		result = prime * result + (becameUnpredictable ? 1231 : 1237);
		result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
		result =
				prime * result
						+ ((description == null) ? 0 : description.hashCode());
		result =
				prime * result
						+ ((eventType == null) ? 0 : eventType.hashCode());
		result =
				prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + (predictable ? 1231 : 1237);
		result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
		result =
				prime
						* result
						+ ((routeShortName == null) ? 0 : routeShortName
								.hashCode());
		result =
				prime * result
						+ ((serviceId == null) ? 0 : serviceId.hashCode());
		result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
		result =
				prime * result
						+ ((supervisor == null) ? 0 : supervisor.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
		result =
				prime * result
						+ ((vehicleId == null) ? 0 : vehicleId.hashCode());
		return result;
	}

	/**
	 * Because using a composite Id Hibernate wants this method.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VehicleEvent other = (VehicleEvent) obj;
		if (avlTime == null) {
			if (other.avlTime != null)
				return false;
		} else if (!avlTime.equals(other.avlTime))
			return false;
		if (becameUnpredictable != other.becameUnpredictable)
			return false;
		if (blockId == null) {
			if (other.blockId != null)
				return false;
		} else if (!blockId.equals(other.blockId))
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
		if (predictable != other.predictable)
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
		if (supervisor == null) {
			if (other.supervisor != null)
				return false;
		} else if (!supervisor.equals(other.supervisor))
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
		return "VehicleEvent [" 
				+ "time=" + Time.dateTimeStrMsec(time)
				+ ", rShortName=" + routeShortName
				+ ", stopId=" + stopId 
				+ ", vehicleId=" + vehicleId
				+ ", eventType=\"" + eventType + "\"" 
				+ ", description=\"" + description + "\""
				+ ", location=" + location 
				+ ", blockId=" + blockId 
				+ ", serviceId=" + serviceId
				+ ", tripId=" + tripId 
				+ ", routeId=" + routeId
				+ ", predictable=" + predictable 
				+ ", becameUnpredictable=" + becameUnpredictable 
				+ ", avlTime=" + Time.dateTimeStrMsec(avlTime)
				+ ", supervisor=" + supervisor
				+ "]";
	}

	/**
	 * Reads in all VehicleEvents from the database that were between the
	 * beginTime and endTime.
	 * 
	 * @param agencyId
	 *            Which project getting data for
	 * @param beginTime
	 *            Specifies time range for query
	 * @param endTime
	 *            Specifies time range for query
	 * @param sqlClause
	 *            Optional. Can specify an SQL clause to winnow down the data,
	 *            such as "AND routeId='71'".
	 * @return
	 */
	public static List<VehicleEvent> getVehicleEvents(String agencyId,
			Date beginTime, Date endTime, String sqlClause) {
		IntervalTimer timer = new IntervalTimer();
		
		// Get the database session. This is supposed to be pretty light weight
		Session session = HibernateUtils.getSession(agencyId);

		// Create the query. Table name is case sensitive and needs to be the
		// class name instead of the name of the db table.
		String hql = "FROM VehicleEvent " +
				"    WHERE time >= :beginDate " +
				"      AND time < :endDate"; 
		if (sqlClause != null)
			hql += " " + sqlClause;
		Query query = session.createQuery(hql);
		
		// Set the parameters
		query.setTimestamp("beginDate", beginTime);
		query.setTimestamp("endDate", endTime);
		
		try {
			@SuppressWarnings("unchecked")
			List<VehicleEvent> vehicleEvents = query.list();
			logger.debug("Getting VehicleEvents from database took {} msec",
					timer.elapsedMsec());
			return vehicleEvents;
		} catch (HibernateException e) {
			logger.error(e.getMessage(), e);
			return null;
		} finally {
			// Clean things up. Not sure if this absolutely needed nor if
			// it might actually be detrimental and slow things down.
			session.close();
		}
	}
	
	/***************** Getter/Setter methods ***************/
	
	/** 
	 * @return the system time of when the event was created.
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * @return the time of the AVL report that generated the event
	 */
	public Date getAvlTime() {
		return avlTime;
	}
	
	public String getVehicleId() {
		return vehicleId;
	}

	public String getEventType() {
		return eventType;
	}

	public String getDescription() {
		return description;
	}

	public boolean isPredictable() {
		return predictable;
	}

	public boolean isBecameUnpredictable() {
		return becameUnpredictable;
	}

	public String getSupervisor() {
		return supervisor;
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

}
