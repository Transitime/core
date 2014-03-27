/* 
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitime.avl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.configData.AvlConfig;
import org.transitime.core.DataProcessor;
import org.transitime.core.VehicleStateManager;
import org.transitime.core.VehicleState;
import org.transitime.db.structs.AvlReport;
import org.transitime.utils.Time;

/**
 * For handling when a vehicle doesn't report its position for too long. Makes
 * the vehicle unpredictable if a timeout occurs.
 * 
 * @author SkiBu Smith
 * 
 */
public class TimeoutHandler {

	// For keeping track of the last AVL report for each vehicle.
	// Keyed on vehicle ID. Using a LinkedHashMap to maintain
	// the time order of the AVL reports. This way it is easy to
	// look back at the oldest ones to see if that have expired.
	private LinkedHashMap<String, AvlReport> orderedAvlReportsMap = new LinkedHashMap<String, AvlReport>(
			100, 0.75f, true);

	// This is a singleton class
	private static TimeoutHandler singleton = new TimeoutHandler();

	private static final Logger logger = 
			LoggerFactory.getLogger(TimeoutHandler.class);

	/********************** Member Functions **************************/

	/**
	 * Singleton class so constructor declared private
	 */
	private TimeoutHandler() {
	}

	public static TimeoutHandler get() {
		return singleton;
	}

	/**
	 * To be called every time a new AVL report is handled. This way can handle
	 * vehicles timing out even when not running in real time due to running in
	 * batch/playback mode.
	 * <p>
	 * This method is synchronized since the map itself is unsynced and need to
	 * make sure that don't have concurrent access.
	 * 
	 * @param avlReport
	 */
	public synchronized void handlePossibleTimeout(AvlReport newAvlReport) {
		// For ever AVL report that is too old handle that vehicle. Go through
		// AVL reports in order that they were inserted. If one is too old
		// then make the vehicle unpredictable.
		//
		// Using an Iterator instead of for(AvlReport a : map.values()) because
		// removing elements while iterating. Way to do this without
		// getting concurrent access exception is to use an Iterator.
		Iterator<AvlReport> mapIterator = 
				orderedAvlReportsMap.values().iterator();
		// Need to reset AVL report time if vehicle timeouts but was at a 
		// layover. But can't just modify the map while iterating over it.
		// And cannot add or modify an element using the iterator (can only
		// remove an element using iterator). Therefore need to keep track 
		// of which AVL reports should modify and do so after iterating
		// over the map.
		List<AvlReport> avlReportsToModifyDueToLayover = new ArrayList<AvlReport>();
		while (mapIterator.hasNext()) {
			AvlReport avlReport = mapIterator.next();

			if (avlReport.getTime() < newAvlReport.getTime()
					- AvlConfig.getAvlTimeoutSecs() * Time.MS_PER_SEC) {
				// If vehicle is at layover then it is a special case. At
				// layovers vehicles are sometimes powered down and don't
				// necessarily report their positions for a long time.
				VehicleState vehicleState = VehicleStateManager.getInstance()
						.getVehicleState(avlReport.getVehicleId());
				if (vehicleState.atLayover()) {
					// It is a layover so should not timeout the vehicle.
					// Instead should put an updated AvlReport into the map
					// that has the current time. This way won't be
					// checking the vehicle for whether it had timed out
					// every time there is a new AVL report. Can't update
					// the map while iterating over it so simply add the 
					// report to the list and update them after iterating
					// across the map.
					AvlReport avlReportWithUpdatedTime = new AvlReport(
							avlReport, newAvlReport.getDate());
					avlReportsToModifyDueToLayover.add(avlReportWithUpdatedTime);
				} else {
					// Vehicle is not a layover so handle normally

					// Make vehicle unpredictable
					DataProcessor.getInstance().
							makeVehicleUnpredictable(avlReport.getVehicleId());

					// Remove that AVL report from the map since it was handled
					logger.info("vehicleId={} timed out so making it unpredictable. "
							+ "The last AVL report for that vehicle was at {} but "
							+ "currently processing for time {}, which makes it "
							+ "{} old. The max allowable time without a new AVL "
							+ "report is getAvlTimeoutSecs()={}. The last AVL "
							+ "report for the vehicle: {}",
							avlReport.getVehicleId(),
							Time.timeStr(avlReport.getTime()),
							Time.timeStr(newAvlReport.getTime()),
							Time.elapsedTimeStr(newAvlReport.getTime()
									- avlReport.getTime()),
							Time.elapsedTimeStr(AvlConfig.getAvlTimeoutSecs()
									* Time.MS_PER_SEC), avlReport);
					mapIterator.remove();
				}
			} else {
				// This AVL report is not too old. Since they are ordered
				// in the map by when they were inserted, which corresponds
				// to the AVL time, the remaining ones will be even newer.
				// Therefore don't need to look further in the map.
				break;
			}
		}

		// Now that done iterating over the map can update the AVL reports where
		// a vehicle timed out but is at a layover.
		for (AvlReport avlReportWithUpdatedTime : avlReportsToModifyDueToLayover) {
			logger.debug("vehicleId={} timed out but it was on a layover. "
					+ "Therefore reseting its timeout AVL report to be {}",
					avlReportWithUpdatedTime.getVehicleId(),
					avlReportWithUpdatedTime);

			orderedAvlReportsMap.put(avlReportWithUpdatedTime.getVehicleId(),
				avlReportWithUpdatedTime);
		}
		
		// Add the new AVL report to the map so can later check to see if has
		// timed out. Since the map was created using accessOrder this one
		// will end up at the end of the map.
		orderedAvlReportsMap.put(newAvlReport.getVehicleId(), newAvlReport);
	}

}
