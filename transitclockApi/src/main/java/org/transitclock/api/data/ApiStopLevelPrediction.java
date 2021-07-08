package org.transitclock.api.data;

import org.transitclock.ipc.data.IpcPrediction;

import javax.xml.bind.annotation.XmlElement;

/**
 * Model representing an individual prediction of a stop level API.
 */
public class ApiStopLevelPrediction {

    private static final int VALID_FALSE = 0;
    private static final int VALID_TRUE = 1;

    @XmlElement(name = "is_valid")
    private Integer isValid = VALID_TRUE;
    @XmlElement(name = "error_desc")
    private String errorDescription = null;
    @XmlElement(name = "time")
    private Long time = null;  //seconds from epoch
    @XmlElement(name = "trip")
    private String trip;
    @XmlElement(name = "vehicle")
    private String vehicle;

    // required for serialization
    protected ApiStopLevelPrediction() {
    }

    public ApiStopLevelPrediction(IpcPrediction prediction) {
        if (prediction == null) {
            isValid = VALID_FALSE;
            return;
        }
        trip = prediction.getTripId();
        vehicle = prediction.getVehicleId();
        if (prediction.getPredictionTime() > 0) {
            time = prediction.getPredictionTime() / 1000; // seconds
        } else {
            isValid = VALID_FALSE;
            errorDescription = "Invalid prediction value of " + time;
        }

    }

    public ApiStopLevelPrediction(String errorDescription) {
        this.errorDescription = errorDescription;
        isValid = VALID_FALSE;
    }

}
