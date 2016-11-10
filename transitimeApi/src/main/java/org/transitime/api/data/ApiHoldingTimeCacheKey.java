package org.transitime.api.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitime.ipc.data.IpcHoldingTimeCacheKey;

@XmlRootElement(name = "HoldingTimeCacheKey")
public class ApiHoldingTimeCacheKey {

	
	@XmlAttribute
	private String stopid;
	
	@XmlAttribute
	private String vehicleId;
	
	@XmlAttribute
	private String tripId;
	
	public ApiHoldingTimeCacheKey(IpcHoldingTimeCacheKey key) {

		this.stopid=key.getStopid();
		this.vehicleId=key.getVehicleId();
		this.tripId=key.getTripId();
	}

	protected ApiHoldingTimeCacheKey() {	
	}

}
