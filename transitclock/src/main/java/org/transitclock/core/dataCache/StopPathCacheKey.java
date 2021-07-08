package org.transitclock.core.dataCache;

import java.util.Date;

import org.transitclock.core.Indices;
/**
 * @author Sean Óg Crudden
 * 
 */
public class StopPathCacheKey implements java.io.Serializable {
		
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9119654046491298858L;
	private String tripId;
	private Integer stopPathIndex;
	
	/* this is only set for frequency based trips otherwise null. This is seconds from midnight */
	private Long startTime = null  ;
	
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
		this.startTime=null;
	}
	
	public StopPathCacheKey(String tripId, Integer stopPathIndex, boolean travelTime) {
		super();
		
		this.tripId = tripId;
		this.stopPathIndex = stopPathIndex;	
		this.travelTime=travelTime;
		this.startTime = null;
	}
	public StopPathCacheKey(String tripId, Integer stopPathIndex, boolean travelTime, Long startTime) {
		super();
		
		this.tripId = tripId;
		this.stopPathIndex = stopPathIndex;	
		this.travelTime=travelTime;
		this.startTime = startTime;
	}
	
	public StopPathCacheKey(StopPathCacheKey key) {
		
		this.stopPathIndex=new Integer(key.getStopPathIndex());
				
		this.tripId=new String(key.getTripId());
		
		this.travelTime=key.travelTime;	
						
		this.startTime=new Long(key.getStartTime());
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

	public Long getStartTime() {
		return startTime;
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
	public String toString() {
		return "StopPathCacheKey [tripId=" + tripId + ", stopPathIndex=" + stopPathIndex + ", startTime=" + startTime
				+ ", travelTime=" + travelTime + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
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
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
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


						
}




