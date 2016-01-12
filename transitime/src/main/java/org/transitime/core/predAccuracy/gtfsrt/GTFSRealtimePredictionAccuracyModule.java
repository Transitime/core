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
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.ClassConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.core.predAccuracy.PredAccuracyPrediction;
import org.transitime.core.predAccuracy.PredictionAccuracyModule;
import org.transitime.db.structs.ScheduleTime;
import org.transitime.db.structs.StopPath;
import org.transitime.db.structs.Trip;
import org.transitime.db.structs.TripPattern;
import org.transitime.gtfs.DbConfig;
import org.transitime.utils.Time;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;

/**
 * Reads in external prediction data from a GTFS realtime trip updates feed and stores the data in
 * memory. Then when arrivals/departures occur the prediction accuracy can be
 * determined and stored.
 *
 * @author Sean Og Crudden
 *
 */
public class GTFSRealtimePredictionAccuracyModule extends PredictionAccuracyModule {

	private static final Logger logger = LoggerFactory
			.getLogger(GTFSRealtimePredictionAccuracyModule.class);

	/********************** Config Params **************************/
	
	private static final StringConfigValue gtfsTripUpdateUrl = 
			new StringConfigValue("transitime.predAccuracy.gtfsTripUpdateUrl", 
					"http://127.0.0.1:8091/trip-updates",
					"URL to access gtfs-rt trip updates.");
		
