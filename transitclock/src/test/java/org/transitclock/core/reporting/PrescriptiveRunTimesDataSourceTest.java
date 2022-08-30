package org.transitclock.core.reporting;

import org.junit.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.transitclock.applications.Core;
import org.transitclock.db.query.ArrivalDepartureQuery;
import org.transitclock.db.query.RunTimeForRouteQuery;
import org.transitclock.db.structs.*;
import org.transitclock.ipc.data.IpcArrivalDepartureScheduleAdherence;
import org.transitclock.reporting.dao.RunTimeRoutesDao;
import org.transitclock.reporting.service.OnTimePerformanceService;
import org.transitclock.reporting.service.RunTimeService;

import org.transitclock.utils.Time;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;


public class PrescriptiveRunTimesDataSourceTest extends AbstractPrescriptiveRunTimesTests{

    private MockedStatic<Core> singletonCore;

    private static final String SERVICE_PERIOD_169 = "169 - 2022-04-18 - 2022-06-12";

    private static final String SERVICE_PERIOD_171 = "171 - 2022-06-13 - 2022-09-11";

    @Before
    public void setup() {
        // Mock Core
        Time time = new Time(TimeZone.getDefault().getDisplayName());
        Stop stop = new Stop();
        Core mockCore = mock(Core.class, Mockito.RETURNS_DEEP_STUBS);
        when(mockCore.getTime()).thenReturn(time);
        when(mockCore.getDbConfig().getStop(anyString())).thenReturn(stop);

        singletonCore = mockStatic(Core.class);
        singletonCore.when(() -> Core.getInstance()).thenReturn(mockCore);

    }

    @After
    public void teardown(){
        singletonCore.close();
    }


    @Test
    public void processRunTimesForRoutesTest() throws IOException, ParseException {
        String routeShortName = "015";
        List<RunTimesForRoutes> runTimesForRoutes = getRunTimesForRoutes(routeShortName, SERVICE_PERIOD_171);
        Assert.assertEquals(1131, runTimesForRoutes.size());
    }

    @Test
    public void processArrivalDeparturesTest() throws IOException, ParseException {
        String routeShortName = "015";
        List<ArrivalDeparture> arrivalDepartures = getArrivalDepartures(routeShortName, SERVICE_PERIOD_171);
        Assert.assertEquals(11046, arrivalDepartures.size());
    }

    @Test
    public void onTimePerformanceTest() throws Exception {
        String routeShortName = "001";

        List<ArrivalDeparture> arrivalDepartures = getArrivalDepartures(routeShortName, SERVICE_PERIOD_171);
        Assert.assertEquals(25892, arrivalDepartures.size());

        List<ArrivalDeparture> departures = filterDeparturesOnly(arrivalDepartures);
        Assert.assertEquals(12997, departures.size());

        double manualOtpCalc = calculateOnTimePerformance(departures);
        System.out.println(manualOtpCalc);


        OnTimePerformanceService onTimePerformanceService = new OnTimePerformanceService();

        MockedStatic<ArrivalDeparture> arrivalDepartureMockedStatic = mockStatic(ArrivalDeparture.class);
        arrivalDepartureMockedStatic.
                when(() -> ArrivalDeparture.getArrivalsDeparturesFromDb(any(ArrivalDepartureQuery.class)))
                .thenReturn(arrivalDepartures);

        ArrivalDepartureQuery query = new ArrivalDepartureQuery.Builder().build();
        List<IpcArrivalDepartureScheduleAdherence> adScheduleAdherence = onTimePerformanceService.getArrivalsDeparturesForOtp(query);
        System.out.println("done");
    }

    private List<ArrivalDeparture> filterDeparturesOnly(List<ArrivalDeparture> arrivalDepartures){
        List<ArrivalDeparture> departuresOnly = new ArrayList<>();
        for(ArrivalDeparture arrivalDeparture : arrivalDepartures){
            if(arrivalDeparture.isDeparture()){
                departuresOnly.add(arrivalDeparture);
            }
        }
        return departuresOnly;
    }

