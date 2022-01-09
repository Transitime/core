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
import org.junit.Ignore;
import org.junit.Test;
import org.transitclock.SingletonSupport;
import org.transitclock.core.PredictionResult;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Test the scheduled implementation of the kalman prediction generator.
 */
@Ignore
public class KalmanPredictionGeneratorImplTest {


  private KalmanPredictionGeneratorImpl generator;
  private KalmanDataGenerator dataGenerator;

  @Before
  public void setup() {
    long referenceTime = new Date().getTime();
    dataGenerator = new KalmanDataGenerator(referenceTime);
    generator = new KalmanPredictionGeneratorTestImpl();

    // setup core instance to prevent exceptions
    SingletonSupport.createTestCore();
  }

  @Test
  public void getTravelTimeForPath() {

    PredictionResult prediction = generator.getTravelTimeForPath(
            dataGenerator.getIndicies(),
            dataGenerator.getAvlReport(),
            dataGenerator.getVehicleState());

    assertEquals(355, prediction.getPrediction());

  }

}