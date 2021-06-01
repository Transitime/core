package org.transitclock.reporting.keys;

import org.transitclock.utils.MapKey;

public class StopPathRunTimeKey extends MapKey implements Comparable<StopPathRunTimeKey> {
    public StopPathRunTimeKey(String stopPathId, Integer stopPathIndex){
        super(stopPathId, stopPathIndex);
    }

    public String getStopPathId(){
        return (String) o1;
    }

    public Integer getStopPathIndex(){
        if(o2 != null) {
            return (Integer) o2;
        }
        return null;
    }

    @Override
    public String toString() {
        return "StopPathRunTimeKey ["
                + "stopPathId=" + o1
                + ", stopPathIndex=" + o2
                + "]";
    }

    @Override
    public int compareTo(StopPathRunTimeKey sptk2) {
        return this.getStopPathIndex() - sptk2.getStopPathIndex();
    }
}
