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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.AvlReport.AssignmentType;
import org.transitime.utils.IntervalTimer;
import org.transitime.utils.MathUtils;
import org.transitime.utils.Time;

import com.google.protobuf.CodedInputStream;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

/**
 * Reads in GTFS-realtime Vehicle Positions file and converts them into List of
 * AvlReport objects. This class should be inherited from such that
 * handleAvlReport() of the superclass will process the AVL data one report at a
 * time. This way don't have to fill up memory with a giant list of AvlReports.
 * 
 * @author SkiBu Smith
 * 
 */
public class GtfsRtVehiclePositionsReader {

	private static final Logger logger = LoggerFactory
			.getLogger(GtfsRtVehiclePositionsReader.class);

	/********************** Member Functions **************************/
	
	/**
	 * Returns the vehicleID. If vehicle ID not available then returns vehicle
	 * label. Returns null if no VehicleDescription associated with the vehicle
	 * or if no ID or label associated with the VehicleDescription.
	 * 
	 * @param vehicle
	 * @return vehicle ID or label or null if there isn't one
	 */
	private static String getVehicleId(VehiclePosition vehicle) {
		if (!vehicle.hasVehicle()) {
			return null;
		}
		VehicleDescriptor desc = vehicle.getVehicle();
		
		// Return vehicle ID if there is one
		if (desc.hasId())
			return desc.getId();
		
		// No vehicle ID so return vehicle label if there is one
		if (desc.hasLabel())
			return desc.getLabel();

		// No ID nor label so return null
		return null;
	}

	/**
	 * Returns the vehicleID. Returns null if no VehicleDescription associated
	 * with the vehicle or if no ID associated with the VehicleDescription.
	 * 
	 * @param vehicle
	 * @return vehicle ID or null if there isn't one
	 */
	private static String getLicensePlate(VehiclePosition vehicle) {
		if (!vehicle.hasVehicle()) {
			return null;
		}
		VehicleDescriptor desc = vehicle.getVehicle();
		if (!desc.hasLicensePlate()) {
			return null;
		}
		return desc.getLicensePlate();
	}
	
	/**
	 * For each vehicle in the GTFS-realtime message put AvlReport into list.
	 * 
	 * @param message
	 *            Contains all of the VehiclePosition objects
	 * @return List of AvlReports
	 */
	private static Collection<AvlReport> processMessage(FeedMessage message) {
		logger.info("Processing each individual AvlReport...");
		IntervalTimer timer = new IntervalTimer();
		
		// The return value for the method
		Collection<AvlReport> avlReportsReadIn = new ArrayList<AvlReport>();
		
		// For each entity/vehicle process the data
		for (FeedEntity entity : message.getEntityList()) {
			// If no vehicles in the entity then nothing to process 
			if (!entity.hasVehicle())
				continue;
			
			// Get the object describing the vehicle
			VehiclePosition vehicle = entity.getVehicle();
			
			// Determine vehicle ID. If no vehicle ID then can't handle it.
			String vehicleId = getVehicleId(vehicle);
			if (vehicleId == null) 
				continue;

			// Determine the GPS time. If time is not available then use the
			// current time. This is really a bad idea though because the 
			// latency will be quite large, resulting in inaccurate predictions
			// and arrival times. But better than not having a time at all.
			long gpsTime;
			if (vehicle.hasTimestamp())
				gpsTime = vehicle.getTimestamp()*Time.MS_PER_SEC;
			else {
				logger.warn("For vehicleId={} GPS time not available in "
						+ "GTFS-realtime feed so using system time, which is "
						+ "not accurate!",
						vehicleId);
				gpsTime = System.currentTimeMillis();
			}
			
			// Determine the position data
		    Position position = vehicle.getPosition();
		    
		    // If no position then cannot handle the data
		    if (!position.hasLatitude() || !position.hasLongitude())
		    	continue;
		    
		    double lat = position.getLatitude();
		    double lon = position.getLongitude();
		    
		    // Handle speed and heading
		    float speed = Float.NaN;
		    if (position.hasSpeed()) {
		    	speed = position.getSpeed();
		    }
		    float heading = Float.NaN;
		    if (position.hasBearing()) {
		    	heading = position.getBearing();
		    	
		    	// rtd-denver at least sets bearing to 65535.0 when vehicle 
		    	// not moving. For this special case reset heading to NaN.
		    	if (heading == 65535.0)
		    		heading = Float.NaN;
		    }
		    
			// Create the core AVL object. The feed can provide a silly amount 
		    // of precision so round to just 5 decimal places.
            // AvlReport is expecting time in ms while the proto provides it in
		    // seconds
			AvlReport avlReport = new AvlReport(vehicleId, 
					gpsTime,
					MathUtils.round(lat, 5), MathUtils.round(lon, 5), speed,
					heading,
					"GTFS-rt",
					null, // leadingVehicleId,
					null, // driverId
					getLicensePlate(vehicle), 
					null, // passengerCount
					Float.NaN); // passengerFullness
			
			// Determine vehicle assignment information. Trip assignments
			// are more useful than route assignments so check trip
			// assignment first.
			if (vehicle.hasTrip()) {
				TripDescriptor tripDescriptor = vehicle.getTrip();
				if (tripDescriptor.hasTripId()) {
					avlReport.setAssignment(tripDescriptor.getTripId(), 
							AssignmentType.TRIP_ID);
				} else if (tripDescriptor.hasRouteId()) {
					avlReport.setAssignment(tripDescriptor.getRouteId(), 
							AssignmentType.ROUTE_ID);
				}
			}
			
			avlReportsReadIn.add(avlReport);
			
			logger.debug("Processed {}", avlReport);
		  }
		
		logger.info("Successfully processed {} AVL reports from " +
				"GTFS-realtime feed in {} msec",
				avlReportsReadIn.size(), timer.elapsedMsec());
		
		return avlReportsReadIn;
	}
	
