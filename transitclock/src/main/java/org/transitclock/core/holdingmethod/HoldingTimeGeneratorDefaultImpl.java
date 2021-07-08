package org.transitclock.core.holdingmethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringListConfigValue;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.ArrivalDepartureComparator;
import org.transitclock.core.dataCache.HoldingTimeCache;
import org.transitclock.core.dataCache.IpcArrivalDepartureComparator;
import org.transitclock.core.dataCache.PredictionDataCache;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheFactory;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.core.dataCache.VehicleStateManager;
import org.transitclock.core.dataCache.ehcache.StopArrivalDepartureCache;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.HoldingTime;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.ipc.data.IpcPrediction;
import org.transitclock.ipc.data.IpcPredictionsForRouteStopDest;
import org.transitclock.ipc.data.IpcVehicleComplete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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

	protected static BooleanConfigValue storeHoldingTimes = new BooleanConfigValue("transitclock.holding.storeHoldingTimes",
			true,
			"This is set to true to record all holding times.");

	protected static IntegerConfigValue maxPredictionsForHoldingTimeCalculation = new IntegerConfigValue("transitclock.holding.maxPredictionsForHoldingTimeCalculation",
			3,
			"This is the maximim number of arrival predictions to include in holding time calculation");

	protected static BooleanConfigValue useArrivalEvents = new BooleanConfigValue("transitclock.holding.usearrivalevents",
			true,
			"Generate a holding time on arrival events.");

	protected static BooleanConfigValue useArrivalPredictions = new BooleanConfigValue("transitclock.holding.usearrivalpredictions",
			true,
			"Generate a holding time on arrival predictions.");

	protected static BooleanConfigValue regenerateOnDeparture = new BooleanConfigValue("transitclock.holding.regenerateondeparture",
			false,
			"Regenerate a holding time for all vehicles at control point when a vehicle departs the control point.");

	protected static IntegerConfigValue  plannedHeadwayMsec = new IntegerConfigValue("transitclock.holding.plannedHeadwayMsec", 60*1000*9, "Planned Headway");
	protected static StringListConfigValue controlStopList = new StringListConfigValue("transitclock.holding.controlStops", null, "This is a list of stops to generate holding times for.");

	public HoldingTime generateHoldingTime(VehicleState vehicleState, IpcArrivalDeparture event) {

		PredictionDataCache predictionCache = PredictionDataCache.getInstance();

		if(!useArrivalEvents.getValue())
		{
			return null;
		}

		if(event.isArrival() && isControlStop(event.getStopId()))
		{

			List<IpcPrediction> predictions = new ArrayList<IpcPrediction>();

			for(IpcVehicleComplete currentVehicle:VehicleDataCache.getInstance().getVehicles())
			{
				if(predictionCache.getPredictionForVehicle(currentVehicle.getId(), event.getRouteId(), event.getStopId())!=null)
				{
					predictions.add(predictionCache.getPredictionForVehicle(currentVehicle.getId(), event.getRouteId(), event.getStopId()));
				}
			}
			Collections.sort(predictions, new PredictionTimeComparator());

			logger.debug("Calling Holding Generator for event : {} using predictions : {}", event.toString(),predictions.toString());

			//This is to remove a prediction for the current vehicle and stop. Belt and braces.
			if(predictions.size()>0 && predictions.get(0).getVehicleId().equals(event.getVehicleId()))
			{
				predictions.remove(0);
			}

			logger.debug("Have {} predictions for stops {}.", predictions.size(), event.getStopId());

			IpcArrivalDeparture lastVehicleDeparture = getLastVehicleDepartureTime(event.getVehicleId(), event.getTripId(), event.getStopId(), new Date(event.getTime().getTime()));

			/* Now get check if there is current holdingTie for the stop and get the next vehicle scheduled to leave and use its holding time as the departure time for calculation. */
			HoldingTime lastVehicleDepartureByHoldingTime=getNextDepartureByHoldingTime(event.getVehicleId(), getCurrentHoldingTimesForStop(event.getStopId()));

			// IF ARRIVAL OF HOLDING VEHICLE IS AFTER CURRENT DO NOT CONSIDER AS DEPARTURE
			/*if(lastVehicleDepartureByHoldingTime.getArrivalTime().getTime()<event.getTime())
			{
				lastVehicleDepartureByHoldingTime=null;
			}*/

			if(lastVehicleDepartureByHoldingTime!=null)
			{
				logger.debug("Found waiting vehicle with holding time: {}", lastVehicleDepartureByHoldingTime);
				if(predictions.size()>1)
				{
					Long N[]=null;

					int counter=0;

					N=predictionsToLongArray(predictions);

					for(int i=0;i<predictions.size()&&counter<maxPredictionsForHoldingTimeCalculation.getValue();i++)
					{
						logger.debug("Prediction for N-{} {}: {} ", counter+1, predictions.get(i).getVehicleId(),predictions.get(i));
						counter++;
					}

					long current_vehicle_arrival_time=event.getTime().getTime();
					// Use time to leave from now rather than any time in the past.
					long holdingTimeValue=calculateHoldingTime(current_vehicle_arrival_time, lastVehicleDepartureByHoldingTime.getTimeToLeave(event.getTime()).getTime(), N, maxPredictionsForHoldingTimeCalculation.getValue());

					HoldingTime holdingTime=new HoldingTime(new Date(current_vehicle_arrival_time+holdingTimeValue),  new Date(Core.getInstance().getSystemTime()), event.getVehicleId(), event.getStopId(), event.getTripId(),
							event.getRouteId(), false, true, new Date(event.getTime().getTime()), true, N.length);


					logger.debug("Holding time for : {} is {}.", event.toString(), holdingTime);

					if(storeHoldingTimes.getValue())
						Core.getInstance().getDbLogger().add(holdingTime);

					return holdingTime;
				}
				else
				{
					long current_vehicle_arrival_time=event.getTime().getTime();

					long holdingTimeValue=calculateHoldingTime(current_vehicle_arrival_time, lastVehicleDepartureByHoldingTime.getHoldingTime().getTime());

					HoldingTime holdingTime=new HoldingTime(new Date(current_vehicle_arrival_time+holdingTimeValue),  new Date(Core.getInstance().getSystemTime()), event.getVehicleId(), event.getStopId(), event.getTripId(),
							event.getRouteId(), false, true, new Date(event.getTime().getTime()), true, 0);

					logger.debug("Holding time for : {} is {}.", event.toString(), holdingTime);

					if(storeHoldingTimes.getValue())
						Core.getInstance().getDbLogger().add(holdingTime);

					return holdingTime;
				}
			}else
			if(lastVehicleDeparture!=null)
			{
				logger.debug("Found last vehicle departure event: {}", lastVehicleDeparture);

				if(predictions.size()>1)
				{
					Long N[]=null;

					int counter=0;

					N=predictionsToLongArray(predictions);

					for(int i=0;i<predictions.size()&&counter<maxPredictionsForHoldingTimeCalculation.getValue();i++)
					{
						logger.debug("Prediction for N-{} {}: {} ", counter+1, predictions.get(i).getVehicleId(),predictions.get(i));
						counter++;
					}

					long current_vehicle_arrival_time=event.getTime().getTime();

					long holdingTimeValue=calculateHoldingTime(current_vehicle_arrival_time, lastVehicleDeparture.getTime().getTime(), N, maxPredictionsForHoldingTimeCalculation.getValue());


					HoldingTime holdingTime=new HoldingTime(new Date(current_vehicle_arrival_time+holdingTimeValue),  new Date(Core.getInstance().getSystemTime()), event.getVehicleId(), event.getStopId(), event.getTripId(),
							event.getRouteId(), false, true, new Date(event.getTime().getTime()), true, N.length);

					logger.debug("Holding time for : {} is {}.", event.toString(), holdingTime);

					if(storeHoldingTimes.getValue())
						Core.getInstance().getDbLogger().add(holdingTime);

					return holdingTime;
				}
				else
				{
					long current_vehicle_arrival_time=event.getTime().getTime();

					long holdingTimeValue=calculateHoldingTime(current_vehicle_arrival_time, lastVehicleDeparture.getTime().getTime());

					logger.debug("Holding time for : {} is {}.", event.toString(), holdingTimeValue);

					HoldingTime holdingTime=new HoldingTime(new Date(current_vehicle_arrival_time+holdingTimeValue),  new Date(Core.getInstance().getSystemTime()), event.getVehicleId(), event.getStopId(), event.getTripId(),
							event.getRouteId(), false, true, new Date(event.getTime().getTime()), true, 0);

					if(storeHoldingTimes.getValue())
						Core.getInstance().getDbLogger().add(holdingTime);

					return holdingTime;
				}
			}else
			{
				logger.debug("Did not find last vehicle departure for stop {}. This is required to calculate holding time.",event.getStopId() );
				long current_vehicle_arrival_time=event.getTime().getTime();
				// TODO WHY BOTHER?
				HoldingTime holdingTime=new HoldingTime(new Date(current_vehicle_arrival_time),  new Date(Core.getInstance().getSystemTime()), event.getVehicleId(), event.getStopId(), event.getTripId(),
						event.getRouteId(), false, true, new Date(event.getTime().getTime()), false, 0);

				if(storeHoldingTimes.getValue())
					Core.getInstance().getDbLogger().add(holdingTime);

				return holdingTime;
			}
		}
		// Return null so has no effect.
		return null;
	}
	public static List<String> getOrderedListOfVehicles(String routeId)
	{
		int count=0;
		boolean canorder=true;
		List<VehicleState> unordered=new ArrayList<VehicleState>();
		List<String> ordered=null;
		for(VehicleState currentVehicleState:VehicleStateManager.getInstance().getVehiclesState())
		{
			if(currentVehicleState.getTrip()!=null&&currentVehicleState.getTrip().getRoute().getId().equals(routeId)&&currentVehicleState.isPredictable())
			{
				count++;
				unordered.add(currentVehicleState);
				if(currentVehicleState.getHeadway()==null)
				{
					canorder=false;
					
				}
			}
		}
		if(canorder)
		{
			ordered=new ArrayList<String>();
			
			while(((count+1) > 0)&&unordered.size() > 0)			
			{
				if(ordered.size()==0)
				{
					String first=unordered.get(0).getVehicleId();
					String second=unordered.get(0).getHeadway().getOtherVehicleId();
				
					ordered.add(first);
					count--;
					ordered.add(second);
					count--;
				}else
				{
					ordered.add(VehicleStateManager.getInstance().getVehicleState(ordered.get(ordered.size()-1)).getHeadway().getOtherVehicleId());
					count--;
				}
				
			}
			// check first vehicle equals last vehicle
			if(ordered.size()>1)
			{
				if(!ordered.get(ordered.size()-1).equals(ordered.get(0)))
				{
					return null;
				}
			}
		}
		return ordered;
	}
	protected ArrayList<HoldingTime> getCurrentHoldingTimesForStop(String stopId)
	{
		ArrayList<HoldingTime> currentHoldingTimes=new ArrayList<HoldingTime>();

		for(IpcVehicleComplete currentVehicle:VehicleDataCache.getInstance().getVehicles())
		{
			VehicleState vehicleState = VehicleStateManager.getInstance().getVehicleState(currentVehicle.getId());
			if(vehicleState.getHoldingTime()!=null)
			{
				if(vehicleState.getHoldingTime().getStopId().equals(stopId))
				{
					currentHoldingTimes.add(vehicleState.getHoldingTime());
				}
			}
		}
		return currentHoldingTimes;
	}
	protected HoldingTime getNextDepartureByHoldingTime(String currentVehicleId, List<HoldingTime> holdingTimes)
	{
		logger.debug("Looking for next departure by holding time for vehicle {}.", currentVehicleId );


		HoldingTime nextDeparture=null;
		for(HoldingTime holdingTime:holdingTimes)
		{

			if(!holdingTime.getVehicleId().equals(currentVehicleId) && holdingTime.isArrivalPredictionUsed()==false)
			{
				// Ignore predicted holding times in calculation
				if(holdingTime.isArrivalPredictionUsed()==false)
				{
					if(nextDeparture==null||holdingTime.getHoldingTime().before(nextDeparture.getHoldingTime()))
					{
						nextDeparture=holdingTime;
					}
				}else
				{
					logger.debug("Holding time not considered as it is a predicted holding time. {}", holdingTime);
				}
			}
		}
		return nextDeparture;
	}
	private IpcArrivalDeparture getLastVehicleDepartureTime(String currentVehicleId, String tripId, String stopId, Date time)
	{
		StopArrivalDepartureCacheKey currentStopKey=new StopArrivalDepartureCacheKey(stopId,time);

		List<IpcArrivalDeparture> currentStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(currentStopKey);

		IpcArrivalDeparture closestDepartureEvent=null;
		if(currentStopList!=null)
		{
			/* These are sorted when placed in cache */
			for(IpcArrivalDeparture event:currentStopList)
			{
				//if(event.isDeparture() && event.getTripId().equals(tripId)) TODO This is because of the GTFS config of Atlanta Streetcar. Could we use route_id instead?
				if(!event.getVehicleId().equals(currentVehicleId))
				{
					if(event.isDeparture() )
					{
						if(closestDepartureEvent==null)
							closestDepartureEvent=event;
						else if (time.getTime()-event.getTime().getTime() < time.getTime()-closestDepartureEvent.getTime().getTime())
						{
							closestDepartureEvent=event;
						}
					}
				}
			}
		}
		return closestDepartureEvent;
	}

	private ArrayList<String> getOtherVehiclesAtStop(String stopId, String currentVehicleId)
	{
		ArrayList<String> alsoAtStop=new ArrayList<String>();

		for(IpcVehicleComplete currentVehicle:VehicleDataCache.getInstance().getVehicles())
		{
			if(currentVehicle.isAtStop() && currentVehicle.getAtOrNextStopId().equals(stopId) && !currentVehicle.getId().equals(currentVehicleId))
			{
				alsoAtStop.add(currentVehicle.getId());
			}
		}

		return alsoAtStop;
	}

	private IpcArrivalDeparture getLastVehicleArrivalEvent(String stopid, String vehicleid, Date time)
	{
		StopArrivalDepartureCacheKey currentStopKey=new StopArrivalDepartureCacheKey(stopid,time);

		List<IpcArrivalDeparture> currentStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(currentStopKey);

		IpcArrivalDeparture closestArrivalEvent=null;

		if(currentStopList!=null)
		{
			for(IpcArrivalDeparture event:currentStopList)
			{
				if(event.getVehicleId().equals(vehicleid))
				{
					// if it arrives after the current time
					if(event.isArrival() && event.getStopId().equals(stopid) )
					{
						if(event.getTime().getTime()>time.getTime())
						{
							if(closestArrivalEvent==null )
							{
								closestArrivalEvent=event;
							}
							else if (Math.abs(time.getTime()-event.getTime().getTime()) < Math.abs(time.getTime()-closestArrivalEvent.getTime().getTime()))
							{
								closestArrivalEvent=event;
							}
						}
					}
				}
			}
		}
		return closestArrivalEvent;
	}
	public ArrayList<IpcArrivalDeparture> addArrivalTimesForVehiclesAtStop(String stopid, String vehicleid, Date time, ArrayList<Long> arrivalTimes)
	{

		ArrayList<IpcArrivalDeparture> events=new ArrayList<IpcArrivalDeparture>();
		for(String othervehicle:getOtherVehiclesAtStop(stopid, vehicleid))
		{
			IpcArrivalDeparture event = getLastVehicleArrivalEvent(stopid, othervehicle, time);

			if(event!=null)
			{
				arrivalTimes.add(event.getTime().getTime());
				events.add(event);
			}
		}
		Collections.sort(arrivalTimes);
		Collections.sort(events, new IpcArrivalDepartureComparator());
		return events;
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
	public HoldingTime generateHoldingTime(VehicleState vehicleState, IpcPrediction arrivalPrediction) {

		if(!useArrivalPredictions.getValue())
		{
			return null;
		}

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

			IpcArrivalDeparture lastDeparture = getLastVehicleDepartureTime(arrivalPrediction.getVehicleId(),arrivalPrediction.getTripId(), arrivalPrediction.getStopId(), new Date(arrivalPrediction.getPredictionTime()));
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
					if(lastDeparture.getTime().getTime()>forwardDeparturePrediction.getPredictionTime())
					{
						long holdingTimeValue=calculateHoldingTime(arrivalPrediction.getPredictionTime(), forwardDeparturePrediction.getPredictionTime() ,N, maxPredictionsForHoldingTimeCalculation.getValue());

						holdingTime=new HoldingTime(new Date(arrivalPrediction.getPredictionTime()+holdingTimeValue),  new Date(Core.getInstance().getSystemTime()), arrivalPrediction.getVehicleId(), arrivalPrediction.getStopId(), arrivalPrediction.getTripId(),
								arrivalPrediction.getRouteId(),true, false, new Date(arrivalPrediction.getPredictionTime()),  true, N.length);

						logger.debug("Holding time for : {} is {}.", arrivalPrediction.toString(), holdingTime);
						if(storeHoldingTimes.getValue())
							Core.getInstance().getDbLogger().add(holdingTime);

					}else if(lastDeparture.getTime().getTime()<=forwardDeparturePrediction.getPredictionTime())
					{
						long holdingTimeValue=calculateHoldingTime(arrivalPrediction.getPredictionTime(), lastDeparture.getTime().getTime() ,N, maxPredictionsForHoldingTimeCalculation.getValue());

						holdingTime=new HoldingTime(new Date(arrivalPrediction.getPredictionTime()+holdingTimeValue),  new Date(Core.getInstance().getSystemTime()), arrivalPrediction.getVehicleId(), arrivalPrediction.getStopId(), arrivalPrediction.getTripId(),
								arrivalPrediction.getRouteId(),true, false, new Date(arrivalPrediction.getPredictionTime()), true, N.length);

						logger.debug("Holding time for : {} is {}.", arrivalPrediction.toString(), holdingTime);
						if(storeHoldingTimes.getValue())
							Core.getInstance().getDbLogger().add(holdingTime);

					}
				}else if(forwardDeparturePrediction!=null)
				{
					long holdingTimeValue=calculateHoldingTime(arrivalPrediction.getPredictionTime(), forwardDeparturePrediction.getPredictionTime() ,N, maxPredictionsForHoldingTimeCalculation.getValue());

					holdingTime=new HoldingTime(new Date(arrivalPrediction.getPredictionTime()+holdingTimeValue),  new Date(Core.getInstance().getSystemTime()), arrivalPrediction.getVehicleId(), arrivalPrediction.getStopId(), arrivalPrediction.getTripId(),
							arrivalPrediction.getRouteId(), true, false, new Date(arrivalPrediction.getPredictionTime()), true, N.length);
					logger.debug("Holding time for : {} is {}.", arrivalPrediction.toString(), holdingTime);
					if(storeHoldingTimes.getValue())
						Core.getInstance().getDbLogger().add(holdingTime);

				}else if(lastDeparture!=null)
				{
					long holdingTimeValue=calculateHoldingTime(arrivalPrediction.getPredictionTime(), lastDeparture.getTime().getTime() ,N, maxPredictionsForHoldingTimeCalculation.getValue());



					holdingTime=new HoldingTime(new Date(arrivalPrediction.getPredictionTime()+holdingTimeValue),  new Date(Core.getInstance().getSystemTime()), arrivalPrediction.getVehicleId(), arrivalPrediction.getStopId(), arrivalPrediction.getTripId(),
							arrivalPrediction.getRouteId(), true, false, new Date(arrivalPrediction.getPredictionTime()), true, N.length);
					logger.debug("Holding time for : {} is {}.", arrivalPrediction.toString(), holdingTime);
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
						holdingTimeValue=calculateHoldingTime(Math.min(lastDeparture.getTime().getTime(), forwardDeparturePrediction.getPredictionTime()), lastDeparture.getTime().getTime());
					}else
					if(lastDeparture!=null&&forwardDeparturePrediction==null)
					{
						holdingTimeValue=calculateHoldingTime(arrivalPrediction.getPredictionTime(), lastDeparture.getTime().getTime() );
					}else
					if(lastDeparture==null&&forwardDeparturePrediction!=null)
					{
						holdingTimeValue=calculateHoldingTime(arrivalPrediction.getPredictionTime(), forwardDeparturePrediction.getPredictionTime() );
					}
					if(holdingTimeValue!=null)
					{
						holdingTime=new HoldingTime(new Date(arrivalPrediction.getPredictionTime()+holdingTimeValue),  new Date(Core.getInstance().getSystemTime()), arrivalPrediction.getVehicleId(), arrivalPrediction.getStopId(), arrivalPrediction.getTripId(),
								arrivalPrediction.getRouteId(), true, false, new Date(arrivalPrediction.getPredictionTime()), true, 0);

						logger.debug("Holding time for : {} is {}.", arrivalPrediction.toString(), holdingTime);

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
		IpcPrediction closestPrediction=null;
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
					closestPrediction=predictions.get(i);

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

		if(controlStopList.getValue() == null){
			return null;
		}

		ArrayList<ControlStop> controlStops=new ArrayList<ControlStop>();

		for(String stopEntry: controlStopList.getValue())
		{
			controlStops.add(new ControlStop(stopEntry));
		}
		return controlStops;
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
	ArrayList<Long> predictionsToLongArrayList(List<IpcPrediction> predictions, ArrayList<Long> list)
	{

		if(predictions!=null)
		{
			for(IpcPrediction prediction:predictions)
			{
				list.add(new Long(prediction.getPredictionTime()));
			}
		}
		Collections.sort(list);
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

	@Override
	public void handleDeparture(VehicleState vehicleState, ArrivalDeparture arrivalDeparture) {
		/* if it is a departure from a control stop */
		if(arrivalDeparture.isDeparture() && isControlStop(arrivalDeparture.getStopId()))
		{
			/* remove holding time once vehicle has left control point. */

			logger.debug("Removing holding time {} due to departure {}.",vehicleState.getHoldingTime(), arrivalDeparture);
			vehicleState.setHoldingTime(null);

			if(regenerateOnDeparture.getValue())
			{
				for(HoldingTime holdingTime:getCurrentHoldingTimesForStop(arrivalDeparture.getStopId()))
				{
					VehicleState otherState = VehicleStateManager.getInstance().getVehicleState(holdingTime.getVehicleId());

					IpcArrivalDeparture lastArrival = getLastVehicleArrivalEvent(arrivalDeparture.getStopId(), otherState.getVehicleId(), arrivalDeparture.getAvlTime());

					HoldingTime otherHoldingTime = HoldingTimeGeneratorFactory.getInstance().generateHoldingTime(otherState, lastArrival);

					HoldingTimeCache.getInstance().putHoldingTime(otherHoldingTime);

					otherState.setHoldingTime(otherHoldingTime);
				}
			}
		}
	}

}
