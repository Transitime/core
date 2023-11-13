package org.transitclock.core.barefoot;

import java.util.Objects;

public class ReferenceId {
    int stopPathIndex;
    int segmentIndex;
    static long mutiplier=1000;
    public ReferenceId(int stopPathIndex, int segmentIndex) {
        super();
        this.stopPathIndex = stopPathIndex;
        this.segmentIndex = segmentIndex;
    }

    public int getStopPathIndex() {
        return stopPathIndex;
    }
    public void setStopPathIndex(int stopPathIndex) {
        this.stopPathIndex = stopPathIndex;
    }
    public int getSegmentIndex() {
        return segmentIndex;
    }
    public void setSegmentIndex(int segmentIndex) {
        this.segmentIndex = segmentIndex;
    }
    static ReferenceId deconstructRefId(long refId)
    {
        int segmentIndex=(int) Math.floorDiv(refId,mutiplier);
        int stopPathIndex=(int) Math.floorMod(refId,mutiplier);
        return new ReferenceId(stopPathIndex, segmentIndex);
    }
    long getRefId()
    {
        return  stopPathIndex+segmentIndex*mutiplier;
    }

    @Override
    public String toString() {
        return "ReferenceId [stopPathIndex=" + stopPathIndex + ", segmentIndex=" + segmentIndex + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(segmentIndex, stopPathIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReferenceId other = (ReferenceId) obj;
        return segmentIndex == other.segmentIndex && stopPathIndex == other.stopPathIndex;
    }

}