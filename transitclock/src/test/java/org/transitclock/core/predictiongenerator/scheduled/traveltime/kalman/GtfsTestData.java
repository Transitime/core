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
package org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman;

import org.transitclock.db.structs.Stop;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.gtfs.TitleFormatter;
import org.transitclock.gtfs.gtfsStructs.GtfsRoute;

import java.util.Date;

/**
 * Testing version of GtfsData that allows some hooks for test data.
 */
public class GtfsTestData extends GtfsData {
  public KalmanDataGenerator dataGenerator;
  private long referenceTime;

  public GtfsTestData(long referenceTime, int configRev, String notes, Date zipFileLastModifiedTime, boolean shouldStoreNewRevs, boolean shouldDeleteRevs, String projectId, String gtfsDirectoryName, String supplementDir, double pathOffsetDistance, double maxStopToPathDistance, double maxDistanceForEliminatingVertices, int defaultWaitTimeAtStopMsec, double maxSpeedKph, double maxTravelTimeSegmentLength, boolean trimPathBeforeFirstStopOfTrip, TitleFormatter titleFormatter) {
    super(configRev, notes, zipFileLastModifiedTime, shouldStoreNewRevs, shouldDeleteRevs, projectId, gtfsDirectoryName, supplementDir, pathOffsetDistance, maxStopToPathDistance, maxDistanceForEliminatingVertices, defaultWaitTimeAtStopMsec, maxSpeedKph, maxTravelTimeSegmentLength, trimPathBeforeFirstStopOfTrip, titleFormatter);
    this.referenceTime = referenceTime;
    this.dataGenerator = new KalmanDataGenerator(referenceTime);
  }



  public GtfsRoute getGtfsRoute(String routeId) {
    return dataGenerator.getGtfsRoute();
  }

  public Stop getStop(String stopId) {
    Stop stop = dataGenerator.getStop();
    return stop;
  }

}
