package org.transitclock.feed.gtfsRt;

import com.google.protobuf.CodedInputStream;
import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.dataCache.SkippedStopsManager;
import org.transitclock.ipc.data.IpcSkippedStop;
import org.transitclock.utils.IntervalTimer;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.*;

public abstract class GtfsRtTripUpdatesReaderBase {

    private static final Logger logger = LoggerFactory
            .getLogger(GtfsRtTripUpdatesReaderBase.class);

    /********************** Member Functions **************************/

    public GtfsRtTripUpdatesReaderBase() { }

    /**
     * Actually processes the GTFS-realtime file and calls handleAvlReport()
     * for each AvlReport.
     */
    public synchronized void process(String urlString) {
        try {
            logger.info("Getting GTFS-realtime AVL data from URL={} ...",
                    urlString);
            IntervalTimer timer = new IntervalTimer();

            URI uri = new URI(urlString);
            URL url = uri.toURL();

            // Create a CodedInputStream instead of just a regular InputStream
            // so that can change the size limit. Otherwise if file is greater
            // than 64MB get an exception.
            InputStream inputStream = url.openStream();
            CodedInputStream codedStream =
                    CodedInputStream.newInstance(inputStream);
            // What to use instead of default 64MB limit
            final int GTFS_SIZE_LIMIT = 200000000;
            codedStream.setSizeLimit(GTFS_SIZE_LIMIT);

            // Actual read in the data into a protobuffer FeedMessage object.
            // Would prefer to do this one VehiclePosition at a time using
            // something like VehiclePosition.parseFrom(codedStream) so that
            // wouldn't have to load entire protobuffer file into memory. But
            // it never seemed to complete, even for just a single call to
            // parseFrom(). Therefore loading in entire file at once.
            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(codedStream);
            logger.info("Parsing GTFS-realtime file into a FeedMessage took " +
                    "{} msec", timer.elapsedMsec());

            // Process each individual VehiclePostions message
            processMessage(feed);
            inputStream.close();
        } catch (Exception e) {
            logger.error("Exception when reading GTFS-realtime data from " +
                            "URL {}",
                    urlString, e);
        }
    }


    /**
     * Goes through each entity and passes TripDescriptor to
     *
     * @param message
     *            Contains all of the VehiclePosition objects
     * @return List of AvlReports
     */
    private void processMessage(FeedMessage message) {
        logger.info("Processing each individual AvlReport...");
        IntervalTimer timer = new IntervalTimer();

        Map<String, HashSet<IpcSkippedStop>> skippedStopsMap = new HashMap<>();

        // For each entity/vehicle process the data
        int counter = 0;
        for (FeedEntity entity : message.getEntityList()) {
            // If no trip in the entity then nothing to process
            if (!entity.hasTripUpdate())
                continue;

            // Get the object describing the trip
            TripUpdate tripUpdate = entity.getTripUpdate();

            TripDescriptor trip = getTrip(tripUpdate);

            if(trip == null)
                continue;

            HashSet<IpcSkippedStop> skippedStops = new HashSet<>();
            for(TripUpdate.StopTimeUpdate stopTimeUpdate : tripUpdate.getStopTimeUpdateList()){
                if(stopTimeUpdate.hasScheduleRelationship() &&
                        stopTimeUpdate.getScheduleRelationship() == TripUpdate.StopTimeUpdate.ScheduleRelationship.SKIPPED){
                    skippedStops.add(new IpcSkippedStop(stopTimeUpdate.getStopId(), stopTimeUpdate.getStopSequence()));
                }
            }

            skippedStopsMap.put(trip.getTripId(), skippedStops);

            // The callback for each TripDescriptor
            handleTrip(trip);

            ++counter;
        }
        if(SkippedStopsManager.getInstance() != null) {
            SkippedStopsManager.getInstance().putAll(skippedStopsMap);
        }

        logger.info("Successfully processed {} Trips from " +
                        "GTFS-realtime Trip Updates feed in {} msec",
                counter, timer.elapsedMsec());


    }

    /**
     * To be overridden by superclass. Is called each time an
     * AvlReport is handled.
     *
     * @param tripDescriptor
     */
    protected abstract void handleTrip(TripDescriptor tripDescriptor);

    private static TripDescriptor getTrip(TripUpdate tripUpdate){
        if (!tripUpdate.hasTrip()) {
            return null;
        }
        return   tripUpdate.getTrip();
    }
}