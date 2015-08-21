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
import org.transitime.config.StringConfigValue;
import org.transitime.core.predAccuracy.PredAccuracyPrediction;
import org.transitime.core.predAccuracy.PredictionAccuracyModule;
import org.transitime.db.structs.Trip;
import org.transitime.gtfs.DbConfig;
import org.transitime.modules.Module;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
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

	// For when requesting predictions from external GTFS RT URL
	private static final int timeoutMsec = 20000;
	
	private static final Logger logger = LoggerFactory
			.getLogger(GTFSRealtimePredictionAccuracyModule.class);

	/********************** Config Params **************************/
	
	private static final StringConfigValue gtfsTripUpdateUrl = 
			new StringConfigValue("transitime.predAccuracy.gtfsTripUpdateUrl", 
					"http://127.0.0.1:8091/trip-updates",
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
						 	
						 	if(update.getTrip().hasDirectionId())
						 		direction=""+update.getTrip().getDirectionId();
						 							 	
							if (update.getTrip() != null) {
								Trip trip = dbConfig.getTrip(update.getTrip().getTripId());
								if (trip != null) {
									direction = trip.getDirectionId();
								} else {
									logger.error("Got tripTag={} but no such trip in "
											+ "the configuration.", update.getTrip().getTripId());
								}
							}
							
							logger.info("Storing external prediction routeId={}, "
									+ "directionId={}, tripId={}, vehicleId={}, "
									+ "stopId={}, prediction={}, isArrival={}",
									update.getTrip().getRouteId(), direction, update.getTrip().getTripId(), update.getVehicle().getId(), stopTime.getStopId(),
									new Date(stopTime.getArrival().getTime()*1000), true);
						 	
						 	logger.info("Prediction in milliseonds is {} and converted is {}",stopTime.getArrival().getTime()*1000,  new Date(stopTime.getArrival().getTime()*1000));
													 							 							 							
						 	if(stopTime.hasArrival())
						 	{
							 	PredAccuracyPrediction pred = new PredAccuracyPrediction(
								update.getTrip().getRouteId(), 
								direction, 
								stopTime.getStopId(), 
								update.getTrip().getTripId(), 
								update.getVehicle().getId(),									
								new Date(stopTime.getArrival().getTime()*1000) , 
								new Date(feed.getHeader().getTimestamp()*1000),													
								true,
								new Boolean(false), 
								"GTFS-rt");
							 	
							 	storePrediction(pred);
						 	}
						 	if(stopTime.hasDeparture())
						 	{
						 		PredAccuracyPrediction pred = new PredAccuracyPrediction(
								update.getTrip().getRouteId(), 
								direction, 
								stopTime.getStopId(), 
								update.getTrip().getTripId(), 
								update.getVehicle().getId(),	
								new Date(stopTime.getDeparture().getTime()*1000) , 
								new Date(feed.getHeader().getTimestamp()*1000),													
								false,
								new Boolean(false), 
								"GTFS-rt");
						 								 		 									
								storePrediction(pred);
						 	}						 							 	
					 }					
					 else
					 {
						 logger.debug("No predictions for vehicleId={} for stop={}",update.getVehicle().getId(),stopTime.getStopId());
					 }
				 }
		     }
		 }			
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
