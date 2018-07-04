package org.transitclock.core.holdingmethod;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringListConfigValue;
import org.transitclock.core.ArrivalDepartureGeneratorDefaultImpl;
import org.transitclock.core.Indices;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.PredictionDataCache;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheFactory;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.core.dataCache.VehicleStateManager;
import org.transitclock.core.dataCache.ehcache.StopArrivalDepartureCache;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.HoldingTime;
import org.transitclock.db.structs.Prediction;
import org.transitclock.db.structs.Stop;
import org.transitclock.ipc.data.IpcPrediction;
import org.transitclock.ipc.data.IpcPredictionsForRouteStopDest;
import org.transitclock.ipc.data.IpcVehicleComplete;

/**
 * @author Sean Óg Crudden
 * Simple holding time generator.
 */
public class SimpleHoldingTimeGeneratorImpl implements HoldingTimeGenerator {
	private static final Logger logger =
			LoggerFactory.getLogger(SimpleHoldingTimeGeneratorImpl.class);

	protected static BooleanConfigValue storeHoldingTimes = new BooleanConfigValue("transitclock.holding.storeHoldingTimes",
			true,
			"This is set to true to record all holding times.");

	protected static IntegerConfigValue  plannedHeadwayMsec = new IntegerConfigValue("transitclock.holding.plannedHeadwayMsec", 60*1000*9, "Planned Headway");
	protected static StringListConfigValue controlStopList = new StringListConfigValue("transitclock.holding.controlStops", null, "This is a list of stops to generate holding times for.");

	public HoldingTime generateHoldingTime(VehicleState vehicleState, ArrivalDeparture event) {

		if(event.isArrival() && isControlStop(event.getStopId(), event.getStopPathIndex()))
		{
 			logger.debug("Calling Simple Holding Generator for event : {}", event.toString());

			ArrivalDeparture lastVehicleDeparture = getLastVehicleDepartureTime(event.getTripId(), event.getStopId(), new Date(event.getTime()));

			if(lastVehicleDeparture!=null)
			{
				logger.debug("Found last vehicle departure event: {}", lastVehicleDeparture.toString());

				long current_vehicle_arrival_time=event.getTime();

				long holdingTimeValue=calculateHoldingTime(current_vehicle_arrival_time, lastVehicleDeparture.getTime());

				logger.debug("Holding time for : {} is {}.", event.toString(), holdingTimeValue);

				HoldingTime holdingTime=new HoldingTime(new Date(current_vehicle_arrival_time+holdingTimeValue),  new Date(Core.getInstance().getSystemTime()), event.getVehicleId(), event.getStopId(), event.getTripId(),
						event.getRouteId(), false, true, new Date(event.getTime()), true, 0);

				if(storeHoldingTimes.getValue())
					Core.getInstance().getDbLogger().add(holdingTime);

				return holdingTime;

			}else
			{
				logger.debug("Did not find last vehicle departure for stop {}. This is required to calculate holding time.",event.getStopId() );

				long current_vehicle_arrival_time=event.getTime();
				HoldingTime holdingTime=new HoldingTime(new Date(current_vehicle_arrival_time),  new Date(Core.getInstance().getSystemTime()), event.getVehicleId(), event.getStopId(), event.getTripId(),
						event.getRouteId(), false, true, new Date(event.getTime()), false, 0);
				return holdingTime;
			}

		}
		// Return null so has no effect.
		return null;
	}

	private ArrivalDeparture getLastVehicleDepartureTime(String tripId, String stopId, Date time)
	{
		StopArrivalDepartureCacheKey currentStopKey=new StopArrivalDepartureCacheKey(stopId,time);

		List<ArrivalDeparture> currentStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(currentStopKey);

		ArrivalDeparture closestDepartureEvent=null;
		if(currentStopList!=null)
		{
			/* These are sorted when placed in cache */
			for(ArrivalDeparture event:currentStopList)
			{
				//if(event.isDeparture() && event.getTripId().equals(tripId)) TODO This is because of the GTFS config of Atlanta Streetcar. Could we use route_id instead?
				if(event.isDeparture() )
				{
					if(closestDepartureEvent==null)
						closestDepartureEvent=event;
					else if (time.getTime()-event.getTime() < time.getTime()-closestDepartureEvent.getTime())
					{
						closestDepartureEvent=event;
					}
				}
			}
		}
		return closestDepartureEvent;
	}
	private long calculateHoldingTime(long current_vehicle_arrival_time, long last_vehicle_departure_time, long N_1, long N_2)
	{
		//G16=MAX(IF((E18-H15)/3>(F17-H15)/2,((E18-H15)/3-(B16-H15))/1.5,((F17-H15)/2-(B16-H15))/2),0)

		/*Vehicle Z departure time at this stop. (H15)
		One vehicle arrive at control point at 3:20. (vehicle A) (B16)
		The next + 1 vehicle arrives at control point at 3:29 (vehicle B) (F17)
		The next + 2 vehicle arrives at control point at 3:42 (vehicle C) (E18) */

		long holdingTime;
		long E18=N_2;
		long F17=N_1;
		long B16=current_vehicle_arrival_time;
		long H15=last_vehicle_departure_time;

		if(((E18-H15)/3)>((F17-H15)/2))
		{
			holdingTime=(long) (((E18-H15)/3-(B16-H15)));
		}else
		{
			holdingTime=((F17-H15)/2-(B16-H15));
		}
		return Math.max(holdingTime, 0);

	}



