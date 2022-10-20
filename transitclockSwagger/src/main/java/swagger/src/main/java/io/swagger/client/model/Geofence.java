package io.swagger.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2022-05-02T11:56:05.975-04:00")
public class Geofence   {
  
  private Integer id = null;
  private String name = null;
  private String description = null;
  private String area = null;
  private Integer calendarId = null;

  
  /**
   **/
  public Geofence id(Integer id) {
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
  public Geofence name(String name) {
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
  public Geofence description(String description) {
    this.description = description;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   **/
  public Geofence area(String area) {
    this.area = area;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("area")
  public String getArea() {
    return area;
  }
  public void setArea(String area) {
    this.area = area;
  }

  
  /**
   **/
  public Geofence calendarId(Integer calendarId) {
    this.calendarId = calendarId;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("calendarId")
  public Integer getCalendarId() {
    return calendarId;
  }
  public void setCalendarId(Integer calendarId) {
    this.calendarId = calendarId;
  }

  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Geofence geofence = (Geofence) o;
    return Objects.equals(this.id, geofence.id) &&
        Objects.equals(this.name, geofence.name) &&
        Objects.equals(this.description, geofence.description) &&
        Objects.equals(this.area, geofence.area) &&
        Objects.equals(this.calendarId, geofence.calendarId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, area, calendarId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Geofence {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    area: ").append(toIndentedString(area)).append("\n");
    sb.append("    calendarId: ").append(toIndentedString(calendarId)).append("\n");
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

