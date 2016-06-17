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

package org.transitime.core.predAccuracy.gtfsrt;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.StringConfigValue;
import org.transitime.core.predAccuracy.PredAccuracyPrediction;
import org.transitime.core.predAccuracy.PredictionAccuracyModule;
import org.transitime.db.structs.ScheduleTime;
import org.transitime.db.structs.StopPath;
import org.transitime.db.structs.Trip;
import org.transitime.gtfs.DbConfig;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;

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
			"transitime.predAccuracy.gtfsTripUpdateUrl", "http://127.0.0.1:8091/trip-updates",
			"URL to access gtfs-rt trip updates.");

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

			FeedMessage feed = FeedMessage.parseFrom(url.openStream());
			logger.info("Prediction read successfully from URL={}", getGtfstripupdateurl().getValue());
			return feed;
		} catch (Exception e) {
			logger.error("Problem when getting data from GTFS realtime trip updates URL={}", url, e);
			return null;
		}
	}

	/**
	 * Takes data from XML Document object and processes it and calls
	 * storePrediction() on the predictions.
	 * 
	 * @param feed
	 * @param predictionsReadTime
	 */
	private void processExternalPredictions(FeedMessage feed, Date predictionsReadTime) {

		// If couldn't read data from feed then can't process it
		if (feed == null)
			return;

		// So can look up direction in database
		DbConfig dbConfig = Core.getInstance().getDbConfig();

		logger.info("Processing GTFS-rt feed.....");
		for (FeedEntity entity : feed.getEntityList()) {
			if (entity.hasTripUpdate()) {
				TripUpdate update = entity.getTripUpdate();
				List<StopTimeUpdate> stopTimes = update.getStopTimeUpdateList();
				
				for (StopTimeUpdate stopTime : stopTimes) {
					if (stopTime.hasArrival() || stopTime.hasDeparture()) {

						String direction = null;

						if (update.getTrip().hasDirectionId())
							direction = "" + update.getTrip().getDirectionId();

						if (update.getTrip() != null) {
							Trip trip = dbConfig.getTrip(update.getTrip().getTripId());
							if (trip != null) {
								direction = trip.getDirectionId();
							} else {
								logger.error("Got tripTag={} but no such trip in " + "the configuration.",
										update.getTrip().getTripId());
							}
						}
						ScheduleTime scheduledTime=null;
						
						logger.debug("Processing : " +   stopTime);
						/* use stop as means for getting scheduled time */
						
						Trip gtfsTrip = dbConfig.getTrip(update.getTrip().getTripId());
												
						if(gtfsTrip!=null)
							logger.debug("Trip loaded.");
						
						if(stopTime.hasStopSequence())
						{							
							try {
								scheduledTime = gtfsTrip.getScheduleTimes().get(stopTime.getStopSequence());
							} catch (Exception e) {
								logger.error("Not valid stop sequence {} for trip {}." , stopTime.getStopSequence(), gtfsTrip.getId());
							}
						}							
						else if(stopTime.hasStopId() && !stopTime.hasStopSequence())
						{												
							StopPath stopPath = gtfsTrip.getStopPath(stopTime.getStopId());
																					
							if(stopPath!=null)
								logger.debug(stopPath.toString());
	
							int stopPathIndex = getStopPathIndex(gtfsTrip, stopPath);
							
							logger.debug("StopPathIndex : "+stopPathIndex);
	
							scheduledTime = gtfsTrip.getScheduleTime(stopPathIndex);														
						}	
						
						if(scheduledTime!=null)
							logger.debug(scheduledTime.toString());
						else
							logger.debug("No schedule time found");
						
						Date eventTime = null;
						if(scheduledTime!=null)
						{
							eventTime = null;
							if (stopTime.hasArrival()) {
								if(stopTime.getArrival().hasTime())
								{
									eventTime = new Date(stopTime.getArrival().getTime());							
									
									logger.debug("Event Time : "+ eventTime );
								}else
								if (stopTime.getArrival().hasDelay()) {
									
									int timeInSeconds = scheduledTime.getDepartureTime() + stopTime.getArrival().getDelay();
	
									Calendar calendar = Calendar.getInstance();
									calendar.set(Calendar.HOUR_OF_DAY, 0);
									calendar.add(Calendar.SECOND, timeInSeconds);
	
									eventTime = calendar.getTime();
									logger.debug("Event Time : "+ eventTime );
									logger.debug("Time in seconds :" + timeInSeconds );
									
								} else if(update.hasDelay()) {
									
									int timeInSeconds = scheduledTime.getDepartureTime() + update.getDelay();
	
									Calendar calendar = Calendar.getInstance();
									calendar.set(Calendar.HOUR_OF_DAY, 0);
									calendar.add(Calendar.SECOND, timeInSeconds);
	
									eventTime = calendar.getTime();
									logger.debug("Event Time : "+ eventTime );
									logger.debug("Time in seconds :" + timeInSeconds );
								}
								if (eventTime != null) {
									
									logger.info(
											"Storing external prediction routeId={}, " + "directionId={}, tripId={}, vehicleId={}, "
													+ "stopId={}, prediction={}, isArrival={}",
													gtfsTrip.getRouteId(), direction, update.getTrip().getTripId(),
											update.getVehicle().getId(), stopTime.getStopId(),
											eventTime, true);
	
									logger.info("Prediction in milliseconds is {} and converted is {}",
											eventTime.getTime(),
											eventTime);
	
									
									PredAccuracyPrediction pred = new PredAccuracyPrediction(gtfsTrip.getRouteId(),
											direction, stopTime.getStopId(), update.getTrip().getTripId(),
											update.getVehicle().getId(), eventTime,
											new Date(feed.getHeader().getTimestamp() * 1000), true, new Boolean(false),
											"GTFS-rt");
	
									storePrediction(pred);
								}
							}
							eventTime = null;
							if (stopTime.hasDeparture()) {
								if(stopTime.getDeparture().hasTime())
								{
									eventTime = new Date(stopTime.getDeparture().getTime());
									logger.debug("Event Time : "+ eventTime );
								}else
								if (stopTime.getDeparture().hasDelay()) {
									int timeInSeconds = scheduledTime.getDepartureTime() + stopTime.getDeparture().getDelay();
	
									Calendar calendar = Calendar.getInstance();
									calendar.set(Calendar.HOUR_OF_DAY, 0);
									calendar.add(Calendar.SECOND, timeInSeconds);
	
									eventTime = calendar.getTime();
									logger.debug("Event Time : "+ eventTime );
									logger.debug("Time in seconds :" + timeInSeconds );
								} else if(update.hasDelay()) {
									
									int timeInSeconds = scheduledTime.getDepartureTime() + update.getDelay();
	
									Calendar calendar = Calendar.getInstance();
									calendar.set(Calendar.HOUR_OF_DAY, 0);
									calendar.add(Calendar.SECOND, timeInSeconds);
	
									eventTime = calendar.getTime();
									logger.debug("Event Time : "+ eventTime );
									logger.debug("Time in seconds :" + timeInSeconds );
								} 
								if (eventTime != null) {
									
									logger.info(
											"Storing external prediction routeId={}, " + "directionId={}, tripId={}, vehicleId={}, "
													+ "stopId={}, prediction={}, isArrival={}",
													gtfsTrip.getRouteId(), direction, update.getTrip().getTripId(),
											update.getVehicle().getId(), stopTime.getStopId(),
											eventTime, false);
	
									logger.info("Prediction in milliseonds is {} and converted is {}",
											eventTime.getTime(),
											eventTime);
	
									
									PredAccuracyPrediction pred = new PredAccuracyPrediction(gtfsTrip.getRouteId(),
											direction, stopTime.getStopId(), update.getTrip().getTripId(),
											update.getVehicle().getId(), eventTime,
											new Date(feed.getHeader().getTimestamp() * 1000), false, new Boolean(false),
											"GTFS-rt");
	
									storePrediction(pred);
								}
	
							}
						}						
					} else {
						logger.debug("No predictions for vehicleId={} for stop={}", update.getVehicle().getId(),
								stopTime.getStopId());
					}
				}
			}
		}
	}

	private int getStopPathIndex(Trip gtfsTrip, StopPath stopPath) {
		// TODO Auto-generated method stub

		if (gtfsTrip != null && stopPath != null) {
			int i = 0;
			for (StopPath nextStopPath : gtfsTrip.getStopPaths()) {
				if (nextStopPath.basicEquals(stopPath)) {
					return i;
				}
				i++;
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
		super.getAndProcessData(routesAndStops, predictionsReadTime);

		logger.info("Calling GTFSRealtimePredictionAccuracyModule." + "getAndProcessData()");

		// Get data for all items in the GTFS-RT trip updates feed
		FeedMessage feed = getExternalPredictions();

		processExternalPredictions(feed, predictionsReadTime);

	}
}
