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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.DoubleConfigValue;
import org.transitclock.core.TemporalDifference;
import org.transitclock.core.travelTimes.DataFetcher.DbDataMapKey;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Match;
import org.transitclock.db.structs.StopPath;
import org.transitclock.db.structs.Trip;
import org.transitclock.monitoring.MonitoringService;
import org.transitclock.statistics.Statistics;
import org.transitclock.utils.Geo;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.MapKey;
import org.transitclock.utils.StringUtils;
import org.transitclock.utils.Time;

import com.amazonaws.services.importexport.model.InvalidParameterException;

/**
 * Takes arrival/departure times plus the matches (where vehicle is matched to a
 * route between stops) that are read from db and processes the data into
 * average travel and dwell times and stores the data into maps. The travel
 * times can then be written to the database.
 * <p>
 * The data is processed on a per tripId basis. With GTFS trips are unique
 * across service IDs and days of the week so this is adequate. But at some
 * point really might want to divide up the data by day of the week in order to
 * get greater accuracy (assuming that buses might consistently travel
 * differently on Monday compared to Friday even though they have the same
 * service ID.
 *
 * @author SkiBu Smith
 *
 */
public class TravelTimesProcessor {


	// Used when determining stop time for first stop of trip. A value
	// of 1.0 means that will use a stop time that is within 1.0 
	// standard deviation of the mean or higher, which is,
	// 68% + (100% - 68%)/2 = 84%, meaning that 84% of the time the 
	// vehicle would leave after the calculated stop time.
	private final static double STD_DEV_BIAS_FOR_FIRST_STOP = 1.5;
	
	// For first stop of trip should add a bit of a bias since passengers 
	// have to get on a few seconds before doors shut and vehicle starts 
	// moving. Time is in msec.
	private final static int STOP_TIME_BIAS_FOR_FIRST_STOP = 10000;
	
	// For determining stop time for first stop in trip. Need to limit it 
	// because if vehicle is really late then perhaps it is matched to
	// wrong trip or such. At the very minimum it is an anomaly. In such a 
	// case the data would only skew the stop time.
	public final static int MAX_SCHED_ADH_FOR_FIRST_STOP_TIME =
			10*Time.MS_PER_MIN;
	
	private final static int MAX_SCHED_ADH_SECS =
			30*Time.SEC_PER_MIN;
	
	public static boolean shouldResetEarlyTerminalDepartures() {
		return resetEarlyTerminalDepartures.getValue();
	}
	
	// For when determining stop times. Throws out
	// outliers if they are less than 0.7 or greater than 1/0.7
	// of the average.	 
	private static DoubleConfigValue fractionLimitForStopTimes = new DoubleConfigValue("transitclock.traveltimes.fractionLimitForStopTimes",
			0.7,
			"For when determining stop times. Throws out outliers.");
	
	
	// For when determining travel times for segments. Throws out
	// outliers if they are less than 0.7 or greater than 1/0.7
	// of the average.
	private static DoubleConfigValue fractionLimitForTravelTimes = new DoubleConfigValue("transitclock.traveltimes.fractionLimitForTravelTimes",
			0.7,
			"For when determining travel times. Throws out outliers.");
	
	private static BooleanConfigValue resetEarlyTerminalDepartures =
			new BooleanConfigValue("transitclock.travelTimes.resetEarlyTerminalDepartures",
					true,
					"For some agencies vehicles won't be departing terminal "
					+ "early. If an early departure is detected for such an "
					+ "agency then will use the schedule time since the "
					+ "arrival time is likely a mistake.");
	
	private static double getMaxTravelTimeSegmentLength() {
		return maxTravelTimeSegmentLength.getValue();
	}
	private static DoubleConfigValue maxTravelTimeSegmentLength =
			new DoubleConfigValue("transitclock.traveltimes.maxTravelTimeSegmentLength",
					250.0,
					"The longest a travel time segment can be. If a stop path "
					+ "is longer than this distance then it will be divided "
					+ "into multiple travel time segments of even length.");

	private static double getMinSegmentSpeedMps() {
		return minSegmentSpeedMps.getValue();
	}
	private static DoubleConfigValue minSegmentSpeedMps =
			new DoubleConfigValue("transitclock.traveltimes.minSegmentSpeedMps",
					0.0,
					"If a travel time segment is determined to have a lower "
					+ "speed than this value in meters/sec then the travel time"
					+ " will be increased to meet this limit. Purpose is to "
					+ "make sure that don't get invalid travel times due to "
					+ "bad data.");
	
	private static DoubleConfigValue maxSegmentSpeedMps =
			new DoubleConfigValue("transitclock.traveltimes.maxSegmentSpeedMps",
					27.0, // 27.0m/s = 60mph
					"If a travel time segment is determined to have a higher "
					+ "speed than this value in meters/second then the travel "
					+ "time will be decreased to meet this limit. Purpose is "
					+ "to make sure that don't get invalid travel times due to "
					+ "bad data.");
	
