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

package org.transitclock.core.travelTimes;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.db.structs.ActiveRevisions;
import org.transitclock.db.structs.Agency;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Match;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.MapKey;
import org.transitclock.utils.Time;

/**
 * For retrieving historic AVL based data from database so that travel times can
 * be determined.
 * 
 * @author SkiBu Smith
 * 
 */
public class DataFetcher {

	private static boolean pageDbReads() {
		return pageDbReads.getValue();
	}
	private static BooleanConfigValue pageDbReads =
			new BooleanConfigValue("transitclock.updates.pageDbReads",
					true,
					"page database reads to break up long reads. "
					+ "It may impact performance on MySql"
					);
	private static Integer pageSize() {
	  return pageSize.getValue();
	}
	private static IntegerConfigValue pageSize =
	    new IntegerConfigValue("transitclock.updates.pageSize",
	        50000,
	        "Number of records to read in at a time");
	
	// The data ends up in arrivalDepartureMap and matchesMap.
	// It is keyed by DbDataMapKey which means that data is grouped
	// per vehicle trip. This way can later subsequent arrivals/departures
	// for a vehicle trip to determine travel and stop times.
	private Map<DbDataMapKey, List<ArrivalDeparture>> arrivalDepartureMap;
	private Map<DbDataMapKey, List<Match>> matchesMap;

//	private Map<String, Calendar> gtfsCalendars = null;
	
	private java.util.Calendar calendar = null;
	
//	private List<Integer> specialDaysOfWeek = null;

	private static final Logger logger = 
			LoggerFactory.getLogger(DataFetcher.class);

	/********************** Member Functions **************************/

	/**
	 * Sets up needed calendar information if separating out data for special
	 * days of the week, such as Fridays for weekday service.
	 * 
	 * @param dbName
	 * @param newSpecialDaysOfWeek
	 *            List of Integers indicating day of week. Uses
	 *            java.util.Calendar values such as java.util.Calendar.MONDAY .
	 *            Set to null if not going to use.
	 */
	public DataFetcher(String dbName, List<Integer> newSpecialDaysOfWeek) {
		// Create the member calendar using timezone specified in db for the 
		// agency. Use the currently active config rev.
		int configRev = ActiveRevisions.get(dbName).getConfigRev();
		List<Agency> agencies = Agency.getAgencies(dbName, configRev);
		TimeZone timezone = agencies.get(0).getTimeZone();
		calendar = new GregorianCalendar(timezone);		
	}

	public DataFetcher(TimeZone timezone){
		calendar = new GregorianCalendar(timezone);
	}
	
	/**
	 * Takes the date and returns the day into the year. Useful for keeping
	 * track of trip data one day at a time.
	 * 
	 * @param date
	 * @return
	 */
	private int dayOfYear(Date date) {
		// Adjust date by three hours so if get a time such as 2:30 am
		// it will be adjusted back to the previous day. This way can handle
		// trips that span midnight. But this doesn't work for trips that
		// span 3am.
		Date adjustedDate = new Date(date.getTime()-3*Time.MS_PER_HOUR);
		calendar.setTime(adjustedDate);
		return calendar.get(java.util.Calendar.DAY_OF_YEAR);
	}
	
//	/**
//	 * NOTE: Deprecated because haven't yet figured out how to deal with special
//	 * days of the week.
//	 * <p>
//	 * Gets the day of the week string for use with the keys for the maps. If
//	 * special days of the week were specified using initializeServiceInfo() and
//	 * the day of the week as specified by the data parameter is for a day of
//	 * the week for the calendar specified by the service ID then a string
//	 * representing the day of the week is returned.
//	 * 
//	 * @param serviceId
//	 *            the service ID
//	 * @param date
//	 *            for determining day of the week
//	 * @return string indicating day of the week if there is a match, otherwise
//	 *         null
//	 */
//	@Deprecated
//	private String getMatchingDayOfWeek(String serviceId, Date date) {
//		if (specialDaysOfWeek != null && gtfsCalendars != null) {
//			// Determine the day of the week for the data
//			java.util.Calendar cal = new GregorianCalendar();
//			cal.setTime(date);
//			int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
//			
//			// If the day of the week is not one of the special days of the week
//			// then it doesn't need special treatment so return null.
//			if (!specialDaysOfWeek.contains(dayOfWeek))
//				return null;
//			
//			// The day of the week is a special day. Therefore see if it is included
//			// in the calendar specified by the service ID. If it is then return
//			// string indicating that the data for the day should be treated special.
//			Calendar gtfsCalendar = gtfsCalendars.get(serviceId);
//			if (dayOfWeek== java.util.Calendar.MONDAY && gtfsCalendar.getMonday())
//				return "Monday";
//			else if (dayOfWeek== java.util.Calendar.TUESDAY && gtfsCalendar.getTuesday())
//				return "Tuesday";
//			else if (dayOfWeek== java.util.Calendar.WEDNESDAY && gtfsCalendar.getWednesday())
//				return "Wednesday";
//			else if (dayOfWeek== java.util.Calendar.THURSDAY && gtfsCalendar.getThursday())
//				return "Thursday";
//			else if (dayOfWeek== java.util.Calendar.FRIDAY && gtfsCalendar.getFriday())
//				return "Friday";
//			else if (dayOfWeek== java.util.Calendar.SATURDAY && gtfsCalendar.getSaturday())
//				return "Saturday";
//			else if (dayOfWeek== java.util.Calendar.SUNDAY && gtfsCalendar.getSunday())
//				return "Sunday";
//		}
//		
//		// Not a match so return null
//		return null;		
//	}

