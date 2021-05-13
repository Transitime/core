package org.transitclock.core.reporting;

import org.hibernate.Session;

public class TestRunTimeWriterImpl implements RunTimeWriter{
    @Override
    public void writeToDatabase(Session session, RunTimeCache cache) {

    }

    @Override
    public int cleanupFromPreviousRun(Session session, String agencyId) {
        return 0;
    }
}
