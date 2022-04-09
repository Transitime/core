package org.transitclock.ipc.data;

import java.io.Serializable;

import org.transitclock.core.dataCache.KalmanErrorCacheKey;
import org.transitclock.core.dataCache.StopPathCacheKey;

public class IpcKalmanErrorCacheKey implements Serializable	
{
	
	private static final long serialVersionUID = -748336688312487490L;

	private String routeId;
	private String directionId;
	private Integer startTimeSecondsIntoDay;
	private String originStopId;
	private String destinationStopId;


	public IpcKalmanErrorCacheKey(KalmanErrorCacheKey key) {
		super();
		this.routeId = key.getRouteId();
		this.directionId = key.getDirectionId();
		this.startTimeSecondsIntoDay = key.getStartTimeSecondsIntoDay();
		this.originStopId = key.getOriginStopId();
		this.destinationStopId = key.getDestinationStopId();
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	public String getDirectionId() {
		return directionId;
	}

	public void setDirectionId(String directionId) {
		this.directionId = directionId;
	}

	public Integer getStartTimeSecondsIntoDay() {
		return startTimeSecondsIntoDay;
	}

	public void setStartTimeSecondsIntoDay(Integer startTimeSecondsIntoDay) {
		this.startTimeSecondsIntoDay = startTimeSecondsIntoDay;
	}

	public String getOriginStopId() {
		return originStopId;
	}

	public void setOriginStopId(String originStopId) {
		this.originStopId = originStopId;
	}

	public String getDestinationStopId() {
		return destinationStopId;
	}

	public void setDestinationStopId(String destinationStopId) {
		this.destinationStopId = destinationStopId;
	}
}