	// The aggregate data processed from the historic db data.
	// ProcessedDataMapKey combines tripId and stopPathIndex in 
	// order to combine data for a particular tripId and stopPathIndex.
	// stopTimesMap contains data for each trip on how long vehicle was stopped
	// for at a particular stop. It is obtained by comparing the arrival time
	// with the departure time for each stop for each trip. There is one
	// entry per data point, hence a List of Integers with one Integer
	// per data point.
	private static Map<ProcessedDataMapKey, List<Integer>> stopTimesMap = 
			new HashMap<ProcessedDataMapKey, List<Integer>>();	
	// Values are List of List of times where outer List is by single trip and
	// inner List is by travel time segment. For every trip that has historical
	// data we get a single entry in the outer List. For every travel time
	// segment we have historical data for we get an entry in the inner List.
	private static Map<ProcessedDataMapKey, List<List<Integer>>> travelTimesMap =
			new HashMap<ProcessedDataMapKey, List<List<Integer>>>();

	private static final Logger logger = 
			LoggerFactory.getLogger(TravelTimesProcessor.class);

	private MonitoringService monitoringService;

  private boolean isEmpty = true;
	
  public boolean isEmpty() {
    return isEmpty;
  }
  
	public TravelTimesProcessor() {
    monitoringService = MonitoringService.getInstance();
	}
	
	/********************** Member Functions **************************/

	/**
	 * Special MapKey class so that can make sure using the proper one for the
	 * associated maps in this class. The key is made up of the tripId, the
	 * stopPathIndex, and the stopId. The stopId is included so that can see if
	 * the historic data stopPathIndex and stopId match for the currently
	 * configured trip. This is important for making sure that don't use
	 * historic data for a stopPathIndex when stops have been removed from or
	 * added to the trip.
	 */
	public static class ProcessedDataMapKey extends MapKey {
		public ProcessedDataMapKey(String tripId, int stopPathIndex,
				String stopId) {
			super(tripId, stopPathIndex, stopId);
		}
		
		private String getTripId() {
			return (String) o1;
		}
		
		private int getStopPathIndex() {
			return (int) o2;
		}

		private String getStopId() {
			return (String) o3;
		}
		
		@Override
		public String toString() {
			return "ProcessedDataMapKey [" 
					+ "tripId=" + o1 
					+ ", stopPathIndex=" + o2 
					+ ", stopId=" + o3 + "]";
		}
	}

	private static ProcessedDataMapKey getKey(String tripId, int stopPathIndex,
			String stopId) {
		return new ProcessedDataMapKey(tripId, stopPathIndex, stopId);
	}

	/**
	 * Adds stop times for a stop path for a single trip to the stopTimesMap.
	 * 
	 * @param mapKey
	 * @param stopTimeMsec
	 */
	private static void addStopTimeToMap(ProcessedDataMapKey mapKey,
			int stopTimeMsec) {
		List<Integer> stopTimesForStop = stopTimesMap.get(mapKey);
		if (stopTimesForStop == null) {
			stopTimesForStop = new ArrayList<Integer>();
			stopTimesMap.put(mapKey, stopTimesForStop);
		}
		stopTimesForStop.add(stopTimeMsec);
	}
	
	/**
	 * Adds travel times for stop path for a single trip to the travelTimesMap.
	 * 
	 * @param mapKey
	 * @param travelTimesForStopPath
	 */
	private static void addTravelTimesToMap(ProcessedDataMapKey mapKey, 
			List<Integer> travelTimesForStopPath) {
		// If there is no data then simply return
		if (travelTimesForStopPath == null || travelTimesForStopPath.isEmpty())
			return;
		
		List<List<Integer>> travelTimesForStop = travelTimesMap.get(mapKey);
		if (travelTimesForStop == null) {
			travelTimesForStop = new ArrayList<List<Integer>>();
			travelTimesMap.put(mapKey, travelTimesForStop);
		}
		travelTimesForStop.add(travelTimesForStopPath);
	}
	
	/**
	 * Just for debugging. Logs raw data for trip.
	 * 
	 * @param arrDepList
	 */
	private static void debugLogTrip(List<ArrivalDeparture> arrDepList) {
		logger.trace("====================");
		for (ArrivalDeparture arrivalDeparture : arrDepList) {
			logger.trace(arrivalDeparture.toString());
		}
	}
	
	/**
	 * For when the arrival/departure is for first stop of trip. If the schedule
	 * adherence isn't too bad adds the stop time to the stop wait map.
	 * 
	 * @param arrDep
	 */
	private static void processFirstStopOfTrip(ArrivalDeparture arrDep) {
		// Only need to handle departure for first stop in trip
		if (arrDep.getStopPathIndex() != 0) 
			return;
		
		// Should only process departures for the first stop, so make sure.
		// If not a departure then continue to the next stop
		if (arrDep.isArrival())
			return;

		// First stop in trip so just deal with departure time.
		// Don't need to deal with travel time.
		int lateTimeMsec = 
				(int) (arrDep.getTime() - arrDep.getScheduledTime());

		// If schedule adherence is really far off then ignore the data
		// point because it would skew the results.
		if (Math.abs(lateTimeMsec) > MAX_SCHED_ADH_FOR_FIRST_STOP_TIME)
			return;
		
		// If configured to not use early departures then reset lateTimeMsec
		// to 0 if it is negative. This is useful for agencies like commuter
		// rail since trains really aren't going to leave early and early
		// departures indicates a problem with travel times.
		if (shouldResetEarlyTerminalDepartures() && lateTimeMsec < 0)
			lateTimeMsec = 0;

		// Get the MapKey so can put stop time into map
		ProcessedDataMapKey mapKeyForTravelTimes =
				getKey(arrDep.getTripId(), arrDep.getStopPathIndex(),
						arrDep.getStopId());

		// Add this stop time to map so it can be averaged
		addStopTimeToMap(mapKeyForTravelTimes, lateTimeMsec);		
	}
	
