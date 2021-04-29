package org.transitclock.feed.gtfsRt;

import com.google.protobuf.CodedInputStream;
import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.StringConfigValue;
import org.transitclock.core.blockAssigner.BlockAssigner;
import org.transitclock.core.dataCache.canceledTrip.CanceledTripCache;
import org.transitclock.core.dataCache.canceledTrip.CanceledTripKey;
import org.transitclock.core.dataCache.canceledTrip.CanceledTripAndVehicleCache;
import org.transitclock.core.dataCache.SkippedStopsManager;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.ipc.data.IpcCanceledTrip;
import org.transitclock.ipc.data.IpcSkippedStop;
import org.transitclock.utils.IntervalTimer;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class GtfsRtTripUpdatesReaderBase {

    private static final Logger logger = LoggerFactory
            .getLogger(GtfsRtTripUpdatesReaderBase.class);

    private static StringConfigValue gtfsRealtimeHeaderKey =
            new StringConfigValue("transitclock.avl.apiKeyHeader",
                    null,
                    "api key header value if necessary, null if not needed");

    private static StringConfigValue gtfsRealtimeHeaderValue =
            new StringConfigValue("transitclock.avl.apiKeyValue",
                    null,
                    "api key value if necessary, null if not needed");

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

            HttpURLConnection
                    connection = (HttpURLConnection) url.openConnection();

            if (gtfsRealtimeHeaderKey.getValue() != null &&
                    gtfsRealtimeHeaderValue.getValue() != null) {
                connection.addRequestProperty(gtfsRealtimeHeaderKey.getValue(), gtfsRealtimeHeaderValue.getValue());
                connection.addRequestProperty("Cache-Control", "no-cache");
            }

            // Create a CodedInputStream instead of just a regular InputStream
            // so that can change the size limit. Otherwise if file is greater
            // than 64MB get an exception.
            InputStream inputStream = connection.getInputStream();
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
        Map<String, IpcCanceledTrip> cancelledTripsMap = new HashMap<>();

        // For each entity/vehicle process the data
        int counter = 0;
        for (FeedEntity entity : message.getEntityList()) {
            // If no trip in the entity then nothing to process
            if (!entity.hasTripUpdate())
                continue;

            // Get the object describing the trip
            TripUpdate tripUpdate = entity.getTripUpdate();

            TripDescriptor tripDescriptor = getTrip(tripUpdate);
            VehicleDescriptor vehicleDescriptor = tripUpdate.getVehicle();

            if(tripDescriptor == null)
                continue;

            if (tripDescriptor.hasTripId() &&
                tripDescriptor.hasScheduleRelationship() &&
                tripDescriptor.getScheduleRelationship() == TripDescriptor.ScheduleRelationship.CANCELED) {

                String tripId = getTripId(tripDescriptor);
                if (tripId == null)
                    continue;

                String vehicleId = null;
                if(vehicleDescriptor != null && vehicleDescriptor.hasId()){
                    vehicleId = vehicleDescriptor.getId();
                }

                String startDate = null;
                if(tripDescriptor.hasStartDate()){
                    startDate = tripDescriptor.getStartDate();
                }

                Long timestamp = null;
                if(tripUpdate.hasTimestamp()){
                    timestamp = tripUpdate.getTimestamp();
                }

                String routeId = null;
                if(tripDescriptor.hasRouteId()){
                    routeId = tripDescriptor.getRouteId();
                }

                IpcCanceledTrip canceledTrip = new IpcCanceledTrip(tripId, routeId, vehicleId, startDate, timestamp);

                cancelledTripsMap.put(tripId, canceledTrip);
                logger.debug("Adding canceledTrip to map {}", canceledTrip);

            }

            HashSet<IpcSkippedStop> skippedStops = new HashSet<>();
            String skippedTripId = null;
            boolean skippedTripAlreadyChecked = false;
            for(TripUpdate.StopTimeUpdate stopTimeUpdate : tripUpdate.getStopTimeUpdateList()){
                if(stopTimeUpdate.hasScheduleRelationship() &&
                        stopTimeUpdate.getScheduleRelationship() == TripUpdate.StopTimeUpdate.ScheduleRelationship.SKIPPED){
                    if(skippedTripId == null && !skippedTripAlreadyChecked){
                        skippedTripId = getTripId(tripDescriptor);
                        skippedTripAlreadyChecked = true;
                    }
                    if (skippedTripId == null)
                        continue;
                    IpcSkippedStop skippedStop = new IpcSkippedStop(vehicleDescriptor.getId(), stopTimeUpdate.getStopId(), stopTimeUpdate.getStopSequence());
                    skippedStops.add(skippedStop);
                    logger.debug("Adding skipped stop to map {}", skippedStop);
                    skippedStopsMap.put(skippedTripId, skippedStops);
                }
            }

            // The callback for each TripDescriptor
            handleTrip(tripDescriptor);

            ++counter;
        }

        CanceledTripCache.getInstance().putAll(cancelledTripsMap);
        SkippedStopsManager.getInstance().putAll(skippedStopsMap);

        logger.info("Successfully processed {} Trips from " +
                        "GTFS-realtime Trip Updates feed in {} msec",
                counter, timer.elapsedMsec());


    }

    private String getTripId(TripDescriptor tripDescriptor) {
        DbConfig config = Core.getInstance().getDbConfig();
        String tripId = tripDescriptor.getTripId();

        if(config.getServiceIdSuffix()){
            Trip trip = BlockAssigner.getInstance().getTripWithServiceIdSuffix(config,tripId);
            if (trip == null) {
                logger.info("missing trip {}", tripId);
                return null;
            }
            tripId = trip.getId();
        }
        return tripId;
    }

    private CanceledTripKey getCanceledTripKey(VehicleDescriptor vehicleDescriptor, String tripId){
        if(vehicleDescriptor.hasId()){
            return new CanceledTripKey(vehicleDescriptor.getId(), tripId);
        }
        return new CanceledTripKey(null, tripId);
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