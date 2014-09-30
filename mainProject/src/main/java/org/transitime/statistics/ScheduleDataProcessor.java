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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.configData.DbSetupConfig;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.ArrivalDeparture.ArrivalsOrDepartures;
import org.transitime.gtfs.gtfsStructs.GtfsExtendedStopTime;
import org.transitime.gtfs.gtfsStructs.GtfsFrequency;
import org.transitime.gtfs.gtfsStructs.GtfsStopTime;
import org.transitime.gtfs.readers.GtfsFrequenciesReader;
import org.transitime.gtfs.readers.GtfsStopTimesReader;
import org.transitime.gtfs.writers.GtfsExtendedStopTimesWriter;
import org.transitime.gtfs.writers.GtfsStopTimesWriter;
import org.transitime.statistics.ScheduleStatistics.Stats;
import org.transitime.utils.MapKey;
import org.transitime.utils.Time;

/**
 * For processing arrival/departure times based on AVL data in order to
 * determine more accurate schedule information. The results are output into new
 * stop_times GTFS files. Data is handled on a per trip basis. This means that
 * for regular schedule based systems that run a trip only once per day you need
 * several days of data to get multiple data points. But for frequency based
 * configurations where a trip is run multiple times a day each run of that trip
 * for the day is processed into a single value.
 * <p>
 * This class also processes schedule adherence information so one can determine
 * what the schedule adherence was with the old stop_times and what it would be
 * with the new ones. This allows one to see directly what kind of improvement
 * using the new stop_times will provide. The schedule adherence information is
 * included in the log file, not in the new stop_times files.
 * <p>
 * The results are output into the original GTFS directory as two new stop times
 * files: stop_times.txt_new and stop_times.txt_extended. Note that the original
 * stop_times.txt file will not be overwritten. If you want to use the new
 * stop_times.txt_new file you need to change its name to stop_times.txt,
 * thereby overwriting the old file.
 * <p>
 * The stop_times.txt_new file has exactly the same format as the standard
 * stop_times.txt file. The stop_times.txt_extended file contains the standard
 * data but also adds some very useful columns including the original stop time
 * so you can see how much it is being changed, the min and max arrival &
 * departure times, the standard deviation so you can see the distribution of
 * times, and the number of data points so you can see how many trips were used
 * to generate the values.
 * <p>
 * The order of the rows in the new stop_times files will not necessarily be the
 * same as the original stop_times file. If the ordering of the original
 * stop_times file is adequate such that trips are grouped together and that the
 * stop_sequence increases within the trip, then the new stop_times files can
 * have the same order. But if there are issues with the ordering of the data in
 * the original stop_times file, which is somewhat common, then the data is
 * first sorted so that can determine the first stops of trips, which is
 * important for the GTFS data is frequency based. This leads to a different
 * ordering for the stop_times.txt_new and stop_times.txt_extended files.
 * <p>
 * To process the data this class reads in arrival and departure data from the
 * database. It batch reads the data 500,000 datapoints at a time, a value
 * chosen to make db reading quick (want a high number) without using too much
 * heap memory at once (want a low number). The arrivals and departures data is
 * read into maps
 * <code>Map&ltString, Map&ltTripStopKey, List&ltInteger&gt&gt&gt</code> using
 * <code>readInArrivalsOrDeparturesFromDb()</code>. The map is keyed on routeId
 * so that can handle each route separately (though this isn't truly needed).
 * The data is simply stored as Integers indicating the time of day of the
 * arrival or departure. Once this data is determined the ArrivalDeparture
 * object is not needed anymore and can be garbage collected. When reading in
 * departures it also puts the trip departure times into
 * departureTimesFromTerminalMap so that can determine elapsed time for when
 * frequency based trips are used.
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
 * The results are then output into the stop_times.txt_new and
 * stop_times.txt_extended files described above.
 * 
 * @author SkiBu Smith
 * 
 */
public class ScheduleDataProcessor {

