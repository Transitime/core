package org.transitclock.core.reporting;

import org.hibernate.Session;

public interface RunTimeWriter {
    void writeToDatabase(Session session, RunTimeCache cache);

    int cleanupFromPreviousRun(Session session, String agencyId);
}
