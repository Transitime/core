package org.transitime.api.data;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitime.ipc.data.IpcArrivalDeparture;

@XmlRootElement(name = "arrivaldeparture")
public class ApiArrivalDeparture {
	@XmlAttribute
	private String stopId;
	@XmlAttribute
	private String vehicleId;
	@XmlAttribute
	private Date time;		
	@XmlAttribute
	private boolean isArrival;
	@XmlAttribute
	private String tripId;			
	@XmlAttribute
	private String routeId;
	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiArrivalDeparture() {
	}
	
	public ApiArrivalDeparture(IpcArrivalDeparture ipcArrivalDeparture) throws IllegalAccessException, InvocationTargetException {
		this.vehicleId=ipcArrivalDeparture.getVehicleId();
		this.time=ipcArrivalDeparture.getTime();
		this.routeId=ipcArrivalDeparture.getRouteId();
		this.tripId=ipcArrivalDeparture.getTripId();
		this.isArrival=ipcArrivalDeparture.isArrival();
	}

}