	// To specify where to read and write the GTFS data
	private final String gtfsDirectoryName;
	// Tells the db query the time range to use
	private final Date beginTime;
	private final Date endTime;
	// So can convert from between epoch times and seconds into day
	private final Time timeForUsingCalendar;
	// Specifies the desired number of the arrival times that should be early
	private final double desiredFractionEarly;
	// So can not use the more accurate time for the first stop of a trip.
	// This can be important if there will be only one schedule for both
	// drivers and passengers.
	private final boolean doNotUpdateFirstStopOfTrip;

	// Specifies how consistent arrival/departure times from Db must be
	// in order to be used.
	private final int allowableDifferenceFromMeanSecs;
	private final int allowableDifferenceFromOriginalTimeSecs;

	// For schedule adherence
	private final int allowableEarlySecs;
	private final int allowableLateSecs;

	// The GTFS stop_times.txt data. Keyed by trip/stop.
	private final Map<TripStopKey, GtfsStopTime> gtfsStopTimes;

	// The GTFS frequencies.txt info. Keyed on tripId. For keeping track
	// of which trips are configured to be frequency based such that the
	// arrival/departure time has to be converted to seconds since
	// configured beginning of trip.
	private final Set<String> gtfsFrequencyBasedTrips;

	private final Map<TerminalDeparturesKey, Integer> 
		departureTimesFromTerminalMap = 
			new HashMap<TerminalDeparturesKey, Integer>();

	private static final Logger logger = 
			LoggerFactory.getLogger(ScheduleDataProcessor.class);

	/**
	 * Special MapKey class so that can make sure using the proper key for the
	 * several maps in this class.
	 */
	public static class TripStopKey extends MapKey {
		private TripStopKey(String tripId, String stopId) {
			super(tripId, stopId);
		}

		@Override
		public String toString() {
			return "TripStopKey [" + "tripId=" + o1 + ", stopId=" + o2 + "]";
		}
		
		public String getTripId() {
			return (String) o1;
		}
		
		public String getStopId() {
			return (String) o2;
		}
	}

	/**
	 * Special MapKey class so that can make sure using the proper key for the
	 * departureTimesFromTerminalMap map in this class. Needs vehicleId because
	 * multiple vehicles could be assigned to the same block. Need dayOfYear so
	 * can handle arrival/departure data for multiple days.
	 */
	public static class TerminalDeparturesKey extends MapKey {
		private TerminalDeparturesKey(String vehicleId, String blockId,
				int dayOfYear) {
			super(vehicleId, blockId, dayOfYear);
		}

		@Override
		public String toString() {
			return "TerminalDeparturesKey [" + "vehicleId=" + o1 + ", blockId="
					+ o2 + ", dayOfYear=" + o3 + "]";
		}
	}

	/********************** Member Functions **************************/

	/**
	 * 
	 * @param gtfsDirectoryName
	 * @param beginTime
	 * @param endTime
	 * @param timeForUsingCalendar
	 * @param desiredFractionEarly
	 *            how many arrival/departures should be early
	 * @param allowableDifferenceFromMeanSecs
	 * @param allowableDifferenceFromOriginalTimeSecs
	 * @param doNotUpdateFirstStopOfTrip
	 * @param allowableEarlySecs
	 * @param allowableLateSecs
	 */
	public ScheduleDataProcessor(String gtfsDirectoryName,
			Date beginTime, Date endTime, Time timeForUsingCalendar,
			double desiredFractionEarly, int allowableDifferenceFromMeanSecs,
			int allowableDifferenceFromOriginalTimeSecs,
			boolean doNotUpdateFirstStopOfTrip, int allowableEarlySecs,
			int allowableLateSecs) {
		this.gtfsDirectoryName = gtfsDirectoryName;
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.timeForUsingCalendar = timeForUsingCalendar;
		this.desiredFractionEarly = desiredFractionEarly;
		this.allowableDifferenceFromMeanSecs = allowableDifferenceFromMeanSecs;
		this.allowableDifferenceFromOriginalTimeSecs = 
				allowableDifferenceFromOriginalTimeSecs;
		this.doNotUpdateFirstStopOfTrip = doNotUpdateFirstStopOfTrip;
		this.allowableEarlySecs = allowableEarlySecs;
		this.allowableLateSecs = allowableLateSecs;

		this.gtfsStopTimes = getGtfsStopTimes(gtfsDirectoryName);
		this.gtfsFrequencyBasedTrips = 
				getFrequencyBasedTrips(gtfsDirectoryName);
	}

