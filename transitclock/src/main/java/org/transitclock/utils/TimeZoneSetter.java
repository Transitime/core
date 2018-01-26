/* This file is part of Transitime.org
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
package org.transitclock.utils;

import java.util.TimeZone;

import org.transitclock.config.StringConfigValue;

/**
 * For setting timezone for application. Ideally would get timezone from the
 * agency db but once a Hibernate session factory is created, such as for
 * reading timezone from db, then it is too late to set the timezone. Therefore
 * this provides ability to set it manually.
 * 
 * @author Michael
 *
 */
public class TimeZoneSetter {
	public static String getTimezone() {
		return timezone.getValue();
	}
	private static StringConfigValue timezone =
			new StringConfigValue("transitime.core.timezone", 
					"For setting timezone for application. Ideally would get "
					+ "timezone from the agency db but once a Hibernate "
					+ "session factory is created, such as for reading "
					+ "timezone from db, then it is too late to set the "
					+ "timezone. Therefore this provides ability to set it "
					+ "manually.");
	
	/**
	 * For setting timezone for application to name specified by the Java
	 * property transitime.core.timezone. Ideally would get timezone from the
	 * agency db but once a Hibernate session factory is created, such as for
	 * reading timezone from db, then it is too late to set the timezone.
	 * Therefore this provides ability to set it manually.
	 */
	public static void setTimezone() {
		String timezoneStr = timezone.getValue();
		if (timezoneStr != null) {
			TimeZone.setDefault(TimeZone.getTimeZone(timezoneStr));
		}
	}
	

}
