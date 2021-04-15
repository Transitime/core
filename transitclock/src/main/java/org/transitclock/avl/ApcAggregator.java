package org.transitclock.avl;

import org.transitclock.db.structs.ApcArrivalRate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Group APC data to generate APCArrivalRate
 */
public class ApcAggregator {

  private static final ApcAggregator singleton = new ApcAggregator();
  private HashMap<RateKey, List<ApcMatch>> cache = new HashMap<RateKey, List<ApcMatch>>();

  public static ApcAggregator getInstance() {
    return singleton;
  }

  private ApcAggregator() {
  }

  public synchronized List<ApcArrivalRate> analyze(List<ApcMatch> matches ) {
    if (matches == null) return new ArrayList<>();
    for (ApcMatch match : matches) {
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

  private void accumulate(RateKey hash, ApcMatch match) {
    cache.get(hash).add(match);
  }

  private void create(RateKey hash, ApcMatch match) {
    ArrayList<ApcMatch> empty = new ArrayList<>();
    empty.add(match);
    cache.put(hash, empty);
  }

  private RateKey hash(ApcMatch match) {
    if (match.getArrivalDeparture() == null) return null;
    return new RateKey(match.getApc().getTime(), match.getArrivalDeparture().getStopId());
  }

  public static class RateKey {
    public RateKey(long time, String stopId) {

    }
  }
}
