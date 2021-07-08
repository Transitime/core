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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.gtfs.gtfsStructs.GtfsFrequency;


/**
 * Contains data from the frequencies.txt GTFS file. This class is
 * for reading/writing that data to the db.
 * 
 * @author SkiBu Smith
 *
 */
@Entity @DynamicUpdate @Table(name="Frequencies")
public class Frequency implements Serializable {

	@Column 
	@Id 
	private final int configRev;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	@Id 
	private final String tripId;
	
	@Column
	@Id 
	private final int startTime;
	
	@Column
	private final int endTime;
	
	@Column
	private final int headwaySecs;
	
	@Column
	private final boolean exactTimes;

	// Because Hibernate requires objects with composite Ids to be Serializable
	private static final long serialVersionUID = 3616161947931923670L;

	/********************** Member Functions **************************/

	/**
	 * Constructor
	 * 
	 * @param configRev
	 * @param gtfsFrequency
	 */
	public Frequency(int configRev, GtfsFrequency gtfsFrequency) {
		this.configRev = configRev;
		this.tripId = gtfsFrequency.getTripId();
		this.startTime = gtfsFrequency.getStartTime();
		this.endTime = gtfsFrequency.getEndTime();
		this.headwaySecs = gtfsFrequency.getHeadwaySecs();
		this.exactTimes = (gtfsFrequency.getExactTimes() != null ?  gtfsFrequency.getExactTimes() : false); 
	}
	
	/**
	 * Needed because Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private Frequency() {
		configRev = -1;
		tripId = null;
		startTime = -1;
		endTime = -1;
		headwaySecs = -1;
		exactTimes = false;
	}
	
	/**
	 * Deletes rev from the Frequencies table
	 * 
	 * @param session
	 * @param configRev
	 * @return Number of rows deleted
	 * @throws HibernateException
	 */
	public static int deleteFromRev(Session session, int configRev) 
			throws HibernateException {
		// Note that hql uses class name, not the table name
		String hql = "DELETE Frequency WHERE configRev=" + configRev;
		int numUpdates = session.createQuery(hql).executeUpdate();
		return numUpdates;
	}
	
	/**
	 * Returns List of Frequency objects for the specified database revision.
	 * 
	 * @param session
	 * @param configRev
	 * @return
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<Frequency> getFrequencies(Session session, int configRev) 
			throws HibernateException {
		String hql = "FROM Frequency " +
				"    WHERE configRev = :configRev";
		Query query = session.createQuery(hql);
		query.setInteger("configRev", configRev);
		return query.list();
	}

	/**
	 * Needed because have a composite ID for Hibernate storage
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + configRev;
		result = prime * result + endTime;
		result = prime * result + (exactTimes ? 1231 : 1237);
		result = prime * result + headwaySecs;
		result = prime * result + startTime;
		result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
		return result;
	}

	/**
	 * Needed because have a composite ID for Hibernate storage
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Frequency other = (Frequency) obj;
		if (configRev != other.configRev)
			return false;
		if (endTime != other.endTime)
			return false;
		if (exactTimes != other.exactTimes)
			return false;
		if (headwaySecs != other.headwaySecs)
			return false;
		if (startTime != other.startTime)
			return false;
		if (tripId == null) {
			if (other.tripId != null)
				return false;
		} else if (!tripId.equals(other.tripId))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Frequency [configRev=" + configRev 
				+ ", tripId=" + tripId
				+ ", startTime=" + startTime 
				+ ", endTime=" + endTime
				+ ", headwaySecs=" + headwaySecs 
				+ ", exactTimes=" + exactTimes 
				+ "]";
	}
	
	/********************** Getter Methods *****************************/

	/**
	 * @return the configRev
	 */
	public int getConfigRev() {
		return configRev;
	}

	/**
	 * @return the tripId
	 */
	public String getTripId() {
		return tripId;
	}

	/**
	 * @return the startTime
	 */
	public int getStartTime() {
		return startTime;
	}

	/**
	 * @return the endTime
	 */
	public int getEndTime() {
		return endTime;
	}

	/**
	 * @return the headwaySecs
	 */
	public int getHeadwaySecs() {
		return headwaySecs;
	}

	/**
	 * The exact_times field determines if frequency-based trips should be
	 * exactly scheduled based on the specified headway information. Valid
	 * values for this field are:
	 * 
	 * <ul>
	 * <li>0 or (empty) - Frequency-based trips are not exactly scheduled. This
	 * is the default behavior. 1 - Frequency-based trips are exactly scheduled.
	 * For a frequencies.txt row, trips are scheduled starting with
	 * trip_start_time = start_time + x * headway_secs for all x in (0, 1, 2,
	 * ...) where trip_start_time < end_time.</li>
	 * <li>The value of exact_times must be the same for all frequencies.txt
	 * rows with the same trip_id. If exact_times is 1 and a frequencies.txt row
	 * has a start_time equal to end_time, no trip must be scheduled. When
	 * exact_times is 1, care must be taken to choose an end_time value that is
	 * greater than the last desired trip start time but less than the last
	 * desired trip start time + headway_secs.</li>
	 * </ul>
	 * 
	 * @return the exactTimes
	 */
	public boolean getExactTimes() {
		return exactTimes;
	}
	
}
