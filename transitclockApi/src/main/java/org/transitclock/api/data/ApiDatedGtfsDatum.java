package org.transitclock.api.data;

import org.transitclock.ipc.data.IpcDatedGtfs;

import javax.xml.bind.annotation.XmlAttribute;

public class ApiDatedGtfsDatum {

    @XmlAttribute
    private String startDate;

    @XmlAttribute
    private String endDate;

    @XmlAttribute
    private String version;

    @XmlAttribute
    private Integer configRev;

    @XmlAttribute
    private String label;


    public ApiDatedGtfsDatum() {
    }

    public ApiDatedGtfsDatum(IpcDatedGtfs datedGtfs){
        this.startDate = datedGtfs.getStartDate();
        this.endDate = datedGtfs.getEndDate();
        this.version = datedGtfs.getVersion();
        this.configRev = datedGtfs.getConfigRev();
        this.label = getLabel(datedGtfs);
    }

    private String getLabel(IpcDatedGtfs datedGtfs){
        return datedGtfs.getVersion() + " (" + datedGtfs.getStartDate() + " - " + datedGtfs.getEndDate() + ")";
    }

}
