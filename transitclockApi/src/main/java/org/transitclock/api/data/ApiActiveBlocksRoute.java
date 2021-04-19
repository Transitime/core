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
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.transitclock.ipc.data.IpcActiveBlock;

/**
 * A route for when outputting active blocks
 *
 * @author SkiBu Smith
 *
 */
public class ApiActiveBlocksRoute {

	// ID of route
	@XmlAttribute
	private String id;

	// Route short name
	@XmlAttribute
	private String shortName;

	// Name of route
	@XmlAttribute
	private String name;

	// The active blocks for the route
	@XmlElement(name = "block")
	private List<ApiActiveBlock> activeBlocks;
	
	/********************** Member Functions **************************/
	
    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
	protected ApiActiveBlocksRoute() {
	}

	public ApiActiveBlocksRoute(String id, String shortName, String name) {
		this.id = id;
		this.shortName = shortName;
		this.name = name;
		
		activeBlocks = new ArrayList<ApiActiveBlock>();
	}

	public void add(IpcActiveBlock ipcActiveBlock, String agencyId, SpeedFormat speedFormat) throws IllegalAccessException, InvocationTargetException {
		activeBlocks.add(new ApiActiveBlock(ipcActiveBlock, agencyId, speedFormat));
	}
	
	public String getName() {
		return name;
	}
}
