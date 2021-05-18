package org.transitclock.core;

import org.transitclock.db.structs.Calendar;

import java.time.DayOfWeek;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ServiceTypeUtil {
    public static ServiceType getServiceTypeForDay(DayOfWeek dayOfWeek){
        switch (dayOfWeek) {
            case MONDAY:
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
            case FRIDAY:
                return ServiceType.WEEKDAY;
            case SATURDAY:
                return ServiceType.SATURDAY;
            case SUNDAY:
                return ServiceType.SUNDAY;
            default:
                return null;
        }
    }

    public static boolean isServiceTypeActiveForServiceCal(ServiceType serviceType, Calendar calendar){
        if(serviceType != null && calendar != null) {
            if (serviceType.equals(ServiceType.SUNDAY) && calendar.getSunday()) {
                return Boolean.TRUE;
            } else if (serviceType.equals(ServiceType.SATURDAY) && calendar.getSaturday()) {
                return Boolean.TRUE;
            } else if (serviceType.equals(ServiceType.WEEKDAY) && (
                    calendar.getMonday() ||
                            calendar.getTuesday() ||
                            calendar.getWednesday() ||
                            calendar.getThursday() ||
                            calendar.getFriday()
            )) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public static long getTimeOfDayForServiceType(Date avlTime, Integer tripStartTime){
        long updatedTime = avlTime.getTime();
        if(tripStartTime != null){
            if(tripStartTime < (TimeUnit.HOURS.toSeconds(6))){
                updatedTime += TimeUnit.HOURS.toMillis(6);
            } else if(tripStartTime > (TimeUnit.HOURS.toSeconds(18))){
                updatedTime -= TimeUnit.HOURS.toMillis(6);
            }
        }
        return updatedTime;
    }
}
