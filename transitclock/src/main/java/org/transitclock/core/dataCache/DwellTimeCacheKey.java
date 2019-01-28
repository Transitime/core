package org.transitclock.core.dataCache;

import java.io.Serializable;

import org.transitclock.core.Indices;

public class DwellTimeCacheKey implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4967988031829738238L;
	private String tripId;
	private Integer stopPathIndex;
	public DwellTimeCacheKey(String tripId, Integer stopPathIndex) {	
		this.tripId = tripId;
		this.stopPathIndex = stopPathIndex;
	}
	
	public DwellTimeCacheKey(Indices indices) {
	
		this.tripId = indices.getTrip().getId();
		this.stopPathIndex = indices.getStopPathIndex();
	}
	public String getTripId() {
		return tripId;
	}
	public void setTripId(String tripId) {
		this.tripId = tripId;
	}
	public Integer getStopPathIndex() {
		return stopPathIndex;
	}
	public void setStopPathIndex(Integer stopPathIndex) {
		this.stopPathIndex = stopPathIndex;
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
	public String toString() {
		return "DwellTimeCacheKey [tripId=" + tripId + ", stopPathIndex=" + stopPathIndex + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DwellTimeCacheKey other = (DwellTimeCacheKey) obj;
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

	
}
