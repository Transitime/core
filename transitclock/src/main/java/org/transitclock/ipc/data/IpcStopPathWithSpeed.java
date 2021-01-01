package org.transitclock.ipc.data;

import org.transitclock.db.structs.StopPath;

public class IpcStopPathWithSpeed extends  IpcStopPath{

    private final Double speed;

    public IpcStopPathWithSpeed(StopPath stopPath, Double speed) {
        super(stopPath);
        this.speed = speed;
    }

    public Double getSpeed() {
        return speed;
    }
}
