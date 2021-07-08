package org.transitclock.core.blockAssigner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread safe caching of external block assignments
 */
public class BlockAssignerCache {


    private ConcurrentMap<String, ArrayList<String>> _blockAssignmentsByVehicleId = new ConcurrentHashMap<>();
    private IntegerConfigValue _cacheTTL = null;
    private BlockAssignerUpdater _updater;

    private static final Logger logger = LoggerFactory
            .getLogger(BlockAssignerCache.class);

    public BlockAssignerCache(BlockAssignerUpdater updater, IntegerConfigValue cacheTTL) {
        this._updater = updater;
        this._cacheTTL = cacheTTL;
    }


    /**
     * retrieve a copy of the cache, potentially forcing a cache update AFTER the read
     * for performance reasons.
     * @return
     */
    public ConcurrentMap<String, ArrayList<String>> getBlockAssignmentsByVehicleIdMap() {
        // we only update the cache if we are in use
        if (_updater.needsUpdate(_cacheTTL.getValue())) {
            // mark cache as updated regardless of success to prevent multiple concurrent attempts
            _updater.markUpdated();
            asyncUpdate();
        }
        return _blockAssignmentsByVehicleId;
    }

    /**
     * perform an asynchronous update of the cache.  Managed internally, does not need to
     * be explicitly called.
     */
    public void asyncUpdate() {
        WorkerThread worker = new WorkerThread(_blockAssignmentsByVehicleId, _updater);
        new Thread(worker).start();
    }

    /**
     * perform a synchronous update of the cache.  Managed internally, does not need to be
     * explicitly called.
     */
    public void update() {
        new WorkerThread(_blockAssignmentsByVehicleId, _updater).run();
    }


    /**
     * Call the Updater on a separate thread.
     */
    private static class WorkerThread implements Runnable {

        private ConcurrentMap<String, ArrayList<String>> blockAssignmentsByVehicleIdMap = null;
        private BlockAssignerUpdater updater = null;
        public WorkerThread(ConcurrentMap<String, ArrayList<String>> blockAssignments, BlockAssignerUpdater updater) {
            this.blockAssignmentsByVehicleIdMap = blockAssignments;
            this.updater = updater;
        }

        public void run() {
            long start = System.currentTimeMillis();
            try {
                Map<String, ArrayList<String>> newBlockAssignmentsByVehicleIdMap = updater.getBlockAssignmentsByVehicleIdMap();
                if (newBlockAssignmentsByVehicleIdMap != null) {
                    // if the call fails or exception thrown preserve internal state so last value can be used
                    synchronized (blockAssignmentsByVehicleIdMap) {
                        blockAssignmentsByVehicleIdMap.clear();
                        blockAssignmentsByVehicleIdMap.putAll(newBlockAssignmentsByVehicleIdMap);
                    }
                }
            } catch (IOException ioe) {
                logger.info("Exception retrieving external block assignments", ioe);
            } finally {
                long stop = System.currentTimeMillis();
                logger.info("cache update in {}ms", (stop - start));
            }
        }
    }
}
