package org.transitime.core.dataCache;

import java.util.Comparator;

import org.transitime.db.structs.ArrivalDeparture;

public class ArrivalDepartureComparator implements Comparator<ArrivalDeparture> {	
	

	@Override
	public int compare(ArrivalDeparture ad1, ArrivalDeparture ad2) {
		
		if(ad1.getTime()<ad2.getTime())
		{
			 return -1;
		}else if(ad1.getTime()> ad2.getTime())
		{
			 return 1;
		}
		return 0;
	}
}
