package org.transitclock.core.reporting;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.core.ServiceType;
import org.transitclock.core.ServiceUtils;
import org.transitclock.core.SpatialMatch;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.*;
import org.transitclock.db.structs.*;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.reporting.SpeedCalculator;
import org.transitclock.utils.IntervalTimer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RunTimeGenerator {

	private static final Logger logger =
			LoggerFactory.getLogger(RunTimeGenerator.class);

	private static boolean getUseScheduledArrivalDepartureValues() {
		return useScheduledArrivalDepartureValues.getValue();
	}
	private static BooleanConfigValue useScheduledArrivalDepartureValues =
			new BooleanConfigValue(
					"transitclock.reporting.useScheduledArrivalDepartureValues",
					false,
					"Optionally use scheduled arrival departure information when not available from realtime" +
							"data.");

	public  boolean generate(VehicleState vehicleState) {

		try {
			// Make sure match is valid
			SpatialMatch newMatch = vehicleState.getMatch();
			if(newMatch == null){
				logger.error("Vehicle was not matched when trying to process running times. {}", vehicleState);
				return false;
			}

			// Make sure we're at the last stop
			SpatialMatch matchAtPreviousStop = vehicleState.getMatch().getMatchAtPreviousStop();
			Integer lastStopIndex;
			if(matchAtPreviousStop==null || (lastStopIndex = atEndOfTrip(matchAtPreviousStop)) == null) {
				return false;
			}

			// Check to see if we already processed this run time
			SpatialMatch twoMatchesAgo = matchAtPreviousStop.getMatchAtPreviousStop();
			if(twoMatchesAgo == null || (lastStopIndex == twoMatchesAgo.getStopPathIndex() &&
					twoMatchesAgo.getStopPath().getStopId().equals(matchAtPreviousStop.getStopPath().getStopId()))){
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

		List<RunTimesForStops> runTimesForStops = new ArrayList<>();
		RunTimesForRoutes runTimesForRoutes = new RunTimesForRoutes();

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

						// Get scheduled time info for stopPath
						ScheduleTime prevStopScheduledTime = getScheduledTime(trip, arrivalDeparture.getStopPathIndex() - 1);
						ScheduleTime currentStopScheduledTime = getScheduledTime(trip, arrivalDeparture.getStopPathIndex());

						IpcArrivalDeparture prevDeparture =
								getPreviousDeparture(i, arrivalDeparture.getStopPathIndex(), arrivalDeparturesForStop);

						// Lookup stopPath for trip using ArrivalDeparture stopPathIndex
						StopPath stopPath = trip.getStopPath(arrivalDeparture.getStopPathIndex());


						// Run Time For Stops Properties
						int configRev = trip.getConfigRev();
						String stopId = arrivalDeparture.getStopId();
						int stopPathIndex = arrivalDeparture.getStopPathIndex();
						Date arrivalTime = arrivalDeparture.getTime();
						Date prevStopDepartureTime = getPrevStopDepartureTime(prevDeparture);
						Integer scheduledCurrentStopArrivalTime = getScheduledTime(currentStopScheduledTime);
						Integer scheduledPrevStopDepartureTime = getScheduledTime(prevStopScheduledTime);
						Long dwellTime = 0l;
						Boolean lastStop = true;
						Boolean timePoint = isTimePoint(stopPath);


						// Setting dwell time and stopPathLenght to help calculate speed since
						// we need to subtract dwell time to get arrival time and use stopPathLength
						// to get distance travelled
						arrivalDeparture.setDwellTime(dwellTime);
						arrivalDeparture.setStopPathLength(getStopPathLength(stopPath));
						Double speed = SpeedCalculator.getSpeedDataBetweenTwoArrivalDepartures(prevDeparture, arrivalDeparture);


						RunTimesForStops runTimesForStop = new RunTimesForStops(
								configRev,
								stopId,
								stopPathIndex,
								arrivalTime,
								prevStopDepartureTime,
								scheduledCurrentStopArrivalTime,
								scheduledPrevStopDepartureTime,
								dwellTime,
								speed,
								lastStop,
								timePoint
						);
						runTimesForStop.setRunTimesForRoutes(runTimesForRoutes);
						runTimesForStops.add(runTimesForStop);
					}
					else {

						continue;
					}
				}
				else if(finalStopArrivalTime != null && isMiddleStopForTrip(arrivalDeparture.getStopPathIndex(), lastStopIndex)){
					if(arrivalDeparture.isDeparture()){
						if(totalDwellTime != null && arrivalDeparture.getDwellTime() != null &&
								dwellTimeCount == arrivalDeparture.getStopPathIndex()){

							// Get scheduled time info for stopPath
							ScheduleTime prevStopScheduledTime = getScheduledTime(trip, arrivalDeparture.getStopPathIndex() - 1);
							ScheduleTime currentStopScheduledTime = getScheduledTime(trip, arrivalDeparture.getStopPathIndex());

							IpcArrivalDeparture prevDeparture =
									getPreviousDeparture(i, arrivalDeparture.getStopPathIndex(), arrivalDeparturesForStop);

							// Lookup stopPath for trip using ArrivalDeparture stopPathIndex
							StopPath stopPath = trip.getStopPath(arrivalDeparture.getStopPathIndex());

							// Run Time For Stops Properties
							int configRev = trip.getConfigRev();
							String stopId = arrivalDeparture.getStopId();
							int stopPathIndex = arrivalDeparture.getStopPathIndex();
							Date departureTime = arrivalDeparture.getTime();
							Date prevStopDepartureTime = getPrevStopDepartureTime(prevDeparture);
							Integer scheduledCurrentStopDepartureTime = getScheduledTime(currentStopScheduledTime);
							Integer scheduledPrevStopDepartureTime = getScheduledTime(prevStopScheduledTime);
							Long dwellTime = getDwellTime(arrivalDeparture, currentStopScheduledTime);
							Boolean lastStop = false;
							Boolean timePoint = isTimePoint(stopPath);

							// Setting dwell time and stopPathLenght to help calculate speed since
							// we need to subtract dwell time to get arrival time and use stopPathLength
							// to get distance travelled
							arrivalDeparture.setDwellTime(dwellTime);
							arrivalDeparture.setStopPathLength(getStopPathLength(stopPath));
							Double speed = SpeedCalculator.getSpeedDataBetweenTwoArrivalDepartures(prevDeparture, arrivalDeparture);

							RunTimesForStops runTimesForStop = new RunTimesForStops(
									configRev,
									stopId,
									stopPathIndex,
									departureTime,
									prevStopDepartureTime,
									scheduledCurrentStopDepartureTime,
									scheduledPrevStopDepartureTime,
									dwellTime,
									speed,
									lastStop,
									timePoint
							);
							runTimesForStop.setRunTimesForRoutes(runTimesForRoutes);
							runTimesForStops.add(runTimesForStop);

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
				else if(arrivalDeparture.isDeparture() && finalStopArrivalTime != null && i == 0){
					boolean isFirstStop = isFirstStopForTrip(arrivalDeparture.getStopPathIndex());

					ServiceUtils serviceUtils = Core.getInstance().getServiceUtils();
					ServiceType serviceType = serviceUtils.getServiceTypeForTrip(arrivalDeparture.getTime(),
																				 trip.getStartTime(),
																				 trip.getServiceId());

					// Get scheduled time info for stopPath
					ScheduleTime currentStopScheduledTime = getScheduledTime(trip, arrivalDeparture.getStopPathIndex());

					// Lookup stopPath for trip using ArrivalDeparture stopPathIndex
					StopPath stopPath = trip.getStopPath(arrivalDeparture.getStopPathIndex());

					// Run Time For Stops Properties
					int configRev = trip.getConfigRev();
					String stopId = arrivalDeparture.getStopId();
					int stopPathIndex = arrivalDeparture.getStopPathIndex();
					Date departureTime = arrivalDeparture.getTime();
					Date prevStopDepartureTime = null;
					Integer scheduledCurrentStopDepartureTime = getScheduledTime(currentStopScheduledTime);
					Integer scheduledPrevStopDepartureTime = null;
					Boolean lastStop = false;
					Boolean timePoint = isTimePoint(stopPath);
					Long dwellTime = getDwellTime(arrivalDeparture, currentStopScheduledTime);
					Double speed = null;

					// Update total dwell time
					if(isFirstStop && totalDwellTime != null && dwellTimeCount == arrivalDeparture.getStopPathIndex()){
						totalDwellTime += dwellTime;
					} else {
						totalDwellTime = null;
					}

					// Setting first stop properties
					if(isFirstStop){
						speed = 0.0;
						scheduledPrevStopDepartureTime = 0;
					}

					RunTimesForStops runTimesForStop = new RunTimesForStops(
							configRev,
							stopId,
							stopPathIndex,
							departureTime,
							prevStopDepartureTime,
							scheduledCurrentStopDepartureTime,
							scheduledPrevStopDepartureTime,
							dwellTime,
							speed,
							lastStop,
							timePoint
					);

					runTimesForStops.add(runTimesForStop);
					runTimesForStop.setRunTimesForRoutes(runTimesForRoutes);

					runTimesForRoutes.setConfigRev(trip.getConfigRev());
					runTimesForRoutes.setServiceId(trip.getServiceId());
					runTimesForRoutes.setDirectionId(trip.getDirectionId());
					runTimesForRoutes.setRouteShortName(trip.getRouteShortName());
					runTimesForRoutes.setTripPatternId(trip.getTripPattern().getId());
					runTimesForRoutes.setTripId(trip.getId());
					runTimesForRoutes.setHeadsign(trip.getHeadsign());
					runTimesForRoutes.setStartTime(arrivalDeparture.getTime());
					runTimesForRoutes.setEndTime(finalStopArrivalTime);
					runTimesForRoutes.setScheduledStartTime(trip.getStartTime());
					runTimesForRoutes.setScheduledEndTime(trip.getEndTime());
					runTimesForRoutes.setNextTripStartTime(getNextTripStartTime(trip));
					runTimesForRoutes.setVehicleId(vehicleId);
					runTimesForRoutes.setServiceType(serviceType);
					runTimesForRoutes.setDwellTime(totalDwellTime);
					runTimesForRoutes.setRunTimesForStops(runTimesForStops);
					runTimesForRoutes.setStartStopPathIndex(arrivalDeparture.getStopPathIndex());

					Core.getInstance().getDbLogger().add(runTimesForRoutes);
					logger.debug("Processing Run Times for Route took {} msec", timer.elapsedMsec());
					return true;
				}
			}
		}
		return false;
	}

	private float getStopPathLength(StopPath stopPath){
		return (float) stopPath.getLength();
	}

	private boolean isTimePoint(StopPath stopPath){
		return stopPath.isScheduleAdherenceStop();
	}

	private Date getPrevStopDepartureTime(IpcArrivalDeparture prevDeparture){
		if(prevDeparture != null){
			return prevDeparture.getTime();
		}
		return null;
	}

	private Integer getScheduledTime(ScheduleTime scheduleTime) {
		if(scheduleTime != null){
			return scheduleTime.getTime();
		}
		return null;
	}

	private IpcArrivalDeparture getPreviousDeparture(int arrivalDepartureIndex,
															int currentStopPathIndex,
															List<IpcArrivalDeparture> arrivalDepartures){

		if(arrivalDepartureIndex > 0){
			int prevStopPathIndex = currentStopPathIndex - 1;
			int currentArrivalDepartureIndex = arrivalDepartureIndex + 1;

			IpcArrivalDeparture prevArrivalDeparture = arrivalDepartures.get(currentArrivalDepartureIndex);

			while(currentArrivalDepartureIndex > 0 &&
					(prevStopPathIndex == prevArrivalDeparture.getStopPathIndex() ||
							currentStopPathIndex == prevArrivalDeparture.getStopPathIndex())){
				if(prevStopPathIndex == prevArrivalDeparture.getStopPathIndex() && prevArrivalDeparture.isDeparture()){
					return prevArrivalDeparture;
				}
				++currentArrivalDepartureIndex;
				prevArrivalDeparture = arrivalDepartures.get(currentArrivalDepartureIndex);
			}
		}
		return null;
	}

	private IpcArrivalDeparture getNextArrival(int arrivalDepartureIndex,
														int currentStopPathIndex,
														List<IpcArrivalDeparture> arrivalDepartures){
		if(arrivalDepartureIndex < arrivalDepartures.size()){

			int nextStopPathIndex = currentStopPathIndex + 1;
			int currentArrivalDepartureIndex = arrivalDepartureIndex + 1;

			IpcArrivalDeparture nextArrivalDeparture = arrivalDepartures.get(currentArrivalDepartureIndex);

			while(currentArrivalDepartureIndex < arrivalDepartures.size() &&
					(nextStopPathIndex == nextArrivalDeparture.getStopPathIndex() ||
							currentStopPathIndex == nextArrivalDeparture.getStopPathIndex())){
				if(nextStopPathIndex == nextArrivalDeparture.getStopPathIndex() && nextArrivalDeparture.isArrival()){
					return nextArrivalDeparture;
				}
				++currentArrivalDepartureIndex;
				nextArrivalDeparture = arrivalDepartures.get(currentArrivalDepartureIndex);
			}
		}
		return null;
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

	public ScheduleTime getScheduledTime(Trip trip, int stopPathIndex){
		if (!trip.isNoSchedule()) {
			return trip.getScheduleTime(stopPathIndex);
		}
		return null;
	}

	public Long getDwellTime(IpcArrivalDeparture arrivalDeparture, ScheduleTime scheduleTime){
		if(arrivalDeparture.getDwellTime() != null){
			return arrivalDeparture.getDwellTime();
		} else if(getUseScheduledArrivalDepartureValues() && scheduleTime != null &&
				scheduleTime.getArrivalTime() != null && scheduleTime.getDepartureTime() != null){
			return TimeUnit.SECONDS.toMillis(Long.valueOf((scheduleTime.getDepartureTime() - scheduleTime.getArrivalTime())));
		}
		return null;
	}
}
