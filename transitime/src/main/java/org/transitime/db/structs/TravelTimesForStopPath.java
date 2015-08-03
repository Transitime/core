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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.utils.Geo;


/**
 * Contains the expected time it takes to travel along the specified path, which
 * is for one stop to another. There can be a different list of
 * TravelTimesForStopPath for each trip. The idea is to share travel times when
 * possible, when they are relatively the same for a trip pattern. But if a trip
 * needs separate travel times then it can have it.
 * 
 * @author SkiBu Smith
 * 
 */
@Entity 
@DynamicUpdate
@Table(name="TravelTimesForStopPaths")
public class TravelTimesForStopPath implements Serializable {

	// Need a generated ID because trying to share TravelTimesForStopPath objects because
	// having a separate set for each trip would be too much. But will usually
	// still have a few per path and trip pattern. Therefore also need the
	// generated ID.
	@Id 
	@Column 
	@GeneratedValue 
	private Integer id;
	
	// Need configRev for the configuration so that when old configurations 
	// cleaned out can also get rid of old travel times.
	@Column
	private final int configRev;
	
	// Each time update travel times it gets a new travel time rev. This
	// way can compare travel times with previous revisions. Probably only need
	// to keep the previous travel time rev around for comparison but by
	// using an integer for the rev all of the revs can be kept in the db 
	// if desired.
	@Column
	private final int travelTimesRev;
	
	// Which stop on the trip the travel times are for. Using size of
	// 2 * DEFAULT_ID_SIZE since stop path names are stop1_to_stop2 so can
	// be twice as long as other IDs. And when using GTFS Editor the IDs
	// are quite long, a bit longer than 40 characters.
	@Column(length=2*HibernateUtils.DEFAULT_ID_SIZE)
	private final String stopPathId;
	
	// The distance for each travel time segment for this path. Doesn't 
	// need to be precise so use float instead of double to save memory.
	@Column
	private final float travelTimeSegmentLength;
	
	// Travel time is a List of Integers containing the expected travel time
	// for each travel time segment whose length is travelTimeSegmentLength. 
	// Integers are used to make it take
	// less space and processing than if longs were used. Values in milliseconds.
	// There are two ways to deal with Lists of basic types in Hibernate. The
	// normal way is to declare it an @ElementCollection (and use @OrderColumn
	// to maintain order of data in list) but this causes the list data to be
	// stored in a separate table, one having the same primary keys. When 
	// reading data need to do a join of the tables. When doing a write
	// many SQL statements are needed (one for each element in the List).
	// This is all very cumbersome, slow, and space inefficient since
	// storing the primary keys again for each row in the additional table.
	// The other way to deal with such a list is to have it be serializable
	// and specify (length=1000) in the @Column annotation. In this way the
	// List data is simply serialized/unserialized into a BLOB. This means that
	// don't have a separate table with a separate row for each item in the 
	// list. The drawback is that since the data is in a blob it cannot be
	// read directly using SQL on the command line or for reports. But since
	// it can make things so much more efficient want to try using it.
	// NOTE: since trying to use serialization need to use ArrayList<> instead
	// of List<> since List<> doesn't implement Serializable.
	private static final int travelTimesMaxBytes = 100000;
	@Column(length=travelTimesMaxBytes)
	private final ArrayList<Integer> travelTimesMsec;

	// There is a separate time for travel and for actually stopping. For
	// many systems might not be able to really differentiate between the two
	// but if can then can make more accurate predictions. The stopTimeMsec
	// can also be used at beginning of trips to determine when buses really
	// do leave the terminus. In this way if a driver always leaves a couple
	// minutes late then the predictions will be adjusted accordingly.
	@Column
	private final int stopTimeMsec;
	
	// For somehow overriding times for a particular day of the week.
	// For example, could have a serviceId that represents weekdays
	// for which the same service is provided. But might want to have
	// different travel times for Fridays since afternoon rush hour is 
	// definitely different for Fridays. 
	@Column
	private final short daysOfWeekOverride;

	// For keeping track of how the data was obtained (historic GPS,
	// schedule, default speed, etc)
	@Column(length=5)
	@Enumerated(EnumType.STRING)
	private final HowSet howSet;
	
	// Needed because class is serializable
	private static final long serialVersionUID = -5136757109373446841L;

	private static final Logger logger = 
			LoggerFactory.getLogger(TravelTimesForStopPath.class);

	/**
	 * This enumeration is for keeping track of how the travel times were  
	 * determined. This way can tell of they should be overridden or not.  
	 */
	public enum HowSet {
		// From when there are no schedule times so simply need to use a
		// default speed
		SPEED(0),

		// From interpolating data in GTFS stop_times.txt file
		SCHED(1),
		
		// No AVL data was available for the actual day so using data from
		// another day.
		SERVC(2),
	
		// No AVL data was available for the actual trip so using data from
		// a trip that is before or after the trip in question
		TRIP(3),
		
		// Based on actual running times as determined by AVL data
		AVL(4);
		
		@SuppressWarnings("unused")
		private int value;
		
