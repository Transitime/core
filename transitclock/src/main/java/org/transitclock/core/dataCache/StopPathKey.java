package org.transitclock.core.dataCache;

import java.util.Objects;

/**
 * StopPatch caching based in TripInstance (Route/Direction/time) instead of tripId.
 */
public class StopPathKey extends TripKey implements java.io.Serializable {

  private static long serialVersionUID = 1L;

  private String originStopId;
  private String destinationStopId;
  private boolean travelTime;

  public StopPathKey(String routeId,
                     String directionId,
                     Integer startTimeSecondsIntoDay,
                     Long tripStartTime,
                     String originStopId,
                     String destinationStopId,
                     boolean travelTime) {
    super(routeId, directionId, tripStartTime, startTimeSecondsIntoDay);
    this.originStopId = originStopId;
    this.destinationStopId = destinationStopId;
    this.travelTime = travelTime;
  }

  public String getOriginStopId() {
    return originStopId;
  }

  public String getDestinationStopId() {
    return destinationStopId;
  }

  public boolean isTravelTime() {
    return travelTime;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getRouteId(), getDirectionId(), getStartTimeSecondsIntoDay(),
            getTripStartTime(), originStopId, destinationStopId, travelTime);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return true;
    StopPathKey that = (StopPathKey) o;
    return Objects.equals(getRouteId(), that.getRouteId())
            && Objects.equals(getDirectionId(), that.getDirectionId())
            && Objects.equals(getStartTimeSecondsIntoDay(), that.getStartTimeSecondsIntoDay())
            && Objects.equals(getTripStartTime(), that.getTripStartTime())
            && Objects.equals(originStopId, that.originStopId)
            && Objects.equals(destinationStopId, that.destinationStopId)
            && Objects.equals(travelTime, that.travelTime);
  }
}
