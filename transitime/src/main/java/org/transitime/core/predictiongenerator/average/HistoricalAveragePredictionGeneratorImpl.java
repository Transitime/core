package org.transitime.core.predictiongenerator.average;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.IntegerConfigValue;
import org.transitime.core.Indices;
import org.transitime.core.PredictionGeneratorDefaultImpl;
import org.transitime.core.dataCache.HistoricalAverage;
import org.transitime.core.dataCache.HistoricalAverageCache;
import org.transitime.core.dataCache.HistoricalAverageCacheKey;
import org.transitime.core.predictiongenerator.PredictionComponentElementsGenerator;
import org.transitime.db.structs.AvlReport;
import org.transitime.ipc.data.IpcPrediction;

/**
 * @author Sean Og Crudden
 *	This provides a prediction based on the average of historical data. The average is taken from the HistoricalAverageCache which is 
 *  populated each time an arrival/departure event occurs. The HistoricalAverageCache is updated using data from the TripDataHistory cache.
 */
public class HistoricalAveragePredictionGeneratorImpl extends
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

		logger.debug("Calling historical average algorithm.");				
		/*
		 * if we have enough data start using historical average otherwise
		 * revert to default. This does not mean that this method of
		 * prediction is better than the default.
		 */
		HistoricalAverageCacheKey historicalAverageCacheKey=new HistoricalAverageCacheKey(indices.getBlock().getId(),indices.getTripIndex(), indices.getStopPathIndex());
		
		HistoricalAverage average = HistoricalAverageCache.getInstance().getAverage(historicalAverageCacheKey);
		
		if(average.getCount()>=minDays.getValue())
			return (long)average.getAverage();
		
		logger.debug("No historical average found, generating default prediction.");
		/* default to parent method if not enough data. This will be based on schedule if UpdateTravelTimes has not been called. */
		return super.getTravelTimeForPath(indices, avlReport);
	}
	
}
