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
import org.transitime.config.StringConfigValue;
import org.transitime.db.structs.AvlReport;
import org.transitime.modules.Module;
import org.transitime.utils.MathUtils;
import org.transitime.utils.Time;

/**
 * Reads AVL data from Zonar XML AVL feed. Only GPS data is available. There is
 * no assignment info available as far as I can tell.
 * 
 * @author Michael Smith
 *
 */
public class ZonarAvlModule extends XmlPollingAvlModule {

	private static StringConfigValue zonarAccount =
			new StringConfigValue("transitime.avl.zonarAccount",
					"The Zonar count name used as part of the domain name for "
					+ "requests. Consists of 3 lower-case letters and 4 "
					+ "digits, e.g. abc1234.");
 
	private static StringConfigValue zonarUserName =
			new StringConfigValue("transitime.avl.zonarUserName",
					"The Zonar user name for API calls.");
 
	private static StringConfigValue zonarPassword =
			new StringConfigValue("transitime.avl.zonarPassword",
					"The Zonar password for API calls.");
 
	private static final double KM_PER_HOUR_TO_METERS_PER_SEC = 0.27777777777778;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(ZonarAvlModule.class);	

	/********************** Member Functions **************************/

	/**
	 * Constructor
	 * 
	 * @param projectId
	 */
	public ZonarAvlModule(String projectId) {
		super(projectId);
	}

	@Override
	protected Collection<AvlReport> extractAvlData(Document doc) throws NumberFormatException {
		logger.info("Extracting data from xml file");

		// Get root of doc
		Element rootNode = doc.getRootElement();

		// The return value for the method
		Collection<AvlReport> avlReportsReadIn = new ArrayList<AvlReport>();

		List<Element> assets = rootNode.getChildren("asset");
		for (Element asset : assets) {
			String vehicleId = asset.getAttributeValue("id");
			
			// Only need 5 digits of precision for lat & lon. Anything more 
			// than that is silly.
			String latStr = asset.getChild("lat").getValue();
			double lat = MathUtils.round(Double.parseDouble(latStr), 5);
			String lonStr = asset.getChild("long").getValue();
			double lon = MathUtils.round(Double.parseDouble(lonStr), 5);
			
			String headingStr = asset.getChild("heading").getValue();
			float heading = Float.parseFloat(headingStr);			
			
			Element speedElement = asset.getChild("speed");
			String speedStr = speedElement.getValue();
			float speed = Float.parseFloat(speedStr);
			String unitsStr = speedElement.getAttributeValue("unit");
			if (unitsStr.equals("Km/Hour")) {
				speed *= KM_PER_HOUR_TO_METERS_PER_SEC;
			} else {
				logger.error("Cannot handle units of \"{}\" for Zonar feed.", 
						unitsStr);
			}
			
			String timeStr = asset.getChild("time").getValue();
			long time = Long.parseLong(timeStr)	* Time.MS_PER_SEC;
			
			// Power and odometer are currently not used but are processed 
			// here just in case they are helpful in the future
			@SuppressWarnings("unused")
			String power = asset.getChild("power").getValue(); // on or off
			@SuppressWarnings("unused")
			String odometer = asset.getChild("odometer").getValue();
			
			// Create and process the AVL report. 
			AvlReport avlReport = new AvlReport(vehicleId, time,
					MathUtils.round(lat, 5), MathUtils.round(lon, 5), speed,
					heading, "Zonar");

			avlReportsReadIn.add(avlReport);
		}
		
		return avlReportsReadIn;
	}
	

	/**
	 * Feed specific URL to use when accessing data.
	 */
	@Override
	protected String getUrl() {
		return "https://" + zonarAccount 
				+ ".zonarsystems.net/interface.php?username=" + zonarUserName 
				+ "&password=" + zonarPassword 
				+ "&action=showposition&operation=current&logvers=3.2&format=xml";
	}

	/**
	 * Just for debugging
	 */
	public static void main(String[] args) {
		// Create a ZonarAvlModule for testing
		Module.start("org.transitime.avl.ZonarAvlModule");
	}

}