	/**
	 * Reads in GTFS stop_times.txt file from the GTFS directory specified by
	 * the gtfsDirectoryName command line option. The map is actually an ordered
	 * one so that can both look up gtfsStopTimes by trip & stop so that can do
	 * proper filtered of data from db yet still output the the stop_times.txt
	 * data in the same order as the original file.
	 * 
	 * @param gtfsDirectoryName
	 *            Where to find the GTFS stop_times.txt file
	 * @return Ordered map of the GtfsStopTimes. Keyed on tripId/stopId.
	 */
	private static Map<TripStopKey, GtfsStopTime> getGtfsStopTimes(
			String gtfsDirectoryName) {
		// Read in stop_times.txt file
		logger.info("Reading in original stop_times.txt file...");

		// Get list of stop_times, but these can be a bit out of
		// order with respect to stop sequence within a trip.
		GtfsStopTimesReader stopTimesReader = 
				new GtfsStopTimesReader(gtfsDirectoryName);
		List<GtfsStopTime> gtfsStopTimesList = stopTimesReader.get(100000);

		// Determine if any of the stop times are out of order. If any are
		// then will sort the list. This is necessary because to know which
		// is the first stop of a trip because it is treated differently.
		// Plus really want the stop times in order because it makes it easier
		// to understand the data in the resulting stop_times.txt file. But
		// don't want to sort and change the ordering if the trips are already
		// together and the stop sequences are in order. This way the new
		// stop_times file can be in the same order as the original one,
		// as long as the original doesn't have problems with its order.
		Set<String> tripIdsInvestigated = new HashSet<String>();
		boolean orderProblem = false;
		for (int i = 1; i < gtfsStopTimesList.size(); ++i) {
			GtfsStopTime current = gtfsStopTimesList.get(i);
			GtfsStopTime previous = gtfsStopTimesList.get(i - 1);
			boolean tripAlreadyDealtWith = 
					tripIdsInvestigated.contains(current.getTripId());
			if (tripAlreadyDealtWith) {
				// Already have encountered this trip so make sure the
				// new stop time is in proper order.
				if (!current.getTripId().equals(previous.getTripId())
						|| current.getStopSequence() < previous
								.getStopSequence()) {
					orderProblem = true;
					break;
				}
			} else {
				// Its first stop time of trip so order is OK.
				// Record that have dealt with this trip ID.
				tripIdsInvestigated.add(current.getTripId());
			}
		}

		if (orderProblem) {
			// Sort the list so that the trips are grouped together and so that
			// the stop sequences for the trips are in order. This means that
			// when the stop_times are written out they will be in a different
			// order than originally.
			Collections.sort(gtfsStopTimesList, new Comparator<GtfsStopTime>() {
				@Override
				public int compare(GtfsStopTime arg0, GtfsStopTime arg1) {
					int tripCompare = arg0.getTripId().compareTo(
							arg1.getTripId());
					if (tripCompare != 0)
						return tripCompare;

					// Trip IDs are the same
					if (arg0.getStopSequence() == arg1.getStopSequence()) {
						return 0;
					}
					return arg0.getStopSequence() < arg1.getStopSequence() ? 
							-1 : 1;
				}
			});
		}

		// Create the ordered map to be returned
		Map<TripStopKey, GtfsStopTime> gtfsStopTimesMap = 
				new LinkedHashMap<TripStopKey, GtfsStopTime>(
						gtfsStopTimesList.size());
		for (GtfsStopTime gtfsStopTime : gtfsStopTimesList) {
			TripStopKey key = getTripStopKey(gtfsStopTime.getTripId(),
					gtfsStopTime.getStopId());
			gtfsStopTimesMap.put(key, gtfsStopTime);
		}

		return gtfsStopTimesMap;
	}

