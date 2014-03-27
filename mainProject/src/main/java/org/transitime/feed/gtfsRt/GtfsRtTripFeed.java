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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.ipc.clients.PredictionsInterfaceFactory;
import org.transitime.ipc.data.Prediction;
import org.transitime.ipc.interfaces.PredictionsInterface;
import org.transitime.utils.IntervalTimer;

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

	private final String projectId;
	
	private static final int PREDICTION_MAX_FUTURE_SECS = 25 * 60; // 25 minutes
	
	private static final Logger logger = 
			LoggerFactory.getLogger(GtfsRtTripFeed.class);

	/********************** Member Functions **************************/

	public GtfsRtTripFeed(String projectId) {
		this.projectId = projectId;				
	}

	private TripUpdate createTripUpdate(List<Prediction> predsForTrip) {
		// Create the parent TripUpdate object that is returned.
		TripUpdate.Builder tripUpdate =
				TripUpdate.newBuilder();
				  
		// Add the trip descriptor information
		Prediction firstPred = predsForTrip.get(0);
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
		for (Prediction pred : predsForTrip) {
			StopTimeUpdate.Builder stopTimeUpdate =	StopTimeUpdate.newBuilder()
					.setStopSequence(pred.getStopSequence())
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
		
		// FIXME
		//also need way to more limit prediction, such as to only 20 minutes into future
		
		// Add timestamp
		tripUpdate.setTimestamp(firstPred.getAvlTime());
		
		// Return the results
		return tripUpdate.build();
	}
	
	/**
	 * Creates a GTFS-realtime message for the predictions by trip passed in.
	 * 
	 * @param predsByTrip
	 *            the data to be put into the GTFS-realtime message
	 * @return the GTFS-realtime FeedMessage
	 */
	private FeedMessage createMessage(List<List<Prediction>> predsByTrip) {
		FeedMessage.Builder message = FeedMessage.newBuilder();
		
		FeedHeader.Builder feedheader = FeedHeader.newBuilder()
				.setGtfsRealtimeVersion("1.0")
				.setIncrementality(Incrementality.FULL_DATASET)
				.setTimestamp(System.currentTimeMillis());
		message.setHeader(feedheader);
		  
		// For each trip...
		for (List<Prediction> predsForTrip : predsByTrip) {				
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
	 * Returns lists of list of all predictions for the project. Returns null if
	 * there was a problem getting the data via RMI. There is a separate list of
	 * predictions for each trip.
	 * 
	 * @return List of List of Prediction objects, or null if not available.
	 */
	private List<List<Prediction>> getPredictionsPerTrip() {
		// Get all the predictions, grouped by vehicle, from the server
		PredictionsInterface predictionsInterface = 
				PredictionsInterfaceFactory.get(projectId);
		List<List<Prediction>> predictionsByVehicle;
		try {
			predictionsByVehicle = predictionsInterface.getPredictionsByVehicle(
					PREDICTION_MAX_FUTURE_SECS);
		} catch (RemoteException e) {
			logger.error("Exception when getting vehicles from RMI", e);
			return null;
		}
		
		// Group the predictions by trip instead of by vehicle
		List<List<Prediction>> predictionsByTrip = 
				new ArrayList<List<Prediction>>();
		for (List<Prediction> predictionsForVehicle : predictionsByVehicle) {
			List<Prediction> predictionsForTrip = null;
			
			String previousTripIdForVehicle = null;
			for (Prediction pred : predictionsForVehicle) {
				// If new trip then need a new predictionsForTrip list
				if (!pred.getTripId().equals(previousTripIdForVehicle)) {
					// A new trip so need to use a new trip list
					predictionsForTrip = new ArrayList<Prediction>();
					predictionsByTrip.add(predictionsForTrip);
				}
				previousTripIdForVehicle = pred.getTripId();
				
				// Add this prediction to the proper list of predictions for 
				// the trip.
				predictionsForTrip.add(pred);
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
		List<List<Prediction>> predsByTrip = getPredictionsPerTrip();
		logger.debug("Getting predictions via RMI for " +
				"GtfsRtTripFeed.createMessage() took {} msec", 
				timer.elapsedMsec());
		
		// Use prediction data to create GTFS-RT message and return it.
		return createMessage(predsByTrip);
	}

}
