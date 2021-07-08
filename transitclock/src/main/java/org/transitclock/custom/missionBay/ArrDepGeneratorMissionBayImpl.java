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

package org.transitclock.custom.missionBay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.ArrivalDepartureGeneratorDefaultImpl;
import org.transitclock.core.VehicleState;
import org.transitclock.db.structs.Arrival;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Departure;
import org.transitclock.db.structs.Location;
import org.transitclock.utils.Time;

/**
 * Handles generation of Arrivals and Departures by extending the
 * class ArrivalDepartureGeneratorDefaultImpl but also logs arrival/departure
 * information to the SFMTA API for tracking when tech shuttles are stopped
 * at SFMTA stops.
 *
 * @author SkiBu Smith
 *
 */
public class ArrDepGeneratorMissionBayImpl 
	extends ArrivalDepartureGeneratorDefaultImpl {

	// For keeping track of which stops are actually sfmta stops that need
	// to be tracked. Keyed on the missionBay stopId and contains corresponding
	// SFMTA numeric stop ID.
	private static Map<String, Integer> sfmtaStopMap = 
			new HashMap<String, Integer>();
	
	// For keeping track of corresponding arrivals
	private List<Arrival> arrivals = new ArrayList<Arrival>();
	
	private static final Logger logger = LoggerFactory
			.getLogger(ArrDepGeneratorMissionBayImpl.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor. Initializes sfmtaStopMap to have all the stops that
	 * need to be tracked.
	 */
	public ArrDepGeneratorMissionBayImpl() {
		logger.info("Initializing class ArrDepGeneratorMissionBayImpl for "
				+ "sending arrival/departure info to SFMTA API.");
		
		// Specify the missionBay stopID and the SFMTA stopID of the
		// stops that need to be tracked.
		sfmtaStopMap.put("calcream", 6694);
	}
	
	/**
	 * Calls the superclass createArrivalTime() but also stores arrival time at
	 * SFMTA stops so that when corresponding departure is determined both the
	 * arrival and the departure time for that stop can be written to the SFMTA
	 * API.
	 */
	protected Arrival createArrivalTime(VehicleState vehicleState,
			long arrivalTime, Block block, int tripIndex, int stopPathIndex, String stopPathId) {
		// Call parent class createArrivalTime()
		Arrival arrival = super.createArrivalTime(vehicleState, arrivalTime, block,
				tripIndex, stopPathIndex);
		
		// If special SFMTA stop then store the arrival time
		if (sfmtaStopMap.containsKey(arrival.getStopId())) {
			logger.debug("Arrived at SFMTA stop: {}", arrival);
			arrivals.add(arrival);
		}
		
		return arrival;
	}

	/**
	 * Calls the superclass createDepartureTime() but for SFMTA stops also
	 * writes both the arrival and the departure time for that stop can be
	 * written to the SFMTA API.
	 */
	protected Departure createDepartureTime(VehicleState vehicleState,
			long departureTime, Block block, int tripIndex, int stopPathIndex, Long dwellTime) {
		// Call parent class createDepartureTime()
		Departure departure = super.createDepartureTime(vehicleState, departureTime, block,
				tripIndex, stopPathIndex, dwellTime);
		
		// If special SFMTA stop then write arrival/departure time to SFMTA API
		if (sfmtaStopMap.containsKey(departure.getStopId())) {
			logger.debug("Departed SFMTA stop: {}", departure);
			
			// If arrival is more than 10 minutes old then must have missed
			// the corresponding departure so should eliminate the arrival
			// from the list.
			Iterator<Arrival> it = arrivals.iterator();
			while (it.hasNext()) {
				Arrival arrival = it.next();			
				if (arrival.getTime() < 
						departure.getTime() - 10*Time.MS_PER_MIN) {
					it.remove();
					logger.error("Arrival was more than 10 minutes old without "
							+ "finding corresponding departure. Therefore "
							+ "dropping the arrival. {}", arrival);
				}
			}
			
			// Find the corresponding arrival for the departure and push it 
			// to the SFMTA API
			for (Arrival arrival : arrivals) {
				if (arrival.getVehicleId().equals(departure.getVehicleId())
						&& arrival.getStopId().equals(departure.getStopId())) {
					// For the departure found the matching arrival
					logger.info("Found arrival/departure pair for SFMTA stop. "
							+ "{} {} ",	arrival, departure);
					
					// Post the stop report to the SFMTA API
					String vehicleId = departure.getVehicleId();
					int stopId = sfmtaStopMap.get(departure.getStopId());
					Location stopLoc = departure.getStop().getLoc();					
					SfmtaApiCaller.postStopReport(vehicleId, stopId,
							stopLoc.getLat(), stopLoc.getLon(),
							arrival.getDate(), departure.getDate());
					
					// Matched this arrival so remove it from the list
					arrivals.remove(arrival);
					
					// Push it to the API
					break;
				}
			}
		}
		
		return departure;
	}
}