	/**
	 * Creates set containing the trip ID of all trips that are defined in the
	 * GTFS frequencies.txt file.
	 * 
	 * @param gtfsDirectoryName
	 *            Where to find the GTFS frequencies.txt file
	 * @return Set of all trips that are frequency based.
	 */
	private static Set<String> getFrequencyBasedTrips(String gtfsDirectoryName) {
		// Read in frequencies.txt file
		logger.info("Reading in frequencies.txt file...");

		GtfsFrequenciesReader frequenciesReader = 
				new GtfsFrequenciesReader(gtfsDirectoryName);
		List<GtfsFrequency> gtfsFrequencies = frequenciesReader.get();

		Set<String> gtfsFrequencyTrips = new HashSet<String>();
		for (GtfsFrequency gtfsFrequency : gtfsFrequencies) {
			gtfsFrequencyTrips.add(gtfsFrequency.getTripId());
		}

		return gtfsFrequencyTrips;
	};

	/**
	 * Returns trip if trip specified is frequency based as defined by the GTFS
	 * frequencies.txt file.
	 * 
	 * @param tripId
	 * @return
	 */
	private boolean isTripFrequencyBased(String tripId) {
		return gtfsFrequencyBasedTrips.contains(tripId);
	}

	/**
	 * For use in the sub-maps of arrivalTimesFromDbByRouteByTripStopMap and
	 * departureTimesFromDbByRouteByTripStopMap.
	 * 
	 * The key is simply tripId + stopId. Previously used tripId + "=" + stopId
	 * but trying to make things as efficient as possible. This might not
	 * actually be a good idea since it could make debugging a bit more
	 * difficult.
	 * 
	 * @param tripId
	 * @param stopId
	 * @return
	 */
	public static TripStopKey getTripStopKey(String tripId, String stopId) {
		// Return the key. Use intern() on the strings because will be often
		// repeating trips and stops and don't need many copies of each one.
		return new TripStopKey(tripId.intern(), stopId.intern());
	}

	/**
	 * For adding a new arrival/departure time to either
	 * arrivalTimesFromDbByRouteByTripStopMap or
	 * departureTimesFromDbByRouteByTripStopMap that is passed in.
	 * 
	 * @param timesByTripMap
	 *            map keyed on routeId containing a sub-map
	 * @param ad
	 */
	private void addArrivalDepartureToMap(
			Map<String, Map<TripStopKey, List<Integer>>> timesByTripMap,
			ArrivalDeparture ad) {
		// Get the existing sub map for the routeId
		String routeKey = ad.getRouteId();
		Map<TripStopKey, List<Integer>> timesByTripSubMap = 
				timesByTripMap.get(routeKey);

		// If the sub map for the routeId not created yet, then create it
		if (timesByTripSubMap == null) {
			timesByTripSubMap = new HashMap<TripStopKey, List<Integer>>();
			timesByTripMap.put(routeKey, timesByTripSubMap);
		}

		// Add the new arrival/departure to the sub map
		addArrivalDepartureToSubMap(timesByTripSubMap, ad);
	}

