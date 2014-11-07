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

package org.transitime.statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.structs.ArrivalDeparture.ArrivalsOrDepartures;
import org.transitime.gtfs.gtfsStructs.GtfsStopTime;
import org.transitime.statistics.ScheduleDataProcessor.TripStopKey;
import org.transitime.utils.StringUtils;
import org.transitime.utils.Time;

/**
 * Contains methods for taking arrival/departure info that is collected
 * in maps and processing it to determine optimal schedule.
 * <p>
 * Once all of the arrival and departure times have been processed into a map
 * statistics is used to determine which is the best arrival/departure for the
 * stop_times output. The goal is to use a time such that only the a desired
 * fraction of arrivals/departures will be early. For example, if you want only
 * 20% of the vehicle to be early with respect to the schedule time, which is
 * reasonable because for passengers it is better for vehicles to be late rather
 * then early so they don't miss the vehicle, then the value should be 0.2. This
 * desiredFractionEarly value is specified when the ScheduleDataProcessor object is
 * constructed.
 * <p>
 * The way the software tries to achieve the desiredFractionEarly is by assuming
 * there is a Gaussian distribution of the times. By using the standard
 * deviation of a Gaussian distribution the software estimates the value to use
 * to such that desiredFractionEarly will be attained. Of course the
 * distribution is not truly Gaussian. Therefore several iterations are used to
 * adjust the value in order to get the desired results.
 * <p>
 * Note: convenient website for determining standard deviation is at
 * http://www.mathsisfun.com/data/standard-deviation-calculator.html
 * 
 * @author SkiBu Smith
 * 
 */
public class ScheduleStatistics {

	// Number of iterations to use when determining how many standard deviations
	// are needed to achieve the desired desiredFractionEarly
	private static final int NUMBER_ITERATIONS = 5;

	private static final Logger logger = 
			LoggerFactory.getLogger(ScheduleStatistics.class);

	/********************** Member Functions **************************/

	/**
	 * For containing the results of processing AVL based arrival/departure
	 * times as part of determining more accurate schedule times.
	 */
	public static class Stats {
		// Seconds into day
		public int bestValue;
		// The arrival/departure times. Ones from the db that are far from the
		// schedule time or the mean time are filtered out and therefore not
		// included.
		public int filteredTimesArray[];
		// The arrival/departure times from the db. Includes even the filtered
		// times.
		public int unfilteredTimesArray[];
		// The average of the filtered times
		public float mean;
		// Will be NaN if there was only a single data point for the trip/stop
		public float standardDeviation;
		// Initialize to MAX_VALUE to simplify determination of minimum
		public int min = Integer.MAX_VALUE;
		// Initialize to MIN_VALUE to simplify determination of maximum
		public int max = Integer.MIN_VALUE;
	}


