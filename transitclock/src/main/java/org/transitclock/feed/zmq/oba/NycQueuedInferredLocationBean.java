/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * The code below comes courtesy of OBA, but paired down to data fields only.  Its a single bean that
 * acts as an interface between systems.
 */
package org.transitclock.feed.zmq.oba;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * An "over the wire", queued inferred location result--gets passed between the inference
 * engine and the TDF/TDS running on all front-end notes, plus the archiver and other inference
 * data consumers.
 *
 * @author jmaki
 *
 */
public class NycQueuedInferredLocationBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private final int DECIMAL_PLACES = 6;

  // the timestamp applied to the record when received by the inference engine
  private Long recordTimestamp;

  private String vehicleId;

  // service date of trip/block
  private Long serviceDate;

  private Integer scheduleDeviation;

  private String blockId;

  private String tripId;

  private Double distanceAlongBlock;

  private Double distanceAlongTrip;

  // snapped lat/long of vehicle to route shape
  private Double inferredLatitude;

  private Double inferredLongitude;

  // raw lat/long of vehicle as reported by BHS.
  private Double observedLatitude;

  private Double observedLongitude;

  // inferred operational status/phase
  private String phase;

  private String status;

  // inference engine telemetry
  private NycVehicleManagementStatusBean managementRecord;

  private String runId;

  private String routeId;

  private double bearing;

  private BigDecimal speed;

  // Fields from TDS

  // Stop ID of next scheduled stop
  private String nextScheduledStopId;

  // Distance to next scheduled stop
  private Double nextScheduledStopDistance;

  // Stop ID from previous scheduled stop
  private String previousScheduledStopId;

  // Distance from previous scheduled stop
  private Double previousScheduledStopDistance;

  private String inferredBlockId;

  private String inferredTripId;

  private String inferredRouteId;

  private String inferredDirectionId;

  private Long lastLocationUpdateTime;

  private String assignedBlockId;


  public NycQueuedInferredLocationBean() {}

  public Long getRecordTimestamp() {
    return recordTimestamp;
  }

  public void setRecordTimestamp(Long recordTimestamp) {
    this.recordTimestamp = recordTimestamp;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public Long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(Long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public Integer getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(Integer scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public String getBlockId() {
    return blockId;
  }

  public void setBlockId(String blockId) {
    this.blockId = blockId;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public Double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  public void setDistanceAlongBlock(Double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }

  public Double getDistanceAlongTrip() {
    return distanceAlongTrip;
  }

  public void setDistanceAlongTrip(Double distanceAlongTrip) {
    this.distanceAlongTrip = distanceAlongTrip;
  }

  public Double getInferredLatitude() {
    return inferredLatitude;
  }

  public void setInferredLatitude(Double inferredLatitude) {
    this.inferredLatitude = inferredLatitude;
  }

  public Double getInferredLongitude() {
    return inferredLongitude;
  }

  public void setInferredLongitude(Double inferredLongitude) {
    this.inferredLongitude = inferredLongitude;
  }

  public Double getObservedLatitude() {
    return observedLatitude;
  }

  public void setObservedLatitude(Double observedLatitude) {
    this.observedLatitude = observedLatitude;
  }

  public Double getObservedLongitude() {
    return observedLongitude;
  }

  public void setObservedLongitude(Double observedLongitude) {
    this.observedLongitude = observedLongitude;
  }

  public String getPhase() {
    return phase;
  }

  public void setPhase(String phase) {
    this.phase = phase;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public NycVehicleManagementStatusBean getManagementRecord() {
    return managementRecord;
  }

  public void setManagementRecord(NycVehicleManagementStatusBean managementRecord) {
    this.managementRecord = managementRecord;
  }

  public void setRunId(String runId) {
    this.runId = runId;
  }

  public String getRunId() {
    return runId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public String getRouteId() {
    return routeId;
  }

  public void setBearing(double bearing) {
    this.bearing = bearing;
  }

  public double getBearing() {
    return bearing;
  }

  // Properties from TDS

  public void setNextScheduledStopId(String nextScheduledStopId) {
    this.nextScheduledStopId = nextScheduledStopId;
  }

  public String getNextScheduledStopId() {
    return nextScheduledStopId;
  }

  public void setNextScheduledStopDistance(Double distance) {
    this.nextScheduledStopDistance = distance;
  }

  public Double getNextScheduledStopDistance() {
    return nextScheduledStopDistance;
  }

  public String getPreviousScheduledStopId() {
    return previousScheduledStopId;
  }

  public void setPreviousScheduledStopId(String previousScheduledStopId) {
    this.previousScheduledStopId = previousScheduledStopId;
  }

  public Double getPreviousScheduledStopDistance() {
    return previousScheduledStopDistance;
  }

  public void setPreviousScheduledStopDistance(
          Double previousScheduledStopDistance) {
    this.previousScheduledStopDistance = previousScheduledStopDistance;
  }

  public String getInferredBlockId() {
    return inferredBlockId;
  }

  public void setInferredBlockId(String inferredBlockId) {
    this.inferredBlockId = inferredBlockId;
  }

  public String getInferredTripId() {
    return inferredTripId;
  }

  public void setInferredTripId(String inferredTripId) {
    this.inferredTripId = inferredTripId;
  }

  public String getInferredRouteId() {
    return inferredRouteId;
  }

  public void setInferredRouteId(String inferredRouteId) {
    this.inferredRouteId = inferredRouteId;
  }

  public String getInferredDirectionId() {
    return inferredDirectionId;
  }

  public void setInferredDirectionId(String inferredDirectionId) {
    this.inferredDirectionId = inferredDirectionId;
  }

  public Long getLastLocationUpdateTime() {
    return lastLocationUpdateTime;
  }

  public void setLastLocationUpdateTime(Long lastLocationUpdateTime) {
    this.lastLocationUpdateTime = lastLocationUpdateTime;
  }

  public BigDecimal getSpeed() {
    return speed;
  }

  public void setSpeed(BigDecimal speed) {
    this.speed = speed;
  }




  private Double scaleDouble(Double doubleVal, int decimalPlaces){
    if(doubleVal == null || doubleVal.isNaN())
      return doubleVal;

    return new BigDecimal(doubleVal).setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP).doubleValue();

  }

  public void setAssignedBlockId(String assignedBlockId) {
    this.assignedBlockId = assignedBlockId;
  }

  public String getAssignedBlockId() {
    return assignedBlockId;
  }

}

