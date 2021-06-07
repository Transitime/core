package org.transitclock.core;

import org.transitclock.db.structs.Calendar;
import org.transitclock.db.structs.CalendarDate;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
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

    public static boolean isCalendarValidForServiceType(Calendar calendar, ServiceType serviceType){
        if(calendar != null){
            if(serviceType.equals(ServiceType.WEEKDAY) && calendar.isOnWeekDay()){
                return true;
            } else if(serviceType.equals(ServiceType.SATURDAY) && calendar.getSaturday()){
                return true;
            } else if(serviceType.equals(ServiceType.SATURDAY) && calendar.getSunday()){
                return true;
            }
        }
        return false;
    }

    public static boolean isCalendarDatesForServiceType(List<CalendarDate> calendarDates, ServiceType serviceType){
        if(calendarDates != null){
            for(CalendarDate calendarDate : calendarDates){
                DayOfWeek dayOfWeek = Instant.ofEpochMilli(calendarDate.getDate().getTime())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate().getDayOfWeek();
                ServiceType calDateServiceType = getServiceTypeForDay(dayOfWeek);
                if(serviceType.equals(calDateServiceType)){
                    return true;
                }
            }

        }
        return false;
    }
}
