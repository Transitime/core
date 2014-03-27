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
import java.text.ParseException;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.ipc.clients.VehiclesInterfaceFactory;
import org.transitime.ipc.data.Vehicle;
import org.transitime.ipc.interfaces.VehiclesInterface;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

/**
 * For creating GTFS-realtime Vehicle feed.
 *
 * @author SkiBu Smith
 *
 */
public class GtfsRtVehicleFeed {

	private final String projectId;
	
	private static final Logger logger = LoggerFactory
			.getLogger(GtfsRtVehicleFeed.class);

	/********************** Member Functions **************************/

	public GtfsRtVehicleFeed(String projectId) {
		this.projectId = projectId;				
	}
	
	/**
	 * Takes in VehicleData and puts it into a GTFS-realtime 
	 * VehiclePosition object.
	 *  
	 * @param vehicleData
	 * @return the resulting VehiclePosition
	 * @throws ParseException
	 */
	private static VehiclePosition createVehiclePosition(Vehicle vehicleData) 
			throws ParseException {
		// Create the parent VehiclePosition object that is returned.
		VehiclePosition.Builder vehiclePosition =
				  VehiclePosition.newBuilder();
				  
		// If there is route information then add it via the TripDescriptor
		if (vehicleData.getRouteId() != null && vehicleData.getRouteId().length() > 0) {
			TripDescriptor.Builder tripDescriptor = TripDescriptor.newBuilder()
					.setRouteId(vehicleData.getRouteId())
					.setTripId(vehicleData.getTripId());
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
		Position.Builder position = Position.newBuilder()
				.setLatitude(vehicleData.getLatitude())
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
		vehiclePosition.setTimestamp(gpsTime);
		
		// Return the results
		return vehiclePosition.build();
	}

	/**
	 * Creates a GTFS-realtime message for the list of VehicleData passed in.
	 * 
	 * @param vehicles
	 *            the data to be put into the GTFS-realtime message
	 * @return the GTFS-realtime FeedMessage
	 */
	private FeedMessage createMessage(Collection<Vehicle> vehicles) {
		FeedMessage.Builder message = FeedMessage.newBuilder();
		
		FeedHeader.Builder feedheader = FeedHeader.newBuilder()
				.setGtfsRealtimeVersion("1.0")
				.setTimestamp(System.currentTimeMillis());
		message.setHeader(feedheader);
		  
		for (Vehicle vehicle : vehicles) {
			FeedEntity.Builder vehiclePositionEntity = FeedEntity.newBuilder()
					.setId(vehicle.getId());

			try {
				VehiclePosition vehiclePosition = createVehiclePosition(vehicle);
	    		vehiclePositionEntity.setVehicle(vehiclePosition);	    		
	    		message.addEntity(vehiclePositionEntity);
			} catch (Exception e) {
				// Output error message
				System.err.println("Error parsing vehicle data. " + 
						e.getMessage() + ".\n" + 
						vehicle);
				e.printStackTrace();
			}
		}		
		
		return message.build();
	}

	/**
	 * Returns collection of all vehicles for the project. Returns
	 * null if there was a problem getting the data via RMI
	 * 
	 * @return Collection of Vehicle objects, or null if not available.
	 */
	private Collection<Vehicle> getVehicles() {
		VehiclesInterface vehiclesInterface = 
				VehiclesInterfaceFactory.get(projectId);
		Collection<Vehicle> vehicles = null;
		try {
			vehicles = vehiclesInterface.get();
		} catch (RemoteException e) {
			logger.error("Exception when getting vehicles from RMI", e);
		}
		return vehicles;
	}
	
	/**
	 * Gets the Vehicle data from RMI and creates corresponding
	 * GTFS-RT vehicle feed.
	 * 
	 * @return GTFS-RT FeedMessage for vehicle positions
	 */
	public FeedMessage createMessage() {
		Collection<Vehicle> vehicles = getVehicles();
		return createMessage(vehicles);
	}
}
