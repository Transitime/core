package org.transitclock.api.data;

import org.transitclock.ipc.data.IpcDatedGtfs;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.stream.Collectors;

@XmlRootElement(name = "datedGtfsData")
public class ApiDatedGtfsData {

    @XmlElement(name = "datedGtfs")
    private List<ApiDatedGtfsDatum> datedGtfsData;


    public ApiDatedGtfsData() {
    }

    public ApiDatedGtfsData(List<IpcDatedGtfs> datedGtfs){
        datedGtfsData = datedGtfs.stream().map(d -> new ApiDatedGtfsDatum(d)).collect(Collectors.toList());
    }
}
