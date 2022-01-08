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
import org.transitclock.db.hibernate.HibernateUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the sensor specific data from an external system.
 */
@Entity(name="TrafficSensor")
@DynamicUpdate
@Table(name="TrafficSensor")
public class TrafficSensor implements Serializable {

  @Column(length=HibernateUtils.DEFAULT_ID_SIZE)
  @Id
  private final String id;

  @Column
  @Id
  private final Integer trafficRev;

  @Column(length=HibernateUtils.DEFAULT_ID_SIZE)
  private String externalId;

  @Column(length=2*HibernateUtils.DEFAULT_ID_SIZE)
  private String trafficPathId;

  @Column
  private String description;

  /**
   * Simple constructor
   * @param id
   * @param trafficRev
   * @param externalId
   */
  public TrafficSensor(String id,
                       int trafficRev,
                       String externalId,
                       String trafficPathId) {
    this.id = id;
    this.trafficRev = trafficRev;
    this.externalId = externalId;
    this.trafficPathId = trafficPathId;
  }

  /**
   * Needed because Hibernate requires no-arg constructor
   */
  public TrafficSensor() {
    this.id = null;
    this.trafficRev = null;
    this.externalId = null;
    this.trafficPathId = null;
  }

  public void setDescription(String s) {
    this.description = s;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, trafficRev);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TrafficSensor tp = (TrafficSensor) o;
    return Objects.equals(this.id, tp.id) &&
            Objects.equals(this.trafficRev, tp.trafficRev);
  }


}
