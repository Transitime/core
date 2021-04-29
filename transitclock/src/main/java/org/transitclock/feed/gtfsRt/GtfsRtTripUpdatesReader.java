package org.transitclock.feed.gtfsRt;

import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import org.transitclock.core.dataCache.canceledTrip.CanceledTripAndVehicleCache;

public class GtfsRtTripUpdatesReader extends  GtfsRtTripUpdatesReaderBase{

    /********************** Member Functions **************************/


    public GtfsRtTripUpdatesReader() { }

    @Override
    public void handleTrip(TripDescriptor trip) {
        return;
        // Empty
    }
}