	/**
	 * Returns the matches for the particular stopPath for the service ID and
	 * trip.
	 * <P>
	 * Note: This is pretty inefficient. Seems that should be able to get
	 * list of matches for the stopPath directly from the map instead of getting
	 * all the matches for the trip and then filtering them.
	 * 
	 * @param dataFetcher
	 * @param arrDep
	 * @return List of Match objects. Never returns null.
	 */
	private static List<Match> getMatchesForStopPath(DataFetcher dataFetcher,
			ArrivalDeparture arrDep) {
		// For returning the results
		List<Match> matchesForStopPath = new ArrayList<Match>();

		// Get the matches for the entire trip since that is
		// how the data is available
		DbDataMapKey mapKey = dataFetcher.getKey(arrDep.getServiceId(),
				arrDep.getDate(), arrDep.getTripId(), arrDep.getVehicleId());
		List<Match> matchesForTrip = dataFetcher.getMatchesMap().get(mapKey);
		
		// If no matches were found for this trip then return empty
		// array (don't continue since would get NPE).
		if (matchesForTrip == null)
			return matchesForStopPath;
		
		for (Match match : matchesForTrip) {
			if (match.getStopPathIndex() == arrDep.getStopPathIndex())
				matchesForStopPath.add(match);
			else {
				// If looking at matches past the stop then done. Breaking
				// out of the for loop makes the code more efficient.
				if (match.getStopPathIndex() > arrDep.getStopPathIndex())
					break;
			}
		}
		
		return matchesForStopPath;
	}
	
	/**
	 * Internal structure for keeping track of matches that are between an
	 * departure and an arrival. Needed for when there are multiple travel
	 * time segments within a stop path. 
	 */
	private static class MatchPoint {
		private final Date time;
		private final float distanceAlongStopPath;
		private final MatchPointReason reason;
		
		public enum MatchPointReason {DEPARTURE, MATCH, ARRIVAL};
		
		private MatchPoint(Date time, float distance, MatchPointReason reason) {
			this.time = time;
			this.distanceAlongStopPath = distance;
			this.reason = reason;
		}
		
		private long getTime() {
			return time.getTime();
		}

		@Override
		public String toString() {
			return "MatchPoint [" 
					+ "time=" + time
					+ ", distanceAlongStopPath=" 
						+ Geo.distanceFormat(distanceAlongStopPath) 
					+ ", reason=" + reason
					+ "]";
		}	
		
	}
	
	/**
	 * Number of travel time segments for the stop path with the specified
	 * length.
	 * 
	 * @param pathLength
	 * @return number of travel time segments
	 */
	private static int getNumTravelTimeSegments(double pathLength) {
		int numberTravelTimeSegments = 
				(int) (pathLength / getMaxTravelTimeSegmentLength() + 1.0);		
		return numberTravelTimeSegments;
	}

	/**
	 * Number of travel time segments for the stop path specified.
	 * 
	 * @param trip
	 * @param stopPathIndex
	 * @return Number of travel time segments
	 * @throws InvalidParameterException
	 */
	private static int getNumTravelTimeSegments(Trip trip, int stopPathIndex) {
		StopPath stopPath = trip.getStopPath(stopPathIndex);
		if (stopPath == null) {
			String message =
					"In getNumTravelTimeSegments() stopPathIndex="
							+ stopPathIndex + " not " + "valid for trip="
							+ trip;
			logger.error(message);
			throw new InvalidParameterException(message);
		}
		double pathLength = stopPath.getLength();
		return getNumTravelTimeSegments(pathLength);
	}
	
	/**
	 * The travel time length is the length of the stop path divided into equal
	 * segments such that the segments are no longer than
	 * maxTravelTimeSegmentLength.
	 * 
	 * @param trip
	 *            For determining the StopPath
	 * @param stopPathIndex
	 *            For determining the StopPath
	 * @return travel time length for the specified StopPath
	 */
	private static double getTravelTimeSegmentLength(Trip trip,
			int stopPathIndex) {
		double pathLength = trip.getStopPath(stopPathIndex).getLength();
		double segLength =
				pathLength / getNumTravelTimeSegments(trip, stopPathIndex);
		return segLength;
	}
	
	/**
	 * The travel time length is the length of the stop path divided into equal
	 * segments such that the segments are no longer than
	 * maxTravelTimeSegmentLength.
	 * 
	 * @param arrDep
	 *            The arrival stop for which to determine the travel time length
	 * @return travel time length for the specified arrival
	 */
	private static double getTravelTimeSegmentLength(ArrivalDeparture arrDep) {
		double pathLength = arrDep.getStopPathLength();
		double segLength = pathLength / getNumTravelTimeSegments(pathLength);
		return segLength;
	}
	
