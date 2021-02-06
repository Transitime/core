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

import org.junit.Before;
import org.junit.Test;
import org.transitclock.db.structs.TrafficSensorData;

import java.util.List;

import static org.junit.Assert.*;

public class TrafficManagerTest {

  private TrafficManager tm;
  @Before
  public void setUp() throws Exception {
    tm = new TrafficManager();
    tm.setTrafficRev(0);
  }

  @Test
  public void loadData() throws Exception {
    List<TrafficSensorData> sensorData = tm.loadData();
    assertNotNull(sensorData);
    assertTrue(sensorData.size() > 40);
  }
}