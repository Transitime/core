package org.transitclock.core.dataCache;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.transitclock.ipc.data.IpcArrivalDeparture;
public class TripEvents implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -510989387398784934L;
	
	
	public List <IpcArrivalDeparture> events = new ArrayList<>();

	public List<IpcArrivalDeparture> getEvents() {
		return events;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((events == null) ? 0 : events.hashCode());
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
		TripEvents other = (TripEvents) obj;
		if (events == null) {
			if (other.events != null)
				return false;
		} else if (!events.equals(other.events))
			return false;
		return true;
	}

	public TripEvents() {
		super();		
	}

	/**
	 * make immuntable for Thread safe usage
	 * @param event
	 */
	public TripEvents(IpcArrivalDeparture event) {
		super();
		synchronized (this.events) {
			this.events.add(event);
			Collections.sort(this.events, new IpcArrivalDepartureComparator());
		}
	}

	public TripEvents(List<IpcArrivalDeparture> events) {
		super();
		synchronized (this.events) {
			this.events.addAll(events);
			Collections.sort(this.events, new IpcArrivalDepartureComparator());
		}
	}

	public TripEvents copyAdd(IpcArrivalDeparture additional) {
		List<IpcArrivalDeparture> newEvents = new ArrayList<>(this.events);
		newEvents.add(additional);
		TripEvents newEvent = new TripEvents(newEvents);
		return newEvent;
	}

}