	/**
	 * Gets the Matches that are associated with the stop path specified by
	 * arrDep2 (the path leading up to that stop) for the same trip (makes sure
	 * it is for the same tripId for the same day of the year). The returned
	 * matches will include the departure time from the first stop (arrDep1), in
	 * between matches, and the arrival time as the second stop (arrDep2).
	 * 
	 * @param dataFetcher
	 * @param arrDep1
	 *            The departure stop
	 * @param arrDep2
	 *            The arrival stop. Also defines which stop path working with.
	 * @return List of MatchPoints, which contain the basic Match info needed
	 *         for determining travel times.
	 */
	private static List<MatchPoint> getMatchPoints(DataFetcher dataFetcher,
			ArrivalDeparture arrDep1, ArrivalDeparture arrDep2) {
		// The array to be returned
		List<MatchPoint> matchPoints = new ArrayList<MatchPoint>();
		
		// Add the departure time to the list of data points
		matchPoints.add(new MatchPoint(arrDep1.getDate(), 0.0f,
				MatchPoint.MatchPointReason.DEPARTURE));
		
		// Stop path is long enough such that have more than one travel
		// time segment. Get the corresponding matches
		List<Match> matchesForStopPath = 
				getMatchesForStopPath(dataFetcher, arrDep2);

		// Add the matches that are in between the arrival and the departure.
		for (Match match : matchesForStopPath) {
			matchPoints.add(new MatchPoint(match.getDate(), 
					match.getDistanceAlongStopPath(), 
					MatchPoint.MatchPointReason.MATCH));
		}
		
		// Add the arrival time to the list of data points
		matchPoints.add(new MatchPoint(arrDep2.getDate(), 
				arrDep2.getStopPathLength(), 
				MatchPoint.MatchPointReason.ARRIVAL));

		// Return the list of time/distanceAlongStopPath points
		return matchPoints;
	}

	/**
	 * Determines the travel times. If stop path is short enough such that it is
	 * only a single travel time segment then returns single travel time set to
	 * the difference between the departure time for arrDep1 and the arrival
	 * time for arrDep2. For when stop path is long enough such that there are
	 * multiple travel time segments, for the particular stop path, as specified
	 * by the arrival stop, looks up the associated Matches in order to
	 * determine how the vehicle travels along the stop path. Determines when
	 * the travel time segment vertices are crossed and uses the vertices, along
	 * with the departure time and the arrival times at the ends of the stop
	 * path, to determine the travel time for each travel time segment for this
	 * particular trip.
	 * 
	 * @param dataFetcher
	 * @param arrDep1
	 *            The departure stop
	 * @param arrDep2
	 *            The arrival stop. Also defines which stop path working with.
	 * @return List of travel times in msec. There is a separate travel time for
	 *         each travel time segment. If the match points are garbled and go
	 *         backwards in time then null is returned.
	 */
	private List<Integer> determineTravelTimesForStopPath(
			DataFetcher dataFetcher, ArrivalDeparture arrDep1,
			ArrivalDeparture arrDep2) {
		// Determine departure time. If shouldn't use departures times
		// for terminal departure that are earlier then schedule time
		// then use the scheduled departure time. This prevents creating
		// bad predictions due to incorrectly determined travel times. 
		long departureTime = arrDep1.getTime();
		if (shouldResetEarlyTerminalDepartures() 
				&& arrDep1.getStopPathIndex() == 0 
				&& arrDep1.getTime() < arrDep1.getScheduledTime()) {
			logger.debug("Note: for {} using scheduled departure time instead "
					+ "of the calculated departure time since "
					+ "transitclock.travelTimes.resetEarlyTerminalDepartures is "
					+ "true and the departure time was (likely incorrectly) "
					+ "calculated to be before the scheduled departure time",
					arrDep1);
			departureTime = arrDep1.getScheduledTime();
		}

		// If this stop path is short enough such that it is just a single 
		// travel times segment then handle specially since don't need
		// to look at matches.
		if (arrDep2.getStopPathLength() < getMaxTravelTimeSegmentLength()) {
			// Determine and return the travel time between the stops
			int travelTimeBetweenStopsMsec = 
					(int) (arrDep2.getTime() - departureTime);
			List<Integer> travelTimesForStopPath = new ArrayList<Integer>();
			travelTimesForStopPath.add(travelTimeBetweenStopsMsec);
			return travelTimesForStopPath;
		}

		// Stop path is longer than a single travel time segment so need to
		// look at matches to determine travel times for each travel time
		// segment.
		double travelTimeSegmentLength = getTravelTimeSegmentLength(arrDep2);

		List<MatchPoint> matchPoints = 
				getMatchPoints(dataFetcher, arrDep1, arrDep2);
		
		// The times when a travel time segment vertex is crossed.
		// Will include the departure time, the middle vertices, and
		// the arrival time so that all the travel times can be determined.
		List<Long> vertexTimes = new ArrayList<Long>();
		
		// Add departure time from first stop
		vertexTimes.add(departureTime);
		
		for (int i=0; i<matchPoints.size()-1; ++i) {
			MatchPoint pt1 = matchPoints.get(i);
			MatchPoint pt2 = matchPoints.get(i+1);

			// If the match points decrease in time then it means that data
			// is messed up and can't determine travel times. This can happen
			// if the predictor is restarted and vehicles are first matched
			// incorrectly due to them being off schedule.			
			if (pt2.getTime() < pt1.getTime()) {
				logger.error("Encountered two match points that go backwards " +
						"in time and are therefore incorrect. They are {} " +
						"and {} between {} and {}", pt1, pt2, arrDep1, arrDep2);
				return null;
			}
			
			// Determine which travel time segment the match points are on.
			// Need to subtract 0.0000001 for segIndex2 because the distance
			// can be the distance to the end of the stop path which of
			// course is an exact multiple of the travelTimeSegmentLength.
			// To get the right segment index need to subtract a bit.
			int segIndex1 = (int) (pt1.distanceAlongStopPath / 
					travelTimeSegmentLength);
			int segIndex2 = (int) ((pt2.distanceAlongStopPath-0.0000001) / 
					travelTimeSegmentLength);
			
			// If the two matches span a travel time segment vertex...
			if (segIndex1 != segIndex2) {
				// Determine speed traveled between the two matches.
				// Note that the speed is in meters per msec.
				long timeBtwnMatches = pt2.getTime() - pt1.getTime();
				float distanceBtwnMatches = 
						pt2.distanceAlongStopPath - pt1.distanceAlongStopPath;
				double speed = distanceBtwnMatches / timeBtwnMatches;
				
				// Determine when crossed the first vertex between the match points
				// and add the time to the vertex times
				double distanceOfFirstVertex = 
						(segIndex1+1) * travelTimeSegmentLength;
				double distanceToFirstVertex = 
						distanceOfFirstVertex - pt1.distanceAlongStopPath;
				long crossingTimeForVertex = 
						pt1.getTime() + (long) (distanceToFirstVertex/speed);
				vertexTimes.add(crossingTimeForVertex);
				
				// Add any subsequent vertices crossed between the match points
				for (int segIndex=segIndex1+1; segIndex<segIndex2; ++segIndex) {
					crossingTimeForVertex += travelTimeSegmentLength / speed;
					vertexTimes.add(crossingTimeForVertex);
				}
			}			
		}
		// Deal with the final travel time segment that goes to the arrival stop
		vertexTimes.add(arrDep2.getTime());
		
		// Now that we have all the vertex times for the stop path determine the
		// travel times and add them to the list of times to be returned.
		List<Integer> travelTimesForStopPath = new ArrayList<Integer>();
		for (int i=0; i<vertexTimes.size()-1; ++i) {
			// The segment time is the time between two vertices
			long vertexTime1 = vertexTimes.get(i);
			long vertexTime2 = vertexTimes.get(i+1);
			int segmentTime = (int) (vertexTime2 - vertexTime1);
			
			// Make sure segment speed isn't ridiculously low. For MBTA commuter
			// rail for example vehicles don't travel below a certain speed.
			// A low speed indicates a problem with the data.
			double segmentSpeedMps = 
					travelTimeSegmentLength * Time.MS_PER_SEC / segmentTime;
			if (segmentSpeedMps < 0.0) {
				// arrival / departure were switched, clamp to 0 travel time
				logger.error("For segmentIdx={} segment speed of {}m/s is "
								+ "negative"
								+ "Therefore it is being reset to zero. "
								+ "arrDep1={} arrDep2={}",
						i, StringUtils.twoDigitFormat(segmentSpeedMps),
						arrDep1, arrDep2);
				segmentTime = 0;
			} else if (segmentSpeedMps < getMinSegmentSpeedMps()) {
				logger.error("For segmentIdx={} segment speed of {}m/s is "
						+ "below the limit of minSegmentSpeedMps={}m/s. "
						+ "Therefore it is being reset to min segment speed. "
						+ "arrDep1={} arrDep2={}",
						i, StringUtils.twoDigitFormat(segmentSpeedMps), 
						minSegmentSpeedMps.getValue(), arrDep1, arrDep2);
				segmentTime = (int) (travelTimeSegmentLength * Time.MS_PER_SEC / 
						getMinSegmentSpeedMps());
			}
			
			// Make sure segment speed isn't ridiculously high.
			if (segmentSpeedMps > maxSegmentSpeedMps.getValue()) {
				logger.error("For segmentIdx={} segment speed of {}m/s is "
						+ "above the limit of maxSegmentSpeedMps={}m/s. "
						+ "Therefore it is being reset to max segment speed. "
						+ "arrDep1={} arrDep2={}",
						i, StringUtils.twoDigitFormat(segmentSpeedMps), 
						maxSegmentSpeedMps.getValue(), arrDep1, arrDep2);
				segmentTime = (int) (travelTimeSegmentLength * Time.MS_PER_SEC / 
						maxSegmentSpeedMps.getValue());
			}
			
			// Keep track of this segment time for this segment
			travelTimesForStopPath.add(segmentTime);
		}
		return travelTimesForStopPath;
	}
	
