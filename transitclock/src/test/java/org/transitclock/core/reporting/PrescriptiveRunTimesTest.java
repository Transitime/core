package org.transitclock.core.reporting;

import org.hibernate.Session;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.transitclock.applications.Core;
import org.transitclock.core.ServiceType;
import org.transitclock.core.reporting.dao.*;
import org.transitclock.db.query.ArrivalDepartureQuery;
import org.transitclock.db.query.RunTimeForRouteQuery;
import org.transitclock.db.query.TripQuery;
import org.transitclock.db.structs.*;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimesForPattern;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimesForPatterns;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimesForTimeBand;
import org.transitclock.reporting.dao.RunTimeRoutesDao;
import org.transitclock.reporting.service.RunTimeService;
import org.transitclock.reporting.service.runTime.prescriptive.PrescriptiveRunTimeService;
import org.transitclock.reporting.service.runTime.prescriptive.PrescriptiveTimebandService;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.PrescriptiveRuntimeClusteringService;
import org.transitclock.utils.MapKey;
import org.transitclock.utils.Time;

import java.io.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.mockito.Mockito.*;


public class PrescriptiveRunTimesTest {

    private static LocalDate beginDate = LocalDate.now();
    private static LocalDate endDate = LocalDate.now();
    private static ServiceType serviceType = ServiceType.WEEKDAY;
    private static boolean readOnly = true;


    @BeforeClass
    public static void setup() throws IOException, ParseException {
        // Mock Core
        Time time = new Time(TimeZone.getDefault().getDisplayName());
        Stop stop = new Stop();
        Core mockCore = mock(Core.class, Mockito.RETURNS_DEEP_STUBS);
        when(mockCore.getTime()).thenReturn(time);
        when(mockCore.getDbConfig().getStop(anyString())).thenReturn(stop);

        MockedStatic<Core> singletonCore = mockStatic(Core.class);
        singletonCore.when(() -> Core.getInstance()).thenReturn(mockCore);

    }

    @Test
    public void processRunTimesForRoutesTest() throws IOException, ParseException {
        List<RunTimesForRoutes> runTimesForRoutes = getRunTimesForRoutes();
        Assert.assertEquals(1131, runTimesForRoutes.size());
    }

    private List<RunTimesForRoutes> getRunTimesForRoutes() throws IOException, ParseException {
        String runTimesForRoutesFile = "reporting/prescriptive/routes/015/runTimesForRoutes_015.csv";
        RunTimesForRoutesCsvDao runTimesForRouteDao = new RunTimesForRoutesCsvDao(runTimesForRoutesFile);
        List<RunTimesForRoutes> runTimesForRoutes = runTimesForRouteDao.getAll(ServiceType.WEEKDAY);

        String runTimesForStopsFile = "reporting/prescriptive/routes/015/runTimesForStops_015.csv";
        RunTimesForStopsCsvDao runTimesForStopsCsvDao = new RunTimesForStopsCsvDao(runTimesForStopsFile);
        Map<MapKey, List<RunTimesForStops>> runTimesForStopByRouteKey = runTimesForStopsCsvDao.getAllByRunTimesForRoute();

        for(RunTimesForRoutes runTimesForRoute : runTimesForRoutes){
            List<RunTimesForStops> runTimesForStops = runTimesForStopByRouteKey.get(runTimesForRoute.getKey());
            if(runTimesForStops != null){
                runTimesForRoute.setRunTimesForStops(runTimesForStops);
            }
        }

        return runTimesForRoutes;
    }

    @Test
    public void processArrivalDeparturesTest() throws IOException, ParseException {
        List<ArrivalDeparture> arrivalDepartures = getArrivalDepartures();
        Assert.assertEquals(11046, arrivalDepartures.size());
    }

    private List<ArrivalDeparture> getArrivalDepartures() throws IOException, ParseException {
        String arrivalsDeparturesFile = "reporting/prescriptive/routes/015/arrivals_departures_015.csv";
        ArrivalsDeparturesCsvDao adDao = new ArrivalsDeparturesCsvDao(arrivalsDeparturesFile);
        return adDao.getAll();
    }

    @Test
    public void processCalendarsTest() throws IOException, ParseException {
        List<org.transitclock.db.structs.Calendar> calendars = getCalendars();
        Assert.assertEquals(9, calendars.size());
    }

    private List<org.transitclock.db.structs.Calendar> getCalendars() throws IOException, ParseException {
        String calendarsFile = "reporting/prescriptive/routes/015/calendars.csv";
        CalendarsCsvDao calendarsDao = new CalendarsCsvDao(calendarsFile);
        return calendarsDao.getAll();
    }

