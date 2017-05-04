package org.transitime.core.holdingmethod;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.BooleanConfigValue;
import org.transitime.config.IntegerConfigValue;
import org.transitime.config.StringListConfigValue;
import org.transitime.core.ArrivalDepartureGeneratorDefaultImpl;
import org.transitime.core.Indices;
import org.transitime.core.VehicleState;
import org.transitime.core.dataCache.PredictionDataCache;
import org.transitime.core.dataCache.StopArrivalDepartureCache;
import org.transitime.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitime.core.dataCache.VehicleDataCache;
import org.transitime.core.dataCache.VehicleStateManager;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.HoldingTime;
import org.transitime.db.structs.Prediction;
import org.transitime.db.structs.Stop;
import org.transitime.ipc.data.IpcPrediction;
import org.transitime.ipc.data.IpcPredictionsForRouteStopDest;
import org.transitime.ipc.data.IpcVehicleComplete;

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
	
	protected static IntegerConfigValue maxPredictionsForHoldingTimeCalculation = new IntegerConfigValue("transitime.holding.maxPredictionsForHoldingTimeCalculation", 
			3,
			"This is the maximim number of arrival predictions to include in holding time calculation");
	
	protected static IntegerConfigValue  plannedHeadwayMsec = new IntegerConfigValue("transitime.holding.plannedHeadwayMsec", 60*1000*9, "Planned Headway");
	protected static StringListConfigValue controlStopList = new StringListConfigValue("transitime.holding.controlStops", null, "This is a list of stops to generate holding times for."); 
	
	public HoldingTime generateHoldingTime(ArrivalDeparture event) {
				
		PredictionDataCache predictionCache = PredictionDataCache.getInstance();
							
		HashMap<String, List<IpcPrediction>> predictionsByVehicle = new HashMap<String, List<IpcPrediction>>();
					
		if(event.isArrival() && isControlStop(event.getStopId()))
		{ 				
			//logger.debug("All predictions : {}",predictionCache.getAllPredictions(5, 1000000).toString());													
			List<IpcPredictionsForRouteStopDest> predictionsForRouteStopDests = predictionCache.getPredictions(event.getRouteId(), event.getStopId());
			
			logger.debug("Calling Holding Generator for event : {} using predictions : {}", event.toString(),predictionsForRouteStopDests.toString());
			for(IpcPredictionsForRouteStopDest predictionForRouteStopDest: predictionsForRouteStopDests)
			{						
				for(IpcPrediction prediction:predictionForRouteStopDest.getPredictionsForRouteStop())
				{					
					Date eventFreqStartTime=event.getFreqStartTime();
					
					Date predictionFreqStartTime=new Date(prediction.getFreqStartTime());
										
					long diffInMinutes = -1;
					if(eventFreqStartTime!=null && predictionFreqStartTime!=null)			
						diffInMinutes=Math.abs(TimeUnit.MILLISECONDS.toMinutes(eventFreqStartTime.getTime()-predictionFreqStartTime.getTime()));
					
					/* do not include prediction for current vehicle for current stop/trip. OK to include if on next trip around. */
					if(!prediction.getVehicleId().equals(event.getVehicleId())
							|| !prediction.getTripId().equals(event.getTripId())
							|| ( eventFreqStartTime!=null && predictionFreqStartTime!=null && diffInMinutes>2))
					{		
						if(prediction.getVehicleId().equals(event.getVehicleId())  && diffInMinutes<2)
								logger.debug("Stop here");
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
				if(predictionCache.getPredictionForVehicle(key, event.getRouteId(), event.getStopId())!=null)
					predictions.add(predictionCache.getPredictionForVehicle(key, event.getRouteId(), event.getStopId()));				
			}
		
			Collections.sort(predictions, new PredictionTimeComparator());
			
			//This is to remove a prediction for the current vehicle and stop. Belt and braces.
			if(predictions.size()>0 && predictions.get(0).getVehicleId().equals(event.getVehicleId()))
			{
				predictions.remove(0);
			}
													
			logger.debug("Have {} predictions for stop {}.", predictions.size(), event.getStopId());
			
			ArrivalDeparture lastVehicleDeparture = getLastVehicleDepartureTime(event.getVehicleId(), event.getTripId(), event.getStopId(), new Date(event.getTime()));
								
			if(lastVehicleDeparture!=null)
			{
				logger.debug("Found last vehicle departure event: {}", lastVehicleDeparture.toString());
				
				if(predictions.size()>1&& Math.abs(predictions.get(1).getPredictionTime()-Core.getInstance().getSystemTime())>(17*60*1000))
				{
					for(int i=0;i<predictions.size()&&i<maxPredictionsForHoldingTimeCalculation.getValue();i++)
					{
						logger.debug("Prediction for N-{} {}: {} ", i+1, predictions.get(i).getVehicleId(),predictions.get(i));
					}
					Long N[]=predictionsToLongArray(predictions);
					long current_vehicle_arrival_time=event.getTime();
					long holdingTimeValue=calculateHoldingTime(current_vehicle_arrival_time, lastVehicleDeparture.getTime(), N, maxPredictionsForHoldingTimeCalculation.getValue());
					
					logger.debug("Holding time for : {} is {}.", event.toString(), holdingTimeValue);
					
					HoldingTime holdingTime=new HoldingTime(new Date(current_vehicle_arrival_time+holdingTimeValue),  new Date(Core.getInstance().getSystemTime()), event.getVehicleId(), event.getStopId(), event.getTripId(),
							event.getRouteId(), false, true, new Date(event.getTime()), true, N.length);
					
					if(storeHoldingTimes.getValue())
						Core.getInstance().getDbLogger().add(holdingTime);
					
					return holdingTime;
				}						
				else 
				{																																		
					long current_vehicle_arrival_time=event.getTime();
					
					long holdingTimeValue=calculateHoldingTime(current_vehicle_arrival_time, lastVehicleDeparture.getTime());
					
					logger.debug("Holding time for : {} is {}.", event.toString(), holdingTimeValue);
					
					HoldingTime holdingTime=new HoldingTime(new Date(current_vehicle_arrival_time+holdingTimeValue),  new Date(Core.getInstance().getSystemTime()), event.getVehicleId(), event.getStopId(), event.getTripId(),
							event.getRouteId(), false, true, new Date(event.getTime()), true, 0); 
					
					if(storeHoldingTimes.getValue())
						Core.getInstance().getDbLogger().add(holdingTime);
					
					return holdingTime;				
				}
			}else
			{
				
				logger.debug("Did not find last vehicle departure for stop {}. This is required to calculate holding time.",event.getStopId() );
				long current_vehicle_arrival_time=event.getTime();
				HoldingTime holdingTime=new HoldingTime(new Date(current_vehicle_arrival_time),  new Date(Core.getInstance().getSystemTime()), event.getVehicleId(), event.getStopId(), event.getTripId(),
						event.getRouteId(), false, true, new Date(event.getTime()), false, 0);
				
				if(storeHoldingTimes.getValue())
					Core.getInstance().getDbLogger().add(holdingTime);
				
				return holdingTime;
			}
		
		}
		// Return null so has no effect.
		return null;		
	}
	// TODO this may need similar for scheduled based but may not as holding is really something for frequency based services.
	private boolean vehicleInBetween(VehicleDataCache vehicleDataCache, String routeId, String vehicleId_1, String vehicleId_2) {
		
		Collection<IpcVehicleComplete> vehicles = vehicleDataCache.getVehiclesForRoute(routeId);
		
		IpcVehicleComplete vehicle_1=vehicleDataCache.getVehicle(vehicleId_1);
		IpcVehicleComplete vehicle_2=vehicleDataCache.getVehicle(vehicleId_2);
				
		for(IpcVehicleComplete vehicle:vehicles)
		{
			if(!vehicleId_1.equals(vehicle.getId())&&!vehicleId_2.equals(vehicle.getId()))
			{
				if(vehicle.getFreqStartTime()<=vehicle_1.getFreqStartTime() && vehicle.getFreqStartTime()>=vehicle_2.getFreqStartTime() )
				{
					if(vehicle.getFreqStartTime()==vehicle_1.getFreqStartTime() &&  vehicle.getFreqStartTime()==vehicle_2.getFreqStartTime() )
					{					
						if(vehicle.getDistanceAlongTrip()>vehicle_1.getDistanceAlongTrip() && vehicle.getDistanceAlongTrip()<vehicle_2.getDistanceAlongTrip())
						{
							return true;
						}
						if(vehicle.getFreqStartTime()<vehicle_1.getFreqStartTime() && vehicle.getDistanceAlongTrip()<vehicle_2.getDistanceAlongTrip())
						{
							return true;
						}
						if(vehicle.getFreqStartTime()>vehicle_2.getFreqStartTime() && vehicle.getDistanceAlongTrip()>vehicle_1.getDistanceAlongTrip())
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	private ArrivalDeparture getLastVehicleDepartureTime(String currentVehicleId, String tripId, String stopId, Date time)
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
				if(!event.getVehicleId().equals(currentVehicleId))
				{
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
		}
		return closestDepartureEvent;
	}
	private static Long calculateHoldingTime(Long current_vehicle_arrival_time, Long last_vehicle_departure_time, Long N[], int max_predictions)
	{		
		long max_value=-1;
		
		for(int i=0;i<N.length&&i<max_predictions;i++)
		{
			long value=(N[i]-last_vehicle_departure_time)/(i+2);
			if(value>max_value)
			{
				max_value=value;				
			}		
		}
		return Math.max(max_value-(current_vehicle_arrival_time-last_vehicle_departure_time),0);
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
			
			ArrivalDeparture lastDeparture = getLastVehicleDepartureTime(arrivalPrediction.getVehicleId(),arrivalPrediction.getTripId(), arrivalPrediction.getStopId(), new Date(arrivalPrediction.getPredictionTime()));
			if(lastDeparture==null)
				logger.debug("Cannot find last departure for : {}", arrivalPrediction.toString());
			else
				logger.debug("Found last vehicle departure event: {}", lastDeparture.toString());
			
			List<IpcPrediction> backwardArrivalPredictions=getBackwardArrivalPredictions(arrivalPrediction);
			if(backwardArrivalPredictions!=null)
				logger.debug("Found {} arrival predictions for stop {}.", backwardArrivalPredictions.size(),arrivalPrediction.getStopId());
									
			if((forwardDeparturePrediction != null || lastDeparture!=null) && backwardArrivalPredictions!=null && backwardArrivalPredictions.size()>0)
			{				
				for(int i=0;i<backwardArrivalPredictions.size();i++)
				{
					logger.debug("Prediction for N-{} {}: {} ", i+1, backwardArrivalPredictions.get(i).getVehicleId(),backwardArrivalPredictions.get(i));
				}
				Long N[]=predictionsToLongArray(backwardArrivalPredictions);
				if(lastDeparture!=null&&forwardDeparturePrediction!=null)
				{
					if(lastDeparture.getTime()>forwardDeparturePrediction.getPredictionTime())
					{
						long holdingTimeValue=calculateHoldingTime(arrivalPrediction.getPredictionTime(), forwardDeparturePrediction.getPredictionTime() ,N, maxPredictionsForHoldingTimeCalculation.getValue());
						
						logger.debug("Holding time for : {} is {}.", arrivalPrediction.toString(), holdingTimeValue);
						
						holdingTime=new HoldingTime(new Date(arrivalPrediction.getPredictionTime()+holdingTimeValue),  new Date(Core.getInstance().getSystemTime()), arrivalPrediction.getVehicleId(), arrivalPrediction.getStopId(), arrivalPrediction.getTripId(),
								arrivalPrediction.getRouteId(),true, false, new Date(arrivalPrediction.getPredictionTime()),  true, N.length);
						
						if(storeHoldingTimes.getValue())
							Core.getInstance().getDbLogger().add(holdingTime);
																						
					}else if(lastDeparture.getTime()<=forwardDeparturePrediction.getPredictionTime())
					{
						long holdingTimeValue=calculateHoldingTime(arrivalPrediction.getPredictionTime(), lastDeparture.getTime() ,N, maxPredictionsForHoldingTimeCalculation.getValue());
						
						logger.debug("Holding time for : {} is {}.", arrivalPrediction.toString(), holdingTimeValue);
						
						holdingTime=new HoldingTime(new Date(arrivalPrediction.getPredictionTime()+holdingTimeValue),  new Date(Core.getInstance().getSystemTime()), arrivalPrediction.getVehicleId(), arrivalPrediction.getStopId(), arrivalPrediction.getTripId(),
								arrivalPrediction.getRouteId(),true, false, new Date(arrivalPrediction.getPredictionTime()), true, N.length); 
						
						if(storeHoldingTimes.getValue())
							Core.getInstance().getDbLogger().add(holdingTime);

					}
				}else if(forwardDeparturePrediction!=null)
				{
					long holdingTimeValue=calculateHoldingTime(arrivalPrediction.getPredictionTime(), forwardDeparturePrediction.getPredictionTime() ,N, maxPredictionsForHoldingTimeCalculation.getValue());
					
					logger.debug("Holding time for : {} is {}.", arrivalPrediction.toString(), holdingTimeValue);
					
					holdingTime=new HoldingTime(new Date(arrivalPrediction.getPredictionTime()+holdingTimeValue),  new Date(Core.getInstance().getSystemTime()), arrivalPrediction.getVehicleId(), arrivalPrediction.getStopId(), arrivalPrediction.getTripId(),
							arrivalPrediction.getRouteId(), true, false, new Date(arrivalPrediction.getPredictionTime()), true, N.length);
					
					if(storeHoldingTimes.getValue())
						Core.getInstance().getDbLogger().add(holdingTime);
					
				}else if(lastDeparture!=null)
				{
					long holdingTimeValue=calculateHoldingTime(arrivalPrediction.getPredictionTime(), lastDeparture.getTime() ,N, maxPredictionsForHoldingTimeCalculation.getValue());
					
					logger.debug("Holding time for : {} is {}.", arrivalPrediction.toString(), holdingTimeValue);
					
					holdingTime=new HoldingTime(new Date(arrivalPrediction.getPredictionTime()+holdingTimeValue),  new Date(Core.getInstance().getSystemTime()), arrivalPrediction.getVehicleId(), arrivalPrediction.getStopId(), arrivalPrediction.getTripId(),
							arrivalPrediction.getRouteId(), true, false, new Date(arrivalPrediction.getPredictionTime()), true, N.length);
					
					if(storeHoldingTimes.getValue())
						Core.getInstance().getDbLogger().add(holdingTime);
				}
					
			}else
			{
				if(forwardDeparturePrediction != null || lastDeparture!=null)
				{
					Long holdingTimeValue=null;
					if(lastDeparture!=null&&forwardDeparturePrediction!=null)
					{
						holdingTimeValue=calculateHoldingTime(Math.min(lastDeparture.getTime(), forwardDeparturePrediction.getPredictionTime()), lastDeparture.getTime() );
					}else
					if(lastDeparture!=null&&forwardDeparturePrediction==null)
					{
						holdingTimeValue=calculateHoldingTime(arrivalPrediction.getPredictionTime(), lastDeparture.getTime() );
					}else
					if(lastDeparture==null&&forwardDeparturePrediction!=null)
					{
						holdingTimeValue=calculateHoldingTime(arrivalPrediction.getPredictionTime(), forwardDeparturePrediction.getPredictionTime() );
					}
					if(holdingTimeValue!=null)
					{
						holdingTime=new HoldingTime(new Date(arrivalPrediction.getPredictionTime()+holdingTimeValue),  new Date(Core.getInstance().getSystemTime()), arrivalPrediction.getVehicleId(), arrivalPrediction.getStopId(), arrivalPrediction.getTripId(),
								arrivalPrediction.getRouteId(), true, false, new Date(arrivalPrediction.getPredictionTime()), true, 0);
						
						if(storeHoldingTimes.getValue())
							Core.getInstance().getDbLogger().add(holdingTime);
						
						return holdingTime;
					}else
					{
						logger.debug("Did not calucate holding time for some strange reason.",arrivalPrediction.getStopId());
						holdingTime=new HoldingTime(new Date(arrivalPrediction.getPredictionTime()),  new Date(Core.getInstance().getSystemTime()), arrivalPrediction.getVehicleId(), arrivalPrediction.getStopId(), arrivalPrediction.getTripId(),
								arrivalPrediction.getRouteId(), true, true, new Date(arrivalPrediction.getPredictionTime()), false, 0);
						
						if(storeHoldingTimes.getValue())
							Core.getInstance().getDbLogger().add(holdingTime);
						
						return holdingTime;
					}
				}else
				{
					logger.debug("Did not find last vehicle departure for stop {}. This is required to calculate holding time.",arrivalPrediction.getStopId());
					long current_vehicle_arrival_time=arrivalPrediction.getPredictionTime();
					holdingTime=new HoldingTime(new Date(current_vehicle_arrival_time),  new Date(Core.getInstance().getSystemTime()), arrivalPrediction.getVehicleId(), arrivalPrediction.getStopId(), arrivalPrediction.getTripId(),
							arrivalPrediction.getRouteId(), true, true, new Date(arrivalPrediction.getPredictionTime()), false, 0);
					
					if(storeHoldingTimes.getValue())
						Core.getInstance().getDbLogger().add(holdingTime);
					
					return holdingTime;
				}
			}
		}					
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
				//logger.debug(prediction.toString());
				//TODO if(prediction.getPredictionTime()>predictionEvent.getPredictionTime()&&prediction.isArrival())
				// The end of the route it seems to only generate departure predictions which points towards an issue.
				//if(prediction.getPredictionTime()>predictionEvent.getPredictionTime())
				if(prediction.getPredictionTime()>predictionEvent.getPredictionTime()&&prediction.isArrival()&&!predictionEvent.getVehicleId().equals(prediction.getVehicleId()))
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
	private Long[] predictionsToLongArray(List<IpcPrediction> predictions)
	{		
		Long[] list=new Long[predictions.size()];
		
		if(predictions!=null)
		{
			int i=0;
			for(IpcPrediction prediction:predictions)
			{
				list[i]=new Long(prediction.getPredictionTime());
				i++;
			}
		}
		return list;				
	}
	
	private boolean isControlStop(String stopId)
	{				
		ControlStop controlStop=new ControlStop( null, stopId);
		if(getControlPointStops()!=null)
		{
			for(ControlStop controlStopInList:getControlPointStops())
			{
				if(controlStopInList.getStopId().equals(controlStop.getStopId()))
				{
					return true;
				}
			}			
		}					
		return false;
	}
	public static void main(String[] args) {
		{
			Long N[]={new Long(11),new Long(18),new Long(28)};
		
			Long result=calculateHoldingTime(5L, 0L, N, maxPredictionsForHoldingTimeCalculation.getValue());
		
			System.out.println("Holding time: " + result);
		}
		{
			Long N[]={9L,21L,26L};
		
			Long result=calculateHoldingTime(5L, 0L, N, maxPredictionsForHoldingTimeCalculation.getValue());
		
			System.out.println("Holding time: " + result);
		}
		{
			Long N[]={10L};
			
			Long result=calculateHoldingTime(4L, 0L, N, maxPredictionsForHoldingTimeCalculation.getValue());
		
			System.out.println("Holding time: " + result);
		
		}		
		
		{
			Long N[]={12L,18L,25L};
			
			Long result=calculateHoldingTime(7L, 0L, N, maxPredictionsForHoldingTimeCalculation.getValue());
		
			System.out.println("Holding time: " + result);
		
		}
		{
			Long N[]={10L,14L,19L};
			
			Long result=calculateHoldingTime(3L, 0L, N, maxPredictionsForHoldingTimeCalculation.getValue());
		
			System.out.println("Holding time: " + result);
		
		}
		{
			Long N[]={13L,20L,28L};
			
			Long result=calculateHoldingTime(4L, 0L, N, maxPredictionsForHoldingTimeCalculation.getValue());
		
			System.out.println("Holding time: " + result);
		
		}
	}
}
