package io.swagger.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import org.joda.time.DateTime;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2022-05-02T11:56:05.975-04:00")
public class ReportStops   {
  
  private Integer deviceId = null;
  private String deviceName = null;
  private Integer duration = null;
  private DateTime startTime = null;
  private String address = null;
  private BigDecimal lat = null;
  private BigDecimal lon = null;
  private DateTime endTime = null;
  private BigDecimal spentFuel = null;
  private Integer engineHours = null;

  
  /**
   **/
  public ReportStops deviceId(Integer deviceId) {
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
  public ReportStops deviceName(String deviceName) {
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
   **/
  public ReportStops duration(Integer duration) {
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
  public ReportStops startTime(DateTime startTime) {
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
  public ReportStops address(String address) {
    this.address = address;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("address")
  public String getAddress() {
    return address;
  }
  public void setAddress(String address) {
    this.address = address;
  }

  
  /**
   **/
  public ReportStops lat(BigDecimal lat) {
    this.lat = lat;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("lat")
  public BigDecimal getLat() {
    return lat;
  }
  public void setLat(BigDecimal lat) {
    this.lat = lat;
  }

  
  /**
   **/
  public ReportStops lon(BigDecimal lon) {
    this.lon = lon;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("lon")
  public BigDecimal getLon() {
    return lon;
  }
  public void setLon(BigDecimal lon) {
    this.lon = lon;
  }

  
  /**
   * in IS0 8601 format. eg. `1963-11-22T18:30:00Z`
   **/
  public ReportStops endTime(DateTime endTime) {
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
   * in liters
   **/
  public ReportStops spentFuel(BigDecimal spentFuel) {
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
  public ReportStops engineHours(Integer engineHours) {
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
    ReportStops reportStops = (ReportStops) o;
    return Objects.equals(this.deviceId, reportStops.deviceId) &&
        Objects.equals(this.deviceName, reportStops.deviceName) &&
        Objects.equals(this.duration, reportStops.duration) &&
        Objects.equals(this.startTime, reportStops.startTime) &&
        Objects.equals(this.address, reportStops.address) &&
        Objects.equals(this.lat, reportStops.lat) &&
        Objects.equals(this.lon, reportStops.lon) &&
        Objects.equals(this.endTime, reportStops.endTime) &&
        Objects.equals(this.spentFuel, reportStops.spentFuel) &&
        Objects.equals(this.engineHours, reportStops.engineHours);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deviceId, deviceName, duration, startTime, address, lat, lon, endTime, spentFuel, engineHours);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReportStops {\n");
    
    sb.append("    deviceId: ").append(toIndentedString(deviceId)).append("\n");
    sb.append("    deviceName: ").append(toIndentedString(deviceName)).append("\n");
    sb.append("    duration: ").append(toIndentedString(duration)).append("\n");
    sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    lat: ").append(toIndentedString(lat)).append("\n");
    sb.append("    lon: ").append(toIndentedString(lon)).append("\n");
    sb.append("    endTime: ").append(toIndentedString(endTime)).append("\n");
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

