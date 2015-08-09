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

package org.transitime.custom.irishrail;

import java.io.InputStream;
import java.util.List;

import org.transitime.avl.GtfsRealtimeModule;
import org.transitime.config.StringConfigValue;
import org.transitime.db.structs.AvlReport;
import org.transitime.feed.gtfsRt.GtfsRtVehiclePositionsReader;


/**
 * This is a seperate module to read the nexala realtime data form the GTFS-RT vechicle location url. 
 * This is to allow for the less accurate realtime info be read from else where.
 *
 * @author Sean Og Crudden
 *
 */
public class NexalaAvlModule extends GtfsRealtimeModule {
	
	/*********** Configurable Parameters for this module ***********/
	public static String getGtfsRealtimeNexalaURI() {
		return gtfsRealtimeNexalaFeedURI.getValue();
	}
	
	private static StringConfigValue gtfsRealtimeNexalaFeedURI =
			new StringConfigValue("transitime.avl.gtfsRealtimeNexalaFeedURI", 
					"file:///C:/Users/SeanOg/gtfsRealtimeData",
					"The URI of the GTFS-realtime feed generated form the Nexala data.");

	/********************** Member Functions **************************/

	/**
	 * @param projectId
	 */
	public NexalaAvlModule(String projectId) {
		super(projectId);
	}

	/**
	 * Reads and processes the data. Called by AvlModule.run().
	 * Reading GTFS-realtime doesn't use InputSteram so overriding
	 * getAndProcessData().
	 */
	@Override
	protected void getAndProcessData() {
		List<AvlReport> avlReports = GtfsRtVehiclePositionsReader
				.getAvlReports(getGtfsRealtimeNexalaURI());
		for (AvlReport avlReport : avlReports) {
			avlReport.setSource("NexalaGTFS");
			processAvlReport(avlReport);
		}
	}

	/* (non-Javadoc)
	 * @see org.transitime.avl.AvlModule#getUrl()
	 */
	@Override
	protected String getUrl() {
		return getGtfsRealtimeNexalaURI();
	}

	/* Not needed since all processing for this class is done in
	 * the overridden getAndProcessData().
	 * (non-Javadoc)
	 * @see org.transitime.avl.AvlModule#processData(java.io.InputStream)
	 */
	@Override
	protected void processData(InputStream in) throws Exception {
	}
}