	/**
	 * Actually processes the GTFS-realtime file and calls handleAvlReport()
	 * for each AvlReport.
	 */
	public static Collection<AvlReport> process(InputStream inputStream) {
		IntervalTimer timer = new IntervalTimer();
		
		// Create a CodedInputStream instead of just a regular InputStream
		// so that can change the size limit. Otherwise if file is greater
		// than 64MB get an exception.
		CodedInputStream codedStream = 
				CodedInputStream.newInstance(inputStream);
		// What to use instead of default 64MB limit
		final int GTFS_SIZE_LIMIT = 200000000;
		codedStream.setSizeLimit(GTFS_SIZE_LIMIT);	
		
		// Actual read in the data into a protobuffer FeedMessage object.
		// Would prefer to do this one VehiclePosition at a time using
		// something like VehiclePosition.parseFrom(codedStream) so that
		// wouldn't have to load entire protobuffer file into memory. But
		// it never seemed to complete, even for just a single call to
		// parseFrom(). Therefore loading in entire file at once.
		FeedMessage feedMessage;
		try {
			feedMessage = FeedMessage.parseFrom(codedStream);
			logger.info("Parsing GTFS-realtime file into a FeedMessage took " +
					"{} msec", timer.elapsedMsec());
			return processMessage(feedMessage);
		} catch (IOException e) {
			logger.error("Exception when reading GTFS-realtime data from " +
					"input stream.", e);
			return new ArrayList<AvlReport>();
		}
		
	}
	
	/**
	 * Actually processes the GTFS-realtime file and calls handleAvlReport()
	 * for each AvlReport.
	 */
	public static Collection<AvlReport> getAvlReports(String urlString) {
		try {
			logger.info("Getting GTFS-realtime AVL data from URL={} ...", 
					urlString);

			URI uri = new URI(urlString);
			URL url = uri.toURL();		
			URLConnection con = url.openConnection();
			
			InputStream inputStream = con.getInputStream();

			return process(inputStream);
		} catch (Exception e) {
			logger.error("Exception when reading GTFS-realtime data from " +
					"URL {}", 
					urlString, e);
			return new ArrayList<AvlReport>();
		}				
	}
	
}
