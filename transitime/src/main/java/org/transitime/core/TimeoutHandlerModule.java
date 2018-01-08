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
package org.transitime.core;

import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.IntegerConfigValue;
import org.transitime.configData.AgencyConfig;
import org.transitime.core.dataCache.VehicleStateManager;
import org.transitime.core.schedBasedPreds.SchedBasedPredsModule;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.VehicleEvent;
import org.transitime.logging.Markers;
import org.transitime.modules.Module;
import org.transitime.utils.IntervalTimer;
import org.transitime.utils.Time;

/**
 * For handling when a vehicle doesn't report its position for too long. Makes
 * the vehicle unpredictable if a timeout occurs.
 * <p>
 * Note: only predictable vehicles are timed out. This is because vehicles that
 * are not in service are likely to get turned off and not report their position
 * for a long period of time. Plus since they are already not predictable there
 * is no need to be make them unpredictable when there is a timeout.
 * 
 * @author SkiBu Smith
 * 
 */
public class TimeoutHandlerModule extends Module {

	// For keeping track of the last AVL report for each vehicle. Keyed on 
	// vehicle ID. Synchronize map modifications since elsewhere the elements 
	// can be removed from the map.
	private HashMap<String, AvlReport> avlReportsMap = 
			new HashMap<String, AvlReport>();

	/********************* Parameters *********************************/

	private static IntegerConfigValue pollingRateSecs = 
			new IntegerConfigValue(
					"transitime.timeout.pollingRateSecs", 
					30,
					"Specifies in seconds how frequently the TimeoutHandler "
					+ "should actually look for timeouts. Don't want to do "
					+ "this too frequently because because "
					+ "TimeoutHandler.handlePossibleTimeout() is called with "
					+ "every new AVL report and it has to look at every "
					+ "vehicle to see if has been timed out.");

	private static IntegerConfigValue allowableNoAvlSecs =
			new IntegerConfigValue(
					"transitime.timeout.allowableNoAvlSecs", 
					6*Time.SEC_PER_MIN,
					"For AVL timeouts. If don't get an AVL report for the "
					+ "vehicle in this amount of time in seconds then the " 
					+ "vehicle will be made non-predictable.");

	private static IntegerConfigValue allowableNoAvlAfterSchedDepartSecs = 
			new IntegerConfigValue(
					"transitime.timeout.allowableNoAvlAfterSchedDepartSecs",
					6 * Time.SEC_PER_MIN,
					"If a vehicle is at a wait stop, such as "
					+ "sitting at a terminal, and doesn't provide an AVL report "
					+ "for this number of seconds then the vehicle is made "
					+ "unpredictable. Important because sometimes vehicles "
					+ "don't report AVL at terminals because they are powered "
					+ "down. But don't want to continue to provide predictions "
					+ "for long after scheduled departure time if vehicle "
					+ "taken out of service.");

	/********************* Logging ************************************/

