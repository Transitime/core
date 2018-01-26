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

package org.transitclock.custom.sfmta.delayTimes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.Location;
import org.transitclock.utils.Geo;
import org.transitclock.utils.StringUtils;
import org.transitclock.utils.Time;

/**
 * An experiment for determining using GPS how much time is lost
 * when a bus has to stop at 4-way stop. Used to better understand
 * planned changes for the 71-Haight.
 *
 * @author SkiBu Smith
 *
 */
public class SfmtaTrackerDataProcessor {

	// Once found top speed index MAX_DELTA specifies how far up and down
	// list to look for the begin loc and the end loc to be used for 
	// determining top speed. A higher value prevents a bad high top 
	// speed that is just due to noise. But a value too high could cause
	// the true top speed to not be found.
	private final static int MAX_DELTA = 3;

	// How far a loc needs to be from intersection for it to be used
	// to determine top speed. For avoiding using nonsensical value
	// right near the intersection where the vehicle should actually
	// be stopped.
	private final static double MIN_RADIUS_FROM_INTERSECTION = 35.0;// FIXME changed to 15 to get Shrader data // 35.0;

	private static final Logger logger = LoggerFactory
			.getLogger(SfmtaTrackerDataProcessor.class);

	/********************** Member Functions **************************/

	private static class TopSpeedLoc {
		private int topSpeedIdx;
		private double topSpeed;
		private Loc loc;
		
		private Location getLocation() {
			return new Location(loc.lat, loc.lon);
		}
	}
	
	private static double speed(Loc loc1, Loc loc2) {
		double distanceBetweenLocs = 
				Geo.distanceHaversine(loc1.getLocation(), loc2.getLocation());
		long timeDelta = loc2.epochTime - loc1.epochTime;
		double speedMetersPerSecond = 1000.0 * distanceBetweenLocs / timeDelta;

		return speedMetersPerSecond;
	}
	
