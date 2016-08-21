package org.transitime.core.predictiongenerator.lastvehicle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.IntegerConfigValue;
import org.transitime.core.Indices;
import org.transitime.core.PredictionGeneratorDefaultImpl;
import org.transitime.core.VehicleState;
import org.transitime.core.dataCache.HistoricalAverage;
import org.transitime.core.dataCache.HistoricalAverageCache;
import org.transitime.core.dataCache.HistoricalAverageCacheKey;
import org.transitime.core.dataCache.TripDataHistoryCache;
import org.transitime.core.dataCache.TripStopPathCacheKey;
import org.transitime.core.dataCache.VehicleDataCache;
import org.transitime.core.dataCache.VehicleStateManager;
import org.transitime.core.predictiongenerator.PredictionComponentElementsGenerator;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.PredictionForStopPath;
import org.transitime.ipc.data.IpcPrediction;
import org.transitime.ipc.data.IpcVehicleComplete;

/**
 * @author Sean Og Crudden
 *	This provides a prediction based on the time it took the previous vehicle on the same route to cover the same ground. This is another step to get to Kalman implementation.
 *	  
 *  TODO Debug as this has yet to be tried and tested.
 *  Could do a combination with historical average  so that it improves quickly rather than just waiting on having enough data to support average or Kalman.
 *  So do a progression from LastVehicle --> Historical Average --> Kalman. Might be interesting to look at the rate of improvement of prediction as well as the end result.
 *  
 *  Doen this by changing which class each extends. How can we make configurable?
 */
public class LastVehiclePredictionGeneratorImpl extends
	PredictionGeneratorDefaultImpl implements PredictionComponentElementsGenerator {
	
	/* (non-Javadoc)
	 * @see org.transitime.core.predictiongenerator.KalmanPredictionGeneratorImpl#generatePredictionForStop(org.transitime.db.structs.AvlReport, org.transitime.core.Indices, long, boolean, boolean, boolean, boolean)
	 */
	@Override
	public IpcPrediction generatePredictionForStop(AvlReport avlReport,
			Indices indices, long predictionTime, boolean useArrivalTimes,
			boolean affectedByWaitStop, boolean isDelayed,
			boolean lateSoMarkAsUncertain) {
		// TODO Auto-generated method stub
		return super.generatePredictionForStop(avlReport, indices, predictionTime,
				useArrivalTimes, affectedByWaitStop, isDelayed, lateSoMarkAsUncertain);
	}
		
	private static final Logger logger = LoggerFactory
			.getLogger(LastVehiclePredictionGeneratorImpl.class);

	/* (non-Javadoc)
	 * @see org.transitime.core.predictiongenerator.KalmanPredictionGeneratorImpl#getTravelTimeForPath(org.transitime.core.Indices, org.transitime.db.structs.AvlReport)
	 */
	@Override
	public long getTravelTimeForPath(Indices indices, AvlReport avlReport) {

		logger.debug("Calling last vehicle algorithm : "+indices.toString());
		
		VehicleDataCache vehicleCache = VehicleDataCache.getInstance();
		
		TripDataHistoryCache tripCache = TripDataHistoryCache.getInstance();

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
			
			logger.debug("Using last vehicle algorithm (getLastVehicleTravelTime) for prediction : " + indices + " Value: "+time + " instead of "+super.getClass().getName()+" prediction: "
					+ super.getTravelTimeForPath(indices, avlReport) +" for : " + indices.toString());
			
			if(storeTravelTimeStopPathPredictions.getValue())
			{
				PredictionForStopPath predictionForStopPath=new PredictionForStopPath(Calendar.getInstance().getTime(), new Double(new Long(time).intValue()), indices.getTrip().getId(), indices.getStopPathIndex(), this.getClass().getName());			
				Core.getInstance().getDbLogger().add(predictionForStopPath);
			}
			
			return time;
		}
				
		//logger.debug("No last vehicle data found, generating default prediction : " + indices.toString());
		/* default to parent method if not enough data. This will be based on schedule if UpdateTravelTimes has not been called. */
		return super.getTravelTimeForPath(indices, avlReport);
	}	
}
