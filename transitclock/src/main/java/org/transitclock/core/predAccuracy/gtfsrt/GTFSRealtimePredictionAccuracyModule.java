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

package org.transitclock.core.predAccuracy.gtfsrt;

import com.google.protobuf.CodedInputStream;
import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.core.blockAssigner.BlockAssigner;
import org.transitclock.core.predAccuracy.PredAccuracyPrediction;
import org.transitclock.core.predAccuracy.PredictionAccuracyModule;
import org.transitclock.db.structs.*;
import org.transitclock.gtfs.DbConfig;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.Calendar;

/**
 * Reads in external prediction data from a GTFS realtime trip updates feed and
 * stores the data in memory. Then when arrivals/departures occur the prediction
 * accuracy can be determined and stored.
 *
 * @author Sean Og Crudden
 *
 */
public class GTFSRealtimePredictionAccuracyModule extends PredictionAccuracyModule {

	private static final Logger logger = LoggerFactory.getLogger(GTFSRealtimePredictionAccuracyModule.class);

	/********************** Config Params **************************/


	private static final StringConfigValue gtfsTripUpdateUrl = new StringConfigValue(
			"transitclock.predAccuracy.gtfsTripUpdateUrl", "http://127.0.0.1:8091/trip-updates",
			"URL to access gtfs-rt trip updates.");

	private static StringConfigValue gtfsRealtimeHeaderKey =
			new StringConfigValue("transitclock.predictionAccuracy.apiKeyHeader",
					null,
					"api key header value if necessary, null if not needed");

	private static StringConfigValue gtfsRealtimeHeaderValue =
			new StringConfigValue("transitclock.predictionAccuracy.apiKeyValue",
					null,
					"api key value if necessary, null if not needed");

	
  	private static ClassConfigValue translatorConfig =
		  new ClassConfigValue("transitclock.predAccuracy.RtTranslator", null,
			  "Implementation of GTFSRealtimeTranslator to perform " +
		  "the translation of stopIds and other rt quirks");
	
  // if stopIds needs optional parsing/translation
  private GTFSRealtimeTranslator translator = null;

	/**
	 * @return the gtfstripupdateurl
	 */
	public static StringConfigValue getGtfstripupdateurl() {
		return gtfsTripUpdateUrl;
	}

	/********************** Member Functions **************************/

	/**
	 * @param agencyId
	 */
	public GTFSRealtimePredictionAccuracyModule(String agencyId) {
		super(agencyId);
	}

	/**
	 * Gets GTFS realtime feed all routes from URL and return FeedMessage
	 * 
	 * @return the FeedMessage to be processed
	 */

	private FeedMessage getExternalPredictions() {

		// Will just read all data from gtfs-rt url
		URL url = null;
		logger.info("Getting predictions from API using URL={}", getGtfstripupdateurl().getValue());

		try {

			// Create the connection
			url = new URL(getGtfstripupdateurl().getValue());

			HttpURLConnection
					connection = (HttpURLConnection) url.openConnection();

			if (gtfsRealtimeHeaderKey.getValue() != null &&
					gtfsRealtimeHeaderValue.getValue() != null) {
				connection.addRequestProperty(gtfsRealtimeHeaderKey.getValue(), gtfsRealtimeHeaderValue.getValue());
				connection.addRequestProperty("Cache-Control", "no-cache");
			}


			// Create a CodedInputStream instead of just a regular InputStream
			// so that can change the size limit. Otherwise if file is greater
			// than 64MB get an exception.
			InputStream inputStream = connection.getInputStream();
			CodedInputStream codedStream =
					CodedInputStream.newInstance(inputStream);
			// What to use instead of default 64MB limit
			final int GTFS_SIZE_LIMIT = 200000000;
			codedStream.setSizeLimit(GTFS_SIZE_LIMIT);

			FeedMessage feed = FeedMessage.parseFrom(codedStream);
			logger.info("Prediction read successfully from URL={}", getGtfstripupdateurl().getValue());

			return feed;
		} catch (Exception e) {
			logger.error("Problem when getting data from GTFS realtime trip updates URL={}", url, e);
			return null;
		}
	}

