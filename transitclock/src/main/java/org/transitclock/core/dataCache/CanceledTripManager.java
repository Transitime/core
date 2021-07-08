package org.transitclock.core.dataCache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.transitclock.config.LongConfigValue;
import org.transitclock.ipc.data.IpcCanceledTrip;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CanceledTripManager {

    private static LongConfigValue canceledTripCacheExpireSec =
            new LongConfigValue("transitclock.avl.canceledTripCacheExpireSec", 60l,
                    "The amount of time to keep a trip schedule status in cache.");

    private Cache<CanceledTripKey, IpcCanceledTrip> canceledTripCache;

    // This is a singleton class
    private static CanceledTripManager singleton = new CanceledTripManager();

    /********************** Member Functions **************************/

    /**
     * Constructor made private because this is singleton class where
     * getInstance() should be used to get the VehicleStateManager.
     */
    private CanceledTripManager() {
        canceledTripCache = CacheBuilder.newBuilder()
                .expireAfterWrite(canceledTripCacheExpireSec.getValue(), TimeUnit.SECONDS)
                .build();
    }

    /**
     * Returns the singleton CanceledTripManager
     * @return
     */
    public static CanceledTripManager getInstance() {
        if(singleton == null){
            synchronized (CanceledTripManager.class){
                if(singleton == null){
                    singleton = new CanceledTripManager();
                }
            }
        }
        return singleton;
    }

    public boolean isCanceled(String vehicleId, String tripId){
        CanceledTripKey key = new CanceledTripKey(vehicleId, tripId);
        IpcCanceledTrip cachedTrip = canceledTripCache.getIfPresent(key);
        if(cachedTrip != null &&  cachedTrip.getTripId() != null &&
                tripId != null && cachedTrip.getTripId().equalsIgnoreCase(tripId)){
            return true;
        }
        return false;
    }

    public synchronized void putAll(Map<CanceledTripKey, IpcCanceledTrip> tripStatusMap){
        canceledTripCache.putAll(tripStatusMap);
    }

    public synchronized HashMap<CanceledTripKey, IpcCanceledTrip> getAll(){
        HashMap<CanceledTripKey, IpcCanceledTrip> canceledTripMap = new HashMap(canceledTripCache.asMap());
        return canceledTripMap;
    }

}