    @Test
    public void processCalendarDatesTest() throws IOException, ParseException {
        List<CalendarDate> calendarDates = getCalendarDates();
        Assert.assertEquals(135, calendarDates.size());
    }

    private List<CalendarDate> getCalendarDates() throws IOException, ParseException {
        String calendarDatesFile = "reporting/prescriptive/routes/015/calendar_dates.csv";
        CalendarDatesCsvDao calendarDatesDao = new CalendarDatesCsvDao(calendarDatesFile);
        return calendarDatesDao.getAll();
    }

    @Test
    public void processTripsTest() throws IOException, ParseException {

        List<Trip> trips = getTripsWithScheduleTimes();

        Assert.assertEquals(272, trips.size());

        Trip sampleTrip = trips.get(0);

        Assert.assertNotNull(sampleTrip.getStartTime());
        Assert.assertNotNull(sampleTrip.getEndTime());
        Assert.assertNotNull(sampleTrip.getTripPatternId());

    }

    private static List<Trip> getTripsWithScheduleTimes() throws IOException, ParseException {
        String tripsFile = "reporting/prescriptive/routes/015/trips_015.csv";
        TripsCsvDao tripsDao = new TripsCsvDao(tripsFile);
        List<Trip> trips = tripsDao.getAll();

        String tripsScheduleFile = "reporting/prescriptive/routes/015/trip_schedule_times_015.csv";
        TripScheduleTimesCsvDao tripSchedulesDao = new TripScheduleTimesCsvDao(tripsScheduleFile);
        Map<String, List<ScheduleTime>> tripSchedulesByTripId = tripSchedulesDao.getAllTimesByTrip();

        for(Trip trip : trips){
            List<ScheduleTime> scheduleTimes = tripSchedulesByTripId.get(trip.getId());
            if(scheduleTimes != null){
                trip.addScheduleTimes(scheduleTimes);
            }
        }

        return trips;
    }

    @Test
    public void processTripPattern() throws IOException, ParseException {

        List<TripPattern> tripPatterns = getTripPatternsWithStopPaths();

        Assert.assertEquals(2, tripPatterns.size());

        TripPattern tripPattern = tripPatterns.get(0);
        Assert.assertEquals(62, tripPattern.getStopPaths().size());

    }

    private List<TripPattern> getTripPatternsWithStopPaths() throws IOException, ParseException {
        String stopPathsFile = "reporting/prescriptive/routes/015/stopPaths_015.csv";
        StopPathsCsvDao stopPathsCsvDao = new StopPathsCsvDao(stopPathsFile);
        List<StopPath> stopPaths = stopPathsCsvDao.getAll();

        String tripPatternsFile = "reporting/prescriptive/routes/015/trip_patterns_015.csv";
        TripPatternsCsvDao tripPatternsDao = new TripPatternsCsvDao(tripPatternsFile);
        List<TripPattern> tripPatterns = tripPatternsDao.getAllTripPatterns(stopPaths);

        return tripPatterns;
    }

    @Test
    public void runTimeServiceTest() throws Exception {
        List<RunTimesForRoutes> runTimesForRoutes = getRunTimesForRoutes();

        // Mock RunTimeService
        RunTimeService runTimeService = spy(RunTimeService.class);

        // Mock RunTimeRoutesDao
        RunTimeRoutesDao runTimeRoutesDao = mock(RunTimeRoutesDao.class);
        when(runTimeRoutesDao.getRunTimesForRoutes(any(RunTimeForRouteQuery.class))).thenReturn(getRunTimesForRoutes());

        // RunTimeService Inject Mocked Classes
        runTimeService.setDao(runTimeRoutesDao);

        RunTimeForRouteQuery query = new RunTimeForRouteQuery.Builder().build();

        List<RunTimesForRoutes> filteredRunTimesForRoutes = runTimeService.getRunTimesForRoutes(query);

        Assert.assertEquals(1131, runTimesForRoutes.size());
        Assert.assertEquals(689, filteredRunTimesForRoutes.size());
    }


