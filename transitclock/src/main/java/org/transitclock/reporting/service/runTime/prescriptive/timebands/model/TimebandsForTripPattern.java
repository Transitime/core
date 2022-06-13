package org.transitclock.reporting.service.runTime.prescriptive.timebands.model;

import java.util.List;

public class TimebandsForTripPattern {
    String routeShortName;
    String tripPatternId;
    List<TimebandTime> timebandTimes;
    List<TimebandTime> adjustedTimebandTimes;

    public TimebandsForTripPattern(String routeShortName, String tripPatternId, List<TimebandTime> timebandTimes) {
        this.routeShortName = routeShortName;
        this.tripPatternId = tripPatternId;
        this.timebandTimes = timebandTimes;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }

    public String getTripPatternId() {
        return tripPatternId;
    }

    public void setTripPatternId(String tripPatternId) {
        this.tripPatternId = tripPatternId;
    }

    public List<TimebandTime> getTimebandTimes() {
        return timebandTimes;
    }

    public void setTimebandTimes(List<TimebandTime> timebandTimes) {
        this.timebandTimes = timebandTimes;
    }

    public List<TimebandTime> getAdjustedTimebandTimes() {
        return adjustedTimebandTimes;
    }

    public void setAdjustedTimebandTimes(List<TimebandTime> adjustedTimebandTimes) {
        this.adjustedTimebandTimes = adjustedTimebandTimes;
    }
}
