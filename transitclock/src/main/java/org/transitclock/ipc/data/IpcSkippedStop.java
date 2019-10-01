package org.transitclock.ipc.data;

import java.io.Serializable;
import java.util.Objects;

public class IpcSkippedStop implements Serializable {

    private String stopId;
    private int stopSequence;

    public IpcSkippedStop(String stopId, int stopSequence){
        this.stopId = stopId;
        this.stopSequence = stopSequence;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public int getStopSequence() {
        return stopSequence;
    }

    public void setStopSequence(int stopSequence) {
        this.stopSequence = stopSequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IpcSkippedStop that = (IpcSkippedStop) o;
        return stopSequence == that.stopSequence &&
                Objects.equals(stopId, that.stopId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stopId, stopSequence);
    }

    @Override
    public String toString() {
        return "IpcSkippedStop{" +
                "stopId='" + stopId + '\'' +
                ", stopSequence=" + stopSequence +
                '}';
    }
}
