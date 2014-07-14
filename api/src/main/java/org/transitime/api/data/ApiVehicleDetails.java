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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.transitime.core.BlockAssignmentMethod;
import org.transitime.ipc.data.IpcVehicle;

/**
 * Contains data for a single vehicle with additional info that is meant more
 * for management than for passengers.
 * <p>
 * Note: unfortunately cannot put the attributes in this class after the
 *  attributes of the parent class.
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement(name="vehicle")
@XmlType(propOrder = { "scheduleAdherence",
	"scheduleAdherenceStr", "blockId", "blockAssignmentMethod", "tripId",
	"driverId"})
public class ApiVehicleDetails extends ApiVehicle {

    @XmlAttribute(name="schAdh")
    private int scheduleAdherence;
 
    @XmlAttribute(name="schAdhStr")
    private String scheduleAdherenceStr;

    @XmlAttribute(name="block")
    private String blockId;
 
    @XmlAttribute(name="blockMthd")
    private BlockAssignmentMethod blockAssignmentMethod;
    
    @XmlAttribute(name="trip")
    private String tripId;
    
    @XmlElement(name="driver")
    private String driverId;
    
    /**
     * Need a no-arg constructor for Jersey. Otherwise get really
     * obtuse "MessageBodyWriter not found for media type=application/json"
     * exception.
     */
    protected ApiVehicleDetails() {}

    /**
     * Takes a Vehicle object for client/server communication and constructs a
     * ApiVehicle object for the API.
     * 
     * @param vehicle
     */
    public ApiVehicleDetails(IpcVehicle vehicle) {
	super(vehicle);
	
	scheduleAdherence = vehicle.getRealTimeSchedAdh().getTemporalDifference();
	scheduleAdherenceStr = vehicle.getRealTimeSchedAdh().toString();
	blockId = vehicle.getBlockId();
	blockAssignmentMethod = vehicle.getBlockAssignmentMethod();
	tripId = vehicle.getTripId();
	driverId = vehicle.getAvl().getDriverId();
    }	

    
}
