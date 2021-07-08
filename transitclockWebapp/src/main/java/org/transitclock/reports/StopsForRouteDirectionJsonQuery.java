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
package org.transitclock.reports;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.GenericQuery;
import org.transitclock.utils.Time;

/**
 * Does a query of arrival departure data and returns result in JSON format.
 * 
 * @author SkiBu Smith
 *
 */
public class StopsForRouteDirectionJsonQuery {

	private static final Logger logger = LoggerFactory
			.getLogger(StopsForRouteDirectionJsonQuery.class);
	
	/**
	 * Queries agency for arrival departure data and returns result as a JSON string.
	 * 
	 * @param agencyId
	 *
	 * @param routeId
	 *            routeId parameter for stops
	 * @param headsign
	 * 			  headsign for stops
	 * @return List of stops in JSON format. Can be empty JSON array if no data
	 *         meets criteria.
	 */
	public static String getStopsJson(String agencyId, String routeId, String headsign) {

		String sql = "SELECT tp.id " +
					 "FROM TripPatterns tp " +
					 "WHERE tp.routeId = '" + routeId + "' AND tp.headsign = '" + headsign + "' AND tp.configRev = (select configRev from ActiveRevisions limit 1) ";
		/*GenericQuery

		String sql = "SELECT s.id, s.name, sp.tripPatternId, sp.gtfsStopSeq " +
				"FROM Stops s " +
				"JOIN StopPaths sp ON s.id = sp.stopId " +
				"JOIN TripPatterns tp ON sp.tripPatternId = tp.id AND sp.routeId = tp.routeId AND sp.configRev = tp.configRev AND tp.configRev = s.configRev " +
				"WHERE tp.routeId = '" + routeId + "' AND tp.headsign = '" + headsign + "' AND tp.configRev = (select configRev from ActiveRevisions limit 1) " +
				"ORDER BY sp.tripPatternId, sp.gtfsStopSeq;";*/

		String json = GenericJsonQuery.getJsonString(agencyId, sql);

		return json;

	}
}
