package org.transitclock.utils;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum DistanceType {
    METER("METER","METERS") {
        @Override
        public Double convertDistanceToMeters(Double value) {
            return value;
        }
    },
    KM("KM","KILOMETER","KILOMETERS") {
        @Override
        public Double convertDistanceToMeters(Double value) {
            return DistanceConverter.kmToMeters(value);
        }
    },
    MILE("MILE","MILES","MI"){
        @Override
        public Double convertDistanceToMeters(Double value) {
            return DistanceConverter.milesToMeters(value);
        }
    },
    FT("FOOT","FEET","FT"){
        @Override
        public Double convertDistanceToMeters(Double value) {
            return DistanceConverter.feetToMeters(value);
        }
    },
    YARD("YARD","YARDS","YD"){
        @Override
        public Double convertDistanceToMeters(Double value) {
            return DistanceConverter.yardsToMeters(value);
        }
    },
    FURLONG("FURLONG"){
        @Override
        public Double convertDistanceToMeters(Double value) {
            return DistanceConverter.furlongToMeters(value);
        }
    };

    private static final Map<String, DistanceType> BY_LABEL = new HashMap<>();

    private String[] labels;

    static {
        for (DistanceType dt: values()) {
            for(String label : dt.labels){
                BY_LABEL.put(label, dt);
            }
        }
    }

    private DistanceType(String... labels) {
        this.labels = labels;
    }

    public static DistanceType valueOfLabel(String label) {
        DistanceType distanceType = BY_LABEL.get(label.toUpperCase());
        if(distanceType == null){
            throw new IllegalArgumentException("No enum constant with label " + label);
        }
        return distanceType;
    }

    public abstract Double convertDistanceToMeters(Double value);

    @Override
    public String toString() {
        return Arrays.toString(this.labels);
    }


}
