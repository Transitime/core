package org.transitclock.api.data;

import org.transitclock.ipc.data.IpcStop;
import org.transitclock.ipc.data.IpcStopWithDwellTime;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@XmlRootElement
public class ApiStopsWithDwellTime {

    @XmlElement
    private List<ApiStopWithDwellTime> stops;

    public ApiStopsWithDwellTime(){}

    public ApiStopsWithDwellTime(List<IpcStopWithDwellTime> stopsWithDwellTime){
        Map<String, IpcStopWithDwellTime> stopsMap = new LinkedHashMap<>();
        Map<String, LongSummaryStatistics> stopIdToDwellTimesMap = new HashMap<>();

        for(IpcStopWithDwellTime stop : stopsWithDwellTime) {

            IpcStopWithDwellTime ipcStopFromMap = stopsMap.get(stop.getId());

            if(ipcStopFromMap == null){
                stopsMap.put(stop.getId(), stop);
                stopIdToDwellTimesMap.put(stop.getId(), new LongSummaryStatistics());
            }
            else if(isStopNewer(ipcStopFromMap, stop)) {
                stopsMap.put(stop.getId(), stop);
            }

            if (stop.getDwellTime() != null) {
                stopIdToDwellTimesMap.get(stop.getId()).accept(stop.getDwellTime());
            }
        }

        stops = new ArrayList<>(stopsMap.entrySet().size());

        for (Map.Entry<String,IpcStopWithDwellTime> entry : stopsMap.entrySet()){
            IpcStopWithDwellTime ipcStopWithDwellTime = entry.getValue();
            LongSummaryStatistics dwellTimeStats = stopIdToDwellTimesMap.get(ipcStopWithDwellTime.getId());
            Double avgDwellTime = dwellTimeStats != null && dwellTimeStats.getCount() > 0 ? dwellTimeStats.getAverage() : null;
            stops.add(new ApiStopWithDwellTime(ipcStopWithDwellTime, avgDwellTime));
        }
    }

    private boolean isStopNewer(IpcStopWithDwellTime currentStop, IpcStopWithDwellTime newStop){
        return currentStop.getConfigRev() < newStop.getConfigRev();
    }

    public List<ApiStopWithDwellTime> getStops() {
        return stops;
    }

}
