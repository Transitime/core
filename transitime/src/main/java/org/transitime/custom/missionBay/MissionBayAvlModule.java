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

package org.transitime.custom.missionBay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.avl.NextBusAvlModule;
import org.transitime.db.structs.AvlReport;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class MissionBayAvlModule extends NextBusAvlModule {

	private static final Logger logger = LoggerFactory
			.getLogger(MissionBayAvlModule.class);

	/********************** Member Functions **************************/

	/**
	 * @param agencyId
	 */
	public MissionBayAvlModule(String agencyId) {
		super(agencyId);
	}

	/**
	 * Does normal handling of AVL report but also sends data to the
	 * SFMTA API.
	 */
	protected void processAvlReport(AvlReport avlReport) {
		// Do the normal handling of the AVL report
		super.processAvlReport(avlReport);
		
		// Send the data to the SFMTA API
		logger.info("Batching avlReport to send to SFMTA API when have "
				+ "enough reports. {}", avlReport);
		SfmtaApiCaller.postAvlReportWhenAppropriate(avlReport);
	}
}
