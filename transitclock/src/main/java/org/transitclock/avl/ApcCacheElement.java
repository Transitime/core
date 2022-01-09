package org.transitclock.avl;

import org.transitclock.db.structs.ApcReport;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Moving average of APC observations.
 */
public class ApcCacheElement implements Serializable {

  private double movingAverage;
  private List<ApcReport> binElements;
  public ApcCacheElement(double movingAverage, List<ApcReport> binElements) {
    this.movingAverage = movingAverage;
    this.binElements = binElements;
  }

  public double getAverage() {
    return movingAverage;
  }

  public Collection<? extends ApcReport> getRecords() {
    return binElements;
  }
}

