package org.transitime.core.headwaygenerator;

import java.util.Date;
import java.util.List;

import org.transitime.applications.Core;
import org.transitime.core.HeadwayGenerator;
import org.transitime.core.VehicleState;
import org.transitime.core.dataCache.StopArrivalDepartureCache;
import org.transitime.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.Headway;

/**
 *
 * @author Sean Óg Crudden
 * 
 * This is a first pass at generating a Headway value. It will find the last arrival time at the last stop for the vehicle and then get the vehicle ahead of it and check when it arrived at the same stop. The difference will be used as the headway.
 * 
 * This is a WIP
 * 
 * Maybe should be a list and have a predicted headway at each stop along the route. So key for headway could be (stop, vehicle, trip, start_time).
 */
public class LastArrivalsHeadwayGenerator implements HeadwayGenerator {

	@Override
	public  Headway generate(VehicleState vehicleState) {
	
		try {
			String stopId = vehicleState.getMatch().getMatchAtPreviousStop().getAtStop().getStopId();
			
			long date = vehicleState.getMatch().getAvlTime();
			
			String vehicleId=vehicleState.getVehicleId();
			
			StopArrivalDepartureCacheKey key=new StopArrivalDepartureCacheKey(stopId, new Date(date));
			
			List<ArrivalDeparture> stopList=StopArrivalDepartureCache.getInstance().getStopHistory(key);
			int lastStopArrivalIndex =-1;
			int previousVehicleArrivalIndex = -1;
			
			if(stopList!=null)
			{
				for(int i=0;i<stopList.size() && previousVehicleArrivalIndex==-1 ;i++)
				{
					ArrivalDeparture arrivalDepature = stopList.get(i);
					if(arrivalDepature.isArrival() && arrivalDepature.getStopId().equals(stopId) && arrivalDepature.getVehicleId().equals(vehicleId) )
					{
						// This the arrival of this vehicle now the next arrival in the list will be the previous vehicle (The arrival of the vehicle ahead).
						lastStopArrivalIndex=i;				
					}
					if(lastStopArrivalIndex>-1 && arrivalDepature.isArrival() && arrivalDepature.getStopId().equals(stopId) && !arrivalDepature.getVehicleId().equals(vehicleId) )
					{
						previousVehicleArrivalIndex = i;
					}
				}
				if(previousVehicleArrivalIndex!=-1 && lastStopArrivalIndex!=-1)
				{
					long headwayTime=Math.abs(stopList.get(lastStopArrivalIndex).getTime()-stopList.get(previousVehicleArrivalIndex).getTime());
	
					Headway headway=new Headway(headwayTime, new Date(date), vehicleId, stopId, vehicleState.getTrip().getId(), vehicleState.getTrip().getRouteId());
					// TODO Core.getInstance().getDbLogger().add(headway);
					return headway;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return null;		
	}

}
