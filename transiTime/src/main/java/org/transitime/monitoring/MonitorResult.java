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

package org.transitime.monitoring;

import java.io.Serializable;

/**
 * Contains status for an individual monitor. To be based
 * via IPC to client.
 *
 * @author SkiBu Smith
 *
 */
public class MonitorResult implements Serializable {

	private final String type;
	private final String message;

	private static final long serialVersionUID = 8865389000445125279L;

	/********************** Member Functions **************************/

	public MonitorResult(String type, String message) {
		this.type = type;
		this.message = message;
	}
	
	@Override
	public String toString() {
		return "MonitorResult [type=" + type + ", message=" + message + "]";
	}

	public String getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}

}
