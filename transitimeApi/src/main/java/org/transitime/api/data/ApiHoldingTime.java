package org.transitime.api.data;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitime.ipc.data.IpcHoldingTime;
@XmlRootElement(name = "holdingtime")
public class ApiHoldingTime {

	@XmlAttribute
	private  Date holdingTime;	

	@XmlAttribute
	private Date creationTime;
	
	@XmlAttribute
	private Date currentTime;
	
	@XmlAttribute
	private String vehicleId;
	
	@XmlAttribute
	private String stopId;
	
	@XmlAttribute
	private String tripId;
	
	@XmlAttribute
	private String routeId;
	
	@XmlAttribute
	private boolean arrivalPredictionUsed;
	
	@XmlAttribute
	private boolean arrivalUsed;
	
	@XmlAttribute
	private Date arrivalTime;

	protected ApiHoldingTime() {
	
	}
	
	public ApiHoldingTime(IpcHoldingTime ipcHoldingTime) throws IllegalAccessException, InvocationTargetException {
		this.holdingTime=ipcHoldingTime.getHoldingTime();
		this.creationTime=ipcHoldingTime.getCreationTime();
		this.vehicleId=ipcHoldingTime.getVehicleId();
		this.tripId=ipcHoldingTime.getTripId();
		this.routeId=ipcHoldingTime.getRouteId();
		this.stopId=ipcHoldingTime.getStopId();
		this.arrivalPredictionUsed = ipcHoldingTime.isArrivalPredictionUsed();
		this.arrivalUsed = ipcHoldingTime.isArrivalUsed();	
		this.currentTime = ipcHoldingTime.getCurrentTime();
		this.arrivalTime = ipcHoldingTime.getArrivalTime();
	}
}
