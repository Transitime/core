package io.swagger.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import org.joda.time.DateTime;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2022-05-02T11:56:05.975-04:00")
public class ReportTrips   {
  
  private Integer deviceId = null;
  private String deviceName = null;
  private BigDecimal maxSpeed = null;
  private BigDecimal averageSpeed = null;
  private BigDecimal distance = null;
  private BigDecimal spentFuel = null;
  private Integer duration = null;
  private DateTime startTime = null;
  private String startAddress = null;
  private BigDecimal startLat = null;
  private BigDecimal startLon = null;
  private DateTime endTime = null;
  private String endAddress = null;
  private BigDecimal endLat = null;
  private BigDecimal endLon = null;
  private Integer driverUniqueId = null;
  private String driverName = null;

  
  /**
   **/
  public ReportTrips deviceId(Integer deviceId) {
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
  public ReportTrips deviceName(String deviceName) {
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
  public ReportTrips maxSpeed(BigDecimal maxSpeed) {
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
  public ReportTrips averageSpeed(BigDecimal averageSpeed) {
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
  public ReportTrips distance(BigDecimal distance) {
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
  public ReportTrips spentFuel(BigDecimal spentFuel) {
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
  public ReportTrips duration(Integer duration) {
    this.duration = duration;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("duration")
  public Integer getDuration() {
    return duration;
  }
  public void setDuration(Integer duration) {
    this.duration = duration;
  }

  
  /**
   * in IS0 8601 format. eg. `1963-11-22T18:30:00Z`
   **/
  public ReportTrips startTime(DateTime startTime) {
    this.startTime = startTime;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "in IS0 8601 format. eg. `1963-11-22T18:30:00Z`")
  @JsonProperty("startTime")
  public DateTime getStartTime() {
    return startTime;
  }
  public void setStartTime(DateTime startTime) {
    this.startTime = startTime;
  }

  
  /**
   **/
  public ReportTrips startAddress(String startAddress) {
    this.startAddress = startAddress;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("startAddress")
  public String getStartAddress() {
    return startAddress;
  }
  public void setStartAddress(String startAddress) {
    this.startAddress = startAddress;
  }

  
  /**
   **/
  public ReportTrips startLat(BigDecimal startLat) {
    this.startLat = startLat;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("startLat")
  public BigDecimal getStartLat() {
    return startLat;
  }
  public void setStartLat(BigDecimal startLat) {
    this.startLat = startLat;
  }

  
  /**
   **/
  public ReportTrips startLon(BigDecimal startLon) {
    this.startLon = startLon;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("startLon")
  public BigDecimal getStartLon() {
    return startLon;
  }
  public void setStartLon(BigDecimal startLon) {
    this.startLon = startLon;
  }

  
  /**
   * in IS0 8601 format. eg. `1963-11-22T18:30:00Z`
   **/
  public ReportTrips endTime(DateTime endTime) {
    this.endTime = endTime;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "in IS0 8601 format. eg. `1963-11-22T18:30:00Z`")
  @JsonProperty("endTime")
  public DateTime getEndTime() {
    return endTime;
  }
  public void setEndTime(DateTime endTime) {
    this.endTime = endTime;
  }

  
  /**
   **/
  public ReportTrips endAddress(String endAddress) {
    this.endAddress = endAddress;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("endAddress")
  public String getEndAddress() {
    return endAddress;
  }
  public void setEndAddress(String endAddress) {
    this.endAddress = endAddress;
  }

  
  /**
   **/
  public ReportTrips endLat(BigDecimal endLat) {
    this.endLat = endLat;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("endLat")
  public BigDecimal getEndLat() {
    return endLat;
  }
  public void setEndLat(BigDecimal endLat) {
    this.endLat = endLat;
  }

  
  /**
   **/
  public ReportTrips endLon(BigDecimal endLon) {
    this.endLon = endLon;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("endLon")
  public BigDecimal getEndLon() {
    return endLon;
  }
  public void setEndLon(BigDecimal endLon) {
    this.endLon = endLon;
  }

  
  /**
   **/
  public ReportTrips driverUniqueId(Integer driverUniqueId) {
    this.driverUniqueId = driverUniqueId;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("driverUniqueId")
  public Integer getDriverUniqueId() {
    return driverUniqueId;
  }
  public void setDriverUniqueId(Integer driverUniqueId) {
    this.driverUniqueId = driverUniqueId;
  }

  
  /**
   **/
  public ReportTrips driverName(String driverName) {
    this.driverName = driverName;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("driverName")
  public String getDriverName() {
    return driverName;
  }
  public void setDriverName(String driverName) {
    this.driverName = driverName;
  }

  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReportTrips reportTrips = (ReportTrips) o;
    return Objects.equals(this.deviceId, reportTrips.deviceId) &&
        Objects.equals(this.deviceName, reportTrips.deviceName) &&
        Objects.equals(this.maxSpeed, reportTrips.maxSpeed) &&
        Objects.equals(this.averageSpeed, reportTrips.averageSpeed) &&
        Objects.equals(this.distance, reportTrips.distance) &&
        Objects.equals(this.spentFuel, reportTrips.spentFuel) &&
        Objects.equals(this.duration, reportTrips.duration) &&
        Objects.equals(this.startTime, reportTrips.startTime) &&
        Objects.equals(this.startAddress, reportTrips.startAddress) &&
        Objects.equals(this.startLat, reportTrips.startLat) &&
        Objects.equals(this.startLon, reportTrips.startLon) &&
        Objects.equals(this.endTime, reportTrips.endTime) &&
        Objects.equals(this.endAddress, reportTrips.endAddress) &&
        Objects.equals(this.endLat, reportTrips.endLat) &&
        Objects.equals(this.endLon, reportTrips.endLon) &&
        Objects.equals(this.driverUniqueId, reportTrips.driverUniqueId) &&
        Objects.equals(this.driverName, reportTrips.driverName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deviceId, deviceName, maxSpeed, averageSpeed, distance, spentFuel, duration, startTime, startAddress, startLat, startLon, endTime, endAddress, endLat, endLon, driverUniqueId, driverName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReportTrips {\n");
    
    sb.append("    deviceId: ").append(toIndentedString(deviceId)).append("\n");
    sb.append("    deviceName: ").append(toIndentedString(deviceName)).append("\n");
    sb.append("    maxSpeed: ").append(toIndentedString(maxSpeed)).append("\n");
    sb.append("    averageSpeed: ").append(toIndentedString(averageSpeed)).append("\n");
    sb.append("    distance: ").append(toIndentedString(distance)).append("\n");
    sb.append("    spentFuel: ").append(toIndentedString(spentFuel)).append("\n");
    sb.append("    duration: ").append(toIndentedString(duration)).append("\n");
    sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
    sb.append("    startAddress: ").append(toIndentedString(startAddress)).append("\n");
    sb.append("    startLat: ").append(toIndentedString(startLat)).append("\n");
    sb.append("    startLon: ").append(toIndentedString(startLon)).append("\n");
    sb.append("    endTime: ").append(toIndentedString(endTime)).append("\n");
    sb.append("    endAddress: ").append(toIndentedString(endAddress)).append("\n");
    sb.append("    endLat: ").append(toIndentedString(endLat)).append("\n");
    sb.append("    endLon: ").append(toIndentedString(endLon)).append("\n");
    sb.append("    driverUniqueId: ").append(toIndentedString(driverUniqueId)).append("\n");
    sb.append("    driverName: ").append(toIndentedString(driverName)).append("\n");
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

