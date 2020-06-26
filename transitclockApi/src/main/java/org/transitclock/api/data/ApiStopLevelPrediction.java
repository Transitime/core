package org.transitclock.api.data;

import org.transitclock.ipc.data.IpcPrediction;

import javax.xml.bind.annotation.XmlElement;

/**
 * Model representing an individual prediction of a stop level API.
 */
public class ApiStopLevelPrediction {

    @XmlElement(name = "is_valid")
    private Boolean isValid = null;
    @XmlElement(name = "time")
    private long time;  //seconds from epoch
    @XmlElement(name = "trip")
    private String trip;
    @XmlElement(name = "vehicle")
    private String vehicle;

    // required for serialization
    protected ApiStopLevelPrediction() {
    }

    public ApiStopLevelPrediction(IpcPrediction prediction) {
        if (prediction == null) {
            isValid = Boolean.FALSE;
            return;
        }
        trip = prediction.getTripId();
        vehicle = prediction.getVehicleId();
        if (prediction.getPredictionTime() != 0) {
            time = prediction.getPredictionTime() / 1000; // seconds
        } else {
            time = prediction.getPredictionTime();
        }

    }

}
