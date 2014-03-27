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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.AvlReport.AssignmentType;
import org.transitime.modules.Module;
import org.transitime.utils.Time;

/**
 * Reads AVL data from the NYC MTA BusTime Siri AVL feed and publishes it to
 * appropriate JMS topic so that it can be read by AVL clients.
 * 
 * @author SkiBu Smith
 *
 */
public class BusTimeSiriAvlModule extends XmlPollingAvlModule {
	
	// Time in XML feed has the format "2013-07-29T20:06:51.844-04:00"
	private static final DateFormat dateFormatter = 
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	private static final Logger logger= 
			LoggerFactory.getLogger(BusTimeSiriAvlModule.class);	
	
	/********************** Member Functions **************************/
	
	/**
	 * Constructor
	 * @param projectId
	 */
	public BusTimeSiriAvlModule(String projectId) {
		super(projectId);
	}
	
	/**
	 * Feed specific URL to use when accessing data.
	 */
	@Override
	protected String getUrl() {
		// Determine the URL to use
		String url = "http://bustime.mta.info/api/siri/vehicle-monitoring.xml?";
		String queryStr= "key=359e1a36-af6b-4c51-b759-9ad1e528f9d6";
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
		
		// Get the Namespace. Found that if don't use the namespace when calling 
		// getChild() then it returns null!
		Namespace ns = rootNode.getNamespace();

		Element serviceDelivery = rootNode.getChild("ServiceDelivery", ns);
		if (serviceDelivery == null) {
			logger.error("ServiceDelivery element not found in XML data");
			return;
		}
		Element vehicleMonitoringDelivery = serviceDelivery.getChild("VehicleMonitoringDelivery", ns);
		if (vehicleMonitoringDelivery == null) {
			logger.error("VehicleMonitoringDelivery element not found in XML data");
			return;
		}
		
		// Handle any error message
		Element errorCondition = vehicleMonitoringDelivery.getChild("ErrorCondition", ns);
		if (errorCondition != null) {
			Element description = errorCondition.getChild("Description", ns);
			String errorStr = description.getTextNormalize();
			logger.error("While processing AVL data in BusTimeSiriAvlModule: " + errorStr);
			return;
		}
		
		// Handle getting vehicle location data
		List<Element> vehicles = vehicleMonitoringDelivery.getChildren("VehicleActivity", ns);
		for (Element vehicle : vehicles) {			
			Element monitoredVehicleJourney = vehicle.getChild("MonitoredVehicleJourney", ns);
			if (monitoredVehicleJourney != null) {
				// Get vehicle id
				Element vehicleRef = monitoredVehicleJourney.getChild("VehicleRef", ns);
				String vehicleRefStr = vehicleRef.getTextNormalize();
				String vehicleId = vehicleRefStr.substring(vehicleRefStr.lastIndexOf('_') + 1);
				
				// Get the timestamp of the GPS report. 
				// Note: this probably isn't the actually GPS timestamp but instead
				// when the GPS report was "recorded", but it is the best we have.
				Element recordedAtTime = monitoredVehicleJourney.getChild("RecordedAtTime", ns);
				String timestampStr = recordedAtTime.getTextNormalize();
				// The timestamp in the XML has the TimeZone specified as "-04:00" instead of
				// what SimpleDateFormat can handle, which is "0400". Therefore need to remove
				// that one semicolon.
				String fixedStr = timestampStr.substring(0, timestampStr.lastIndexOf(':')) + "00";
				long gpsEpochTime = 0;
				try {
					gpsEpochTime = dateFormatter.parse(fixedStr).getTime();
				} catch (ParseException e) {
					logger.error("Could not parse <RecordedAtTime> of \"" + timestampStr + "\"");
				}
				
				// Get lat & lon
				Element vehicleLocation = monitoredVehicleJourney.getChild("VehicleLocation", ns);
				Element latitude = vehicleLocation.getChild("Latitude", ns);
				float lat = Float.parseFloat(latitude.getTextNormalize());
				Element longitude = vehicleLocation.getChild("Longitude", ns);
				float lon = Float.parseFloat(longitude.getTextNormalize());
				
				// Block assignment. This one is hard to figure out. While there is a <BlockRef>
				// element it appears that it is not the right one. Instead need to use
				// <FramedVehicleJourneyRef><DatedVehicleJourneyRef> and then just use
				// the last two chunks of something like "MTA NYCT_GH_C3-Weekday-SDon-118800_BX44A_115".
				// And at NextBus we definitely saw lots of problems where the block assignment
				// info wasn't accurate.
				Element framedVehicleJourneyRef = 
						monitoredVehicleJourney.getChild("FramedVehicleJourneyRef", ns);
				Element datedVehicleJourneyRef =
						framedVehicleJourneyRef.getChild("DatedVehicleJourneyRef", ns);
				String fullBlockName = datedVehicleJourneyRef.getTextNormalize();
				String blockName2 = fullBlockName.substring(fullBlockName.lastIndexOf('-')+1);
				String block = blockName2.substring(blockName2.indexOf('_')+1);
				
				// Heading
				float heading = Float.NaN;
				Element bearing = monitoredVehicleJourney.getChild("Bearing", ns);
				if (bearing != null) {
					heading = Float.parseFloat(bearing.getTextNormalize());
				}

				// Speed is not available
				float speed = Float.NaN;
				
				logger.debug("vehicle={} time={} lat={} lon={} spd={} head={} blk={}", 
						vehicleId, Time.timeStr(gpsEpochTime), lat, lon, speed, heading, block);
				
				// Create the AVL object and send it to the JMS topic
				AvlReport avlReport = new AvlReport(vehicleId, gpsEpochTime, lat, lon, speed, heading);
				avlReport.setAssignment(block, AssignmentType.BLOCK_ID);
				
				writeAvlReportToJms(avlReport);
			}
		}
		
	}
	
	/**
	 * Just for debugging
	 */
	public static void main(String[] args) {
		// Create a BustimeSiriAvlModule for testing
		Module.start("org.transitime.avl.BusTimeSiriAvlModule");
	}

}
