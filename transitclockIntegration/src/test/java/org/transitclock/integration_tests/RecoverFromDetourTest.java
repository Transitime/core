package org.transitclock.integration_tests;

import org.junit.Test;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.ipc.data.IpcVehicleComplete;
import org.transitclock.integration_tests.playback.PlaybackModule;
import org.transitclock.utils.Time;

import junit.framework.TestCase;

/*
 * This tests Transitime successfully recovering from detours. In this AVL trace the
 * bus goes off-route and returns to the route. We test that after the bus returns
 * to the route, it is not assigned to layover state and its schedule adherence
 * is reasonable.
 */
public class RecoverFromDetourTest extends TestCase {

	private static final String GTFS = "src/test/resources/gtfs/3T";
	private static final String AVL = "src/test/resources/avl/3T_3757.csv";
	private static final String VEHICLE = "3757";
	
	@Test
	public void test() {
		PlaybackModule.runTrace(GTFS, AVL, null, "America/New_York");
		IpcVehicleComplete v = VehicleDataCache.getInstance().getVehicle(VEHICLE);
		assertFalse(v.isLayover());
		int adh = Math.abs(v.getRealTimeSchedAdh().getTemporalDifference());
		assertTrue(adh < 10 * Time.MIN_IN_MSECS);
	}
	
}
