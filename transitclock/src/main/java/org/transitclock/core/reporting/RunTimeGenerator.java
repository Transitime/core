package org.transitclock.core.reporting;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
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
			ServiceUtils serviceUtils = Core.getInstance().getServiceUtils();
			return processor.processRunTimesForTrip(vehicleId, trip, arrivalDeparturesForStop, prevMatch, match,
					lastStopIndex, serviceUtils);

		} catch (Exception e) {
			logger.error("Exception when processing run times", e);
		}
		return false;
	}

	boolean hasCrossedToNextTrip(TemporalMatch currentMatch,
															 TemporalMatch previousMatch) {

		if(currentMatch != null && previousMatch != null &&
						previousMatch.getTripIndex() < currentMatch.getTripIndex() &&
						previousMatch.getBlock().getId().equals(currentMatch.getBlock().getId())){
			return true;
		}
		return false;
	}

	boolean isVehicleStateValid(VehicleState vehicleState){
		return vehicleState.isPredictable() &&
						vehicleState.getMatch() != null &&
						vehicleState.getMatch().getMatchAtPreviousStop() != null;
	}


	public List<IpcArrivalDeparture> getArrivalDeparturesForTrip(String tripId, long vehicleMatchAvlTime, Integer startTime){
		Date nearestDay = DateUtils.truncate(new Date(vehicleMatchAvlTime), Calendar.DAY_OF_MONTH);
		TripKey key=new TripKey(tripId, nearestDay, startTime);
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
