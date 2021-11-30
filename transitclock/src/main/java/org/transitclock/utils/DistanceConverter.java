package org.transitclock.utils;

public class DistanceConverter {

    public static final double METERS_IN_KM = 1000;
    public static final double METERS_IN_MILE = 1609.344;
    public static final double METERS_IN_FT = 0.3048;
    public static final double METERS_IN_YARD = 0.9144;
    public static final double METERS_IN_FURLONG = 201.168;

    private static Double convertDistance(Double value, double unitConversion){
        if(value != null){
            return value * unitConversion;
        }
        return null;
    }

    public static Double kmToMeters(Double km){
        return convertDistance(km, METERS_IN_KM);
    }

    public static Double milesToMeters(Double miles) {
        return convertDistance(miles, METERS_IN_MILE);

    }

    public static Double feetToMeters(Double feet) {
        return convertDistance(feet, METERS_IN_FT);
    }

    public static Double yardsToMeters(Double yards) {
        return convertDistance(yards, METERS_IN_YARD);
    }

    public static Double furlongToMeters(Double furlongs) {
        return convertDistance(furlongs, METERS_IN_FURLONG);
    }
}
