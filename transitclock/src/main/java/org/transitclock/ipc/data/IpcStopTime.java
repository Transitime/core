package org.transitclock.ipc.data;

import java.io.Serializable;

public class IpcStopTime implements Serializable {
    private final String tripId;
    private final String arrivalTime;
    private final String departureTime;
    private final String stopId;
    private final Integer stopSequence;
    private String stopHeadsign = null;
    private String pickupType = null;
    private String dropOffType = null;
    private Integer timepointStop = null;
    private Double shapeDistTraveled = null;

    public IpcStopTime(String tripId, String arrivalTime, String departureTime, String stopId, Integer stopSequence){
        this.tripId = tripId;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.stopId = stopId;
        this.stopSequence = stopSequence;
    }

    public IpcStopTime(String tripId, String arrivalTime, String departureTime, String stopId, Integer stopSequence,
                       Boolean timepointStop){
        this.tripId = tripId;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.stopId = stopId;
        this.stopSequence = stopSequence;
        setTimePointAsInt(timepointStop);
    }

    public IpcStopTime(String tripId, String arrivalTime, String departureTime, String stopId,
                       Integer stopSequence, String stopHeadsign, String pickupType, String dropOffType,
                       Boolean timepointStop, Double shapeDistTraveled) {
        this.tripId = tripId;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.stopId = stopId;
        this.stopSequence = stopSequence;
        this.stopHeadsign = stopHeadsign;
        this.pickupType = pickupType;
        this.dropOffType = dropOffType;
        this.shapeDistTraveled = shapeDistTraveled;
        setTimePointAsInt(timepointStop);
    }

    public String getTripId() {
        return tripId;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getStopId() {
        return stopId;
    }

    public Integer getStopSequence() {
        return stopSequence;
    }

    public String getStopHeadsign() {
        return stopHeadsign;
    }

    public String getPickupType() {
        return pickupType;
    }

    public String getDropOffType() {
        return dropOffType;
    }

    public Integer getTimepointStop() {
        return timepointStop;
    }

    public Double getShapeDistTraveled() {
        return shapeDistTraveled;
    }

    public void setTimePointAsInt(Boolean timepointStop) {
        if(timepointStop == null){
            this.timepointStop = null;
        } else if(timepointStop){
            this.timepointStop = 1;
        } else {
            this.timepointStop = 0;
        }
    }

    public void setStopHeadsign(String stopHeadsign) {
        this.stopHeadsign = stopHeadsign;
    }

    public void setPickupType(String pickupType) {
        this.pickupType = pickupType;
    }

    public void setDropOffType(String dropOffType) {
        this.dropOffType = dropOffType;
    }

    public void setTimepointStop(Integer timepointStop) {
        this.timepointStop = timepointStop;
    }

    public void setShapeDistTraveled(Double shapeDistTraveled) {
        this.shapeDistTraveled = shapeDistTraveled;
    }
}
