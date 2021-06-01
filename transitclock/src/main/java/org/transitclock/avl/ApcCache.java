package org.transitclock.avl;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.dataCache.ehcache.CacheManagerFactory;
import org.transitclock.db.structs.ApcReport;
import org.transitclock.utils.Time;
import org.transitclock.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Group APC data to generate counts per minute interval
 *
 * TODO: stopCache is no longer required. Delete it.
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
  private Cache<String, ApcEvents> stopCache = null;
  private Cache<CacheBin, ApcCacheElements> binCache;
  private long cacheSize = 0;

  public ApcCache(String tz) {
    this.tz = tz;
    CacheManager cm = CacheManagerFactory.getInstance();
    stopCache = cm.getCache(cacheName, String.class, ApcEvents.class);
    binCache = cm.getCache(cacheName+"Bin", CacheBin.class, ApcCacheElements.class);
    if (binCache == null) throw new IllegalStateException("Invalid configuration, cache "
    + cacheName+"Bin did not load successfully");
  }

  public Double getBoardingsPerMinuteLegacy(String stopId, Date arrival) {
    String hash = stopHash(stopId, arrival);
    List<ApcReport> apcReports = get(hash);
    if (apcReports == null || apcReports.isEmpty()) return 0.0;
    return new Double(sum(apcReports)) / apcReports.size() / WINDOW_IN_MINUTES.getValue();
  }

  public Double getBoardingsPerMinute(String routeId, String stopId, Date arrival) {
    CacheBin bin = new CacheBin(arrival.getTime(), routeId, stopId, 0, WINDOW_IN_MINUTES.getValue()*Time.MS_PER_MIN);
    ApcCacheElements apcCacheElements = binCache.get(bin);
    Double average = average(apcCacheElements);
    if (average == null) return null;
    return average / WINDOW_IN_MINUTES.getValue();
  }
  public Long getDwellTime(String routeId, String stopId, Date arrivalTime) {
    CacheBin bin = new CacheBin(arrivalTime.getTime(), routeId, stopId, 0, WINDOW_IN_MINUTES.getValue()*Time.MS_PER_MIN);
    ApcCacheElements apcCacheElements = binCache.get(bin);
    if (apcCacheElements == null) return null;
    List<ApcReport> reports = new ArrayList<>();
    for (ApcCacheElement elements : apcCacheElements.getElements()) {
      reports.addAll(elements.getRecords());
    }
    return getDwellAverage(reports);
  }
  // TODO: convert this to use routeCache, and remove stopCache
  public Long getDwellTimeLegacy(String stopId, Date arrivalTime) {
    String hash = stopHash(stopId, arrivalTime);
    List<ApcReport> apcReports = get(hash);
    if (apcReports == null || apcReports.isEmpty()) return null;
    return getDwellAverage(apcReports);
  }

  private Long getDwellAverage(List<ApcReport> apcReports) {
    Long dwellTotal = 0l;
    int count = 0;

    for (ApcReport apc : apcReports) {
      Long arrivalEpoch = 0l;
      Long departureEpoch = 0l;
      if (apc.getDoorOpen() > 0) {
        arrivalEpoch = apc.getDoorOpenEpoch();
      } else if (apc.getArrival() > 0) {
        arrivalEpoch = apc.getArrivalEpoch();
      }
      if (apc.getDoorClose() > 0) {
        departureEpoch = apc.getDoorCloseEpoch();
      } else if (apc.getDeparture() > 0) {
        departureEpoch = apc.getDepartureEpoch();
      }
      if (arrivalEpoch > 0 && departureEpoch > 0) {
        dwellTotal = dwellTotal + (departureEpoch - arrivalEpoch);
        count++;
      }
    }

    if (count == 0) return null;
    return dwellTotal / count;

  }

  private List<ApcReport> get(String hash) {
    ApcEvents apcEvents = null;
    synchronized (stopCache) {
      apcEvents = stopCache.get(hash);
    }
    if (apcEvents != null)
      return apcEvents.getRecords();
    return null;
  }

  public synchronized void analyze(List<ApcReport> matches ) {

    if (matches == null || matches.isEmpty()) {
      logger.info("no matches");
      return;
    }
    logger.info("analyze called with {} matches", matches.size());
    for (ApcReport match : matches) {
      String stopHash = stopHash(match);
      if (stopHash == null) continue;
      if (stopHashContainsKey(stopHash)) {
        accumulateStopCacheElement(stopHash, match);
      } else {
        createStopCacheElement(stopHash, match);
      }
    }

    analyzeStats(matches);

    logger.info("analyze after {} matches cache size is now {} for match date {}",
            matches.size(), cacheSize(), DateUtils.truncate(matches.get(0).getTime(), Calendar.HOUR));

  }

  void analyzeStats(List<ApcReport> matches) {
    Map<String, ApcEvents> routeCache = populateRouteCache(matches);

    for (String routeHash : routeCache.keySet()) {
      List<CacheBin> bins = createBinKeys(routeCache, routeHash);
      for (CacheBin bin : bins) {
        ApcCacheElement element = movingAverage(routeCache, routeHash, bin);
        addToCache(bin, element);
      }
    }
  }

  Map<String, ApcEvents> populateRouteCache(List<ApcReport> matches) {
    Map<String, ApcEvents> routeCache = new HashMap<>();
    for (ApcReport match : matches) {
      String routeHash = routeHash(match);
      if (routeHash == null) continue;
      if (routeHashContainsKey(routeCache, routeHash)) {
        accumulateRouteElement(routeCache, routeHash, match);
      } else {
        createRouteElement(routeCache, routeHash, match);
      }
    }

    return routeCache;
  }

  private void addToCache(CacheBin bin, ApcCacheElement element) {
    if (binCache.containsKey(bin)) {
      ApcCacheElements apcCacheElements = binCache.get(bin);
      binCache.put(bin, new ApcCacheElements(apcCacheElements.getElements(), element));
    } else {
      binCache.put(bin, new ApcCacheElements(element));
    }
  }

  ApcCacheElement movingAverage(Map<String, ApcEvents> routeCache, String routeHash, CacheBin bin) {
    // find the last three elements within a reasonable window
    List<ApcReport> sortedRecords = routeCache.get(routeHash).getSortedRecords();
    List<ApcReport> binElements = new ArrayList<>();
    int index = findIndex(sortedRecords, bin);
    if (index == -1) throw new IllegalStateException("expected element "+ bin + " for bin " + routeHash);

    double total = 0.0;
    int totalElements = 0;
    int historicalElements = 0;
    while (index < sortedRecords.size()
            && historicalElements < 3) {
      ApcReport apcReport = sortedRecords.get(index);
      if (bin.fitsBin(apcReport.getArrivalDeparture().getTime())) {
        binElements.add(apcReport);
      } else {
        historicalElements++;
      }
      total = total + sortedRecords.get(index).getBoardings();
      index++;
      totalElements++;
    }

    double movingAverage = total/totalElements;
    return new ApcCacheElement(movingAverage, binElements);
  }

  private int findIndex(List<ApcReport> sortedRecords, CacheBin bin) {
    int index = 0;

    for (ApcReport apc : sortedRecords) {
      if (bin.getTime() == DateUtils.dateBinning(apc.getArrivalDeparture().getTime(), WINDOW_IN_MINUTES.getValue()*Time.MS_PER_MIN)) {
        return index;
      }
      index++;
    }

    return -1;
  }

  List<CacheBin> createBinKeys(Map<String, ApcEvents> routeCache, String routeHash) {
    ApcEvents apcEvents = routeCache.get(routeHash);
    if (apcEvents == null) return null;
    LinkedHashSet<CacheBin> bins = new LinkedHashSet<>();
    for (ApcReport apc : apcEvents.getSortedRecords()) {
      bins.add(createCacheBin(apc));
    }

    List<CacheBin> sortedBins = new ArrayList<>(bins);
    Collections.sort(sortedBins, new CacheBinComparator());
    return sortedBins;
  }

  private CacheBin createCacheBin(ApcReport apc) {
    return new CacheBin(apc.getArrivalDeparture().getTime(),
            apc.getArrivalDeparture().getRouteId(),
            apc.getArrivalDeparture().getStopId(),
            apc.getArrivalDeparture().getGtfsStopSequence(),
            WINDOW_IN_MINUTES.getValue() * Time.MS_PER_MIN);
  }


  private boolean routeHashContainsKey(Map<String, ApcEvents> routeCache, String routeHash) {
    return routeCache.containsKey(routeHash);
  }

  private String routeHash(ApcReport match) {
    // we deliberately don't consider stop sequence here
    if (match.getArrivalDeparture() == null) return null;
    return match.getArrivalDeparture().getRouteId()
            + ":" + match.getArrivalDeparture().getStopId()
            /*+ ":" + match.getArrivalDeparture().getGtfsStopSequence()*/;
  }

  private void accumulateRouteElement(Map<String, ApcEvents> routeCache, String routeHash, ApcReport match) {
    ApcEvents apcEvents = routeCache.get(routeHash);
    ArrayList copyAdd = new ArrayList(apcEvents.getRecords());
    copyAdd.add(match);
    routeCache.put(routeHash, new ApcEvents(copyAdd));
  }

  private boolean stopHashContainsKey(String hash) {
    synchronized (stopCache) {
      return stopCache.containsKey(hash);
    }
  }

  private void createRouteElement(Map<String, ApcEvents> routeCache, String routeHash, ApcReport match) {
    ArrayList<ApcReport> empty = new ArrayList<>();
    empty.add(match);
    routeCache.put(routeHash, new ApcEvents(empty));
  }


  private void accumulateStopCacheElement(String hash, ApcReport match) {
    synchronized (stopCache) {
      ApcEvents apcEvents = stopCache.get(hash);
      ArrayList copyAdd = new ArrayList(apcEvents.getRecords());
      copyAdd.add(match);
      stopCache.put(hash, new ApcEvents(copyAdd));
    }
  }

  private void createStopCacheElement(String hash, ApcReport match) {
    ArrayList<ApcReport> empty = new ArrayList<>();
    empty.add(match);
    stopCache.put(hash, new ApcEvents(empty));
    cacheSize++;
  }

  private String stopHash(ApcReport match) {
    if (match == null) return null;
    if (match.getArrivalDeparture() == null) return null;
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(tz));
    cal.setTime(new Date(match.getArrivalEpoch()));
    return stopHash(match.getArrivalDeparture().getStopId(), cal.getTime());
  }

  private String stopHash(String stopId, Date arrival) {
    Date binArrival = DateUtils.dateBinning(arrival, Calendar.MINUTE, WINDOW_IN_MINUTES.getValue());
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

  private Double average(ApcCacheElements elements) {
    if (elements == null || elements.getElements().isEmpty()) return null;
    int count = 0;
    double total = 0.0;
    for (ApcCacheElement element : elements.getElements()) {
      total = total + element.getAverage();
      count++;
    }
    return total/count;
  }

  public long cacheSize() {
    return cacheSize;
  }



  public static class CacheBinComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
      CacheBin c1 = (CacheBin) o1;
      CacheBin c2 = (CacheBin) o2;
      int compare = new Long(c2.getTime() - c1.getTime()).intValue();
      if (compare == 0) {
        return new String(c2.getRouteId() + c2.getStopId()).compareTo(
                new String(c1.getRouteId() + c1.getStopId()));
      }
      return compare;
    }
  }
}
