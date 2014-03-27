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

/**
 * For mbta the block assignments from the feed don't
 * match to the GTFS data. Therefore this module must be used
 * for the mbta feed.
 *
 * @author SkiBu Smith
 *
 */
public class MbtaNextBusAvlModule extends NextBusAvlModule {

	/**
	 * @param projectId
	 */
	public MbtaNextBusAvlModule(String projectId) {
		super(projectId);
	}
	
	/**
	 * At least for mbta the feed uses '_' characters in the block
	 * assignment but the GTFS data instead uses '-' characters.
	 * Therefore need to process the block IDs.
	 * 
	 * @param originalBlockIdFromFeed
	 *            the block ID to be modified
	 * @return the modified block ID that corresponds to the GTFS data
	 */
	@Override
	protected String processBlockId(String originalBlockIdFromFeed) {
		if (originalBlockIdFromFeed == null)
			return null;
		else
			return originalBlockIdFromFeed.replace('_', '-');
	}

}
