package org.transitclock.api.utils;

import org.transitclock.applications.Core;

public class TripFormatter {

    public static String getFormattedTripId(String tripId){
        if(tripId != null && Core.getInstance().getDbConfig().getServiceIdSuffix()){
            return tripId.split("-")[0];
        }
        return tripId;
    }
}
