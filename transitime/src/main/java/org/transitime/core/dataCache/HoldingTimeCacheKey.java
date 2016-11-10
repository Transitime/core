package org.transitime.core.dataCache;

import java.util.Date;

import org.transitime.db.structs.HoldingTime;
/**
 * @author Sean Óg Crudden
 * 
 */
public class HoldingTimeCacheKey implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2621110737961919479L;

	public HoldingTimeCacheKey(String stopid, String vehicleId, String tripId) {
		super();
		this.stopid = stopid;
		this.vehicleId = vehicleId;	
		this.tripId = tripId;
	
	}
	public HoldingTimeCacheKey(HoldingTime holdTime)
	{
		this.stopid=holdTime.getStopId();
		this.vehicleId=holdTime.getVehicleId();
		this.tripId=holdTime.getTripId();
	}
	@Override
	public String toString() {
		return "HoldingTimeCacheKey [stopid=" + stopid + ", vehicleId=" + vehicleId + ", tripId=" + tripId + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stopid == null) ? 0 : stopid.hashCode());
		result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
		result = prime * result + ((vehicleId == null) ? 0 : vehicleId.hashCode());
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
		HoldingTimeCacheKey other = (HoldingTimeCacheKey) obj;
		if (stopid == null) {
			if (other.stopid != null)
				return false;
		} else if (!stopid.equals(other.stopid))
			return false;
		if (tripId == null) {
			if (other.tripId != null)
				return false;
		} else if (!tripId.equals(other.tripId))
			return false;
		if (vehicleId == null) {
			if (other.vehicleId != null)
				return false;
		} else if (!vehicleId.equals(other.vehicleId))
			return false;
		return true;
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
	
	private String stopid;
	private String vehicleId;
	private String tripId;

	
	public String getTripId() {
		return tripId;
	}
	public void setTripId(String tripId) {
		this.tripId = tripId;
	}

}
