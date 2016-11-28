package org.transitime.core.holdingmethod;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.BooleanConfigValue;
import org.transitime.config.StringListConfigValue;
import org.transitime.core.ArrivalDepartureGeneratorDefaultImpl;
import org.transitime.core.dataCache.PredictionDataCache;
import org.transitime.core.dataCache.StopArrivalDepartureCache;
import org.transitime.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.HoldingTime;
import org.transitime.db.structs.Prediction;
import org.transitime.db.structs.Stop;
import org.transitime.ipc.data.IpcPrediction;
import org.transitime.ipc.data.IpcPredictionsForRouteStopDest;

/**
 * @author Sean Óg Crudden
 * This is a default implementation of the holding time generator and is an implementation of the approach proposed by Simon Berrebi et al.
 * 
 * http://www.worldtransitresearch.info/research/5644/
 * 
 * The theory is to use the time generated as the holding time for the vehicle at the control point. This is in an attempt to manage the headway between vehicles along the route.
 * 
 * This is a WIP.
 */
public class HoldingTimeGeneratorDefaultImpl implements HoldingTimeGenerator {
	private static final Logger logger = 
			LoggerFactory.getLogger(HoldingTimeGeneratorDefaultImpl.class);
	
	protected static BooleanConfigValue storeHoldingTimes = new BooleanConfigValue("transitime.holding.storeHoldingTimes", 
			true,
			"This is set to true to record all holding times.");
	protected static StringListConfigValue controlStopList = new StringListConfigValue("transitime.holding.controlStops", null, "This is a list of stops to generate holding times for."); 
	
