package org.transitclock.feed.gtfsRt;

import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import org.transitclock.core.dataCache.CanceledTripManager;

public class GtfsRtTripUpdatesReader extends  GtfsRtTripUpdatesReaderBase{

    /********************** Member Functions **************************/
    private CanceledTripManager canceledTripManager;


    public GtfsRtTripUpdatesReader() {
        canceledTripManager = CanceledTripManager.getInstance();
    }

    @Override
    public void handleTrip(TripDescriptor trip) {
        return;
        // Empty
    }
}