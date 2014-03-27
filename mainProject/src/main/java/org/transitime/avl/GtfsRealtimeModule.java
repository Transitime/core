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
package org.transitime.avl;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.transitime.config.StringConfigValue;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.AvlReport.AssignmentType;
import org.transitime.utils.MathUtils;

import com.google.protobuf.CodedInputStream;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

/**
 * For reading in GTFS-realtime AVL data. Is used for both realtime feeds and
 * for when reading in a giant batch of data.
 * 
 * @author SkiBu Smith
 * 
 */
public class GtfsRealtimeModule extends AvlModule {

	private static final Logger logger = 
			LoggerFactory.getLogger(GtfsRealtimeModule.class);

	/*********** Configurable Parameters for this module ***********/
	public static String getGtfsRealtimeURI() {
		return gtfsRealtimeURI.getValue();
	}
	private static StringConfigValue gtfsRealtimeURI =
			new StringConfigValue("transitime.avl.gtfsRealtimeFeedURI", 
					"file:///C:/Users/Mike/gtfsRealtimeData");

	/********************** Member Functions **************************/

	/**
	 * @param projectId
	 */
	public GtfsRealtimeModule(String projectId) {
		super(projectId);
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
	 * For each vehicle in the GTFS-realtime message put AvlReport
	 * into list.
	 * 
	 * @param message
	 * @return List of AvlReports
	 */
	private static List<AvlReport> processMessage(FeedMessage message) {
		List<AvlReport> avlReports =  new ArrayList<AvlReport>();
		
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
				gpsTime = vehicle.getTimestamp();
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
			AvlReport avlReport = new AvlReport(vehicleId, gpsTime,
					MathUtils.round(lat, 5), MathUtils.round(lon, 5), speed,
					heading);
			
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
			
			// Set the license plate if data is available
			avlReport.setLicensePlate(getLicensePlate(vehicle));
			
			logger.debug("Processed {}", avlReport);
			
			avlReports.add(avlReport);
		  }
		
		logger.info("Successfully processed {} AVL reports from GTFS-realtime feed",
				avlReports.size());
		
		// Return the array of AvlReports
		return avlReports;
	}

	/**
	 * Reads GTFS-realtime data from the URI specified by 
	 * CoreConfig.getGtfsRealtimeURI(), processes the data, and
	 * returns List of AvlReports. Declared static so can be used by 
	 * other non-AvlModule modules.
	 * 
	 * @return List of AvlReports
	 */
	protected static List<AvlReport> getAvlReports() {
		String urlString = getGtfsRealtimeURI();
		
		try {
			logger.info("Getting GTFS-realtime AVL data from URL={}", 
					urlString);
			
			URI uri = new URI(urlString);
			URL url = uri.toURL();
			
			// Create a CodedInputStream instead of just a regular InputStream
			// so that can change the size limit. Otherwise if file is greater
			// than 64MB get an exception.
			CodedInputStream codedStream = 
					CodedInputStream.newInstance(url.openStream());
			// What to use instead of default 64MB limit
			final int GTFS_SIZE_LIMIT = 200000000;
			codedStream.setSizeLimit(GTFS_SIZE_LIMIT);
			
			// Actual read in the data into a list of AVLReport objects
			FeedMessage feed = FeedMessage.parseFrom(codedStream);
			List<AvlReport> avlReports = processMessage(feed);
			return avlReports;
		} catch (Exception e) {
			logger.error("Exception when reading GTFS-realtime data from " + 
					urlString + " . " + e.getMessage());
			return null;
		}		
	}
	
	/**
	 * Reads and processes the data. Called by AvlModule.run().
	 */
	protected void getAndProcessData() {
		List<AvlReport> avlReports = getAvlReports();
		for (AvlReport avlReport : avlReports) {
			writeAvlReportToJms(avlReport);
		}
	}

}
