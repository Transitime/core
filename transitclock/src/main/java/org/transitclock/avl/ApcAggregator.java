package org.transitclock.avl;

import org.transitclock.db.structs.ApcArrivalRate;
import org.transitclock.db.structs.ApcReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Group APC data to generate APCArrivalRate
 */
public class ApcAggregator {

  private static final ApcAggregator singleton = new ApcAggregator();
  private HashMap<RateKey, List<ApcReport>> cache = new HashMap<RateKey, List<ApcReport>>();

  public static ApcAggregator getInstance() {
    return singleton;
  }

  private ApcAggregator() {
  }

  public synchronized List<ApcArrivalRate> analyze(List<ApcReport> matches ) {
    cache.clear();
    if (matches == null) return new ArrayList<>();
    for (ApcReport match : matches) {
      RateKey hash = hash(match);
      if (hash == null) continue;
      if (containsKey(hash)) {
        accumulate(hash, match);
      } else {
        create(hash, match);
      }
    }
    return archiveAndPrune();
  }

  private boolean containsKey(RateKey hash) {
    return cache.containsKey(hash);
  }

  private List<ApcArrivalRate> archiveAndPrune() {
    return null;
  }

  private void accumulate(RateKey hash, ApcReport match) {
    cache.get(hash).add(match);
  }

  private void create(RateKey hash, ApcReport match) {
    ArrayList<ApcReport> empty = new ArrayList<>();
    empty.add(match);
    cache.put(hash, empty);
  }

  private RateKey hash(ApcReport match) {
    if (match == null) return null;
    if (match.getArrivalDeparture() == null) return null;
    return new RateKey(match.getTime(), match.getArrivalDeparture().getStopId());
  }

  public static class RateKey {
    public RateKey(long time, String stopId) {

    }
  }
}
