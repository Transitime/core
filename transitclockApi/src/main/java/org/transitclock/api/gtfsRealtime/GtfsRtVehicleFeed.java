/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License (GPL) as published by the
 * Free Software Foundation, either version 3 of the License, or any later
 * version.
 * 
 * Transitime.org is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Transitime.org . If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitclock.api.gtfsRealtime;

import com.google.transit.realtime.GtfsRealtime.*;
import com.google.transit.realtime.GtfsRealtime.FeedHeader.Incrementality;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.VehicleStopStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.api.utils.AgencyTimezoneCache;
import org.transitclock.core.dataCache.canceledTrip.CanceledTripKey;
import org.transitclock.ipc.clients.ConfigInterfaceFactory;
import org.transitclock.ipc.clients.PredictionsInterfaceFactory;
import org.transitclock.ipc.clients.VehiclesInterfaceFactory;
import org.transitclock.ipc.data.IpcCanceledTrip;
import org.transitclock.ipc.data.IpcVehicleGtfsRealtime;
import org.transitclock.ipc.interfaces.ConfigInterface;
import org.transitclock.ipc.interfaces.VehiclesInterface;
import org.transitclock.utils.Time;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * For creating GTFS-realtime Vehicle feed. The data is obtained via RMI.
 *
 * @author SkiBu Smith
 *
 */
public class GtfsRtVehicleFeed {

	private final String agencyId;

	// For outputting date in GTFS-realtime format
	private SimpleDateFormat gtfsRealtimeDateFormatter = 
			new SimpleDateFormat("yyyyMMdd");
	
	private SimpleDateFormat gtfsRealtimeTimeFormatter = 
			new SimpleDateFormat("HH:mm:ss");
	
	private static final Logger logger = LoggerFactory
			.getLogger(GtfsRtVehicleFeed.class);

	/********************** Member Functions **************************/

	public GtfsRtVehicleFeed(String agencyId) {
		this.agencyId = agencyId;
		
		this.gtfsRealtimeDateFormatter.setTimeZone(AgencyTimezoneCache
				.get(agencyId));
	}

