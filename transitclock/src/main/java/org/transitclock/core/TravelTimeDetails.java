package org.transitclock.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.utils.Time;

public class TravelTimeDetails {
	private ArrivalDeparture departure;
	private ArrivalDeparture arrival;
	
	private static final IntegerConfigValue maxTravelTime = 
			new IntegerConfigValue(
					"transitclock.core.maxTravelTime",
					30 * Time.MS_PER_MIN,
					"This is a maximum allowed for travel between two stops. Used as a sanity check for cache and predictions.");
	
	private static final Logger logger = LoggerFactory
			.getLogger(TravelTimeDetails.class);
	
	public ArrivalDeparture getDeparture() {
		return departure;
	}	
	public ArrivalDeparture getArrival() {
		return arrival;
	}
	
	public long getTravelTime()
	{
		if(this.arrival!=null && this.departure!=null && arrival.isArrival() && departure.isDeparture())
		{						
			if(sanityCheck())
			{
				long travelTime=this.arrival.getTime()-this.getDeparture().getTime();
				return travelTime;
			}else
			{
				logger.warn("Outside bounds : {} ", this);
			}
		}	
		return -1;				
	}
	public TravelTimeDetails(ArrivalDeparture departure, ArrivalDeparture arrival) {
		super();
		this.departure = departure;
		this.arrival = arrival;
	}
	
	public boolean sanityCheck()
	{
		if(this.arrival!=null && this.departure!=null && arrival.isArrival() && departure.isDeparture())
		{
			long travelTime=this.arrival.getTime()-this.getDeparture().getTime();
		
			if(travelTime<=0||travelTime>maxTravelTime.getValue())
			{
				return false;
			}else
			{
				return true;
			}
		}else
		{
			return false;
		}
	}
	@Override
	public String toString() {
		return "TravelTimeDetails [departure=" + departure + ", arrival=" + arrival + ", getTravelTime()="
				+ getTravelTime() + ", sanityCheck()=" + sanityCheck() + "]";
	}
}
