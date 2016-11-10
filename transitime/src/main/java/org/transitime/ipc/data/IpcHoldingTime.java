package org.transitime.ipc.data;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import org.transitime.db.structs.HoldingTime;

public class IpcHoldingTime implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2869001113239169554L;

	private final Date holdingTime;	

	private final Date creationTime;
	
	private final Date currentTime;
	
	private final String vehicleId;
	
	private final String stopId;
		
	private final String tripId;
	
	private final String routeId;
			
	private boolean arrivalPredictionUsed;
	
	private boolean arrivalUsed;
	
	
	public IpcHoldingTime(Date holdingTime, Date creationTime, String vehicleId, String stopId, String tripId,
			String routeId, boolean arrivalPredictionUsed , boolean arrivalUsed) {
		super();
		this.holdingTime = holdingTime;
		this.creationTime = creationTime;
		this.vehicleId = vehicleId;
		this.stopId = stopId;
		this.tripId = tripId;
		this.routeId = routeId;
		this.currentTime = Calendar.getInstance().getTime();
	}
	public IpcHoldingTime(HoldingTime holdingTime) {
		
		this.holdingTime = holdingTime.getHoldingTime();
		this.creationTime = holdingTime.getCreationTime();
		this.vehicleId = holdingTime.getVehicleId();
		this.stopId = holdingTime.getStopId();
		this.tripId = holdingTime.getTripId();
		this.routeId = holdingTime.getRouteId();
		this.arrivalPredictionUsed = holdingTime.isArrivalPredictionUsed();
		this.arrivalUsed =  holdingTime.isArrivalUsed();
		this.currentTime = Calendar.getInstance().getTime();
	}

	public Date getCurrentTime() {
		return currentTime;
	}
	public Date getHoldingTime() {
		return holdingTime;
	}


	public Date getCreationTime() {
		return creationTime;
	}


	public String getVehicleId() {
		return vehicleId;
	}


	public String getStopId() {
		return stopId;
	}


	public String getTripId() {
		return tripId;
	}


	public String getRouteId() {
		return routeId;
	}

	public boolean isArrivalPredictionUsed() {
		return arrivalPredictionUsed;
	}
	
	public boolean isArrivalUsed() {
		return arrivalUsed;
	}
	
}
