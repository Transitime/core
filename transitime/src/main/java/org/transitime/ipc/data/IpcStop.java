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

package org.transitime.ipc.data;

import java.io.Serializable;

import org.transitime.db.structs.Location;
import org.transitime.db.structs.Stop;

/**
 * Contains information for a single route. Since Stop objects do 
 * not change dynamically this class is not made immutable via a
 * serialization proxy. This simplifies the class greatly.
 *
 * @author SkiBu Smith
 *
 */
public class IpcStop implements Serializable {

	private final String id;
	private final String name;
	private final Integer code;
	private final Location loc;
	private final boolean isUiStop;
	private final String directionId;
	
	private static final long serialVersionUID = 8964112532327897125L;

	/********************** Member Functions **************************/

	public IpcStop(Stop dbStop, boolean aUiStop, String directionId) {
		this.id = dbStop.getId();
		this.name = dbStop.getName();
		this.code = dbStop.getCode();
		this.loc = dbStop.getLoc();
		this.isUiStop = aUiStop;
		this.directionId = directionId;
	}
	
	/**
	 * Constructs a stop and sets isUiStop to true.
	 * 
	 * @param dbStop
	 */
	public IpcStop(Stop dbStop, String directionId) {
		this.id = dbStop.getId();
		this.name = dbStop.getName();
		this.code = dbStop.getCode();
		this.loc = dbStop.getLoc();
		this.isUiStop = true;
		this.directionId = directionId;
	}
	
	@Override
	public String toString() {
		return "IpcStop [" 
				+ "id=" + id 
				+ ", name=" + name 
				+ ", code=" + code
				+ ", loc=" + loc
				+ ", isUiStop=" + isUiStop
				+ ", directionId" + directionId
				+ "]";
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Integer getCode() {
		return code;
	}

	public Location getLoc() {
		return loc;
	}

	public boolean isUiStop() {
		return isUiStop;
	}
	
	public String getDirectionId() {
		return directionId;
	}
}
