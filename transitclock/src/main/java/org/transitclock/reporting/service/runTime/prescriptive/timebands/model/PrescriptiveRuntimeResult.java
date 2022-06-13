package org.transitclock.reporting.service.runTime.prescriptive.timebands.model;

import com.google.common.base.Objects;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.kmeans.Centroid;

import java.util.List;
import java.util.Map;

public class PrescriptiveRuntimeResult {
    private RunTimeData firstRunTime = null;
    private Map<Centroid, List<RunTimeData>> runTimeDataPerCentroid;

    public PrescriptiveRuntimeResult(RunTimeData firstRunTime,
                                     Map<Centroid, List<RunTimeData>> runTimeDataPerCentroid) {
        this.firstRunTime = firstRunTime;
        this.runTimeDataPerCentroid = runTimeDataPerCentroid;
    }

    public RunTimeData getFirstRunTime() {
        return firstRunTime;
    }


    public Map<Centroid, List<RunTimeData>> getRunTimeDataPerCentroid() {
        return runTimeDataPerCentroid;
    }

    public String getSortHash(){
        String sortHash = "";
        if(firstRunTime != null && firstRunTime.getRouteShortName()!= null){
            sortHash += firstRunTime.getRouteShortName() + "_";
        }
        if(firstRunTime != null && firstRunTime.getHeadsign()!= null){
            sortHash +=  firstRunTime.getHeadsign();
        }
        return sortHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrescriptiveRuntimeResult that = (PrescriptiveRuntimeResult) o;
        return Objects.equal(firstRunTime, that.firstRunTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(firstRunTime);
    }

    @Override
    public String toString() {
        return "PrescriptiveRuntimeResult{" +
                "firstRunTime=" + firstRunTime +
                ", runTimeDataPerCentroid=" + runTimeDataPerCentroid +
                '}';
    }
}
