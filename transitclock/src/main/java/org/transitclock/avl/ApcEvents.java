package org.transitclock.avl;

import org.transitclock.db.structs.ApcReport;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Cache value for ApcCache.
 */
public class ApcEvents implements Serializable {
  private ArrayList<ApcReport> records;

  public ApcEvents(ArrayList<ApcReport> records) {
    this.records = records;
  }

  public ArrayList<ApcReport> getRecords() {
    return records;
  }
}
