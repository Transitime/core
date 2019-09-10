package org.transitclock.core.predictiongenerator.scheduled.average;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.Indices;
import org.transitclock.core.PredictionGeneratorDefaultImpl;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.HistoricalAverage;
import org.transitclock.core.dataCache.StopPathCacheKey;
import org.transitclock.core.dataCache.StopPathPredictionCacheFactory;
import org.transitclock.core.dataCache.scheduled.ScheduleBasedHistoricalAverageCache;
import org.transitclock.core.predictiongenerator.PredictionComponentElementsGenerator;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.PredictionForStopPath;

import java.util.Date;

/**
 * @author Sean Óg Crudden
 *	This provides a prediction based on the average of historical data for schedules based services. The average is taken from the HistoricalAverageCache which is 
 *  populated each time an arrival/departure event occurs. The HistoricalAverageCache is updated using data from the TripDataHistory cache.
 */
public class HistoricalAveragePredictionGeneratorImpl extends
	PredictionGeneratorDefaultImpl implements PredictionComponentElementsGenerator {
	private String alternative="LastVehiclePredictionGeneratorImpl";
	


	private static final IntegerConfigValue minDays = new IntegerConfigValue(
			"transitclock.prediction.data.average.mindays",
			new Integer(1),
			"Min number of days trip data that needs to be available before historical average prediciton is used instead of default transiTime prediction.");
	
	
	private static final Logger logger = LoggerFactory
			.getLogger(HistoricalAveragePredictionGeneratorImpl.class);

	/* (non-Javadoc)
	 * @see org.transitclock.core.predictiongenerator.KalmanPredictionGeneratorImpl#getTravelTimeForPath(org.transitclock.core.Indices, org.transitclock.db.structs.AvlReport)
	 */
	@Override
	public long getTravelTimeForPath(Indices indices, AvlReport avlReport, VehicleState vehicleState) {

		logger.debug("Calling historical average algorithm : "+indices.toString());				
		/*
		 * if we have enough data start using historical average otherwise
		 * revert to default. This does not mean that this method of
		 * prediction is better than the default.
		 */				
		StopPathCacheKey historicalAverageCacheKey=new StopPathCacheKey(indices.getTrip().getId(), indices.getStopPathIndex());
		
		HistoricalAverage average = ScheduleBasedHistoricalAverageCache.getInstance().getAverage(historicalAverageCacheKey);
		
		if(average!=null && average.getCount()>=minDays.getValue())
		{
			if(storeTravelTimeStopPathPredictions.getValue())
			{
				PredictionForStopPath predictionForStopPath=new PredictionForStopPath(vehicleState.getVehicleId(), new Date(Core.getInstance().getSystemTime()), average.getAverage(), indices.getTrip().getId(), indices.getStopPathIndex(), "HISTORICAL AVERAGE",true, null);			
				Core.getInstance().getDbLogger().add(predictionForStopPath);
				StopPathPredictionCacheFactory.getInstance().putPrediction(predictionForStopPath);
			}
			
			logger.debug("Using historical average algorithm for prediction : " +average.toString() + " instead of "+alternative+" prediction: "
					+ super.getTravelTimeForPath(indices, avlReport,vehicleState) +" for : " + indices.toString());
			//logger.debug("Instead of transitime value : " + super.getTravelTimeForPath(indices, avlReport));
			return (long)average.getAverage();
		}
		
		//logger.debug("No historical average found, generating prediction using lastvehicle algorithm: " + historicalAverageCacheKey.toString());
		/* default to parent method if not enough data. This will be based on schedule if UpdateTravelTimes has not been called. */
		return super.getTravelTimeForPath(indices, avlReport, vehicleState);
	}	

	@Override
	public long getStopTimeForPath(Indices indices,  AvlReport avlReport, VehicleState vehicleState) {
		
		StopPathCacheKey historicalAverageCacheKey=new StopPathCacheKey(indices.getTrip().getId(), indices.getStopPathIndex(),false);
		
		HistoricalAverage average = ScheduleBasedHistoricalAverageCache.getInstance().getAverage(historicalAverageCacheKey);
		
		if(average!=null && average.getCount()>=minDays.getValue())
		{
			logger.debug("Using historical average alogrithm for dwell time prediction : "+average.toString() + " instead of "+alternative+" prediction: "
					+ super.getStopTimeForPath(indices,  avlReport, vehicleState) +" for : " + indices.toString());
			return (long)average.getAverage();
		}
					
		return super.getStopTimeForPath(indices,  avlReport, vehicleState);
	}
	
	@Override
	public boolean hasDataForPath(Indices indices, AvlReport avlReport) {
		StopPathCacheKey historicalAverageCacheKey = new StopPathCacheKey(indices.getTrip().getId(), indices.getStopPathIndex());
		HistoricalAverage average = ScheduleBasedHistoricalAverageCache.getInstance().getAverage(historicalAverageCacheKey);
		
		return (average!=null && average.getCount()>=minDays.getValue());
	}
}
