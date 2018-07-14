package org.transitclock.core.predictiongenerator.rls.dwell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.core.HeadwayDetails;
import org.transitclock.core.Indices;
import org.transitclock.core.TemporalDifference;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.DwellTimeModelCacheFactory;
import org.transitclock.core.dataCache.VehicleStateManager;
import org.transitclock.core.predictiongenerator.kalman.KalmanPredictionGeneratorImpl;
import org.transitclock.db.structs.AvlReport;

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
	
	private static BooleanConfigValue useScheduleWithinBounds = new BooleanConfigValue("org.transitclock.core.predictiongenerator.rls.dwell.useScheduleWithinBounds",false,"If vehicles within bounds of schedule use the scheduled ");
	@Override
	public long getStopTimeForPath(Indices indices, AvlReport avlReport, VehicleState vehicleState) {
		Long result=null;
		try {
			HeadwayDetails headway = this.getHeadway(indices, avlReport, vehicleState);
			if(headway!=null)
			{
				logger.debug("Headway at {} based on avl {} is {}.",indices, avlReport, headway);
				headway.getVehicleAheadPrediction().getVehicleId();
				
				VehicleStateManager vehicleStateManager = VehicleStateManager.getInstance();
				
				TemporalDifference aheadScheduleAdherence = vehicleStateManager.getVehicleState(headway.getVehicleAheadPrediction().getVehicleId()).getRealTimeSchedAdh();
				TemporalDifference behindScheduleAdherence = vehicleStateManager.getVehicleState(headway.getVehicleBehindPrediction().getVehicleId()).getRealTimeSchedAdh();
				
				if(useScheduleWithinBounds.getValue() && aheadScheduleAdherence.isWithinBounds() && behindScheduleAdherence.isWithinBounds())
				{
					/* Vehicles running as per schedule so use the scheduled dwell time or transitime default prediction method. 
					 * This will depend on if UpdateTravelTimes has been run.
					 */
					result = super.getStopTimeForPath(indices, avlReport, vehicleState);
				}else
				{								
					/* Change approach to use a RLS model.
					*/																		
					if(super.getStopTimeForPath(indices, avlReport, vehicleState)>0)
					{												
						result = DwellTimeModelCacheFactory.getInstance().predictDwellTime(indices, headway);
						
						if(result==null)
							result = super.getStopTimeForPath(indices, avlReport, vehicleState);
						
						
						/* should never have a negative dwell time */
						if(result<0)
						{
							logger.error("Predicted negative dwell time {} for {}.", result, indices);
							result=0L;
						}
							
					}else
					{
						result = super.getStopTimeForPath(indices, avlReport, vehicleState);
					}				
				}
			}
			else
			{
				result = super.getStopTimeForPath(indices, avlReport, vehicleState);
			}			
			logger.debug("Using dwell time {} for {} instead of {}.",result,indices, super.getStopTimeForPath(indices, avlReport, vehicleState));
		} catch (Exception e) {

			logger.error(e.getMessage(),e);

		}
		
		return result;
	}

}
