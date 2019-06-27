package org.transitclock.core.dataCache;
import java.io.Serializable;
import java.util.List;

import org.transitclock.ipc.data.IpcArrivalDeparture;
public class TripEvents implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -510989387398784934L;
	
	
	public List <IpcArrivalDeparture> events;

	public List<IpcArrivalDeparture> getEvents() {
		return events;
	}

	public void setEvents(List<IpcArrivalDeparture> events) {
		this.events = events;
	}

	public TripEvents(List<IpcArrivalDeparture> events) {
		super();
		this.events = events;
	}

}
