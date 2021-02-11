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
import org.transitclock.applications.Core;
import org.transitclock.utils.Time;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Test the scheduled implementation of the kalman prediction generator.
 */
public class KalmanPredictionGeneratorImplTest {


  private KalmanPredictionGeneratorImpl generator;
  private KalmanDataGenerator dataGenerator;

  @Before
  public void setup() {
    long referenceTime = new Date().getTime();
    dataGenerator = new KalmanDataGenerator(referenceTime);
    generator = new KalmanPredictionGeneratorTestImpl();

    /**
     * time needs to be populated for ArrivalDeparture creation
     */
    if (!Core.isCoreApplication()) {
      Core.createTestCore(dataGenerator.AGENCY_ID);
      if (Core.getInstance().getTime() == null) {
        Core.getInstance().setTime(new Time(dataGenerator.getTimeZone()));
      }
    }
  }

  @Test
  public void getTravelTimeForPath() {

    long prediction = generator.getTravelTimeForPath(
            dataGenerator.getIndicies(),
            dataGenerator.getAvlReport(),
            dataGenerator.getVehicleState());

    assertEquals(355, prediction);

  }

}