		private HowSet(int value) {
			this.value =  value;
		}
		
		public boolean isScheduleBased() {
			return this == SPEED || 
					this == SCHED;
		}
	};
	
	/********************** Member Functions **************************/

	/**
	 * Constructs a new TravelTimesForStopPath object.
	 * 
	 * @param configRev
	 * @param travelTimesRev
	 * @param stopPathId
	 * @param travelTimeSegmentDistance
	 * @param travelTimesMsec
	 *            The travel times for the travel time segments.
	 * @param stopTimeMsec
	 * @param howSet
	 * @param daysOfWeekOverride
	 * @param trip for logging useful error message. OK if null.
	 * @throws ArrayIndexOutOfBoundsException
	 *             Thrown if not enough memory allocated for column
	 *             travelTimesMsec for serializing the object.
	 */
	public TravelTimesForStopPath(int configRev, int travelTimesRev,
			String stopPathId, double travelTimeSegmentDistance,
			List<Integer> travelTimesMsec, int stopTimeMsec,
			int daysOfWeekOverride, HowSet howSet, Trip trip) 
					throws ArrayIndexOutOfBoundsException {
		// First make sure that travelTimesMsec isn't bigger than
		// the space allocated for it. Only bother checking if have
		// at least a few travel times for the path.
		if (travelTimesMsec.size() > 5) {
			int serializedSize = HibernateUtils.sizeof(travelTimesMsec);
			if (serializedSize > travelTimesMaxBytes) {
				String msg = "Too many elements in "
						+ "travelTimesMsec when constructing a "
						+ "TravelTimesForStopPath for stopPathId=" + stopPathId 
						+ " and travelTimeSegmentDistance=" 
						+ Geo.distanceFormat(travelTimeSegmentDistance)
						+ " . Have " + travelTimesMsec.size()
						+ " travel time segments taking up " + serializedSize 
						+ " bytes but only have " + travelTimesMaxBytes 
						+ " bytes allocated for the data. TripId=" 
						+ (trip!=null ? trip.getId() : "") 
						+ " routeId=" + (trip!=null ? trip.getRouteId() : "")
						+ " routeShortName=" 
						+ (trip!=null ? trip.getRouteShortName() : "")
						+ ". You most likely need to set the "
						+ "-maxTravelTimeSegmentLength command line option to "
						+ "a larger value than than the default of 200m.";
				logger.error(msg);
				
				// Since this could be a really problematic issue, throw an error
				throw new ArrayIndexOutOfBoundsException(msg);
			}
		}
		
		this.configRev = configRev;
		this.travelTimesRev = travelTimesRev;
		this.stopPathId = stopPathId;
		this.travelTimeSegmentLength = (float) travelTimeSegmentDistance;		
		this.travelTimesMsec = (ArrayList<Integer>) travelTimesMsec;
		this.stopTimeMsec = stopTimeMsec;
		this.daysOfWeekOverride = (short) daysOfWeekOverride;
		this.howSet = howSet;
	}
	
