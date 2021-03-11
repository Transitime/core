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

package org.transitclock.custom.lametro;

import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.avl.NextBusAvlModule;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.utils.Geo;
import org.transitclock.utils.MathUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * For lametro agency the block assignments from the feed don't
 * match to the GTFS data. Therefore this module must be used
 * for the sfmta AVL feed.
 *
 * @author SkiBu Smith
 * 
 */
public class LametroNextBusAvlModule extends NextBusAvlModule {

	private static final Logger logger =
			LoggerFactory.getLogger(LametroNextBusAvlModule.class);

	private static StringConfigValue xmlFeedURI = new StringConfigValue("transitclock.avl.xmlFeedURI",
			"http://example.com",
			"nextbus avl feed URI");

	/**
	 * @param agencyId
	 */
	public LametroNextBusAvlModule(String agencyId) {
		super(agencyId);
	}

	@Override
	protected String getUrl() {
		return xmlFeedURI.getValue();
		//return "http://data.nextbus.com/service/customerfeed/lametro/avl?command=lastdata";
	}

	@Override
	protected Collection<AvlReport> extractAvlData(Document doc)
			throws NumberFormatException {
		try {
			logger.info("Extracting data from xml file...");
			// Get root of doc
			Element rootNode = doc.getRootElement();

			// Handle any error message
			Element error = rootNode.getChild("Error");
			if (error != null) {
				String errorStr = error.getTextNormalize();
				logger.error("While processing AVL data in NextBusAvlModule: " + errorStr);
				return new ArrayList<AvlReport>();
			}

			// The return value for the method
			Collection<AvlReport> avlReportsReadIn = new ArrayList<AvlReport>();
			// Handle getting vehicle location data
			Element vehiclesNode = rootNode.getChild("vehicles");
			List<Element> vehicles = vehiclesNode.getChildren();
			for (Element vehicle : vehicles) {
				String vehicleId = vehicle.getChild("id").getValue();
				String dateStr = vehicle.getChild("date").getValue();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = null;
				try {
					if (dateStr != null) {
						sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
						date = sdf.parse(dateStr);
						logger.info("created date {}", date);
					} else {
						logger.info("missing date for {}", vehicleId);
						continue;
					}
				} catch (ParseException e) {
					logger.error("invalid date {}, skipping", dateStr);
					continue;
				}
				if (date == null) {
					logger.error("missing(1) date for {}", vehicleId);
					continue;
				}
				float lat = Float.parseFloat(vehicle.getChild("lat").getValue());
				float lon = Float.parseFloat(vehicle.getChild("lon").getValue());
				String speedStr = null;
				if (vehicle.getChild("speed") != null)
					speedStr = vehicle.getChild("speed").getValue(); // appears to be in kmh
				float speed = 0.0f;
				if (speedStr != null)
					speed = Geo.converKmPerHrToMetersPerSecond(Integer.parseInt(speedStr));
				Integer bearing = Integer.parseInt(vehicle.getChild("direction").getValue());
				String blockId = null;
				if (vehicle.getChild("block") != null)
					blockId = vehicle.getChild("block").getValue();
				String operator = null;
				if (vehicle.getChild("driver") != null)
					operator = vehicle.getChild("driver").getValue();

				AvlReport avlReport = new AvlReport(vehicleId, date.getTime(),
						MathUtils.round(lat, 5), MathUtils.round(lon, 5),
						speed, bearing, "NextBus", null, operator,
						null,
						null,
						Float.NaN);
				avlReport.setAssignment(blockId, AvlReport.AssignmentType.BLOCK_ID);
				avlReportsReadIn.add(avlReport);
			}
			logger.info("processed {} records", avlReportsReadIn.size());
			return avlReportsReadIn;
		} catch (Throwable t) {
			logger.info("horrible mistake: ", t);
			return new ArrayList<AvlReport>();
		} finally {
			logger.info("exiting xml data extraction");
		}
	}

	/**
	 * At least for sfmta agency they don't use a leading 0 in the block ID in
	 * the GTFS data. Therefore to match strip out leading zeros from the block
	 * ID here.
	 * 
	 * @param originalBlockIdFromFeed
	 *            the block ID to be modified
	 * @return the modified block ID that corresponds to the GTFS data
	 */
	@Override
	protected String processBlockId(String originalBlockIdFromFeed) {
		String block = originalBlockIdFromFeed;
		while (block != null && block.startsWith("0"))
			block = block.substring(1);
		return block;
	}

}