	/**
	 * For looking at travel time between two arrival/departures. It can be an
	 * arrival and then a departure for the same stop or a departure for one
	 * stop and then an arrival for the subsequent stop. If the schedule
	 * adherence is off too much (by MAX_SCHED_ADH_SECS) then the data is
	 * ignored. If schedule adherence is acceptable then the resulting travel
	 * and stop/dwell times are put into the stopTimesMap and travelTimesMap
	 * members for further processing.
	 * 
	 * @param dataFetcher
	 *            Contains the AVL based historic data in maps
	 * @param arrDep1
	 *            The first arrival/departure
	 * @param arrDep2
	 *            The second arrival/departure
	 */
	private void processDataBetweenTwoArrivalDepartures(
			DataFetcher dataFetcher, ArrivalDeparture arrDep1,
			ArrivalDeparture arrDep2) {
		// If schedule adherence is really far off then ignore the data
		// point because it would skew the results.
		TemporalDifference schedAdh = arrDep1.getScheduleAdherence();
		if (schedAdh == null)
			schedAdh = arrDep2.getScheduleAdherence();
		if (schedAdh != null
				&& !schedAdh.isWithinBounds(MAX_SCHED_ADH_SECS,
						MAX_SCHED_ADH_SECS)) {
			// Schedule adherence is off so don't use this data
			return;
		}
		
		// Determine the key for storing the data into appropriate map
		ProcessedDataMapKey mapKeyForTravelTimes =
				getKey(arrDep2.getTripId(), arrDep2.getStopPathIndex(),
						arrDep2.getStopId());
			
		// If looking at arrival and departure for same stop then determine
		// the stop time.
		if (arrDep1.getStopPathIndex() == arrDep2.getStopPathIndex() 
				&& arrDep1.isArrival() 
				&& arrDep2.isDeparture()) {
			// Determine time at stop
			int dwellTimeMsec = (int) (arrDep2.getTime() - arrDep1.getTime());

			// Add this stop time to map so it can be averaged
			if (dwellTimeMsec >= 0)
				addStopTimeToMap(mapKeyForTravelTimes, dwellTimeMsec);		
			else
				logger.error("Ignoring negative dwell time={} for stop path "
						+ "at arrival/departures {} and {} (key = {})",
						dwellTimeMsec, arrDep1, arrDep2,
						mapKeyForTravelTimes);
			
			return;
		}
		
		// If looking at departure from one stop to the arrival time at the
		// very next stop then can determine the travel times between the stops.
		if (arrDep1.getStopPathIndex() - arrDep2.getStopPathIndex() != 1
				&& arrDep1.isDeparture()
				&& arrDep2.isArrival()) {
			// Determine the travel times and add them to the map
			List<Integer> travelTimesForStopPath = 
					determineTravelTimesForStopPath(dataFetcher, arrDep1, 
							arrDep2);
			
			// Ignore a stop path if any segment travel time is negative. Nulls will
			// be ignored downstream anyway so can also ignore those.
			if (travelTimesForStopPath == null)
				return;
			for (Integer travelTimeForSegment : travelTimesForStopPath) {
				if (travelTimeForSegment < 0) {
					logger.error("Ignoring negative travel times={} for stop path "
							+ "between arrival/departures {} and {} (key = {})",
							travelTimesForStopPath, arrDep1, arrDep2,
							mapKeyForTravelTimes);
					return;
				}
			}
			
			addTravelTimesToMap(mapKeyForTravelTimes, travelTimesForStopPath);
				
			return;
		}
	}
	
