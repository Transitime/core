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

/**
 * Represents server stats for Inter Process Communication (IPC)
 *
 * @author SkiBu Smith
 *
 */
public class IpcServerStatus implements Serializable {

	private final int dbLoggerQueueSize;
	private final double dbLoggerQueueLevel;

	private static final long serialVersionUID = 1548817311193140609L;

	/********************** Member Functions **************************/

	public IpcServerStatus(int dbLoggerQueueSize, double dbLoggerQueueLevel) {
		this.dbLoggerQueueSize = dbLoggerQueueSize;
		this.dbLoggerQueueLevel = dbLoggerQueueLevel;
	}

	@Override
	public String toString() {
		return "IpcServerStatus [" 
				+ "dbLoggerQueueSize=" + dbLoggerQueueSize
				+ ", dbLoggerQueueLevel=" + dbLoggerQueueLevel
				+ "]";
	}

	public int getDbLoggerQueueSize() {
		return dbLoggerQueueSize;
	}

	public double getDbLoggerQueueLevel() {
		return dbLoggerQueueLevel;
	}
	
}
