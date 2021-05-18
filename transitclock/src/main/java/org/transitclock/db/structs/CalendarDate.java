/**
 * 
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
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.gtfs.gtfsStructs.GtfsCalendarDate;

/**
 * Contains data from the calendardates.txt GTFS file. This class is
 * for reading/writing that data to the db.
 *
 * @author SkiBu Smith
 *
 */
@Entity @DynamicUpdate @Table(name="CalendarDates")
public class CalendarDate implements Serializable{

	@Column @Id
	private final int configRev;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) @Id
	private final String serviceId;
	
	@Column @Id @Temporal(TemporalType.DATE)
	private final Date date;
	
	@Column(length=2)
	private final String exceptionType;

	// Logging
	public static final Logger logger = 
			LoggerFactory.getLogger(CalendarDate.class);

	// Because Hibernate requires objects with composite IDs to be Serializable
	private static final long serialVersionUID = -4825360997804688749L;

	/********************** Member Functions **************************/

	/**
	 * Constructor
	 * 
	 * @param configRev
	 * @param gtfsCalendarDate
	 * @param dateFormat
	 */
	public CalendarDate(int configRev, GtfsCalendarDate gtfsCalendarDate, 
			DateFormat dateFormat) {
		this.configRev = configRev;
		this.serviceId = gtfsCalendarDate.getServiceId();
		
		// Dealing with date is complicated because must parse
		Date tempDate;
		try {
			tempDate = dateFormat.parse(gtfsCalendarDate.getDate());
		} catch (ParseException e) {
			logger.error("Could not parse calendar date \"{}\" from " +
					"line #{} from file {}", 
					gtfsCalendarDate.getDate(), 
					gtfsCalendarDate.getLineNumber(),
					gtfsCalendarDate.getFileName());
			tempDate = new Date();
		}
		this.date = tempDate;
		
		this.exceptionType = gtfsCalendarDate.getExceptionType();
	}

	/**
	 * Needed because Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private CalendarDate() {
		configRev = -1;
		serviceId = null;
		date = null;
		exceptionType = null;	
	}
	
	/**
	 * Deletes rev from the CalendarDates table
	 * 
	 * @param session
	 * @param configRev
	 * @return Number of rows deleted
	 * @throws HibernateException
	 */
	public static int deleteFromRev(Session session, int configRev) 
			throws HibernateException {
		// Note that hql uses class name, not the table name
		String hql = "DELETE CalendarDate WHERE configRev=" + configRev;
		int numUpdates = session.createQuery(hql).executeUpdate();
		return numUpdates;
	}

	/**
	 * Returns List of Agency objects for the specified database revision.
	 * 
	 * @param session
	 * @param configRev
	 * @return
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<CalendarDate> getCalendarDates(Session session, int configRev) 
			throws HibernateException {
		String hql = "FROM CalendarDate " +
				"    WHERE configRev = :configRev";
		Query query = session.createQuery(hql);
		query.setInteger("configRev", configRev);
		return query.list();
	}

	/**
	 * Returns List of Agency objects for the specified database revision.
	 *
	 * @param session
	 * @param configRev
	 * @return
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<CalendarDate> getCalendarDates(Session session, int configRev, Date date)
			throws HibernateException {
		String hql = "FROM CalendarDate " +
				"    WHERE configRev = :configRev " +
				"    AND date = :date";
		Query query = session.createQuery(hql);
		query.setInteger("configRev", configRev);
		query.setDate("date", date);
		return query.list();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CalendarDate [" 
				+ "configRev=" + configRev 
				+ ", serviceId=" + serviceId
				+ ", date=" + date 
				+ ", exceptionType=" + exceptionType 
				+ " (" + (addService()?"add":"subtract") + " service)"
				+ "]";
	}
	
	/**
	 * Needed because have a composite ID for Hibernate storage
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + configRev;
		result = prime * result
				+ ((exceptionType == null) ? 0 : exceptionType.hashCode());
		result = prime * result
				+ ((serviceId == null) ? 0 : serviceId.hashCode());
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
		CalendarDate other = (CalendarDate) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (configRev != other.configRev)
			return false;
		if (exceptionType == null) {
			if (other.exceptionType != null)
				return false;
		} else if (!exceptionType.equals(other.exceptionType))
			return false;
		if (serviceId == null) {
			if (other.serviceId != null)
				return false;
		} else if (!serviceId.equals(other.serviceId))
			return false;
		return true;
	}

	/******************** Getter Methods **************************/
	/**
	 * @return the configRev
	 */
	public int getConfigRev() {
		return configRev;
	}

	/**
	 * @return the serviceId
	 */
	public String getServiceId() {
		return serviceId;
	}

	/**
	 * The epoch start time of midnight, the beginning of the day.
	 * 
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * The epoch start time of midnight, the beginning of the day.
	 * 
	 * @return the epoch time
	 */
	public long getTime() {
		return date.getTime();
	}
	
	/**
	 * Note that is probably more clear to use addService() since
	 * that way don't need to know what valid values of exception_type
	 * are in GTFS.
	 * 
	 * @return the exceptionType
	 */
	public String getExceptionType() {
		return exceptionType;
	}
	
	/**
	 * Returns true if for this calendar date should add this service.
	 * Otherwise should subtract this service for this date.
	 * @return True if should add service for this calendar date.
	 */
	public boolean addService() {
		return "1".equals(exceptionType);
	}
	
}
