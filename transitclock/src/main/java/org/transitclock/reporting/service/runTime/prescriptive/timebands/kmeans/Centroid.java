package org.transitclock.reporting.service.runTime.prescriptive.timebands.kmeans;

public class Centroid {
    private Double centroidValue;
    private Double actualMeanValue;
    private Double expectedMeanValue;
    private Double length;

    public Double getCentroidValue() { return this.centroidValue; }

    public Double getActualMeanValue() { return this.actualMeanValue; }
    void setActualMeanValue(Double actualMeanValue) { this.actualMeanValue = actualMeanValue; }

    public Double getExpectedMeanValue() {
        return expectedMeanValue;
    }

    public void setExpectedMeanValue(Double expectedMeanValue) {
        this.expectedMeanValue = expectedMeanValue;
    }

    public Double getLength() { return this.length; }
    void setLength(Double length) { this.length = length; }

    public Centroid(Double centroidValue){
        this.centroidValue = centroidValue;
    }

    @Override
    public String toString() {
        return "Centroid{" +
                "centroidValue=" + centroidValue +
                ", actualMeanValue=" + actualMeanValue +
                ", expectedMeanValue=" + expectedMeanValue +
                ", length=" + length +
                '}';
    }
}
