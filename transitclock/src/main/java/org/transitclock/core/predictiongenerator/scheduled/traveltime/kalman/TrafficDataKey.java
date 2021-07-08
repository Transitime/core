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
package org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman;

import org.transitclock.db.structs.StopPath;
import org.transitclock.utils.Time;

import java.util.Objects;

/**
 * Cache key for TrafficSensorData.
 */
public class TrafficDataKey {
  private StopPath stopPath;
  private Long time;

  public TrafficDataKey(StopPath stopPath,
                    Long time) {
    this.stopPath = stopPath;
    this.time = shave(time);
  }

  public Long getTime() {
    return time;
  }

  public StopPath getStopPath() {
    return stopPath;
  }

  // shave precision to the minute
  Long shave(Long time) {
    long seconds = time / Time.MS_PER_SEC;
    long minutes = seconds / Time.SEC_PER_MIN;

    return minutes * Time.MS_PER_MIN;
  }

  @Override
  public boolean equals(Object otherObject) {
    if (this == otherObject) {
      return true;
    }
    if (!(otherObject instanceof  TrafficDataKey)) {
      return false;
    }

    TrafficDataKey otherKey = (TrafficDataKey) otherObject;
    return Objects.equals(this.stopPath, otherKey.stopPath)
            && Objects.equals(this.time, otherKey.time);
  }


  @Override
  public int hashCode() {
    int result = 17;
    result = 37 * result + Objects.hashCode(stopPath);
    result = 37 * result + Objects.hashCode(time);

    return result;
  }
}
