package org.transitclock.ipc.data;

import com.google.common.base.Objects;

import java.io.Serializable;

public class IpcRunTime implements Serializable {
    private final Double avgRunTime;
    private final Double fixed;
    private final Double variable;
    private final Double dwell;



    public IpcRunTime(Double avgRunTime, Double fixed, Double variable, Double dwell){
        this.avgRunTime = avgRunTime;
        this.fixed = fixed;
        this.variable = variable;
        this.dwell = dwell;
    }

    public IpcRunTime(Double fixed, Double variable, Double dwell){
        this.fixed = fixed;
        this.variable = variable;
        this.dwell = dwell;
        this.avgRunTime = fixed + variable + dwell;

    }

    public Double getAvgRunTime() {
        return avgRunTime;
    }

    public Double getFixed() {
        return fixed;
    }

    public Double getVariable() {
        return variable;
    }

    public Double getDwell() {
        return dwell;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IpcRunTime that = (IpcRunTime) o;
        return Objects.equal(avgRunTime, that.avgRunTime) &&
                Objects.equal(fixed, that.fixed) &&
                Objects.equal(variable, that.variable) &&
                Objects.equal(dwell, that.dwell);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(avgRunTime, fixed, variable, dwell);
    }

    @Override
    public String toString() {
        return "IpcRunTime{" +
                "avgRunTime=" + avgRunTime +
                ", fixed=" + fixed +
                ", variable=" + variable +
                ", dwell=" + dwell +
                '}';
    }
}
