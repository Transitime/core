package org.transitime.ipc.data;

import java.io.Serializable;

import org.transitime.core.dataCache.TripStopPathCacheKey;

public class IpcHistoricalAverageCacheKey implements Serializable	
{
	private static final long serialVersionUID = 8712981814295653998L;
	
	
	private String tripId;
	private Integer stopPathIndex;
	
	public IpcHistoricalAverageCacheKey(TripStopPathCacheKey key) {
		super();		
		this.tripId=key.getTripId();
		this.stopPathIndex=key.getStopPathIndex();	
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
}
