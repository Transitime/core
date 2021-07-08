package org.transitclock.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.utils.Time;

public class DwellTimeDetails {
	private IpcArrivalDeparture departure;
	private IpcArrivalDeparture arrival;
	private static final Logger logger = LoggerFactory
			.getLogger(DwellTimeDetails.class);
	private static final IntegerConfigValue maxDwellTime = 
			new IntegerConfigValue(
					"transitclock.core.maxDwellTime",
					10 * Time.MS_PER_MIN,
					"This is a maximum dwell time at a stop to be taken into account for cache or prediction calculations.");
	
	
	public IpcArrivalDeparture getDeparture() {
		return departure;
	}	
	public IpcArrivalDeparture getArrival() {
		return arrival;
	}
	public DwellTimeDetails(IpcArrivalDeparture arrival, IpcArrivalDeparture departure) {
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
				long dwellTime=this.departure.getTime().getTime()-this.arrival.getTime().getTime();
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
			long dwellTime=this.departure.getTime().getTime()-this.arrival.getTime().getTime();
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
