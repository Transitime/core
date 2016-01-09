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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.BooleanConfigValue;
import org.transitime.config.IntegerConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.AvlReport.AssignmentType;
import org.transitime.modules.Module;
import org.transitime.utils.Geo;
import org.transitime.utils.MathUtils;
import org.transitime.utils.Time;

/**
 * Reads AVL data from a NextBus AVL feed and processes each AVL report.
 * 
 * @author SkiBu Smith
 *
 */
public class NextBusAvlModule extends XmlPollingAvlModule {
	
	// Parameter that specifies URL of the NextBus feed.
	private static StringConfigValue nextBusFeedUrl = 
			new StringConfigValue("transitime.avl.nextbus.url", 
					"http://webservices.nextbus.com/service/publicXMLFeed",
					"The URL of the NextBus feed to use.");
	private static String getNextBusFeedUrl() {
		return nextBusFeedUrl.getValue();
	}
	
	// Config param that specifies the agency name to use as part
	// of the NextBus feed URL.
	private static StringConfigValue agencyNameForFeed =
			new StringConfigValue("transitime.avl.nextbus.agencyNameForFeed",
					"If set then specifies the agency name to use for the "
					+ "feed. If not set then the transitime.core.agencyId "
					+ "is used.");
	protected String getAgencyNameForFeed() {
		return agencyNameForFeed.getValue();
	}
	
	private static BooleanConfigValue useTripShortNameForAssignment =
			new BooleanConfigValue(
					"transitime.avl.nextbus.useTripShortNameForAssignment",
					false,
					"For some agencies the block info in the feed doesn't "
					+ "match the GTFS data. For these can sometimes use the "
					+ "trip short name instead.");
	
	// Query for determining value to use is:
	// select max(time - timeprocessed)	from avlreports	where time between '10-16-2015' and '10-17-2015';
	// This will show the GPS time that is farthest into the future. If 
	// greater than 0 then apiClockSkewMsecs needs to be increased
	// accordingly. If negative than this value might need to be
	// decreased.
	private static IntegerConfigValue apiClockSkewMsecs =
			new IntegerConfigValue(
					"transitime.avl.nextbus.apiClockSkewMsecs",
					0,
					"Determining GPS time from API is kludgey. Only have "
					+ "secsSinceReport attribute in API. Sometimes, probably "
					+ "when there is a clock skew in the NextBus web server or "
					+ "predictor system, secsSinceReport is negative, which is "
					+ "definitely wrong. Therefore need to sometimes look at "
					+ "AVL reports in db and see which one has the highest "
					+ "time-timeprocessed and adjust apiClockSkewMsecs "
					+ "accordingly.");
	
	// So can just get data since last query. Initialize so when first called
	// get data for last 10 minutes. Definitely don't want to use t=0 because
	// then can end up with some really old reports.
	private long previousTime = System.currentTimeMillis() - 10*Time.MS_PER_MIN;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(NextBusAvlModule.class);	

	/********************** Member Functions **************************/
	
	/**
	 * Constructor
	 * @param agencyId
	 */
	public NextBusAvlModule(String agencyId) {
		super(agencyId);
	}
	
	/**
	 * Feed specific URL to use when accessing data.
	 */
	@Override
	protected String getUrl() {
		// Determine the URL to use.
		String url = getNextBusFeedUrl();
		String agencyStr = getAgencyNameForFeed()!=null ? 
				getAgencyNameForFeed() : getAgencyId();
		String queryStr= "?command=vehicleLocations" + 
				"&a=" + agencyStr + 
				"&details=true" +        // So get the more detailed info
				"&t=" + previousTime;
		return url + queryStr;
	}

