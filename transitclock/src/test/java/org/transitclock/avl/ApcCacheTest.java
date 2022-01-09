package org.transitclock.avl;

import org.junit.Before;
import org.junit.Test;
import org.transitclock.SingletonSupport;
import org.transitclock.db.structs.ApcReport;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.transitclock.SingletonSupport.toDate;

public class ApcCacheTest extends ApcTest {

  @Before
  public void setupClass() throws Exception {
    SingletonSupport.createTestCore("CDT");

  }

  @Test
  public void createBinKeys() throws Exception {
    ApcCache aggregator = new ApcCache("CDT");
    List<ApcReport> matches = loadMatches();

    Map<String, ApcEvents> routeCache = aggregator.populateRouteCache(matches);
    assertEquals("21:16867:127", stats(routeCache));
    String firstHash = "755:17840";
    ApcEvents apcEvents = routeCache.get(firstHash);
    List<CacheBin> binKeys = aggregator.createBinKeys(routeCache, firstHash);
    assertNotNull(binKeys);
    assertEquals(1, binKeys.size());
    List<ApcReport> sortedRecords = apcEvents.getSortedRecords();
    assertEquals(toDate("2021-04-21", "16:19:32", null).getTime(), apcEvents.getSortedRecords().get(0).getArrivalDeparture().getTime());
    assertEquals(toDate("2021-04-21", "16:15:00", null).getTime(), binKeys.get(0).getTime());
    assertEquals("755", binKeys.get(0).getRouteId());
    assertEquals("17840", binKeys.get(0).getStopId());

    String secondHash = "4:17928";
    apcEvents = routeCache.get(secondHash);
    binKeys = aggregator.createBinKeys(routeCache, secondHash);
    assertNotNull(binKeys);
    assertEquals(10, binKeys.size());
    assertEquals(toDate("2021-04-22", "02:17:45", null).getTime(), apcEvents.getSortedRecords().get(0).getArrivalDeparture().getTime());
    assertEquals(toDate("2021-04-22", "02:15:00", null).getTime(), binKeys.get(0).getTime());
    assertEquals("4", binKeys.get(0).getRouteId());
    assertEquals("17928", binKeys.get(0).getStopId());

    // if this changes the sorting is not stable
    assertEquals(toDate("2021-04-22", "00:29:55", null).getTime(), apcEvents.getSortedRecords().get(1).getArrivalDeparture().getTime());
    assertEquals(toDate("2021-04-22", "00:15:00", null).getTime(), binKeys.get(1).getTime());
    assertEquals("4", binKeys.get(1).getRouteId());
    assertEquals("17928", binKeys.get(1).getStopId());

    String largeHash = "21:16867";
    binKeys = aggregator.createBinKeys(routeCache, largeHash);
    assertNotNull(binKeys);
    assertEquals(54, binKeys.size());
    apcEvents = routeCache.get(largeHash);
    assertEquals(127, apcEvents.getSortedRecords().size());
    ApcReport apcReport = apcEvents.getSortedRecords().get(0);
    assertEquals(toDate("2021-04-22", "05:56:35", null).getTime(), apcEvents.getSortedRecords().get(0).getArrivalDeparture().getTime());

    ApcCacheElement apcCacheElement = aggregator.movingAverage(routeCache, largeHash, binKeys.get(0));
    assertNotNull(apcCacheElement);
    assertEquals(0.6, apcCacheElement.getAverage(), 0.001);

  }

  private String stats(Map<String, ApcEvents> routeCache) {
    int maxSize = 0;
    String maxKey = null;

    for (String key : routeCache.keySet()) {
      if (maxKey == null) {
        maxKey = key;
        maxSize = routeCache.get(key).getRecords().size();
      } else {
        if (routeCache.get(key).getRecords().size() > maxSize) {
          maxKey = key;
          maxSize = routeCache.get(key).getRecords().size();
        }
      }
    }
    return maxKey + ":" + maxSize;
  }
}