	/**
	 * Takes in IpcGtfsRealtimeVehicle and puts it into a GTFS-realtime
	 * VehiclePosition object.
	 * 
	 * @param vehicleData
	 * @return the resulting VehiclePosition
	 * @throws ParseException
	 */
	private VehiclePosition createVehiclePosition(
			IpcVehicleGtfsRealtime vehicleData, boolean isCanceled) throws ParseException {
		// Create the parent VehiclePosition object that is returned.
		VehiclePosition.Builder vehiclePosition = VehiclePosition.newBuilder();

		// If there is route information then add it via the TripDescriptor
		if (vehicleData.getRouteId() != null
				&& vehicleData.getRouteId().length() > 0) {
			String tripStartDateStr =
					gtfsRealtimeDateFormatter.format(new Date(vehicleData
							.getTripStartEpochTime()));
			TripDescriptor.Builder tripDescriptor =					
					TripDescriptor.newBuilder()
							.setRouteId(vehicleData.getRouteId())
							.setTripId(vehicleData.getTripId())
							
							.setStartDate(tripStartDateStr);

			if(vehicleData.getFreqStartTime()>0)
			{
				String tripStartTimeStr=gtfsRealtimeTimeFormatter.format(new Date(vehicleData.getFreqStartTime()));
				tripDescriptor.setStartTime(tripStartTimeStr);
			}

			TripDescriptor.ScheduleRelationship scheduleRelationship = getScheduleRelationship(vehicleData);

			// Set the relation between this trip and the static schedule. ADDED and CANCELED not supported.
			if(isCanceled){
				tripDescriptor.setScheduleRelationship(TripDescriptor.ScheduleRelationship.CANCELED);
			}
			else if (vehicleData.isTripUnscheduled()) {
				// A trip that is running with no schedule associated to it -
				// this value is used to identify trips defined in GTFS frequencies.txt with exact_times = 0
				tripDescriptor.setScheduleRelationship(TripDescriptor.ScheduleRelationship.UNSCHEDULED);
			} else {
				// Trip that is running in accordance with its GTFS schedule,
				// or is close enough to the scheduled trip to be associated with it.
				tripDescriptor.setScheduleRelationship(scheduleRelationship);
			}

			vehiclePosition.setTrip(tripDescriptor);
			
		}

		// Add the VehicleDescriptor information
		VehicleDescriptor.Builder vehicleDescriptor =
				VehicleDescriptor.newBuilder().setId(vehicleData.getId());
		// License plate information is optional so only add it if not null
		if (vehicleData.getLicensePlate() != null)
			vehicleDescriptor.setLicensePlate(vehicleData.getLicensePlate());
		vehiclePosition.setVehicle(vehicleDescriptor);

		// Add the Position information
		Position.Builder position =
				Position.newBuilder().setLatitude(vehicleData.getLatitude())
						.setLongitude(vehicleData.getLongitude());
		// Heading and speed are optional so only add them if actually a
		// valid number.
		if (!Float.isNaN(vehicleData.getHeading())) {
			position.setBearing(vehicleData.getHeading());
		}
		if (!Float.isNaN(vehicleData.getSpeed())) {
			position.setSpeed(vehicleData.getSpeed());
		}
		vehiclePosition.setPosition(position);

		// Convert the GPS timestamp information to an epoch time as
		// number of milliseconds since 1970.
		long gpsTime = vehicleData.getGpsTime();
		vehiclePosition.setTimestamp(gpsTime / Time.MS_PER_SEC);

		// Set the stop_id if at a stop or going to a stop
		String stopId = vehicleData.getAtOrNextStopId();
		if (stopId != null)
			vehiclePosition.setStopId(stopId);

		// Set current_status part of vehiclePosition if vehicle is actually
		// predictable. If not predictable then the vehicle stop status will
		// not be included in feed since it is not stopped nor in transit to.
		if (vehicleData.isPredictable()) {
			VehicleStopStatus currentStatus =
					vehicleData.isAtStop() ? VehicleStopStatus.STOPPED_AT
							: VehicleStopStatus.IN_TRANSIT_TO;
			vehiclePosition.setCurrentStatus(currentStatus);
			
			if (vehicleData.getAtOrNextGtfsStopSeq() != null)
				vehiclePosition.setCurrentStopSequence(vehicleData.getAtOrNextGtfsStopSeq());
		}

		// Return the results
		return vehiclePosition.build();
	}


	private TripDescriptor.ScheduleRelationship getScheduleRelationship(IpcVehicleGtfsRealtime prediction){

		if(prediction.isCanceled()){
			return TripDescriptor.ScheduleRelationship.CANCELED;
		}

		return TripDescriptor.ScheduleRelationship.SCHEDULED;

	}

	/**
	 * Creates a GTFS-realtime message for the list of ApiVehicle passed in.
	 * 
	 * @param vehicles
	 *            the data to be put into the GTFS-realtime message
	 * @return the GTFS-realtime FeedMessage
	 */
	private FeedMessage createMessage(
			Collection<IpcVehicleGtfsRealtime> vehicles) {
		FeedMessage.Builder message = FeedMessage.newBuilder();

		FeedHeader.Builder feedheader =
				FeedHeader
						.newBuilder()
						.setGtfsRealtimeVersion("1.0")
						.setIncrementality(Incrementality.FULL_DATASET)
						.setTimestamp(
								System.currentTimeMillis() / Time.MS_PER_SEC);
		message.setHeader(feedheader);

		HashMap<String, IpcCanceledTrip> allCanceledTrips = new HashMap<>();

		try {
			allCanceledTrips = PredictionsInterfaceFactory.get(agencyId).getAllCanceledTrips();
		} catch (RemoteException e) {
			logger.error("Exception when getting all canceled trips from RMI", e);
		}

		boolean serviceIdSuffix = getServiceIdSuffix();

		for (IpcVehicleGtfsRealtime vehicle : vehicles) {
			boolean isCanceled = isVehicleAndTripCanceledAndCached(vehicle, allCanceledTrips, serviceIdSuffix);

			FeedEntity.Builder vehiclePositionEntity =
					FeedEntity.newBuilder().setId(vehicle.getId());
			try {
				VehiclePosition vehiclePosition =
						createVehiclePosition(vehicle, isCanceled);
				vehiclePositionEntity.setVehicle(vehiclePosition);
				message.addEntity(vehiclePositionEntity);
			} catch (Exception e) {
				logger.error("Error parsing vehicle data for vehicle={}",
						vehicle, e);
			}
		}

		/*for(Map.Entry<String, IpcCanceledTrip> entry : allCanceledTrips.entrySet()){
			IpcCanceledTrip canceledTrip = entry.getValue();

			if(canceledTrip != null && canceledTrip.getVehicleId() != null){
				FeedEntity.Builder vehiclePositionEntity =
						FeedEntity.newBuilder().setId(canceledTrip.getVehicleId());
				try {
					VehiclePosition vehiclePosition =
							createCanceledVehiclePosition(canceledTrip.getVehicleId(), canceledTrip);
					vehiclePositionEntity.setVehicle(vehiclePosition);
					message.addEntity(vehiclePositionEntity);
				} catch (Exception e) {
					logger.error("Error parsing vehicle data for canceled trip vehicle={}", canceledTrip.getVehicleId(), e);
				}
			}
		}*/

		return message.build();
	}

