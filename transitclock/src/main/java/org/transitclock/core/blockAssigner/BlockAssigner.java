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
package org.transitclock.core.blockAssigner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.ServiceUtilsImpl;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.utils.Time;

import java.util.Collection;

/**
 * Singleton class that handles block assignments from AVL feed.
 *
 * @author SkiBu Smith
 *
 */
public class BlockAssigner {

    // Singleton class
    private static BlockAssigner singleton = new BlockAssigner();

    private static final Logger logger =
            LoggerFactory.getLogger(BlockAssigner.class);

    /********************** Member Functions **************************/

    /**
     * Constructor private since singleton class
     */
    private BlockAssigner() {}

    /**
     * Returns the BlockAssigner singleton
     *
     * @return
     */
    public static BlockAssigner getInstance() {
        return singleton;
    }

    /**
     * Gets the appropriate block associated with the AvlReport. If the
     * assignment is a block assignment then first gets the proper serviceIds
     * that are active for the AVL timestamp, and then determines the
     * appropriate block using the serviceIds and the assignment from the AVL
     * report.
     * <p>
     * Works for block assignments, trip assignments, and trip short name
     * assignments. If the assignment not specified in AVL data or the block
     * could not be found for the serviceIds, it could not matched to the trip,
     * or it was a route assignment then null will be returned
     *
     * @param avlReport
     *            So can determine the assignment ID, and the time so that so
     *            that can determine the proper service ID.
     * @return Block corresponding to the time and blockId from AVL report, or
     *         null if could not determine block.
     */
    public Block getBlockAssignment(AvlReport avlReport) {
        // If vehicle has assignment...
        if (avlReport != null && avlReport.getAssignmentId() != null) {
            DbConfig config = Core.getInstance().getDbConfig();

            // If using block assignment...
            if (avlReport.isBlockIdAssignmentType()) {
                ServiceUtilsImpl serviceUtis = Core.getInstance().getServiceUtils();
                Collection<String> serviceIds =
                        serviceUtis.getServiceIds(avlReport.getDate());
                // Go through all current service IDs to find the block
                // that is currently active
                Block activeBlock = null;
                for (String serviceId : serviceIds) {
                    Block blockForServiceId = config.getBlock(serviceId,
                            avlReport.getAssignmentId());
                    // If there is a block for the current service ID
                    if (blockForServiceId != null) {
                        // If found a best match so far then remember it
                        if (activeBlock == null
                                || blockForServiceId.isActive(
                                avlReport.getTime(),
                                90 * Time.MIN_IN_SECS)) {
                            activeBlock = blockForServiceId;
                            logger.debug("For vehicleId={} and serviceId={} "
                                            + "the active block assignment from the "
                                            + "AVL feed is blockId={}",
                                    avlReport.getVehicleId(), serviceId,
                                    activeBlock.getId());
                        }
                    }
                }
                if (activeBlock == null) {
                    logger.error("For vehicleId={} AVL report specifies "
                                    + "blockId={} but block is not valid for "
                                    + "serviceIds={}",
                            avlReport.getVehicleId(),
                            avlReport.getAssignmentId(),
                            serviceIds);
                }
                return activeBlock;
            } else if (avlReport.isTripIdAssignmentType()) {
                // Using trip ID
                Trip trip = config.getTrip(avlReport.getAssignmentId());
                if (trip != null && trip.getBlock() != null) {
                    Block block = trip.getBlock();
                    logger.debug("For vehicleId={} the trip assignment from "
                                    + "the AVL feed is tripId={} which corresponds to "
                                    + "blockId={}",
                            avlReport.getVehicleId(),
                            avlReport.getAssignmentId(), block.getId());
                    return block;
                } else {
                    if (config.getServiceIdSuffix()) {
                        for (String serviceId : Core.getInstance().getDbConfig().getCurrentServiceIds()) {

                            Trip tripPrefix = getTripWithServiceIdSuffix(config, avlReport.getAssignmentId());

                            if (tripPrefix != null){
                                Block blockPrefix = tripPrefix.getBlock();
                                logger.debug("For vehicleId={} the trip assigngment from "
                                                + "the AVL feed is tripId={} and serviceId={} which corresponds to "
                                                + "blockId={}",
                                        avlReport.getVehicleId(),
                                        avlReport.getAssignmentId(),
                                        serviceId,
                                        blockPrefix.getId());
                                return blockPrefix;
                            }
                        }
                    }
                    logger.error("For vehicleId={} AVL report specifies " +
                                    "assignment tripId={} but that trip is not valid.",
                            avlReport.getVehicleId(),
                            avlReport.getAssignmentId());
                }
            } else if (avlReport.isTripShortNameAssignmentType()) {
                // Using trip short name
                String tripShortName = avlReport.getAssignmentId();
                Trip trip = config.getTripUsingTripShortName(tripShortName);
                if (trip != null) {
                    Block block = trip.getBlock();
                    logger.debug("For vehicleId={} the trip assignment from "
                                    + "the AVL feed is tripShortName={} which "
                                    + "corresponds to blockId={}",
                            avlReport.getVehicleId(), tripShortName,
                            block.getId());
                    return block;
                } else {
                    logger.error("For vehicleId={} AVL report specifies "
                                    + "assignment tripShortName={} but that trip is not "
                                    + "valid.",
                            avlReport.getVehicleId(), tripShortName);
                }
            }
        }

        // No valid block so return null
        return null;
    }

    public Trip getTripWithServiceIdSuffix(DbConfig config, String assignmentId) {
        for (String serviceId : Core.getInstance().getDbConfig().getCurrentServiceIds()) {
            Trip tripPrefix = config.getTrip(assignmentId + "-" + serviceId);
            int secondsIntoDay = 120 * Time.SEC_PER_MIN;
            if (tripPrefix != null
                    && tripPrefix.getBlock() != null
                    && tripPrefix.getBlock()
                    .isActive( Core.getInstance().getSystemTime(), secondsIntoDay)){
               return tripPrefix;
            }
        }
        return null;
    }

    /**
     * Returns the route ID specified in the AVL feed. If no route ID then
     * returns null.
     *
     * @param avlReport
     * @return The route ID or null if none assigned
     */
    public String getRouteIdAssignment(AvlReport avlReport) {
        if (avlReport != null
                && avlReport.getAssignmentId() != null
                && avlReport.isRouteIdAssignmentType()) {
            // Route ID specified so return it
            return avlReport.getAssignmentId();
        } else {
            // No route ID specified in AVL feed so return null
            return null;
        }

    }
}
