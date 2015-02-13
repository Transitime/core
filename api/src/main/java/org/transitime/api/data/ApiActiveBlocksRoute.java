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

package org.transitime.api.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.transitime.ipc.data.IpcActiveBlock;

/**
 * A route for when outputting active blocks
 *
 * @author SkiBu Smith
 *
 */
public class ApiActiveBlocksRoute {

	@XmlAttribute
	private String id;

	@XmlAttribute(name = "rShortName")
	private String shortName;

	@XmlAttribute
	private String name;

	@XmlElement(name = "block")
	private List<ApiActiveBlock> activeBlocks;
	
	/********************** Member Functions **************************/
	
	protected ApiActiveBlocksRoute() {
	}

	public ApiActiveBlocksRoute(String id, String shortName, String name) {
		this.id = id;
		this.shortName = shortName;
		this.name = name;
		
		activeBlocks = new ArrayList<ApiActiveBlock>();
	}

	public void add(IpcActiveBlock ipcActiveBlock) {
		activeBlocks.add(new ApiActiveBlock(ipcActiveBlock));
	}
	
	public String getName() {
		return name;
	}
}
