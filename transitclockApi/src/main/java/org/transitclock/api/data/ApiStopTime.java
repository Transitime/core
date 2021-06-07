package org.transitclock.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.transitclock.ipc.data.IpcStopTime;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@JsonPropertyOrder({"tripId","arrivalTime","departureTime","stopId","stopSequence","stopHeadsign","pickupType",
                    "dropOffType","shapeDistTraveled","timepoint"})
@XmlType(propOrder={"tripId","arrivalTime","departureTime","stopId","stopSequence",
        "stopHeadsign","pickupType","dropOffType","shapeDistTraveled","timepoint"})
public class ApiStopTime {
    @XmlAttribute(name="trip_id")
    @JsonProperty("trip_id")
    private String tripId;

    @XmlAttribute(name="arrival_time")
    @JsonProperty("arrival_time")
    private String arrivalTime;

    @XmlAttribute(name="departure_time")
    @JsonProperty("departure_time")
    private String departureTime;

    @XmlAttribute(name="stop_id")
    @JsonProperty("stop_id")
    private String stopId;

    @XmlAttribute(name="stop_sequence")
    @JsonProperty("stop_sequence")
    private Integer stopSequence;

    @XmlAttribute(name="stop_headsign")
    @JsonProperty("stop_headsign")
    private String stopHeadsign = null;

    @XmlAttribute(name="pickup_type")
    @JsonProperty("pickup_type")
    private String pickupType = null;

    @XmlAttribute(name="drop_off_type")
    @JsonProperty("drop_off_type")
    private String dropOffType = null;

    @XmlAttribute(name="shape_dist_traveled")
    @JsonProperty("shape_dist_traveled")
    private Long shapeDistTraveled = null;

    @XmlAttribute(name="timepoint")
    @JsonProperty("timepoint")
    private Integer timepoint = null;

    public ApiStopTime() { }

    public ApiStopTime(IpcStopTime ipcStopTime){
        this.tripId = ipcStopTime.getTripId();
        this.arrivalTime = ipcStopTime.getArrivalTime();
        this.departureTime = ipcStopTime.getDepartureTime();
        this.stopId = ipcStopTime.getStopId();
        this.stopSequence = ipcStopTime.getStopSequence();
        this.timepoint = ipcStopTime.getTimepointStop();
    }

    public ApiStopTime(String tripId, String arrivalTime, String departureTime, String stopId, Integer stopSequence,
                       String stopHeadsign, String pickupType, String dropOffType, Integer timepoint,
                       Long shapeDistTraveled) {
        this.tripId = tripId;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.stopId = stopId;
        this.stopSequence = stopSequence;
        this.stopHeadsign = stopHeadsign;
        this.pickupType = pickupType;
        this.dropOffType = dropOffType;
        this.timepoint = timepoint;
        this.shapeDistTraveled = shapeDistTraveled;
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

    public Integer getTimepoint() {
        return timepoint;
    }

    public Long getShapeDistTraveled() {
        return shapeDistTraveled;
    }
}
