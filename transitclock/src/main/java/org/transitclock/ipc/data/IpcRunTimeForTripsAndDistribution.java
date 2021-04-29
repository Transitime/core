package org.transitclock.ipc.data;

import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class IpcRunTimeForTripsAndDistribution implements Serializable {
    List<IpcRunTimeForTrip> aggregatedRunTimesForTrips;
    Map<String, List<Long>> runTimesForAllTrips;

    public IpcRunTimeForTripsAndDistribution(List<IpcRunTimeForTrip> aggregatedRunTimesForTrips,
                                             Map<String, List<Long>> runTimesForAllTrips) {
        this.aggregatedRunTimesForTrips = aggregatedRunTimesForTrips;
        this.runTimesForAllTrips = runTimesForAllTrips;
    }

    public List<IpcRunTimeForTrip> getAggregatedRunTimesForTrips() {
        return aggregatedRunTimesForTrips;
    }

    public Map<String, List<Long>> getRunTimesForAllTrips() {
        return runTimesForAllTrips;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IpcRunTimeForTripsAndDistribution that = (IpcRunTimeForTripsAndDistribution) o;
        return Objects.equal(aggregatedRunTimesForTrips, that.aggregatedRunTimesForTrips) &&
                Objects.equal(runTimesForAllTrips, that.runTimesForAllTrips);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(aggregatedRunTimesForTrips, runTimesForAllTrips);
    }

    @Override
    public String toString() {
        return "IpcRunTimeForTrips{" +
                "aggregatedRunTimesForTrips=" + aggregatedRunTimesForTrips +
                ", runTimesForAllTrips=" + runTimesForAllTrips +
                '}';
    }
}
