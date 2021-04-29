package org.transitclock.core.reporting;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.core.*;
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

	public boolean generate(VehicleState vehicleState) {
		try {
			// check that vehicle state is valid
			if(!isVehicleStateValid(vehicleState)){
				return false;
			}

			// check that this is a valid run time candidate
			TemporalMatch match = vehicleState.getMatch();
			TemporalMatch prevMatch = vehicleState.getPreviousMatch();
			if(!hasCrossedToNextTrip(match, prevMatch)){
				return false;
			}

			// Lookup Arrivals Departures for Trip
			String tripId = prevMatch.getTrip().getId();
			long vehicleMatchAvlTime = prevMatch.getAvlTime();
			Integer tripStartTime = prevMatch.getTrip().getStartTime();

			List<IpcArrivalDeparture> arrivalDeparturesForStop = getArrivalDeparturesForTrip(tripId,
					vehicleMatchAvlTime, tripStartTime);

			if(!isArrivalDeparturesValid(arrivalDeparturesForStop)){
				return false;
			}

			int lastStopIndex = getLastStopIndex(prevMatch);
			String vehicleId= vehicleState.getVehicleId();
			Trip trip = prevMatch.getTrip();
			return processRunTimesForTrip(vehicleId, trip, arrivalDeparturesForStop, prevMatch, match, lastStopIndex);

		} catch (Exception e) {
			logger.error("Exception when processing run times", e);
		}
		return false;
	}

	private boolean isVehicleStateValid(VehicleState vehicleState){
		return vehicleState.isPredictable() &&
				vehicleState.getMatch() != null &&
				vehicleState.getMatch().getMatchAtPreviousStop() != null;
	}


	private boolean hasCrossedToNextTrip(TemporalMatch currentMatch,
										 TemporalMatch previousMatch) {

		if(currentMatch != null && previousMatch != null &&
				previousMatch.getTripIndex() < currentMatch.getTripIndex() &&
				previousMatch.getBlock().getId().equals(currentMatch.getBlock().getId())){
			return true;
		}
		return false;
	}

	private int getLastStopIndex(SpatialMatch matchAtPreviousStop){
		Block block = matchAtPreviousStop.getBlock();
		int tripIndex = matchAtPreviousStop.getTripIndex();
		return block.numStopPaths(tripIndex) - 1;
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

	public List<IpcArrivalDeparture> getArrivalDeparturesForTrip(String tripId, long vehicleMatchAvlTime, Integer startTime){
		Date nearestDay = DateUtils.truncate(new Date(vehicleMatchAvlTime), Calendar.DAY_OF_MONTH);
		TripKey key=new TripKey(tripId, nearestDay, startTime);
		return TripDataHistoryCacheFactory.getInstance().getTripHistory(key);
	}

	public boolean processRunTimesForTrip(String vehicleId,
										  Trip trip,
										  List<IpcArrivalDeparture> arrivalDeparturesForStop,
										  TemporalMatch matchAtPreviousStop,
										  TemporalMatch matchAtCurrentStop,
										  Integer lastStopIndex){


		int dwellTimeCount = lastStopIndex;
		Long totalDwellTime = null;
		Date firstStopDepartureTime = null;
		Date finalStopArrivalTime = null;
		Integer firstStopPathIndex = null;
		Integer finalStopPathIndex = null;

		List<RunTimesForStops> runTimesForStops = new ArrayList<>();
		RunTimesForRoutes runTimesForRoutes = new RunTimesForRoutes();

		IntervalTimer timer = new IntervalTimer();
		for(int i=0;i<arrivalDeparturesForStop.size(); i++)
		{
			IpcArrivalDeparture arrivalDeparture = arrivalDeparturesForStop.get(i);

			if(isSpatialMatchAndArrivalDepartureMatch(arrivalDeparture, trip.getId(), vehicleId))
			{
				if(finalStopArrivalTime == null){
					if(arrivalDeparture.isArrival()){
						finalStopArrivalTime = arrivalDeparture.getTime();
						finalStopPathIndex = arrivalDeparture.getStopPathIndex();
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
						String stopPathId = getStopPathId(stopPath);
						int stopPathIndex = arrivalDeparture.getStopPathIndex();
						Date arrivalTime = arrivalDeparture.getTime();
						Date prevStopDepartureTime = getPrevStopDepartureTime(prevDeparture);
						Integer scheduledCurrentStopArrivalTime = getScheduledTime(currentStopScheduledTime);
						Integer scheduledPrevStopDepartureTime = getScheduledTime(prevStopScheduledTime);
						Long dwellTime = 0l;
						Boolean lastStop = true;
						Boolean timePoint = isTimePoint(stopPath);


						// Setting dwell time and stopPathLength to help calculate speed since
						// we need to subtract dwell time to get arrival time and use stopPathLength
						// to get distance travelled
						arrivalDeparture.setDwellTime(dwellTime);
						arrivalDeparture.setStopPathLength(getStopPathLength(stopPath));
						Double speed = SpeedCalculator.getSpeedDataBetweenTwoArrivalDepartures(prevDeparture, arrivalDeparture);


						RunTimesForStops runTimesForStop = new RunTimesForStops(
								configRev,
								stopPathId,
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
				else if(finalStopArrivalTime != null &&
						isNotLastStopForTrip(arrivalDeparture.getStopPathIndex(), lastStopIndex) &&
						i < arrivalDeparturesForStop.size()-1){
					if(arrivalDeparture.isDeparture()){
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
						String stopPathId = getStopPathId(stopPath);
						int stopPathIndex = arrivalDeparture.getStopPathIndex();
						Date prevStopDepartureTime = getPrevStopDepartureTime(prevDeparture);
						Integer scheduledCurrentStopDepartureTime = getScheduledTime(currentStopScheduledTime);
						Integer scheduledPrevStopDepartureTime = getScheduledTime(prevStopScheduledTime);
						Long dwellTime = getDwellTime(arrivalDeparture, currentStopScheduledTime);
						Boolean lastStop = false;
						Boolean timePoint = isTimePoint(stopPath);
						Date departureTime = arrivalDeparture.getTime();

						// Setting dwell time and stopPathLenght to help calculate speed since
						// we need to subtract dwell time to get arrival time and use stopPathLength
						// to get distance travelled
						arrivalDeparture.setDwellTime(dwellTime);
						arrivalDeparture.setStopPathLength(getStopPathLength(stopPath));
						Double speed = SpeedCalculator.getSpeedDataBetweenTwoArrivalDepartures(prevDeparture, arrivalDeparture);

						// Setting first stop properties
						boolean isFirstStop = isFirstStopForTrip(arrivalDeparture.getStopPathIndex());
						if(isFirstStop){
							speed = 0.0;
							scheduledPrevStopDepartureTime = 0;
						}

						RunTimesForStops runTimesForStop = new RunTimesForStops(
								configRev,
								stopPathId,
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

						firstStopDepartureTime = arrivalDeparture.getTime();
						firstStopPathIndex = arrivalDeparture.getStopPathIndex();

						// Process Total Dwell Time
						if(totalDwellTime != null && dwellTime != null &&
								dwellTimeCount == arrivalDeparture.getStopPathIndex()) {
							totalDwellTime += arrivalDeparture.getDwellTime();
							--dwellTimeCount;
						} else {
							totalDwellTime = null;
						}
					}
					else {
						continue;
					}
				}
				else if(finalStopArrivalTime != null && i == arrivalDeparturesForStop.size()-1){

					if(arrivalDeparture.isDeparture()) {

						boolean isFirstStop = isFirstStopForTrip(arrivalDeparture.getStopPathIndex());

						// Get scheduled time info for stopPath
						ScheduleTime currentStopScheduledTime = getScheduledTime(trip, arrivalDeparture.getStopPathIndex());

						// Lookup stopPath for trip using ArrivalDeparture stopPathIndex
						StopPath stopPath = trip.getStopPath(arrivalDeparture.getStopPathIndex());


						// Run Time For Stops Properties
						int configRev = trip.getConfigRev();
						String stopId = arrivalDeparture.getStopId();
						String stopPathId = getStopPathId(stopPath);
						int stopPathIndex = arrivalDeparture.getStopPathIndex();
						Date prevStopDepartureTime = null;
						Integer scheduledCurrentStopDepartureTime = getScheduledTime(currentStopScheduledTime);
						Integer scheduledPrevStopDepartureTime = null;
						Boolean lastStop = false;
						Boolean timePoint = isTimePoint(stopPath);
						Long dwellTime = getDwellTime(arrivalDeparture, currentStopScheduledTime);
						Double speed = null;
						Date departureTime = arrivalDeparture.getTime();


						// Setting first stop properties
						if(isFirstStop){
							speed = 0.0;
							scheduledPrevStopDepartureTime = 0;
						}

						firstStopDepartureTime = arrivalDeparture.getTime();
						firstStopPathIndex = arrivalDeparture.getStopPathIndex();

						RunTimesForStops runTimesForStop = new RunTimesForStops(
								configRev,
								stopPathId,
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

						if(totalDwellTime != null && dwellTime != null &&
								dwellTimeCount == arrivalDeparture.getStopPathIndex()) {
							totalDwellTime += arrivalDeparture.getDwellTime();
							--dwellTimeCount;
						} else {
							totalDwellTime = null;
						}
					}

					// Confirm totalDwellTime count is valid
					if (dwellTimeCount >= 0 && dwellTimeCount != arrivalDeparture.getStopPathIndex()) {
						totalDwellTime = null;
					}

					// Process Run Times for Route
					ServiceUtils serviceUtils = Core.getInstance().getServiceUtils();
					ServiceType serviceType = serviceUtils.getServiceTypeForTrip(arrivalDeparture.getTime(),
							trip.getStartTime(), trip.getServiceId());

					runTimesForRoutes.setConfigRev(trip.getConfigRev());
					runTimesForRoutes.setServiceId(trip.getServiceId());
					runTimesForRoutes.setDirectionId(trip.getDirectionId());
					runTimesForRoutes.setRouteShortName(trip.getRouteShortName());
					runTimesForRoutes.setTripPatternId(trip.getTripPattern().getId());
					runTimesForRoutes.setTripId(trip.getId());
					runTimesForRoutes.setHeadsign(trip.getHeadsign());
					runTimesForRoutes.setStartTime(firstStopDepartureTime);
					runTimesForRoutes.setEndTime(finalStopArrivalTime);
					runTimesForRoutes.setScheduledStartTime(trip.getStartTime());
					runTimesForRoutes.setScheduledEndTime(trip.getEndTime());
					runTimesForRoutes.setNextTripStartTime(getNextTripStartTime(trip));
					runTimesForRoutes.setVehicleId(vehicleId);
					runTimesForRoutes.setServiceType(serviceType);
					runTimesForRoutes.setDwellTime(totalDwellTime);
					runTimesForRoutes.setStartStopPathIndex(firstStopPathIndex);
					runTimesForRoutes.setActualLastStopPathIndex(finalStopPathIndex);
					runTimesForRoutes.setExpectedLastStopPathIndex(lastStopIndex);
					runTimesForRoutes.setRunTimesForStops(runTimesForStops);

					if(matchAtPreviousStop != null){
						logger.debug("Previous Match {}", matchAtPreviousStop.toString());
					}
					if(matchAtPreviousStop != null){
						logger.debug("Current Match {}", matchAtCurrentStop.toString());
					}

					logger.debug("{} with {} stops", runTimesForRoutes.toString(), runTimesForStops.size());

					Core.getInstance().getDbLogger().add(runTimesForRoutes);
					logger.debug("Processing Run Times for Route took {} msec", timer.elapsedMsecStr());
					return true;
				}
			}
		}
		return false;
	}

	private Date getArrivalTimeFromDepartureTime(IpcArrivalDeparture departure) {
		if(departure.getDwellTime() != null){
			long departureTime = departure.getTime().getTime();
			return new Date(departureTime - departure.getDwellTime());
		}
		return null;
	}

	private String getStopPathId(StopPath stopPath){
		if(stopPath != null){
			return stopPath.getId();
		}
		return null;
	}

	private float getStopPathLength(StopPath stopPath){
		if(stopPath != null){
			return (float) stopPath.getLength();
		}
		return Float.NaN;
	}

	private Boolean isTimePoint(StopPath stopPath){
		if(stopPath != null) {
			return stopPath.isScheduleAdherenceStop();
		}
		return null;
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
		try {
			if (currentStopPathIndex > 0 && arrivalDepartureIndex < arrivalDepartures.size() - 1) {

				int prevStopPathIndex = currentStopPathIndex - 1;
				int currentArrivalDepartureIndex = arrivalDepartureIndex + 1;

				IpcArrivalDeparture currentPrevArrivalDeparture = arrivalDepartures.get(currentArrivalDepartureIndex);

				boolean arrivalDepartureIndexInBounds = currentArrivalDepartureIndex < arrivalDepartures.size() - 1;

				while (arrivalDepartureIndexInBounds && currentPrevArrivalDeparture.getStopPathIndex() >= prevStopPathIndex) {

					if (prevStopPathIndex == currentPrevArrivalDeparture.getStopPathIndex() && currentPrevArrivalDeparture.isDeparture()) {
						return currentPrevArrivalDeparture;
					}
					++currentArrivalDepartureIndex;

					// Update while loop conditions
					arrivalDepartureIndexInBounds = currentArrivalDepartureIndex < arrivalDepartures.size();

					if(arrivalDepartureIndexInBounds){
						currentPrevArrivalDeparture = arrivalDepartures.get(currentArrivalDepartureIndex);
					}

				}
			}
		} catch(Exception e){
			logger.error("Unable to retrieve Previous Departure", e);
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


	public boolean isArrivalDeparturesValid(List<IpcArrivalDeparture> arrivalDeparturesForStop){
		if(arrivalDeparturesForStop == null || arrivalDeparturesForStop.size() < 2){
			return false;
		}
		return true;
	}

	private boolean isSpatialMatchAndArrivalDepartureMatch(IpcArrivalDeparture arrivalDeparture,
														   String tripId,
														   String vehicleId){
		return arrivalDeparture.getTripId().equals(tripId) &&
				arrivalDeparture.getVehicleId().equals(vehicleId);
	}

	private boolean isNotLastStopForTrip(Integer currentStopPathIndex, Integer lastStopIndex){
		return currentStopPathIndex < lastStopIndex;
	}

	private boolean isFirstStopForTrip(Integer currentStopPathIndex){
		return currentStopPathIndex == 0;
	}

	public Integer getNextTripStartTime(Trip trip){
		Trip nextTrip = trip.getBlock().getTrip(trip.getIndexInBlock() + 1);
		if(nextTrip != null){
			return nextTrip.getStartTime();
		}
		return null;
	}

	public ScheduleTime getScheduledTime(Trip trip, int stopPathIndex){
		if (stopPathIndex >= 0 && !trip.isNoSchedule()) {
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
