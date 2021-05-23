package org.transitclock.core.reporting;

import java.util.Date;

public interface RunTimeWriter {
    void writeToDatabase(String agencyId, RunTimeCache cache);
    int cleanupFromPreviousRun(String agencyId, Date beginDate, Date endDate);
}