	/**
	 * Adds the arrival/departure time to the sub-map that actually contains the
	 * list of arrival/departure times.
	 * <p>
	 * For frequency based trips need the initial departure time for the trip.
	 * If that time is not available for a frequency based trip then this
	 * ArrivalDeparture will be ignored.
	 * 
	 * @param timesByTripMap
	 * @param ad
	 */
	private void addArrivalDepartureToSubMap(
			Map<TripStopKey, List<Integer>> timesByTripMap, 
			ArrivalDeparture ad) {
		// Get the existing list of times for the trip/stop
		TripStopKey key = getTripStopKey(ad.getTripId(), ad.getStopId());
		int arrDepSecsIntoDay = 
				timeForUsingCalendar.getSecondsIntoDay(ad.getDate());

		// If frequency based then need to subtract trip start time
		int timeWithRespectToTripStart = arrDepSecsIntoDay;
		if (isTripFrequencyBased(ad.getTripId())) {
			Integer terminalDepartureTimeSecs = getTerminalDepartureTime(ad);
			if (terminalDepartureTimeSecs == null)
				return;

			// If the trip start time is after the arrival/departure time
			// then we definitely have a problem. It most likely means that
			// got multiple arrival/departures for particular vehicle/trip/stop.
			// Ignore such data since there is definitely a problem.
			if (terminalDepartureTimeSecs > arrDepSecsIntoDay) {
				logger.error("Got an arrival or departure before the start "
						+ "of the trip. This of course indicates a problem. "
						+ "Therefore this arrival/departure won't be used as "
						+ "part of the stats. Trip start time={}. {}",
						Time.timeOfDayStr(terminalDepartureTimeSecs), ad);
				// Ignore this data point
				return;
			}

			// Subtract the terminal departure time. Initially simply used
			// ArrivalDeparture.getTripStartTime() but that is the time vehicle
			// would leave if exactly following the frequency exact time of
			// departure. But that of course is not nearly as good as using the
			// measured departure time for the terminal.
			timeWithRespectToTripStart -= terminalDepartureTimeSecs;

		}

		// If list of times for the trip/stop doesn't exist, yet create it
		List<Integer> times = timesByTripMap.get(key);
		if (times == null) {
			// Create the array that the times go into. Initialize size of list
			// to 5 instead of the default of 10 so that it uses less memory.
			// 5 is ideal because it is smaller but if process two weeks of
			// data then it will be expanded to 10 for the 10 weekdays. If
			// would use 4 then it would be expanded to 8 and then 16, actually
			// taking up more space than if the default size of 10 were used.
			times = new ArrayList<Integer>(5);
			timesByTripMap.put(key, times);
		}

		// Add the new time to the list for the trip/stop
		times.add(timeWithRespectToTripStart);
	}

	/**
	 * Determines based on the ArrivalDeparture parameter the appropriate key to
	 * use for the departureTimesFromTerminalMap.
	 * <p>
	 * Note: this does not work if trip spans midnight. This isn't important for
	 * now but could be if have frequencies based trips that do span midnight.
	 * 
	 * @param arrDep
	 * @return
	 */
	private TerminalDeparturesKey getTerminalDeparturesKey(
			ArrivalDeparture arrDep) {
		return new TerminalDeparturesKey(arrDep.getVehicleId().intern(), arrDep
				.getBlockId().intern(),
				timeForUsingCalendar.getDayOfYear(arrDep.getDate()));
	}

	/**
	 * If handling departures and this trip is defined as a frequency then need
	 * store the departure time of the first stop in the trip in a map so can
	 * use it for determine the arrival/departure times relative to the start of
	 * the trip.
	 * 
	 * @param arrDep
	 */
	private void handleMapOfTerminalStartTimes(ArrivalDeparture arrDep) {
		// If it is a departure from a terminal for a frequency based...
		if (arrDep.isDeparture() && arrDep.getStopPathIndex() == 0
				&& isTripFrequencyBased(arrDep.getTripId())) {
			// It is terminal departure time so add it to map
			int departureTimeSecsIntoDay = 
					timeForUsingCalendar.getSecondsIntoDay(arrDep.getDate());
			TerminalDeparturesKey mapKey = getTerminalDeparturesKey(arrDep);
			departureTimesFromTerminalMap.put(mapKey, departureTimeSecsIntoDay);
		}
	}

	/**
	 * Returns the start time of the departure from the terminal for the trip
	 * associated with the ArrivalDeparture.
	 * 
	 * @param arrDep
	 *            Specifies the trip that should get terminal departure time
	 *            for.
	 * @return Departure of the trip specified by arrDep parameter
	 */
	private Integer getTerminalDepartureTime(ArrivalDeparture arrDep) {
		TerminalDeparturesKey mapKey = getTerminalDeparturesKey(arrDep);
		return departureTimesFromTerminalMap.get(mapKey);
	}

