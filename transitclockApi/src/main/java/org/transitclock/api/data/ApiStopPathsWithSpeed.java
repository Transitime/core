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

import org.transitclock.db.structs.Agency;
import org.transitclock.ipc.data.IpcRoute;
import org.transitclock.ipc.data.IpcRouteSummary;
import org.transitclock.ipc.data.IpcStopPath;
import org.transitclock.ipc.data.IpcStopPathWithSpeed;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An ordered list of stopPaths.
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement
public class ApiStopPathsWithSpeed {
	// So can easily get agency name when getting routes. Useful for db reports
	// and such.

	// List of stop paths info
	@XmlElement(name = "stopPaths")
	private List<ApiStopPathWithSpeed> stopPathData;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiStopPathsWithSpeed() {
	}

	/**
	 * Constructs an ApiStopPaths using a collection of IpcStopPath objects.
	 *
	 * @param stopPaths
	 *            so can get agency name
	 */
	public ApiStopPathsWithSpeed(List<IpcStopPathWithSpeed> stopPaths) {
		stopPathData = new ArrayList<>();
		for (IpcStopPathWithSpeed stopPath : stopPaths) {
			ApiStopPathWithSpeed apiStopPath = new ApiStopPathWithSpeed(stopPath);
			stopPathData.add(apiStopPath);
		}
	}
}
