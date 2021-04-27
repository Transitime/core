package org.transitclock.avl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.ApcReport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Group APC data to generate counts per minute interval
 */
public class ApcAggregator {

  private static final Logger logger =
          LoggerFactory.getLogger(ApcAggregator.class);

  private String tz = null;
  private HashMap<String, List<ApcReport>> cache = new HashMap<String, List<ApcReport>>();
  private Map<String, Integer> countsByHash = new HashMap<>();
  private boolean debug = true;

  public ApcAggregator(String tz) {
    this.tz = tz;
  }

  public Integer getCount(String stopId, Date arrival) {
    String hash = hash(stopId, arrival);
    List<ApcReport> apcReports = cache.get(hash);
    return sum(apcReports);
  }

  public synchronized void analyze(List<ApcReport> matches ) {
    cache.clear();
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
    for (int index=0; index<5; index++) {
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


  private class CountsComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
      Map.Entry<String, Integer> e1 = (Map.Entry<String, Integer>) o1;
      Map.Entry<String, Integer> e2 = (Map.Entry<String, Integer>) o2;
      return e2.getValue().compareTo(e1.getValue());
    }
  }
}
