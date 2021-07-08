package org.transitclock.ipc.data;

import java.io.Serializable;

import org.transitclock.core.dataCache.KalmanErrorCacheKey;
import org.transitclock.core.dataCache.StopPathCacheKey;

public class IpcKalmanErrorCacheKey implements Serializable	
{
	
	private static final long serialVersionUID = -748336688312487489L;
	private String tripId;
	private Integer stopPathIndex;
	
	public IpcKalmanErrorCacheKey(KalmanErrorCacheKey key) {
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
