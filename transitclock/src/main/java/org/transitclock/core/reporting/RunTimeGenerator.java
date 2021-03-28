package org.transitclock.core.reporting;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.ServiceType;
import org.transitclock.core.ServiceUtils;
import org.transitclock.core.SpatialMatch;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.*;
import org.transitclock.db.structs.*;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.utils.IntervalTimer;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RunTimeGenerator {

	private static final Logger logger =
			LoggerFactory.getLogger(RunTimeGenerator.class);

	public  boolean generate(VehicleState vehicleState) {

		try {
			SpatialMatch matchAtPreviousStop = vehicleState.getMatch().getMatchAtPreviousStop();
			Integer lastStopIndex;
			if(matchAtPreviousStop==null || (lastStopIndex = atEndOfTrip(matchAtPreviousStop)) == null) {
				return false;
			}

			String tripId = matchAtPreviousStop.getAtStop().getTrip().getId();
			long vehicleMatchAvlTime = vehicleState.getMatch().getAvlTime();
			String vehicleId= vehicleState.getVehicleId();
			Integer tripStartTime = matchAtPreviousStop.getTrip().getStartTime();


			List<IpcArrivalDeparture> arrivalDeparturesForStop = getArrivalDeparturesForTrip(tripId,
					vehicleMatchAvlTime, tripStartTime);

			if(arrivalDeparturesForStop!=null)
			{
				boolean isValid = processRunTimesForEndOfTrip( vehicleId,
																arrivalDeparturesForStop,
																matchAtPreviousStop,
																lastStopIndex
															 );
				return isValid;
			}
		} catch (Exception e) {
			logger.error("Exception when processing run times", e);
		}
		return false;
	}

	public List<IpcArrivalDeparture> getArrivalDeparturesForTrip(String tripId, long vehicleMatchAvlTime, Integer startTime){
		Date nearestDay = DateUtils.truncate(new Date(vehicleMatchAvlTime), Calendar.DAY_OF_MONTH);
		TripKey key=new TripKey(tripId, nearestDay, startTime);
		return TripDataHistoryCacheFactory.getInstance().getTripHistory(key);
	}

	public boolean processRunTimesForEndOfTrip( String vehicleId,
												List<IpcArrivalDeparture> arrivalDeparturesForStop,
											   	SpatialMatch matchAtPreviousStop,
											    Integer lastStopIndex){



		if(!isRunTimeValid(arrivalDeparturesForStop, lastStopIndex)){
			return false;
		}

		Trip trip = matchAtPreviousStop.getTrip();
		int dwellTimeCount = lastStopIndex;
		Long totalDwellTime = null;
		Date finalStopArrivalTime = null;

		IntervalTimer timer = new IntervalTimer();
		for(int i=0;i<arrivalDeparturesForStop.size(); i++)
		{
			IpcArrivalDeparture arrivalDeparture = arrivalDeparturesForStop.get(i);

			if(isSpatialMatchAndArrivalDepartureMatch(arrivalDeparture, matchAtPreviousStop, vehicleId))
			{
				if(isLastStopForTrip(arrivalDeparture.getStopPathIndex(), lastStopIndex)){
					if(arrivalDeparture.isArrival()){
						finalStopArrivalTime = arrivalDeparture.getTime();
						totalDwellTime = 0l;
						--dwellTimeCount;
					}
					else {
						continue;
					}
				}
				else if(finalStopArrivalTime != null && isMiddleStopForTrip(arrivalDeparture.getStopPathIndex(), lastStopIndex)){
					if(arrivalDeparture.isDeparture()){
						if(totalDwellTime != null &&
							arrivalDeparture.getDwellTime() != null &&
							dwellTimeCount == arrivalDeparture.getStopPathIndex()){
							totalDwellTime += arrivalDeparture.getDwellTime();
							--dwellTimeCount;
						}
						else {
							totalDwellTime = null;
						}

					}
					else {
						continue;
					}
				}
				else if(finalStopArrivalTime != null && isFirstStopForTrip(arrivalDeparture.getStopPathIndex())){
					if(totalDwellTime != null && dwellTimeCount == arrivalDeparture.getStopPathIndex()){
						totalDwellTime += arrivalDeparture.getDwellTime();
					} else{
						totalDwellTime = null;
					}
					ServiceUtils serviceUtils = Core.getInstance().getServiceUtils();
					ServiceType serviceType = serviceUtils.getServiceTypeForTrip(arrivalDeparture.getTime(),
																				 trip.getStartTime(),
																				 trip.getServiceId());
					RunTimesForRoutes runTimesForRoutes = new RunTimesForRoutes(
							trip.getConfigRev(),
							trip.getServiceId(),
							trip.getDirectionId(),
							trip.getRouteShortName(),
							trip.getTripPattern().getId(),
							trip.getId(),
							trip.getHeadsign(),
							arrivalDeparture.getTime(),
							finalStopArrivalTime,
							trip.getStartTime(),
							trip.getEndTime(),
							getNextTripStartTime(trip),
							vehicleId,
							serviceType,
							totalDwellTime);

					Core.getInstance().getDbLogger().add(runTimesForRoutes);
					logger.debug("Processing Run Times for Route took {} msec", timer.elapsedMsec());
					return true;
				}
			}
		}
		return false;
	}

	public boolean isRunTimeValid(List<IpcArrivalDeparture> arrivalDeparturesForStop, int lastStopIndex){
		if(arrivalDeparturesForStop == null ||
				arrivalDeparturesForStop.size() < 2 ||
				arrivalDeparturesForStop.get(0).getStopPathIndex() != lastStopIndex ||
				arrivalDeparturesForStop.get(arrivalDeparturesForStop.size()-1).getStopPathIndex() != 0){
			return false;
		}
		return true;
	}

	private boolean isSpatialMatchAndArrivalDepartureMatch(IpcArrivalDeparture arrivalDeparture,
														   SpatialMatch matchAtPreviousStop,
														   String vehicleId){
		return arrivalDeparture.getTripId().equals(matchAtPreviousStop.getTrip().getId()) &&
				arrivalDeparture.getVehicleId().equals(vehicleId);
				//matchAtPreviousStop.getTripIndex() == arrivalDeparture.getTripIndex();
	}

	private boolean isLastStopForTrip(Integer currentStopPathIndex, Integer lastStopIndex){
		return currentStopPathIndex == lastStopIndex;
	}

	private boolean isMiddleStopForTrip(Integer currentStopPathIndex, Integer lastStopIndex){
		return currentStopPathIndex > 0 && currentStopPathIndex < lastStopIndex;
	}

	private boolean isFirstStopForTrip(Integer currentStopPathIndex){
		return currentStopPathIndex == 0;
	}

	public Integer atEndOfTrip(SpatialMatch matchAtPreviousStop) {
		Block block = matchAtPreviousStop.getBlock();
		int tripIndex = matchAtPreviousStop.getTripIndex();
		int stopPathIndex = matchAtPreviousStop.getStopPathIndex();
		if(stopPathIndex == block.numStopPaths(tripIndex) - 1){
			return stopPathIndex;
		}
		return null;
	}

	public Integer getNextTripStartTime(Trip trip){
		if(trip.getBlock().getTrip(trip.getIndexInBlock() + 1) != null){
			return trip.getStartTime();
		}
		return null;
	}
}
