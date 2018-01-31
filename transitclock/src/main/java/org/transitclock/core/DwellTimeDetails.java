package org.transitclock.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.utils.Time;

public class DwellTimeDetails {
	private ArrivalDeparture departure;
	private ArrivalDeparture arrival;
	private static final Logger logger = LoggerFactory
			.getLogger(DwellTimeDetails.class);
	private static final IntegerConfigValue maxDwellTime = 
			new IntegerConfigValue(
					"transitclock.core.maxDwellTime",
					10 * Time.MS_PER_MIN,
					"This is a maximum dwell time at a stop to be taken into account for cache or prediction calculations.");
	
	
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
		if(this.arrival!=null && this.departure!=null&& arrival.isArrival() && departure.isDeparture())
		{
			
			
			if(sanityCheck())
			{
				long dwellTime=this.departure.getTime()-this.arrival.getTime();
				return dwellTime;
			}else
			{
				logger.warn("Outside bounds : {} ", this);
			}
		}		
		return -1;		
	}
	public boolean sanityCheck()
	{
		if(this.arrival!=null && this.departure!=null&& arrival.isArrival() && departure.isDeparture())
		{
			long dwellTime=this.departure.getTime()-this.arrival.getTime();
			if(dwellTime<0||dwellTime>maxDwellTime.getValue())
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
		return "DwellTimeDetails [departure=" + departure + ", arrival=" + arrival + ", getDwellTime()="
				+ getDwellTime() + ", sanityCheck()=" + sanityCheck() + "]";
	}
	
}
