package org.transitclock.core.dataCache.canceledTrip;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.transitclock.config.LongConfigValue;
import org.transitclock.ipc.data.IpcCanceledTrip;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CanceledTripCache {

    private static LongConfigValue canceledTripCacheExpireSec =
            new LongConfigValue("transitclock.avl.canceledTripCacheExpireSec", 60l,
                    "The amount of time to keep a trip schedule status in cache.");

    private Cache<String, IpcCanceledTrip> canceledTripCache;


    // This is a singleton class
    private static CanceledTripCache singleton = new CanceledTripCache();

    /********************** Member Functions **************************/

    /**
     * Constructor made private because this is singleton class where
     * getInstance() should be used to get the VehicleStateManager.
     */
    private CanceledTripCache() {
        canceledTripCache = CacheBuilder.newBuilder()
                .expireAfterWrite(canceledTripCacheExpireSec.getValue(), TimeUnit.SECONDS)
                .build();
    }

    /**
     * Returns the singleton CanceledTripManager
     * @return
     */
    public static CanceledTripCache getInstance() {
        if(singleton == null){
            synchronized (CanceledTripCache.class){
                if(singleton == null){
                    singleton = new CanceledTripCache();
                }
            }
        }
        return singleton;
    }

    public boolean isCanceled(String tripId){
        IpcCanceledTrip cachedTrip = canceledTripCache.getIfPresent(tripId);
        if(cachedTrip != null &&  cachedTrip.getTripId() != null &&
                tripId != null && cachedTrip.getTripId().equalsIgnoreCase(tripId)){
            return true;
        }
        return false;
    }

    public synchronized void putAll(Map<String, IpcCanceledTrip> tripStatusMap){
        canceledTripCache.putAll(tripStatusMap);
    }

    public synchronized HashMap<String, IpcCanceledTrip> getAll(){
        HashMap<String, IpcCanceledTrip> canceledTripMap = new HashMap(canceledTripCache.asMap());
        return canceledTripMap;
    }

}
