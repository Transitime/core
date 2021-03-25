package org.transitclock.reporting;

public class RouteStatistics {
    private int early;
    private int onTime;
    private int late;

    public RouteStatistics() { }

    public void addEarly(){
        ++this.early;
    }

    public void addOnTime(){
        ++this.onTime;
    }

    public void addLate(){
        ++this.late;
    }

    public int getEarlyCount() {
        return early;
    }

    public int getOnTimeCount() {
        return onTime;
    }

    public int getLateCount() {
        return late;
    }
}
