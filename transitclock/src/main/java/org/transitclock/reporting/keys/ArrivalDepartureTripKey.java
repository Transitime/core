package org.transitclock.reporting.keys;

import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.utils.MapKey;

import java.time.ZoneId;
import java.util.Date;

import static org.transitclock.ipc.util.GtfsTimeUtil.dayOfYearForTrip;

public class ArrivalDepartureTripKey extends MapKey {
    public ArrivalDepartureTripKey(String serviceId,
                                   Integer dayOfYear,
                                   String tripId,
                                    String vehicleId) {
        super(serviceId, dayOfYear, tripId, vehicleId);
    }

    public static ArrivalDepartureTripKey getKey(final String serviceId, final Date date,
                                                 final String tripId, final String vehicleId, ZoneId zone) {
        return new ArrivalDepartureTripKey(serviceId, dayOfYearForTrip(date, zone), tripId, vehicleId);
    }

    public static ArrivalDepartureTripKey getKey(final ArrivalDeparture ad, final ZoneId zoneId) {
        return new ArrivalDepartureTripKey(ad.getServiceId(), dayOfYearForTrip(ad.getDate(), zoneId),
                ad.getTripId(), ad.getVehicleId());
    }

    public String getTripId(){
        return o3.toString();
    }

    @Override
    public String toString() {
        return "DbDataMapKey ["
                + "serviceId=" + o1
                + ", dayOfYear=" + o2
                + ", tripId=" + o3
                + ", vehicleId=" + o4
                + "]";
    }

}