	private long calculateHoldingTime(long current_vehicle_arrival_time, long last_vehicle_departure_time)
	{
		// TODO to be implemented as per google doc.

		//HoldingTime= max(0,PlannedHeadway - TimeSinceLastDeparture)
		long holdingTime;

		holdingTime=Math.max(plannedHeadwayMsec.getValue().longValue()-Math.abs(current_vehicle_arrival_time-last_vehicle_departure_time) , 0);

		return holdingTime;
	}
	@Override
	public HoldingTime generateHoldingTime(VehicleState vehicleState,IpcPrediction arrivalPrediction) {

		HoldingTime holdingTime = null;

		return holdingTime;
	}
	protected IpcPrediction getForwardVehicleDeparturePrediction(IpcPrediction predictionEvent)
	{
		PredictionDataCache predictionCache = PredictionDataCache.getInstance();

		List<IpcPrediction> predictions = new ArrayList<IpcPrediction>();

		List<IpcPredictionsForRouteStopDest> predictionsForRouteStopDests = predictionCache.getPredictions(predictionEvent.getRouteId(), predictionEvent.getStopId());

		for(IpcPredictionsForRouteStopDest predictionForRouteStopDest: predictionsForRouteStopDests)
		{
			for(IpcPrediction prediction:predictionForRouteStopDest.getPredictionsForRouteStop())
			{
				predictions.add(prediction);
			}
		}
		Collections.sort(predictions, new PredictionTimeComparator());

		int found=-1;

		for(int i=0;i<predictions.size();i++)
		{
			if(predictions.get(i).getStopId().equals(predictionEvent.getStopId())&&
					predictions.get(i).getTripId().equals(predictionEvent.getTripId())&&predictions.get(i).isArrival()&&predictionEvent.isArrival())
			{
				found=i;
			}
			/* now look until after this prediction and it is the next one for same stop but a different vehicle */
			if(found!=-1 && i>found)
			{
				if(predictions.get(i).isArrival()==false
					&& predictions.get(i).getStopId().equals(predictionEvent.getStopId())
					&& !predictions.get(i).getTripId().equals(predictionEvent.getTripId()))
				{
					return predictions.get(i);
				}
			}
		}
		return null;
	}
	protected List<IpcPrediction> getBackwardArrivalPredictions(IpcPrediction predictionEvent)
	{
		PredictionDataCache predictionCache = PredictionDataCache.getInstance();

		List<IpcPrediction> predictions = new ArrayList<IpcPrediction>();

		List<IpcPredictionsForRouteStopDest> predictionsForRouteStopDests = predictionCache.getPredictions(predictionEvent.getRouteId(), predictionEvent.getStopId());

		for(IpcPredictionsForRouteStopDest predictionForRouteStopDest: predictionsForRouteStopDests)
		{
			for(IpcPrediction prediction:predictionForRouteStopDest.getPredictionsForRouteStop())
			{
				logger.debug(prediction.toString());
				//TODO if(prediction.getPredictionTime()>predictionEvent.getPredictionTime()&&prediction.isArrival())
				// The end of the route it seems to only generate departure predictions which points towards an issue.
				//if(prediction.getPredictionTime()>predictionEvent.getPredictionTime())
				if(prediction.getPredictionTime()>predictionEvent.getPredictionTime()&&prediction.isArrival())
					predictions.add(prediction);
			}
		}
		Collections.sort(predictions, new PredictionTimeComparator());
		/* TODO get the first two of the same type */
		return predictions;
	}
	@Override
	public List<ControlStop> getControlPointStops() {

		ArrayList<ControlStop> controlStops=new ArrayList<ControlStop>();

		for(String stopEntry: controlStopList.getValue())
		{
			controlStops.add(new ControlStop(stopEntry));
		}
		return controlStops;
	}

	private boolean isControlStop(String stopId, int stopPathIndex)
	{
		ControlStop controlStop=new ControlStop(""+stopPathIndex, stopId);
		if(getControlPointStops()!=null)
			return getControlPointStops().contains(controlStop);
		else
			return false;
	}

	@Override
	public void handleDeparture(VehicleState vehicleState, ArrivalDeparture arrivalDeparture) {
		// TODO Auto-generated method stub

	}


}
