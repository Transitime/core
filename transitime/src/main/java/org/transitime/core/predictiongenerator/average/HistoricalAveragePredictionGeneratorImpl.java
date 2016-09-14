package org.transitime.core.predictiongenerator.average;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.IntegerConfigValue;
import org.transitime.core.Indices;
import org.transitime.core.PredictionGeneratorDefaultImpl;
import org.transitime.core.dataCache.HistoricalAverage;
import org.transitime.core.dataCache.HistoricalAverageCache;

import org.transitime.core.dataCache.StopPathCacheKey;
import org.transitime.core.predictiongenerator.PredictionComponentElementsGenerator;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.PredictionForStopPath;

/**
 * @author Sean Og Crudden
 *	This provides a prediction based on the average of historical data. The average is taken from the HistoricalAverageCache which is 
 *  populated each time an arrival/departure event occurs. The HistoricalAverageCache is updated using data from the TripDataHistory cache.
 */
public class HistoricalAveragePredictionGeneratorImpl extends
	PredictionGeneratorDefaultImpl implements PredictionComponentElementsGenerator {
	private String alternative="LastVehiclePredictionGeneratorImpl";
	


	private static final IntegerConfigValue minDays = new IntegerConfigValue(
			"transitime.prediction.data.average.mindays",
			new Integer(1),
			"Min number of days trip data that needs to be available before historical average prediciton is used instead of default transiTime prediction.");
	
	
	private static final Logger logger = LoggerFactory
			.getLogger(HistoricalAveragePredictionGeneratorImpl.class);

	/* (non-Javadoc)
	 * @see org.transitime.core.predictiongenerator.KalmanPredictionGeneratorImpl#getTravelTimeForPath(org.transitime.core.Indices, org.transitime.db.structs.AvlReport)
	 */
	@Override
	public long getTravelTimeForPath(Indices indices, AvlReport avlReport) {

		logger.debug("Calling historical average algorithm : "+indices.toString());				
		/*
		 * if we have enough data start using historical average otherwise
		 * revert to default. This does not mean that this method of
		 * prediction is better than the default.
		 */				
		StopPathCacheKey historicalAverageCacheKey=new StopPathCacheKey(indices.getTrip().getId(), indices.getStopPathIndex());
		
		HistoricalAverage average = HistoricalAverageCache.getInstance().getAverage(historicalAverageCacheKey);
		
		if(average!=null && average.getCount()>=minDays.getValue())
		{
			if(storeTravelTimeStopPathPredictions.getValue())
			{
				PredictionForStopPath predictionForStopPath=new PredictionForStopPath(Calendar.getInstance().getTime(), average.getAverage(), indices.getTrip().getId(), indices.getStopPathIndex(), "HISTORICAL AVERAGE");			
				Core.getInstance().getDbLogger().add(predictionForStopPath);
			}
			
			logger.debug("Using historical average algorithm for prediction : " +average.toString() + " instead of "+alternative+" prediction: "
					+ super.getTravelTimeForPath(indices, avlReport) +" for : " + indices.toString());
			//logger.debug("Instead of transtime value : " + super.getTravelTimeForPath(indices, avlReport));
			return (long)average.getAverage();
		}
		
		//logger.debug("No historical average found, generating prediction using lastvehicle algorithm: " + historicalAverageCacheKey.toString());
		/* default to parent method if not enough data. This will be based on schedule if UpdateTravelTimes has not been called. */
		return super.getTravelTimeForPath(indices, avlReport);
	}	

	@Override
	public long getStopTimeForPath(Indices indices, AvlReport avlReport) {
		// TODO Auto-generated method stub
		return super.getStopTimeForPath(indices, avlReport);
	}
	
	@Override
	public boolean hasDataForPath(Indices indices, AvlReport avlReport) {
		StopPathCacheKey historicalAverageCacheKey = new StopPathCacheKey(indices.getTrip().getId(), indices.getStopPathIndex());
		HistoricalAverage average = HistoricalAverageCache.getInstance().getAverage(historicalAverageCacheKey);
		
		return (average!=null && average.getCount()>=minDays.getValue());
	}
}
