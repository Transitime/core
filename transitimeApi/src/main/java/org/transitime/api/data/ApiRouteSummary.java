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

import javax.xml.bind.annotation.XmlAttribute;

import org.transitime.ipc.data.IpcRouteSummary;

/**
 * A short description of a route. For when outputting list of routes for
 * agency.
 *
 * @author SkiBu Smith
 *
 */
public class ApiRouteSummary {

	@XmlAttribute
	private String id;

	@XmlAttribute
	private String shortName;

	@XmlAttribute
	private String name;

	/********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
	protected ApiRouteSummary() {
	}

	public ApiRouteSummary(IpcRouteSummary route) {
		this.id = route.getId();
		this.shortName = route.getShortName();
		this.name = route.getName();
	}

}
