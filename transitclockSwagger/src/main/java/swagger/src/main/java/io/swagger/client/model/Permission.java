package io.swagger.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;



/**
 * This is a permission map that contain two object indexes. It is used to link/unlink objects. Order is important. Example: { deviceId:8, geofenceId: 16 }
 **/

@ApiModel(description = "This is a permission map that contain two object indexes. It is used to link/unlink objects. Order is important. Example: { deviceId:8, geofenceId: 16 }")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2022-05-02T11:56:05.975-04:00")
public class Permission   {
  
  private Integer userId = null;
  private Integer deviceId = null;
  private Integer groupId = null;
  private Integer geofenceId = null;
  private Integer calendarId = null;
  private Integer attributeId = null;
  private Integer driverId = null;
  private Integer managedUserId = null;

  
  /**
   * User Id, can be only first parameter
   **/
  public Permission userId(Integer userId) {
    this.userId = userId;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "User Id, can be only first parameter")
  @JsonProperty("userId")
  public Integer getUserId() {
    return userId;
  }
  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  
  /**
   * Device Id, can be first parameter or second only in combination with userId
   **/
  public Permission deviceId(Integer deviceId) {
    this.deviceId = deviceId;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Device Id, can be first parameter or second only in combination with userId")
  @JsonProperty("deviceId")
  public Integer getDeviceId() {
    return deviceId;
  }
  public void setDeviceId(Integer deviceId) {
    this.deviceId = deviceId;
  }

  
  /**
   * Group Id, can be first parameter or second only in combination with userId
   **/
  public Permission groupId(Integer groupId) {
    this.groupId = groupId;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Group Id, can be first parameter or second only in combination with userId")
  @JsonProperty("groupId")
  public Integer getGroupId() {
    return groupId;
  }
  public void setGroupId(Integer groupId) {
    this.groupId = groupId;
  }

  
  /**
   * Geofence Id, can be second parameter only
   **/
  public Permission geofenceId(Integer geofenceId) {
    this.geofenceId = geofenceId;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Geofence Id, can be second parameter only")
  @JsonProperty("geofenceId")
  public Integer getGeofenceId() {
    return geofenceId;
  }
  public void setGeofenceId(Integer geofenceId) {
    this.geofenceId = geofenceId;
  }

  
  /**
   * Geofence Id, can be second parameter only and only in combination with userId
   **/
  public Permission calendarId(Integer calendarId) {
    this.calendarId = calendarId;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Geofence Id, can be second parameter only and only in combination with userId")
  @JsonProperty("calendarId")
  public Integer getCalendarId() {
    return calendarId;
  }
  public void setCalendarId(Integer calendarId) {
    this.calendarId = calendarId;
  }

  
  /**
   * Computed Attribute Id, can be second parameter only
   **/
  public Permission attributeId(Integer attributeId) {
    this.attributeId = attributeId;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Computed Attribute Id, can be second parameter only")
  @JsonProperty("attributeId")
  public Integer getAttributeId() {
    return attributeId;
  }
  public void setAttributeId(Integer attributeId) {
    this.attributeId = attributeId;
  }

  
  /**
   * Driver Id, can be second parameter only
   **/
  public Permission driverId(Integer driverId) {
    this.driverId = driverId;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Driver Id, can be second parameter only")
  @JsonProperty("driverId")
  public Integer getDriverId() {
    return driverId;
  }
  public void setDriverId(Integer driverId) {
    this.driverId = driverId;
  }

  
  /**
   * User Id, can be second parameter only and only in combination with userId
   **/
  public Permission managedUserId(Integer managedUserId) {
    this.managedUserId = managedUserId;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "User Id, can be second parameter only and only in combination with userId")
  @JsonProperty("managedUserId")
  public Integer getManagedUserId() {
    return managedUserId;
  }
  public void setManagedUserId(Integer managedUserId) {
    this.managedUserId = managedUserId;
  }

  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Permission permission = (Permission) o;
    return Objects.equals(this.userId, permission.userId) &&
        Objects.equals(this.deviceId, permission.deviceId) &&
        Objects.equals(this.groupId, permission.groupId) &&
        Objects.equals(this.geofenceId, permission.geofenceId) &&
        Objects.equals(this.calendarId, permission.calendarId) &&
        Objects.equals(this.attributeId, permission.attributeId) &&
        Objects.equals(this.driverId, permission.driverId) &&
        Objects.equals(this.managedUserId, permission.managedUserId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, deviceId, groupId, geofenceId, calendarId, attributeId, driverId, managedUserId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Permission {\n");
    
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    deviceId: ").append(toIndentedString(deviceId)).append("\n");
    sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
    sb.append("    geofenceId: ").append(toIndentedString(geofenceId)).append("\n");
    sb.append("    calendarId: ").append(toIndentedString(calendarId)).append("\n");
    sb.append("    attributeId: ").append(toIndentedString(attributeId)).append("\n");
    sb.append("    driverId: ").append(toIndentedString(driverId)).append("\n");
    sb.append("    managedUserId: ").append(toIndentedString(managedUserId)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

