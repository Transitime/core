package org.transitclock.core.predictiongenerator.kalman.dwell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.HeadwayDetails;
import org.transitclock.core.Indices;
import org.transitclock.core.TemporalDifference;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.VehicleStateManager;
import org.transitclock.core.predictiongenerator.kalman.KalmanPredictionGeneratorImpl;
import org.transitclock.db.structs.AvlReport;

public class DwellTimePredictionGeneratorImpl extends KalmanPredictionGeneratorImpl {
	
	private static final Logger logger = LoggerFactory.getLogger(DwellTimePredictionGeneratorImpl.class);
	@Override
	public long getStopTimeForPath(Indices indices, AvlReport avlReport, VehicleState vehicleState) {
		long result=-1L;
		try {
			HeadwayDetails headway = this.getHeadway(indices, avlReport, vehicleState);
			if(headway!=null)
			{
				logger.debug("Headway at {} based on avl {} is {}.",indices, avlReport, headway);
				headway.getVehicleAheadPrediction().getVehicleId();
				
				VehicleStateManager vehicleStateManager = VehicleStateManager.getInstance();
				
				TemporalDifference aheadScheduleAdherence = vehicleStateManager.getVehicleState(headway.getVehicleAheadPrediction().getVehicleId()).getRealTimeSchedAdh();
				TemporalDifference behindScheduleAdherence = vehicleStateManager.getVehicleState(headway.getVehicleBehindPrediction().getVehicleId()).getRealTimeSchedAdh();
				
				if(aheadScheduleAdherence.isWithinBounds() && behindScheduleAdherence.isWithinBounds())
				{
					/* Vehicles running as per schedule so use the scheduled dwell time or transitime default prediction method. 
					 * This will depend on if UpdateTravelTimes has been run.
					 */
					result = super.getStopTimeForPath(indices, avlReport, vehicleState);
				}else
				{								
					/*Simplistic but for a start take (predicted_headway/scheduled_headway) * scheduled dwell time.
					 TODO could use average dwell time instead of schedule dwell time.
					 TODO could create an ANN to use here. It could be trained with inputs of schedule_dwell_time, headway, time_of_day.
					*/				
					long vehicleAheadScheduleTime = headway.getVehicleAheadPrediction().getActualPredictionTime()+headway.getVehicleAheadPrediction().getDelay();
					long vehicleBehindScheduleTime = headway.getVehicleAheadPrediction().getActualPredictionTime()+headway.getVehicleAheadPrediction().getDelay();
					long scheduledHeadway=vehicleBehindScheduleTime-vehicleAheadScheduleTime;
					
					if(super.getStopTimeForPath(indices, avlReport, vehicleState)>0)
					{
						result = (headway.getHeadway()/scheduledHeadway)*super.getStopTimeForPath(indices, avlReport, vehicleState);
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
		} catch (Exception e) {

			logger.error(e.getMessage(),e);

		}
		return result;
	}

}
