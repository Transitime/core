package org.transitclock.core.headwaygenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.StringConfigValue;
import org.transitclock.configData.HeadwayConfig;
import org.transitclock.core.HeadwayGenerator;
import org.transitclock.core.MatchProcessor;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.PredictionDataCache;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheFactory;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.core.dataCache.VehicleStateManager;
import org.transitclock.core.dataCache.ehcache.StopArrivalDepartureCache;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Headway;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.ipc.data.IpcVehicleComplete;

import static org.transitclock.configData.HeadwayConfig.calculateSystemVariance;

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
public class LastDepartureHeadwayGenerator extends AbstractHeadwayGenerator {

	private static final Logger logger =
			LoggerFactory.getLogger(LastDepartureHeadwayGenerator.class);

	@Override
	public  Headway generate(VehicleState vehicleState) {

		try {

			if(vehicleState.getMatch().getMatchAtPreviousStop()==null)
				return null;

			String stopId = vehicleState.getMatch().getMatchAtPreviousStop().getAtStop().getStopId();
			long vehicleMatchAvlTime = vehicleState.getMatch().getAvlTime();
			String vehicleId=vehicleState.getVehicleId();

			List<IpcArrivalDeparture> arrivalDeparturesForStop = getRecentArrivalDeparturesForStop(stopId, vehicleMatchAvlTime);

			int lastStopArrivalIndex;
			int previousVehicleArrivalIndex;

			if(arrivalDeparturesForStop!=null)
			{
				boolean useArrivals = false;
				int[] arrivalIndexes = getLastStopAndPrevVehicleArrivalDepartureIndex(vehicleState, vehicleId, stopId,
																					arrivalDeparturesForStop, useArrivals);
				lastStopArrivalIndex = arrivalIndexes[0];
				previousVehicleArrivalIndex = arrivalIndexes [1];

				if(previousVehicleArrivalIndex!=-1 && lastStopArrivalIndex!=-1) {

					IpcArrivalDeparture lastStopDeparture = arrivalDeparturesForStop.get(lastStopArrivalIndex);
					IpcArrivalDeparture lastStopPreviousVehicleDeparture = arrivalDeparturesForStop.get(previousVehicleArrivalIndex);

					long headwayTime= calculateHeadway(lastStopDeparture, lastStopPreviousVehicleDeparture);
					Long scheduledHeadwayTime = calculateScheduledHeadway(lastStopDeparture, lastStopPreviousVehicleDeparture);

					Headway headway=new Headway(headwayTime,
												scheduledHeadwayTime,
												new Date(vehicleMatchAvlTime),
												vehicleId,
												arrivalDeparturesForStop.get(previousVehicleArrivalIndex).getVehicleId(),
												stopId,
												vehicleState.getTrip().getId(),
												vehicleState.getTrip().getRouteId(),
												new Date(arrivalDeparturesForStop.get(lastStopArrivalIndex).getTime().getTime()),
												new Date(arrivalDeparturesForStop.get(previousVehicleArrivalIndex).getTime().getTime()),
												new Date(vehicleState.getAvlReport().getTimeProcessed()));

					if(isInvalidHeadway(headway, lastStopArrivalIndex))
					{
						return null;
					}

					if(calculateSystemVariance()){
						setSystemVariance(headway);
					}

					return headway;
				}
			}
		} catch (Exception e) {
			logger.error("Exception when processing headway", e);
		}
		return null;
	}

	private boolean isInvalidHeadway(Headway headway, int lastStopArrivalIndex){
		if(Math.abs(headway.getCreationTime().getTime()-headway.getFirstDeparture().getTime())>1200000||lastStopArrivalIndex>5)
		{
			return true;
		}
		return false;
	}
}
