package org.transitclock.ipc.interfaces;

import org.transitclock.core.TemporalDifference;

import java.util.Date;

public interface ArrivalDepartureSpeed {

    TemporalDifference getScheduleAdherence();

    int getStopPathIndex();

    String getStopPathId();

    float getStopPathLength();

    Date getDate();

    boolean isArrival();

    boolean isDeparture();

    Date getScheduledDate();

    Long getDwellTime();

}
