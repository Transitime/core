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
	
	private final Date arrivalTime;
	
	private boolean hasD1;
	
	private boolean hasN1;
	
	private boolean hasN2;
	
	
	public boolean isHasD1() {
		return hasD1;
	}
	public void setHasD1(boolean hasD1) {
		this.hasD1 = hasD1;
	}
	public boolean isHasN1() {
		return hasN1;
	}
	public void setHasN1(boolean hasN1) {
		this.hasN1 = hasN1;
	}
	public boolean isHasN2() {
		return hasN2;
	}
	public void setHasN2(boolean hasN2) {
		this.hasN2 = hasN2;
	}
	public IpcHoldingTime(Date holdingTime, Date creationTime, String vehicleId, String stopId, String tripId,
			String routeId, boolean arrivalPredictionUsed , boolean arrivalUsed, Date arrivalTime, boolean hasN1, boolean hasN2, boolean hasD1) {
		super();
		this.holdingTime = holdingTime;
		this.creationTime = creationTime;
		this.vehicleId = vehicleId;
		this.stopId = stopId;
		this.tripId = tripId;
		this.routeId = routeId;
		this.currentTime = Calendar.getInstance().getTime();
		this.arrivalTime=arrivalTime;
		this.hasD1=hasD1;
		this.hasN1=hasN1;
		this.hasN2=hasN2;
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
		this.arrivalTime=holdingTime.getArrivalTime();
		this.hasD1=holdingTime.isHasD1();
		this.hasN1=holdingTime.isHasN1();
		this.hasN2=holdingTime.isHasN2();
	
	}

	public Date getArrivalTime() {
		return arrivalTime;
	}
	public void setArrivalPredictionUsed(boolean arrivalPredictionUsed) {
		this.arrivalPredictionUsed = arrivalPredictionUsed;
	}
	public void setArrivalUsed(boolean arrivalUsed) {
		this.arrivalUsed = arrivalUsed;
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
