package io.swagger.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import org.joda.time.DateTime;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2022-05-02T11:56:05.975-04:00")
public class Position   {
  
  private Integer id = null;
  private Integer deviceId = null;
  private String protocol = null;
  private DateTime deviceTime = null;
  private DateTime fixTime = null;
  private DateTime serverTime = null;
  private Boolean outdated = null;
  private Boolean valid = null;
  private BigDecimal latitude = null;
  private BigDecimal longitude = null;
  private BigDecimal altitude = null;
  private BigDecimal speed = null;
  private BigDecimal course = null;
  private String address = null;
  private BigDecimal accuracy = null;
  private String network = null;

  
  /**
   **/
  public Position id(Integer id) {
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
  public Position deviceId(Integer deviceId) {
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
  public Position protocol(String protocol) {
    this.protocol = protocol;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("protocol")
  public String getProtocol() {
    return protocol;
  }
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  
  /**
   * in IS0 8601 format. eg. `1963-11-22T18:30:00Z`
   **/
  public Position deviceTime(DateTime deviceTime) {
    this.deviceTime = deviceTime;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "in IS0 8601 format. eg. `1963-11-22T18:30:00Z`")
  @JsonProperty("deviceTime")
  public DateTime getDeviceTime() {
    return deviceTime;
  }
  public void setDeviceTime(DateTime deviceTime) {
    this.deviceTime = deviceTime;
  }

  
  /**
   * in IS0 8601 format. eg. `1963-11-22T18:30:00Z`
   **/
  public Position fixTime(DateTime fixTime) {
    this.fixTime = fixTime;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "in IS0 8601 format. eg. `1963-11-22T18:30:00Z`")
  @JsonProperty("fixTime")
  public DateTime getFixTime() {
    return fixTime;
  }
  public void setFixTime(DateTime fixTime) {
    this.fixTime = fixTime;
  }

  
  /**
   * in IS0 8601 format. eg. `1963-11-22T18:30:00Z`
   **/
  public Position serverTime(DateTime serverTime) {
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
  public Position outdated(Boolean outdated) {
    this.outdated = outdated;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("outdated")
  public Boolean getOutdated() {
    return outdated;
  }
  public void setOutdated(Boolean outdated) {
    this.outdated = outdated;
  }

  
  /**
   **/
  public Position valid(Boolean valid) {
    this.valid = valid;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("valid")
  public Boolean getValid() {
    return valid;
  }
  public void setValid(Boolean valid) {
    this.valid = valid;
  }

  
  /**
   **/
  public Position latitude(BigDecimal latitude) {
    this.latitude = latitude;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("latitude")
  public BigDecimal getLatitude() {
    return latitude;
  }
  public void setLatitude(BigDecimal latitude) {
    this.latitude = latitude;
  }

  
  /**
   **/
  public Position longitude(BigDecimal longitude) {
    this.longitude = longitude;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("longitude")
  public BigDecimal getLongitude() {
    return longitude;
  }
  public void setLongitude(BigDecimal longitude) {
    this.longitude = longitude;
  }

  
  /**
   **/
  public Position altitude(BigDecimal altitude) {
    this.altitude = altitude;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("altitude")
  public BigDecimal getAltitude() {
    return altitude;
  }
  public void setAltitude(BigDecimal altitude) {
    this.altitude = altitude;
  }

  
  /**
   * in knots
   **/
  public Position speed(BigDecimal speed) {
    this.speed = speed;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "in knots")
  @JsonProperty("speed")
  public BigDecimal getSpeed() {
    return speed;
  }
  public void setSpeed(BigDecimal speed) {
    this.speed = speed;
  }

  
  /**
   **/
  public Position course(BigDecimal course) {
    this.course = course;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("course")
  public BigDecimal getCourse() {
    return course;
  }
  public void setCourse(BigDecimal course) {
    this.course = course;
  }

  
  /**
   **/
  public Position address(String address) {
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
  public Position accuracy(BigDecimal accuracy) {
    this.accuracy = accuracy;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("accuracy")
  public BigDecimal getAccuracy() {
    return accuracy;
  }
  public void setAccuracy(BigDecimal accuracy) {
    this.accuracy = accuracy;
  }

  
  /**
   **/
  public Position network(String network) {
    this.network = network;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("network")
  public String getNetwork() {
    return network;
  }
  public void setNetwork(String network) {
    this.network = network;
  }

  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Position position = (Position) o;
    return Objects.equals(this.id, position.id) &&
        Objects.equals(this.deviceId, position.deviceId) &&
        Objects.equals(this.protocol, position.protocol) &&
        Objects.equals(this.deviceTime, position.deviceTime) &&
        Objects.equals(this.fixTime, position.fixTime) &&
        Objects.equals(this.serverTime, position.serverTime) &&
        Objects.equals(this.outdated, position.outdated) &&
        Objects.equals(this.valid, position.valid) &&
        Objects.equals(this.latitude, position.latitude) &&
        Objects.equals(this.longitude, position.longitude) &&
        Objects.equals(this.altitude, position.altitude) &&
        Objects.equals(this.speed, position.speed) &&
        Objects.equals(this.course, position.course) &&
        Objects.equals(this.address, position.address) &&
        Objects.equals(this.accuracy, position.accuracy) &&
        Objects.equals(this.network, position.network);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, deviceId, protocol, deviceTime, fixTime, serverTime, outdated, valid, latitude, longitude, altitude, speed, course, address, accuracy, network);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Position {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    deviceId: ").append(toIndentedString(deviceId)).append("\n");
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
    sb.append("    deviceTime: ").append(toIndentedString(deviceTime)).append("\n");
    sb.append("    fixTime: ").append(toIndentedString(fixTime)).append("\n");
    sb.append("    serverTime: ").append(toIndentedString(serverTime)).append("\n");
    sb.append("    outdated: ").append(toIndentedString(outdated)).append("\n");
    sb.append("    valid: ").append(toIndentedString(valid)).append("\n");
    sb.append("    latitude: ").append(toIndentedString(latitude)).append("\n");
    sb.append("    longitude: ").append(toIndentedString(longitude)).append("\n");
    sb.append("    altitude: ").append(toIndentedString(altitude)).append("\n");
    sb.append("    speed: ").append(toIndentedString(speed)).append("\n");
    sb.append("    course: ").append(toIndentedString(course)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    accuracy: ").append(toIndentedString(accuracy)).append("\n");
    sb.append("    network: ").append(toIndentedString(network)).append("\n");
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

