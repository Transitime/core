package org.transitime.core.predictiongenerator.lastvehicle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.IntegerConfigValue;
import org.transitime.core.Indices;
import org.transitime.core.PredictionGeneratorDefaultImpl;
import org.transitime.core.VehicleState;
import org.transitime.core.dataCache.HistoricalAverage;
import org.transitime.core.dataCache.TripDataHistoryCache;
import org.transitime.core.dataCache.StopPathCacheKey;
import org.transitime.core.dataCache.StopPathPredictionCache;
import org.transitime.core.dataCache.VehicleDataCache;
import org.transitime.core.dataCache.VehicleStateManager;
import org.transitime.core.dataCache.scheduled.ScheduleBasedHistoricalAverageCache;
import org.transitime.core.predictiongenerator.PredictionComponentElementsGenerator;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.PredictionForStopPath;
import org.transitime.ipc.data.IpcPrediction;
import org.transitime.ipc.data.IpcVehicleComplete;

/**
 * @author Sean Óg Crudden
 *	This provides a prediction based on the time it took the previous vehicle on the same route to cover the same ground. This is another step to get to Kalman implementation.
 *	  
 *  TODO Debug as this has yet to be tried and tested.
 *  Could do a combination with historical average  so that it improves quickly rather than just waiting on having enough data to support average or Kalman.
 *  So do a progression from LastVehicle --> Historical Average --> Kalman. Might be interesting to look at the rate of improvement of prediction as well as the end result.
 *  
 *  Does this by changing which class each extends. How can we make configurable?
 *  
 *  This works for both schedules based and frequency based services out of the box. Not so for historical average or Kalman filter.
 */
public class LastVehiclePredictionGeneratorImpl extends
	PredictionGeneratorDefaultImpl implements PredictionComponentElementsGenerator {
	private String alternative="PredictionGeneratorDefaultImpl";
				
	private static final Logger logger = LoggerFactory
			.getLogger(LastVehiclePredictionGeneratorImpl.class);

	/* (non-Javadoc)
	 * @see org.transitime.core.predictiongenerator.KalmanPredictionGeneratorImpl#getTravelTimeForPath(org.transitime.core.Indices, org.transitime.db.structs.AvlReport)
	 */
	@Override
	public long getTravelTimeForPath(Indices indices, AvlReport avlReport, VehicleState vehicleState) {
	
		VehicleDataCache vehicleCache = VehicleDataCache.getInstance();
		
		List<VehicleState> vehiclesOnRoute = new ArrayList<VehicleState>();

		VehicleStateManager vehicleStateManager = VehicleStateManager
				.getInstance();

		VehicleState currentVehicleState = vehicleStateManager
				.getVehicleState(avlReport.getVehicleId());

		for (IpcVehicleComplete vehicle : emptyIfNull(vehicleCache
				.getVehiclesForRoute(currentVehicleState.getRouteId()))) {
			VehicleState vehicleOnRouteState = vehicleStateManager
					.getVehicleState(vehicle.getId());
			vehiclesOnRoute.add(vehicleOnRouteState);
		}
				
		long time = 0;
		if((time=this.getLastVehicleTravelTime(currentVehicleState, indices))>0)
		{			
			logger.debug("Using last vehicle algorithm for prediction : " + time + " for : " + indices.toString());					
			
			if(storeTravelTimeStopPathPredictions.getValue())
			{
				PredictionForStopPath predictionForStopPath=new PredictionForStopPath(vehicleState.getVehicleId(), new Date(Core.getInstance().getSystemTime()), new Double(new Long(time).intValue()), indices.getTrip().getId(), indices.getStopPathIndex(), "LAST VEHICLE", true, null);				
				
				Core.getInstance().getDbLogger().add(predictionForStopPath);
				StopPathPredictionCache.getInstance().putPrediction(predictionForStopPath);
			}
			
			return time;
		}
				
		//logger.debug("No last vehicle data found, generating default prediction : " + indices.toString());
		/* default to parent method if not enough data. This will be based on schedule if UpdateTravelTimes has not been called. */
		return super.getTravelTimeForPath(indices, avlReport, currentVehicleState);
	}
	@Override
	public long getStopTimeForPath(Indices indices, AvlReport avlReport, VehicleState vehicleState) {
		// Looking at last vehicle value would be a bad idea for dwell time, so no implementation here.
		
		return super.getStopTimeForPath(indices, avlReport, vehicleState);
	}
}
