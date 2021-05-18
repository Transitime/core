package org.transitclock.core;

import org.transitclock.db.structs.Trip;

import java.util.Date;

public interface RunTimeServiceUtils {
    ServiceType getServiceTypeForTrip(Date avlTime, Trip trip);
}