	private boolean getServiceIdSuffix(){
		ConfigInterface configInterface = ConfigInterfaceFactory.get(agencyId);
		if (configInterface == null) {
			logger.error("Agency ID {} is not valid", agencyId);
			return false;
		}
		try {
			return configInterface.getServiceIdSuffix();
		} catch (Exception e){
			return false;
		}
	}

	/**
	 * Returns collection of all vehicles for the project obtained via RMI.
	 * Returns null if there was a problem getting the data via RMI
	 * 
	 * @return Collection of Vehicle objects, or null if not available.
	 */
	private Collection<IpcVehicleGtfsRealtime> getVehicles() {
		VehiclesInterface vehiclesInterface =
				VehiclesInterfaceFactory.get(agencyId);
		Collection<IpcVehicleGtfsRealtime> vehicles = null;
		try {
			vehicles = vehiclesInterface.getGtfsRealtime();
		} catch (RemoteException e) {
			logger.error("Exception when getting vehicles from RMI", e);
		}
		return vehicles;
	}

	private boolean isVehicleAndTripCanceledAndCached(IpcVehicleGtfsRealtime vehicle,
													  Map<String, IpcCanceledTrip> canceledTrips,
													  boolean serviceIdSuffix) {
		String tripId = vehicle.getTripId();

		IpcCanceledTrip canceledTrip = canceledTrips.get(tripId);

		if(canceledTrip != null && canceledTrip.getTripId() != null && canceledTrip.getTripId().equalsIgnoreCase(tripId)){
			return true;
		}

		return false;
	}

	/**
	 * Gets the Vehicle data from RMI and creates corresponding GTFS-RT vehicle
	 * feed.
	 * 
	 * @return GTFS-RT FeedMessage for vehicle positions
	 */
	public FeedMessage createMessage() {
		Collection<IpcVehicleGtfsRealtime> vehicles = getVehicles();
		return createMessage(vehicles);
	}

	// For getPossiblyCachedMessage()
	private static final DataCache vehicleFeedDataCache = new DataCache();

	/**
	 * For caching Vehicle Positions feed messages.
	 * 
	 * @param agencyId
	 * @param cacheTime
	 * @return
	 */
	public static FeedMessage getPossiblyCachedMessage(String agencyId,
			int cacheTime) {
		FeedMessage feedMessage = vehicleFeedDataCache.get(agencyId, cacheTime);
		if (feedMessage != null)
			return feedMessage;
		
		synchronized(vehicleFeedDataCache) {
		
			// Cache may have been filled while waiting.
			feedMessage = vehicleFeedDataCache.get(agencyId, cacheTime);
			if (feedMessage != null)
				return feedMessage;
		
			GtfsRtVehicleFeed feed = new GtfsRtVehicleFeed(agencyId);
			feedMessage = feed.createMessage();
			vehicleFeedDataCache.put(agencyId, feedMessage);
		}
		
		return feedMessage;
	}
}
