package io.swagger.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.joda.time.DateTime;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2022-05-02T11:56:05.975-04:00")
public class Event   {
  
  private Integer id = null;
  private String type = null;
  private DateTime serverTime = null;
  private Integer deviceId = null;
  private Integer positionId = null;
  private Integer geofenceId = null;

  
  /**
   **/
  public Event id(Integer id) {
    this.id = id;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("id")
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

  
  /**
   **/
  public Event type(String type) {
    this.type = type;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  
  /**
   * in IS0 8601 format. eg. `1963-11-22T18:30:00Z`
   **/
  public Event serverTime(DateTime serverTime) {
    this.serverTime = serverTime;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "in IS0 8601 format. eg. `1963-11-22T18:30:00Z`")
  @JsonProperty("serverTime")
  public DateTime getServerTime() {
    return serverTime;
  }
  public void setServerTime(DateTime serverTime) {
    this.serverTime = serverTime;
  }

  
  /**
   **/
  public Event deviceId(Integer deviceId) {
    this.deviceId = deviceId;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("deviceId")
  public Integer getDeviceId() {
    return deviceId;
  }
  public void setDeviceId(Integer deviceId) {
    this.deviceId = deviceId;
  }

  
  /**
   **/
  public Event positionId(Integer positionId) {
    this.positionId = positionId;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("positionId")
  public Integer getPositionId() {
    return positionId;
  }
  public void setPositionId(Integer positionId) {
    this.positionId = positionId;
  }

  
  /**
   **/
  public Event geofenceId(Integer geofenceId) {
    this.geofenceId = geofenceId;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("geofenceId")
  public Integer getGeofenceId() {
    return geofenceId;
  }
  public void setGeofenceId(Integer geofenceId) {
    this.geofenceId = geofenceId;
  }

  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Event event = (Event) o;
    return Objects.equals(this.id, event.id) &&
        Objects.equals(this.type, event.type) &&
        Objects.equals(this.serverTime, event.serverTime) &&
        Objects.equals(this.deviceId, event.deviceId) &&
        Objects.equals(this.positionId, event.positionId) &&
        Objects.equals(this.geofenceId, event.geofenceId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, serverTime, deviceId, positionId, geofenceId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Event {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    serverTime: ").append(toIndentedString(serverTime)).append("\n");
    sb.append("    deviceId: ").append(toIndentedString(deviceId)).append("\n");
    sb.append("    positionId: ").append(toIndentedString(positionId)).append("\n");
    sb.append("    geofenceId: ").append(toIndentedString(geofenceId)).append("\n");
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

