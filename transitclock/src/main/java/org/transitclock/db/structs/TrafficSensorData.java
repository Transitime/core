/*
 * This file is part of Transitime.org
 *
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitclock.db.structs;

import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * Represents one sample of traffic data from a single traffic sensor.
 */
@Entity(name="TrafficSensorData")
@DynamicUpdate
@Table(name="TrafficSensorData")
public class TrafficSensorData implements Serializable {

  @Column
  @Id
  private final String trafficSensorId;

  @Column
  @Id
  private final int trafficRev;

  @Column
  @Id
  private final Date time;

  @Column
  private Double speed;

  @Column
  private Double delayMillis;

  @Column
  private Double confidence;

  @Column
  private Integer travelTimeMillis;

  /**
   * Simple constructor
   * @param trafficSensorId
   * @param trafficRev
   * @param time
   * @param speed
   * @param delayMillis
   * @param confidence
   * @param travelTimeMillis
   */
  public TrafficSensorData(String trafficSensorId,
                           int trafficRev,
                           Date time,
                           Double speed,
                           Double delayMillis,
                           Double confidence,
                           Integer travelTimeMillis) {
    this.trafficSensorId = trafficSensorId;
    this.trafficRev = trafficRev;
    this.time = time;
    this.speed = speed;
    this.delayMillis = delayMillis;
    this.confidence = confidence;
    this.travelTimeMillis = travelTimeMillis;
  }

  /**
   * Hibernate requires no-arg constructor
   */
  public TrafficSensorData() {
    trafficSensorId = null;
    trafficRev = -1;
    time = null;
    speed = null;
    delayMillis = null;
    confidence = null;
    travelTimeMillis = null;
  }

  public String getTrafficSensorId() { return trafficSensorId; }
  public double getSpeed() {
    return speed;
  }

}
