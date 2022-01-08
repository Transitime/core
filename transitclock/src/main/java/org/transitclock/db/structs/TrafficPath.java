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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.hibernate.HibernateUtils;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Mirroring the conventions in StopPath, a TrafficPath is a set of points
 * that defines the shape of a traffic sensor.  StopPaths are then mapped
 * to these TrafficPaths where overlap occurs.
 */
@Entity(name="TrafficPath")
@DynamicUpdate
@Table(name="TrafficPaths")
public class TrafficPath implements Serializable {

  @Column(length=2* HibernateUtils.DEFAULT_ID_SIZE)
  @Id
  private final String trafficPathId;

  @Column
  @Id
  private final Integer trafficRev;

  @ElementCollection(fetch=FetchType.LAZY)
  @OrderColumn
  private List<Location> locations;

  @OneToMany(fetch=FetchType.LAZY)
  @Cascade({CascadeType.SAVE_UPDATE})
  @JoinTable(name="TrafficPath_to_StopPath_joinTable")
  @OrderColumn( name="listIndex")
  final protected List<StopPath> stopPaths;

  @Column
  private float pathLength;

  private static long serialVersionUID = 1;

  private static final Logger logger =
          LoggerFactory.getLogger(TrafficPath.class);

  /**
   * Public constructor.
   *
   * @param trafficPathId
   * @param trafficRev
   * @param pathLength
   */
  public TrafficPath(String trafficPathId,
                      int trafficRev,
                      float pathLength) {
    this.trafficPathId = trafficPathId;
    this.trafficRev = trafficRev;
    this.pathLength = pathLength;
    stopPaths = new ArrayList<>();
  }

  /**
   * Needed because Hibernate requires no-arg constructor
   */
  public TrafficPath() {
    this.trafficPathId = null;
    this.trafficRev = null;
    this.pathLength = -1.0f;
    stopPaths = new ArrayList<>();
  }

  public static List<TrafficPath> getTrafficPaths(Session session, Integer trafficRev) {
    String hql = "FROM TrafficPath " +
            " WHERE trafficRev = :trafficRev";
    Query query = session.createQuery(hql);
    query.setInteger("trafficRev", trafficRev);
    return query.list();
  }


  public void setLocations(ArrayList<Location> locations) {
    this.locations = locations;
  }
  public List<Location> getLocations() {
    return locations;
  }

  public List<StopPath> getStopPaths() {
    return stopPaths;
  }

  public String getTrafficPathId() {
    return trafficPathId;
  }

  public int getTrafficRev() {
    return trafficRev;
  }

  @Override
  public int hashCode() {
    return Objects.hash(trafficPathId, trafficRev);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TrafficPath tp = (TrafficPath) o;
    return Objects.equals(this.trafficPathId, tp.trafficPathId) &&
            Objects.equals(this.trafficRev, tp.trafficRev);
  }

}
