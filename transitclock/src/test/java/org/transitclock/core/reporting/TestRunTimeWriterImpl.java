package org.transitclock.core.reporting;

import java.util.Date;

public class TestRunTimeWriterImpl implements RunTimeWriter{
    @Override
    public void writeToDatabase(String agencyId, RunTimeCache cache) {

    }

    @Override
    public int cleanupFromPreviousRun(String agencyId, Date beginDate, Date endDate) {
        return 0;
    }
}
