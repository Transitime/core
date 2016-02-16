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

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.WebApplicationException;

import org.transitime.config.IntegerConfigValue;

/**
 * For making sure that use of API doesn't exceed limits. Intended to deal with
 * bad applications that are requesting too much data or a denial of service
 * attack. Currently simply checks to make sure that for a given request IP
 * address that there aren't more than a certain number of requests per time
 * frame.
 * <p>
 * This check should probably be refined in the future.
 * <p>
 * Note that a map is used to keep track of the last request times. Eventually
 * this map could take up quite a bit of memory if old requests are never
 * cleared out.
 * 
 * @author SkiBu Smith
 * 
 */
public class UsageValidator {

	// The limits of requests per IP address
	
	private static IntegerConfigValue maxRequests = new IntegerConfigValue(
			"transitime.usage.maxRequests", 2000,
			"Maximum number of requests to allow within the specified time frame");
	
	private static IntegerConfigValue maxRequestsTimeMsec = new IntegerConfigValue(
			"transitime.usage.maxRequestsTimeMsec", 1000,
			"Amount of time in msec before max requests count limit is reset");
	
	// This is a singleton class
	private static UsageValidator singleton = new UsageValidator();

	// Keyed on IP address. Contains list of times API last called for the 
	// IP address.
	private Map<String, LinkedList<Long>> requestTimesPerIp = 
			new ConcurrentHashMap<String, LinkedList<Long>>();

	/********************** Member Functions **************************/

	/**
	 * Constructor private because singleton class
	 */
	private UsageValidator() {
	}

	/**
	 * Get singleton instance.
	 * 
	 * @return
	 */
	public static UsageValidator getInstance() {
		return singleton;
	}

	/**
	 * TODO: previous impl was not thread safe -- so now its just a placeholder
	 * for future checks  
	 * @param stdParameters
	 * @throws WebApplicationException
	 */
	public void validateUsage(StandardParameters stdParameters)
			throws WebApplicationException {

		return;
	}
}
