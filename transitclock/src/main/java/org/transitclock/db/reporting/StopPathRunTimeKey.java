package org.transitclock.db.reporting;

import org.transitclock.utils.MapKey;

public class StopPathRunTimeKey extends MapKey {
    public StopPathRunTimeKey(String stopId, String stopPatternId, Integer stopPatternIndex){
        super(stopId, stopPatternId, stopPatternIndex);
    }

    @Override
    public String toString() {
        return "StopPathRunTimeKey ["
                + "stopId=" + o1
                + ", stopPatternId=" + o2
                + ", stopPatternIndex=" + o3
                + "]";
    }
}