	/**
	 * Determines top speed for the locations. Since speed usually not available
	 * directly on iPhone when using Javascript this method looks at two
	 * subsequent locations in the array.
	 * 
	 * @param locs
	 * @param intersectionLoc
	 *            Location of intersection that matches must be away from
	 * @param minRadiusFromIntersection
	 *            How far matches need to be away from intersection to be used
	 *            as top speed index. Prevents using noisy value.
	 * @return
	 */
	private static TopSpeedLoc topSpeed(List<Loc> locs, Location intersectionLoc,
			double minRadiusFromIntersection) {
		if (locs.size() < 2)
			return null;
		
		// Determine the top speed between the locs
		double topSpeed = 0.0;
		int topSpeedIdx = -1;
		
		// Go through each loc for the segment to determine the top speed.
		// But don't use the first or last set of locs to determine speed
		// since those are too close to the intersection
		for (int i=1; i<locs.size()-2; ++i) {
			Loc loc1 = locs.get(i);
			Loc loc2 = locs.get(i+1);
			
			// Make sure not too close to the intersection. This check prevents
			// from using an index that generates a high speed just because it
			// noisy.
			double distanceFromIntersection = 
					Geo.distance(intersectionLoc, loc1.getLocation());
			if (distanceFromIntersection < minRadiusFromIntersection) {
				logger.debug("For i={} was {}m from intersection which is too "
						+ "close, so not using for finding top speed index. epoch={} {} {}", 
						i, StringUtils.oneDigitFormat(distanceFromIntersection), 
						Time.timeStrMsec(loc1.epochTime), loc1.epochTime, loc1.getLocation());
				continue;
			}
			
			double speedMetersPerSecond = speed(loc1, loc2);
			
			// Log info about speed for the current index
			double distance = Geo.distance(new Location(loc1.lat, loc1.lon), 
					new Location(loc2.lat, loc2.lon));
			double elapsedTime = (loc2.epochTime-loc1.epochTime)/1000.0;
			logger.debug("speed for i={} is {}m/s or {}mph latLon={},{} "
					+ "distance={}m time={}s epoch={} {}", 
					i, StringUtils.oneDigitFormat(speedMetersPerSecond), 
					StringUtils.oneDigitFormat(speedMetersPerSecond*Geo.MPS_TO_MPH),
					StringUtils.sixDigitFormat(loc1.lat),
					StringUtils.sixDigitFormat(loc1.lon),
					StringUtils.oneDigitFormat(distance),
					StringUtils.twoDigitFormat(elapsedTime),
					Time.timeStrMsec(loc1.epochTime), loc1.epochTime);
			
			if (speedMetersPerSecond > topSpeed) {
				topSpeed = speedMetersPerSecond;
				topSpeedIdx = i;
			}
		}
		
		// If no valid speed data (all points to close to intersection) return bad match
		if (topSpeedIdx < 0)
			return null;
		
		// Don't want to use just one data point since that would provide
		// a really noisy speed. So if there are previous and subsequent matches,
		// use those.
		int lowerIndex = Math.max(1, topSpeedIdx-MAX_DELTA);
		int upperIndex = Math.min(locs.size()-1, topSpeedIdx+MAX_DELTA);
		
		Loc loc1 = locs.get(lowerIndex);
		Loc loc2 = locs.get(upperIndex);
		double speedMetersPerSecond = speed(loc1, loc2);
		
		// If speed unreasonable (greater than 24mph) then cap it
		if (speedMetersPerSecond > 24.0 * Geo.MPH_TO_MPS)
			speedMetersPerSecond = 24.0 * Geo.MPH_TO_MPS;
		
		// Log info
		double distance = Geo.distance(new Location(loc1.lat, loc1.lon), 
				new Location(loc2.lat, loc2.lon));
		double elapsedTime = (loc2.epochTime-loc1.epochTime)/1000.0;
	    logger.debug("Returned speed={}m/s or {} mph "
	    		+ "topSpeedIdx={} lowerIndex={} upperIndex={} "
	    		+ "distance={}m time={}s",
	    		StringUtils.oneDigitFormat(speedMetersPerSecond),
	    		StringUtils.oneDigitFormat(speedMetersPerSecond*Geo.MPS_TO_MPH),
	    		topSpeedIdx, lowerIndex, upperIndex, 
	    		StringUtils.oneDigitFormat(distance), elapsedTime);
	    
		TopSpeedLoc t = new TopSpeedLoc();
		t.topSpeedIdx = topSpeedIdx;
		t.topSpeed = speedMetersPerSecond;
		t.loc = locs.get(topSpeedIdx);
		return t;
	}
	
