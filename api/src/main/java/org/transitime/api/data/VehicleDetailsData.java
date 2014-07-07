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

import javax.xml.bind.annotation.XmlRootElement;

import org.transitime.core.BlockAssignmentMethod;
import org.transitime.ipc.data.Vehicle;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement(name="vehicle")
public class VehicleDetailsData extends VehicleData {

    private int scheduleAdherence;
    private String scheduleAdherenceStr;
    private String blockId;
    private BlockAssignmentMethod blockAssignmentMethod;
    private String tripId;
    private String driverId;
    
    /**
     * Need a no-arg constructor for Jersey. Otherwise get really
     * obtuse "MessageBodyWriter not found for media type=application/json"
     * exception.
     */
    protected VehicleDetailsData() {}

    /**
     * Takes a Vehicle object for client/server communication and constructs a
     * VehicleData object for the API.
     * 
     * @param vehicle
     */
    public VehicleDetailsData(Vehicle vehicle) {
	super(vehicle);
	
	scheduleAdherence = vehicle.getRealTimeSchedAdh().getTemporalDifference();
	scheduleAdherenceStr = vehicle.getRealTimeSchedAdh().toString();
	blockId = vehicle.getBlockId();
	blockAssignmentMethod = vehicle.getBlockAssignmentMethod();
	tripId = vehicle.getTripId();
	driverId = vehicle.getAvl().getDriverId();
    }	

    public int getScheduleAdherence() {
        return scheduleAdherence;
    }

    public void setScheduleAdherence(int scheduleAdherence) {
	this.scheduleAdherence = scheduleAdherence;
    }
    
    public String getScheduleAdherenceStr() {
        return scheduleAdherenceStr;
    }

    public void setScheduleAdherenceStr(String scheduleAdherenceStr) {
	this.scheduleAdherenceStr = scheduleAdherenceStr;
    }
    
    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
	this.blockId = blockId;
    }
    
    public BlockAssignmentMethod getBlockAssignmentMethod() {
	return blockAssignmentMethod;
    }
    
    public void setBlockAssignmentMethod(
	    BlockAssignmentMethod blockAssignmentMethod) {
	this.blockAssignmentMethod = blockAssignmentMethod;
    }

    public String getTripId() {
	return tripId;
    }

    public void setTripId(String tripId) {
	this.tripId = tripId;
    }
    
    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
	this.driverId = driverId;
    }
    
}
