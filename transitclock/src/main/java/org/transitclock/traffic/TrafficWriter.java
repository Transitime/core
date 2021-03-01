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
package org.transitclock.traffic;

import org.hibernate.Session;
import org.transitclock.db.structs.TrafficPath;
import org.transitclock.db.structs.TrafficSensor;

import java.util.List;

/**
 * Write out traffic config data to database.  Follows conventions
 * of DbWriter to remain consistent.
 */
public class TrafficWriter {
  private Session session;
  private Integer trafficRev;
  public TrafficWriter(Session session, Integer trafficRev) {
    this.session = session;
    this.trafficRev = trafficRev;
  }

  /**
   * Write data to database.  Assumes transaction management occurs
   * external to this method.
   * @param trafficPaths
   */
  public void writeTrafficPaths(List<TrafficPath> trafficPaths) {
    for (TrafficPath path : trafficPaths) {
      session.save(path);
    }
  }

  /**
   *
   * Write data to database.  Assumes transaction management occurs
   * external to this method.
   * @param sensors
   */
  public void writeSensors(List<TrafficSensor> sensors) {
    for (TrafficSensor sensor : sensors) {
      session.save(sensor);
    }
  }
}
