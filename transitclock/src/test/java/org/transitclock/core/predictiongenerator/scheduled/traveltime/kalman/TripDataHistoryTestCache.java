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

import org.transitclock.core.dataCache.TripDataHistoryCacheInterface;
import org.transitclock.core.dataCache.TripKey;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.ipc.data.IpcArrivalDeparture;

import java.util.List;

/**
 * Test implementation of TripDataHistoryCache that does nothing.
 */
public class TripDataHistoryTestCache implements TripDataHistoryCacheInterface {
  @Override
  public List<IpcArrivalDeparture> getTripHistory(TripKey tripKey) {
    return null;
  }

  @Override
  public TripKey putArrivalDeparture(ArrivalDeparture arrivalDeparture) {
    return null;
  }

  @Override
  public void populateCacheFromDb(List<ArrivalDeparture> results) {

  }

  @Override
  public IpcArrivalDeparture findPreviousArrivalEvent(List<IpcArrivalDeparture> arrivalDepartures, IpcArrivalDeparture current) {
    return null;
  }

  @Override
  public IpcArrivalDeparture findPreviousDepartureEvent(List<IpcArrivalDeparture> arrivalDepartures, IpcArrivalDeparture current) {
    return null;
  }

  @Override
  public List<TripKey> getKeys() {
    return null;
  }
}
