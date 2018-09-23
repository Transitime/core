package org.transitclock.core.predictiongenerator.scheduled.dwell.rls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.core.HeadwayDetails;
import org.transitclock.core.Indices;
import org.transitclock.core.TemporalDifference;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.DwellTimeModelCacheFactory;
import org.transitclock.core.dataCache.StopPathCacheKey;
import org.transitclock.core.dataCache.VehicleStateManager;
import org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman.KalmanPredictionGeneratorImpl;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.Headway;
import org.transitclock.ipc.data.IpcPrediction;

/**
 * @author Sean Og Crudden
 * 
 * This is an experiment to see if headway can be used to better predict dwell time. Most of what 
 * I have read tells me it can but in conjunction with APC data and estimation of demand at stops.
 * 
 * I do wonder if headway alone is enough to at least improve things beyond using the schedule?
 *
 */
public class DwellTimePredictionGeneratorImpl extends KalmanPredictionGeneratorImpl {
	
	private static final Logger logger = LoggerFactory.getLogger(DwellTimePredictionGeneratorImpl.class);
		
	@Override
	public long getStopTimeForPath(Indices indices,  AvlReport avlReport, VehicleState vehicleState) {
		Long result=null;
		try {
			Headway headway = vehicleState.getHeadway();
			
			if(headway!=null)
			{
				logger.debug("Headway at {} based on avl {} is {}.",indices, avlReport, headway);												
																	
				/* Change approach to use a RLS model.
				*/																		
				if(super.getStopTimeForPath(indices, avlReport, vehicleState)>0)
				{																		
					
					StopPathCacheKey cacheKey=new StopPathCacheKey(indices.getTrip().getId(), indices.getStopPathIndex(),  false); 
					
					
					result = DwellTimeModelCacheFactory.getInstance().predictDwellTime(cacheKey, headway);
					
					if(result==null)
					{
						logger.debug("Using scheduled value for dwell time as no RLS data available for {}.", indices);
						result = super.getStopTimeForPath(indices,  avlReport, vehicleState);
					}
					
					
					/* should never have a negative dwell time */
					if(result<0)
					{
						logger.debug("Predicted negative dwell time {} for {}.", result, indices);
						result=0L;
					}
						
				}else
				{
					logger.debug("Scheduled dwell time is less than 0 for {}.", indices);
					result = super.getStopTimeForPath(indices, avlReport, vehicleState);
				}				
								
				logger.debug("Using dwell time {} for {} instead of {}. Headway for vehicle {} is {}",result,indices, super.getStopTimeForPath(indices,  avlReport, vehicleState), vehicleState.getVehicleId(),headway );
			}
			else
			{
				result = super.getStopTimeForPath(indices, avlReport, vehicleState);
				logger.debug("Using dwell time {} for {} instead of {}. No headway.",result,indices, super.getStopTimeForPath(indices ,avlReport, vehicleState));
			}			
	
		} catch (Exception e) {

			logger.error(e.getMessage(),e);
			e.printStackTrace();

		}
		
		return result;
	}

}