    private double calculateOnTimePerformance(List<ArrivalDeparture> departures){
        double onTimeCount = 0;
        double totalCount = departures.size();
        long maxLateMsec = TimeUnit.MINUTES.toMillis(-5);
        long maxEarlyMsec = TimeUnit.MINUTES.toMillis(1);
        for(ArrivalDeparture departure : departures){
            long timeDiffMsec = departure.getScheduledTime() - departure.getTime();
            // Late time is negative since we subtract scheduled from actual time
            // eg. 10:40 scheduled time minus 10:45 actual time is -5 minutes
            if(timeDiffMsec < maxLateMsec){
                continue;
            }
            // Late time is positive since we subtract scheduled from actual time
            // eg. 10:45 scheduled time minus 10:44 actual time is 1 minute
            if(timeDiffMsec > maxEarlyMsec){
                continue;
            }
            onTimeCount++;
        }
        double otpRatio = (onTimeCount/totalCount) * 100d;
        return otpRatio;
    }



    @Test
    public void processCalendarsTest() throws IOException, ParseException {
        String routeShortName = "015";
        List<org.transitclock.db.structs.Calendar> calendars = getCalendars(routeShortName, SERVICE_PERIOD_171);
        Assert.assertEquals(9, calendars.size());
    }

    @Test
    public void processCalendarDatesTest() throws IOException, ParseException {
        String routeShortName = "015";
        List<CalendarDate> calendarDates = getCalendarDates(routeShortName, SERVICE_PERIOD_171);
        Assert.assertEquals(135, calendarDates.size());
    }

    @Test
    public void processTripsTest() throws IOException, ParseException {
        String routeShortName = "015";

        List<Trip> trips = getTripsWithScheduleTimes(routeShortName, SERVICE_PERIOD_171);

        Assert.assertEquals(272, trips.size());

        Trip sampleTrip = trips.get(0);

        Assert.assertNotNull(sampleTrip.getStartTime());
        Assert.assertNotNull(sampleTrip.getEndTime());
        Assert.assertNotNull(sampleTrip.getTripPatternId());

    }

    @Test
    public void processTripPattern() throws IOException, ParseException {
        String routeShortName = "015";
        List<TripPattern> tripPatterns = getTripPatternsWithStopPaths(routeShortName, SERVICE_PERIOD_171);

        Assert.assertEquals(2, tripPatterns.size());

        TripPattern tripPattern = tripPatterns.get(0);
        Assert.assertEquals(62, tripPattern.getStopPaths().size());

    }

    @Test
    public void runTimeServiceTest() throws Exception {
        String routeShortName = "015";
        List<RunTimesForRoutes> runTimesForRoutes = getRunTimesForRoutes(routeShortName, SERVICE_PERIOD_171);

        // Mock RunTimeService
        RunTimeService runTimeService = spy(RunTimeService.class);

        // Mock RunTimeRoutesDao
        RunTimeRoutesDao runTimeRoutesDao = mock(RunTimeRoutesDao.class);
        when(runTimeRoutesDao.getRunTimesForRoutes(any(RunTimeForRouteQuery.class)))
                             .thenReturn(getRunTimesForRoutes(routeShortName, SERVICE_PERIOD_171));

        // RunTimeService Inject Mocked Classes
        runTimeService.setDao(runTimeRoutesDao);

        RunTimeForRouteQuery query = new RunTimeForRouteQuery.Builder().build();

        List<RunTimesForRoutes> filteredRunTimesForRoutes = runTimeService.getRunTimesForRoutes(query);

        Assert.assertEquals(1131, runTimesForRoutes.size());
        Assert.assertEquals(689, filteredRunTimesForRoutes.size());
    }

    @Test
    public void processFeedInfo() throws IOException, ParseException {
        String servicePeriod = "169 - 2022-04-18 - 2022-06-12";
        String feedVersion = "V310-166-165-20220124";

        String routeShortName = "";
        List<FeedInfo> feedInfoList = getFeedInfo( servicePeriod);

        Assert.assertEquals(17, feedInfoList.size());

        FeedInfo feedInfo = feedInfoList.get(0);
        Assert.assertEquals(feedVersion, feedInfo.getFeedVersion());

    }
}
