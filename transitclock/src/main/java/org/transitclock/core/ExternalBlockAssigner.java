package org.transitclock.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.configData.CoreConfig;
import org.transitclock.core.blockAssigner.BlockAssignerCache;
import org.transitclock.core.blockAssigner.BlockAssignerUpdater;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.Block;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Enable external AVL management integration, such as via a CSV web service / file
 */
public class ExternalBlockAssigner {


    static BooleanConfigValue externalAssignerEnabled =
            new BooleanConfigValue(
                    "transitclock.externalAssignerEnabled",
                    false,
                    "Set to true to enable the manual assignment feature where "
                            + "the system tries to assign vehicle to an available block");

    static StringConfigValue externalAssignerUrl =
            new StringConfigValue("transitclock.externalAssignerUrl",
                    null,
                    "Set to the URL or file of the external AVL feed");

    static StringConfigValue blockParam =
            new StringConfigValue("transitclock.externalAssigner.block_param",
                    "block",
                    "CSV header for the block of the external AVL feed");


    static StringConfigValue vehicleParam =
            new StringConfigValue("transitclock.externalAssigner.vehicle_param",
                    "vehicle",
                    "CSV header for the vehicle of the external AVL feed");

    static IntegerConfigValue cacheTTL =
            new IntegerConfigValue("transitclock.externalAssigner.cacheTTL",
                    60,
                    "time in seconds to cache feed");

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
    private static BlockAssignerCache CACHE;
    private static BlockAssignerUpdater UPDATER;


    /**
     * retrieve singleton instance of ExternalBlockAssigner
     * @return
     */
    public static ExternalBlockAssigner getInstance() {
        if (CACHE == null) {
            // lazy instantiation of remaining members only if enabled
            UPDATER = new BlockAssignerUpdater(externalAssignerUrl, blockParam, vehicleParam);
            CACHE = new BlockAssignerCache(UPDATER, cacheTTL);
            forceUpdate();
        }
        return INSTANCE;
    }

    /**
     * reset internal state for unit tests.
     */
    static void reset() {
        UPDATER = null;
        CACHE = null;
        INSTANCE = new ExternalBlockAssigner();
    }

    /**
     * for the given avlReport, regardless of the current block assignment,
     * check configured external BLOCK_FEED (\"transitclock.externalAssignerUrl\")
     * and if block is present and active override the current assignment.
     * @param avlReport
     * @return
     */
    public String getActiveAssignmentForVehicle(AvlReport avlReport) {
        int possibleAssignments = 0;
        for (String assignmentId : blockMatch(avlReport.getVehicleId())) {
            logger.info("possible assignment for vehicle {} = {}", avlReport.getVehicleId(), assignmentId);
            possibleAssignments++;
            if (assignmentId != null) {
                int agencySeparator = assignmentId.lastIndexOf('_');
                if (agencySeparator != -1) {
                    assignmentId = assignmentId.substring(agencySeparator+1);
                }
                Block requestedBlock = getActiveBlock(assignmentId, avlReport.getDate());
                if (requestedBlock != null) {
                    logger.info("found active block {} for vehicle {}", assignmentId, avlReport.getVehicleId());
                    return assignmentId;
                } else {
                    logger.info("block {} mismatch for vehicle {}", assignmentId, avlReport.getVehicleId());
                }
            }
        }
        logger.info("no active external assignment for vehicle {} with {} possible assignments and cache= {}",
                avlReport.getVehicleId(), possibleAssignments, getBlockAssignmentsByVehicleIdMapFromCache().keySet());
        return null;
    }

    /**
     * if the blockId is active within window of service date / now retrieve the
     * entire block.
     * @param assignmentId
     * @param avlReportDate
     * @return
     */
    Block getActiveBlock(String assignmentId, Date avlReportDate) {
        Collection<Block> dbBlocks =
                Core.getInstance().getDbConfig().getBlocksForAllServiceIds(assignmentId);
        if (dbBlocks == null) {
            logger.warn("no block found for {}", assignmentId);
            return null;
        }
        logger.info("getActiveBlock({}, {}) found {} potential blocks: {}",
                assignmentId, avlReportDate, dbBlocks.size(), dbBlocks);
        for (Block requestedBlock : dbBlocks) {
            if (requestedBlock.isActive(avlReportDate,
                    CoreConfig.getAllowableEarlySeconds(),
                    -1 /* use endTime*/)) {
                return requestedBlock;
            } else {
                logger.info("requestedBlock {} is not active on serviceDate {}", requestedBlock.getId(), avlReportDate);
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
        ArrayList<String> blocks = getBlockAssignmentsByVehicleIdMapFromCache().get(vehicleId);
        if (blocks != null)
            return blocks;
        // return empty list for easy inclusion in loop
        return new ArrayList<>();
    }


    Map<String, ArrayList<String>> getBlockAssignmentsByVehicleIdMapFromCache() {
        return CACHE.getBlockAssignmentsByVehicleIdMap();
    }

    /**
     * retrieve a list of block ids indexed on vehicleId.  Does no caching.
     * @return
     */
    Map<String, ArrayList<String>> getBlockAssignmentsByVehicleIdMap() throws IOException {
        return UPDATER.getBlockAssignmentsByVehicleIdMap();
    }

    /**
     * retrieve the raw webservice feed, package private for unit tests.
     */
    InputStream getBlockAssignmentsByVehicleIdFeed() throws Exception {
        if (enabled())
            return UPDATER.getBlockAssignmentsByVehicleIdFeed();
        return null;
    }

    /**
     * Force a sycnhronous update of the cache.  Managed internally, does not need to be
     * explicitly called.
     */
    static void forceUpdate() {
        CACHE.update();
    }



}
