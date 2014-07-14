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
import javax.xml.bind.annotation.XmlRootElement;

import org.transitime.ipc.data.IpcPrediction;

/**
 * Contains data for a single prediction.
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement
public class ApiPrediction {

    @XmlAttribute(name="time")
    private long time;
    
    @XmlAttribute(name="sec")
    private int seconds;
    
    @XmlAttribute(name="min")
    private int minutes;
    
    // Most of the time will be predicting arrival predictions. Therefore
    // isDeparture will only be displayed for the more rare times that
    // departure prediction is being provided.
    @XmlAttribute(name="departure")
    private String isDeparture;
    
    @XmlAttribute(name="trip")
    private String tripId;
    
    @XmlAttribute(name="vehicle")
    private String vehicleId;
    
    // Only output if true
    @XmlAttribute(name="notYetStarted")
    private String basedOnScheduledDeparture;
    
    // Only output if passenger count is valid
    @XmlAttribute(name="passengerCount")
    private String passengerCount;
    
    /********************** Member Functions **************************/
    
    /**
     * Need a no-arg constructor for Jersey. Otherwise get really 
     * obtuse "MessageBodyWriter not found for media type=application/json"
     * exception.
     */
    public ApiPrediction() {}

    public ApiPrediction(IpcPrediction prediction) {
	time = prediction.getTime();
	seconds = (int) (time - System.currentTimeMillis()) / 1000;
	// Always round down minutes to be conservative and so that user
	// doesn't miss bus.
	minutes = seconds / 60;
	
	if (!prediction.isArrival())
	    isDeparture = "t";
	
	tripId = prediction.getTripId();
	
	vehicleId = prediction.getVehicleId();
	
	// Only set basedOnScheduledDeparture if true so that it is not output
	// if false since it will then be null
	if (prediction.isAffectedByWaitStop())
	    basedOnScheduledDeparture = "t";
	
	// Only set passengerCount if it is valid so that it is not output if it
	// is not valid since will then be null
	if (prediction.isPassengerCountValid())
	    passengerCount = Integer.toString(prediction.getPassengerCount());
    }

}
