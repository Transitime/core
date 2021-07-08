package org.transitclock.core.dataCache;

import org.transitclock.core.Indices;

/**
 * @author Sean Og Crudden
 * TODO This is the same as StopPathCacheKey but left seperate in case we might use block_id as well.
 */
public class KalmanErrorCacheKey implements java.io.Serializable {
	
	
	public String getTripId() {
		return tripId;
	}

	public void setTripId(String tripId) {
		this.tripId = tripId;
	}

	public void setStopPathIndex(Integer stopPathIndex) {
		this.stopPathIndex = stopPathIndex;
	}

	private String tripId;
	private Integer stopPathIndex;
	
	// The vehicleId is only used for debug purposed we know in log which vehicle set the error value
	private String vehiceId;
	
		
	public String getVehiceId() {
		return vehiceId;
	}

	public void setVehiceId(String vehiceId) {
		this.vehiceId = vehiceId;
	}

	/**
	 * Needs to be serializable to add to cache
	 */
	private static final long serialVersionUID = 5029823633051153716L;
	

	public KalmanErrorCacheKey(Indices indices, String vehicleId) {
		super();
		
		this.tripId=indices.getBlock().getTrip(indices.getTripIndex()).getId();
		this.stopPathIndex=indices.getStopPathIndex();		
		this.vehiceId=vehicleId;
		
	}
	public KalmanErrorCacheKey(Indices indices) {
		super();
		
		this.tripId=indices.getBlock().getTrip(indices.getTripIndex()).getId();
		this.stopPathIndex=indices.getStopPathIndex();		
		
		
	}
	@Override
	public String toString() {
		return "KalmanErrorCacheKey [tripId=" + tripId + ", stopPathIndex=" + stopPathIndex + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stopPathIndex == null) ? 0 : stopPathIndex.hashCode());
		result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
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
		if (stopPathIndex == null) {
			if (other.stopPathIndex != null)
				return false;
		} else if (!stopPathIndex.equals(other.stopPathIndex))
			return false;
		if (tripId == null) {
			if (other.tripId != null)
				return false;
		} else if (!tripId.equals(other.tripId))
			return false;
		return true;
	}

	public KalmanErrorCacheKey(String tripId, Integer stopPathIndex) {
		super();
		
		this.tripId = tripId;
		this.stopPathIndex = stopPathIndex;
	}

	
	/**
	 * @return the stopPathIndex
	 */
	public int getStopPathIndex() {
		return stopPathIndex;
	}

	/**
	 * @param stopPathIndex the stopPathIndex to set
	 */
	public void setStopPathIndex(int stopPathIndex) {
		this.stopPathIndex = stopPathIndex;
	}
	
}




