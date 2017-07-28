package org.transitime.core;

import org.transitime.db.structs.ArrivalDeparture;

public class TravelTimeDetails {
	private ArrivalDeparture departure;
	private ArrivalDeparture arrival;
	public ArrivalDeparture getDeparture() {
		return departure;
	}	
	public ArrivalDeparture getArrival() {
		return arrival;
	}
	
	public long getTravelTime()
	{
		if(this.arrival!=null && this.departure!=null)
		{
			return this.arrival.getTime()-this.getDeparture().getTime();
		}else
		{
			return -1;
		}
			
	}
	public TravelTimeDetails(ArrivalDeparture departure, ArrivalDeparture arrival) {
		super();
		this.departure = departure;
		this.arrival = arrival;
	}
	@Override
	public String toString() {
		return "TravelTimeDetails [departure=" + departure + ", arrival=" + arrival + ", getTravelTime()="
				+ getTravelTime() + "]";
	}

}
