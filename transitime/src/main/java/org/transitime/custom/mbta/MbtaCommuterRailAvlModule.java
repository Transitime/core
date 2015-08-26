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

package org.transitime.custom.mbta;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.avl.AvlModule;
import org.transitime.avl.PollUrlAvlModule;
import org.transitime.avl.TaipGpsLocation;
import org.transitime.config.StringConfigValue;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.AvlReport.AssignmentType;
import org.transitime.modules.Module;

/**
 * Gets GPS data from the MBTA Commuter Rail AVL feed. The data comes from a 
 * URL and is in TAIP format.
 *
 * @author SkiBu Smith
 *
 */
public class MbtaCommuterRailAvlModule extends PollUrlAvlModule {

	// If debugging feed and want to not actually process
	// AVL reports to generate predictions and such then
	// set shouldProcessAvl to false;
	private static boolean shouldProcessAvl = true;
	
	// For logging use AvlModule class so that will end up in the AVL log file
	private static final Logger logger = LoggerFactory
			.getLogger(AvlModule.class);

	/********************** Member Functions **************************/
	
	private static StringConfigValue mbtaCommuterRailFeedUrl = 
			new StringConfigValue("transitime.avl.mbtaCommuterRailFeedUrl", 
					"The URL of the MBTA commuter rail feed to use.");
	private static String getMbtaCommuterRailFeedUrl() {
		return mbtaCommuterRailFeedUrl.getValue();
	}

	/**
	 * Simple constructor
	 * 
	 * @param agencyId
	 */
	public MbtaCommuterRailAvlModule(String agencyId) {
		super(agencyId);
	}

	/* (non-Javadoc)
	 * @see org.transitime.avl.AvlModule#getUrl()
	 */
	@Override
	protected String getUrl() {
		return getMbtaCommuterRailFeedUrl();
	}

