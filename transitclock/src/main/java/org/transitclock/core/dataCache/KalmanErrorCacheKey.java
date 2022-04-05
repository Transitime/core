package org.transitclock.core.dataCache;

import org.transitclock.core.Indices;
import org.transitclock.db.structs.Trip;

/**
 * @author Sean Og Crudden
 * TODO This is the same as StopPathCacheKey but left seperate in case we might use block_id as well.
 */
public class KalmanErrorCacheKey implements java.io.Serializable {
	

	private String routeId;
	private String directionId;
	private Integer startTimeSecondsIntoDay;
	private String originStopId;
	private String destinationStopId;
	
	// The vehicleId is only used for debug purposed we know in log which vehicle set the error value
	private String vehicleId;

	public String getRouteId() {
		return routeId;
	}

	public String getDirectionId() {
		return directionId;
	}

	public Integer getStartTimeSecondsIntoDay() {
		return startTimeSecondsIntoDay;
	}

	public String getOriginStopId() {
		return originStopId;
	}

	public String getDestinationStopId() {
		return destinationStopId;
	}

	public String getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(String vehicleId) {
		this.vehicleId = vehicleId;
	}

	/**
	 * Needs to be serializable to add to cache
	 */
	private static final long serialVersionUID = 5029823633051153717L;
	

	public KalmanErrorCacheKey(Indices indices, String vehicleId) {
		super();

		Trip trip = indices.getBlock().getTrip(indices.getTripIndex());
		this.routeId = trip.getRouteId();
		this.directionId = trip.getDirectionId();
		this.startTimeSecondsIntoDay = trip.getStartTime();
		this.originStopId = trip.getTripPattern().getStopIds().get(0);
		this.destinationStopId = trip.getLastStopId();
		this.vehicleId =vehicleId;
		
	}
	public KalmanErrorCacheKey(Indices indices) {
		super();

		Trip trip = indices.getBlock().getTrip(indices.getTripIndex());
		this.routeId = trip.getRouteId();
		this.directionId = trip.getDirectionId();
		this.startTimeSecondsIntoDay = trip.getStartTime();
		this.originStopId = trip.getTripPattern().getStopIds().get(0);
		this.destinationStopId = trip.getLastStopId();

	}
	@Override
	public String toString() {
		return "KalmanErrorCacheKey [routeId=" + routeId
						+ ", directionId=" + directionId
						+ ", startTime=" + startTimeSecondsIntoDay
						+ ", originStopId=" + originStopId
						+ ", destinationStopId=" + destinationStopId
						+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
		result = prime * result + ((directionId == null) ? 0 : directionId.hashCode());
		result = prime * result + ((originStopId == null) ? 0 : originStopId.hashCode());
		result = prime * result + ((destinationStopId == null) ? 0 : destinationStopId.hashCode());
		result = prime * result + ((startTimeSecondsIntoDay == null) ? 0 : startTimeSecondsIntoDay.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KalmanErrorCacheKey other = (KalmanErrorCacheKey) obj;
		if (routeId == null) {
			if (other.routeId != null)
				return false;
		} else if (!routeId.equals(other.routeId))
			return false;
		if (directionId == null) {
			if (other.directionId != null)
				return false;
		} else if (!directionId.equals(other.directionId))
			return false;
		if (startTimeSecondsIntoDay == null) {
			if (other.startTimeSecondsIntoDay != null)
				return false;
		} else if (!startTimeSecondsIntoDay.equals(other.startTimeSecondsIntoDay))
			return false;
		if (originStopId == null) {
			if (other.originStopId != null)
				return false;
		} else if (!originStopId.equals(other.originStopId))
			return false;
		if (destinationStopId == null) {
			if (other.destinationStopId != null)
				return false;
		} else if (!destinationStopId.equals(other.destinationStopId))
			return false;

		return true;
	}

	public KalmanErrorCacheKey(String routeId, String directionId,
														 Integer startTimeSecondsIntoDay, String originStopId,
														 String destinationStopId) {
		super();
		
		this.routeId = routeId;
		this.directionId = directionId;
		this.startTimeSecondsIntoDay = startTimeSecondsIntoDay;
		this.originStopId = originStopId;
		this.destinationStopId = destinationStopId;
	}

	
}




