package org.transitime.core.dataCache;

import org.transitime.core.Indices;
/**
 * @author Sean Og Crudden
 * 
 */
public class StopPathCacheKey implements java.io.Serializable {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 9119654046491298858L;
	private String tripId;
	private Integer stopPathIndex;
	
	private boolean travelTime=true;
	
	public boolean isTravelTime() {
		return travelTime;
	}

	public StopPathCacheKey(String tripId, Integer stopPathIndex)
	{
		super();
		
		this.tripId = tripId;
		this.stopPathIndex = stopPathIndex;	
		this.travelTime=true;
	}
	
	public StopPathCacheKey(String tripId, Integer stopPathIndex, boolean travelTime) {
		super();
		
		this.tripId = tripId;
		this.stopPathIndex = stopPathIndex;	
		this.travelTime=travelTime;
	}
	public StopPathCacheKey(Indices indices) {
		super();
				
		this.stopPathIndex=indices.getStopPathIndex();
		
		int tripIndex = indices.getTripIndex();
		
		this.tripId=indices.getBlock().getTrip(tripIndex).getId();
		
		this.travelTime=true;
		
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
		result = prime * result + (travelTime ? 1231 : 1237);
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
		StopPathCacheKey other = (StopPathCacheKey) obj;
		if (stopPathIndex == null) {
			if (other.stopPathIndex != null)
				return false;
		} else if (!stopPathIndex.equals(other.stopPathIndex))
			return false;
		if (travelTime != other.travelTime)
			return false;
		if (tripId == null) {
			if (other.tripId != null)
				return false;
		} else if (!tripId.equals(other.tripId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StopPathCacheKey [tripId=" + tripId + ", stopPathIndex=" + stopPathIndex + ", travelTime=" + travelTime
				+ "]";
	}


	
						
}