	public HoldingTime generateHoldingTime(ArrivalDeparture event) {
		PredictionDataCache predictionCache = PredictionDataCache.getInstance();
							
		HashMap<String, List<IpcPrediction>> predictionsByVehicle = new HashMap<String, List<IpcPrediction>>();
		
		if(event.isArrival() && isControlStop(event.getStopId()))
		{
			logger.debug("Calling Holding Generator for event : {}", event.toString());
															
			List<IpcPredictionsForRouteStopDest> predictionsForRouteStopDests = predictionCache.getPredictions(event.getRouteId(), event.getStopId());
			
			for(IpcPredictionsForRouteStopDest predictionForRouteStopDest: predictionsForRouteStopDests)
			{						
				for(IpcPrediction prediction:predictionForRouteStopDest.getPredictionsForRouteStop())
				{
					/* do not include prediction for current vehicle for current stop/trip. OK to include if on next trip around. */
					if(!prediction.getVehicleId().equals(event.getVehicleId())|| !prediction.getTripId().equals(event.getTripId()))
					{														
						if(predictionsByVehicle.containsKey(prediction.getVehicleId()))
						{
							predictionsByVehicle.get(prediction.getVehicleId()).add(prediction);
						}else
						{
							List<IpcPrediction> vehiclePredictions=new ArrayList<IpcPrediction>();
							vehiclePredictions.add(prediction);
							predictionsByVehicle.put(prediction.getVehicleId(), vehiclePredictions);								
						}
					}
				}													
			}
			
			List<IpcPrediction> predictions = new ArrayList<IpcPrediction>();
			
			for(String key:predictionsByVehicle.keySet())
			{
				predictions.add(predictionCache.getPredictionForVehicle(key, event.getRouteId(), event.getStopId()));
			}
			
			Collections.sort(predictions, new PredictionTimeComparator());
											
			logger.debug("Have {} predictions for stop {}.", predictions.size(), event.getStopId());
			
			if(predictions.size()>=2)
			{
				logger.debug("Prediction for N-1 {}: {} ", predictions.get(0).getVehicleId(),predictions.get(0).getPredictionTime());
				logger.debug("Prediction for N-2 {}: {} ", predictions.get(1).getVehicleId(),predictions.get(1).getPredictionTime());
				 						
				ArrivalDeparture lastVehicleDeparture = getLastVehicleDepartureTime(event.getTripId(), event.getStopId(), new Date(event.getTime()));
				
				if(lastVehicleDeparture!=null)
				{				
					logger.debug("Found last vehicle departure event: {}", lastVehicleDeparture.toString());
					
					long current_vehicle_arrival_time=event.getTime();
					
					long holdingTimeValue=calculateHoldingTime(current_vehicle_arrival_time, lastVehicleDeparture.getTime(),predictions.get(0).getPredictionTime(), predictions.get(1).getPredictionTime());
					
					logger.debug("Holding time for : {} is {}.", event.toString(), holdingTimeValue);
					
					HoldingTime holdingTime=new HoldingTime(new Date(current_vehicle_arrival_time+holdingTimeValue), Calendar.getInstance().getTime(), event.getVehicleId(), event.getStopId(), event.getTripId(),
							event.getRouteId(), false, true); 
					
					if(storeHoldingTimes.getValue())
						Core.getInstance().getDbLogger().add(holdingTime);
					
					return holdingTime;
				}else
				{
					logger.debug("Did not find last vehicle departure for stop {}. This is required to calculate holding time.",event.getStopId() );
				}
			}else
			{
				logger.debug("Insufficient predictions for stop to calculate holding time.");
			}
		
		}
		// Return null so has no effect.
		return null;		
	}
	private ArrivalDeparture getLastVehicleDepartureTime(String tripId, String stopId, Date time)
	{
		StopArrivalDepartureCacheKey currentStopKey=new StopArrivalDepartureCacheKey(stopId,time);
		
		List<ArrivalDeparture> currentStopList = StopArrivalDepartureCache.getInstance().getStopHistory(currentStopKey);
		
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
			holdingTime=(long) (((E18-H15)/3-(B16-H15))/1.5);
		}else
		{
			holdingTime=((F17-H15)/2-(B16-H15))/2;
		}		
		return Math.max(holdingTime, 0);
	}
	
	@Override
	public HoldingTime generateHoldingTime(IpcPrediction arrivalPrediction) {
		
		HoldingTime holdingTime = null;
		if(arrivalPrediction.isArrival() && isControlStop(arrivalPrediction.getStopId()))
		{
			logger.debug("Calling Holding Generator for prediction : {}", arrivalPrediction.toString());
			
			/* TODO We will also need a holding time based on predictions to inform predictions for later stops. We can swap to arrival one once we have arrival event for vehicle and set a definite hold time.*/
			IpcPrediction forwardDeparturePrediction=getForwardVehicleDeparturePrediction(arrivalPrediction);
			if(forwardDeparturePrediction==null)
				logger.debug("Cannot find last departure prediction for : {}", arrivalPrediction.toString());
			else
				logger.debug("Found last vehicle predicted departure event: {}", forwardDeparturePrediction.toString());
			
			ArrivalDeparture lastDeparture = getLastVehicleDepartureTime(arrivalPrediction.getTripId(), arrivalPrediction.getStopId(), new Date(arrivalPrediction.getPredictionTime()));
			if(lastDeparture==null)
				logger.debug("Cannot find last departure for : {}", arrivalPrediction.toString());
			else
				logger.debug("Found last vehicle departure event: {}", lastDeparture.toString());
			
			List<IpcPrediction> backwardArrivalPredictions=getBackwardArrivalPredictions(arrivalPrediction);
			if(backwardArrivalPredictions!=null)
				logger.debug("Found {} arrival predictions for stop {}.", backwardArrivalPredictions.size(),arrivalPrediction.getStopId());
									
			if((forwardDeparturePrediction != null || lastDeparture!=null) && backwardArrivalPredictions!=null && backwardArrivalPredictions.size()>=2)
			{
				logger.debug("Prediction for N-1 {}: {} ", backwardArrivalPredictions.get(0).getVehicleId(),backwardArrivalPredictions.get(0).getPredictionTime());
				logger.debug("Prediction for N-2 {}: {} ", backwardArrivalPredictions.get(1).getVehicleId(),backwardArrivalPredictions.get(1).getPredictionTime());
				
				if(lastDeparture!=null&&forwardDeparturePrediction!=null)
				{
					if(lastDeparture.getTime()>forwardDeparturePrediction.getPredictionTime())
					{
						long holdingTimeValue=calculateHoldingTime(arrivalPrediction.getPredictionTime(), forwardDeparturePrediction.getPredictionTime() ,backwardArrivalPredictions.get(0).getPredictionTime(), backwardArrivalPredictions.get(1).getPredictionTime());
						
						logger.debug("Holding time for : {} is {}.", arrivalPrediction.toString(), holdingTimeValue);
						
						holdingTime=new HoldingTime(new Date(arrivalPrediction.getPredictionTime()+holdingTimeValue), Calendar.getInstance().getTime(), arrivalPrediction.getVehicleId(), arrivalPrediction.getStopId(), arrivalPrediction.getTripId(),
								arrivalPrediction.getRouteId(),true, false);
																						
					}else if(lastDeparture.getTime()<=forwardDeparturePrediction.getPredictionTime())
					{
						long holdingTimeValue=calculateHoldingTime(arrivalPrediction.getPredictionTime(), lastDeparture.getTime() ,backwardArrivalPredictions.get(0).getPredictionTime(), backwardArrivalPredictions.get(1).getPredictionTime());
						
						logger.debug("Holding time for : {} is {}.", arrivalPrediction.toString(), holdingTimeValue);
						
						holdingTime=new HoldingTime(new Date(arrivalPrediction.getPredictionTime()+holdingTimeValue), Calendar.getInstance().getTime(), arrivalPrediction.getVehicleId(), arrivalPrediction.getStopId(), arrivalPrediction.getTripId(),
								arrivalPrediction.getRouteId(),true, false); 

					}
				}else if(forwardDeparturePrediction!=null)
				{
					long holdingTimeValue=calculateHoldingTime(arrivalPrediction.getPredictionTime(), forwardDeparturePrediction.getPredictionTime() ,backwardArrivalPredictions.get(0).getPredictionTime(), backwardArrivalPredictions.get(1).getPredictionTime());
					
					logger.debug("Holding time for : {} is {}.", arrivalPrediction.toString(), holdingTimeValue);
					
					holdingTime=new HoldingTime(new Date(arrivalPrediction.getPredictionTime()+holdingTimeValue), Calendar.getInstance().getTime(), arrivalPrediction.getVehicleId(), arrivalPrediction.getStopId(), arrivalPrediction.getTripId(),
							arrivalPrediction.getRouteId(), true, false);
					
				}else if(lastDeparture!=null)
				{
					long holdingTimeValue=calculateHoldingTime(arrivalPrediction.getPredictionTime(), lastDeparture.getTime() ,backwardArrivalPredictions.get(0).getPredictionTime(), backwardArrivalPredictions.get(1).getPredictionTime());
					
					logger.debug("Holding time for : {} is {}.", arrivalPrediction.toString(), holdingTimeValue);
					
					holdingTime=new HoldingTime(new Date(arrivalPrediction.getPredictionTime()+holdingTimeValue), Calendar.getInstance().getTime(), arrivalPrediction.getVehicleId(), arrivalPrediction.getStopId(), arrivalPrediction.getTripId(),
							arrivalPrediction.getRouteId(), true, false);				
				}
					
			}else
			{
				logger.debug("Insufficient data for stop to calculate holding time.");
			}
		}
		
		if(storeHoldingTimes.getValue() && holdingTime!=null)
			Core.getInstance().getDbLogger().add(holdingTime);
		
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
	public List<String> getControlPointStops() {
		return controlStopList.getValue();		
	}
	
	private boolean isControlStop(String stopId)
	{
		if(getControlPointStops()!=null)
			return getControlPointStops().contains(stopId);
		else
			return false;
	}
	
}
