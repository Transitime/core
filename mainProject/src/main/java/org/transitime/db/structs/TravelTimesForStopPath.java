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

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.DynamicUpdate;
import org.transitime.applications.Core;
import org.transitime.db.hibernate.HibernateUtils;


/**
 * Contains the expected time it takes to travel along the specified
 * path, which is for one stop to another. There can be a different
 * list of TravelTimesForStopPath for each trip. The idea is to share travel times 
 * when possible, when they are relatively the same for a trip pattern.
 * But if a trip needs separate travel times then it can have it.
 * 
 * @author SkiBu Smith
 * 
 */
@Entity @DynamicUpdate
public class TravelTimesForStopPath implements Serializable {

	// Need a generated ID because trying to share TravelTimesForStopPath objects because
	// having a separate set for each trip would be too much. But will usually
	// still have a few per path and trip pattern. Therefore also need the
	// generated ID.
	@Id 
	@Column 
	@GeneratedValue 
	private Integer id;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String stopPathId;
	
	// The distance for each travel time segment for this path
	@Column
	private final double travelTimeSegmentLength;
	
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
	@Column(length=1000)// @ElementCollection @OrderColumn
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
	private final int daysOfWeekOverride;

	// For keeping track of how the data was obtained (historic GPS,
	// schedule, default speed, etc)
	@Column(length=17)
	@Enumerated(EnumType.STRING)
	private final HowSet howSet;
	
	// Needed because class is serializable
	private static final long serialVersionUID = -5136757109373446841L;

	/**
	 * This enumeration is for keeping track of how the travel times were  
	 * determined. This way can tell of they should be overridden or not.  
	 */
	public enum HowSet {
		// From when there are no schedule times so simply need to use a
		// default speed
		DEFAULT_SPEED(0),

		// From interpolating data in GTFS stop_times.txt file
		SCHEDULE_TIMES(1),
		
		// No AVL data was available for the actual day so using data from
		// another day.
		AVL_OTHER_SERVICE(2),
	
		// No AVL data was available for the actual trip so using data from
		// a trip that is before or after the trip in question
		AVL_OTHER_TRIP(3),
		
		// Based on actual running times as determined by AVL data
		AVL_DATA(4);
		
		@SuppressWarnings("unused")
		private int value;
		
		private HowSet(int value) {
			this.value =  value;
		}
		
		public boolean isScheduleBased() {
			return this == DEFAULT_SPEED || 
					this == SCHEDULE_TIMES;
		}
	};
	
	/********************** Member Functions **************************/

	/**
	 * Constructs a new TravelTimesForStopPath object.
	 * 
	 * @param stopPathId
	 * @param travelTimesMsec
	 *            The travel times for the travel time segments. 
	 * @param stopTimeMsec
	 * @param howSet
	 * @param daysOfWeekOverride
	 */
	public TravelTimesForStopPath(String stopPathId,
			double travelTimeSegmentDistance,
			List<Integer> travelTimesMsec, int stopTimeMsec,
			int daysOfWeekOverride, HowSet howSet) {
		this.stopPathId = stopPathId;
		this.travelTimeSegmentLength = travelTimeSegmentDistance;
		this.travelTimesMsec = (ArrayList<Integer>) travelTimesMsec;
		this.stopTimeMsec = stopTimeMsec;
		this.daysOfWeekOverride = daysOfWeekOverride;
		this.howSet = howSet;
	}
	
	/**
	 * Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private TravelTimesForStopPath() {
		this.stopPathId = null;
		this.travelTimeSegmentLength = Double.NaN;
		this.travelTimesMsec = null;
		this.stopTimeMsec = -1;
		this.daysOfWeekOverride = -1;
		this.howSet = HowSet.SCHEDULE_TIMES;
	}
	
	/**
	 * Creates a new object. Useful for when need to copy a schedule based
	 * travel time. By having a copy can erase the original one when done with
	 * the rev, without deleting this new one.
	 * 
	 * @param original
	 * @return
	 */
	public TravelTimesForStopPath clone() {
		return new TravelTimesForStopPath(stopPathId,
				travelTimeSegmentLength, travelTimesMsec,
				stopTimeMsec, daysOfWeekOverride, howSet);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TravelTimesForStopPath [" 
				+ "id=" + id 
				+ ", stopPathId=" + stopPathId 
				+ ", travelTimeSegmentLength=" + travelTimeSegmentLength
				+ ", travelTimesMsec=" + travelTimesMsec 
				+ ", stopTimeMsec=" + stopTimeMsec
				+ ", daysOfWeekOverride=" + daysOfWeekOverride
				+ ", howSet=" + howSet 
				+ "]";
	}

	/************************ Getter Methods *************************/	

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
			Core.getLogger().error(e.getMessage(), e);
			return null;
		} finally {
			// Clean things up. Not sure if this absolutely needed nor if
			// it might actually be detrimental and slow things down.
			session.close();
		}
	}
	
}