	/**
	 * Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private TravelTimesForStopPath() {
		this.configRev = -1;
		this.travelTimesRev = -1;
		this.stopPathId = null;
		this.travelTimeSegmentLength = Float.NaN;
		this.travelTimesMsec = null;
		this.stopTimeMsec = -1;
		this.daysOfWeekOverride = -1;
		this.howSet = HowSet.SCHED;
	}
	
	/**
	 * Creates a new object. Useful for when need to copy a schedule based
	 * travel time. By having a copy can erase the original one when done with
	 * the travel time rev, without deleting this new one.
	 * 
	 * @param newTravelTimesRev
	 *            The new travel times rev to use for the clone
	 * @return
	 */
	public TravelTimesForStopPath clone(int newTravelTimesRev) {
		return new TravelTimesForStopPath(configRev, newTravelTimesRev,
				stopPathId, travelTimeSegmentLength, travelTimesMsec,
				stopTimeMsec, daysOfWeekOverride, howSet, null);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TravelTimesForStopPath [" 
				+ "id=" + id
				+ ", configRev=" + configRev
				+ ", travelTimesRev=" + travelTimesRev
				+ ", stopPathId=" + stopPathId 
				+ ", travelTimeSegmentLength=" + travelTimeSegmentLength
				+ ", travelTimesMsec=" + travelTimesMsec 
				+ ", stopTimeMsec=" + stopTimeMsec
				+ ", travelTimeMsec=" + getStopPathTravelTimeMsec()
				+ ", daysOfWeekOverride=" + daysOfWeekOverride
				+ ", howSet=" + howSet 
				+ "]";
	}
	
	/**
	 * For when the travelTimesMsec are most important element. Lists the
	 * travelTimesMsec first.
	 * 
	 * @return
	 */
	public String toStringEmphasizeTravelTimes() {
		return "TTForStopPath ["
				+ "stopTimeMsec=" + stopTimeMsec
				+ ", travelTimeMsec=" + getStopPathTravelTimeMsec()
				+ ", travelTimesMsec=" + travelTimesMsec 
				+ ", stopPathId=" + stopPathId
				+ ", ttSegLen=" + Geo.distanceFormat(travelTimeSegmentLength)
				+ ", howSet=" + howSet 
				+ ", ttRev=" + travelTimesRev
				+ "]"; 
	}

	/************************ Getter Methods *************************/	

	public int getConfigRev() {
		return configRev;		
	}
	
	public int getTravelTimesRev() {
		return travelTimesRev;
	}
	
	/**
	 * @return the stopPathId
	 */
	public String getStopPathId() {
		return stopPathId;
	}

	/**
	 * The travel time segment distance specifies how the stop path is divided
	 * up with respect to travel times. The travel times for a stop path are
	 * uniformly divided. This means that each travel time segment for a stop
	 * path has the same length.
	 * 
	 * @return the travelTimeSegmentLength
	 */
	public double getTravelTimeSegmentLength() {
		return travelTimeSegmentLength;
	}
	
	/**
	 * @return the travelTimeMsec
	 */
	public List<Integer> getTravelTimesMsec() {
		return travelTimesMsec;
	}

	/**
	 * @return How many travel time segments there are for the stop path
	 */
	public int getNumberTravelTimeSegments() {
		return travelTimesMsec.size();
	}
	
	/**
     * Returns total travel time for the stop path. Does not include the stop
     * time.
     *
	 * @return total travel time for the stop path in msec
	 */
	public int getStopPathTravelTimeMsec() {
		int totalTravelTimeMsec = 0;
		for (Integer timeMsec : travelTimesMsec)
			totalTravelTimeMsec += timeMsec;
		return totalTravelTimeMsec;
	}
	
	/**
	 * Returns the travel time for the specified travel time segment in msec
	 * 
	 * @param segmentIndex
	 * @return travel time for the specified travel time segment in msec
	 */
	public int getTravelTimeSegmentMsec(int segmentIndex) {
		return travelTimesMsec.get(segmentIndex);
	}
	
	/**
	 * How long the vehicle is expected to dwell at stop. Doesn't include
	 * layover times and such. Based on historic AVL data.
	 * 
	 * @return the stopTimeMsec
	 */
	public int getStopTimeMsec() {
		return stopTimeMsec;
	}

	/**
	 * @return the daysOfWeekOverride
	 */
	public int getDaysOfWeekOverride() {
		return daysOfWeekOverride;
	}

	/**
	 * @return the howSet
	 */
	public HowSet getHowSet() {
		return howSet;
	}
	
	/************************* Database Methods *************************/

	/**
	 * Reads in all the travel times for the specified rev
	 * 
	 * @param sessionFactory
	 * @param configRev
	 * @return
	 */
	public static List<TravelTimesForStopPath> getTravelTimes(SessionFactory sessionFactory, 
			int configRev) {
		// Sessions are not threadsafe so need to create a new one each time.
		// They are supposed to be lightweight so this should be OK.
		Session session = sessionFactory.openSession();
		
		// Create the query. Table name is case sensitive!
		String hql = "FROM TravelTimesForStopPath " +
				"    WHERE configRev=:configRev "; 
		Query query = session.createQuery(hql);
		
		// Set the parameters
		query.setInteger("configRev", configRev);
		
		try {
			@SuppressWarnings("unchecked")
			List<TravelTimesForStopPath> travelTimes = query.list();
			return travelTimes;
		} catch (HibernateException e) {
			// Log error to the Core logger
			logger.error(e.getMessage(), e);
			return null;
		} finally {
			// Clean things up. Not sure if this absolutely needed nor if
			// it might actually be detrimental and slow things down.
			session.close();
		}
	}

	/**
	 * Defined so can use as key in map
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + configRev;
		result = prime * result + daysOfWeekOverride;
		result = prime * result + ((howSet == null) ? 0 : howSet.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((stopPathId == null) ? 0 : stopPathId.hashCode());
		result = prime * result + stopTimeMsec;
		result = prime * result + Float.floatToIntBits(travelTimeSegmentLength);
		result = prime * result
				+ ((travelTimesMsec == null) ? 0 : travelTimesMsec.hashCode());
		result = prime * result + travelTimesRev;
		return result;
	}

	/**
	 * Defined so can use as key in map
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TravelTimesForStopPath other = (TravelTimesForStopPath) obj;
		if (configRev != other.configRev)
			return false;
		if (daysOfWeekOverride != other.daysOfWeekOverride)
			return false;
		if (howSet != other.howSet)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (stopPathId == null) {
			if (other.stopPathId != null)
				return false;
		} else if (!stopPathId.equals(other.stopPathId))
			return false;
		if (stopTimeMsec != other.stopTimeMsec)
			return false;
		if (Float.floatToIntBits(travelTimeSegmentLength) != Float
				.floatToIntBits(other.travelTimeSegmentLength))
			return false;
		if (travelTimesMsec == null) {
			if (other.travelTimesMsec != null)
				return false;
		} else if (!travelTimesMsec.equals(other.travelTimesMsec))
			return false;
		if (travelTimesRev != other.travelTimesRev)
			return false;
		return true;
	}
	
}
