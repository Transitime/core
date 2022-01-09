package org.transitclock.avl;

import org.transitclock.utils.DateUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * APC cache key.
 */
public class CacheBin implements Serializable {
  private long time;
  private String stopId;
  private String routeId;
  private int binWidth;

  public CacheBin(long time, String routeId, String stopId, int sequence, int binWidth) {
    this.time = DateUtils.dateBinning(time, binWidth);
    this.stopId = stopId;
    this.routeId = routeId;
    // we don't consider sequence deliberately
    this.binWidth = binWidth;
  }

  public long getTime() {
    return time;
  }

  public String getRouteId() {
    return routeId;
  }

  public String getStopId() {
    return stopId;
  }

  @Override
  public String toString() {
    return new Date(time).toString() + ":" + routeId + ":" + stopId;
  }

  public boolean fitsBin(long comparisonTime) {
    return this.time == DateUtils.dateBinning(comparisonTime, binWidth);
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return true;
    CacheBin that = (CacheBin) o;
    return Objects.equals(time, that.time)
            && Objects.equals(routeId, that.routeId)
            && Objects.equals(stopId, that.stopId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(time, routeId, stopId);
  }
}