	/**
	 * Takes data from Protocol Buffer object and processes it and calls
	 * storePrediction() on the predictions.
	 * 
	 * @param feed
	 * @param dbConfig
	 */
	public void processExternalPredictions(FeedMessage feed, DbConfig dbConfig) {

		// If couldn't read data from feed then can't process it
		if (feed == null)
			return;

		// So can look up direction in database

		TimeZone timeZone = dbConfig.getFirstAgency().getTimeZone();

		logger.info("Processing GTFS-rt feed.....");

		for (FeedEntity entity : feed.getEntityList()) {
			if (entity.hasTripUpdate()) {
				TripUpdate update = entity.getTripUpdate();

				Date eventReadTime;
				if (update.hasTimestamp() && update.getTimestamp() <= feed.getHeader().getTimestamp()) {
					/*
					 * this is the best option as it is specific to each trip
					 * update
					 */
					eventReadTime = new Date(update.getTimestamp() * 1000);
				} else {
					/*
					 * can't do anything else? TODO Except maybe look to see if
					 * this has changed and then take the time it has changed as
					 * the read time
					 */
					eventReadTime = new Date(feed.getHeader().getTimestamp() * 1000);
				}

				GtfsRealtime.TripDescriptor tripDescriptor = update.getTrip();
				Trip gtfsTrip;
				String direction;


				if (tripDescriptor != null) {
					String tripId = getTripId(dbConfig, tripDescriptor);
					logger.debug("Trip Descriptor: {}", tripDescriptor);
					gtfsTrip = dbConfig.getTrip(tripId);

					if (gtfsTrip != null) {
						direction = gtfsTrip.getDirectionId();

						List<StopTimeUpdate> tripUpdateStopTimes;

						tripUpdateStopTimes = update.getStopTimeUpdateList();

						Integer lastStopTimeDelay = null;

						// Loop through trip update stop times
						for (int i = 0; i < tripUpdateStopTimes.size(); i++) {

							StopTimeUpdate stopTimeUpdate = tripUpdateStopTimes.get(i);

							// Confirm stopTime update has arrival or departure
							if (stopTimeUpdate.hasArrival() || stopTimeUpdate.hasDeparture()) {

								// Get stop path index for stop time update
								int stopPathIndex = getStopPathIndex(stopTimeUpdate, gtfsTrip);

								if(stopPathIndex >= 0){
									logger.debug("StopPathIndex : " + stopPathIndex);
									int stopPathIndexIncrement = 0;
									StopTimeUpdate nextStopTime = getNextStopTime(i, tripUpdateStopTimes);
									boolean isLastStopTimeUpdate = nextStopTime == null;
									int totalStopPaths = gtfsTrip.getStopPaths().size();

									// Check if it is last stopTime for trip
									if(isLastStopTimeUpdate){
										// Loop through remaining stop paths in case they are not in the feed
										while(stopPathIndex + stopPathIndexIncrement < totalStopPaths){
											String stopId = gtfsTrip.getStopPath(stopPathIndex + stopPathIndexIncrement).getStopId();
											ScheduleTime scheduledTime = gtfsTrip.getScheduleTime(stopPathIndex + stopPathIndexIncrement);
											StopPath stopPath = gtfsTrip.getStopPath(stopPathIndex + stopPathIndexIncrement);
											boolean stopTimeMatchesSchedule = false;

											if(stopTimeMatchesStopPath(stopTimeUpdate, stopPath)){
												lastStopTimeDelay = getStopTimeDelay(stopTimeUpdate, scheduledTime, timeZone.toZoneId());
												stopTimeMatchesSchedule = true;
											}

											processPrediction(scheduledTime, stopTimeUpdate, update, gtfsTrip, direction,
													stopId, eventReadTime, lastStopTimeDelay, stopTimeMatchesSchedule);

											stopPathIndexIncrement++;

										}
									}
									else{
										// Look for stopPaths that not be included in the feed and process them
										while(stopPathIndex + stopPathIndexIncrement < totalStopPaths &&
												!stopTimeMatchesStopPath(nextStopTime, gtfsTrip.getStopPath(stopPathIndex + stopPathIndexIncrement))){

											String stopId = gtfsTrip.getStopPath(stopPathIndex + stopPathIndexIncrement).getStopId();
											ScheduleTime scheduledTime = gtfsTrip.getScheduleTime(stopPathIndex + stopPathIndexIncrement);
											boolean stopTimeMatchesSchedule = stopTimeMatchesStopPath(stopTimeUpdate, gtfsTrip.getStopPath(stopPathIndex + stopPathIndexIncrement));

											if(stopTimeMatchesSchedule){
												lastStopTimeDelay = getStopTimeDelay(stopTimeUpdate, scheduledTime, timeZone.toZoneId());
											}

											processPrediction(scheduledTime, stopTimeUpdate, update, gtfsTrip, direction,
													stopId, eventReadTime, lastStopTimeDelay, stopTimeMatchesSchedule);

											stopPathIndexIncrement++;
										}
									}
								}
							}
							else {
								logger.debug("No predictions for vehicleId={} for stop={}", update.getVehicle().getId(),
										stopTimeUpdate.getStopId());
							}
						}

					}
					else {
						logger.warn("Got tripId={} but no such trip in the active GTFS", tripId);
					}

				}
			}
		}
	}