	/**
	 * Process historic data from database for single trip. Puts resulting data
	 * into stopTimesMap and travelTimesMap.
	 * 
	 * @param dataFetcher
	 *            Contains arrival/departures and matches fetched from database
	 * @param arrDepList
	 *            List of ArrivalDepartures for vehicle for a trip
	 */
	private void aggregateTripDataIntoMaps(DataFetcher dataFetcher,
			List<ArrivalDeparture> arrDepList) {
		
		for (int i=0; i<arrDepList.size()-1; ++i) {
			ArrivalDeparture arrDep1 = arrDepList.get(i);
			
			// Handle first stop in trip specially
			if (arrDep1.getStopPathIndex() == 0) {
				// Don't need to deal with first arrival stop for trip since
				// don't care when vehicle arrives at layover
				if (arrDep1.isArrival())
					continue;

				// Handle first stop
				processFirstStopOfTrip(arrDep1);
			} 
			
			// Deal with normal travel times
			ArrivalDeparture arrDep2 = arrDepList.get(i+1);				
			processDataBetweenTwoArrivalDepartures(dataFetcher, arrDep1, arrDep2);							
		}		
	}
		
	/**
	 * Converts the list of travel times such that times are grouped by segment
	 * instead of by single trips. This allows the times to be more easily
	 * processed when determining outliers, averages, etc.
	 * <p>
	 * Only uses historic travel time data if the number of travel times
	 * segments in the data match the number of travel time segments needed for
	 * the current trip configuration (which depends on the current path length
	 * for the trip). This is important because configuration can change (stops
	 * can move or the path between stops can change). Therefore need to make
	 * sure that only using data if it applies to the current config (has same
	 * number of travel time segments). This way can use as much historic info
	 * as possible, yet not try to use data that doesn't pertain.
	 * 
	 * @param historicTravelTimes
	 *            List of List of times where outer List is by single trip and
	 *            inner List is travel time segment.
	 * @param trip
	 *            Contains config info for the trip that the data is for. Used
	 *            to determine if the historic data for the trip has the proper
	 *            number of travel time segments.
	 * @param stopPathIndex
	 *            Used to determine if the historic data for the trip has the
	 *            proper number of travel time segments.
	 * @return List of List of times but the outer List is by travel time
	 *         segment and there is an inner List with a value per single trip,
	 *         or null if there is no valid historic data for the trip.
	 */
	private static List<List<Integer>> bySegment(
			List<List<Integer>> historicTravelTimes, Trip trip,
			int stopPathIndex) {
		// Determine how many travel time segments there should be for the stop
		// path according to the current configuration of the trip's path 
		// length.
		int expectedTravelTimeSegments =
				getNumTravelTimeSegments(trip, stopPathIndex);
		
		// Create results object. Make array size only as big as the number of
		// travel segments, instead of the default value of 10, to reduce 
		// memory use.
		List<List<Integer>> timesBySegment = 
				new ArrayList<List<Integer>>(expectedTravelTimeSegments);
		for (int i=0; i<expectedTravelTimeSegments; ++i) {
			timesBySegment.add(new ArrayList<Integer>());
		}
		
		// Put the historic per trip travel time data into the per segment array
		boolean validDataFound = false;
		for (int tripIdx = 0; tripIdx < historicTravelTimes.size(); ++tripIdx) {
			// Determine historic travel times for the current trip.
			List<Integer> historicTravelTimeForTrip =
					historicTravelTimes.get(tripIdx);
			int numberHistoricTravelTimeSegs = historicTravelTimeForTrip.size();

			// Only use the historic travel times if have the number of travel
			// time segments for the historical data matches the current 
			// configuration of the trip (the path length). 
			if (numberHistoricTravelTimeSegs == expectedTravelTimeSegments) {
				validDataFound = true;
				for (int segIdx = 0; segIdx < expectedTravelTimeSegments; ++segIdx) {
					Integer value = historicTravelTimeForTrip.get(segIdx);
					timesBySegment.get(segIdx).add(value);
				}
			}
		}
		
		// If valid data found then return the times grouped by segment
		if (validDataFound)
			return timesBySegment;
		else
			return null;
	}
	
