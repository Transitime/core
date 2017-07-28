package org.transitime.core;

import org.transitime.db.structs.ArrivalDeparture;

public class DwellTimeDetails {
	private ArrivalDeparture departure;
	private ArrivalDeparture arrival;
	public ArrivalDeparture getDeparture() {
		return departure;
	}	
	public ArrivalDeparture getArrival() {
		return arrival;
	}
	public DwellTimeDetails(ArrivalDeparture arrival, ArrivalDeparture departure) {
		super();
		this.arrival = arrival;
		this.departure = departure;
	}
	public long getDwellTime()
	{
		if(this.arrival!=null && this.departure!=null)
		{
			return this.departure.getTime()-this.arrival.getTime();
		}else
		{
			return -1;
		}
	}
	@Override
	public String toString() {
		return "DwellTimeDetails [departure=" + departure + ", arrival=" + arrival + ", getDwellTime()="
				+ getDwellTime() + "]";
	}
}
