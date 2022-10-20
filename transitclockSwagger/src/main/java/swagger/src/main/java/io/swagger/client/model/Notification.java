package io.swagger.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2022-05-02T11:56:05.975-04:00")
public class Notification   {
  
  private Integer id = null;
  private String type = null;
  private Boolean always = null;
  private Boolean web = null;
  private Boolean mail = null;
  private Boolean sms = null;

  
  /**
   **/
  public Notification id(Integer id) {
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
  public Notification type(String type) {
    this.type = type;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  
  /**
   **/
  public Notification always(Boolean always) {
    this.always = always;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("always")
  public Boolean getAlways() {
    return always;
  }
  public void setAlways(Boolean always) {
    this.always = always;
  }

  
  /**
   **/
  public Notification web(Boolean web) {
    this.web = web;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("web")
  public Boolean getWeb() {
    return web;
  }
  public void setWeb(Boolean web) {
    this.web = web;
  }

  
  /**
   **/
  public Notification mail(Boolean mail) {
    this.mail = mail;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("mail")
  public Boolean getMail() {
    return mail;
  }
  public void setMail(Boolean mail) {
    this.mail = mail;
  }

  
  /**
   **/
  public Notification sms(Boolean sms) {
    this.sms = sms;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("sms")
  public Boolean getSms() {
    return sms;
  }
  public void setSms(Boolean sms) {
    this.sms = sms;
  }

  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Notification notification = (Notification) o;
    return Objects.equals(this.id, notification.id) &&
        Objects.equals(this.type, notification.type) &&
        Objects.equals(this.always, notification.always) &&
        Objects.equals(this.web, notification.web) &&
        Objects.equals(this.mail, notification.mail) &&
        Objects.equals(this.sms, notification.sms);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, always, web, mail, sms);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Notification {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    always: ").append(toIndentedString(always)).append("\n");
    sb.append("    web: ").append(toIndentedString(web)).append("\n");
    sb.append("    mail: ").append(toIndentedString(mail)).append("\n");
    sb.append("    sms: ").append(toIndentedString(sms)).append("\n");
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

