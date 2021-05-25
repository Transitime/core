package org.transitclock.avl;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.dataCache.ehcache.CacheManagerFactory;
import org.transitclock.db.structs.ApcReport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import static org.transitclock.utils.DateUtils.dateBinning;

/**
 * Group APC data to generate counts per minute interval
 */
public class ApcCache {

  private static final Logger logger =
          LoggerFactory.getLogger(ApcCache.class);
  public static IntegerConfigValue WINDOW_IN_MINUTES =
          new IntegerConfigValue("transitclock.apc.cacheBinMinutes",
                  15,
                  "Bin size in minutes to cache APC data");

  private String tz = null;
  private static final String cacheName = "apcCache";
  private Cache<String, ApcEvents> cache = null;

  public ApcCache(String tz) {
    this.tz = tz;
    CacheManager cm = CacheManagerFactory.getInstance();
    cache = cm.getCache(cacheName, String.class, ApcEvents.class);
  }

  public Double getBoardingsPerMinute(String stopId, Date arrival) {
    String hash = hash(stopId, arrival);
    List<ApcReport> apcReports = get(hash);
    if (apcReports == null || apcReports.isEmpty()) return 0.0;
    return new Double(sum(apcReports)) / apcReports.size() / WINDOW_IN_MINUTES.getValue();
  }

  private List<ApcReport> get(String hash) {
    ApcEvents apcEvents = null;
    synchronized (cache) {
      apcEvents = cache.get(hash);
    }
    if (apcEvents != null)
      return apcEvents.getRecords();
    return null;
  }

  public synchronized void analyze(List<ApcReport> matches ) {

    if (matches == null) {
      logger.info("no matches");
      return;
    }
    logger.info("analyze called with {} matches", matches.size());
    for (ApcReport match : matches) {
      String hash = hash(match);
      if (hash == null) continue;
      if (containsKey(hash)) {
        accumulate(hash, match);
      } else {
        create(hash, match);
      }
    }
  }

  private boolean containsKey(String hash) {
    synchronized (cache) {
      return cache.containsKey(hash);
    }
  }

  private void accumulate(String hash, ApcReport match) {
    synchronized (cache) {
      ApcEvents apcEvents = cache.get(hash);
      ArrayList copyAdd = new ArrayList(apcEvents.getRecords());
      copyAdd.add(match);
      cache.put(hash, new ApcEvents(copyAdd));
    }
  }

  private void create(String hash, ApcReport match) {
    ArrayList<ApcReport> empty = new ArrayList<>();
    empty.add(match);
    cache.put(hash, new ApcEvents(empty));
  }

  private String hash(ApcReport match) {
    if (match == null) return null;
    if (match.getArrivalDeparture() == null) return null;
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(tz));
    cal.setTime(new Date(match.getArrivalEpoch()));
    return hash(match.getArrivalDeparture().getStopId(), cal.getTime());
  }

  private String hash(String stopId, Date arrival) {
    Date binArrival = dateBinning(arrival, Calendar.MINUTE, WINDOW_IN_MINUTES.getValue());
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
    if (tz != null) sdf.setTimeZone(TimeZone.getTimeZone(tz));
    return stopId + "." + sdf.format(binArrival);
  }

  private int sum(List<ApcReport> apcReports) {
    int sum = 0;
    Set<String> seenVehicles = new HashSet<>();
    if (apcReports == null || apcReports.isEmpty()) return sum;
    for (ApcReport apc : apcReports) {
      // prune duplicate data
      if (!seenVehicles.contains(apc.getVehicleId())) {
        sum = sum + apc.getBoardings();
        seenVehicles.add(apc.getVehicleId());
      }
    }
    return sum;
  }

  public int cacheSize() {
    return -1;
  }

}
