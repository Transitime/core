package org.transitime.ipc.data;

import java.io.Serializable;

import org.transitime.core.dataCache.HoldingTimeCacheKey;

public class IpcHoldingTimeCacheKey  implements Serializable	{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2625236742413645110L;
	private String stopid;
	private String vehicleId;
	private String tripId;
	public IpcHoldingTimeCacheKey(HoldingTimeCacheKey holdingTimeCacheKey) {
		this.stopid=holdingTimeCacheKey.getStopid();
		this.vehicleId=holdingTimeCacheKey.getVehicleId();
		this.setTripId(holdingTimeCacheKey.getTripId());
	}
	public String getStopid() {
		return stopid;
	}
	public void setStopid(String stopid) {
		this.stopid = stopid;
	}
	public String getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(String vehicleId) {
		this.vehicleId = vehicleId;
	}
	public String getTripId() {
		return tripId;
	}
	public void setTripId(String tripId) {
		this.tripId = tripId;
	}
}