	private static final Logger logger = LoggerFactory
			.getLogger(TimeoutHandlerModule.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor
	 */
	public TimeoutHandlerModule(String agencyId) {
		super(agencyId);
	}

	/**
	 * Stores the specified AVL report into map so know the last time received
	 * AVL data for the vehicle.
	 * 
	 * @param avlReport
	 *            AVL report to store
	 */
	public void storeAvlReport(AvlReport avlReport) {
		// Synchronize map modifications since elsewhere the elements can be removed
		// from the map.
		synchronized (avlReportsMap) {
			avlReportsMap.put(avlReport.getVehicleId(), avlReport);
		}
	}
	
	/**
	 * For regular predictable vehicle that is not a schedule based prediction
	 * nor a vehicle at a wait stop. If haven't reported in too long makes the
	 * vehicle unpredictable and logs situation.
	 * 
	 * @param vehicleState
	 * @param now
	 * @param mapIterator
	 */
	private void handlePredictablePossibleTimeout(VehicleState vehicleState, long now, Iterator<AvlReport> mapIterator) {
		// If haven't reported in too long...
		long maxNoAvl = allowableNoAvlSecs.getValue() * Time.MS_PER_SEC;
		if (now > vehicleState.getAvlReport().getTime() + maxNoAvl) {
			// Make vehicle unpredictable
			String eventDescription = "Vehicle timed out because it "
					+ "has not reported in "
					+ Time.elapsedTimeStr(now
							- vehicleState.getAvlReport().getTime())
					+ " while allowable time without an AVL report is "
					+ Time.elapsedTimeStr(maxNoAvl)
					+ " and so was made unpredictable.";
			AvlProcessor.getInstance().makeVehicleUnpredictable(
					vehicleState.getVehicleId(), eventDescription,
					VehicleEvent.TIMEOUT);
			
			// Also log the situation
			logger.info("For vehicleId={} {}", 
					vehicleState.getVehicleId(), eventDescription);
			
			// Remove vehicle from map for next time looking for timeouts
			mapIterator.remove();
			
		}
	}
	
	/**
	 * Don't need to worry about vehicles that are not predictable. But might as
	 * well remove vehicle from map so don't examine vehicle every
	 * TimeoutHandleModule polling cycle
	 * 
	 * @param mapIterator
	 *            So can remove AVL report from map
	 */
	private void handleNotPredictablePossibleTimeout(Iterator<AvlReport> mapIterator) {
		mapIterator.remove();
	}

	/**
	 * For schedule based predictions. If past the scheduled departure time by
	 * more than allowed amount then the schedule based vehicle is removed.
	 * Useful for situations such as when using schedule based vehicles and auto
	 * assigner but the auto assigner can't find a vehicle for a while,
	 * indicating no such vehicle in service.
	 * 
	 * @param vehicleState
	 * @param now
	 * @param mapIterator
	 */
	private void handleSchedBasedPredsPossibleTimeout(VehicleState vehicleState,
					long now, Iterator<AvlReport> mapIterator) {
		// If should timeout the schedule based vehicle...
		String shouldTimeoutEventDescription =
				SchedBasedPredsModule.shouldTimeoutVehicle(vehicleState, now);				
		if (shouldTimeoutEventDescription != null) {
			AvlProcessor.getInstance().makeVehicleUnpredictable(
					vehicleState.getVehicleId(), shouldTimeoutEventDescription,
					VehicleEvent.TIMEOUT);
			
			// Also log the situation
			logger.info("For schedule based vehicleId={} generated timeout "
					+ "event. {}", 
					vehicleState.getVehicleId(), shouldTimeoutEventDescription);
			
			// Remove vehicle from map for next time looking for timeouts
			mapIterator.remove();			
		}
	}
	
	/**
	 * It is a wait stop which means that vehicle can be stopped and turned off
	 * for a while such that don't expect to get any AVL reports. Only timeout
	 * if past more that the allowed time for wait stops
	 * 
	 * @param vehicleState
	 * @param now
	 * @param mapIterator
	 */
	private void handleWaitStopPossibleTimeout(VehicleState vehicleState, long now,
			Iterator<AvlReport> mapIterator) {

	  // we can't easily determine wait stop time for frequency based trips  
	  // so don't timeout based on stop info
	  if (vehicleState.getBlock().isNoSchedule()) {
      logger.debug("not timing out frequency based assignment {}", vehicleState);
      return;
    }
	  
	  // If hasn't been too long between AVL reports then everything is fine
		// and simply return
		long maxNoAvl = allowableNoAvlSecs.getValue() * Time.MS_PER_SEC;
		if (now < vehicleState.getAvlReport().getTime() + maxNoAvl)
			return;

		// It has been a long time since an AVL report so see if also past the 
		// scheduled time for the wait stop
		long scheduledDepartureTime =
				vehicleState.getMatch().getScheduledWaitStopTime();
		if (scheduledDepartureTime >= 0) {
			// There is a scheduled departure time. Make sure not too
			// far past it
			long maxNoAvlAfterSchedDepartSecs =
					allowableNoAvlAfterSchedDepartSecs.getValue() * Time.MS_PER_SEC;
			if (now > scheduledDepartureTime + maxNoAvlAfterSchedDepartSecs) {				
				// Make vehicle unpredictable
				String stopId = "none (vehicle not matched)";
				if (vehicleState.getMatch() != null) {
					if (vehicleState.getMatch().getAtEndStop() != null) {
						stopId = vehicleState.getMatch().getAtStop().getStopId();
					}
				}
				String eventDescription = "Vehicle timed out because it "
						+ "has not reported AVL location in "
						+ Time.elapsedTimeStr(now - 
								vehicleState.getAvlReport().getTime())
						+ " and it is "
						+ Time.elapsedTimeStr(now
								- scheduledDepartureTime)
						+ " since the scheduled departure time "
						+ Time.dateTimeStr(scheduledDepartureTime)
						+ " for the wait stop ID " 
						+ stopId
						+ " while allowable time without an AVL report is "
						+ Time.elapsedTimeStr(maxNoAvl)
						+ " and maximum allowed time after scheduled departure "
						+ "time without AVL is "  
						+ Time.elapsedTimeStr(maxNoAvlAfterSchedDepartSecs)
						+ ". Therefore vehicle was made unpredictable.";
				AvlProcessor.getInstance().makeVehicleUnpredictable(
						vehicleState.getVehicleId(), eventDescription,
						VehicleEvent.TIMEOUT);
				
				// Also log the situation
				logger.info("For vehicleId={} {}", 
						vehicleState.getVehicleId(), eventDescription);
				
				// Remove vehicle from map for next time looking for timeouts
				mapIterator.remove();
			}
		}
	}

	/**
	 * Goes through all vehicles and finds ones that have timed out
	 */
	public void handlePossibleTimeouts() {
		// Determine what now is. Don't use System.currentTimeMillis() since
		// that doesn't work for playback.
		long now = Core.getInstance().getSystemTime();

		// Sync access to avlReportsMap since it can be simultaneously 
		// modified elsewhere
		synchronized (avlReportsMap) {				
			// Using an Iterator instead of for(AvlReport a : map.values()) 
			// because removing elements while iterating. Way to do this without
			// getting concurrent access exception is to use an Iterator.
			Iterator<AvlReport> mapIterator = avlReportsMap.values().iterator();
			while (mapIterator.hasNext()) {
				AvlReport avlReport = mapIterator.next();
	
				// Get state of vehicle and handle based on it
				VehicleState vehicleState = VehicleStateManager.getInstance()
						.getVehicleState(avlReport.getVehicleId());
	
				// Need to synchronize on vehicleState since it might be getting
				// modified via a separate main AVL processing executor thread.
				synchronized (vehicleState) {
					if (!vehicleState.isPredictable()) {
						// Vehicle is not predictable
						handleNotPredictablePossibleTimeout(mapIterator);
					} else if (vehicleState.isForSchedBasedPreds()) {
						// Handle schedule based predictions vehicle
						handleSchedBasedPredsPossibleTimeout(vehicleState, now,
								mapIterator);
					} else if (vehicleState.isWaitStop()) {
						// Handle where vehicle is at a wait stop
						handleWaitStopPossibleTimeout(vehicleState, now,
								mapIterator);
					} else {
						// Not a special case. Simply determine if vehicle 
						// timed out
						handlePredictablePossibleTimeout(vehicleState, now,
								mapIterator);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		logger.info("Starting module {} for agencyId={}", getClass().getName(),
				getAgencyId());

		// No need to run at startup since haven't processed AVL data yet
		Time.sleep(pollingRateSecs.getValue() * Time.MS_PER_SEC);

		// Run forever
		while (true) {
			try {
				// For determining when to poll next
				IntervalTimer timer = new IntervalTimer();

				// Do the actual work
				handlePossibleTimeouts();

				// Wait appropriate amount of time till poll again
				long sleepTime = pollingRateSecs.getValue() * Time.MS_PER_SEC
						- timer.elapsedMsec();
				if (sleepTime > 0)
					Time.sleep(sleepTime);
			} catch (Exception e) {
				logger.error(Markers.email(),
						"Error with TimeoutHandlerModule for agencyId={}", 
						AgencyConfig.getAgencyId(), e);
			}

		}
	}

}