	/**
	 * Takes the data from the stopTimesMap and travelTimesMap and creates
	 * corresponding travel times. Puts those travel times into the
	 * TravelTimeInfoMap that is returned.
	 * 
	 * @param tripMap
	 *            contains all the trips that are configured and that need
	 *            travel times for.
	 * @return TravelTimeInfoMap The generated travel times
	 */
	public TravelTimeInfoMap createTravelTimesFromMaps(
			Map<String, Trip> tripMap) {
		logger.info("Processing data into a TravelTimeInfoMap...");
		IntervalTimer intervalTimer = new IntervalTimer();

		TravelTimeInfoMap travelTimeInfoMap = new TravelTimeInfoMap();
		
		// Need to look at all trips that have data for. Therefore need
		// to combine keys from both stopTimesMap and travelTimesMap.
		Set<ProcessedDataMapKey> combinedKeySet = 
				new HashSet<ProcessedDataMapKey>();
		combinedKeySet.addAll(travelTimesMap.keySet());
		combinedKeySet.addAll(stopTimesMap.keySet());
		int setSize = 0;
		int unmatched = 0;
		int matched = 0;
		int invalid = 0;
		
		// For each trip/stop path that had historical arrivals/departures and 
		// or matches in the database...
		for (ProcessedDataMapKey mapKey : combinedKeySet) {
		  setSize++;
			// Determine the associated Trip object for the data
			Trip trip = tripMap.get(mapKey.getTripId());
			if (trip == null) {
				logger.error("No trip exists for trip ID={} in " +
						"configuration data even though historic data was " +
						"found for it.", 
						mapKey.getTripId());
				invalid ++;
				continue;
			}

			// Make sure stopPathIndex and stopId from historic data match
			// the current trip configuration. This is important since stops
			// for a trip might have changed.
			if (mapKey.getStopPathIndex() >= trip.getStopPaths().size()) {
				logger.error("Problem with stopPathIndex for historical data. "
						+ "The stopPathIndex from the historical data {} is "
						+ "greater than the number of stop paths for {}",
						mapKey.getStopPathIndex(), trip);
				invalid++;
				continue;
			}
			String stopIdFromTrip = 
					trip.getStopPath(mapKey.getStopPathIndex()).getStopId();
			if (!mapKey.getStopId().equals(stopIdFromTrip)) {
				logger.error("Problem with stopPathIndex for historical data. "
						+ "The stopPathIndex from the historical data {} "
						+ "corresponds to stopId={} but for the trip the "
						+ "stopId={}. {}",
						mapKey.getStopPathIndex(), mapKey.getStopId(), 
						stopIdFromTrip, trip);
				invalid++;
				continue;
			}
			// Determine average travel times for this trip/stop path
			List<List<Integer>> travelTimesForStopPathForTrip =
					travelTimesMap.get(mapKey);
			List<Integer> averageTravelTimes = new ArrayList<Integer>();
			if (travelTimesForStopPathForTrip != null) {
				// Get the travel times, grouped by segment
				List<List<Integer>> travelTimesBySegment =
						bySegment(travelTimesForStopPathForTrip, trip,
								mapKey.getStopPathIndex());

				// Only continue to process if some of the historic data for 
				// the trip was actually valid
				if (travelTimesBySegment != null) {
					// For each segment, process travel times...
					for (List<Integer> travelTimesByTripForSegment : 
							travelTimesBySegment) {
						int averageTravelTimeForSegment = Statistics
								.filteredMean(travelTimesByTripForSegment, 
										fractionLimitForTravelTimes.getValue());
						averageTravelTimes.add(averageTravelTimeForSegment);
					}
				}
			}
			
			// Determine average stop time for this trip/stop
			int averagedStopTime;
			List<Integer> stopTimesForStopPathForTrip = 
					stopTimesMap.get(mapKey);
			if (stopTimesForStopPathForTrip != null) { 
				// For first stops of trip will be providing departure
				// times so need to be conservative and bias the stop time
				if (mapKey.getStopPathIndex() == 0) {
					// First stop of trip so be extra conservative because
					// don't want to determine that vehicles depart at 8:02
					// when the doors actually shut at 8:01 and the vehicle
					// starts moving slowly giving a slightly wrong departure
					// time.
					// Determine best stop time to use
					averagedStopTime =
							Statistics.biasedFilteredMean(
									stopTimesForStopPathForTrip,
									fractionLimitForStopTimes.getValue(),
									STD_DEV_BIAS_FOR_FIRST_STOP);
					
					// So far have determine when vehicle has departed. But should add
					// a bit of a bias since passengers have to get on a few seconds
					// before doors shut and vehicle starts moving.
					averagedStopTime = Math.max(0, averagedStopTime - STOP_TIME_BIAS_FOR_FIRST_STOP);
				} else {
					// Not first stop of trip
					averagedStopTime = Statistics.filteredMean(
							stopTimesForStopPathForTrip,
							fractionLimitForStopTimes.getValue());
				}
			} else {
				// No arrival and corresponding departure time for the stop. 
				averagedStopTime = TravelTimeInfo.STOP_TIME_NOT_VALID;

				// Not having stop time indicates possible problem unless it 
				// is the last stop path for the trip. So if not the last stop  
				// path for trip then log the problem.
				if (mapKey.getStopPathIndex() != trip.getNumberStopPaths()-1) {
					logger.debug("No stop times for {} even though there are " +
						"travel times for that map key", mapKey);
				} else {
				  unmatched++;
				}
			}
			
			// Determine the travel time segment length actually used
			double travelTimeSegLength = 
					getTravelTimeSegmentLength(trip, mapKey.getStopPathIndex());
			
			// Put the results into TravelTimeInfo object and put into 
			// TravelTimeInfo map so can be used to find best travel times 
			// when there is no data for particular trip.
			TravelTimeInfo travelTimeInfo = new TravelTimeInfo(trip,
					mapKey.getStopPathIndex(), averagedStopTime,
					averageTravelTimes, travelTimeSegLength);
			travelTimeInfoMap.add(travelTimeInfo);
			matched++;
		}

		// Nice to log how long things took so can see progress and bottle necks
		logger.info("Processing data (total={} matched={} unmatched={} invalid={}) into a TravelTimeInfoMap took {} msec.", 
				setSize, matched, unmatched, invalid, intervalTimer.elapsedMsec());
		reportStatus(setSize, matched, unmatched, invalid);
		// Return the map with all the processed travel time data in it
		return travelTimeInfoMap;	
	}
	
