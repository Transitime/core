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

package org.transitclock.ipc.data;

import java.io.Serializable;

import com.google.common.base.Objects;
import org.transitclock.db.structs.Location;
import org.transitclock.db.structs.Stop;

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
	private Double stopPathLength;
	
	public Double getStopPathLength() {
		return stopPathLength;
	}

	public void setStopPathLength(Double stopPathLength) {
		this.stopPathLength = stopPathLength;
	}

	private static final long serialVersionUID = 8964112532327897125L;

	/********************** Member Functions **************************/

	public IpcStop(Stop dbStop, boolean aUiStop, String directionId, Double stopPathLength) {
		this.id = dbStop.getId();
		this.name = dbStop.getName();
		this.code = dbStop.getCode();
		this.loc = dbStop.getLoc();
		this.isUiStop = aUiStop;
		this.directionId = directionId;
		this.stopPathLength=stopPathLength;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		IpcStop ipcStop = (IpcStop) o;
		return isUiStop == ipcStop.isUiStop &&
				Objects.equal(id, ipcStop.id) &&
				Objects.equal(name, ipcStop.name) &&
				Objects.equal(code, ipcStop.code) &&
				Objects.equal(loc, ipcStop.loc) &&
				Objects.equal(directionId, ipcStop.directionId) &&
				Objects.equal(stopPathLength, ipcStop.stopPathLength);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, name, code, loc, isUiStop, directionId, stopPathLength);
	}
}