	/**
	 * Reads in the arrival/departure times from the db and returns a map
	 * containing just the arrival/departure times.
	 * departureTimesFromDbByRouteByTripStopMap. Reads in data in batches
	 * because that is more memory efficient than reading in everything at once
	 * when there is a large amount of data. Also, batching is much quicker than
	 * using the iterator method since that does a separate query for each row.
	 * Found a batch size of at least 50k is much more efficient than 10k by
	 * about a factor of 2.
	 * 
	 * @param arrivalOrDeparture
	 *            Specifies whether should read in arrivals or, instead,
	 *            departures.
	 * @return Big map keyed on route of data. The sub-map is keyed on on a
	 *         TripStopKey using tripId+stopId and contains list of all of the
	 *         times (arrivals or departures) for that trip/stop for the route.
	 *         The times are seconds into the day. If frequencies are being used
	 *         then the times are relative to the trip start time, because that
	 *         is how frequency based data is specified in the stop_times.txt
	 *         file.
	 */
	private Map<String, Map<TripStopKey, List<Integer>>> 
		readInArrivalsOrDeparturesFromDb(
			ArrivalsOrDepartures arrivalOrDeparture) {
		logger.info("Reading {} from db for dbName={} for beginDate={} "
				+ "and endDate={}", arrivalOrDeparture, DbSetupConfig.getDbName(), 
				beginTime, endTime);

		Map<String, Map<TripStopKey, List<Integer>>> 
			arrivalDeparatureTimesFromDbByRouteByTripStopMap = 
				new HashMap<String, Map<TripStopKey, List<Integer>>>();

		// Go through all the arrival/departure data and put it into a map
		// that just keeps track of arrival/departure times for trip/stops.
		try {
			// Use two levels of batching to be efficient. At the low level
			// only read in 50k rows at a time so that never read in too much
			// at once. This way won't run out of memory. But this type of
			// batching where we specify a firstResult and a batchSize can be
			// inefficient when firstResult becomes large because the database
			// still has to process a huge amount of data in order to return
			// the proper batch. So also just dealing with a day at a time by
			// dividing the beginTime and endTime into 1 day chunks.
			long batchBeginTime = beginTime.getTime();
			long batchEndTime = beginTime.getTime() + Time.MS_PER_DAY;
			while (batchBeginTime < endTime.getTime()) {
				logger.info("Reading in a days worth of {} data "
						+ "for between {} and {}", arrivalOrDeparture,
						new Date(batchBeginTime), new Date(batchEndTime));

				// For keeping track of which rows should be returned by the
				// batch.
				int firstResult = 0;
				// Batch size of 50k found to be significantly faster than 10k,
				// by about a factor of 2. So want to use as large a value as
				// possible without running out of memory. Found that with
				// 500,000
				// can still read in all data with default heap size of 1G.
				// Batch size of 650,000 seems to complete halt the process with
				// only 1G of heap.
				int batchSize = 500000; // Also known as maxResults
				// The temporary list for the loop that contains a batch of
				// results
				List<ArrivalDeparture> arrDepBatchList;
				// Read in batch of 50k rows of data and process it
				do {
					// Note: I tried adding a "ORDER BY time" clause to see
					// if that would speed things up when doing multiple batches
					// but it only served to slow things down.
					arrDepBatchList = ArrivalDeparture
							.getArrivalsDeparturesFromDb(new Date(
									batchBeginTime), new Date(batchEndTime),
									null, // SQL clause
									firstResult, batchSize, arrivalOrDeparture);

					for (ArrivalDeparture arrDep : arrDepBatchList) {
						// If handling departures and this trip is defined as a
						// frequency then need store the departure time of the
						// first stop in the trip in a map so can use it for
						// determine the arrival/departure times relative to the
						// start of the trip.
						handleMapOfTerminalStartTimes(arrDep);

						// Add arrival/departure time to appropriate map
						addArrivalDepartureToMap(
								arrivalDeparatureTimesFromDbByRouteByTripStopMap,
								arrDep);
					}

					logger.info("Read in total of {} {}", firstResult
							+ arrDepBatchList.size(), arrivalOrDeparture);

					// Update firstResult for reading next batch of data
					firstResult += batchSize;
				} while (arrDepBatchList.size() == batchSize);

				// Get ready to read in chunk of data for the next day
				batchBeginTime += Time.MS_PER_DAY;
				batchEndTime += Time.MS_PER_DAY;
				if (batchEndTime > endTime.getTime())
					batchEndTime = endTime.getTime();
			}
		} catch (Exception e) {
			logger.error("Exception occurred when reading arrivals/departure "
					+ "from db", e);
		}

		return arrivalDeparatureTimesFromDbByRouteByTripStopMap;
	}

