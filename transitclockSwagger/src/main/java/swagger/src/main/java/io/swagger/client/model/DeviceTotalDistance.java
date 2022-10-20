package io.swagger.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2022-05-02T11:56:05.975-04:00")
public class DeviceTotalDistance   {
  
  private Integer deviceId = null;
  private BigDecimal totalDistance = null;

  
  /**
   **/
  public DeviceTotalDistance deviceId(Integer deviceId) {
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
   * in meters
   **/
  public DeviceTotalDistance totalDistance(BigDecimal totalDistance) {
    this.totalDistance = totalDistance;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "in meters")
  @JsonProperty("totalDistance")
  public BigDecimal getTotalDistance() {
    return totalDistance;
  }
  public void setTotalDistance(BigDecimal totalDistance) {
    this.totalDistance = totalDistance;
  }

  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeviceTotalDistance deviceTotalDistance = (DeviceTotalDistance) o;
    return Objects.equals(this.deviceId, deviceTotalDistance.deviceId) &&
        Objects.equals(this.totalDistance, deviceTotalDistance.totalDistance);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deviceId, totalDistance);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeviceTotalDistance {\n");
    
    sb.append("    deviceId: ").append(toIndentedString(deviceId)).append("\n");
    sb.append("    totalDistance: ").append(toIndentedString(totalDistance)).append("\n");
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

