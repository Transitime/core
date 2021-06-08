package org.transitclock.api.data;

import org.transitclock.api.data.ApiStopTime;
import org.transitclock.ipc.data.IpcStopTime;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ApiStopTimes {
    @XmlElement
    private List<ApiStopTime> apiStopTimes = new ArrayList<>();

    public ApiStopTimes() { }

    public ApiStopTimes(List<IpcStopTime> ipcStopTimes){
        for(IpcStopTime ipcStopTime : ipcStopTimes){
            apiStopTimes.add(new ApiStopTime(ipcStopTime));
        }
    }

    public List<ApiStopTime> getApiStopTimes() {
        return apiStopTimes;
    }
}