	/**
	 * Reads in times from database and fills in statsResultsByTripStopMap by
	 * determining the best arrival/departure time to use. Does this on a per
	 * route basis.
	 * 
	 * @param originalGtfsStopTimes
	 *            original data from stop_times.txt file so that can filter out
	 *            outliers. Can be null for testing. Map is keyed on trip/stops
	 *            and contains GtfsStopTimes.
	 * @param arrivalsOrDepartures
	 *            specifies whether should handle as arrivals or departures
	 * @return map containing stats for each trip/stop for all routes
	 */
	private Map<TripStopKey, Stats> determineStatsForRoutes(
			Map<TripStopKey, GtfsStopTime> originalGtfsStopTimes,
			ArrivalsOrDepartures arrivalsOrDepartures) {
		// For returning results
		Map<TripStopKey, Stats> statsResultsByTripStopMap = 
				new HashMap<TripStopKey, Stats>();

		// Read the arrival/departure times from the db
		Map<String, Map<TripStopKey, List<Integer>>>
			timesFromDbByRoutesByTripStopMap = 
				readInArrivalsOrDeparturesFromDb(arrivalsOrDepartures);

		// Handle the arrival/departure times for each route
		Set<String> routeIds = timesFromDbByRoutesByTripStopMap.keySet();
		for (String routeId : routeIds) {
			Map<TripStopKey, List<Integer>> timesByTripStopForRouteSubMap = 
					timesFromDbByRoutesByTripStopMap.get(routeId);
			ScheduleStatistics.determineStatsForRoute(originalGtfsStopTimes,
					timesByTripStopForRouteSubMap, statsResultsByTripStopMap,
					routeId, allowableDifferenceFromMeanSecs,
					allowableDifferenceFromOriginalTimeSecs,
					desiredFractionEarly, arrivalsOrDepartures);
		}

		return statsResultsByTripStopMap;
	}

