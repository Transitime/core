package org.transitclock.reporting.service.runTime.prescriptive.timebands.helper;

import org.transitclock.reporting.service.runTime.prescriptive.timebands.model.RunTimeData;

import java.util.Comparator;

public class RunTimeDataScheduledStartTimeComparator implements Comparator<RunTimeData> {

    @Override
    public int compare(RunTimeData o1, RunTimeData o2) {
        if (o1 == null || o2 == null) throw new IllegalArgumentException("arguments cannot be null");
        if (o1.getScheduledStartTime() == null) {
            if (o2.getScheduledStartTime() == null) return 0;//both null so just return 0;
            //make o1 scheduled start time zero for purposes of comparison
            else return Integer.valueOf(0).compareTo(o2.getScheduledStartTime());
        }

        return o1.getScheduledStartTime().compareTo(o2.getScheduledStartTime());
    }
}
