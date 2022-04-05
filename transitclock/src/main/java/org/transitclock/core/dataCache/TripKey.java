package org.transitclock.core.dataCache;

import java.util.Date;
import java.util.Objects;

/**
 * index based on the instance of a trip via route, direction, and startTime
 */
public class TripKey implements java.io.Serializable {

  private static final long serialVersionUID = 1;
  private String routeId;
  private String directionId;
  private Long tripStartTime;
  private Integer startTimeSecondsIntoDay;

  public TripKey(String routeId,
                 String directionId,
                 Long tripStartTime,
                 Integer startTimeSecondsIntoDay) {
    this.routeId = routeId;
    this.directionId = directionId;
    this.tripStartTime = tripStartTime;
    this.startTimeSecondsIntoDay = startTimeSecondsIntoDay;
  }

  public String getRouteId() {
    return routeId;
  }

  public String getDirectionId() {
    return directionId;
  }

  public Integer getStartTimeSecondsIntoDay() {
    return startTimeSecondsIntoDay;
  }

  public Long getTripStartTime() {
    return tripStartTime;
  }

  @Override
  public int hashCode() {
    return Objects.hash(routeId, directionId, startTimeSecondsIntoDay, tripStartTime);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return true;
    TripKey that = (TripKey) o;
    return Objects.equals(routeId, that.routeId)
            && Objects.equals(directionId, that.directionId)
            && Objects.equals(startTimeSecondsIntoDay, that.startTimeSecondsIntoDay)
            && Objects.equals(tripStartTime, that.tripStartTime);
  }

  @Override
  public String toString() {
    return "TripKey [routeId=" + routeId + ", directionId=" + directionId + ", startTimeSecondsIntoDay=" + startTimeSecondsIntoDay
            + ", tripStartTime=" + new Date(tripStartTime) + "]";
  }

}