	/**
	 * For the trip/stop specified by gtfsStopTime determines the Stats object
	 * which contains mean, standard deviation, min, max, and number of data
	 * points. Sets everything in the Stats structure except for the bestValue
	 * member.
	 * 
	 * @param timesFromDb
	 *            data for particular trip/stop. This list will be modified by
	 *            filtering out data that is too far from the schedule time or
	 *            too far from the mean.
	 * @param originalGtfsStopTime
	 *            The original data from stop_times.txt for this trip/stop. Used
	 *            for filtering out outliers. Can be null when using test data
	 *            instead of data from stop_times.txt.
	 * @param arrivalsOrDepartures
	 *            specifies whether should handle as arrivals or departures
	 * @return Stats object containing the statistics for the trip/stop or null
	 *         if there is no data.
	 */
	private static Stats getStatisticsForTripStop(List<Integer> timesFromDb,
			GtfsStopTime originalGtfsStopTime,
			int allowableDifferenceFromMeanSecs,
			int allowableDifferenceFromOriginalTimeSecs,
			ArrivalsOrDepartures arrivalsOrDepartures) {
		// If no data for this trip/stop then return null
		if (timesFromDb == null)
			return null;

		// There is data for this trip/stop so process it. Create the Stats
		// object and start filling it out.
		Stats results = new Stats();
		results.unfilteredTimesArray = Statistics.toArray(timesFromDb);

		// Determine mean before filtering out data
		double mean = Statistics.getMean(results.unfilteredTimesArray);

		// Filter out outliers from timesFromDb and put into filteredTimesFromDb
		List<Integer> filteredTimesFromDb = new ArrayList<Integer>(timesFromDb);
		boolean timeFilteredOut = false;
		Iterator<Integer> iterator = filteredTimesFromDb.iterator();
		Integer originalScheduleTime = 0;
		if (originalGtfsStopTime != null) {
			originalScheduleTime = 
					arrivalsOrDepartures == ArrivalsOrDepartures.ARRIVALS ? 
							originalGtfsStopTime.getArrivalTimeSecs() : 
								originalGtfsStopTime.getDepartureTimeSecs();
		}
		while (iterator.hasNext()) {
			int time = iterator.next();

			// If time is too far away from mean then don't use it
			if (Math.abs(time - mean) > allowableDifferenceFromMeanSecs) {
				timeFilteredOut = true;
				iterator.remove();
				logger.debug(
						"For trip={} stop={} filtering out {} time={} because "
								+ "it is {} seconds from the mean={} which is futher "
								+ "awat than the allowable={} secs.",
						originalGtfsStopTime == null ? "NA"
								: originalGtfsStopTime.getTripId(),
						originalGtfsStopTime == null ? "NA"
								: originalGtfsStopTime.getStopId(),
						arrivalsOrDepartures, Time.timeOfDayStr(time),
						StringUtils.oneDigitFormat(Math.abs(time - mean)), Time
								.timeOfDayStr(Math.round(mean)),
						allowableDifferenceFromMeanSecs);
			}

			// If time is too far away from original time in stop_times.txt
			// then don't use it.
			else if (Math.abs(time - originalScheduleTime) > allowableDifferenceFromOriginalTimeSecs) {
				timeFilteredOut = true;
				iterator.remove();
				logger.debug(
						"For trip={} stop={} filtering out {} time={} because "
								+ "it is {} seconds from the original time={} which is "
								+ "further away than the allowable={} secs.",
						originalGtfsStopTime == null ? "NA"
								: originalGtfsStopTime.getTripId(),
						originalGtfsStopTime == null ? "NA"
								: originalGtfsStopTime.getStopId(),
						arrivalsOrDepartures, Time.timeOfDayStr(time), time
								- originalScheduleTime, Time
								.timeOfDayStr(originalScheduleTime),
						allowableDifferenceFromOriginalTimeSecs);
			}
		}

		// If no data points left due to filtering then simply return null
		if (filteredTimesFromDb.size() == 0)
			return null;

		// If filtered out any times then need to update timesArray and the mean
		if (timeFilteredOut) {
			results.filteredTimesArray = 
					Statistics.toArray(filteredTimesFromDb);
			results.mean = 
					(float) Statistics.getMean(results.filteredTimesArray);
		} else {
			results.filteredTimesArray = results.unfilteredTimesArray;
			results.mean = (float) mean;
		}

		// Determine the standard deviation using the filtered times
		double doubleArray[] = Statistics
				.toDoubleArray(results.filteredTimesArray);
		results.standardDeviation = (float) Statistics
				.getSampleStandardDeviation(doubleArray, mean);

		// Determine min and max using the filtered times
		for (int time : results.filteredTimesArray) {
			// Deal with min and max
			if (time < results.min)
				results.min = time;
			if (time > results.max)
				results.max = time;
		}

		// Return the results for this trip/stop
		return results;
	}

