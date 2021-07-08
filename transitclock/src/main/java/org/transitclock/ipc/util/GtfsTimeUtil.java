package org.transitclock.ipc.util;

import org.transitclock.utils.Time;

import java.time.ZoneId;
import java.util.Date;

public class GtfsTimeUtil {

    public static int dayOfYearForTrip(Date date, ZoneId zone) {
        // Adjust date by three hours so if get a time such as 2:30 am
        // it will be adjusted back to the previous day. This way can handle
        // trips that span midnight. But this doesn't work for trips that
        // span 3am.
        return date.toInstant().minusMillis(3* Time.MS_PER_HOUR).atZone(zone).toLocalDate().getDayOfYear();
    }
}
