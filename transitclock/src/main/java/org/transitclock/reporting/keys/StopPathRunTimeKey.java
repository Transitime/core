package org.transitclock.reporting.keys;

import org.transitclock.utils.MapKey;

public class StopPathRunTimeKey extends MapKey {
    public StopPathRunTimeKey(String stopId, String stopPathId, Integer stopPathIndex){
        super(stopId, stopPathId, stopPathIndex);
    }

    public String getStopId(){
        return (String) o1;
    }

    public String getStopPathId(){
        return (String) o2;
    }

    public Integer getStopPathIndex(){
        if(o3 != null) {
            return (Integer) o3;
        }
        return null;
    }

    @Override
    public String toString() {
        return "StopPathRunTimeKey ["
                + "stopId=" + o1
                + ", stopPathId=" + o2
                + ", stopPathIndex=" + o3
                + "]";
    }
}
