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

import java.util.ArrayList;
import java.util.List;

import org.transitime.db.structs.AvlReport;

/**
 * For reading GTFS-realtime Vehicle Positions feed and returning data
 * as a List of AvlReports.
 *
 * @author SkiBu Smith
 *
 */
public class GtfsRtVehiclePositionsReader extends
		GtfsRtVehiclePositionsReaderBase {

	private List<AvlReport> avlReports = new ArrayList<AvlReport>();
	
	/********************** Member Functions **************************/

	/**
	 * Simple constructor. Protected because should access this class through
	 * getAvlReports().
	 * 
	 * @param urlString
	 */
	protected GtfsRtVehiclePositionsReader(String urlString) {
		super(urlString);
	}

	/**
	 * Called for each AvlReport processed. Adds the report to the list of
	 * AvlReports.
	 */
	@Override
	protected void handleAvlReport(AvlReport avlReport) {
		avlReports.add(avlReport);
	}

	/**
	 * Returns list of AvlReports read from GTFS-realtime file
	 * specified by urlString.
	 * 
	 * @param urlString URL of GTFS-rt file
	 * @return List of AvlReports
	 */
	public static List<AvlReport> getAvlReports(String urlString) {
		GtfsRtVehiclePositionsReader reader = 
				new GtfsRtVehiclePositionsReader(urlString);
		
		reader.process();
		
		return reader.avlReports;
	}
}
