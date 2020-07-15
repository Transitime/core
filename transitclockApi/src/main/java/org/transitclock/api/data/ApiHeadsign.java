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
import org.transitclock.ipc.data.IpcTripPattern;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.Objects;

/**
 * A short description of a headsign. For when outputting list of headsigns for
 * route and agency.
 *
 * @author Lenny Caraballo
 *
 */
public class ApiHeadsign implements Comparable<ApiHeadsign>{

	@XmlAttribute
	private String headsign;

	@XmlAttribute
	private String direction;

	@XmlAttribute
	private String label;

	/********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
	protected ApiHeadsign() {
	}

	public ApiHeadsign(IpcTripPattern tripPattern) {
		this.headsign = tripPattern.getHeadsign();
		this.direction = tripPattern.getDirectionId();
		this.label = this.headsign + " (" + this.direction + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ApiHeadsign that = (ApiHeadsign) o;
		return Objects.equals(headsign, that.headsign) &&
				Objects.equals(direction, that.direction);
	}

	@Override
	public int hashCode() {
		return Objects.hash(headsign, direction);
	}

	@Override
	public int compareTo(ApiHeadsign apiHeadsign) {
		return this.toString().compareTo(apiHeadsign.toString());
	}

	@Override
	public String toString() {
		return this.headsign + "_" + this.direction;
	}
}
