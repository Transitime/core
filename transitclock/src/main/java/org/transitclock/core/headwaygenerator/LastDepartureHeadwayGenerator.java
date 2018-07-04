package org.transitclock.core.headwaygenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.transitclock.applications.Core;
import org.transitclock.core.HeadwayGenerator;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.PredictionDataCache;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheFactory;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.core.dataCache.VehicleStateManager;
import org.transitclock.core.dataCache.ehcache.StopArrivalDepartureCache;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Headway;
import org.transitclock.ipc.data.IpcVehicleComplete;

/**
 *
 * @author Sean Óg Crudden
 *
 * This is a first pass at generating a Headway value. It will find the last departure time at the last stop for the vehicle and then get the vehicle ahead of it and check when it departed the same stop. The difference will be used as the headway.
 *
 * This is a WIP
 *
 * Maybe should be a list and have a predicted headway at each stop along the route. So key for headway could be (stop, vehicle, trip, start_time).
 */
public class LastDepartureHeadwayGenerator implements HeadwayGenerator {

	@Override
	public  Headway generate(VehicleState vehicleState) {

		try {
			String stopId = vehicleState.getMatch().getMatchAtPreviousStop().getAtStop().getStopId();

			long date = vehicleState.getMatch().getAvlTime();

			String vehicleId=vehicleState.getVehicleId();

			StopArrivalDepartureCacheKey key=new StopArrivalDepartureCacheKey(stopId, new Date(date));

			List<ArrivalDeparture> stopList=StopArrivalDepartureCacheFactory.getInstance().getStopHistory(key);
			int lastStopArrivalIndex =-1;
			int previousVehicleArrivalIndex = -1;


			if(stopList!=null)
			{
				for(int i=0;i<stopList.size() && previousVehicleArrivalIndex==-1 ;i++)
				{
					ArrivalDeparture arrivalDepature = stopList.get(i);
					if(arrivalDepature.isDeparture() && arrivalDepature.getStopId().equals(stopId) && arrivalDepature.getVehicleId().equals(vehicleId)
							&& (vehicleState.getTrip().getDirectionId()==null || vehicleState.getTrip().getDirectionId().equals(arrivalDepature.getDirectionId())))
					{
						// This the arrival of this vehicle now the next arrival in the list will be the previous vehicle (The arrival of the vehicle ahead).
						lastStopArrivalIndex=i;
					}
					if(lastStopArrivalIndex>-1 && arrivalDepature.isDeparture() && arrivalDepature.getStopId().equals(stopId) && !arrivalDepature.getVehicleId().equals(vehicleId)
							&& (vehicleState.getTrip().getDirectionId()==null || vehicleState.getTrip().getDirectionId().equals(arrivalDepature.getDirectionId())))
					{
						previousVehicleArrivalIndex = i;
					}
				}
				if(previousVehicleArrivalIndex!=-1 && lastStopArrivalIndex!=-1)
				{
					long headwayTime=Math.abs(stopList.get(lastStopArrivalIndex).getTime()-stopList.get(previousVehicleArrivalIndex).getTime());

					Headway headway=new Headway(headwayTime, new Date(date), vehicleId, stopList.get(previousVehicleArrivalIndex).getVehicleId(), stopId, vehicleState.getTrip().getId(), vehicleState.getTrip().getRouteId(), new Date(stopList.get(lastStopArrivalIndex).getTime()), new Date(stopList.get(previousVehicleArrivalIndex).getTime()));
					// TODO Core.getInstance().getDbLogger().add(headway);

					// remove rubish data from departure sfrom t
					if(Math.abs(headway.getCreationTime().getTime()-headway.getFirstDeparture().getTime())>1200000||lastStopArrivalIndex>5)
					{
						headway=null;
						vehicleState.setHeadway(null);
						return null;
					}
					if(headway!=null)
					{
						if(vehicleState.getHeadway()==null||!vehicleState.getHeadway().equals(headway))
						{
							vehicleState.setHeadway(headway);
							setSystemVariance(headway);
							return headway;
						}else
						{
							return null;
						}
					}

				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	private void setSystemVariance(Headway headway)
	{
		ArrayList<Headway> headways=new ArrayList<Headway>();

		int total_with_headway=0;
		int total_vehicles=0;
		boolean error=false;
		for(IpcVehicleComplete currentVehicle:VehicleDataCache.getInstance().getVehicles())
		{
			VehicleState vehicleState = VehicleStateManager.getInstance().getVehicleState(currentVehicle.getId());
			if(vehicleState.getHeadway()!=null)
			{
				headways.add(vehicleState.getHeadway());
				total_with_headway++;
			}
			total_vehicles++;
		}
		// ONLY SET IF HAVE VALES FOR ALL VEHICLES ON ROUTE.
		if(VehicleDataCache.getInstance().getVehicles().size()==headways.size()&&total_vehicles==total_with_headway)
		{
			headway.setAverage(average(headways));
			headway.setVariance(variance(headways));
			headway.setCoefficientOfVariation(coefficientOfVariance(headways));
			headway.setNumVehicles(headways.size());
		}else
		{
			headway.setAverage(-1);
			headway.setVariance(-1);
			headway.setCoefficientOfVariation(-1);
			headway.setNumVehicles(total_with_headway);
		}
	}
	private double average(List<Headway> headways)
	{
		double total=0;
		for(Headway headway:headways)
		{
			total=total+headway.getHeadway();
		}
		return total/headways.size();
	}
	private double variance(List<Headway> headways)
	{
		double topline=0;
		double average = average(headways);
		for(Headway headway:headways)
		{
			topline=topline+((headway.getHeadway()-average)*(headway.getHeadway()-average));
		}
		return topline/headways.size();
	}
	private double coefficientOfVariance(List<Headway> headways)
	{
		double variance = variance(headways);;
		double average = average(headways);

		return variance/(average*average);
	}

}
