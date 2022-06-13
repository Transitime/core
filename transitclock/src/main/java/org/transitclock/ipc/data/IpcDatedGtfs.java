package org.transitclock.ipc.data;

import org.transitclock.reporting.service.runTime.prescriptive.model.DatedGtfs;

import java.io.Serializable;
import java.time.LocalDate;

public class IpcDatedGtfs implements Serializable {

    private static final long serialVersionUID = 1L;

    private String startDate;
    private String endDate;
    private int configRev;

    private String version;


    public IpcDatedGtfs(){}

    public IpcDatedGtfs(DatedGtfs datedGtfs){
        this.startDate = datedGtfs.getStartDate().toString();
        this.endDate = datedGtfs.getEndDate().toString();
        this.configRev = datedGtfs.getConfigRev();
        this.version = datedGtfs.getVersion();
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public int getConfigRev() {
        return configRev;
    }

    public String getVersion() {
        return version;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setConfigRev(int configRev) {
        this.configRev = configRev;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
