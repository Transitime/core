package org.transitime.integration_tests;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.avl.BatchCsvAvlFeedModule.AvlPostProcessor;
import org.transitime.core.RealTimeSchedAdhProcessor;
import org.transitime.core.TemporalDifference;
import org.transitime.core.VehicleState;
import org.transitime.core.dataCache.VehicleStateManager;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.Trip;
import org.transitime.playback.PlaybackModule;

import junit.framework.TestCase;

// When we're using schedule times, effective schedule difference should be equal to schedule deviation.
public class EffectiveScheduleDifferenceDuringLayoverTest extends TestCase {
	
	private static final String GTFS = "src/test/resources/gtfs/5A";
	private static final String AVL = "src/test/resources/avl/5A_8062.csv";
	

	private static final Logger logger = LoggerFactory.getLogger(EffectiveScheduleDifferenceDuringLayoverTest.class);

	/*
	 * Test effective schedule adherence during a layover, which should be
	 * negative (early) if previous trip is still active and positive otherwise.
	 * 
	 * This test case, which is the same as the one used in
	 * GenerateEffectiveScheduleDifferneceTest, ends its first trip a few minutes early.
	 */
	@Test
	public void test() {
		
		AvlPostProcessor processor = new AvlPostProcessor() {

			int lastDifference = 0;
			
			public void postProcess(AvlReport avlReport) {
				String vehicleId = avlReport.getVehicleId();
				VehicleState vehicleState = VehicleStateManager.getInstance()
						.getVehicleState(vehicleId);
				
				if (!vehicleState.isPredictable())
					return;
				
				int difference = RealTimeSchedAdhProcessor.generateEffectiveScheduleDifference(vehicleState).getTemporalDifference();
								
				long avlTime = avlReport.getTime();
								
				Trip trip = vehicleState.getTrip();
				Trip lastTrip = vehicleState.getBlock().getTrip(trip.getIndexInBlock() - 1);
				if (lastTrip != null) {
					long tripEpochStartTime = Core.getInstance().getTime().getEpochTime(trip.getStartTime(), avlTime);
					long lastTripEpochEndTime = Core.getInstance().getTime().getEpochTime(lastTrip.getEndTime(), avlTime);
					
					if (avlTime < tripEpochStartTime) {
						
						if (avlTime < lastTripEpochEndTime) {
							assertTrue(difference < 0);
							assertTrue(difference > lastDifference); // since we are getting less early
						} else
							assertTrue(difference == 0);
						
					}
				}
				
				lastDifference = difference;
			}
			
		};
		
		PlaybackModule.runTrace(GTFS, AVL, processor);
	}
}
