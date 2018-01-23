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
import javax.xml.bind.annotation.XmlType;

import org.transitime.ipc.data.IpcStop;

/**
 * Full description of a stop.
 * <p>
 * Note: extending from ApiLocation since have a lat & lon. Would be nice to
 * have ApiLocation as a member but when try this get a internal server 500
 * error.
 *
 * @author SkiBu Smith
 *
 */
@XmlType(propOrder = { "id", "lat", "lon", "name", "code", "minor" })
public class ApiStop extends ApiTransientLocation {

	@XmlAttribute
	private String id;

	@XmlAttribute
	private String name;

	@XmlAttribute
	private Integer code;

	// For indicating that in UI should deemphasize this stop because it
	// is not on a main trip pattern.
	@XmlAttribute(name = "minor")
	private Boolean minor;

	/********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
	protected ApiStop() {
	}

	public ApiStop(IpcStop stop) {
		super(stop.getLoc().getLat(), stop.getLoc().getLon());
		this.id = stop.getId();
		this.name = stop.getName();
		this.code = stop.getCode();
		// If true then set to null so that this attribute won't then be
		// output as XML/JSON, therefore making output a bit more compact.
		this.minor = stop.isUiStop() ? null : true;
	}

}
