package io.swagger.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.joda.time.DateTime;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2022-05-02T11:56:05.975-04:00")
public class Statistics   {
  
  private DateTime captureTime = null;
  private Integer activeUsers = null;
  private Integer activeDevices = null;
  private Integer requests = null;
  private Integer messagesReceived = null;
  private Integer messagesStored = null;

  
  /**
   * in IS0 8601 format. eg. `1963-11-22T18:30:00Z`
   **/
  public Statistics captureTime(DateTime captureTime) {
    this.captureTime = captureTime;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "in IS0 8601 format. eg. `1963-11-22T18:30:00Z`")
  @JsonProperty("captureTime")
  public DateTime getCaptureTime() {
    return captureTime;
  }
  public void setCaptureTime(DateTime captureTime) {
    this.captureTime = captureTime;
  }

  
  /**
   **/
  public Statistics activeUsers(Integer activeUsers) {
    this.activeUsers = activeUsers;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("activeUsers")
  public Integer getActiveUsers() {
    return activeUsers;
  }
  public void setActiveUsers(Integer activeUsers) {
    this.activeUsers = activeUsers;
  }

  
  /**
   **/
  public Statistics activeDevices(Integer activeDevices) {
    this.activeDevices = activeDevices;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("activeDevices")
  public Integer getActiveDevices() {
    return activeDevices;
  }
  public void setActiveDevices(Integer activeDevices) {
    this.activeDevices = activeDevices;
  }

  
  /**
   **/
  public Statistics requests(Integer requests) {
    this.requests = requests;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("requests")
  public Integer getRequests() {
    return requests;
  }
  public void setRequests(Integer requests) {
    this.requests = requests;
  }

  
  /**
   **/
  public Statistics messagesReceived(Integer messagesReceived) {
    this.messagesReceived = messagesReceived;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("messagesReceived")
  public Integer getMessagesReceived() {
    return messagesReceived;
  }
  public void setMessagesReceived(Integer messagesReceived) {
    this.messagesReceived = messagesReceived;
  }

  
  /**
   **/
  public Statistics messagesStored(Integer messagesStored) {
    this.messagesStored = messagesStored;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("messagesStored")
  public Integer getMessagesStored() {
    return messagesStored;
  }
  public void setMessagesStored(Integer messagesStored) {
    this.messagesStored = messagesStored;
  }

  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Statistics statistics = (Statistics) o;
    return Objects.equals(this.captureTime, statistics.captureTime) &&
        Objects.equals(this.activeUsers, statistics.activeUsers) &&
        Objects.equals(this.activeDevices, statistics.activeDevices) &&
        Objects.equals(this.requests, statistics.requests) &&
        Objects.equals(this.messagesReceived, statistics.messagesReceived) &&
        Objects.equals(this.messagesStored, statistics.messagesStored);
  }

  @Override
  public int hashCode() {
    return Objects.hash(captureTime, activeUsers, activeDevices, requests, messagesReceived, messagesStored);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Statistics {\n");
    
    sb.append("    captureTime: ").append(toIndentedString(captureTime)).append("\n");
    sb.append("    activeUsers: ").append(toIndentedString(activeUsers)).append("\n");
    sb.append("    activeDevices: ").append(toIndentedString(activeDevices)).append("\n");
    sb.append("    requests: ").append(toIndentedString(requests)).append("\n");
    sb.append("    messagesReceived: ").append(toIndentedString(messagesReceived)).append("\n");
    sb.append("    messagesStored: ").append(toIndentedString(messagesStored)).append("\n");
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

