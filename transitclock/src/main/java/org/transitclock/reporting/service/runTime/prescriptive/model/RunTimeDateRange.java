package org.transitclock.reporting.service.runTime.prescriptive.model;

import java.time.LocalDate;

public class RunTimeDateRange {
    private LocalDate startDate;
    private LocalDate endDate;
    private int configRev;

    public RunTimeDateRange() { }

    public RunTimeDateRange(LocalDate startDate, LocalDate endDate, int configRev) {

        this.startDate = startDate;
        this.endDate = endDate;
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
}
