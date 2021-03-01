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

import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO to contain geometry data of traffic sensor.  Will eventually
 * become TrafficPath.
 */
public class FeatureGeometry {
  private List<Double> lats = new ArrayList<>();
  private List<Double> lons = new ArrayList<>();
  public void addLatLon(double lat, double lon) {
    lats.add(lat);
    lons.add(lon);
  }

  public Coordinate[] getAsCoordinateArray() {
    ArrayList<Coordinate> list = new ArrayList<>();
    for (int i = 0; i<lats.size(); i++) {
      list.add(new Coordinate(lats.get(i), lons.get(i)));
    }
    return list.toArray(new Coordinate[list.size()]);
  }
}
