package org.transitclock.ipc.data;

import com.google.common.base.Objects;
import java.io.Serializable;

public class IpcRunTimeForRoute implements Serializable {

    private String routeShortName;
    private Integer earlyCount;
    private Integer onTimeCount;
    private Integer lateCount;

    public IpcRunTimeForRoute(String routeShortName, Integer earlyCount, Integer onTimeCount, Integer lateCount) {
        this.routeShortName = routeShortName;
        this.earlyCount = earlyCount;
        this.onTimeCount = onTimeCount;
        this.lateCount = lateCount;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }

    public Integer getEarlyCount() {
        return earlyCount;
    }

    public void setEarlyCount(Integer earlyCount) {
        this.earlyCount = earlyCount;
    }

    public Integer getOnTimeCount() {
        return onTimeCount;
    }

    public void setOnTimeCount(Integer onTimeCount) {
        this.onTimeCount = onTimeCount;
    }

    public Integer getLateCount() {
        return lateCount;
    }

    public void setLateCount(Integer lateCount) {
        this.lateCount = lateCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IpcRunTimeForRoute that = (IpcRunTimeForRoute) o;
        return Objects.equal(routeShortName, that.routeShortName) &&
                Objects.equal(earlyCount, that.earlyCount) &&
                Objects.equal(onTimeCount, that.onTimeCount) &&
                Objects.equal(lateCount, that.lateCount);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(routeShortName, earlyCount, onTimeCount, lateCount);
    }

    @Override
    public String toString() {
        return "IpcRunTimeForRoute{" +
                "routeShortName='" + routeShortName + '\'' +
                ", earlyCount=" + earlyCount +
                ", onTimeCount=" + onTimeCount +
                ", lateCount=" + lateCount +
                '}';
    }
}
