package org.transitclock.core.dataCache;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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
		Collections.sort(this.events, new IpcArrivalDepartureComparator());
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
		StopEvents other = (StopEvents) obj;
		if (events == null) {
			if (other.events != null)
				return false;
		} else if (!events.equals(other.events))
			return false;
		return true;
	}

	public StopEvents() {
		super();		
	}

	public StopEvents(List<IpcArrivalDeparture> events) {
		super();
		this.events = events;
		
		Collections.sort(this.events, new IpcArrivalDepartureComparator());
		
	}
	
	public void addEvent(IpcArrivalDeparture event)
	{
		if(this.events==null)
		{
			events=new ArrayList<IpcArrivalDeparture>();
		}
		events.add(event);
		Collections.sort(this.events, new IpcArrivalDepartureComparator());		
	}
	
}