	/**
	 * Goes through the Collection of Stats objects passed in and determines the
	 * fraction (0.0 - 1.0) that are before the number of standardDeviations
	 * specified before the mean.
	 * 
	 * @param standardDeviations
	 *            The number of standard of deviations to subtract from the mean
	 *            time for determining fraction of arrivals/departures that are
	 *            early.
	 * @param statistics
	 *            Contains the times, the mean, and the standard deviation used
	 *            for the calculations for all of the routes.
	 * @return The fraction of arrival/departure times that are early
	 */
	private static double determineFractionEarly(double standardDeviations,
			Collection<Stats> statistics) {
		int totalTimes = 0;
		int totalEarly = 0;
		for (Stats stats : statistics) {
			if (stats.filteredTimesArray.length >= 2) {
				totalTimes += stats.filteredTimesArray.length;
				for (int time : stats.filteredTimesArray) {
					double allowableTime = stats.mean - 
							stats.standardDeviation	* standardDeviations;
					if (time < allowableTime) {
						++totalEarly;
					}
				}
			}
		}

		// Return result
		return (double) totalEarly / totalTimes;
	}

	/**
	 * For the route specified first determines the basic stats including the
	 * mean and standard deviation. Then determines how many standard deviations
	 * to use for this route such that get approximately the desired fraction of
	 * times being earlier than the mean minus the number of standard deviations
	 * for this trip/stop. Puts the resulting Stats objects that now contain the
	 * best arrival/departure value to use into statsResultsByTripStopMap. This
	 * is done on a per route basis since it is expected that the distribution
	 * of the stats will vary between routes.
	 * 
	 * @param originalGtfsStopTimes
	 *            original data from stop_times.txt file so that can filter out
	 *            outliers. Can be null for testing. Map is keyed on trip/stops
	 *            and contains GtfsStopTimes.
	 * @param timesFromDbByTripStopForRouteSubMap
	 *            the arrival/departure times for the route
	 * @param statsResultsByTripStopMap
	 *            for returning the results
	 * @param routeId
	 *            for logging
	 * @param allowableDifferenceFromMeanSecs
	 *            Specifies how consistent arrival/departure times from Db must
	 *            be in order to be used.
	 * @param allowableDifferenceFromOriginalTimeSecs
	 *            Specifies how consistent arrival/departure times from Db must
	 *            be in order to be used.
	 * @param desiredFractionEarly
	 *            Specifies the desired number of the arrival times that should
	 *            be early
	 * @param arrivalsOrDepartures
	 *            specifies whether should handle as arrivals or departures
	 */
	public static void determineStatsForRoute(
			Map<TripStopKey, GtfsStopTime> originalGtfsStopTimes,
			Map<TripStopKey, List<Integer>> timesFromDbByTripStopForRouteSubMap,
			Map<TripStopKey, Stats> statsResultsByTripStopMap, String routeId,
			int allowableDifferenceFromMeanSecs,
			int allowableDifferenceFromOriginalTimeSecs,
			double desiredFractionEarly,
			ArrivalsOrDepartures arrivalsOrDepartures) {
		logger.debug("Processing {} data for routeId={}", arrivalsOrDepartures,
				routeId);

		// For the route get all of the statistics including the mean and
		// standard deviation for each stop
		Set<TripStopKey> tripStopKeysForRouteFromDb = 
				timesFromDbByTripStopForRouteSubMap.keySet();
		Map<TripStopKey, Stats> statsForRoute = new HashMap<TripStopKey, Stats>(
				tripStopKeysForRouteFromDb.size());
		for (TripStopKey tripStopKey : tripStopKeysForRouteFromDb) {
			List<Integer> timesFromDb = 
					timesFromDbByTripStopForRouteSubMap.get(tripStopKey);
			GtfsStopTime originalGtfsStopTime = originalGtfsStopTimes == null ? 
					null : originalGtfsStopTimes.get(tripStopKey);
			Stats statsForTripStop = ScheduleStatistics
					.getStatisticsForTripStop(timesFromDb,
							originalGtfsStopTime,
							allowableDifferenceFromMeanSecs,
							allowableDifferenceFromOriginalTimeSecs,
							arrivalsOrDepartures);
			if (statsForTripStop != null) {
				statsForRoute.put(tripStopKey, statsForTripStop);
			}
		}

		// Iterate to find the best number of standard deviations to use to
		// get the fraction of. The result of this section of code is that
		// currentStdDevs will be set to the number of standard deviations to
		// use in order to get the desired number of early arrival/departures
		// for the current route.
		double lowStdDevs = 0.0;
		double highStdDevs = 2.0;
		double currentStdDevs = 1.0;
		for (int iteration = 0; iteration < NUMBER_ITERATIONS; ++iteration) {
			double fractionEarly = determineFractionEarly(currentStdDevs,
					statsForRoute.values());
			logger.debug("For iteration={} currentStdDevs={} "
					+ "desiredFractionEarly={} fractionEarly={}", iteration,
					currentStdDevs, desiredFractionEarly,
					StringUtils.threeDigitFormat(fractionEarly));
			if (fractionEarly < desiredFractionEarly) {
				// Need to use lower std dev to get desired results
				highStdDevs = currentStdDevs;
				currentStdDevs = (currentStdDevs + lowStdDevs) / 2.0;
			} else {
				// Need to use higher std dev to get desired results
				lowStdDevs = currentStdDevs;
				currentStdDevs = (currentStdDevs + highStdDevs) / 2.0;
			}
		}
		double standardDeviationsToUse = currentStdDevs;

		// Now that we know how many standard deviations to use to get
		// approximately the desired fraction of early arrival/departures
		// update the Stats object with the best time and put the Stats
		// object into the results map.
		for (TripStopKey tripStopKey : tripStopKeysForRouteFromDb) {
			// The best arrival/departure time value to use is the mean
			// minus the desired standard deviation if the std dev is
			// valid (there was more than 1 data point).
			Stats stats = statsForRoute.get(tripStopKey);

			// If there is no data for the trip/stop then can't process
			// it. This can happen if all the data points were filtered
			// out for the trip/stop.
			if (stats == null)
				continue;

			double bestValue = stats.mean;
			if (!Float.isNaN(stats.standardDeviation))
				bestValue -= stats.standardDeviation * standardDeviationsToUse;
			stats.bestValue = (int) Math.round(bestValue);

			// Add the stats for this trip/stop to the map of stats that is to
			// be returned.
			statsResultsByTripStopMap.put(tripStopKey, stats);
		}
	}

