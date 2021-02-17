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
import org.transitclock.utils.Time;

import java.util.Date;

import static org.junit.Assert.*;

public class TrafficDataCacheTest {

  TrafficDataCache cache;

  @Before
  public void setUp() throws Exception {
    cache = new TrafficDataCache(null, null, -1);
  }

  @Test
  public void isLatent() {
    long now = System.currentTimeMillis();
    long old = System.currentTimeMillis() - (10 * Time.MS_PER_MIN);
    TrafficSensorData oldElement
            = new TrafficSensorData(
                    "1",
                    -1,
                    new Date(old),
                    null,
                    null,
                    null,
                    null,
                    null);
    assertTrue(cache.isLatent(oldElement));

    TrafficSensorData newElement
            = new TrafficSensorData(
            "1",
            -1,
            new Date(now),
            null,
            null,
            null,
            null,
            null);

    assertFalse(cache.isLatent(newElement));

  }
}