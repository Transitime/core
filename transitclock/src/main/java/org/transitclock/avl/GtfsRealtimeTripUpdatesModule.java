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
package org.transitclock.avl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.feed.gtfsRt.GtfsRtTripUpdatesReader;
import org.transitclock.modules.Module;
import org.transitclock.trip.PollUrlTripModule;

/**
 * For reading in feed of GTFS-realtime AVL data. Is used for both realtime
 * feeds and for when reading in a giant batch of data.
 *
 * @author SkiBu Smith
 *
 */
public class GtfsRealtimeTripUpdatesModule extends PollUrlTripModule {


    private static final Logger logger = LoggerFactory
            .getLogger(GtfsRealtimeTripUpdatesModule.class);

    private GtfsRtTripUpdatesReader reader;


    /********************** Member Functions **************************/

    /**
     * @param projectId
     */
    public GtfsRealtimeTripUpdatesModule(String projectId) {
        super(projectId);
        // GTFS-realtime is already binary so don't want to get compressed
        // version since that would just be a waste.
        reader = new GtfsRtTripUpdatesReader();
    }

    /**
     * Reads and processes the data. Called by AvlModule.run().
     * Reading GTFS-realtime doesn't use InputSteram so overriding
     * getAndProcessData().
     */
    @Override
    protected void getAndProcessData(String url) {
        logger.info("reading  {}", url);
        reader.process(url);
        logger.info("read complete");
        logger.info("processed feed {}", url);
    }

    /**
     * Just for debugging
     */
    public static void main(String[] args) {
        // Create a GtfsRealtimeModule for testing
        Module.start("org.transitclock.avl.GtfsRealtimeTripUpdatesModule");
    }

}
