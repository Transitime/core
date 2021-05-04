package org.transitclock.avl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.db.structs.ApcReport;
import org.transitclock.utils.Time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Group APC data to generate counts per minute interval
 */
public class ApcAggregator {

  public static final IntegerConfigValue apcCacheDaysDefault = new IntegerConfigValue(
          "transitclock.apc.cacheDays",
          null,
          "days of cached data to keep"
  );
  private static final Logger logger =
          LoggerFactory.getLogger(ApcAggregator.class);

  private String tz = null;
  private HashMap<String, List<ApcReport>> cache = new HashMap<String, List<ApcReport>>();
  private Map<String, Integer> countsByHash = new HashMap<>();
  private boolean debug = true;
  private Integer apcCacheDays = null;

  public ApcAggregator(String tz) {
    this.tz = tz;
  }

  public Integer getApcCacheDays() {
    if (apcCacheDays != null)
      return apcCacheDays;
    return apcCacheDaysDefault.getValue();
  }
  public void setApcCacheDays(int cacheDays) {
    this.apcCacheDays = cacheDays;
  }

  public Integer getBoardingsPerMinute(String stopId, Date arrival) {
    String hash = hash(stopId, arrival);
    List<ApcReport> apcReports = cache.get(hash);
    return sum(apcReports);
  }

  public synchronized void analyze(List<ApcReport> matches ) {

    if (matches == null) return;
    for (ApcReport match : matches) {
      String hash = hash(match);
      if (hash == null) continue;
      if (containsKey(hash)) {
        accumulate(hash, match);
      } else {
        create(hash, match);
      }
    }
    if (debug) {
      debugStats();
    }
    prune();
  }

  // remove expired elements from the cache
   void prune() {
    Iterator<Map.Entry<String, List<ApcReport>>> iterator = cache.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, List<ApcReport>> entry = iterator.next();
      if (isOutdated(entry.getKey())) {
        iterator.remove();
        // clean up stats as well
        countsByHash.remove(entry.getKey());
      }
    }
  }

  private boolean isOutdated(String key) {
    if (getApcCacheDays() == null) return false; // keep forever
    return daysOld(key) > getApcCacheDays();
  }

  private int daysOld(String key) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
    String dateComponent = getDateComponentOfKey(key);
    long keyEpoch;
    try {
      Date parse = sdf.parse(dateComponent);
      keyEpoch = parse.getTime();
      return new Long((System.currentTimeMillis() - keyEpoch) / Time.MS_PER_DAY).intValue();
    } catch (ParseException e) {
      logger.error("invalid key in cache {} with date component {}", key, dateComponent);
    }
    return -1;
  }

  private String getDateComponentOfKey(String key) {
    return key.substring(key.lastIndexOf(".")+1,
            key.length());
  }

  private boolean containsKey(String hash) {
    return cache.containsKey(hash);
  }

  private void debugStats() {
    Set<Map.Entry<String, Integer>> entries = countsByHash.entrySet();
    ArrayList<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>();
    sortedEntries.addAll(entries);
    Collections.sort(sortedEntries, new CountsComparator());
    // log top 5
    for (int index=0; index < 5 && index < countsByHash.size(); index++) {
      logger.info("{} most incremented at {}, value {}",
              index,
              sortedEntries.get(index).getKey(),
              sortedEntries.get(index).getValue(),
              cache.get(sortedEntries.get(index).getKey()));
    }
  }


  private void accumulate(String hash, ApcReport match) {
    cache.get(hash).add(match);
    Integer count = countsByHash.get(hash);
    count++;
    countsByHash.put(hash, count);
  }

  private void create(String hash, ApcReport match) {
    ArrayList<ApcReport> empty = new ArrayList<>();
    empty.add(match);
    cache.put(hash, empty);
    countsByHash.put(hash, 1);
  }

  private String hash(ApcReport match) {
    if (match == null) return null;
    if (match.getArrivalDeparture() == null) return null;
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(tz));
    cal.setTime(new Date(match.getArrivalEpoch()));
    return hash(match.getArrivalDeparture().getStopId(), cal.getTime());
  }

  private String hash(String stopId, Date arrival) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
    if (tz != null) sdf.setTimeZone(TimeZone.getTimeZone(tz));
    return stopId + "." + sdf.format(arrival);
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
    return cache.size();
  }

  private class CountsComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
      Map.Entry<String, Integer> e1 = (Map.Entry<String, Integer>) o1;
      Map.Entry<String, Integer> e2 = (Map.Entry<String, Integer>) o2;
      return e2.getValue().compareTo(e1.getValue());
    }
  }
}
