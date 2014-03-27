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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.configData.AvlConfig;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.utils.Geo;
import org.transitime.utils.Time;

import net.jcip.annotations.Immutable;

/**
 * An AvlReport is a GPS report with some additional information, such as 
 * vehicleId. 
 * <p>
 * Serializable since Hibernate requires such.
 *
 * @author SkiBu Smith
 * 
 */
@Immutable // From jcip.annoations
@Entity @DynamicUpdate 
@Table(name="AvlReports") 
@org.hibernate.annotations.Table(appliesTo = "AvlReports", 
                                 indexes = { @Index(name="timeIndex", 
                                                    columnNames={"time"} ) },
                                 foreignKey = @ForeignKey(name="testForeignKey"))
public class AvlReport implements Serializable {
	// vehicleId is an @Id since might get multiple AVL reports
	// for different vehicles with the same time but need a unique
	// primary key.
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) 
	@Id
	private final String vehicleId;
	
	// Need to use columnDefinition to explicitly specify that should use 
	// fractional seconds. This column is an Id since shouldn't get two
	// AVL reports for the same vehicle for the same time.
	@Column(columnDefinition="datetime(3)")	@Temporal(TemporalType.TIMESTAMP)
	@Id
	private final Date time;
	
	// There is a delay between the time an AVL report is first generated
	// till the time it is actually processed. Therefore it is useful to 
	// also keep track of the time it was processed so that can determine
	// latency. Will be null if AV report not yet being processed.
	// Need to use columnDefinition to explicitly specify that should use 
	// fractional seconds.
	@Column(columnDefinition="datetime(3)")	@Temporal(TemporalType.TIMESTAMP)
	private Date timeProcessed;
	
	@Embedded
	private final Location location;
	
	// Speed is an optional element since not always available
	// in an AVL feed. Internally it needs to be a Float and
	// bet set to null when the value is not valid so that it can be stored
	// in the database. This is because Float.NaN doesn't work with JDBC
	// drivers. Externally though, such as when calling the constructor, 
	// should use Float.NaN. It is converted to a null internally.
	@Column
	private final Float speed;   // optional
	
	// Heading is an optional element since not always available
	// in an AVL feed. It is in number of degrees clockwise from 
	// north. Internally it needs to be a Float and
	// bet set to null when the value is not valid so that it can be stored
	// in the database. This is because Float.NaN doesn't work with JDBC
	// drivers. Externally though, such as when calling the constructor, 
	// should use Float.NaN. It is converted to a null internally.
	@Column
	private final Float heading; // optional
	
	// Can be block, trip, or route ID
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String assignmentId;  // optional
	
	public enum AssignmentType {UNSET, BLOCK_ID, ROUTE_ID, TRIP_ID};
	
	@Column(length=40)
	@Enumerated(EnumType.STRING)
	private AssignmentType assignmentType;

	// Optional
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String driverId;
	
	// Optional
	@Column(length=10)
	private String licensePlate;
	
	// Optional. Set to null if passenger count info is not available
	@Column
	private final Integer passengerCount;
	
	// Optional. How full a bus is as a fraction. 0.0=empty, 1.0=at capacity.
	// This parameter is optional. Set to null if data not available.
	@Column
	private final Float passengerFullness;
		
	private static final Logger logger= 
			LoggerFactory.getLogger(AvlReport.class);	

	// Needed because serializable so can transmit using JMS or RMI
	private static final long serialVersionUID = 92384928349823L;
	
	/********************** Member Functions **************************/

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
	protected AvlReport() {
		// Copy the member values
		vehicleId = null;
		time = null;
		location = null;
		speed = null;
		heading = null;
		assignmentId = null;
		assignmentType = AssignmentType.UNSET;
		driverId = null;
		licensePlate = null;
		timeProcessed = null;
		passengerCount = null;
		passengerFullness = null;
	}
	
	/**
	 * Constructor for an AvlReport object that is not yet being processed.
	 * Since not yet being processed timeProcessed is set to null.
	 * 
	 * @param vehicleId
	 * @param time
	 * @param lat
	 * @param lon
	 * @param speed
	 *            should be set to Float.NaN if speed not available
	 * @param heading
	 *            should be set to Float.NaN if speed not available
	 */
	public AvlReport(String vehicleId, long time, double lat, double lon, 
			float speed, float heading) {
		// Store the values
		this.vehicleId = vehicleId;
		this.time = new Date(time);
		this.location = new Location(lat, lon);
		this.speed = Float.isNaN(speed) ? null : speed;
		this.heading = Float.isNaN(heading) ? null : heading;
		this.assignmentId = null;
		this.assignmentType = AssignmentType.UNSET;
		this.driverId = null;
		this.licensePlate = null;
		this.passengerCount = null;
		this.passengerFullness = null;
		
		// Don't yet know when processed so set timeProcessed to null
		this.timeProcessed = null;
	}
	
	/**
	 * Constructor for an AvlReport object that is not yet being processed.
	 * Since not yet being processed timeProcessed is set to null.
	 * 
	 * @param vehicleId
	 * @param time
	 * @param lat
	 * @param lon
	 * @param speed
	 *            should be set to Float.NaN if speed not available
	 * @param heading
	 *            should be set to Float.NaN if speed not available
	 * @param driverId
	 *            Optional value. Set to null if not available.
	 * @param licensePlate
	 *            Optional value. Set to null if not available.
	 * @param passengerCount
	 *            Optional value. Set to the number of passengers on vehicles.
	 *            Set to null if not available.
	 * @param passengerFullness
	 *            Optional Value. Fractional fullness of vehicle. 0.0=empty,
	 *            1.0=full, NaN if data not available.
	 */
	public AvlReport(String vehicleId, long time, double lat, double lon, 
			float speed, float heading, String driverId, String licensePlate,
			Integer passengerCount, float passengerFullness) {
		// Store the values
		this.vehicleId = vehicleId;
		this.time = new Date(time);
		this.location = new Location(lat, lon);
		this.speed = Float.isNaN(speed) ? null : speed;
		this.heading = Float.isNaN(heading) ? null : heading;
		this.assignmentId = null;
		this.assignmentType = AssignmentType.UNSET;
		this.driverId = driverId;
		this.licensePlate = licensePlate;
		this.passengerCount = passengerCount;
		if (!Float.isNaN(passengerFullness))
			this.passengerFullness = passengerFullness;
		else
			this.passengerFullness = null;
		
		// Don't yet know when processed so set timeProcessed to null
		this.timeProcessed = null;
	}

	/**
	 * Constructor for an AvlReport object that is not yet being processed.
	 * Since not yet being processed timeProcessed is set to null. 
	 * 
	 * @param vehicleId
	 * @param time
	 * @param location
	 * @param speed
	 * @param heading
	 */
	public AvlReport(String vehicleId, long time, Location location, 
			float speed, float heading) {
		// Store the values
		this.vehicleId = vehicleId;
		this.time = new Date(time);
		this.location = location;
		this.speed = Float.isNaN(speed) ? null : speed;
		this.heading = Float.isNaN(heading) ? null : heading;
		this.assignmentId = null;
		this.assignmentType = AssignmentType.UNSET;
		this.driverId = null;
		this.licensePlate = null;
		this.passengerCount = null;
		this.passengerFullness = null;
		
		// Don't yet know when processed so set timeProcessed to null
		this.timeProcessed = null;
	}

	/**
	 * Makes a copy of the AvlReport but uses the new time passed in.
	 * Useful for creating a new AvlReport when AVL timeout occurs
	 * when vehicle on a layover.
	 * 
	 * @param toCopy
	 *            The AvlReport to copy (except for the AVL time)
	 * @param newTime
	 *            The AVL time to use
	 */
	public AvlReport(AvlReport toCopy, Date newTime) {
		this.vehicleId = toCopy.vehicleId;
		this.time = newTime;
		this.location = toCopy.location;
		this.speed = toCopy.speed;
		this.heading = toCopy.heading;
		this.assignmentId = toCopy.assignmentId;
		this.assignmentType = toCopy.assignmentType;
		this.driverId = toCopy.driverId;
		this.licensePlate = toCopy.licensePlate;
		this.timeProcessed = toCopy.timeProcessed;
		this.passengerCount = toCopy.passengerCount;
		this.passengerFullness = null;
	}
	
	/**
	 * For when speed and heading are not valid. They are set to Float.NaN .
	 * Since not yet being processed timeProcessed is set to null.

	 * @param vehicleId
	 * @param time
	 * @param lat
	 * @param lon
	 */
	public AvlReport(String vehicleId, long time, double lat, double lon) {
		// Store the values
		this.vehicleId = vehicleId;
		this.time = new Date(time);;
		this.location = new Location(lat, lon);
		this.speed = null;
		this.heading = null;
		this.assignmentId = null;
		this.assignmentType = AssignmentType.UNSET;
		this.driverId = null;
		this.licensePlate = null;
		this.passengerCount = null;
		this.passengerFullness = null;
		
		// Don't yet know when processed so set timeProcessed to null
		this.timeProcessed = null;
	}

	/**
	 * For when speed and heading are not valid. They are set to Float.NaN .
	 * Since not yet being processed timeProcessed is set to null.
	 * 
	 * @param vehicleId
	 * @param time
	 * @param location
	 */
	public AvlReport(String vehicleId, long time, Location location) {
		// Store the values
		this.vehicleId = vehicleId;
		this.time = new Date(time);
		this.location = location;
		this.speed = null;
		this.heading = null;
		this.assignmentId = null;
		this.assignmentType = AssignmentType.UNSET;
		this.driverId = null;
		this.licensePlate = null;
		this.passengerCount = null;
		this.passengerFullness = null;
		
		// Don't yet know when processed so set timeProcessed to null
		timeProcessed = null;
	}

	/**
	 * Makes sure that the members of this class all have reasonable
	 * values. 
	 * @return null if there are no problems. An error message if 
	 * there are problems with the data.
	 */
	public String validateData() {		
		String errorMsg = "";
		
		// Make sure vehicleId is set
		if (vehicleId == null) 
			errorMsg += "VehicleId is null. ";
		else if (vehicleId.length() == 0)
			errorMsg += "VehicleId is empty string. ";
			
		// Make sure GPS time is OK
		long currentTime = System.currentTimeMillis();
		if (time.getTime() < currentTime - 10 * Time.MS_PER_YEAR)
			errorMsg += "Time of " + Time.dateTimeStr(time) + " is more than 10 years old. ";
		if (time.getTime() > currentTime + 1 * Time.MS_PER_MIN) 
			errorMsg += "Time of " + Time.dateTimeStr(time) + " is more than 1 minute into the future. ";
		
		// Make sure lat/lon is OK
		double lat = location.getLat();
		double lon = location.getLon();
		if (lat < AvlConfig.getMinAvlLatitude()) 
			errorMsg += "Latitude of " + lat + " is less than the parameter " + 
					AvlConfig.getMinAvlLatitudeParamName() + " which is set to " + 
					AvlConfig.getMinAvlLatitude();
		if (lat > AvlConfig.getMaxAvlLatitude()) 
			errorMsg += "Latitude of " + lat + " is greater than the parameter " + 
					AvlConfig.getMaxAvlLatitudeParamName() + " which is set to " + 
					AvlConfig.getMaxAvlLatitude();
		if (lon < AvlConfig.getMinAvlLongitude()) 
			errorMsg += "Longitude of " + lon + " is less than the parameter " + 
					AvlConfig.getMinAvlLongitudeParamName() + " which is set to " + 
					AvlConfig.getMinAvlLongitude();
		if (lon > AvlConfig.getMaxAvlLongitude()) 
			errorMsg += "Longitude of " + lon + " is greater than the parameter " + 
					AvlConfig.getMaxAvlLongitudeParamName() + " which is set to " + 
					AvlConfig.getMaxAvlLongitude();
				
		// Make sure speed is OK
		if (isSpeedValid()) {
			if (speed < 0.0f)
				errorMsg += "Speed of " + speed + " is less than zero. ";
			if (speed > AvlConfig.getMaxAvlSpeed()) {
				errorMsg += "Speed of " + speed + 
						"m/s is greater than maximum allowable speed of " + 
						AvlConfig.getMaxAvlSpeed() + "m/s. ";
			}				
		}
		
		// Make sure heading is OK
		if (isHeadingValid()) {
			if (heading < 0.0f)
				errorMsg += "Heading of " + heading + " degrees is less than 0.0 degrees. ";
			if (heading > 360.0f)
				errorMsg += "Heading of " + heading + 
						" degrees is greater than 360.0 degrees. ";			
		}
		
		
		// Return the error message if any
		if (errorMsg.length() > 0)
			return errorMsg;
		else 
			return null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((assignmentId == null) ? 0 : assignmentId.hashCode());
		result = prime * result
				+ ((assignmentType == null) ? 0 : assignmentType.hashCode());
		result = prime * result
				+ ((driverId == null) ? 0 : driverId.hashCode());
		result = prime * result + ((heading == null) ? 0 : heading.hashCode());
		result = prime * result
				+ ((licensePlate == null) ? 0 : licensePlate.hashCode());
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((speed == null) ? 0 : speed.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result
				+ ((timeProcessed == null) ? 0 : timeProcessed.hashCode());
		result = prime * result
				+ ((vehicleId == null) ? 0 : vehicleId.hashCode());
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
		AvlReport other = (AvlReport) obj;
		if (assignmentId == null) {
			if (other.assignmentId != null)
				return false;
		} else if (!assignmentId.equals(other.assignmentId))
			return false;
		if (assignmentType != other.assignmentType)
			return false;
		if (driverId == null) {
			if (other.driverId != null)
				return false;
		} else if (!driverId.equals(other.driverId))
			return false;
		if (heading == null) {
			if (other.heading != null)
				return false;
		} else if (!heading.equals(other.heading))
			return false;
		if (licensePlate == null) {
			if (other.licensePlate != null)
				return false;
		} else if (!licensePlate.equals(other.licensePlate))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (passengerCount == null) {
			if (other.passengerCount != null)
				return false;
		} else if (!passengerCount.equals(other.passengerCount))
			return false;
		if (passengerFullness == null) {
			if (other.passengerFullness != null)
				return false;
		} else if (!passengerFullness.equals(other.passengerFullness))
			return false;
		if (speed == null) {
			if (other.speed != null)
				return false;
		} else if (!speed.equals(other.speed))
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		if (timeProcessed == null) {
			if (other.timeProcessed != null)
				return false;
		} else if (!timeProcessed.equals(other.timeProcessed))
			return false;
		if (vehicleId == null) {
			if (other.vehicleId != null)
				return false;
		} else if (!vehicleId.equals(other.vehicleId))
			return false;
		return true;
	}

	public String getVehicleId() {
		return vehicleId;
	}
	
	public long getTime() {
		return time.getTime();
	}
	
	/**
	 * @return A Date object containing the GPS time
	 */
	public Date getDate() {
		return time;
	}
	
	public long getTimeProcessed() {
		return timeProcessed.getTime();
	}
	
	public Location getLocation() {
		return location;
	}
	
	public double getLat() {
		return location.getLat();
	}
	
	public double getLon() {
		return location.getLon();
	}
	
	/**
	 * @return Speed of vehicle in meters per second.
	 */
	public float getSpeed() {
		return speed==null? Float.NaN : speed;
	}
	
	/**
	 * Heading of vehicles in degrees. The heading can sometimes
	 * be invalid. Though internally an invalid heading is stored
	 * as null it is returned by this method as NaN so that can
	 * return a float. If speed is below 
	 * AvlConfig.minSpeedForValidHeading() then will also return
	 * NaN.
	 * 
	 * @return Heading of vehicles in degrees. If heading not set
	 * or if speed below minimum then Float.NaN is returned.
	 */
	public float getHeading() {
		// If heading not available then return NaN
		if (heading == null)
			return Float.NaN;

		// The heading is valid. If there is a valid speed available and
		// but  it is not high enough to make the heading valid  
		// then return NaN.
		if (speed != null && speed < AvlConfig.minSpeedForValidHeading()) {
			return Float.NaN;
		} else {
			// Heading is valid so return it
			return heading;
		}
	}
	
	/**
	 * Returns how many msec elapsed between the GPS fix was generated
	 * to the time it was finally processed. Returns 0 if timeProcessed
	 * was never set.
	 * @return 
	 */
	public long getLatency() {
		// If never processed then return 0.
		if (timeProcessed == null)
			return 0;
		
		return timeProcessed.getTime() - time.getTime();
	}
	
	/**
	 * For some AVL systems speed is not available and therefore cannot be used.  
	 * @return
	 */
	public boolean isSpeedValid() {
		return speed != null;
	}
	
	/**
	 * For some AVL systems heading is not available and therefore cannot be used.  
	 * @return
	 */
	public boolean isHeadingValid() {
		return heading != null;
	}

	public String getAssignmentId() {
		return assignmentId;
	}
	
	public AssignmentType getAssignmentType() {
		return assignmentType;
	}
	
	private static boolean unpredictableAssignmentsPatternInitialized = false;
	private static Pattern regExPattern = null;
	
	/**
	 * Returns true if the assignment specified matches the regular expression
	 * for unpredictable assignments.
	 * 
	 * @param assignment
	 * @return true if assignment matches regular expression
	 */
	public static boolean matchesUnpredictableAssignment(String assignment) {
		if (unpredictableAssignmentsPatternInitialized == false) {
			String regEx = AvlConfig.getUnpredictableAssignmentsRegEx();
			if (regEx != null) {
				regExPattern = Pattern.compile(regEx);
			}
			unpredictableAssignmentsPatternInitialized = true;
		}
		
		if (regExPattern == null)
			return false;
		
		return regExPattern.matcher(assignment).matches();
	}
	
	/**
	 * Returns whether assignment information was set in the AVL data and that
	 * assignment is valid. An assignment is not valid if it is configured to be
	 * invalid. Examples of such include training vehicles, support vehicles,
	 * and simply vehicles set to a special assignment such as 9999 for sf-muni.
	 * 
	 * @return true if has assignment and it is valid. Otherwise false.
	 */
	public boolean hasValidAssignment() {       
		if (assignmentType != AssignmentType.UNSET && 
				matchesUnpredictableAssignment(assignmentId))
			logger.debug("For vehicleId={} was assigned to \"{}\" but that " +
					"assignment is not considered valid due to " +
					"transitime.avl.unpredictableAssignmentsRegEx being set " +
					"to \"{}\"", 
					vehicleId, assignmentId, 
					AvlConfig.getUnpredictableAssignmentsRegEx());
		
		return assignmentType != AssignmentType.UNSET && 
				!matchesUnpredictableAssignment(assignmentId);
	}
	
	public void setAssignment(String assignmentId, AssignmentType assignmentType) {
		// Make sure don't set to invalid values
		if (assignmentId == null && assignmentType != AssignmentType.UNSET) {
			logger.error("Tried to use setAssignment() to set assignment to " +
					"null with setting assignmentType to UNSET");
			return;
		}
		
		this.assignmentId = assignmentId;
		this.assignmentType = assignmentType;
	}
	
	public String getDriverId() {
		return driverId;
	}
	
	public void setDriverId(String driverId) {
		this.driverId = driverId;
	}
	
	public String getLicensePlate() {
		return licensePlate;
	}
	
	public void setLicensePlate(String licensePlate) {
		this.licensePlate = licensePlate;
	}
	
	/**
	 * Returns the passenger count, as obtained from AVL feed.
	 * If passenger count not available returns -1.
	 * @return
	 */
	public int getPassengerCount() {
		if (passengerCount != null)
			return passengerCount;
		else
			return -1;
	}
	
	/**
	 * Returns whether the passenger count is valid.
	 * 
	 * @return true if count is valid.
	 */
	public boolean isPassengerCountValid() {
		return passengerCount != null;
	}
	
	/**
	 * Fraction indicating how full a vehicle is. Returns NaN if info not 
	 * available.
	 * 
	 * @return
	 */
	public float getPassengerFullness() {
		if (passengerFullness != null)
			return passengerFullness;
		else
			return Float.NaN;
	}
	
	/**
	 * Returns whether the passenger fullness is valid.
	 * @return true if passenger fullness is valid.
	 */
	public boolean isPassengerFullnessValid() {
		return passengerFullness != null;
	}
	
	/**
	 * Updates the object to record the current time as the time that
	 * the data was actually processed.
	 */
	public void setTimeProcessed() {
		timeProcessed = new Date(System.currentTimeMillis());
	}
	
	@Override
	public String toString() {
		return "AvlReport [" +
				"vehicleId=" + vehicleId +
				", time=" + Time.dateTimeStrMsec(time) +
				", timeProcessed=" + 
					(timeProcessed==null? null : Time.dateTimeStrMsec(timeProcessed)) +
				", location=" + location +
				", speed=" + Geo.speedFormat(getSpeed()) +
				", heading=" + Geo.headingFormat(getHeading()) + 
				", assignmentId=" + assignmentId +
				", assignmentType=" + assignmentType +
				(driverId==null? "" : ", driverId=" + driverId) +
				(licensePlate==null? "" : ", licensePlate=" + licensePlate) +	
				(passengerCount==null? "" : ", passengerCount=" + passengerCount) +
				(passengerFullness==null? "" : ", passengerFullness=" + passengerFullness) +
				"]";
	}

	/**
	 * Gets list of AvlReports from database for the time span specified.
	 * 
	 * @param projectId
	 *            Specifies which db to get data from
	 * @param beginTime
	 * @param endTime
	 * @param vehicleId
	 *            Optional. If not null then will only return results for that
	 *            vehicle
	 * @return
	 */
	public static List<AvlReport> getAvlReportsFromDb(String projectId,
			Date beginTime, 
			Date endTime, 
			String vehicleId) {
		// Sessions are not threadsafe so need to create a new one each time.
		// They are supposed to be lightweight so this should be OK.
		SessionFactory sessionFactory = 
				HibernateUtils.getSessionFactory(projectId);
		Session session = sessionFactory.openSession();
		
		// Create the query. Table name is case sensitive!
		String hql = "FROM AvlReport " +
				"    WHERE time >= :beginDate " +
				"      AND time < :endDate"; 
		if (vehicleId != null)
			hql += " AND vehicleId=:vehicleId";
		Query query = session.createQuery(hql);
		
		// Set the parameters
		if (vehicleId != null)
			query.setString("vehicleId", vehicleId);
		query.setTimestamp("beginDate", beginTime);
		query.setTimestamp("endDate", endTime);
		
		try {
			@SuppressWarnings("unchecked")
			List<AvlReport> avlReports = query.list();
			return avlReports;
		} catch (HibernateException e) {
			// Log error to the Core logger
			Core.getLogger().error(e.getMessage(), e);
			return null;
		} finally {
			// Clean things up. Not sure if this absolutely needed nor if
			// it might actually be detrimental and slow things down.
			session.close();
		}
	}

}