  private static ClassConfigValue translatorConfig =
      new ClassConfigValue("transitime.predAccuracy.RtTranslator", null, 
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
	 * @throws Exception 
	 */
	public GTFSRealtimePredictionAccuracyModule(String agencyId) throws Exception {
		super(agencyId);
		if (translatorConfig.getValue() != null) {
		  logger.info("instantiating translator {}", translatorConfig.getValue());
		  translator = (GTFSRealtimeTranslator) translatorConfig.getValue().newInstance();
		}
	}

	
	
	/**
	 * Gets GTFS realtime feed all routes from URL and return FeedMessage
	 * 
	 * @return the FeedMessage to be processed
	 */
	private FeedMessage getExternalPredictions() {
				
		// Will just read all data from gtfs-rt url
		URL url=null;
		logger.info("Getting predictions from API using URL={}", getGtfstripupdateurl().getValue());
		
		try {
			// Create the connection
			url = new URL(getGtfstripupdateurl().getValue());
			
			FeedMessage feed = FeedMessage.parseFrom(url.openStream());			   
			logger.info("Prediction read successfully from URL={}",getGtfstripupdateurl().getValue());
			return feed;
		} catch (Exception e) {
			logger.error("Problem when getting data from GTFS realtime trip updates URL={}", 
					url, e);
			return null;
		}
	}
	
	/**
	 * Takes data from XML Document object and processes it and
	 * calls storePrediction() on the predictions.
	 * 
	 * @param feed
	 * @param predictionsReadTime
	 */
	private void processExternalPredictions(
			FeedMessage feed,
			Date predictionsReadTime) {

		// If couldn't read data from feed then can't process it		
		if (feed == null)		
			return;
		
		// So can look up direction in database
		DbConfig dbConfig = Core.getInstance().getDbConfig();
		
		logger.info("Processing GTFS-rt feed.....");
		Date readTime = getPredictedReadTimeFromHeader(feed.getHeader());
		
		 for (FeedEntity entity : feed.getEntityList()) 
		 {
			 if (entity.hasTripUpdate()) 
			 {
				 TripUpdate update = entity.getTripUpdate();
				 List<StopTimeUpdate> stopTimes = update.getStopTimeUpdateList();
				 for(StopTimeUpdate stopTime : stopTimes)
				 {					 
					 if(stopTime.hasArrival()||stopTime.hasDeparture())
					 {
					
						 	String direction=null;
						 	String stopId = cleanStopId(stopTime.getStopId());
						 	
						 	if(update.getTrip().hasDirectionId())
						 		direction=""+update.getTrip().getDirectionId();
						 							 	
						 	Trip trip = null;
							if (update.getTrip() != null) {
								trip = dbConfig.getTrip(update.getTrip().getTripId());
								if (trip != null) {
									direction = trip.getDirectionId();
								} else {
									logger.error("Got tripTag={} but no such trip in "
											+ "the configuration.", update.getTrip().getTripId());
									continue;
								}
							}
							
							Date prediction = getPredictedTimeFromEvent(stopTime, trip, readTime.getTime());
							if (prediction == null) {
							  logger.error("could not compute prediction for trip {} and stopId {}", trip.getId(), stopId);
							  continue;
							}
							logger.info("Storing external prediction routeId={}, "
									+ "directionId={}, tripId={}, vehicleId={}, "
									+ "stopId={}, prediction={}, isArrival={}",
									update.getTrip().getRouteId(), direction, update.getTrip().getTripId(), update.getVehicle().getId(), stopId,
									prediction, true);
						 	
						 	PredAccuracyPrediction pred = new PredAccuracyPrediction(
							update.getTrip().getRouteId(), 
							direction, 
							stopId, 
							update.getTrip().getTripId(), 
							update.getVehicle().getId(),									
							prediction, 
							readTime,													
							(stopTime.hasArrival()),
							new Boolean(false), 
							"GTFS-rt");
						 	
						 	storePrediction(pred);
					 }					
					 else
					 {
					   Integer delay = null;
					   if (stopTime.hasArrival() && stopTime.getArrival().hasDelay()) {
					     delay = stopTime.getArrival().getDelay();
					   } else if (stopTime.hasDeparture() && stopTime.getDeparture().hasDelay()) {
					     delay = stopTime.getDeparture().getDelay();
					   }
					   if (delay != null) {
					     
					   }
						 logger.info("No predictions for vehicleId={} for stop={} with delay={}",update.getVehicle().getId(),stopTime.getStopId(), delay);
					 }
				 }
		     }
		 }			
	}
	
	private Date getPredictedReadTimeFromHeader(FeedHeader header) {
	  if (translator != null) {
	    return translator.parseFeedHeaderTimestamp(header);
	  }
	  return new Date(header.getTimestamp()*1000);
  }



  private Date getPredictedTimeFromEvent(StopTimeUpdate stopTime, Trip trip, long serviceDate) {
	  if (trip == null) {
	    logger.error("no trip provided for stopTime {}", stopTime);
	    return null;
	  }
	  StopTimeEvent event = null;
	  if (stopTime.hasArrival()) {
	    event = stopTime.getArrival();
	  } else if (stopTime.hasDeparture()) {
	    event = stopTime.getDeparture();
	  }

	  if (event.hasTime()) {
	    // we have an exact time, use it
	    return new Date(event.getTime() * 1000);
	  }
	  
	  if (event.hasDelay()) {
	    // we have a delay relative to the included stop
	    // we need to look up the scheduled time of that stop
	    if (stopTime.hasStopSequence()) {
	      // we were given a stop sequence, index that into schedule times
	      int stopSeq = stopTime.getStopSequence();
	      ScheduleTime scheduleTime = trip.getScheduleTimes().get(stopSeq);
	      return new Date(serviceDate + scheduleTime.getTime()*1000 + event.getDelay()*1000);
	    }
	  
	    if (stopTime.hasStopId()) {
	      // we were given a stop id, we need to 
	      TripPattern tripPattern = trip.getTripPattern();
	      if (tripPattern == null) {
	        logger.error("missing tripPattern for trip {}", trip.getId());
	        return null;
	      }
	      
	      String stopId = cleanStopId(stopTime.getStopId());
	      int i = 0;
	      // scan through patterns to find our stop
	      // use that index to get schedule time
	      for (StopPath stopPath : tripPattern.getStopPaths()) {
	        String stopPathStop = extractStopPathStop(stopPath.getId());
	        // we only need to check the 'to' stop
	        if (stopId.equals(stopPathStop)) {
            if (i < trip.getScheduleTimes().size()) {
              ScheduleTime scheduleTime = trip.getScheduleTimes().get(i);
              return new Date(serviceDate + scheduleTime.getTime()*1000 + event.getDelay()*1000);
            } else {
              logger.error("invalid stopSeq {} for trip {} with {} sequences", 
                  i, trip.getId(), trip.getScheduleTimes().size());
              return null;
            }
          }
          i++;
	      }
	      // we fell through without finding a matching stop
        logger.error("missing stopPath for trip {} and stopId {} when first stopId was {}", 
            trip.getId(), stopTime.getStopId(), tripPattern.getStopPaths().get(0).getStopId());
        return null;
	    }
	  }
	  
    return null;
  }



 private String extractStopPathStop(String id) {
   String[] stopPathStops = id.split("_");
   if (stopPathStops.length == 2)
     return stopPathStops[1];
   return stopPathStops[2];
  }



private String cleanStopId(String stopId) {
   if (translator != null) {
     return translator.parseStopId(stopId);
   }
   return stopId;
  }

  private long getTodayInMilliseconds(long serviceDate) {
    return Time.getStartOfDay(new Date(serviceDate));
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
	protected void getAndProcessData(List<RouteAndStops> routesAndStops, 
			Date predictionsReadTime) {
		// Process internal predictions
		super.getAndProcessData(routesAndStops, predictionsReadTime);
		
		logger.info("Calling GTFSRealtimePredictionAccuracyModule."
				+ "getAndProcessData()");
		
		// Get data for all items in the GTFS-RT trip updates feed		
		FeedMessage feed = getExternalPredictions();
			
		processExternalPredictions(feed, predictionsReadTime);
		
	}
}
