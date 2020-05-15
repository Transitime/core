package org.transitclock.core.dataCache;

/**
 * Represents a Task that can be run in parallel.  See ParallelProcessor.
 */
public interface ParallelTask {
    void run() throws Exception;
}