  /**
	 * Reads in the Matches and the ArrivalDepartures from the database for the
	 * time specified. Puts the data into the stopTimesMap and the travelTimesMap 
	 * for further processing.
	 * 
	 * @param projectId
	 * @param specialDaysOfWeek
	 * @param beginTime
	 * @param endTime
	 */
	public void readAndProcessHistoricData(String projectId, 
			List<Integer> specialDaysOfWeek, Date beginTime, Date endTime) {
		// Read the arrivals/departures and matches into a DataFetcher
		DataFetcher dataFetcher = new DataFetcher(projectId, specialDaysOfWeek);
		dataFetcher.readData(projectId, beginTime, endTime);
		
    // exit here if no matches are present
    // no further work can be done!
    if (dataFetcher.getMatchesMap()== null || dataFetcher.getMatchesMap().isEmpty()) {
      logger.error("No Matches:  Nothing to do!");
      isEmpty = true;
      reportStatus(0, 0, 0, 0);
      return;
    }
    isEmpty = false;
		
		// Process all the historic data read from the database. Puts 
		// resulting data into stopTimesMap and travelTimesMap.
		logger.info("Processing data into travel time maps...");
		IntervalTimer intervalTimer = new IntervalTimer();
		Collection<List<ArrivalDeparture>> arrivalDepartures =
				dataFetcher.getArrivalDepartureMap().values();
		for (List<ArrivalDeparture> arrDepList : arrivalDepartures) {
			debugLogTrip(arrDepList);
			aggregateTripDataIntoMaps(dataFetcher, arrDepList);
		}
		
		// Nice to log how long things took so can see progress and bottle necks
		logger.info("Processing data from db into the travel times and stop " +
				"times map took {} msec.", 
				intervalTimer.elapsedMsec());
	}	
	
	 public Long updateMetrics(Session session, int travelTimesRev) {
	   Long count = Trip.countTravelTimesForTrips(session, travelTimesRev);
	   monitoringService.averageMetric("PredictionLatestTravelTimeRev", travelTimesRev*1.0);
	   if (count != null) {
	     monitoringService.averageMetric("PredictionTravelTimesForTripsCount", count*1.0);
	   } else {
	     monitoringService.averageMetric("PredictionTravelTimesForTripsCount", -1.0);
	   }
	   monitoringService.flush();
	   return count;
	  }


	// cloudwatch reporting/monitoring
  private void reportStatus(int setSize, int matched, int unmatched, int invalid) {
    monitoringService.averageMetric("TravelTimeTotal", setSize * 1.0);
    monitoringService.averageMetric("TravelTimeMatched", matched * 1.0);
    monitoringService.averageMetric("TravelTimeUnmatched", unmatched * 1.0);
    monitoringService.averageMetric("TravelTimeInvalid", invalid * 1.0);

  }

	/*
	 * Just for debugging
	 */
	public static void main(String[] args) {
		List<Integer> t1 = new ArrayList<Integer>();
		t1.add(1);
		t1.add(2);
		t1.add(3);
		
		List<Integer> t2 = new ArrayList<Integer>();
		t2.add(4);
		t2.add(5);
		t2.add(6);
		
		List<List<Integer>> travelTimesByTrip = new ArrayList<List<Integer>>();
		travelTimesByTrip.add(t1);
		travelTimesByTrip.add(t2);
		List<List<Integer>> travelTimesBySegment =
				bySegment(travelTimesByTrip, null, 0);
		System.err.println(travelTimesByTrip);
		System.err.println(travelTimesBySegment);
	}
}
