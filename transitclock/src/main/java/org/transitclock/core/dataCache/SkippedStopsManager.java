package org.transitclock.core.dataCache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.transitclock.config.LongConfigValue;
import org.transitclock.ipc.data.IpcSkippedStop;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SkippedStopsManager {

    private static LongConfigValue skippedStopsCacheExpireSec =
            new LongConfigValue("transitclock.avl.skippedStopsCacheExpireSec", 60l,
                    "The amount of time to keep a trip schedule status in cache.");

    private Cache<String, HashSet<IpcSkippedStop>> skippedStopsCache;

    // This is a singleton class
    private static SkippedStopsManager singleton = new SkippedStopsManager();

    /********************** Member Functions **************************/

    /**
     * Constructor made private because this is singleton class where
     * getInstance() should be used to get the VehicleStateManager.
     */
    private SkippedStopsManager() {
        skippedStopsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(skippedStopsCacheExpireSec.getValue(), TimeUnit.SECONDS)
                .build();
    }

    /**
     * Returns the singleton SkippedStopsManager
     * @return
     */
    public static SkippedStopsManager getInstance() {
        if(singleton == null){
            synchronized (SkippedStopsManager.class){
                if(singleton == null){
                    singleton = new SkippedStopsManager();
                }
            }
        }
        return singleton;
    }

    public void putAll(Map<String, HashSet<IpcSkippedStop>> skippedStopByTripMap){
        skippedStopsCache.putAll(skippedStopByTripMap);
    }

    public HashMap<String, HashSet<IpcSkippedStop>> getAll(){
        HashMap<String, HashSet<IpcSkippedStop>> skippedStopMap = new HashMap(skippedStopsCache.asMap());
        return skippedStopMap;
    }
}