	/**
	 * Special MapKey class so that can make sure using the proper key for the
	 * associated maps in this class. 
	 */
	public static class DbDataMapKey extends MapKey {

		private DbDataMapKey(String serviceId, Integer dayOfYear, String tripId, String vehicleId) {
			super(serviceId, dayOfYear, tripId, vehicleId);
		}

		public String getServiceId(){
			return (String) this.o1;
		}

		public Integer getDayOfYear(){
			return (Integer) this.o2;
		}

		public String getTripId(){
			return (String) this.o3;
		}

		public String getVehicleId(){
			return (String) this.o4;
		}

		
		@Override
		public String toString() {
			return "DbDataMapKey [" 
					+ "serviceId=" + o1 
					+ ", dayOfYear=" + o2 
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
		return new DbDataMapKey(serviceId, dayOfYear(date), tripId, vehicleId);
	}
	
	/**
	 * Adds the arrival/departure to the map.
	 * 
	 * @param map
	 * @param arrDep
	 */
	public void addArrivalDepartureToMap(
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
	 * @param dbName
	 * @param beginTime
	 * @param endTime
	 * @return
	 */
	public Map<DbDataMapKey, List<ArrivalDeparture>> readArrivalsDepartures(
			String dbName, Date beginTime, Date endTime) {
		IntervalTimer timer = new IntervalTimer();

		// For returning the results
		Map<DbDataMapKey, List<ArrivalDeparture>> resultsMap = 
				new HashMap<DbDataMapKey, List<ArrivalDeparture>>();
		
		// For keeping track of which rows should be returned by the batch.
		int firstResult = 0;
		// Batch size of 50k found to be significantly faster than 10k,
		// by about a factor of 2. Since sometimes using really large
		// batches of data using 500k
		int batchSize = pageSize.getValue();  // Also known as maxResults
		// The temporary list for the loop that contains a batch of results
		
		logger.info("counting arrival/departures");
		Long count = ArrivalDeparture.getArrivalsDeparturesCountFromDb(dbName, beginTime, endTime, null, false);
		logger.info("retrieving {} arrival/departures", count);
		List<ArrivalDeparture> arrDepBatchList;
		
		if (!pageDbReads()) {
			// page by day for MySql -- its batch impl falls down on large data
			Date pageBeginTime = beginTime;
			Date pageEndTime = new Date(beginTime.getTime() + Time.MS_PER_DAY);
			int runningCount = 0;
			do {
				logger.info("querying a/d for between {} and {}", pageBeginTime, pageEndTime);
				arrDepBatchList = ArrivalDeparture.getArrivalsDeparturesFromDb(
						dbName, 
						pageBeginTime, pageEndTime, 
						// Order results by time so that process them in the same
						// way that a vehicle travels.
						"ORDER BY time", // SQL clause
						null, null,
						null, // arrivalOrDeparture. Null means read in both
						false);
				
				// Add arrivals/departures to map
				for (ArrivalDeparture arrDep : arrDepBatchList) {
					addArrivalDepartureToMap(resultsMap, arrDep);
				}
				runningCount += arrDepBatchList.size();
				logger.info("Read in total of {} a/ds of {} {}%", 
						runningCount, count, (0.0+runningCount)/count*100);

				pageBeginTime = pageEndTime;
				pageEndTime = new Date(Math.min(pageBeginTime.getTime() + Time.MS_PER_DAY, endTime.getTime()));
				
			} while (pageEndTime.before(endTime));
		} else {
			// Read in batch of 50k rows of data and process it
			do {				
				arrDepBatchList = ArrivalDeparture.getArrivalsDeparturesFromDb(
						dbName, 
						beginTime, endTime, 
						// Order results by time so that process them in the same
						// way that a vehicle travels.
						"ORDER BY time", // SQL clause
						firstResult, batchSize,
						null, // arrivalOrDeparture. Null means read in both
						false);
				// Add arrivals/departures to map
				for (ArrivalDeparture arrDep : arrDepBatchList) {
					addArrivalDepartureToMap(resultsMap, arrDep);
				}
				
				logger.info("Read in total of {} arrival/departures of {} {}%", 
						firstResult+arrDepBatchList.size(), count, (0.0+firstResult+arrDepBatchList.size())/count*100);
				
				// Update firstResult for reading next batch of data
				firstResult += batchSize;
			} while (arrDepBatchList.size() == batchSize);
		}
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
		// by about a factor of 2.  Since sometimes using really large
		// batches of data using 500k
		int batchSize = pageSize.getValue();  // Also known as maxResults
		String sqlClause = "AND atStop = false ORDER BY avlTime";
		logger.info("counting matches...");
		Long count = Match.getMatchesCountFromDb(projectId, beginTime, endTime, "AND atStop = false");
		logger.info("found {} matches", count);
		
		// The temporary list for the loop that contains a batch of results
		List<Match> matchBatchList;
		
		if (!pageDbReads()) {
			// page by day for MySql -- its batch impl falls down on large data
			Date pageBeginTime = beginTime;
			Date pageEndTime = new Date(beginTime.getTime() + Time.MS_PER_DAY);
			int runningCount = 0;
			do {
				logger.info("querying matches for between {} and {}", pageBeginTime, pageEndTime);
				matchBatchList = Match.getMatchesFromDb(projectId, pageBeginTime, pageEndTime, sqlClause, null, null);
				// Add arrivals/departures to map
				for (Match match : matchBatchList) {
					addMatchToMap(resultsMap, match);
				}
				runningCount += matchBatchList.size();
				logger.info("Read in total of {} matches of {} {}%", 
						runningCount, count, (0.0+runningCount)/count*100);

				pageBeginTime = pageEndTime;
				pageEndTime = new Date(Math.min(pageBeginTime.getTime() + Time.MS_PER_DAY, endTime.getTime()));
				
			} while (pageEndTime.before(endTime));
		} else {
		// Read in batch of 50k rows of data and process it
			do {				
				matchBatchList = Match.getMatchesFromDb(
						projectId, 
						beginTime, endTime, 
						// Only want matches that are not at a stop since for that
						// situation instead using arrivals/departures. 
						// Order results by time so that process them in the same
						// way that a vehicle travels.
						sqlClause, // SQL clause
						firstResult, batchSize);
				
				// Add arrivals/departures to map
				for (Match match : matchBatchList) {
					addMatchToMap(resultsMap, match);
				}
				
				logger.info("Read in total of {} matches of {} {}%", 
						firstResult+matchBatchList.size(), count, (0.0+firstResult+matchBatchList.size())/count*100);
				
				// Update firstResult for reading next batch of data
				firstResult += batchSize;
			} while (matchBatchList.size() == batchSize);
			}
		logger.info("Reading matches took {} msec", timer.elapsedMsec());

		// Return the resulting map of arrivals/departures
		return resultsMap;
	}

	/**
	 * Reads arrival/departure times and matches from the db and puts the
	 * data into the arrivalDepartureMap and matchesMap members.
	 * 
	 * @param agencyId
	 * @param beginTime
	 * @param endTime
	 */
	public void readData(String agencyId, Date beginTime, 
			Date endTime) {
		// Read in arrival/departure times and matches from db
		logger.info("Reading historic data from db...");
		matchesMap = readMatches(agencyId, beginTime, endTime);
		if (matchesMap == null || matchesMap.isEmpty()) {
			logger.info("No Matches present in db");
			return;
		}
		arrivalDepartureMap = 
				readArrivalsDepartures(agencyId, beginTime, endTime);
	}

	/**
	 * Provides the arrival/departure data in a map. The values in the map are
	 * Lists of ArrivalDeparture times, one list for each trip where there was
	 * historic data.
	 * 
	 * @return arrival/departure data
	 */
	public Map<DbDataMapKey, List<ArrivalDeparture>> getArrivalDepartureMap() {
		// Make sure data was read in
		if (arrivalDepartureMap == null)
			throw new RuntimeException("Called getArrivalDepartureMap() before "
					+ "data was read in using readData().");
		
		return arrivalDepartureMap;
	}

	/**
	 * Provides the Match data in a map. The values in the map are Lists of
	 * Match objects, one list for each trip where there was historic data.
	 * 
	 * @return match data
	 */
	public Map<DbDataMapKey, List<Match>> getMatchesMap() {
		// Make sure data was read in
		if (matchesMap == null)
			throw new RuntimeException("Called getMatchesMap() before "
					+ "data was read in using readData().");

		return matchesMap;
	}


	
}
