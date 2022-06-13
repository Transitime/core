package org.transitclock.core;

import org.hibernate.Session;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.Calendar;
import org.transitclock.db.structs.CalendarDate;
import org.transitclock.utils.Time;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
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
            } else if(serviceType.equals(ServiceType.SUNDAY) && calendar.getSunday()){
                return true;
            }
        }
        return false;
    }

    public static ServiceType getServiceTypeForCalendar(Calendar calendar){
        if(calendar != null){
            if(calendar.isOnWeekDay()){
                return ServiceType.WEEKDAY;
            } else if(calendar.getSaturday()){
                return ServiceType.SATURDAY;
            } else if(calendar.getSunday()){
                return ServiceType.SUNDAY;
            }
        }
        return null;
    }

    public static ServiceType getServiceTypeForCalendarDate(CalendarDate calendarDate){
        if(calendarDate != null){
            DayOfWeek dayOfWeek = Instant.ofEpochMilli(calendarDate.getDate().getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate().getDayOfWeek();
            return getServiceTypeForDay(dayOfWeek);
        }
        return null;
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

    public static Map<String, Set<ServiceType>> getServiceTypesByIdForCalendars(int configRev,
                                                                                LocalDate beginDate,
                                                                                LocalDate endDate){
        Map<String, Set<ServiceType>> serviceTypesByServiceId = new HashMap<>();
        Session session = HibernateUtils.getSession(true);

        Date calendarStartDate = Time.getLocalDateAsDate(beginDate);
        Date calendarEndDate = Time.getLocalDateAsDate(endDate.plusDays(1));

        // Process Calendars
        List<Calendar> calendars = Calendar.getCalendars(session, configRev);
        for(Calendar calendar : calendars){
            Set<ServiceType> serviceTypes = serviceTypesByServiceId.get(calendar.getServiceId());
            if(serviceTypes == null){
                serviceTypes = new HashSet<>();
                serviceTypesByServiceId.put(calendar.getServiceId(), serviceTypes);
            }
            ServiceType serviceType = ServiceTypeUtil.getServiceTypeForCalendar(calendar);
            serviceTypes.add(serviceType);
        }

        // Process Calendar Dates

        List<CalendarDate> calendarDates = CalendarDate.getCalendarDates(session, configRev);
        for(CalendarDate calendarDate : calendarDates){
            Set<ServiceType> serviceTypes = serviceTypesByServiceId.get(calendarDate.getServiceId());
            // Only add if service id doesn't already exist
            // We want to add unique service ids from Calendar Dates, not exceptions
            if(serviceTypes == null
                    && !calendarStartDate.after(calendarDate.getDate())
                    && calendarDate.getDate().before(calendarEndDate)){
                serviceTypes = new HashSet<>();
                serviceTypesByServiceId.put(calendarDate.getServiceId(), serviceTypes);
                ServiceType serviceType = ServiceTypeUtil.getServiceTypeForCalendarDate(calendarDate);
                serviceTypes.add(serviceType);
            }
        }

        return serviceTypesByServiceId;
    }
}
