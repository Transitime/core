package io.swagger.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2022-05-02T11:56:05.975-04:00")
public class Server   {
  
  private Integer id = null;
  private Boolean registration = null;
  private Boolean readonly = null;
  private Boolean deviceReadonly = null;
  private Boolean limitCommands = null;
  private String map = null;
  private String bingKey = null;
  private String mapUrl = null;
  private BigDecimal latitude = null;
  private BigDecimal longitude = null;
  private Integer zoom = null;
  private Boolean twelveHourFormat = null;
  private String version = null;
  private Boolean forceSettings = null;
  private String coordinateFormat = null;

  
  /**
   **/
  public Server id(Integer id) {
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
  public Server registration(Boolean registration) {
    this.registration = registration;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("registration")
  public Boolean getRegistration() {
    return registration;
  }
  public void setRegistration(Boolean registration) {
    this.registration = registration;
  }

  
  /**
   **/
  public Server readonly(Boolean readonly) {
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
  public Server deviceReadonly(Boolean deviceReadonly) {
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
  public Server limitCommands(Boolean limitCommands) {
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
  public Server map(String map) {
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
  public Server bingKey(String bingKey) {
    this.bingKey = bingKey;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("bingKey")
  public String getBingKey() {
    return bingKey;
  }
  public void setBingKey(String bingKey) {
    this.bingKey = bingKey;
  }

  
  /**
   **/
  public Server mapUrl(String mapUrl) {
    this.mapUrl = mapUrl;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("mapUrl")
  public String getMapUrl() {
    return mapUrl;
  }
  public void setMapUrl(String mapUrl) {
    this.mapUrl = mapUrl;
  }

  
  /**
   **/
  public Server latitude(BigDecimal latitude) {
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
  public Server longitude(BigDecimal longitude) {
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
  public Server zoom(Integer zoom) {
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
  public Server twelveHourFormat(Boolean twelveHourFormat) {
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
  public Server version(String version) {
    this.version = version;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  
  /**
   **/
  public Server forceSettings(Boolean forceSettings) {
    this.forceSettings = forceSettings;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("forceSettings")
  public Boolean getForceSettings() {
    return forceSettings;
  }
  public void setForceSettings(Boolean forceSettings) {
    this.forceSettings = forceSettings;
  }

  
  /**
   **/
  public Server coordinateFormat(String coordinateFormat) {
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

  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Server server = (Server) o;
    return Objects.equals(this.id, server.id) &&
        Objects.equals(this.registration, server.registration) &&
        Objects.equals(this.readonly, server.readonly) &&
        Objects.equals(this.deviceReadonly, server.deviceReadonly) &&
        Objects.equals(this.limitCommands, server.limitCommands) &&
        Objects.equals(this.map, server.map) &&
        Objects.equals(this.bingKey, server.bingKey) &&
        Objects.equals(this.mapUrl, server.mapUrl) &&
        Objects.equals(this.latitude, server.latitude) &&
        Objects.equals(this.longitude, server.longitude) &&
        Objects.equals(this.zoom, server.zoom) &&
        Objects.equals(this.twelveHourFormat, server.twelveHourFormat) &&
        Objects.equals(this.version, server.version) &&
        Objects.equals(this.forceSettings, server.forceSettings) &&
        Objects.equals(this.coordinateFormat, server.coordinateFormat);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, registration, readonly, deviceReadonly, limitCommands, map, bingKey, mapUrl, latitude, longitude, zoom, twelveHourFormat, version, forceSettings, coordinateFormat);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Server {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    registration: ").append(toIndentedString(registration)).append("\n");
    sb.append("    readonly: ").append(toIndentedString(readonly)).append("\n");
    sb.append("    deviceReadonly: ").append(toIndentedString(deviceReadonly)).append("\n");
    sb.append("    limitCommands: ").append(toIndentedString(limitCommands)).append("\n");
    sb.append("    map: ").append(toIndentedString(map)).append("\n");
    sb.append("    bingKey: ").append(toIndentedString(bingKey)).append("\n");
    sb.append("    mapUrl: ").append(toIndentedString(mapUrl)).append("\n");
    sb.append("    latitude: ").append(toIndentedString(latitude)).append("\n");
    sb.append("    longitude: ").append(toIndentedString(longitude)).append("\n");
    sb.append("    zoom: ").append(toIndentedString(zoom)).append("\n");
    sb.append("    twelveHourFormat: ").append(toIndentedString(twelveHourFormat)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    forceSettings: ").append(toIndentedString(forceSettings)).append("\n");
    sb.append("    coordinateFormat: ").append(toIndentedString(coordinateFormat)).append("\n");
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