	/**
	 * Determines and logs schedule adherence results both using the original
	 * schedule and using the new improved schedule.
	 * 
	 * @param gtfsStopTimes
	 *            The original GtfsStopTimes read from stop_times.txt.
	 * @param arrivalStatsResultsByTripStopMap
	 *            All the arrival time results. Keyed on trip/stop.
	 * @param departureStatsResultsByTripStopMap
	 *            All the departure time results. Keyed on trip/stop.
	 * @param allowableEarlySecs
	 *            For schedule determining adherence
	 * @param allowableLateSecs
	 *            For schedule determining adherence
	 */
	public static void processScheduleAdherence(
			Map<TripStopKey, GtfsStopTime> gtfsStopTimes,
			Map<TripStopKey, Stats> arrivalStatsResultsByTripStopMap,
			Map<TripStopKey, Stats> departureStatsResultsByTripStopMap,
			int allowableEarlySecs,
			int allowableLateSecs) {
		logger.info("Processing schedule adherence information...");

		// For keeping track of schedule adherence results while iterating
		// through all of the data
		int numberEarlyOrigSchedule = 0;
		int numberLateOrigSchedule = 0;
		int numberEarlyNewSchedule = 0;
		int numberLateNewSchedule = 0;
		int totalDataPoints = 0;

		// Go through list of stop times from the GTFS stop_times.txt file
		// and create corresponding GtfsExtendedStopTimes. Need to know if
		// last stop of trip so that will use arrival time for schedule
		// adherence instead of departure time. Therefore when iterating
		// across all the GtfsStopTime data need to also keep track of the
		// next ones. Therefore iteration is a bit more complicated than usual.
		Iterator<GtfsStopTime> gtfsStopTimesIterator = 
				gtfsStopTimes.values().iterator();
		GtfsStopTime currentGtfsStopTime = null;
		GtfsStopTime nextGtfsStopTime = gtfsStopTimesIterator.next();
		do {
			// Determine previous, current, and next GtfsStopTime so can
			// handle specially first and last stop of trips.
			currentGtfsStopTime = nextGtfsStopTime;
			if (gtfsStopTimesIterator.hasNext())
				nextGtfsStopTime = gtfsStopTimesIterator.next();
			else
				nextGtfsStopTime = null;

			// Determine the stats, the original schedule time, and the new
			// schedule time. If last stop of trip then use arrival time
			// instead of departure time.
			Stats stats;
			Integer originalScheduleTime;
			TripStopKey tripStopKey = ScheduleDataProcessor.getTripStopKey(
					currentGtfsStopTime.getTripId(),
					currentGtfsStopTime.getStopId());
			if (nextGtfsStopTime == null
					|| !currentGtfsStopTime.getTripId().equals(
							nextGtfsStopTime.getTripId())) {
				// Use arrival time because last stop of trip
				stats = arrivalStatsResultsByTripStopMap.get(tripStopKey);
				originalScheduleTime = currentGtfsStopTime.getArrivalTimeSecs();
			} else {
				// Use normal departure time
				stats = departureStatsResultsByTripStopMap.get(tripStopKey);
				originalScheduleTime = 
						currentGtfsStopTime.getDepartureTimeSecs();
			}
			int newScheduleTime = stats != null ? stats.bestValue
					: originalScheduleTime;

			// Keep count of number vehicles early and late for both the
			// old schedule and the new more accurate schedule so can log
			// schedule adherence totals. This way can see how much
			// schedule adherence will be improved using the new more
			// accurate schedule.
			if (stats != null && originalScheduleTime != null) {
				totalDataPoints += stats.unfilteredTimesArray.length;
				for (int time : stats.unfilteredTimesArray) {
					if (time < originalScheduleTime - allowableEarlySecs)
						++numberEarlyOrigSchedule;
					else if (time > originalScheduleTime + allowableLateSecs)
						++numberLateOrigSchedule;
					if (time < newScheduleTime - allowableEarlySecs)
						++numberEarlyNewSchedule;
					else if (time > newScheduleTime + allowableLateSecs)
						++numberLateNewSchedule;
				}
			}
		} while (nextGtfsStopTime != null);

		// Log schedule adherence results for both the original schedule and
		// for the new more accurate schedule.
		double percentageOnTimeOrigSched = 100.0
				* (totalDataPoints - numberEarlyOrigSchedule - numberLateOrigSchedule)
				/ totalDataPoints;
		logger.info("For original schedule time numDataPoints={} early={} "
				+ "late={} percentageOnTime={}", totalDataPoints,
				numberEarlyOrigSchedule, numberLateOrigSchedule,
				StringUtils.twoDigitFormat(percentageOnTimeOrigSched));
		double percentageOnTimeNewSched = 100.0
				* (totalDataPoints - numberEarlyNewSchedule - numberLateNewSchedule)
				/ totalDataPoints;
		logger.info("For new schedule time numDataPoints={} early={} "
				+ "late={} percentageOnTime={}", totalDataPoints,
				numberEarlyNewSchedule, numberLateNewSchedule,
				StringUtils.twoDigitFormat(percentageOnTimeNewSched));
	}

}
