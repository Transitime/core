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
package org.transitime.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.transitime.db.structs.Calendar;
import org.transitime.db.structs.CalendarDate;
import org.transitime.gtfs.DbConfig;
import org.transitime.utils.Time;

/**
 * For working with service types, such as determining serviceId or
 * appropriate block to use for a given epoch time.
 * 
 * @author SkiBu Smith
 *
 */
public class Service {

	private final GregorianCalendar calendar;
	
	private final DbConfig dbConfig;
	
	private static final Logger logger = LoggerFactory.getLogger(Service.class);

	/********************** Member Functions **************************/

	/**
	 * Service constructor. Creates reusable GregorianCalendar and sets the
	 * timezone so that the calendar can be reused.
	 * 
	 * @param timezoneName See http://en.wikipedia.org/wiki/List_of_tz_zones
	 */
	public Service(DbConfig dbConfig) { 
		this.calendar = 
				new GregorianCalendar(dbConfig.getFirstAgency().getTimeZone());
		this.dbConfig = dbConfig;
	}

	/**
	 * Returns day of the week. Value returned will be a constant from
	 * jvaa.util.Calendar such as Calendar.TUESDAY.
	 * 
	 * @param epochTime
	 * @return Day of the week
	 */
	public int getDayOfWeek(Date epochTime) {
		synchronized (calendar) {
			calendar.setTime(epochTime);
			return calendar.get(java.util.Calendar.DAY_OF_WEEK);			
		}
	}
	
	/**
	 * Gets list of currently active calendars. If all Calendars have expired
	 * then will still use the ones that end at the latest date. This way if
	 * someone forgets to update the GTFS Calendars or if someone forgets to
	 * process the latest GTFS data in time, the system will still run using the
	 * old Calendars. This is very important because it is unfortunately
	 * somewhat common for the Calendars to expire.
	 * 
	 * @param epochTime
	 *            For determining which Calendars are currently active
	 * @return List of active Calendars
	 */
	private List<Calendar> getActiveCalendars(Date epochTime) {
		List<Calendar> originalCalendarList = dbConfig.getCalendars();
		List<Calendar> activeCalendarList = new ArrayList<Calendar>();
		long maxEndTime = 0;
		
		// Go through calendar and find currently active ones
		for (Calendar calendar : originalCalendarList) {
			// If calendar is currently active then add it to list of active ones
			if (epochTime.getTime() >= calendar.getStartDate().getTime()
					&& epochTime.getTime() <= calendar.getEndDate().getTime()
							+ 1 * Time.MS_PER_DAY) {
				activeCalendarList.add(calendar);
			}
			
			// Update the maxEndTime in case all calendars have expired
			maxEndTime = Math.max(calendar.getEndDate().getTime(), maxEndTime);
		}
		
		// If there are no currently active calendars then there most
		// likely someone forgot to update the dates or perhaps the
		// latest GTFS data was never processed. To handle this kind
		// of situation use the most recent Calendars if none are
		// configured to be active.
		if (activeCalendarList.size() == 0) {
			for (Calendar calendar : originalCalendarList) {
				if (calendar.getEndDate().getTime() == maxEndTime) {
					activeCalendarList.add(calendar);
				}
			}
			
			// This is a rather serious issue so log it as an error
			logger.error("All Calendars were expired. Update them!!! So that " +
					"the system will continue to run the old Calendars will " +
					"be used: {}", activeCalendarList);
		}
		
		// Return the results
		return activeCalendarList;
	}
	
	/**
	 * Determines list of current service IDs for the specified time.
	 * These service IDs designate which block assignments are currently
	 * active.
	 * 
	 * @param epochTime The current time that determining service IDs for
	 * @param calendars List of calendar.txt GTFS data
	 * @param calendarDates List of calendar_dates.txt GTFS data
	 * @return List of service IDs that are active for the specified time.
	 */
	public List<String> getServiceIds(Date epochTime) {
		List<String> serviceIds = new ArrayList<String>();
		
		// Make sure haven't accidentally let all calendars expire
		List<Calendar> activeCalendars = getActiveCalendars(epochTime);
		
		// Go through calendars and determine which ones match. For those that
		// match, add them to the list of service IDs.
		int dateOfWeek = getDayOfWeek(epochTime);
		for (Calendar calendar : activeCalendars) {			
			// If calendar for the current day of the week then add the serviceId
			if ((dateOfWeek == java.util.Calendar.MONDAY && calendar.getMonday())   ||
				(dateOfWeek == java.util.Calendar.TUESDAY && calendar.getTuesday()) ||
				(dateOfWeek == java.util.Calendar.WEDNESDAY && calendar.getWednesday()) ||
				(dateOfWeek == java.util.Calendar.THURSDAY && calendar.getThursday()) ||
				(dateOfWeek == java.util.Calendar.FRIDAY && calendar.getFriday()) ||
				(dateOfWeek == java.util.Calendar.SATURDAY && calendar.getSaturday()) ||
				(dateOfWeek == java.util.Calendar.SUNDAY && calendar.getSunday())) {
				serviceIds.add(calendar.getServiceId());				
			}	
		}
		logger.debug("For {} services from calendar.txt that are active are {}", 
				epochTime, serviceIds);
		
		// Go through calendar_dates to see if there is special service for 
		// this date. Add or remove the special service.
		for (CalendarDate calendarDate : dbConfig.getCalendarDates()) {
			// If the time is within 24 hours of the midnight time of the calendar date
			// then it indeed matches the date. Note that this method of checking is
			// much faster than using Calendar class to see if on same day.
			if (epochTime.getTime() < calendarDate.getDate().getTime() + 1*Time.MS_PER_DAY) {
				// Yes, there is special service for this date
				if (calendarDate.addService()) {
					// Add the service for this date
					serviceIds.add(calendarDate.getServiceId());
				} else {
					// Remove the service for this date
					serviceIds.remove(calendarDate.getServiceId());
				}
				
				logger.debug("{} is special service date in calendar_dates.txt file. " + 
						"Services now are {}",
						epochTime, serviceIds);
			}
		}
		
		// Return the results
		return serviceIds;
	}
	
}