	private static void processIntersectionMatch(Intersection intersection,
			List<Loc> matchesToV1, List<Loc> matchesToV2) {
		if (matchesToV1.size() < 2 || matchesToV2.size() < 2) {
			logger.debug("Didn't get at least 2 matches for both matchesToV1 "
					+ "and matchesToV2 so ignoring these matches.");
			return;
		}
		
		Location intersectionLoc = new Location(intersection.latStop, intersection.lonStop);

		logger.debug("Getting speed for v1");
		TopSpeedLoc v1TopSpeedLoc = topSpeed(matchesToV1, 
				intersectionLoc, MIN_RADIUS_FROM_INTERSECTION);
		if (v1TopSpeedLoc == null) {
			logger.debug("No valid speed points for v1");
			return;
		}
		logger.debug("Filtered top speed for v1={}m/s or {}mph at topSpeedIdx={} for {}", 
				StringUtils.oneDigitFormat(v1TopSpeedLoc.topSpeed), 
				StringUtils.oneDigitFormat(v1TopSpeedLoc.topSpeed/Geo.MPH_TO_MPS),
				v1TopSpeedLoc.topSpeedIdx,
				v1TopSpeedLoc.loc);
		
		logger.debug("Getting speed for v2");
		TopSpeedLoc v2TopSpeedLoc = topSpeed(matchesToV2,
				intersectionLoc, MIN_RADIUS_FROM_INTERSECTION);
		if (v2TopSpeedLoc == null) {
			logger.debug("No valid speed points for v2");
			return;
		}
		logger.debug("Filtered top speed for v2={}m/s or {}mph at topSpeedIdx={} for {}", 
				StringUtils.oneDigitFormat(v2TopSpeedLoc.topSpeed), 
				StringUtils.oneDigitFormat(v2TopSpeedLoc.topSpeed/Geo.MPH_TO_MPS),
				v2TopSpeedLoc.topSpeedIdx,
				v2TopSpeedLoc.loc);
		
		double actualTimeBetweenLocs = (v2TopSpeedLoc.loc.epochTime - v1TopSpeedLoc.loc.epochTime) / 1000.0;
		
		double distanceBetweenLocs = Geo.distanceHaversine(
				v1TopSpeedLoc.getLocation(), v2TopSpeedLoc.getLocation());
		
		double averageSpeed = (v1TopSpeedLoc.topSpeed + v2TopSpeedLoc.topSpeed) / 2.0;

		double noStoppingTimeBetweenLocs = distanceBetweenLocs / averageSpeed;
		
		logger.info("RESULT: difference={}s actualTimeBetweenLocs={}s "
				+ "noStoppingTimeBetweenLocs={}s distance={}m "
				+ "averageSpeed={}mph averageSpeed={}m/s time={} loc1={} loc2={}",
				StringUtils.oneDigitFormat(actualTimeBetweenLocs - noStoppingTimeBetweenLocs),
				StringUtils.oneDigitFormat(actualTimeBetweenLocs),
				StringUtils.oneDigitFormat(noStoppingTimeBetweenLocs),
				StringUtils.oneDigitFormat(distanceBetweenLocs),
				StringUtils.oneDigitFormat(averageSpeed/Geo.MPH_TO_MPS),
				StringUtils.oneDigitFormat(averageSpeed),
				new Date(v1TopSpeedLoc.loc.epochTime),
				v1TopSpeedLoc.getLocation(), v2TopSpeedLoc.getLocation());
		logger.debug("");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String intersectionsFileName = args[0];
		String locationsFileName = args[1];
		
		List<Intersection> intersections = Intersection
				.readIntersections(intersectionsFileName);
		List<Loc> locs = Loc.readLocs(locationsFileName);
		
		for (int locCnt=0; locCnt<locs.size(); ++locCnt) {
			Loc loc = locs.get(locCnt);
			
			for (Intersection intersection : intersections) {
				double distanceToV1 = intersection.matchToV1(loc);
				if (!Double.isNaN(distanceToV1)) {
					// Found a match to an intersection so now process
					// subsequent locations until it doesn't match anymore
					List<Loc> matchesToV1 = new ArrayList<Loc>();
					List<Loc> matchesToV2 = new ArrayList<Loc>();
					
					matchesToV1.add(loc);
					
					while (++locCnt < locs.size()) {
						loc = locs.get(locCnt);
						distanceToV1 = intersection.matchToV1(loc);
						if (!Double.isNaN(distanceToV1)) {
							matchesToV1.add(loc);
						} else {
							double distanceToV2 = intersection.matchToV2(loc);
							if (!Double.isNaN(distanceToV2)) {
								matchesToV2.add(loc);
							} else {
								// Doesn't match v1 nor v2 so done getting matches
								// to intersection
								break;
							}
						}
					}
					
					logger.debug("Calling processIntersectionMatch() for "
							+ "\"{}\" for locCnt={}", 
							intersection.name, locCnt);
					processIntersectionMatch(intersection, matchesToV1, matchesToV2);
					
					// Found a match to an intersection so don't need to look
					// at other intersections for this loc
					break;
				}
			}
		}
	}

}
