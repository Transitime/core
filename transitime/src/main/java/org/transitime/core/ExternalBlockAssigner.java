package org.transitime.core;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.BooleanConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.configData.AvlConfig;
import org.transitime.configData.CoreConfig;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.Block;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Enable external AVL management integration, such as via a CSV web service / file
 */
public class ExternalBlockAssigner {


    private static BooleanConfigValue externalAssignerEnabled =
            new BooleanConfigValue(
                    "transitime.externalAssignerEnabled",
                    false,
                    "Set to true to enable the manual assignment feature where "
                            + "the system tries to assign vehicle to an available block");

    static StringConfigValue externalAssignerUrl =
            new StringConfigValue("transitime.externalAssignerUrl",
                    null,
                    "Set to the URL or file of the external AVL feed");

    static StringConfigValue blockParam =
            new StringConfigValue("transitime.externalAssigner.block_param",
                    "block",
                    "CSV header for the block of the external AVL feed");


    static StringConfigValue vehicleParam =
            new StringConfigValue("transitime.externalAssigner.vehicle_param",
                    "vehicle",
                    "CSV header for the vehicle of the external AVL feed");

    private static final Logger logger = LoggerFactory
            .getLogger(ExternalBlockAssigner.class);

    ExternalBlockAssigner() {
        // force singleton usage
    }

    /**
     * Returns true if the externalBlockAssigner is actually enabled.
     *
     * @return true if enabled
     */
    public static boolean enabled() {
        return externalAssignerEnabled.getValue();
    }

    private static ExternalBlockAssigner INSTANCE = new ExternalBlockAssigner();

    /**
     * retrieve singleton instance of ExternalBlockAssigner
     * @return
     */
    public static ExternalBlockAssigner getInstance() {
        return INSTANCE;
    }

    /**
     * for the given avlReport, regardless of the current block assignment,
     * check configured external BLOCK_FEED (\"transitime.externalAssignerUrl\")
     * and if block is present and active override the current assignment.
     * @param avlReport
     * @return
     */
    public String getActiveAssignmentForVehicle(AvlReport avlReport) {
        for (String assignmentId : blockMatch(avlReport.getVehicleId())) {
            if (assignmentId != null) {
                Block requestedBlock = getActiveBlock(assignmentId, avlReport.getDate());
                if (requestedBlock != null) {
                    logger.info("found active block {} for vehicle {}", assignmentId, avlReport.getVehicleId());
                    return assignmentId;
                }
            }
        }
        logger.info("no active external assignment for vehicle {}", avlReport.getVehicleId());
        return null;
    }

    /**
     * if the blockId is active within window of service date / now retrieve the
     * entire block.
     * @param assignmentId
     * @param serviceDate
     * @return
     */
    Block getActiveBlock(String assignmentId, Date serviceDate) {
        Collection<Block> dbBlocks =
                Core.getInstance().getDbConfig().getBlocksForAllServiceIds(assignmentId);
        if (dbBlocks == null) {
            logger.warn("no block found for {}", assignmentId);
            return null;
        }
        for (Block requestedBlock : dbBlocks) {
            if (requestedBlock.isActive(serviceDate,
                    CoreConfig.getAllowableEarlySeconds(),
                    CoreConfig.getAllowableLateSeconds())) {
                return requestedBlock;
            }
        }
        return null;
    }

    /**
     * return a (potentially list) of blocks specified for given vehicleId.
     * @param vehicleId
     * @return
     */
    private ArrayList<String> blockMatch(String vehicleId) {
        // check web service for vehicle_id match
        ArrayList<String> blocks = getBlockAssignmentsByVehicleIdMap().get(vehicleId);
        if (blocks != null)
            return blocks;
        // return empty list for easy inclusion in loop
        return new ArrayList<>();
    }

    /**
     * retrieve a list of block ids indexed on vehicleId.  Does no caching.
     * @return
     */
    Map<String, ArrayList<String>> getBlockAssignmentsByVehicleIdMap() {
        Map<String, ArrayList<String>> blockAssignmentsByVehicleId = new HashMap<>();
        CSVRecord record = null;
        CSVFormat formatter = CSVFormat.DEFAULT.withHeader().withCommentMarker('-');
        InputStream feed = null;
        try {
            feed = getBlockAssignmentsByVehicleIdFeed();
            if (feed == null) {
                logger.warn("In ExternalBlockAssigner but \"transitime.externalAssignerUrl\" not defined!");
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
            logger.error("Exception retrieving feed {} at {}", any, externalAssignerUrl.getValue(), any);
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
     * convenience method to insert into the indexed map.
     * @param blockAssignmentsByVehicleId
     * @param assignment
     */
    private void insert(Map<String, ArrayList<String>> blockAssignmentsByVehicleId, VehicleAssignment assignment) {
        String vehicleId = assignment.vehicleId;
        String blockId = assignment.blockId;
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
     * connect an retrieve as input stream the configured url.  Does no caching.
     * @return
     * @throws Exception
     */
    InputStream getBlockAssignmentsByVehicleIdFeed() throws Exception {
        if (externalAssignerUrl.getValue() == null) return null;  // we are not configured
        URL url = new URL(externalAssignerUrl.getValue());
        URLConnection con = url.openConnection();
        int timeoutMsec = AvlConfig.getAvlFeedTimeoutInMSecs();
        con.setConnectTimeout(timeoutMsec);
        con.setReadTimeout(timeoutMsec);
        InputStream in = con.getInputStream();
        return in;
    }

    /**
     * wrangle a CSVRecord into a blockId and a vehicleId based on configuration.
     * @param record
     * @return
     */
    private VehicleAssignment handleRecord(CSVRecord record) {
        String vehicleId = record.get(vehicleParam.getValue());
        String blockId = record.get(blockParam.getValue());
        if (StringUtils.isNoneBlank(vehicleId) && StringUtils.isNotBlank(blockId)) {
            return new VehicleAssignment(vehicleId, blockId);
        }
        logger.warn("discarding record {}", record.toString());
        return null;
    }

    private static class VehicleAssignment {
        private String vehicleId;
        private String blockId;

        public VehicleAssignment(String vehicleId, String blockId) {
            this.vehicleId = vehicleId;
            this.blockId = blockId;
        }
    }
}
