package io.swagger.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2022-05-02T11:56:05.975-04:00")
public class Device   {
  
  private Integer id = null;
  private String name = null;
  private String uniqueId = null;
  private String status = null;
  private DateTime lastUpdate = null;
  private Integer positionId = null;
  private Integer groupId = null;
  private String phone = null;
  private String model = null;
  private String contact = null;
  private String category = null;
  private List<Integer> geofenceIds = new ArrayList<Integer>();

  
  /**
   **/
  public Device id(Integer id) {
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
  public Device name(String name) {
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
  public Device uniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("uniqueId")
  public String getUniqueId() {
    return uniqueId;
  }
  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  
  /**
   **/
  public Device status(String status) {
    this.status = status;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  
  /**
   * in IS0 8601 format. eg. `1963-11-22T18:30:00Z`
   **/
  public Device lastUpdate(DateTime lastUpdate) {
    this.lastUpdate = lastUpdate;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "in IS0 8601 format. eg. `1963-11-22T18:30:00Z`")
  @JsonProperty("lastUpdate")
  public DateTime getLastUpdate() {
    return lastUpdate;
  }
  public void setLastUpdate(DateTime lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

  
  /**
   **/
  public Device positionId(Integer positionId) {
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
  public Device groupId(Integer groupId) {
    this.groupId = groupId;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("groupId")
  public Integer getGroupId() {
    return groupId;
  }
  public void setGroupId(Integer groupId) {
    this.groupId = groupId;
  }

  
  /**
   **/
  public Device phone(String phone) {
    this.phone = phone;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("phone")
  public String getPhone() {
    return phone;
  }
  public void setPhone(String phone) {
    this.phone = phone;
  }

  
  /**
   **/
  public Device model(String model) {
    this.model = model;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("model")
  public String getModel() {
    return model;
  }
  public void setModel(String model) {
    this.model = model;
  }

  
  /**
   **/
  public Device contact(String contact) {
    this.contact = contact;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("contact")
  public String getContact() {
    return contact;
  }
  public void setContact(String contact) {
    this.contact = contact;
  }

  
  /**
   **/
  public Device category(String category) {
    this.category = category;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("category")
  public String getCategory() {
    return category;
  }
  public void setCategory(String category) {
    this.category = category;
  }

  
  /**
   **/
  public Device geofenceIds(List<Integer> geofenceIds) {
    this.geofenceIds = geofenceIds;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("geofenceIds")
  public List<Integer> getGeofenceIds() {
    return geofenceIds;
  }
  public void setGeofenceIds(List<Integer> geofenceIds) {
    this.geofenceIds = geofenceIds;
  }

  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Device device = (Device) o;
    return Objects.equals(this.id, device.id) &&
        Objects.equals(this.name, device.name) &&
        Objects.equals(this.uniqueId, device.uniqueId) &&
        Objects.equals(this.status, device.status) &&
        Objects.equals(this.lastUpdate, device.lastUpdate) &&
        Objects.equals(this.positionId, device.positionId) &&
        Objects.equals(this.groupId, device.groupId) &&
        Objects.equals(this.phone, device.phone) &&
        Objects.equals(this.model, device.model) &&
        Objects.equals(this.contact, device.contact) &&
        Objects.equals(this.category, device.category) &&
        Objects.equals(this.geofenceIds, device.geofenceIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, uniqueId, status, lastUpdate, positionId, groupId, phone, model, contact, category, geofenceIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Device {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    uniqueId: ").append(toIndentedString(uniqueId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    lastUpdate: ").append(toIndentedString(lastUpdate)).append("\n");
    sb.append("    positionId: ").append(toIndentedString(positionId)).append("\n");
    sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
    sb.append("    phone: ").append(toIndentedString(phone)).append("\n");
    sb.append("    model: ").append(toIndentedString(model)).append("\n");
    sb.append("    contact: ").append(toIndentedString(contact)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    geofenceIds: ").append(toIndentedString(geofenceIds)).append("\n");
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

