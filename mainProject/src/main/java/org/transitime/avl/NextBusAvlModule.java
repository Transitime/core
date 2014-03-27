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

import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.StringConfigValue;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.AvlReport.AssignmentType;
import org.transitime.modules.Module;
import org.transitime.utils.Geo;
import org.transitime.utils.MathUtils;
import org.transitime.utils.Time;

/**
 * Reads AVL data from a NextBus AVL feed and publishes it to
 * appropriate JMS topic so that it can be read by AVL clients.
 * @author SkiBu Smith
 *
 */
public class NextBusAvlModule extends XmlPollingAvlModule {
	
	// Parameter that specifies URL of the NextBus feed.
	// Note that uses the old NextBus feed since that provides 
	// non-predictable vehicles, block assignment, driver ID, and other info.
	// If the old NextBus feed goes away then can use the private one
	// /service/xmlFeed .

	private static StringConfigValue nextBusFeedUrl = 
			new StringConfigValue("transitime.avl.nextbusFeedUrl", 
					"http://webservices.nextbus.com/s/xmlFeed");
	private static String getNextBusFeedUrl() {
		return nextBusFeedUrl.getValue();
	}
	
	// So can just get data since last query. Initialize so when first called
	// get data for last 10 minutes. Definitely don't want to use t=0 because
	// then can end up with some really old reports.
	private long previousTime = System.currentTimeMillis() - 10*Time.MS_PER_MIN;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(NextBusAvlModule.class);	

	/********************** Member Functions **************************/
	
	/**
	 * Constructor
	 * @param projectId
	 */
	public NextBusAvlModule(String projectId) {
		super(projectId);
	}
	
	/**
	 * Feed specific URL to use when accessing data.
	 */
	@Override
	protected String getUrl() {
		// Determine the URL to use.
		String url = getNextBusFeedUrl();
		String queryStr= "?command=vehicleLocations" + 
				"&a=" + getProjectId() + 
				"&details=true" +        // So get the more detailed info
				"&t=" + previousTime;
		return url + queryStr;
	}

	/**
	 * Extracts the AVL data from the XML document.
	 * Uses JDOM to parse the XML because it makes the Java code much simpler.
	 * @param doc
	 * @throws NumberFormatException
	 */
	@Override
	protected void extractAvlData(Document doc) 
			throws NumberFormatException {
		logger.info("Extracting data from xml file");
		
		// Get root of doc
		Element rootNode = doc.getRootElement();

		// Handle any error message
		Element error = rootNode.getChild("Error");
		if (error != null) {
			String errorStr = error.getTextNormalize();
			logger.error("While processing AVL data in NextBusAvlModule: " + errorStr);
			return;
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

		// Handle getting vehicle location data
		List<Element> vehicles = rootNode.getChildren("vehicle");
		for (Element vehicle : vehicles) {
			String vehicleId = vehicle.getAttributeValue("id");
			float lat = Float.parseFloat(vehicle.getAttributeValue("lat"));
			float lon = Float.parseFloat(vehicle.getAttributeValue("lon"));
			
			// Determine GPS time. Use the previousTime read from the feed
			// because it indicates what the secsSinceReport is relative to
			int secsSinceReport = 
					Integer.parseInt(vehicle.getAttributeValue("secsSinceReport"));
			long gpsEpochTime = previousTime - secsSinceReport * 1000;
			
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
			String block = processBlockId(vehicle.getAttributeValue("block"));
			
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
					"blk={} drvr={}", 
					vehicleId, Time.timeStr(gpsEpochTime), lat, lon, speed, 
					heading, block, driverId);
				
			// Create the AVL object and send it to the JMS topic.
			// The NextBus feed provides silly amount of precision so 
			// round to just 5 decimal places.
			AvlReport avlReport = new AvlReport(vehicleId, gpsEpochTime, 
					MathUtils.round(lat, 5), MathUtils.round(lon, 5), 
					speed, heading, driverId, 
					null,       // license plate
					passengerCount,
					Float.NaN); // passengerFullness
			if (block != null)
				avlReport.setAssignment(block, AssignmentType.BLOCK_ID);
			else
				avlReport.setAssignment(null, AssignmentType.UNSET);
			avlReport.setDriverId(driverId);
			
			writeAvlReportToJms(avlReport);
		}		
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
