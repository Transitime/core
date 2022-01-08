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

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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
  private Double speed; // in meters per second!

  @Column
  private Double delayMillis;

  @Column
  private Double confidence;

  @Column
  private Integer travelTimeMillis;

  @Column
  /**
   * An estimated length of the traffic sensor
   * based on the geometry.  This is NOT accuracte
   * enough for travel time calculation.
   */
  private Double length;

  /**
   * Simple constructor
   * @param trafficSensorId
   * @param trafficRev
   * @param time
   * @param speed
   * @param delayMillis
   * @param confidence
   * @param travelTimeMillis
   * @param length
   */
  public TrafficSensorData(String trafficSensorId,
                           int trafficRev,
                           Date time,
                           Double speed,
                           Double delayMillis,
                           Double confidence,
                           Integer travelTimeMillis,
                           Double length) {
    this.trafficSensorId = trafficSensorId;
    this.trafficRev = trafficRev;
    this.time = time;
    this.speed = speed;
    this.delayMillis = delayMillis;
    this.confidence = confidence;
    this.travelTimeMillis = travelTimeMillis;
    this.length = length;
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
    length = null;
  }

  public String getTrafficSensorId() { return trafficSensorId; }

  /**
   * speed in meters per second.
   * @return
   */
  public Double getSpeed() {
    return speed;
  }
  public Integer getTravelTimeMillis() {
    return travelTimeMillis;
  }

  /**
   * estimated length of the traffic sensor shape.
   * @return
   */
  public double getLength() {
    return length;
  }
  public Date getTime() {
    return time;
  }

  /**
   * do bulk load of data.
   * @param session
   * @param startDate
   * @param endDate
   * @return
   */
  public static Iterator<TrafficSensorData> getTrafficSensorDataIteratorFromDb(Session session, Date startDate, Date endDate) {
    String hql = "FROM TrafficSensorData " +
            " WHERE time >= :beginDate " +
            " AND time < :endDate";
    Query query = session.createQuery(hql);
    query.setTimestamp("beginDate", startDate);
    query.setTimestamp("endDate", endDate);
    //iterator performance on mysql is poor!
    return query.iterate();
  }

  public static List<TrafficSensorData> getTrafficSensorDataFromDb(Session session, Date startDate, Date endDate) {
    String hql = "FROM TrafficSensorData " +
            " WHERE time >= :beginDate " +
            " AND time < :endDate";
    Query query = session.createQuery(hql);
    query.setTimestamp("beginDate", startDate);
    query.setTimestamp("endDate", endDate);

    return query.list();
  }

  @Override
  public int hashCode() {
    return Objects.hash(trafficSensorId, trafficRev, time);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TrafficSensorData tp = (TrafficSensorData) o;
    return Objects.equals(this.trafficSensorId, tp.trafficSensorId) &&
            Objects.equals(this.trafficRev, tp.trafficRev) &&
            Objects.equals(this.time, tp.time);
  }

}
