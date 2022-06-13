package org.transitclock.core.reporting;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.configData.CoreConfig;
import org.transitclock.core.*;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.TripDataHistoryCacheFactory;
import org.transitclock.core.dataCache.TripKey;
import org.transitclock.db.structs.*;
import org.transitclock.ipc.data.IpcArrivalDeparture;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RunTimeGenerator {

	private static final Logger logger =
			LoggerFactory.getLogger(RunTimeGenerator.class);

	static boolean getUseScheduledArrivalDepartureValues() {
		return useScheduledArrivalDepartureValues.getValue();
	}
	private static BooleanConfigValue useScheduledArrivalDepartureValues =
			new BooleanConfigValue(
					"transitclock.reporting.useScheduledArrivalDepartureValues",
					false,
					"Optionally use scheduled arrival departure information when not available from realtime" +
							"data.");

	private RunTimeProcessor processor = new RunTimeProcessor();

	public boolean generate(VehicleState vehicleState) {
		try {
			// check that vehicle state is valid
			if(!isVehicleStateValid(vehicleState)){
				return false;
			}

			TemporalMatch currentMatch = vehicleState.getMatch();

			// check that it crossed to the next trip
			// if it crossed to next trip continue
			// otherwise update last known block and trip idx for vehicle
			boolean crossedToNextTrip = hasCrossedToNextTrip(vehicleState, currentMatch);
			updateVehicleLastKnownBlockAndTrip(vehicleState, currentMatch);
			if(!crossedToNextTrip){
				return false;
			}

			// Look for prevMatch
			// This is the trip we are trying to generate runTimes for
			TemporalMatch prevMatch = vehicleState.getPreviousValidMatch();
			if(prevMatch == null){
				return false;
			}

			// Lookup ArrivalsDepartures for Trip
			// If no valid ArrivalsDepartures then return
			String routeId = prevMatch.getRoute().getId();
			String directionId = prevMatch.getTrip().getDirectionId();
			long vehicleMatchAvlTime = prevMatch.getAvlTime();
			Integer tripStartTime = prevMatch.getTrip().getStartTime();

			List<IpcArrivalDeparture> arrivalDeparturesForStop = getArrivalDeparturesForTrip(routeId,
					directionId, vehicleMatchAvlTime, tripStartTime);

			if(!isArrivalDeparturesValid(arrivalDeparturesForStop)){
				return false;
			}

			int lastStopIndex = getLastStopIndex(prevMatch);
			String vehicleId= vehicleState.getVehicleId();
			Trip trip = prevMatch.getTrip();
			Block block = trip.getBlock();

			ServiceUtilsImpl serviceUtils = Core.getInstance().getServiceUtils();
			return processor.processRunTimesForTrip(vehicleId, trip, block, arrivalDeparturesForStop,
					lastStopIndex, serviceUtils);

		} catch (Exception e) {
			logger.error("Exception when processing run times", e);
		}
		return false;
	}

	private void updateVehicleLastKnownBlockAndTrip(VehicleState vehicleState, TemporalMatch currentMatch) {
		if(currentMatch != null){
			if(!vehicleState.getBlock().getId().equals(vehicleState.getLastBlockId())){
				vehicleState.setLastBlockId(currentMatch.getBlock().getId());
			}
			vehicleState.setLastTripIndex(currentMatch.getTripIndex());
		}
	}

	boolean hasCrossedToNextTrip(VehicleState vehicleState, TemporalMatch currentMatch) {
		Block currentMatchBlock = currentMatch.getBlock();
		Integer currentTripIndex = currentMatch.getTripIndex();
		String lastValidBlockId = vehicleState.getLastBlockId();
		Integer lastValidTripIndex = vehicleState.getLastTripIndex();
		VehicleAtStopInfo atStopInfo = currentMatch.getAtStop();

		// 1 - Check if at end of block
		if (atStopInfo != null && atStopInfo.atEndOfBlock()){
			return true;
		}

		// Check to make sure have current match, lastValidBlockId and lastValidTripIndex
		if(currentMatchBlock != null && lastValidBlockId != null && lastValidTripIndex != null){

			// 2 - Check if same block but transitioned to next trip
			if(currentMatchBlock.getId().equals(lastValidBlockId) && currentTripIndex > lastValidTripIndex) {
				logger.debug("Found trip transition with vehicleState lastTripIndex {} and vehicleState lastBlockId {} " +
						"and currentMatch tripIndex {}", lastValidTripIndex, lastValidBlockId, currentTripIndex);
				return true;
			}

			// 3 - Check if transitioned to new block (in case step 1 fails since vehicles don't always reach last stop)
			// If using exact trip match should in theory minimize block flapping making this method more accurate
			if(CoreConfig.tryForExactTripMatch() &&
					currentMatchBlock != null &&  !currentMatchBlock.getId().equals(lastValidBlockId)){
				logger.debug("Found a block transition, hopefully not due to flapping. Prev block is {}, new block is {}",
						lastValidBlockId, currentMatchBlock.getId());
				return true;
			}
		}

		return false;
	}

	boolean isVehicleStateValid(VehicleState vehicleState){
		if(vehicleState.getMatch() == null){
			logger.warn("Can't process RunTime, currentMatch is null for {}", vehicleState);
			return false;
		}
		if(!vehicleState.isPredictable()){
			logger.warn("Can't process RunTime, vehicle is not predictable for {}", vehicleState);
			return false;
		}
		return true;
	}


	public List<IpcArrivalDeparture> getArrivalDeparturesForTrip(String routeId, String directionId, long vehicleMatchAvlTime, Integer startTime){
		Date nearestDay = DateUtils.truncate(new Date(vehicleMatchAvlTime), Calendar.DAY_OF_MONTH);
		TripKey key=new TripKey(routeId, directionId, nearestDay.getTime(), startTime);
		return TripDataHistoryCacheFactory.getInstance().getTripHistory(key);
	}

	public boolean isArrivalDeparturesValid(List<IpcArrivalDeparture> arrivalDeparturesForStop){
		if(arrivalDeparturesForStop == null || arrivalDeparturesForStop.size() < 2){
			return false;
		}
		return true;
	}

	int getLastStopIndex(SpatialMatch matchAtPreviousStop){
		Block block = matchAtPreviousStop.getBlock();
		int tripIndex = matchAtPreviousStop.getTripIndex();
		return block.numStopPaths(tripIndex) - 1;
	}




}
