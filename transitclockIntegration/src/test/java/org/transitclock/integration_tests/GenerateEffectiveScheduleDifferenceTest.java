package org.transitclock.integration_tests;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.avl.BatchCsvAvlFeedModule.AvlPostProcessor;
import org.transitclock.core.RealTimeSchedAdhProcessor;
import org.transitclock.core.TemporalDifference;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.VehicleStateManager;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.Trip;
import org.transitclock.integration_tests.playback.PlaybackModule;

import junit.framework.TestCase;

// When we're using schedule times, effective schedule difference should be equal to schedule deviation.
public class GenerateEffectiveScheduleDifferenceTest extends TestCase {
	
	private static final String GTFS = "src/test/resources/gtfs/5A";
	private static final String AVL = "src/test/resources/avl/5A_8062.csv";
	
	private static final int THRESHOLD = 10000; // 10 seconds
	
	private static final Logger logger = LoggerFactory.getLogger(GenerateEffectiveScheduleDifferenceTest.class);
	
	@Test
	public void test() {
		
		AvlPostProcessor processor = new AvlPostProcessor() {

			public void postProcess(AvlReport avlReport) {
				
				String vehicleId = avlReport.getVehicleId();
				VehicleState vehicleState = VehicleStateManager.getInstance()
						.getVehicleState(vehicleId);
				
				if (!vehicleState.isPredictable())
					return;
				
				TemporalDifference schAdh = vehicleState.getRealTimeSchedAdh();
				TemporalDifference effSchAdh = RealTimeSchedAdhProcessor.generateEffectiveScheduleDifference(vehicleState);
				
				// Only do this check if previous trip is not still active
				int tripIndex = vehicleState.getTrip().getIndexInBlock();
				Trip prevTrip = vehicleState.getBlock().getTrip(tripIndex - 1);				
				if (prevTrip != null && 
						Core.getInstance().getTime().getEpochTime(prevTrip.getEndTime(), avlReport.getTime()) > avlReport.getTime())
					return;
				
				if (schAdh != null) {
					int delta = Math.abs(schAdh.getTemporalDifference() - -1*effSchAdh.getTemporalDifference());
					logger.info("schAdh={}, effSchAdh={}", schAdh, effSchAdh);
					assertTrue(delta < THRESHOLD); 
				}
			}
			
		};
		
		PlaybackModule.runTrace(GTFS, AVL, null, processor, null);
	}
	
}
