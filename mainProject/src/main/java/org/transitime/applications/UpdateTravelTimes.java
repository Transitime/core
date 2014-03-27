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

package org.transitime.applications;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.Match;
import org.transitime.db.structs.TravelTimesForTrip;
import org.transitime.gtfs.DbConfig;
import org.transitime.utils.Time;

/**
 * Uses AVL based data of arrival/departure times and matches from the database
 * to update the expected travel and stop times.
 * 
 * @author SkiBu Smith
 * 
 */
public class UpdateTravelTimes {

	private static final Logger logger = 
			LoggerFactory.getLogger(UpdateTravelTimes.class);

	/********************** Member Functions **************************/

	/**
	 * Adds the arrival/departure to the map.
	 * 
	 * @param map
	 * @param arrDep
	 */
	private static void addArrivalDepartureToMap(
			Map<String, List<ArrivalDeparture>> map, ArrivalDeparture arrDep) {
		// FIXME
	}
	
	private static Map<String, List<ArrivalDeparture>> readArrivalsDepartures(
			String projectId, Date beginTime, Date endTime) {
		// For returning the results
		Map<String, List<ArrivalDeparture>> resultsMap = 
				new HashMap<String, List<ArrivalDeparture>>();
		
		// For keeping track of which rows should be returned by the batch.
		int firstResult = 0;
		// Batch size of 50k found to be significantly faster than 10k,
		// by about a factor of 2.
		int batchSize = 50000;  // Also known as maxResults
		// The temporary list for the loop that contains a batch of results
		List<ArrivalDeparture> arrDepBatchList;
		// Read in batch of 50k rows of data and process it
		do {				
			arrDepBatchList = ArrivalDeparture.getArrivalsDeparturesFromDb(
					projectId, 
					beginTime, endTime, 
					"ORDER BY serviceId, tripId, vehicleId, time", // SQL clause
					firstResult, batchSize,
					null); // arrivalOrDeparture. Null means read in both
			
			// Add arrivals/departures to map
			for (ArrivalDeparture arrDep : arrDepBatchList) {
				addArrivalDepartureToMap(resultsMap, arrDep);
			}
			
			logger.info("Read in {} arrival/departures", 
					firstResult+arrDepBatchList.size());
			
			// Update firstResult for reading next batch of data
			firstResult += batchSize;
		} while (arrDepBatchList.size() == batchSize);

		// Return the resulting map of arrivals/departures
		return resultsMap;
	}
	
	/**
	 * Adds the arrival/departure to the map.
	 * 
	 * @param map
	 * @param arrDep
	 */
	private static void addMatchToMap(Map<String, List<Match>> map, Match match) {
		// FIXME
	}
	
	private static Map<String, List<Match>> readMatches(
			String projectId, Date beginTime, Date endTime) {
		// For returning the results
		Map<String, List<Match>> resultsMap = 
				new HashMap<String, List<Match>>();
		
		// For keeping track of which rows should be returned by the batch.
		int firstResult = 0;
		// Batch size of 50k found to be significantly faster than 10k,
		// by about a factor of 2.
		int batchSize = 50000;  // Also known as maxResults
		// The temporary list for the loop that contains a batch of results
		List<Match> matchBatchList;
		// Read in batch of 50k rows of data and process it
		do {				
			matchBatchList = Match.getMatchesFromDb(
					projectId, 
					beginTime, endTime, 
					"ORDER BY serviceId, tripId, vehicleId, avlTime", // SQL clause
					firstResult, batchSize);
			
			// Add arrivals/departures to map
			for (Match match : matchBatchList) {
				addMatchToMap(resultsMap, match);
			}
			
			logger.info("Read in {} matches", 
					firstResult+matchBatchList.size());
			
			// Update firstResult for reading next batch of data
			firstResult += batchSize;
		} while (matchBatchList.size() == batchSize);

		// Return the resulting map of arrivals/departures
		return resultsMap;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// FIXME
		String projectId = "sf-muni";
		String startDateStr = "3-22-2014";
		String endDateStr = "3-22-14";
		
		Date beginTime = null;
		Date endTime = null;
		try {
			beginTime = Time.parseDate(startDateStr);
			endTime = new Date(Time.parseDate(endDateStr).getTime() + Time.MS_PER_DAY);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		// Read in arrival/departure times and matches from db
		readMatches(projectId, beginTime, endTime);
		
		Map<String, List<ArrivalDeparture>> arrivalDepartureMap =
				readArrivalsDepartures(projectId, beginTime, endTime);
		
		// Read in existing travel times from database
		Map<String, List<TravelTimesForTrip>> travelTimesByTripPatternMap = 
				TravelTimesForTrip.getTravelTimesForTrip(projectId, 
						DbConfig.SANDBOX_REV);

		// Get a database session
		SessionFactory sessionFactory = 
				HibernateUtils.getSessionFactory(projectId);
		Session session = sessionFactory.openSession();

		// Write out the data
		// FIXME
		
		// Close up db connection
		session.close();
	}

}
