package org.transitclock.core;

import org.hibernate.Session;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.Calendar;
import org.transitclock.db.structs.CalendarDate;
import org.transitclock.db.structs.Trip;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.transitclock.core.ServiceTypeUtil.*;

public class RunTimesServiceUtilsImpl implements RunTimeServiceUtils {

    private Session session;

    public RunTimesServiceUtilsImpl(Session session) {
        this.session = session;
    }

    @Override
    public ServiceType getServiceTypeForTrip(Date avlTime, Trip trip){
        if(avlTime != null){
            long updatedTime = getTimeOfDayForServiceType(avlTime, trip.getStartTime());
            DayOfWeek dayOfWeek = Instant.ofEpochMilli(updatedTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate().getDayOfWeek();

            ServiceType serviceType = getServiceTypeForDay(dayOfWeek);

            if(isServiceIdValidForDate(serviceType, trip, new Date(updatedTime))){
                return serviceType;
            }
        }

        return null;
    }

    private boolean isServiceIdValidForDate(ServiceType serviceType, Trip trip, Date date){
        int configRev = trip.getConfigRev();
        String serviceId = trip.getServiceId();
        Calendar calendar = getCalendarForServiceId(session, configRev, serviceId);
        if(isServiceTypeActiveForServiceCal(serviceType, calendar)){
            return true;
        } else{
            List<CalendarDate> calendarDatesForNow = getCalendarDatesForDate(session, configRev, date);
            if (calendarDatesForNow != null) {
                for (CalendarDate calendarDate : calendarDatesForNow) {
                    // Handle special service for this date
                    if (calendarDate.addService() && calendarDate.getServiceId().equals(serviceId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Calendar getCalendarForServiceId(Session session, int configRev, String serviceId){
        try {
            List<Calendar> calendars = Calendar.getCalendar(session, configRev, serviceId);
            return calendars.stream().findAny().orElse(null);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;

    }

    public List<CalendarDate> getCalendarDatesForDate(Session session, int configRev, Date date){
        return CalendarDate.getCalendarDates(session, configRev, date);
    }
}
