package org.transitclock.reporting.service.runTime.prescriptive.model;

import java.time.LocalDate;

public class DatedGtfs {

    private String version;
    private LocalDate startDate;
    private LocalDate endDate;
    private int configRev;

    public DatedGtfs() { }

    public DatedGtfs(LocalDate startDate, LocalDate endDate, String version, int configRev) {
        this.version = version;
        this.startDate = startDate;
        this.endDate = endDate;
        this.configRev = configRev;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getConfigRev() {
        return configRev;
    }

    public void setConfigRev(int configRev) {
        this.configRev = configRev;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "DatedGtfs{" +
                "version='" + version + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", configRev=" + configRev +
                '}';
    }
}