	/**
	 * Takes in the processed arrival and departure information and writes out
	 * the optimized schedule in new stop_times.txt_new and
	 * stop_times.txt_extended files into the same GTFS directory that the
	 * original GTFS data was read from.
	 * 
	 * @param arrivalStatsResultsByTripStopMap
	 *            The statistical results for the arrivals
	 * @param departureStatsResultsByTripStopMap
	 *            The statistical results for the arrivals
	 */
	private void writeNewGtfsStopTimesFiles(
			Map<TripStopKey, Stats> arrivalStatsResultsByTripStopMap,
			Map<TripStopKey, Stats> departureStatsResultsByTripStopMap) {
		// For writing the stop_times.txt_extended file with the additional data
		String extendedFileName = gtfsDirectoryName + "/"
				+ "stop_times.txt_extended";
		logger.info("Creating the new GTFS stop_times file {} ...",
				extendedFileName);
		GtfsExtendedStopTimesWriter extendedWriter = 
				new GtfsExtendedStopTimesWriter(extendedFileName);

		// Write the new stop_times.txt_new data
		String newFileName = gtfsDirectoryName + "/" + "stop_times.txt_new";
		logger.info("Creating the new GTFS stop_times file {} ...", newFileName);
		GtfsStopTimesWriter writer = new GtfsStopTimesWriter(newFileName);

		// Go through list of stop times from the GTFS stop_times.txt file
		// and create corresponding GtfsExtendedStopTimes. Need to determine
		// if first stop of trip to know if should actually modify the schedule
		// time. Therefore keeping track of previousTripId so can determine
		// when iterating over a new trip.
		logger.info("Creating GtfsExtendedStopTimes objects for all data "
				+ "from stop_times.txt file.");
		String previousTripId = null;
		for (GtfsStopTime gtfsStopTime : gtfsStopTimes.values()) {
			// Determine values to use for both arrival and departure times. The
			// Stats will be null if there was no data for the particular
			// trip/stop.
			TripStopKey tripStopKey = getTripStopKey(gtfsStopTime.getTripId(),
					gtfsStopTime.getStopId());
			Stats arrivalTimeResults = arrivalStatsResultsByTripStopMap
					.get(tripStopKey);
			Stats departureTimeResults = departureStatsResultsByTripStopMap
					.get(tripStopKey);

			// Determine if should use original arrival/departure time for
			// this stop/trip. This could be true for first stop of trip
			// where don't want to modify the scheduled departure time
			// just because drivers aren't leaving on time.
			boolean useOriginalSchedTimes = false;
			if (doNotUpdateFirstStopOfTrip) {
				// If at first stop of new trip
				if (!gtfsStopTime.getTripId().equals(previousTripId)) {
					useOriginalSchedTimes = true;
				}
			}
			previousTripId = gtfsStopTime.getTripId();

			// Create the GtfsExtendedStopTime that corresponds to the
			// GtfsStopTime from the stop_times.txt file. These values are
			// used to write the new version of the stop_times file with
			// the more accurate schedule times.
			GtfsExtendedStopTime extendedStopTime = new GtfsExtendedStopTime(
					gtfsStopTime, useOriginalSchedTimes, arrivalTimeResults,
					departureTimeResults);

			// Write the data to the stop_times files
			extendedWriter.write(extendedStopTime);
			writer.write(extendedStopTime);
		}

		// Finish up the writing of the stop_times files
		extendedWriter.close();
		writer.close();		
	}

	/**
	 * Reads original stop_times.txt file, reads in arrival/departures from the
	 * database, processes the arrival/departure info to determine more accurate
	 * schedule times, and writes the results to new stop_times files.
	 * <p>
	 * For each trip/stop in the stop_times.txt file sees if there is AVL based
	 * arrival/departure times. If there is then it is used when creating
	 * GtfsExtendedStopTime object. If no data for the trip/stop then null
	 * values will be used. The result GtfsExtendedStopTimes are then written to
	 * two files: - stop_times.txt_new which uses the standard GTFS format -
	 * stop_times.txt_extended which has additional info such as standard
	 * deviation.
	 */
	public void process() {
		// Determine the more accurate schedule times
		logger.info("Processing the arrival/departure times to determine "
				+ "the more accurate schedule times and then writing the "
				+ "new stop_times files...");
		
		// Determine the arrival/departure times to use by
		// doing statistical analysis. Puts results into
		// arrivalStatsResultsByTripStopMap and
		// departureStatsResultsByTripStopMap parameters.
		// Need to process departures first because for when frequencies
		// used need to determine times relative to the start time
		// of the trip, which is the departure from the terminal.
		Map<TripStopKey, Stats> departureStatsResultsByTripStopMap = 
				determineStatsForRoutes(gtfsStopTimes, 
						ArrivalsOrDepartures.DEPARTURES);
		Map<TripStopKey, Stats> arrivalStatsResultsByTripStopMap = 
				determineStatsForRoutes(gtfsStopTimes, 
						ArrivalsOrDepartures.ARRIVALS);

		// Write the results to the new GTFS stop_times files
		writeNewGtfsStopTimesFiles(arrivalStatsResultsByTripStopMap,
				departureStatsResultsByTripStopMap);
		
		// Log schedule adherence results for both the original schedule and
		// for the new more accurate schedule.
		ScheduleStatistics.processScheduleAdherence(gtfsStopTimes,
				arrivalStatsResultsByTripStopMap,
				departureStatsResultsByTripStopMap, allowableEarlySecs,
				allowableLateSecs);

		// Log that done so can see how long it took
		logger.info("Done creating new GTFS stop_times files.");
	}

}