    @Test
    public void prescriptiveRunTimeServiceTest() throws Exception {

        String routeShortName = "015";
        Integer configRev = 12;

        // Data
        List<RunTimesForRoutes> runTimesForRoutes = getRunTimesForRoutes();
        List<ArrivalDeparture> arrivalDepartures = getArrivalDepartures();
        List<org.transitclock.db.structs.Calendar> calendars = getCalendars();
        List<CalendarDate> calendarDates = getCalendarDates();
        List<TripPattern> tripPatterns = getTripPatternsWithStopPaths();
        List<Trip> trips = getTripsWithScheduleTimes();

        // Mock RunTimeRoutesDao
        RunTimeRoutesDao runTimeRoutesDao = mock(RunTimeRoutesDao.class);
        when(runTimeRoutesDao.getRunTimesForRoutes(any(RunTimeForRouteQuery.class))).thenReturn(runTimesForRoutes);

        // Mock RunTimeService
        RunTimeService runTimeService = spy(RunTimeService.class);

        // Mock PrescriptiveTimebandService
        PrescriptiveTimebandService timebandService = spy(PrescriptiveTimebandService.class);

        // Mock PrescriptiveRunTimeService
        PrescriptiveRuntimeClusteringService prescriptiveClusteringService = spy(PrescriptiveRuntimeClusteringService.class);

        // Mocked Static Methods
        MockedStatic<Trip> staticTrip = mockStatic(Trip.class);
        staticTrip.when(() -> Trip.getTripsFromDb(any(TripQuery.class))).thenReturn(trips);

        MockedStatic<TripPattern> staticTripPattern = mockStatic(TripPattern.class);
        staticTripPattern.when(() -> TripPattern.getTripPatternsForRoute(anyString(), anyInt(), anyBoolean())).thenReturn(tripPatterns);

        MockedStatic<Calendar> staticCalendar = mockStatic(Calendar.class);
        staticCalendar.when(() -> Calendar.getCalendars(any(Session.class), anyInt())).thenReturn(calendars);

        MockedStatic<CalendarDate> staticCalendarDate = mockStatic(CalendarDate.class);
        staticCalendarDate.when(() -> CalendarDate.getCalendarDates(any(Session.class), anyInt())).thenReturn(calendarDates);

        MockedStatic<ArrivalDeparture> arrivalDepartureMockedStatic = mockStatic(ArrivalDeparture.class);
        arrivalDepartureMockedStatic.when(() -> ArrivalDeparture.getArrivalsDeparturesFromDb(any(ArrivalDepartureQuery.class))).thenReturn(arrivalDepartures);

        // Instantiate Prescriptive RunTimes Service
        PrescriptiveRunTimeService prescriptiveRunTimeService = new PrescriptiveRunTimeService();

        // RunTimeService Inject Mocked Classes
        runTimeService.setDao(runTimeRoutesDao);

        // TimebandService Inject Mocked Classes
        timebandService.setRunTimeService(runTimeService);
        timebandService.setClusteringService(prescriptiveClusteringService);

        // PrescriptiveRunTimeService Inject Mocked Classes
        prescriptiveRunTimeService.setRunTimeService(runTimeService);
        prescriptiveRunTimeService.setTimebandService(timebandService);

        IpcPrescriptiveRunTimesForPatterns timeBands = prescriptiveRunTimeService.
                getPrescriptiveRunTimeBands(beginDate, endDate, routeShortName, serviceType, configRev, readOnly);

        Assert.assertNotNull(timeBands);
        Assert.assertEquals(tripPatterns.size(), timeBands.getRunTimesForPatterns().size());

        List<IpcPrescriptiveRunTimesForPattern> patterns = timeBands.getRunTimesForPatterns();
        for(int i=0; i<patterns.size(); i++){
            Assert.assertEquals(routeShortName, patterns.get(i).getRouteShortName());
            Assert.assertEquals(tripPatterns.get(i).getScheduleAdhStopPaths().size(), patterns.get(i).getTimePoints().size());

            List<IpcPrescriptiveRunTimesForTimeBand> timebands = patterns.get(i).getRunTimesForTimeBands();
            for(int j=0; j < timebands.size(); j++){
                IpcPrescriptiveRunTimesForTimeBand timeband = timebands.get(j);
                if(j == 0){
                    Assert.assertEquals("00:00", timeband.getStartTime());
                }
                System.out.println(timeband.getStartTime() + " - " + timeband.getEndTime());
                System.out.println("Current OTP " + timeband.getCurrentOtp());
                System.out.println("Expected OTP " + timeband.getExpectedOtp());
                Assert.assertTrue(timeband.getExpectedOtp() * 100 > 50);
            }

            patterns.get(i).getRunTimesForTimeBands();
        }

        double currentOtp = timeBands.getCurrentOnTime();
        double expectedOtp = timeBands.getExpectedOnTime();
        double totalRunTime = timeBands.getTotalRunTimes();

        System.out.println("-----------------------------------------------------");
        System.out.println("Overall Current OTP " + currentOtp/totalRunTime * 100);
        System.out.println("Overall Expected OTP " + expectedOtp/totalRunTime * 100);

    }





}
