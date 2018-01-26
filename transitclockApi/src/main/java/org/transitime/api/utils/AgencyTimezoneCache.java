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

package org.transitime.api.utils;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.Agency;
import org.transitclock.ipc.clients.ConfigInterfaceFactory;
import org.transitclock.ipc.interfaces.ConfigInterface;

/**
 * So that can get quick access to TimeZone for agency so that can properly
 * format times and dates for feed.
 * 
 * @author Michael
 *
 */
public class AgencyTimezoneCache {
	private final static HashMap<String, TimeZone> timezonesMap =
			new HashMap<String, TimeZone>();
	
	private static final Logger logger = LoggerFactory
			.getLogger(AgencyTimezoneCache.class);

	/**
	 * Returns the TimeZone for the agency specified by the agencyId. The timezone is obtained from 
	 * the core agency server. Therefore it is cached to reduce requests to the server.
	 * @param agencyId
	 * @return The TimeZone for the agency or null if could not be determined
	 */
	public static TimeZone get(String agencyId) {
		// Trying getting timezone from cache
		TimeZone timezone = timezonesMap.get(agencyId);
		
		// If timezone not already in cache then get it and cache it
		if (timezone == null) {
			ConfigInterface inter = ConfigInterfaceFactory.get(agencyId);
			List<Agency> agencies;
			try {
				agencies = inter.getAgencies();
			} catch (RemoteException e) {
				logger.error("Exception getting timezone for agencyId={}", agencyId, e);
				return null;
			}
			
			// Use timezone of first agency
			String timezoneStr = agencies.get(0).getTimeZoneStr();
			if (timezoneStr == null)
				return null;
			timezone = TimeZone.getTimeZone(timezoneStr);
			
			// Cache the timezone
			timezonesMap.put(agencyId, timezone);
		}
		
		// Return the result
		return timezone;
	}
}
