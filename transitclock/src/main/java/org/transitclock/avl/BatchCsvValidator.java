package org.transitclock.avl;

import org.hibernate.Session;
import org.transitclock.db.structs.Stop;

import java.util.HashSet;
import java.util.Set;

/**
 * Arrival Departrue CSV loader has constraints in database.
 * Pre-validate those constratins to prevent database issues
 * during batch loading of data.
 */
public class BatchCsvValidator {

  private Set<String> stopIds = null;
  private Session session;
  public BatchCsvValidator(Session session) {
    this.session = session;
  }

  public boolean validateStopId(String stopId, int configRev) {
    if (stopIds == null) {
      stopIds = new HashSet<>();
      for (Stop stop : Stop.getStops(session, configRev)) {
        stopIds.add(stop.getId());
      }
    }
    return stopIds.contains(stopId);
  }
}