	/* (non-Javadoc)
	 * @see org.transitime.avl.AvlModule#processData(java.io.InputStream)
	 */
	@Override
	protected void processData(InputStream in) throws Exception {
		// Map, keyed on vehicle ID, is for keeping track of the last AVL
		// report for each vehicle. Since GPS reports are in chronological
		// order in the feed the element in the map represents the last
		// AVL report.
		Map<String, AvlReport> avlReports = new HashMap<String, AvlReport>();
		
		// Read in all AVL reports and add them to the avlReports map
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = reader.readLine()) != null) {
			// Log each non-blank line. Use debug logging since pretty verbose
			if (!line.trim().isEmpty())
				logger.debug(line);
			
			AvlReport avlReport = parseAvlReport(line);
			if (avlReport != null) {
				avlReports.put(avlReport.getVehicleId(), avlReport);
			}
		}
		
		// Process the last AVL report for each vehicle
		if (shouldProcessAvl) {
			for (AvlReport avlReport : avlReports.values()) {
				processAvlReport(avlReport);
			}
		}
	}
	
	/**
	 * NOTE: used to be that the GTFS config always provided zero padded 3-digit
	 * pattern/trip short names but the AVL feed would not include the preceding
	 * zeros. Therefore had to adjust the assignment so that the non-padded
	 * value from the feed would match the padded value from the config. But now
	 * MBTA is supplying a supplemental trips.txt file that uses non-padded trip
	 * short names. This means that don't need to adjust the assignments from
	 * the feed anymore. Therefore this method simply returns the original
	 * assignmentFromFeed.
	 * <p>
	 * Deprecated: Turns out that trip IDs in the AVL feed sometimes don't match
	 * the trip short names in the trips.txt GTFS file. If an ID from the feed
	 * is only 1 or 2 characters long then it needs to be padded with zeros. But
	 * there is also another case where the IDs from the feed are 4 digits long
	 * with two trailing zeroes. For these need to take of the trailing zeroes.
	 * Specific examples of these are 61, 62, 63, 64, 67, 68, 69, 72, 94, 95,
	 * 97, 98.
	 * 
	 * @param assignmentFromFeed
	 * @return The assignment, adjusted to be proper number of characters so
	 *         that matches trip short names in the trips.txt GTFS file
	 */
	private String adjustAssignment(String assignmentFromFeed) {
		return assignmentFromFeed;
//		
//		// Handle special null case
//		if (assignmentFromFeed == null)
//			return assignmentFromFeed;
//		
//		// Assignment is too short so pad it
//		if (assignmentFromFeed.length() == 1)
//			return "00" + assignmentFromFeed;
//		else if (assignmentFromFeed.length() == 2)
//			return "0" + assignmentFromFeed;
//		else if (assignmentFromFeed.length() == 4 
//				&& assignmentFromFeed.endsWith("00"))
//			return assignmentFromFeed.substring(0, 2);
//		
//		// Assignment is OK as is so return it
//		return assignmentFromFeed;
	}
	
	// The following are determining the proper place in the
	// AVL string to process for the particular piece of data
	private static String vehicleIdMarker = "Vehicle ID:";
	private static String locationMarker = "Location[";
	private static String workpieceMarker = "Workpiece:";
	private static String patternMarker = "Pattern:";
	private static String gpsMarker = "GPS:";
	
	/**
	 * Reads line from AVL feed and creates and returns corresponding AVL
	 * report.
	 * 
	 * @param line
	 *            Line from AVL feed that represents a single AVL report.
	 * @return AvlReport for the line, or null if there is a problem parsing the
	 *         data
	 */
	private AvlReport parseAvlReport(String line) {
		// If line is not valid then return null
		if (line.isEmpty())
			return null;
		if (!line.contains(vehicleIdMarker) || !line.contains(locationMarker))
			return null;
		
		// Get vehicle ID
		String vehicleId = getValue(line, vehicleIdMarker + "(\\d+)");
		
		// Get the vehicle assignment. The "workpiece" is the block assignment
		// and the "pattern" is the trip ID. Given that block assignments are
		// a bit iffy for MBTA Commuter Rail it is best to use trip assignments.
		String workpiece = 
				adjustAssignment(getValue(line, workpieceMarker + "(\\d+)"));
		String pattern = 
				adjustAssignment(getValue(line, patternMarker + "(\\d+)"));
		
		// Get GPS data from TAIP formatted string
		String gpsTaipStr = getValue(line, gpsMarker + "(\\>.+\\<)");
		TaipGpsLocation taipGpsLoc = TaipGpsLocation.get(gpsTaipStr);
		if (taipGpsLoc == null) {
			logger.debug("Could not parse TAIP string \"{}\" for line {}", 
					gpsTaipStr, line);
			return null;
		}
		long gpsTime = taipGpsLoc.getFixEpochTime();
		double lat = taipGpsLoc.getLatitude();
		double lon = taipGpsLoc.getLongitude();
		float heading = taipGpsLoc.getHeading();
		float speed = taipGpsLoc.getSpeedMetersPerSecond();
		
		// Create the AVL report
		AvlReport avlReport = new AvlReport(vehicleId, gpsTime, lat, lon,
				speed, heading, "MBTA");
		
		// Determine the assignment to use. usingTripsAssignments should be
		// true if using trips assignments instead regular block assignments.
		// If using block assignments but the block is "000" then should
		// instead use the trip ID since it will be valid. This is due to the
		// feed being peculiar and sometimes setting the trip/pattern right
		// away but not setting the block/workpiece for a while.
		// TODO need to determine whether going to use trip IDs or block IDs.
		// Seems that could always use trip assignments since they are always
		// provided (with blocks/workpieces sometimes get a "000" assignment).
		// And with trip assignment the block assigner simply converts the 
		// trip to the block anyways. So only need the block/workpiece
		// assignment to determine the trip to block mapping in order to
		// generate the supplemental GTFS trips.txt file. Don't need 
		// blocks/workpieces when actually running the core predictor software.
		boolean usingTripAssignment = true;
		
		if (usingTripAssignment || workpiece.equals("000")) {
			avlReport.setAssignment(pattern, AssignmentType.TRIP_SHORT_NAME);
			avlReport.setField1("workpiece", workpiece);
		} else {
			// Using block assignment  that is not "000"
			avlReport.setAssignment(workpiece, AssignmentType.BLOCK_ID);
			avlReport.setField1("pattern", pattern);
		}
				
		return avlReport;
	}
	
	/**
	 * Gets a value from the string using specified regular expression.
	 * The regular expression should indicate what is before the value
	 * plus a capture element to indicate the actual value. So for
	 * a string containing something like Value:123 the regEx should
	 * be "Value:{\\d+}".
	 * 
	 * @param str
	 * @param regEx
	 * @return
	 */
	private static String getValue(String str, String regEx) {
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(str);
		
		boolean found = matcher.find();
		if (!found)
			return null;
		
		String result = str.substring(matcher.start(1), matcher.end(1));
		return result;
	}
	
	/**
	 * Just for debugging
	 */
	public static void main(String[] args) {
		// For debugging turn off the actual processing of the AVL data.
		// This way the AVL data is logged, but that is all.
		shouldProcessAvl = false;
		
		// Create a NextBusAvlModue for testing
		Module.start("org.transitime.custom.mbta.MbtaCommuterRailAvlModule");
	}

}

