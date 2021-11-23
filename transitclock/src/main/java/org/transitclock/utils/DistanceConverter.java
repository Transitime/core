package org.transitclock.utils;

public class DistanceConverter {
    public static final double KM_TO_METERS = 1000;

    public static Double kmToMeters(Double km){
        if(km != null){
            return km * KM_TO_METERS;
        }
        return km;
    }
}