	/**
	 * Extracts the AVL data from the XML document.
	 * Uses JDOM to parse the XML because it makes the Java code much simpler.
	 * @param doc
	 * @return Collection of AvlReports
	 * @throws NumberFormatException
	 */
	@Override
	protected Collection<AvlReport> extractAvlData(Document doc) 
			throws NumberFormatException {
		logger.info("Extracting data from xml file");
		
		// Get root of doc
		Element rootNode = doc.getRootElement();

		// Handle any error message
		Element error = rootNode.getChild("Error");
		if (error != null) {
			String errorStr = error.getTextNormalize();
			logger.error("While processing AVL data in NextBusAvlModule: " + errorStr);
			return new ArrayList<AvlReport>();
		}

		// Handle getting last time. This is the system time of the server.
		// This means it can be used to along with secsSinceReport to determine
		// the epoch time when the GPS report was generated.
		Element lastTime = rootNode.getChild("lastTime");
		if (lastTime != null) {
			String lastTimeStr = lastTime.getAttributeValue("time");
			// Store previous time so that it can be used in the URL
			// the next time the feed is polled.
			previousTime = Long.parseLong(lastTimeStr);
			logger.debug("PreviousTime={}", Time.dateTimeStr(previousTime));
		}

		// The return value for the method
		Collection<AvlReport> avlReportsReadIn = new ArrayList<AvlReport>();
		
		// Handle getting vehicle location data
		List<Element> vehicles = rootNode.getChildren("vehicle");
		for (Element vehicle : vehicles) {
			String vehicleId = vehicle.getAttributeValue("id");
			float lat = Float.parseFloat(vehicle.getAttributeValue("lat"));
			float lon = Float.parseFloat(vehicle.getAttributeValue("lon"));
			
			// Determine GPS time. Use the previousTime read from the feed
			// because it indicates what the secsSinceReport is relative to. This
			// is a rather kludgey value and appears to be somewhat incorrect if
			// the NextBus server clock is off. Therefore the time is adjusted 
			// by apiClockSkewMsecs parameter to make sure that GPS times are
			// not in the future.
			int secsSinceReport = 
					Integer.parseInt(vehicle.getAttributeValue("secsSinceReport"));
			long gpsEpochTime =
					previousTime - secsSinceReport * 1000
							- apiClockSkewMsecs.getValue();
			
			// Handle the speed
			float speed = Float.NaN;
			String speedStr = vehicle.getAttributeValue("speedKmHr");
			if (speedStr != null)
				speed = Geo.converKmPerHrToMetersPerSecond(Float.parseFloat(speedStr)); 

			// Handle heading
			float heading = Float.NaN;
			String headingStr = vehicle.getAttributeValue("heading");
			if (headingStr != null) {
				heading = Float.parseFloat(headingStr);
				// Heading less than 0 means it is invalid
				if (heading < 0)
					heading = Float.NaN;
			}
			
			// Get block ID. Since for some feeds the block ID from the feed
			// doesn't match the GTFS data need to process the block ID.
			String blockId = processBlockId(vehicle.getAttributeValue("block"));
			
			String tripId = vehicle.getAttributeValue("tripTag");
			
			// Determine if part of consist
			String leadingVehicleId = vehicle.getAttributeValue("leadingVehicleId");
			
			// Get driver ID. Be consistent about using null if not set 
			// instead of empty string
			String driverId = vehicle.getAttributeValue("driverId");
			if (driverId != null && driverId.length() == 0)
				driverId = null;
			
			// Get passenger count
			Integer passengerCount = null;
			String passengerCountStr = vehicle.getAttributeValue("passengerCount");
			if (passengerCountStr != null) {
				passengerCount = Integer.parseInt(passengerCountStr);
				if (passengerCount < 0)
					passengerCount = 0;
			}
			
			// Log raw info for debugging
			logger.debug("vehicleId={} time={} lat={} lon={} spd={} head={} " +
					"blk={} leadVeh={} drvr={} psngCnt={}", 
					vehicleId, Time.timeStrMsec(gpsEpochTime), lat, lon, speed, 
					heading, blockId, leadingVehicleId, driverId, passengerCount);
				
			// Create the AVL object and send it to the JMS topic.
			// The NextBus feed provides silly amount of precision so 
			// round to just 5 decimal places.
			AvlReport avlReport = new AvlReport(vehicleId, gpsEpochTime, 
					MathUtils.round(lat, 5), MathUtils.round(lon, 5), 
					speed, heading, "NextBus", leadingVehicleId, driverId, 
					null,       // license plate
					passengerCount,
					Float.NaN); // passengerFullness
			
			// Record the assignment for the vehicle if it is available
			if (blockId != null && !useTripShortNameForAssignment.getValue())
				avlReport.setAssignment(blockId, AssignmentType.BLOCK_ID);
			else if (tripId != null && useTripShortNameForAssignment.getValue())
				avlReport.setAssignment(tripId, AssignmentType.TRIP_SHORT_NAME);
			else
				avlReport.setAssignment(null, AssignmentType.UNSET);
			
			avlReportsReadIn.add(avlReport);
		}
		
		return avlReportsReadIn;
	}
	
	/**
	 * Sometimes the raw block ID from the feed does not match the GTFS data.
	 * For such cases need to override this method.
	 * 
	 * @param originalBlockIdFromFeed
	 * @return The processed block ID
	 */
	protected String processBlockId(String originalBlockIdFromFeed) {
		// This default method simply returns the original block ID.
		// Subclasses can process the block ID as needed.
		return originalBlockIdFromFeed;
	}
	
	/**
	 * Just for debugging
	 */
	public static void main(String[] args) {
		// Create a NextBusAvlModue for testing
		Module.start("org.transitime.avl.NextBusAvlModule");
	}

}
