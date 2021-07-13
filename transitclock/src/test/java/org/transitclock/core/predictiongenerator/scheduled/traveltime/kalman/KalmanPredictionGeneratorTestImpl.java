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

import org.transitclock.applications.Core;
import org.transitclock.core.Indices;
import org.transitclock.core.TravelTimeDetails;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.ErrorCache;
import org.transitclock.core.dataCache.TripDataHistoryCacheInterface;
import org.transitclock.core.dataCache.VehicleStateManager;
import org.transitclock.db.structs.Arrival;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.ipc.data.IpcArrivalDeparture;

import java.util.Date;
import java.util.List;

/**
 * This is a supporting class to KalmanPredictionGeneratorImplTest
 * It is an extension of KalmanPredictionGeneratorImpl that overrides some of the
 * otherwise static references / object lookups.
 */
public class KalmanPredictionGeneratorTestImpl extends KalmanPredictionGeneratorImpl {

  private KalmanDataGenerator test;
  private TripDataHistoryTestCache tripDataHistoryCache = new TripDataHistoryTestCache();
  private KalmanErrorTestCache kalmanErrorCache;
  private long referenceTime;

  public void setReferenceTime(long time) {
    referenceTime = time;
  }

  @Override
  protected VehicleStateManager getVehicleStateManager() {
    return super.getVehicleStateManager();
  }

  @Override
  protected TripDataHistoryCacheInterface getTripCache() {
    return tripDataHistoryCache;
  }

  @Override
  protected ErrorCache getKalmanErrorCache() {
    if (kalmanErrorCache == null) {
      kalmanErrorCache = new KalmanErrorTestCache(test);
    }
    return kalmanErrorCache;
  }

  @Override
  protected TravelTimeDetails getLastVehicleTravelTime(VehicleState currentVehicleState, Indices indices) throws Exception {
    KalmanDataGenerator test = getKalmanDataGenerator();
    ArrivalDeparture d = test.getDeparture(test.getDepartureTime(),
            new Date(test.getAvlTime()),
            test.getBlock());
    IpcArrivalDeparture departure = new IpcArrivalDeparture(d);
    ArrivalDeparture a = new Arrival(KalmanDataGenerator.CONFIG_REV,
            KalmanDataGenerator.VEHICLE,
            test.getArrivalTime(),
            new Date(test.getAvlTime()),
            test.getBlock(),
            0,
            0,
            null,
            KalmanDataGenerator.STOP_PATH_ID,
            false);
    IpcArrivalDeparture arrival = new IpcArrivalDeparture(a);
    TravelTimeDetails ttd = new TravelTimeDetails(departure, arrival);
    Core.getInstance().getDbConfig().addBlockToServiceMap(KalmanDataGenerator.SERVICE_ID, KalmanDataGenerator.BLOCK_ID, test.getBlock());
    return ttd;
  }


  private KalmanDataGenerator getKalmanDataGenerator() {
    if (test == null) {
      test = new KalmanDataGenerator(referenceTime);
    }
    return test;
  }

  private ErrorCache getTestKalmanErrorCache() {
    if (kalmanErrorCache == null) {
      kalmanErrorCache = new KalmanErrorTestCache(test);
    }
    return kalmanErrorCache;
  }

  @Override
  protected List<TravelTimeDetails> getHistoricalTravelTimes(AvlReport avlReport,
                                                             Indices indices,
                                                             VehicleState currentVehicleState) {
    KalmanDataGenerator test = new KalmanDataGenerator(referenceTime);
    return test.getLastDaysTimes();
  }
}
