package org.transitclock.api.utils;

import org.transitclock.api.data.SpeedFormat;

public class MathUtils {

    public static float convertSpeed(float speedValue, SpeedFormat speedFormat){
        if(!Float.isNaN(speedValue)) {
            if (speedFormat.equals(SpeedFormat.MS)) {
                return speedValue;
            } else if (speedFormat.equals(SpeedFormat.KM)) {
                return convertMetersPerSecondToKm(speedValue);
            } else if (speedFormat.equals(SpeedFormat.MPH)) {
                return convertMetersPerSecondToMph(speedValue);
            }
        }
        return speedValue;
    }

    public static float convertMetersPerSecondToMph(float value){
        return value * 2.2369f;
    }

    public static float convertMetersPerSecondToKm(float value){
        return value * 3.6f;
    }
}
