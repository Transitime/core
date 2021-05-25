package org.transitclock.core.predictiongenerator.scheduled.dwell;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.avl.ApcStopTimeEvent;
import org.transitclock.core.Indices;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.ehcache.CacheManagerFactory;
import org.transitclock.db.structs.AvlReport;

/**
 * cache APC Dwell Time results.
 */
public class ApcStopTimeCache {

  private static final Logger logger =
          LoggerFactory.getLogger(ApcStopTimeCache.class);

  private static final String cacheName = "apcStopTimeCache";
  private Cache<ApcStopTimeEvent, Long> cache = null;
  private static ApcStopTimeCache singleton = null;

  private ApcStopTimeCache() {
    CacheManager cm = CacheManagerFactory.getInstance();
    cache = cm.getCache(cacheName, ApcStopTimeEvent.class, Long.class);
    singleton = this;
  }

  public static ApcStopTimeCache getInstance() {
    if (singleton == null) {
      synchronized (cacheName) {
        if (singleton == null) {
          singleton = new ApcStopTimeCache();
        }
      }
    }
    return singleton;
  }

  public Long get(Indices indices, AvlReport avlReport, VehicleState vehicleState) {
    return cache.get(hash(indices, avlReport, vehicleState));
  }

  private ApcStopTimeEvent hash(Indices indices, AvlReport avlReport, VehicleState vehicleState) {
    return new ApcStopTimeEvent(indices, avlReport, vehicleState);
  }

  public void put(Indices indices, AvlReport avlReport, VehicleState vehicleState, Long result) {
    cache.put(hash(indices, avlReport, vehicleState), result);
  }


}
