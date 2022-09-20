package io.swagger.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2022-05-02T11:56:05.975-04:00")
public class ReportSummary   {
  
  private Integer deviceId = null;
  private String deviceName = null;
  private BigDecimal maxSpeed = null;
  private BigDecimal averageSpeed = null;
  private BigDecimal distance = null;
  private BigDecimal spentFuel = null;
  private Integer engineHours = null;

  
  /**
   **/
  public ReportSummary deviceId(Integer deviceId) {
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
  public ReportSummary deviceName(String deviceName) {
    this.deviceName = deviceName;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("deviceName")
  public String getDeviceName() {
    return deviceName;
  }
  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  
  /**
   * in knots
   **/
  public ReportSummary maxSpeed(BigDecimal maxSpeed) {
    this.maxSpeed = maxSpeed;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "in knots")
  @JsonProperty("maxSpeed")
  public BigDecimal getMaxSpeed() {
    return maxSpeed;
  }
  public void setMaxSpeed(BigDecimal maxSpeed) {
    this.maxSpeed = maxSpeed;
  }

  
  /**
   * in knots
   **/
  public ReportSummary averageSpeed(BigDecimal averageSpeed) {
    this.averageSpeed = averageSpeed;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "in knots")
  @JsonProperty("averageSpeed")
  public BigDecimal getAverageSpeed() {
    return averageSpeed;
  }
  public void setAverageSpeed(BigDecimal averageSpeed) {
    this.averageSpeed = averageSpeed;
  }

  
  /**
   * in meters
   **/
  public ReportSummary distance(BigDecimal distance) {
    this.distance = distance;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "in meters")
  @JsonProperty("distance")
  public BigDecimal getDistance() {
    return distance;
  }
  public void setDistance(BigDecimal distance) {
    this.distance = distance;
  }

  
  /**
   * in liters
   **/
  public ReportSummary spentFuel(BigDecimal spentFuel) {
    this.spentFuel = spentFuel;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "in liters")
  @JsonProperty("spentFuel")
  public BigDecimal getSpentFuel() {
    return spentFuel;
  }
  public void setSpentFuel(BigDecimal spentFuel) {
    this.spentFuel = spentFuel;
  }

  
  /**
   **/
  public ReportSummary engineHours(Integer engineHours) {
    this.engineHours = engineHours;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("engineHours")
  public Integer getEngineHours() {
    return engineHours;
  }
  public void setEngineHours(Integer engineHours) {
    this.engineHours = engineHours;
  }

  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReportSummary reportSummary = (ReportSummary) o;
    return Objects.equals(this.deviceId, reportSummary.deviceId) &&
        Objects.equals(this.deviceName, reportSummary.deviceName) &&
        Objects.equals(this.maxSpeed, reportSummary.maxSpeed) &&
        Objects.equals(this.averageSpeed, reportSummary.averageSpeed) &&
        Objects.equals(this.distance, reportSummary.distance) &&
        Objects.equals(this.spentFuel, reportSummary.spentFuel) &&
        Objects.equals(this.engineHours, reportSummary.engineHours);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deviceId, deviceName, maxSpeed, averageSpeed, distance, spentFuel, engineHours);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReportSummary {\n");
    
    sb.append("    deviceId: ").append(toIndentedString(deviceId)).append("\n");
    sb.append("    deviceName: ").append(toIndentedString(deviceName)).append("\n");
    sb.append("    maxSpeed: ").append(toIndentedString(maxSpeed)).append("\n");
    sb.append("    averageSpeed: ").append(toIndentedString(averageSpeed)).append("\n");
    sb.append("    distance: ").append(toIndentedString(distance)).append("\n");
    sb.append("    spentFuel: ").append(toIndentedString(spentFuel)).append("\n");
    sb.append("    engineHours: ").append(toIndentedString(engineHours)).append("\n");
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

