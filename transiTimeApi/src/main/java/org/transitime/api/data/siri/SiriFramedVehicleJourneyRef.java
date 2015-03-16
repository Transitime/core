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

package org.transitime.api.data.siri;

import javax.xml.bind.annotation.XmlElement;

import org.transitime.ipc.data.IpcVehicle;

/**
 * Describes the trip
 *
 * @author SkiBu Smith
 *
 */
public class SiriFramedVehicleJourneyRef {

    // The GTFS service date for the trip the vehicle is serving 
    @XmlElement(name="DataFrameRef")
    private String dataFrameRef;
    
    // Trip ID from GTFS
    @XmlElement(name="DatedVehicleJourneyRef")
    private String datedVehicleJourneyRef;
    
    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey for JSON. Otherwise get really
     * obtuse "MessageBodyWriter not found for media type=application/json"
     * exception.
     */
    protected SiriFramedVehicleJourneyRef() {}
    
    public SiriFramedVehicleJourneyRef(IpcVehicle vehicle) {
	// FIXME Note: dataFrameRef is not correct. It should use
	// the service date, not the GPS time. When assignment spans
	// midnight this will be wrong. But of course this isn't too
	// important because if a client would actually want such info
	// they would want service ID, not the date. Sheesh.
	dataFrameRef = Utils.formattedDate(vehicle.getGpsTime());
	datedVehicleJourneyRef = vehicle.getTripId();
    }
}