	private void processPrediction(ScheduleTime scheduledTime, StopTimeUpdate stopTimeUpdate, TripUpdate update,
								   Trip gtfsTrip, String direction, String stopId, Date eventReadTime,
								   Integer stopTimeDelay, boolean stopTimeMatchesSchedule){
		if (scheduledTime != null) {
			Date eventTime;
			if (stopTimeUpdate.hasArrival()) {
				eventTime = getEventTime(update, stopTimeUpdate, scheduledTime, true, stopTimeDelay,
						stopTimeMatchesSchedule, eventReadTime);
				if (eventTime != null) {
					processRecord(gtfsTrip.getRouteId(), direction, stopId, gtfsTrip.getId(),
							update.getVehicle().getId(), eventTime, eventReadTime,
							true, "GTFS-RT (Arrival)", scheduledTime.toString());
				}
			}
			if (stopTimeUpdate.hasDeparture()) {
				eventTime = getEventTime(update, stopTimeUpdate, scheduledTime, false, stopTimeDelay,
						stopTimeMatchesSchedule, eventReadTime);
				if (eventTime != null) {
					processRecord(gtfsTrip.getRouteId(), direction, stopId, gtfsTrip.getId(),
							update.getVehicle().getId(), eventTime, eventReadTime,
							false, "GTFS-RT (Departure)", scheduledTime.toString());
				}
			}

		} else {
			logger.debug("No scheduled time found for trip {}, stopId {}, stopPathIndex {}",
					gtfsTrip.getId(), stopId);
		}
	}

	private Integer getStopTimeDelay(StopTimeUpdate tripUpdateStopTime, ScheduleTime scheduleTime, ZoneId zoneId) {
		if(tripUpdateStopTime != null && scheduleTime != null){
			int scheduledArrivalTime = scheduleTime.getArrivalTime() != null ? scheduleTime.getArrivalTime() : scheduleTime.getDepartureTime();
			int scheduledDepartureTime = scheduleTime.getDepartureTime() != null ? scheduleTime.getDepartureTime() : scheduleTime.getArrivalTime();
			if(tripUpdateStopTime.hasArrival() && tripUpdateStopTime.getArrival().hasTime()){
				int stopTimeSeconds = getSecondsIntoDay(tripUpdateStopTime.getArrival().getTime() * 1000l, zoneId);
				return stopTimeSeconds - scheduledArrivalTime;
			}
			else if(tripUpdateStopTime.hasDeparture() && tripUpdateStopTime.getDeparture().hasDelay()){
				int stopTimeSeconds = getSecondsIntoDay(tripUpdateStopTime.getDeparture().getTime() * 1000l, zoneId);
				return stopTimeSeconds - scheduledDepartureTime;
			}
		}
		return null;
	}

	private void processRecord( String routeId,
							   String direction,
							   String stopId,
							   String tripId,
							   String vehicleId,
							   Date eventTime,
							   Date eventReadTime,
							   boolean isArrival,
							   String source,
							   String scheduledTime){
		if (eventTime.after(eventReadTime)) {

			logger.info(
					"Storing external prediction routeId={}, "
							+ "directionId={}, tripId={}, vehicleId={}, "
							+ "stopId={}, prediction={}, isArrival={}, scheduledTime={}, readTime={}",
					routeId, direction, tripId, vehicleId, stopId, eventTime, true, scheduledTime,
					eventReadTime.toString());

			logger.info("Prediction in milliseconds is {} and converted is {}",
					eventTime.getTime(), eventTime);

			PredAccuracyPrediction pred = new PredAccuracyPrediction(
				routeId,
				direction,
				stopId,
				tripId,
				vehicleId,
				eventTime,
				eventReadTime,
				isArrival,
				false,
				source,
				null,
				scheduledTime
			);
			if(isArrival){
				storeArrivalPrediction(pred);
			} else{
				storeDeparturePrediction(pred);
			}
		} else {
			logger.info(
					"Discarding as prediction after event. routeId={}, "
							+ "directionId={}, tripId={}, vehicleId={}, "
							+ "stopId={}, prediction={}, isArrival={}, scheduledTime={}, readTime={}",
					routeId, direction, tripId, vehicleId,
					stopId, eventTime, isArrival, scheduledTime,
					eventReadTime.toString());
		}
	}

