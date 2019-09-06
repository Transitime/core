package org.transitclock.core.blockAssigner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.StringConfigValue;
import org.transitclock.configData.AvlConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Asynchronous updates of external block assignments.
 */
public class BlockAssignerUpdater {

    private StringConfigValue _url = null;
    private StringConfigValue _blockParam = null;
    private StringConfigValue _vehicleParam = null;

    private AtomicLong _lastRefreshTimeInMillis = new AtomicLong(0);

    private static final Logger logger = LoggerFactory
            .getLogger(BlockAssignerUpdater.class);

    public BlockAssignerUpdater(StringConfigValue externalAssignerUrl,
                                StringConfigValue blockParam,
                                StringConfigValue vehicleParam) {
        this._url = externalAssignerUrl;
        this._blockParam = blockParam;
        this._vehicleParam = vehicleParam;
    }

    /**
     * Read the web service in an assumed CSV format and return as a map
     * @throws IOException on any communicaton issues
     * @return a map, possibly empty, on successful retrieval and parsing of the external feed
     */
    public Map<String, ArrayList<String>>  getBlockAssignmentsByVehicleIdMap() throws IOException {
        Map<String, ArrayList<String>> blockAssignmentsByVehicleId = new HashMap<>();
        CSVRecord record = null;
        CSVFormat formatter = CSVFormat.DEFAULT.withHeader().withCommentMarker('-');
        InputStream feed = null;
        try {
            feed = getBlockAssignmentsByVehicleIdFeed();
            if (feed == null) {
                logger.warn("In ExternalBlockAssigner but \"transitclock.externalAssignerUrl\" not defined!");
                return blockAssignmentsByVehicleId;
            }
            Iterable<CSVRecord> records = formatter.parse(new BufferedReader(new InputStreamReader(feed)));
            Iterator<CSVRecord> iterator = records.iterator();
            while (iterator.hasNext()) {
                record = iterator.next();
                logger.info("record=|" + record.toString() + "|");
                if (record.size() == 0)
                    continue;
                try {
                    VehicleAssignment assignment = handleRecord(record);
                    if (assignment != null) {
                        insert(blockAssignmentsByVehicleId, assignment);
                    }
                } catch (Exception any) {
                    logger.warn("bad record {}", record.toString());
                }
            }

        } catch (Exception any) {
            // we don't throw exception so that last valid value can be used
            logger.error("Exception retrieving feed {} at {}", any, _url, any);
        } finally {
            if (feed != null) {
                try {
                    feed.close();
                } catch (Exception bury) {
                    // don't hide actual exception
                }
            }
        }
        return blockAssignmentsByVehicleId;
    }


    /**
     * connect and retrieve as input stream the configured _url.  Does no caching.
     * @return
     * @throws Exception
     */
    public InputStream getBlockAssignmentsByVehicleIdFeed() throws IOException {
        if (_url.getValue() == null) return null;  // we are not configured
        URL url = new URL(_url.getValue());
        URLConnection con = url.openConnection();
        int timeoutMsec = AvlConfig.getAvlFeedTimeoutInMSecs();
        con.setConnectTimeout(timeoutMsec);
        con.setReadTimeout(timeoutMsec);
        InputStream in = con.getInputStream();
        return in;
    }


    /**
     * test if cache is due for a refresh based on given TTL in seconds
     * @param cacheTTL
     * @return
     */
    public boolean needsUpdate(int cacheTTL) {
        if ((System.currentTimeMillis() - _lastRefreshTimeInMillis.get()) / 1000 > cacheTTL) {
            return true;
        }
        return false;
    }

    /**
     * mark the update as successful
     */
    public void markUpdated() {
        _lastRefreshTimeInMillis.set(System.currentTimeMillis());
    }


    /**
     * convenience method to insert into the indexed map.
     * @param blockAssignmentsByVehicleId
     * @param assignment
     */
    private void insert(Map<String, ArrayList<String>> blockAssignmentsByVehicleId, VehicleAssignment assignment) {
        String vehicleId = assignment.getVehicleId();
        String blockId = assignment.getBlockId();
        ArrayList<String> blocks;
        if (!blockAssignmentsByVehicleId.containsKey(vehicleId)) {
            blocks = new ArrayList<>();
        } else {
            blocks = blockAssignmentsByVehicleId.get(vehicleId);
        }
        blocks.add(blockId);
        logger.debug("blockAssignmentsByVehicleId({})={}", vehicleId, blocks);
        blockAssignmentsByVehicleId.put(vehicleId, blocks);
    }

    /**
     * wrangle a CSVRecord into a blockId and a vehicleId based on configuration.
     * @param record
     * @return
     */
    private VehicleAssignment handleRecord(CSVRecord record) {
        String vehicleId = record.get(_vehicleParam.getValue());
        String blockId = record.get(_blockParam.getValue());
        if (StringUtils.isNoneBlank(vehicleId) && StringUtils.isNotBlank(blockId)) {
            return new VehicleAssignment(vehicleId, blockId);
        }
        logger.warn("discarding record {}", record.toString());
        return null;
    }


}
