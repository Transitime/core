package org.transitclock.avl;

import org.transitclock.db.structs.ArrivalDeparture;


/**
 * Represent the acceptable temporal range an ArrivalDeparture can match
 * to an APC ping.
 */
public class ArrivalDepartureInterval implements Comparable {
  private long start;
  private long end;
  private boolean isArrival;
  private ArrivalDeparture arrivalDeparture;

  public ArrivalDepartureInterval(ArrivalDeparture ad, long windowInMillis, boolean isArrival) {
    this.arrivalDeparture = ad;
    this.start = ad.getTime() - windowInMillis;
    this.end = ad.getTime() + windowInMillis;
    this.isArrival = isArrival;
  }

  public ArrivalDeparture getArrivalDeparture() {
    return arrivalDeparture;
  }

  public long getStart() {
    return start;
  }

  public long getEnd() {
    return end;
  }

  @Override
  public int compareTo(Object o) {
    ArrivalDepartureInterval adi = (ArrivalDepartureInterval) o;
    return new Long(start).compareTo(adi.start);
  }

  public boolean getArrival() {
    return isArrival;
  }
}