	public void storeArrivalPrediction(PredAccuracyPrediction pred){
		storePrediction(pred);
	}

	public void storeDeparturePrediction(PredAccuracyPrediction pred){
		storePrediction(pred);
	}

	private StopTimeUpdate getNextStopTime(int i, List<StopTimeUpdate> stopTimes) {
		if (i + 1 < stopTimes.size()) {
			return stopTimes.get(i + 1);
		}
		return null;
	}

	private Date getEventTime(TripUpdate tripUpdate,
							  StopTimeUpdate stopTime,
							  ScheduleTime scheduledTime,
							  boolean isArrival,
							  Integer stopTimeDelay,
							  boolean stopTimeMatchesSchedule,
							  Date eventReadTime){
		Date eventTime = null;

		if (isArrival && stopTime.getArrival().hasTime() && stopTimeMatchesSchedule) {
			eventTime = new Date(stopTime.getArrival().getTime() * 1000);
		}
		else if(!isArrival && stopTime.getDeparture().hasTime() && stopTimeMatchesSchedule){
			eventTime = new Date(stopTime.getDeparture().getTime() * 1000);
		}
		else if ((isArrival && stopTime.getArrival().hasDelay()) || (!isArrival && stopTime.getDeparture().hasDelay())) {

			int timeInSeconds = isArrival ? stopTime.getArrival().getDelay() : stopTime.getDeparture().getDelay();
			timeInSeconds = getArrivalDepartureTimeInSeconds(timeInSeconds, scheduledTime, isArrival);

			eventTime = getDateForSecondsFromMidnight(eventReadTime, timeInSeconds);
			logger.debug("Event Time : " + eventTime);
			logger.debug("Time in seconds :" + timeInSeconds);

		} else if (tripUpdate.hasDelay()) {

			int timeInSeconds = tripUpdate.getDelay();
			timeInSeconds = getArrivalDepartureTimeInSeconds(timeInSeconds, scheduledTime, isArrival);

			eventTime = getDateForSecondsFromMidnight(eventReadTime, timeInSeconds);
			logger.debug("Event Time : " + eventTime);
			logger.debug("Time in seconds :" + timeInSeconds);
		}
		else if(stopTimeDelay != null){
			int timeInSeconds = stopTimeDelay;
			timeInSeconds = getArrivalDepartureTimeInSeconds(timeInSeconds, scheduledTime, isArrival);

			eventTime = getDateForSecondsFromMidnight(eventReadTime, timeInSeconds);
			logger.debug("Event Time : " + eventTime);
			logger.debug("Time in seconds :" + timeInSeconds);
		}

		return eventTime;
	}

	private Integer getArrivalDepartureTimeInSeconds(int timeInSeconds, ScheduleTime scheduledTime, boolean isArrival){
		if(isArrival){
			if (scheduledTime.getArrivalTime() != null) {
				timeInSeconds = timeInSeconds + scheduledTime.getArrivalTime();
			}
			else if (scheduledTime.getDepartureTime() != null) {
				timeInSeconds = timeInSeconds + scheduledTime.getDepartureTime();
			}
		} else {
			if (scheduledTime.getDepartureTime() != null) {
				timeInSeconds = timeInSeconds + scheduledTime.getDepartureTime();
			}
			else if (scheduledTime.getArrivalTime() != null) {
				timeInSeconds = timeInSeconds + scheduledTime.getArrivalTime();
			}
		}
		return timeInSeconds;
	}

	Date getDateForSecondsFromMidnight(Date epochTime, int secondsFromMidnight) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(epochTime);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		calendar.add(Calendar.SECOND, secondsFromMidnight);

