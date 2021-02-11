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

import org.transitclock.core.Indices;
import org.transitclock.core.dataCache.ErrorCache;
import org.transitclock.core.dataCache.KalmanError;
import org.transitclock.core.dataCache.KalmanErrorCacheKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Testing implementation of KalmanErrorCache that integrates
 * with the testing data generator.
 */
public class KalmanErrorTestCache implements ErrorCache {

  private KalmanDataGenerator test = null;
  public KalmanErrorTestCache(KalmanDataGenerator test) {
    this.test = test;
  }

  private Map<Indices, KalmanError> map = new HashMap<>();

  @Override
  public KalmanError getErrorValue(Indices indices) {
    KalmanError error = map.get(indices);
    if (error == null)
      return test.getErrorValue(indices);
    return error;
  }

  @Override
  public KalmanError getErrorValue(KalmanErrorCacheKey key) {
    throw new UnsupportedOperationException("getErrorValue(KalmanErrorCacheKey key)");
  }

  @Override
  public void putErrorValue(Indices indices, Double value) {
    map.put(indices, new KalmanError(value));
  }

  @Override
  public void putErrorValue(KalmanErrorCacheKey key, Double value) {
    throw new UnsupportedOperationException("putErrorValue(KalmanErrorCacheKey key, Double value)");
  }

  @Override
  public List<KalmanErrorCacheKey> getKeys() {
    throw new UnsupportedOperationException("getKeys()");
  }
}
