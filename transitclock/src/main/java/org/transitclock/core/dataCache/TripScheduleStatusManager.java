package org.transitclock.core.dataCache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor.ScheduleRelationship;
import org.transitclock.core.VehicleState;

import java.util.concurrent.TimeUnit;

public class TripScheduleStatusManager {

    private Cache<String,ScheduleRelationship> tripStatusCache;

    // This is a singleton class
    private static TripScheduleStatusManager singleton = new TripScheduleStatusManager();

    /********************** Member Functions **************************/

    /**
     * Constructor made private because this is singleton class where
     * getInstance() should be used to get the VehicleStateManager.
     */
    private TripScheduleStatusManager() {
        tripStatusCache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    /**
     * Returns the singleton TripScheduleStatusManager
     * @return
     */
    public static TripScheduleStatusManager getInstance() {
        if(singleton == null){
            synchronized (TripScheduleStatusManager.class){
                if(singleton == null){
                    singleton = new TripScheduleStatusManager();
                }
            }
        }
        return singleton;
    }

    public void update(String tripId, ScheduleRelationship scheduleRelationship){
        if(tripId != null && scheduleRelationship != null){
            tripStatusCache.put(tripId, scheduleRelationship);
        }
    }

    public VehicleState.ScheduleStatus getScheduleRelationship(String tripId){
        ScheduleRelationship scheduleRelationship = tripStatusCache.getIfPresent(tripId);
        if(scheduleRelationship == ScheduleRelationship.ADDED){
            return VehicleState.ScheduleStatus.ADDED;
        }
        if(scheduleRelationship == ScheduleRelationship.CANCELED){
            return VehicleState.ScheduleStatus.CANCELED;
        }
        if(scheduleRelationship == ScheduleRelationship.UNSCHEDULED){
            return VehicleState.ScheduleStatus.UNSCHEDULED;
        }
        return VehicleState.ScheduleStatus.SCHEDULED;
    }

}