		return calendar.getTime();

	}

	public int getSecondsIntoDay(long epochTime, ZoneId zoneId) {
		LocalTime localTime = Instant.ofEpochMilli(epochTime).atZone(zoneId).toLocalTime();
		return localTime.toSecondOfDay();
	}

	private String getTripId(DbConfig config, GtfsRealtime.TripDescriptor tripDescriptor) {
		String tripId = tripDescriptor.getTripId();

		if(config.getServiceIdSuffix()){
			Trip trip = BlockAssigner.getInstance().getTripWithServiceIdSuffix(config,tripId);
			if(trip != null) {
				tripId = trip.getId();
			} else {
				logger.warn("No matching trip found for tripId {}", tripId);
			}
		}
		return tripId;
	}

	private boolean stopTimeMatchesStopPath(StopTimeUpdate stopTimeUpdate, StopPath stopPath) {
		if (stopTimeUpdate.hasStopSequence()) {
			if (stopTimeUpdate.getStopSequence() == stopPath.getGtfsStopSeq())
				return true;
			else
				return false;
		} else if (stopTimeUpdate.hasStopId()) {
			if (stopTimeUpdate.getStopId().equals(stopPath.getStopId()))
				return true;
			else
				return false;
		} else
			return false;
	}

	private int getStopPathIndex(StopTimeUpdate tripUpdateStopTime, Trip gtfsTrip){
		int stopPathIndex = -1;

		// Try to get valid stopPathIndex from StopTime
		if (tripUpdateStopTime.hasStopSequence()) {
			try {
				stopPathIndex = getStopPathIndexForStopSequence(gtfsTrip, tripUpdateStopTime.getStopSequence());
			} catch (Exception e) {
				logger.error("Not valid stop sequence {} for trip {}.", tripUpdateStopTime.getStopSequence(),
						gtfsTrip.getId());
			}
		}
		else if (tripUpdateStopTime.hasStopId() && !tripUpdateStopTime.hasStopSequence()) {
			StopPath stopPath = gtfsTrip.getStopPath(tripUpdateStopTime.getStopId());
			if (stopPath != null)
				logger.debug(stopPath.toString());

			stopPathIndex = getStopPathIndexForStopPath(gtfsTrip, stopPath);

		}
		else {
			logger.warn("StopTimeUpdate must have stop id or stop sequence set:" + tripUpdateStopTime.toString());
		}
		return stopPathIndex;
	}

	private int getStopPathIndexForStopSequence(Trip gtfsTrip, int gtfsStopSequence) {
		int index = 0;
		for (StopPath path : gtfsTrip.getStopPaths()) {
			if (path.getGtfsStopSeq() == gtfsStopSequence) {
				return index;
			}
			index++;
		}
		return -1;
	}

	private int getStopPathIndexForStopPath(Trip gtfsTrip, StopPath stopPath) {

		if (gtfsTrip != null && stopPath != null) {
			for (int i = 0; i < gtfsTrip.getNumberStopPaths(); i++) {
				if (gtfsTrip.getStopPath(i).getStopPathId().equals(stopPath.getStopPathId())) {
					return i;
				}
			}
		}
		return -1;
	}

	/**

	 * Processes both the internal and external predictions
	 * 
	 * @param routesAndStops
	 * @param predictionsReadTime
	 *            For keeping track of when the predictions read in. Used for
	 *            determining length of predictions. Should be the same for all
	 *            predictions read in during a polling cycle even if the
	 *            predictions are read at slightly different times. By using the
	 *            same time can easily see from data in db which internal and
	 *            external predictions are associated with each other.
	 */
	@Override
	protected void getAndProcessData(List<RouteAndStops> routesAndStops, Date predictionsReadTime) {
		// Process internal predictions

		// super.getAndProcessData(routesAndStops, predictionsReadTime);

		logger.info("Calling GTFSRealtimePredictionAccuracyModule." + "getAndProcessData()");

		// Get data for all items in the GTFS-RT trip updates feed
		FeedMessage feed = getExternalPredictions();
		DbConfig dbConfig = Core.getInstance().getDbConfig();

		processExternalPredictions(feed, dbConfig);

	}

	class PredictionReadTimeKey {
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + (int) (predictedTime ^ (predictedTime >>> 32));
			result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
			result = prime * result + ((vehicleId == null) ? 0 : vehicleId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PredictionReadTimeKey other = (PredictionReadTimeKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (predictedTime != other.predictedTime)
				return false;
			if (stopId == null) {
				if (other.stopId != null)
					return false;
			} else if (!stopId.equals(other.stopId))
				return false;
			if (vehicleId == null) {
				if (other.vehicleId != null)
					return false;
			} else if (!vehicleId.equals(other.vehicleId))
				return false;
			return true;
		}

		public PredictionReadTimeKey(String stopId, String vehicleId, long predictedTime) {
			super();
			this.stopId = stopId;
			this.vehicleId = vehicleId;
			this.predictedTime = predictedTime;
		}

		String stopId;
		String vehicleId;
		long predictedTime;

		private GTFSRealtimePredictionAccuracyModule getOuterType() {
			return GTFSRealtimePredictionAccuracyModule.this;
		}
	}

	static HashMap<PredictionReadTimeKey, Long> readTimesMap = new HashMap<PredictionReadTimeKey, Long>();


}
