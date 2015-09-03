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
	private static int MAX_REQUESTS = 100;
	private static int MAX_REQUESTS_TIME_MSEC = 10000;

	// This is a singleton class
	private static UsageValidator singleton = new UsageValidator();

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
	 * Makes sure that usage doesn't exceed limits. Intended to deal with bad
	 * applications that are requesting too much data or a denial of service
	 * attack. Currently simply checks to make sure that for a given request IP
	 * address that there aren't more than a certain number of requests per time
	 * frame.
	 * <p>
	 * Probably should be refined in the future.
	 * 
	 * @param stdParameters
	 * @throws WebApplicationException
	 */
	public void validateUsage(StandardParameters stdParameters)
			throws WebApplicationException {

		if (stdParameters.getRequest() != null) {
			String requestIpAddress = stdParameters.getRequest()
					.getRemoteAddr();

			long currentTime = System.currentTimeMillis();

			// Get the list of access times for the IP address. Can be empty.
			LinkedList<Long> accessTimes = requestTimesPerIp
					.get(requestIpAddress);
			if (accessTimes == null) {
				accessTimes = new LinkedList<Long>();
				requestTimesPerIp.put(requestIpAddress, accessTimes);
			}

			// If number of requests already reached see if got too many
			// within the time limit.
			if (accessTimes.size() == MAX_REQUESTS) {
				Long oldestAccessTime = accessTimes.getLast();
				if (oldestAccessTime > currentTime - MAX_REQUESTS_TIME_MSEC) {
					// Note that using special HTTP response 429, which is for
					// Too Many Requests. See
					// http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
					throw WebUtils.badRequestException(429, "Exceeded "
							+ MAX_REQUESTS + " requests within "
							+ MAX_REQUESTS_TIME_MSEC + " msec for IP address "
							+ requestIpAddress);
				}
			}

			// Add the current request time to the queue
			accessTimes.addFirst(currentTime);

			// Clean up queue of old requests that are beyond the time limit
			while (accessTimes.getLast() < currentTime - MAX_REQUESTS_TIME_MSEC)
				accessTimes.removeLast();
		}
	}
}
