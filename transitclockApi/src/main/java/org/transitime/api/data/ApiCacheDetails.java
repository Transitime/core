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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitclock.db.webstructs.WebAgency;
import org.transitclock.ipc.data.IpcVehicle;
import org.transitclock.utils.Time;
import org.transitime.api.rootResources.TransitimeApi.UiMode;

/**
 * For when have list of VehicleDetails. By using this class can control the
 * element name when data is output.
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement
public class ApiCacheDetails {

	public ApiCacheDetails(String name, Integer size) {
		super();
		this.size = size;
		this.name = name;
	}

	@XmlElement(name = "name")
	private String name;
	
	@XmlElement(name = "size")
	private Integer size;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiCacheDetails() {
	}

}
