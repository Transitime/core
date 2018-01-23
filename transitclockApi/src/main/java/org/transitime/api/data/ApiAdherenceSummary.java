package org.transitime.api.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "adherenceSummary")
public class ApiAdherenceSummary {

  @XmlAttribute
  private Integer late;

  @XmlAttribute
  private Integer ontime;

  @XmlAttribute
  private Integer early;
  
  @XmlAttribute
  private Integer nodata;
  
  @XmlAttribute
  private Integer blocks;
  
  /**
   * Need a no-arg constructor for Jersey. Otherwise get really obtuse
   * "MessageBodyWriter not found for media type=application/json" exception.
   */
  protected ApiAdherenceSummary() {
  }

  public ApiAdherenceSummary(int late, int ontime, int early, int nodata, int blocks) {
    this.late = new Integer(late);
    this.ontime = new Integer(ontime);
    this.early = new Integer(early);
    this.nodata = new Integer(nodata);
    this.blocks = new Integer(blocks);
  }
}
