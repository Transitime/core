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

package org.transitime.core.travelTimes;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.Calendar;
import org.transitime.db.structs.Match;
import org.transitime.gtfs.DbConfig;
import org.transitime.utils.IntervalTimer;
import org.transitime.utils.MapKey;

/**
 * For retrieving historic AVL based data from database so that travel times can
 * be determined.
 * 
 * @author SkiBu Smith
 * 
 */
public class DataFetcher {

	// The data ends up in arrivalDepartureMap and matchesMap.
	// It is keyed by DbDataMapKey which means that data is grouped
	// per vehicle trip. This way can later subsequent arrivals/departures
	// for a vehicle trip to determine travel and stop times.
	private Map<DbDataMapKey, List<ArrivalDeparture>> arrivalDepartureMap;
	private Map<DbDataMapKey, List<Match>> matchesMap;

	private Map<String, Calendar> gtfsCalendars = null;
	
	private List<Integer> specialDaysOfWeek = null;

	private static final Logger logger = 
			LoggerFactory.getLogger(DataFetcher.class);

	/********************** Member Functions **************************/

	/**
	 * Sets up needed calendar information if separating out data for special
	 * days of the week, such as Fridays for weekday service.
	 * 
	 * @param projectId
	 * @param newSpecialDaysOfWeek
	 *            List of Integers indicating day of week. Uses
	 *            java.util.Calendar values such as java.util.Calendar.MONDAY .
	 */
	public DataFetcher(String projectId, List<Integer> newSpecialDaysOfWeek) {
		// Read calendar configuration from db
		gtfsCalendars = Calendar.getCalendars(projectId, DbConfig.SANDBOX_REV);
		
		specialDaysOfWeek = newSpecialDaysOfWeek;
	}
	
	/**
	 * Gets the day of the week string for use with the keys for the maps. If special
	 * days of the week were specified using initializeServiceInfo() and the day of the week
	 * as specified by the data parameter is for a day of the week for the calendar specified
	 * by the service ID then a string representing the day of the week is returned.
	 * 
	 * @param serviceId
	 *            the service ID
	 * @param date
	 *            for determining day of the week
	 * @return string indicating day of the week if there is a match, otherwise null
	 */
	private String getMatchingDayOfWeek(String serviceId, Date date) {
		if (specialDaysOfWeek != null && gtfsCalendars != null) {
			// Determine the day of the week for the data
			java.util.Calendar cal = new GregorianCalendar();
			cal.setTime(date);
			int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
			
			// If the day of the week is not one of the special days of the week
			// then it doesn't need special treatment so return null.
			if (!specialDaysOfWeek.contains(dayOfWeek))
				return null;
			
			// The day of the week is a special day. Therefore see if it is included
			// in the calendar specified by the service ID. If it is then return
			// string indicating that the data for the day should be treated special.
			Calendar gtfsCalendar = gtfsCalendars.get(serviceId);
			if (dayOfWeek== java.util.Calendar.MONDAY && gtfsCalendar.getMonday())
				return "Monday";
			else if (dayOfWeek== java.util.Calendar.TUESDAY && gtfsCalendar.getTuesday())
				return "Tuesday";
			else if (dayOfWeek== java.util.Calendar.WEDNESDAY && gtfsCalendar.getWednesday())
				return "Wednesday";
			else if (dayOfWeek== java.util.Calendar.THURSDAY && gtfsCalendar.getThursday())
				return "Thursday";
			else if (dayOfWeek== java.util.Calendar.FRIDAY && gtfsCalendar.getFriday())
				return "Friday";
			else if (dayOfWeek== java.util.Calendar.SATURDAY && gtfsCalendar.getSaturday())
				return "Saturday";
			else if (dayOfWeek== java.util.Calendar.SUNDAY && gtfsCalendar.getSunday())
				return "Sunday";
		}
		
		// Not a match so return null
		return null;		
	}

	/**
	 * Special MapKey class so that can make sure using the proper key for the
	 * associated maps in this class. 
	 */
	public static class DbDataMapKey extends MapKey {
		private DbDataMapKey(String serviceId, String dayOfWeek, String tripId,
				String vehicleId) {
			super(serviceId, dayOfWeek, tripId, vehicleId);
		}
		
		@Override
		public String toString() {
			return "DbDataMapKey [" 
					+ "serviceId=" + o1 
					+ ", dayOfWeek=" + o2 
					+ ", tripId=" + o3 
					+ ", vehicleId=" + o4 
					+ "]";
		}

	}
	
