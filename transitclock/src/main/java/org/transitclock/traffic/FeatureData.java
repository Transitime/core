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


import org.transitclock.db.structs.StopPath;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO to contain traffic sensor specifics.  Will eventually become
 * TrafficSenor.
 */
public class FeatureData {
  private long time;
  private String label;
  private String externalId;
  private FeatureGeometry fg;
  private float length;
  private List<StopPath> snappedStopPaths = new ArrayList();

  public long getTime() {
    return time;
  }
  public void setTime(long time) {
    this.time = time;
  }

  public String getLabel() {
    return label;
  }
  public void setLabel(String label) {
    this.label = label;
  }

  public String getId() {
    return externalId;
  }
  public void setId(String externalId) {
    this.externalId = externalId;
  }
  public void setFeatureGeometry(FeatureGeometry fg) {
    this.fg = fg;
  }

  public void addStopPath(StopPath stopPath) {
    snappedStopPaths.add(stopPath);
  }

  public FeatureGeometry getFeatureGeometry() {
    return fg;
  }

  public List<StopPath> getStopPaths() {
    return snappedStopPaths;
  }

  public float getLength() {
    return length;
  }
  public void setLength(float length) {
    this.length = length;
  }
}
