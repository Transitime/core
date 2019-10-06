package org.transitclock.api.utils;

public class TripFormatter {

    public static String getFormattedTripId(boolean serviceIdSuffix, String tripId){
        if(tripId != null && serviceIdSuffix){
            return tripId.split("-")[0];
        }
        return tripId;
    }
}
