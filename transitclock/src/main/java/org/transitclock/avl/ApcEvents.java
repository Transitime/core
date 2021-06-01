package org.transitclock.avl;

import org.transitclock.db.structs.ApcReport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Cache value for ApcCache.
 */
public class ApcEvents implements Serializable {
  private ArrayList<ApcReport> records;
  private List<ApcReport> sortedRecords = null;

  public ApcEvents(ArrayList<ApcReport> records) {
    this.records = records;
  }

  public ArrayList<ApcReport> getRecords() {
    return records;
  }

  public List<ApcReport> getSortedRecords() {
    if (sortedRecords == null) {
      sortedRecords = new ArrayList<>(records);
      Collections.sort(sortedRecords, new ApcEventComparator());
    }
    return sortedRecords;
  }

  public static class ApcEventComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
      ApcReport a1 = (ApcReport) o1;
      ApcReport a2 = (ApcReport) o2;
      int compare = new Long(a2.getArrivalDeparture().getTime()).compareTo(new Long(a1.getArrivalDeparture().getTime()));
      if (compare == 0) {
        // if equal, make sort stable by comparing vehicles
        return a1.getVehicleId().compareTo(a2.getVehicleId());
      }
      return compare;
    }
  }
}
