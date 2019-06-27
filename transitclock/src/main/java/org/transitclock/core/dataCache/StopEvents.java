package org.transitclock.core.dataCache;
import java.io.Serializable;
import java.util.List;

import org.transitclock.ipc.data.IpcArrivalDeparture;
public class StopEvents implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7968075904267156083L;
	public List <IpcArrivalDeparture> events;

	public List<IpcArrivalDeparture> getEvents() {
		return events;
	}

	public void setEvents(List<IpcArrivalDeparture> events) {
		this.events = events;
	}

	public StopEvents(List<IpcArrivalDeparture> events) {
		super();
		this.events = events;
	}

	
}
