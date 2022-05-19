package org.transitclock.reporting.service.runTime.prescriptive.timebands.kmeans;

import org.transitclock.reporting.service.runTime.prescriptive.timebands.model.RunTimeData;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

//compares two map entries based on the first RunTimeData element's sequence number
public class RunTimeDataListSequenceComparator implements Comparator<Map.Entry<Centroid, List<RunTimeData>>> {

    @Override
    public int compare(Map.Entry<Centroid, List<RunTimeData>> o1, Map.Entry<Centroid, List<RunTimeData>> o2) {
        if (o1 == null || o2 == null) throw new IllegalArgumentException("arguments cannot be null");
        if (o1.getValue() == null || o2.getValue() == null) throw new IllegalArgumentException("values in map cannot be null");

        if (o1.getValue().size() == 0 && o2.getValue().size() == 0)
            return 0;
        if (o1.getValue().size() == 0 && o2.getValue().get(0).getSequence() != null)
            return Integer.valueOf(-1).compareTo(o2.getValue().get(0).getSequence());
        if (o2.getValue().size() == 0 && o1.getValue().get(0).getSequence() != null)
            return o1.getValue().get(0).getSequence().compareTo(Integer.valueOf(-1));
        if (o1.getValue().get(0).getSequence() == null && o2.getValue().get(0).getSequence() == null)
            return 0;
        if (o1.getValue().get(0).getSequence() == null)
            return Integer.valueOf(-1).compareTo(o2.getValue().get(0).getSequence());
        if (o2.getValue().get(0).getSequence() == null)
            return o1.getValue().get(0).getSequence().compareTo(Integer.valueOf(-1));

        return o1.getValue().get(0).getSequence().compareTo(o2.getValue().get(0).getSequence());
    }
}
