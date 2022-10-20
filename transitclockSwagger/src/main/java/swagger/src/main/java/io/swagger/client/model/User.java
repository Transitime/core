package io.swagger.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import org.joda.time.DateTime;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2022-05-02T11:56:05.975-04:00")
public class User   {
  
  private Integer id = null;
  private String name = null;
  private String email = null;
  private Boolean readonly = null;
  private Boolean admin = null;
  private String map = null;
  private BigDecimal latitude = null;
  private BigDecimal longitude = null;
  private Integer zoom = null;
  private String password = null;
  private Boolean twelveHourFormat = null;
  private String coordinateFormat = null;
  private Boolean disabled = null;
  private DateTime expirationTime = null;
  private Integer deviceLimit = null;
  private Integer userLimit = null;
  private Boolean deviceReadonly = null;
  private Boolean limitCommands = null;
  private String token = null;

  
  /**
   **/
  public User id(Integer id) {
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
  public User name(String name) {
    this.name = name;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   **/
  public User email(String email) {
    this.email = email;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }

  
  /**
   **/
  public User readonly(Boolean readonly) {
    this.readonly = readonly;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("readonly")
  public Boolean getReadonly() {
    return readonly;
  }
  public void setReadonly(Boolean readonly) {
    this.readonly = readonly;
  }

  
  /**
   **/
  public User admin(Boolean admin) {
    this.admin = admin;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("admin")
  public Boolean getAdmin() {
    return admin;
  }
  public void setAdmin(Boolean admin) {
    this.admin = admin;
  }

  
  /**
   **/
  public User map(String map) {
    this.map = map;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("map")
  public String getMap() {
    return map;
  }
  public void setMap(String map) {
    this.map = map;
  }

  
  /**
   **/
  public User latitude(BigDecimal latitude) {
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
  public User longitude(BigDecimal longitude) {
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
  public User zoom(Integer zoom) {
    this.zoom = zoom;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("zoom")
  public Integer getZoom() {
    return zoom;
  }
  public void setZoom(Integer zoom) {
    this.zoom = zoom;
  }

  
  /**
   **/
  public User password(String password) {
    this.password = password;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("password")
  public String getPassword() {
    return password;
  }
  public void setPassword(String password) {
    this.password = password;
  }

  
  /**
   **/
  public User twelveHourFormat(Boolean twelveHourFormat) {
    this.twelveHourFormat = twelveHourFormat;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("twelveHourFormat")
  public Boolean getTwelveHourFormat() {
    return twelveHourFormat;
  }
  public void setTwelveHourFormat(Boolean twelveHourFormat) {
    this.twelveHourFormat = twelveHourFormat;
  }

  
  /**
   **/
  public User coordinateFormat(String coordinateFormat) {
    this.coordinateFormat = coordinateFormat;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("coordinateFormat")
  public String getCoordinateFormat() {
    return coordinateFormat;
  }
  public void setCoordinateFormat(String coordinateFormat) {
    this.coordinateFormat = coordinateFormat;
  }

  
  /**
   **/
  public User disabled(Boolean disabled) {
    this.disabled = disabled;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("disabled")
  public Boolean getDisabled() {
    return disabled;
  }
  public void setDisabled(Boolean disabled) {
    this.disabled = disabled;
  }

  
  /**
   * in IS0 8601 format. eg. `1963-11-22T18:30:00Z`
   **/
  public User expirationTime(DateTime expirationTime) {
    this.expirationTime = expirationTime;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "in IS0 8601 format. eg. `1963-11-22T18:30:00Z`")
  @JsonProperty("expirationTime")
  public DateTime getExpirationTime() {
    return expirationTime;
  }
  public void setExpirationTime(DateTime expirationTime) {
    this.expirationTime = expirationTime;
  }

  
  /**
   **/
  public User deviceLimit(Integer deviceLimit) {
    this.deviceLimit = deviceLimit;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("deviceLimit")
  public Integer getDeviceLimit() {
    return deviceLimit;
  }
  public void setDeviceLimit(Integer deviceLimit) {
    this.deviceLimit = deviceLimit;
  }

  
  /**
   **/
  public User userLimit(Integer userLimit) {
    this.userLimit = userLimit;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("userLimit")
  public Integer getUserLimit() {
    return userLimit;
  }
  public void setUserLimit(Integer userLimit) {
    this.userLimit = userLimit;
  }

  
  /**
   **/
  public User deviceReadonly(Boolean deviceReadonly) {
    this.deviceReadonly = deviceReadonly;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("deviceReadonly")
  public Boolean getDeviceReadonly() {
    return deviceReadonly;
  }
  public void setDeviceReadonly(Boolean deviceReadonly) {
    this.deviceReadonly = deviceReadonly;
  }

  
  /**
   **/
  public User limitCommands(Boolean limitCommands) {
    this.limitCommands = limitCommands;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("limitCommands")
  public Boolean getLimitCommands() {
    return limitCommands;
  }
  public void setLimitCommands(Boolean limitCommands) {
    this.limitCommands = limitCommands;
  }

  
  /**
   **/
  public User token(String token) {
    this.token = token;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("token")
  public String getToken() {
    return token;
  }
  public void setToken(String token) {
    this.token = token;
  }

  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return Objects.equals(this.id, user.id) &&
        Objects.equals(this.name, user.name) &&
        Objects.equals(this.email, user.email) &&
        Objects.equals(this.readonly, user.readonly) &&
        Objects.equals(this.admin, user.admin) &&
        Objects.equals(this.map, user.map) &&
        Objects.equals(this.latitude, user.latitude) &&
        Objects.equals(this.longitude, user.longitude) &&
        Objects.equals(this.zoom, user.zoom) &&
        Objects.equals(this.password, user.password) &&
        Objects.equals(this.twelveHourFormat, user.twelveHourFormat) &&
        Objects.equals(this.coordinateFormat, user.coordinateFormat) &&
        Objects.equals(this.disabled, user.disabled) &&
        Objects.equals(this.expirationTime, user.expirationTime) &&
        Objects.equals(this.deviceLimit, user.deviceLimit) &&
        Objects.equals(this.userLimit, user.userLimit) &&
        Objects.equals(this.deviceReadonly, user.deviceReadonly) &&
        Objects.equals(this.limitCommands, user.limitCommands) &&
        Objects.equals(this.token, user.token);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, email, readonly, admin, map, latitude, longitude, zoom, password, twelveHourFormat, coordinateFormat, disabled, expirationTime, deviceLimit, userLimit, deviceReadonly, limitCommands, token);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class User {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    readonly: ").append(toIndentedString(readonly)).append("\n");
    sb.append("    admin: ").append(toIndentedString(admin)).append("\n");
    sb.append("    map: ").append(toIndentedString(map)).append("\n");
    sb.append("    latitude: ").append(toIndentedString(latitude)).append("\n");
    sb.append("    longitude: ").append(toIndentedString(longitude)).append("\n");
    sb.append("    zoom: ").append(toIndentedString(zoom)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
    sb.append("    twelveHourFormat: ").append(toIndentedString(twelveHourFormat)).append("\n");
    sb.append("    coordinateFormat: ").append(toIndentedString(coordinateFormat)).append("\n");
    sb.append("    disabled: ").append(toIndentedString(disabled)).append("\n");
    sb.append("    expirationTime: ").append(toIndentedString(expirationTime)).append("\n");
    sb.append("    deviceLimit: ").append(toIndentedString(deviceLimit)).append("\n");
    sb.append("    userLimit: ").append(toIndentedString(userLimit)).append("\n");
    sb.append("    deviceReadonly: ").append(toIndentedString(deviceReadonly)).append("\n");
    sb.append("    limitCommands: ").append(toIndentedString(limitCommands)).append("\n");
    sb.append("    token: ").append(toIndentedString(token)).append("\n");
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

