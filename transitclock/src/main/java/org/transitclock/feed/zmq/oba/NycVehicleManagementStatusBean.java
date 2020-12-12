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
package org.transitclock.feed.zmq.oba;

import java.io.Serializable;

/**
 * An over the wire management record that is sent by the inference engine and received
 * by the TDF. These records are passed to the VehicleTrackingManagementService for processing.
 *
 * @author jmaki
 *
 */
public class NycVehicleManagementStatusBean implements Serializable {

  private static final long serialVersionUID = 1L;

  // record identifier
  private String uuid;

  // is inference enabled?
  private boolean inferenceIsEnabled;

  // last filter update time for this vehicle
  private Long lastUpdateTime;

  // last time a valid (non 0) location update was provided
  private Long lastLocationUpdateTime;

  // last latitude observed
  private Double lastObservedLatitude;

  // lat longitude observed
  private Double lastObservedLongitude;

  // most recent DSC from the bus
  private String mostRecentObservedDestinationSignCode;

  // most recent inferred DSC
  private String lastInferredDestinationSignCode;

  // is this inference engine the primary?
  private boolean inferenceEngineIsPrimary;

  // the bundle ID this result was generated with
  private String activeBundleId;

  // is inference formal? e.g. comes from an assigned run?
  private boolean inferenceIsFormal;

  // the depot this vehicle is assigned to in the vehicle assignment service
  private String depotId;

  // the bus' in-emergency flag.
  private boolean emergencyFlag;

  // the last observed operator ID from the bus.
  private String lastInferredOperatorId;

  // the run ID calculated by the system
  private String inferredRunId;

  // the run ID provided by the operator assignment service
  private String assignedRunId;

  private String assignedBlockId;

  public String getUUID() {
    return uuid;
  }

  public void setUUID(String uuid) {
    this.uuid = uuid;
  }

  public boolean isInferenceIsEnabled() {
    return inferenceIsEnabled;
  }

  public void setInferenceIsEnabled(boolean enabled) {
    this.inferenceIsEnabled = enabled;
  }

  public long getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(long lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  public long getLastLocationUpdateTime() {
    return lastLocationUpdateTime;
  }

  public void setLastLocationUpdateTime(long lastGpsTime) {
    this.lastLocationUpdateTime = lastGpsTime;
  }

  public double getLastObservedLatitude() {
    return lastObservedLatitude;
  }

  public void setLastObservedLatitude(double lastGpsLat) {
    this.lastObservedLatitude = lastGpsLat;
  }

  public double getLastObservedLongitude() {
    return lastObservedLongitude;
  }

  public void setLastObservedLongitude(double lastGpsLon) {
    this.lastObservedLongitude = lastGpsLon;
  }

  public String getMostRecentObservedDestinationSignCode() {
    return mostRecentObservedDestinationSignCode;
  }

  public void setMostRecentObservedDestinationSignCode(String mostRecentDestinationSignCode) {
    this.mostRecentObservedDestinationSignCode = mostRecentDestinationSignCode;
  }

  public String getLastInferredDestinationSignCode() {
    return lastInferredDestinationSignCode;
  }

  public void setLastInferredDestinationSignCode(String inferredDestinationSignCode) {
    this.lastInferredDestinationSignCode = inferredDestinationSignCode;
  }

  public boolean isInferenceIsFormal() {
    return inferenceIsFormal;
  }

  public void setInferenceIsFormal(boolean inferenceIsFormal) {
    this.inferenceIsFormal = inferenceIsFormal;
  }

  public boolean isInferenceEngineIsPrimary() {
    return inferenceEngineIsPrimary;
  }

  public void setInferenceEngineIsPrimary(boolean inferenceEngineIsPrimary) {
    this.inferenceEngineIsPrimary = inferenceEngineIsPrimary;
  }

  public String getActiveBundleId() {
    return activeBundleId;
  }

  public void setActiveBundleId(String activeBundleId) {
    this.activeBundleId = activeBundleId;
  }

  public void setDepotId(String depotId) {
    this.depotId = depotId;
  }

  public String getDepotId() {
    return depotId;
  }

  public boolean isEmergencyFlag() {
    return emergencyFlag;
  }

  public void setEmergencyFlag(boolean emergencyFlag) {
    this.emergencyFlag = emergencyFlag;
  }

  public void setLastInferredOperatorId(String operatorId) {
    this.lastInferredOperatorId = operatorId;
  }

  public String getLastInferredOperatorId() {
    return lastInferredOperatorId;
  }

  public void setInferredRunId(String inferredRunId) {
    this.inferredRunId = inferredRunId;
  }

  public String getInferredRunId() {
    return inferredRunId;
  }

  public String getAssignedRunId() {
    return assignedRunId;
  }

  public void setAssignedRunId(String assignedRunId) {
    this.assignedRunId = assignedRunId;
  }

  public String getAssignedBlockId() {
    return assignedBlockId;
  }

  public void setAssignedBlockId(String assignedBlockId) {
    this.assignedBlockId = assignedBlockId;
  }

}

