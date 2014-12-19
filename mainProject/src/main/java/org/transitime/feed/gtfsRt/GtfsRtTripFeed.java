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

package org.transitime.feed.gtfsRt;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.ipc.clients.PredictionsInterfaceFactory;
import org.transitime.ipc.data.IpcPrediction;
import org.transitime.ipc.data.IpcPredictionsForRouteStopDest;
import org.transitime.utils.IntervalTimer;
import org.transitime.utils.Time;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate.ScheduleRelationship;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.FeedHeader.Incrementality;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
 
/**
 * For creating GTFS-realtime trip feed.
 *
 * @author SkiBu Smith
 *
 */
public class GtfsRtTripFeed {

	private final String agencyId;
	
	private static final int PREDICTION_MAX_FUTURE_SECS = 25 * 60; // 25 minutes
	
	private static final Logger logger = 
			LoggerFactory.getLogger(GtfsRtTripFeed.class);

	/********************** Member Functions **************************/

	public GtfsRtTripFeed(String agencyId) {
		this.agencyId = agencyId;				
	}

	private TripUpdate createTripUpdate(List<IpcPrediction> predsForTrip) {
		// Create the parent TripUpdate object that is returned.
		TripUpdate.Builder tripUpdate =
				TripUpdate.newBuilder();
				  
		// Add the trip descriptor information
		IpcPrediction firstPred = predsForTrip.get(0);
		TripDescriptor.Builder tripDescriptor = TripDescriptor.newBuilder();
		if (firstPred.getRouteId() != null)
			tripDescriptor.setRouteId(firstPred.getRouteId());
		if (firstPred.getRouteId() != null)
			tripDescriptor.setTripId(firstPred.getTripId());
		tripUpdate.setTrip(tripDescriptor);
		
		// Add the VehicleDescriptor information
		VehicleDescriptor.Builder vehicleDescriptor = 
			VehicleDescriptor.newBuilder().setId(firstPred.getVehicleId());
		tripUpdate.setVehicle(vehicleDescriptor);

		// Add the StopTimeUpdate information for each prediction
		for (IpcPrediction pred : predsForTrip) {
			StopTimeUpdate.Builder stopTimeUpdate =	StopTimeUpdate.newBuilder()
					.setStopSequence(pred.getGtfsStopSeq())
					.setStopId(pred.getStopId());
			StopTimeEvent.Builder stopTimeEvent = StopTimeEvent.newBuilder();
			stopTimeEvent.setTime(pred.getTime());
			if (pred.isArrival())
				stopTimeUpdate.setArrival(stopTimeEvent);
			else
				stopTimeUpdate.setDeparture(stopTimeEvent);
			
			stopTimeUpdate.setScheduleRelationship(ScheduleRelationship.SCHEDULED);
			tripUpdate.addStopTimeUpdate(stopTimeUpdate);
		}
		
		// Add timestamp
		tripUpdate.setTimestamp(firstPred.getAvlTime() / Time.MS_PER_SEC);
		
		// Return the results
		return tripUpdate.build();
	}
	
	/**
	 * Creates a GTFS-realtime message for the predictions by trip passed in.
	 * 
	 * @param predsByTripMap
	 *            the data to be put into the GTFS-realtime message
	 * @return the GTFS-realtime FeedMessage
	 */
	private FeedMessage createMessage(Map<String, List<IpcPrediction>> predsByTripMap) {
		FeedMessage.Builder message = FeedMessage.newBuilder();
		
		FeedHeader.Builder feedheader = FeedHeader.newBuilder()
				.setGtfsRealtimeVersion("1.0")
				.setIncrementality(Incrementality.FULL_DATASET)
				.setTimestamp(System.currentTimeMillis() / Time.MS_PER_SEC);
		message.setHeader(feedheader);
		  
		// For each trip...
		for (List<IpcPrediction> predsForTrip : predsByTripMap.values()) {				
			// Create feed entity for each trip
			FeedEntity.Builder feedEntity = FeedEntity.newBuilder()
					.setId(predsForTrip.get(0).getTripId());
			try {				
				TripUpdate tripUpdate = createTripUpdate(predsForTrip);
				feedEntity.setTripUpdate(tripUpdate);		
	    		message.addEntity(feedEntity);
			} catch (Exception e) {
				// Output error message
				System.err.println("Error parsing trip update data. " + 
						e.getMessage() + ".\n" + 
						predsForTrip);
				e.printStackTrace();
			}
		}		
		
		return message.build();
	}

	/**
	 * Returns map of all predictions for the project. Returns null if there was
	 * a problem getting the data via RMI. There is a separate list of
	 * predictions for each trip. The map is keyed by tripId.
	 * 
	 * @return Map keyed on tripId of List of Predictions for the trip, or null
	 *         if could not get data from server.
	 */
	private Map<String, List<IpcPrediction>> getPredictionsPerTrip() {
		// Get all the predictions, grouped by vehicle, from the server
		List<IpcPredictionsForRouteStopDest> allPredictionsByStop;
		try {
			allPredictionsByStop = PredictionsInterfaceFactory.get(agencyId)
					.getAllPredictions(PREDICTION_MAX_FUTURE_SECS);
		} catch (RemoteException e) {
			logger.error("Exception when getting vehicles from RMI", e);
			return null;
		}
		
		// Group the predictions by trip instead of by vehicle
		Map<String, List<IpcPrediction>> predictionsByTrip = 
				new HashMap<String, List<IpcPrediction>>();
		for (IpcPredictionsForRouteStopDest predictionsForStop : 
				allPredictionsByStop) {
			for (IpcPrediction prediction : 
					predictionsForStop.getPredictionsForRouteStop()) {
				String tripId = prediction.getTripId();
				List<IpcPrediction> predsForTrip = predictionsByTrip.get(tripId);
				if (predsForTrip == null) {
					// A new trip so need to use a new trip list
					predsForTrip = new ArrayList<IpcPrediction>();
					predictionsByTrip.put(tripId, predsForTrip);					
				}
				
				predsForTrip.add(prediction);
			}
		}
		
		// Return results
		return predictionsByTrip;
	}

	/**
	 * Gets the Vehicle data from RMI and creates corresponding
	 * GTFS-RT vehicle feed.
	 * 
	 * @return GTFS-RT FeedMessage for vehicle positions
	 */
	public FeedMessage createMessage() {
		// Get prediction data from server
		IntervalTimer timer = new IntervalTimer();
		Map<String, List<IpcPrediction>> predsByTrip = getPredictionsPerTrip();
		logger.debug("Getting predictions via RMI for " +
				"GtfsRtTripFeed.createMessage() took {} msec", 
				timer.elapsedMsec());
		
		// Use prediction data to create GTFS-RT message and return it.
		return createMessage(predsByTrip);
	}

	// For getPossiblyCachedMessage()
	private static final DataCache tripFeedDataCache = new DataCache();
	
	/**
	 * For caching Vehicle Positions feed messages.
	 * 
	 * @param agencyId
	 * @param cacheTime
	 * @return
	 */
	public static FeedMessage getPossiblyCachedMessage(String agencyId, int cacheTime) {
	    FeedMessage feedMessage = tripFeedDataCache.get(agencyId, cacheTime);
	    if (feedMessage != null)
		return feedMessage;
	    
	    GtfsRtTripFeed feed = new GtfsRtTripFeed(agencyId);
	    feedMessage = feed.createMessage();
	    tripFeedDataCache.put(agencyId, feedMessage);
	    return feedMessage;
	}

}
