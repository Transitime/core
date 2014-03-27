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
 * For sf-muni SFMTA the block assignments from the feed don't
 * match to the GTFS data. Therefore this module must be used
 * for the sf-muni AVL feed.
 *
 * @author SkiBu Smith
 * 
 */
public class MuniNextBusAvlModule extends NextBusAvlModule {

	/**
	 * @param projectId
	 */
	public MuniNextBusAvlModule(String projectId) {
		super(projectId);
	}
	
	/**
	 * At least for sf-muni SFMTA they don't use a leading 0 in the block ID in
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
