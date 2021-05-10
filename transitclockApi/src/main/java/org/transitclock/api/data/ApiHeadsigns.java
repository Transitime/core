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

import java.util.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitclock.db.structs.Agency;
import org.transitclock.ipc.data.IpcTripPattern;

/**
 * An ordered list of headsigns.
 *
 * @author Lenny Caraballo
 *
 */
@XmlRootElement
public class ApiHeadsigns{
    // So can easily get agency name when getting headsigns. Useful for db reports
    // and such.
    @XmlElement(name = "agency")
    private String agencyName;

    // List of headsign info
    @XmlElement(name = "headsigns")
    private Set<ApiHeadsign> headsignsData;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
    protected ApiHeadsigns() {
    }

    /**
     * Constructs an ApiHeadsigns using a collection of IpcTripPattern
     * objects.
     *
     * @param tripPatterns
     * @param agency so can get agency name
     */
    public ApiHeadsigns(Collection<IpcTripPattern> tripPatterns, Agency agency, boolean formatLabel) {
        headsignsData = new TreeSet<>();
        for (IpcTripPattern tripPattern : tripPatterns) {
            ApiHeadsign headsign = new ApiHeadsign(tripPattern, formatLabel);
            headsignsData.add(headsign);
        }

        // Also set agency name
        agencyName = agency.getName();
    }
}