	/**
	 * Returns a key for use in a map. They key consists of the serviceId, an
	 * optional day of the week if the day specified by the date parameter
	 * matches the calendar specified by the service ID, the tripId, and the
	 * vehicleId. This way can separate out data for special days like Fridays
	 * for a weekday service class.
	 * 
	 * @param serviceId
	 * @param date
	 *            Used to determine the day of week to be used in conjunction
	 *            with the service class.
	 * @param tripId
	 * @param vehicleId
	 * @return
	 */
	public DbDataMapKey getKey(String serviceId, Date date, String tripId,
			String vehicleId) {
		String dayOfWeek = getMatchingDayOfWeek(serviceId, date);
		return new DbDataMapKey(serviceId, dayOfWeek, tripId, vehicleId);
	}
	
	/**
	 * Adds the arrival/departure to the map.
	 * 
	 * @param map
	 * @param arrDep
	 */
	private void addArrivalDepartureToMap(
			Map<DbDataMapKey, List<ArrivalDeparture>> map,
			ArrivalDeparture arrDep) {
		DbDataMapKey key = getKey(arrDep.getServiceId(), arrDep.getDate(),
				arrDep.getTripId(), arrDep.getVehicleId());
		List<ArrivalDeparture> list = map.get(key);
		if (list == null) {
			list = new ArrayList<ArrivalDeparture>();
			map.put(key, list);
		}
		list.add(arrDep);
	}
	
	/**
	 * Reads arrivals/departures from db into so can be processed.
	 * 
	 * @param projectId
	 * @param beginTime
	 * @param endTime
	 * @return
	 */
	public Map<DbDataMapKey, List<ArrivalDeparture>> readArrivalsDepartures(
			String projectId, Date beginTime, Date endTime) {
		IntervalTimer timer = new IntervalTimer();

		// For returning the results
		Map<DbDataMapKey, List<ArrivalDeparture>> resultsMap = 
				new HashMap<DbDataMapKey, List<ArrivalDeparture>>();
		
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
					// Order results by time so that process them in the same
					// way that a vehicle travels.
					"ORDER BY time", // SQL clause
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

		logger.info("Reading arrival/departures took {} msec", 
				timer.elapsedMsec());

		// Return the resulting map of arrivals/departures
		return resultsMap;
	}
	
	/**
	 * Adds the arrival/departure to the map.
	 * 
	 * @param map
	 * @param arrDep
	 */
	private void addMatchToMap(Map<DbDataMapKey, List<Match>> map, Match match) {
		DbDataMapKey key = getKey(match.getServiceId(), match.getDate(),
				match.getTripId(), match.getVehicleId());
		List<Match> list = map.get(key);
		if (list == null) {
			list = new ArrayList<Match>();
			map.put(key, list);
		}
		list.add(match);
	}
	
	/**
	 * Reads matches from db into so can be processed.
     *
	 * @param projectId
	 * @param beginTime
	 * @param endTime
	 * @return
	 */
	private Map<DbDataMapKey, List<Match>> readMatches(
			String projectId, Date beginTime, Date endTime) {
		IntervalTimer timer = new IntervalTimer();
		
		// For returning the results
		Map<DbDataMapKey, List<Match>> resultsMap = 
				new HashMap<DbDataMapKey, List<Match>>();
		
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
					// Order results by time so that process them in the same
					// way that a vehicle travels.
					"ORDER BY avlTime", // SQL clause
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

		logger.info("Reading matches took {} msec", timer.elapsedMsec());

		// Return the resulting map of arrivals/departures
		return resultsMap;
	}

	/**
	 * Reads arrival/departure times and matches from the db and puts the
	 * data into the arrivalDepartureMap and matchesMap members.
	 * @param projectId
	 * @param beginTime
	 * @param endTime
	 */
	public void readData(String projectId, Date beginTime, 
			Date endTime) {
		// Read in arrival/departure times and matches from db
		logger.info("Reading historic data from db...");
		matchesMap = readMatches(projectId, beginTime, endTime);
		arrivalDepartureMap = 
				readArrivalsDepartures(projectId, beginTime, endTime);
	}

	/**
	 * Provides the arrival/departure data in a map. The values in the map are
	 * Lists of ArrivalDeparture times, one list for each trip where there was
	 * historic data.
	 * 
	 * @return arrival/departure data
	 */
	public Map<DbDataMapKey, List<ArrivalDeparture>> getArrivalDepartureMap() {
		return arrivalDepartureMap;
	}

	/**
	 * Provides the Match data in a map. The values in the map are Lists of
	 * Match objects, one list for each trip where there was historic data.
	 * 
	 * @return match data
	 */
	public Map<DbDataMapKey, List<Match>> getMatchesMap() {
		return matchesMap;
	}


	
}
