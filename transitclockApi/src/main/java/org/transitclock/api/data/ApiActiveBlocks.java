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

package org.transitclock.api.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitclock.ipc.data.IpcActiveBlock;

/**
 * Collection of ActiveBlocks
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement(name="blocks")
public class ApiActiveBlocks {

    @XmlElement(name="blocks")
    private List<ApiActiveBlock> activeBlocks;

	/********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
    protected ApiActiveBlocks() {}

	public ApiActiveBlocks(Collection<IpcActiveBlock> ipcActiveBlocks,
			String agencyId, SpeedFormat speedFormat) throws IllegalAccessException, InvocationTargetException {
    	activeBlocks = new ArrayList<ApiActiveBlock>();
    	for (IpcActiveBlock ipcActiveBlock : ipcActiveBlocks) {
    		activeBlocks.add(new ApiActiveBlock(ipcActiveBlock, agencyId, speedFormat));
    	}
    	
    	// Sort the active blocks by routeId so that can more easily display
    	// the results in order that is clear to user
    	Collections.sort(activeBlocks, comparator);
    }
    
    /**
     * For sorting the active blocks by route and then block ID
     */
	private static final Comparator<ApiActiveBlock> comparator = 
			new Comparator<ApiActiveBlock>() {
		@Override
		public int compare(ApiActiveBlock o1, ApiActiveBlock o2) {
			// Compare route IDs
			int result = o1.getApiTripSummary().getRouteId().compareTo(o2.getApiTripSummary().getRouteId());
			if (result != 0)
				return result;

			// Route IDs the same so compare block IDs
			return o1.getId().compareTo(o2.getId());
		}
	};

}
