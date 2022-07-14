package org.transitclock.core.reporting;

import org.transitclock.core.ServiceType;
import org.transitclock.core.reporting.dao.*;
import org.transitclock.db.structs.*;
import org.transitclock.utils.MapKey;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public abstract class AbstractPrescriptiveRunTimesTests {

    private String servicePeriod = "171 - 2022-06-13 - 2022-09-11";

    public String getServicePeriod() {
        return servicePeriod;
    }

    public void setServicePeriod(String servicePeriod) {
        this.servicePeriod = servicePeriod;
    }

    public String getDataFilePath(ReportingDataFileType fileType, String routeShortName){
        if(fileType.includeRoute()){
            return MessageFormat.format("reporting/prescriptive/{0}/routes/{1}/{2}_{1}.csv",
                    getServicePeriod(), routeShortName, fileType.fileName());
        } else {
            return MessageFormat.format("reporting/prescriptive/{0}/{1}.csv", getServicePeriod(), fileType.fileName());
        }
    }

    protected List<RunTimesForRoutes> getRunTimesForRoutes(String routeShortName) throws IOException, ParseException {
        String runTimesForRoutesFilePath = getDataFilePath(ReportingDataFileType.RUNTIMES_FOR_ROUTES, routeShortName);
        String runTimesForStopsFilePath = getDataFilePath(ReportingDataFileType.RUNTIMES_FOR_STOPS, routeShortName);

        RunTimesForRoutesCsvDao runTimesForRouteDao = new RunTimesForRoutesCsvDao(runTimesForRoutesFilePath);
        List<RunTimesForRoutes> runTimesForRoutes = runTimesForRouteDao.getAll(ServiceType.WEEKDAY);

        RunTimesForStopsCsvDao runTimesForStopsCsvDao = new RunTimesForStopsCsvDao(runTimesForStopsFilePath);
        Map<MapKey, List<RunTimesForStops>> runTimesForStopByRouteKey = runTimesForStopsCsvDao.getAllByRunTimesForRoute();

        for(RunTimesForRoutes runTimesForRoute : runTimesForRoutes){
            List<RunTimesForStops> runTimesForStops = runTimesForStopByRouteKey.get(runTimesForRoute.getKey());
            if(runTimesForStops != null){
                runTimesForRoute.setRunTimesForStops(runTimesForStops);
            }
        }
        return runTimesForRoutes;
    }

    protected List<ArrivalDeparture> getArrivalDepartures(String routeShortName) throws IOException, ParseException {
        String dataFilePath = getDataFilePath(ReportingDataFileType.ARRIVALS_DEPARTURES, routeShortName);
        ArrivalsDeparturesCsvDao adDao = new ArrivalsDeparturesCsvDao(dataFilePath);
        return adDao.getAll();
    }

    protected List<org.transitclock.db.structs.Calendar> getCalendars(String routeShortName) throws IOException, ParseException {
        String dataFilePath = getDataFilePath(ReportingDataFileType.CALENDARS, routeShortName);
        CalendarsCsvDao calendarsDao = new CalendarsCsvDao(dataFilePath);
        return calendarsDao.getAll();
    }

    protected List<CalendarDate> getCalendarDates(String routeShortName) throws IOException, ParseException {
        String dataFilePath = getDataFilePath(ReportingDataFileType.CALENDAR_DATES, routeShortName);
        CalendarDatesCsvDao calendarDatesDao = new CalendarDatesCsvDao(dataFilePath);
        return calendarDatesDao.getAll();
    }


    protected List<Trip> getTripsWithScheduleTimes(String routeShortName) throws IOException, ParseException {
        String tripsFilePath = getDataFilePath(ReportingDataFileType.TRIPS, routeShortName);
        String tripsScheduleFilePath = getDataFilePath(ReportingDataFileType.TRIP_SCHEDULE_TIMES, routeShortName);

        TripsCsvDao tripsDao = new TripsCsvDao(tripsFilePath);
        List<Trip> trips = tripsDao.getAll();

        TripScheduleTimesCsvDao tripSchedulesDao = new TripScheduleTimesCsvDao(tripsScheduleFilePath);
        Map<String, List<ScheduleTime>> tripSchedulesByTripId = tripSchedulesDao.getAllTimesByTrip();

        for(Trip trip : trips){
            List<ScheduleTime> scheduleTimes = tripSchedulesByTripId.get(trip.getId());
            if(scheduleTimes != null){
                trip.addScheduleTimes(scheduleTimes);
            }
        }

        return trips;
    }

    protected List<TripPattern> getTripPatternsWithStopPaths(String routeShortName) throws IOException, ParseException {
        String stopPathsFilePath = getDataFilePath(ReportingDataFileType.STOP_PATHS, routeShortName);
        String tripPatternsFilePath = getDataFilePath(ReportingDataFileType.TRIP_PATTERNS, routeShortName);

        StopPathsCsvDao stopPathsCsvDao = new StopPathsCsvDao(stopPathsFilePath);
        List<StopPath> stopPaths = stopPathsCsvDao.getAll();

        TripPatternsCsvDao tripPatternsDao = new TripPatternsCsvDao(tripPatternsFilePath);
        List<TripPattern> tripPatterns = tripPatternsDao.getAllTripPatterns(stopPaths);

        return tripPatterns;
    }


}
