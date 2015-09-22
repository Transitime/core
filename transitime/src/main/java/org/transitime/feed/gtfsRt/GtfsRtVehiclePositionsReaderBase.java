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

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

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
public abstract class GtfsRtVehiclePositionsReaderBase {

	private final String urlString;
	
	private static final Logger logger = LoggerFactory
			.getLogger(GtfsRtVehiclePositionsReaderBase.class);

	/********************** Member Functions **************************/

	public GtfsRtVehiclePositionsReaderBase(String urlString) {
		this.urlString = urlString;
	}
	
	/**
	 * Returns the vehicleID. Returns null if no VehicleDescription associated
	 * with the vehicle or if no ID associated with the VehicleDescription.
	 * 
	 * @param vehicle
	 * @return vehicle ID or null if there isn't one
	 */
	private static String getVehicleId(VehiclePosition vehicle) {
		if (!vehicle.hasVehicle()) {
			return null;
		}
		VehicleDescriptor desc = vehicle.getVehicle();
		if (!desc.hasId()) {
			return null;
		}
		return desc.getId();
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
	 * To be overridden by superclass. Is called each time an
	 * AvlReport is handled.
	 * 
	 * @param avlReport
	 */
	protected abstract void handleAvlReport(AvlReport avlReport);
	
	/**
	 * For each vehicle in the GTFS-realtime message put AvlReport into list.
	 * 
	 * @param message
	 *            Contains all of the VehiclePosition objects
	 * @return List of AvlReports
	 */
	private void processMessage(FeedMessage message) {
		logger.info("Processing each individual AvlReport...");
		IntervalTimer timer = new IntervalTimer();
		
		// For each entity/vehicle process the data
		int counter = 0;
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
				gpsTime = vehicle.getTimestamp()*1000;
			else
				gpsTime = System.currentTimeMillis();
			
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
		    }
		    
			// Create the core AVL object. The feed can provide a silly amount 
		    // of precision so round to just 5 decimal places.
            // AvlReport is expecting time in ms while the proto provides it in
		    // seconds
			AvlReport avlReport = new AvlReport(vehicleId, 
					gpsTime * Time.MS_PER_SEC,
					MathUtils.round(lat, 5), MathUtils.round(lon, 5), speed,
					heading,
					"GTFS-rt",
					null, // leadingVehicleId,
					null, // driverId
					getLicensePlate(vehicle), 
					null, // passengerCount
					Float.NaN); // passengerFullness
			
			// Determine vehicle assignment information
			if (vehicle.hasTrip()) {
				TripDescriptor tripDescriptor = vehicle.getTrip();
				if (tripDescriptor.hasTripId()) {
					avlReport.setAssignment(tripDescriptor.getTripId(), 
							AssignmentType.TRIP_ID);
				}
				if (tripDescriptor.hasRouteId()) {
					avlReport.setAssignment(tripDescriptor.getRouteId(), 
							AssignmentType.ROUTE_ID);
				}
			}
			
			logger.debug("Processed {}", avlReport);
			
			// The callback for each AvlReport
			handleAvlReport(avlReport);

			++counter;
		  }
		
		logger.info("Successfully processed {} AVL reports from " +
				"GTFS-realtime feed in {} msec",
				counter, timer.elapsedMsec());
	}
	
	/**
	 * Actually processes the GTFS-realtime file and calls handleAvlReport()
	 * for each AvlReport.
	 */
	public void process() {
		try {
			logger.info("Getting GTFS-realtime AVL data from URL={} ...", 
					urlString);
			IntervalTimer timer = new IntervalTimer();
			
			URI uri = new URI(urlString);
			URL url = uri.toURL();
			
			// Create a CodedInputStream instead of just a regular InputStream
			// so that can change the size limit. Otherwise if file is greater
			// than 64MB get an exception.
			InputStream inputStream = url.openStream();
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
			FeedMessage feed = FeedMessage.parseFrom(codedStream);
			logger.info("Parsing GTFS-realtime file into a FeedMessage took " +
					"{} msec", timer.elapsedMsec());
			
			// Process each individual VehiclePostions message
			processMessage(feed);
		} catch (Exception e) {
			logger.error("Exception when reading GTFS-realtime data from " +
					"URL {}", 
					urlString, e);
		}				
	}
	
	/**
	 * Returns the URL that this class is reading the GTFS-realtime data from.
	 * 
	 * @return The URL being used.
	 */
	public String getUrlString() {
		return urlString;
	}
}
