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

import java.io.InputStream;
import java.util.List;

import org.transitime.config.StringConfigValue;
import org.transitime.db.structs.AvlReport;
import org.transitime.feed.gtfsRt.GtfsRtVehiclePositionsReader;
import org.transitime.modules.Module;

/**
 * For reading in feed of GTFS-realtime AVL data. Is used for both realtime
 * feeds and for when reading in a giant batch of data.
 * 
 * @author SkiBu Smith
 * 
 */
public class GtfsRealtimeModule extends PollUrlAvlModule {

	// If debugging feed and want to not actually process
	// AVL reports to generate predictions and such then
	// set shouldProcessAvl to false;
	private static boolean shouldProcessAvl = true;

	/*********** Configurable Parameters for this module ***********/
	public static String getGtfsRealtimeURI() {
		return gtfsRealtimeURI.getValue();
	}
	private static StringConfigValue gtfsRealtimeURI =
			new StringConfigValue("transitime.avl.gtfsRealtimeFeedURI", 
					"file:///C:/Users/Mike/gtfsRealtimeData",
					"The URI of the GTFS-realtime feed to use.");

	/********************** Member Functions **************************/

	/**
	 * @param projectId
	 */
	public GtfsRealtimeModule(String projectId) {
		super(projectId);
	}

	/**
	 * Reads and processes the data. Called by AvlModule.run().
	 * Reading GTFS-realtime doesn't use InputStream so overriding
	 * getAndProcessData().
	 */
	@Override
	protected void getAndProcessData() {
		List<AvlReport> avlReports = GtfsRtVehiclePositionsReader
				.getAvlReports(getGtfsRealtimeURI());
		for (AvlReport avlReport : avlReports) {
			if (shouldProcessAvl)
				processAvlReport(avlReport);
			else
				System.out.println(avlReport);
		}
	}

	/* (non-Javadoc)
	 * @see org.transitime.avl.AvlModule#getUrl()
	 */
	@Override
	protected String getUrl() {
		return getGtfsRealtimeURI();
	}

	/* Not needed since all processing for this class is done in
	 * the overridden getAndProcessData().
	 * (non-Javadoc)
	 * @see org.transitime.avl.AvlModule#processData(java.io.InputStream)
	 */
	@Override
	protected void processData(InputStream in) throws Exception {
	}

	/**
	 * Just for debugging
	 */
	public static void main(String[] args) {
		// Create a ZonarAvlModule for testing
		shouldProcessAvl = false;
		Module.start("org.transitime.avl.GtfsRealtimeModule");
	}

}
