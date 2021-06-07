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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.gtfs.gtfsStructs.GtfsCalendar;
import org.transitclock.utils.Time;

/**
 * Contains data from the calendar.txt GTFS file. This class is
 * for reading/writing that data to the db.
 *
 * @author SkiBu Smith
 *
 */
@Immutable
@Entity @DynamicUpdate @Table(name="Calendars")
public class Calendar implements Serializable {

	@Column 
	@Id
	private final int configRev;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) 
	@Id
	private final String serviceId;
	
	@Column 
	@Id
	private final boolean monday;
	
	@Column 
	@Id
	private final boolean tuesday;
	
	@Column 
	@Id
	private final boolean wednesday;
	
	@Column 
	@Id
	private final boolean thursday;
	
	@Column 
	@Id
	private final boolean friday;
	
	@Column 
	@Id
	private final boolean saturday;
	
	@Column 
	@Id
	private final boolean sunday;
	
	@Temporal(TemporalType.DATE) 
	@Id
	private final Date startDate;
	
	// The service is to run until midnight of the end date, which is actually
	// the endDate plus 1 day.
	@Temporal(TemporalType.DATE) 
	@Id
	private final Date endDate;

	// For outputting start and end date as strings
	private static final DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");

	// Logging
	public static final Logger logger = LoggerFactory.getLogger(Calendar.class);

	// Because Hibernate requires objects with composite IDs to be Serializable
	private static final long serialVersionUID = -7513544548678963561L;

	/********************** Member Functions **************************/

	/**
	 * Needed because Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private Calendar() {
		configRev = -1;
		serviceId = null;
		monday = false;
		tuesday = false;
		wednesday = false;
		thursday = false;
		friday = false;
		saturday = false;
		sunday = false;
		startDate = null;
		endDate = null;
	}
	
	/**
	 * Constructor
	 * 
	 * @param configRev
	 * @param gc
	 * @param dateFormat
	 */
	public Calendar(int configRev, GtfsCalendar gc,	DateFormat dateFormat) {
		this.configRev = configRev;
		this.serviceId = gc.getServiceId();
		this.monday = isSetToTrue(gc.getMonday());
		this.tuesday = isSetToTrue(gc.getTuesday());
		this.wednesday = isSetToTrue(gc.getWednesday());
		this.thursday = isSetToTrue(gc.getThursday());
		this.friday = isSetToTrue(gc.getFriday());
		this.saturday = isSetToTrue(gc.getSaturday());
		this.sunday = isSetToTrue(gc.getSunday());
		
		// Dealing with dates is complicated because must parse
		Date tempDate;
		try {
			tempDate = dateFormat.parse(gc.getStartDate());
		} catch (ParseException e) {
			logger.error("Could not parse calendar start_date \"{}\" from " +
					"line #{} from file {}", 
					gc.getStartDate(), 
					gc.getLineNumber(),
					gc.getFileName());
			tempDate = new Date();
		}
		this.startDate = tempDate;

		// For end date parse the specified date and add a day so that
		// the end date will be midnight of the date specified.
		try {
			tempDate = dateFormat.parse(gc.getEndDate());
		} catch (ParseException e) {
			logger.error("Could not parse calendar end_date \"{}\" from " +
					"line #{} from file {}", 
					gc.getStartDate(), 
					gc.getLineNumber(),
					gc.getFileName());
			tempDate = new Date();
		}
		this.endDate = tempDate;
	}
	
	/**
	 * Deletes rev from the Calendars table
	 * 
	 * @param session
	 * @param configRev
	 * @return Number of rows deleted
	 * @throws HibernateException
	 */
	public static int deleteFromRev(Session session, int configRev) 
			throws HibernateException {
		// Note that hql uses class name, not the table name
		String hql = "DELETE Calendar WHERE configRev=" + configRev;
		int numUpdates = session.createQuery(hql).executeUpdate();
		return numUpdates;
	}

	/**
	 * Returns List of Calendar objects for the specified database revision.
	 * 
	 * @param session
	 * @param configRev
	 * @return List of Calendar objects
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<Calendar> getCalendars(Session session, int configRev) 
			throws HibernateException {
		String hql = "FROM Calendar " +
				"    WHERE configRev = :configRev" +
				" ORDER BY serviceId";
		Query query = session.createQuery(hql);
		query.setInteger("configRev", configRev);
		return query.list();
	}

	/**
	 * Returns List of Calendar objects for the specified database revision.
	 *
	 * @param session
	 * @param configRev
	 * @return List of Calendar objects
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<Calendar> getCalendar(Session session, int configRev, String serviceId)
			throws HibernateException {

		String hql = "FROM Calendar " +
				"    WHERE configRev = :configRev " +
				"    AND serviceId = :serviceId";
		Query query = session.createQuery(hql);
		query.setInteger("configRev", configRev);
		query.setParameter("serviceId", serviceId);
		return query.setMaxResults(1).list();
	}

	/**
	 * Opens up a new db session and returns Map of Calendar objects for the
	 * specified database revision. The map is keyed on the serviceId.
	 * 
	 * @param dbName
	 *            Specified name of database
	 * @param configRev
	 * @return Map of Calendar objects keyed on serviceId
	 * @throws HibernateException
	 */
	public static Map<String, Calendar> getCalendars(String dbName, int configRev) 
			throws HibernateException {
		// Get the database session. This is supposed to be pretty light weight
		Session session = HibernateUtils.getSession(dbName);
		
		// Get list of calendars
		List<Calendar> calendarList = getCalendars(session, configRev);
		
		// Convert list to map and return result
		Map<String, Calendar> map = new HashMap<String, Calendar>();
		for (Calendar calendar : calendarList)
			map.put(calendar.getServiceId(), calendar);
		return map;
	}

	public boolean isOnWeekDay(){
		return monday || tuesday || wednesday || thursday || friday;
	}

	
	/**
	 * Returns true if the parameter zeroOrOne is set to "1". Otherwise
	 * returns false.
	 * @param zeroOrOne
	 * @return
	 */
	private boolean isSetToTrue(String zeroOrOne) {
		return zeroOrOne != null && zeroOrOne.trim().equals("1");
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Calendar [" 
				+ "configRev=" + configRev 
				+ ", serviceId=" + serviceId
				+ ", monday=" + monday 
				+ ", tuesday=" + tuesday
				+ ", wednesday=" + wednesday 
				+ ", thursday=" + thursday
				+ ", friday=" + friday 
				+ ", saturday=" + saturday
				+ ", sunday=" + sunday 
				+ ", startDate=" + Time.dateTimeStr(startDate)
				+ ", endDate=" + Time.dateTimeStr(endDate) 
				+ "]";
	}

	/**
	 * Needed because have a composite ID for Hibernate storage
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + configRev;
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + (friday ? 1231 : 1237);
		result = prime * result + (monday ? 1231 : 1237);
		result = prime * result + (saturday ? 1231 : 1237);
		result = prime * result
				+ ((serviceId == null) ? 0 : serviceId.hashCode());
		result = prime * result
				+ ((startDate == null) ? 0 : startDate.hashCode());
		result = prime * result + (sunday ? 1231 : 1237);
		result = prime * result + (thursday ? 1231 : 1237);
		result = prime * result + (tuesday ? 1231 : 1237);
		result = prime * result + (wednesday ? 1231 : 1237);
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
		Calendar other = (Calendar) obj;
		if (configRev != other.configRev)
			return false;
		if (endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!endDate.equals(other.endDate))
			return false;
		if (friday != other.friday)
			return false;
		if (monday != other.monday)
			return false;
		if (saturday != other.saturday)
			return false;
		if (serviceId == null) {
			if (other.serviceId != null)
				return false;
		} else if (!serviceId.equals(other.serviceId))
			return false;
		if (startDate == null) {
			if (other.startDate != null)
				return false;
		} else if (!startDate.equals(other.startDate))
			return false;
		if (sunday != other.sunday)
			return false;
		if (thursday != other.thursday)
			return false;
		if (tuesday != other.tuesday)
			return false;
		if (wednesday != other.wednesday)
			return false;
		return true;
	}

	/**************** Getter Methods ********************/

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
	 * @return the monday
	 */
	public boolean getMonday() {
		return monday;
	}

	/**
	 * @return the tuesday
	 */
	public boolean getTuesday() {
		return tuesday;
	}

	/**
	 * @return the wednesday
	 */
	public boolean getWednesday() {
		return wednesday;
	}

	/**
	 * @return the thursday
	 */
	public boolean getThursday() {
		return thursday;
	}

	/**
	 * @return the friday
	 */
	public boolean getFriday() {
		return friday;
	}

	/**
	 * @return the saturday
	 */
	public boolean getSaturday() {
		return saturday;
	}

	/**
	 * @return the sunday
	 */
	public boolean getSunday() {
		return sunday;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @return the start date as a formatted string
	 */
	public String getStartDateStr() {
		return formatter.format(startDate);
	}
	
	/**
	 * End of the last day of service. This means that when an end date is
	 * specified the service runs for up to and including that day. Since days
	 * start at midnight, this method returns the endDate plus 1 day so that
	 * it represents midnight of the configured endDate.
	 * @return midnight at the end of the endDate
	 */
	public Date getEndDate() {
		return new Date(endDate.getTime() + 1*Time.MS_PER_DAY);
	}
	
	/**
	 * @return the end date as a formatted string
	 */
	public String getEndDateStr() {
		return formatter.format(endDate);
	}
	
}
