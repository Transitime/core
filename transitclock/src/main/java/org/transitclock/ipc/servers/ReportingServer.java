package org.transitclock.ipc.servers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.ServiceType;
import org.transitclock.ipc.data.*;
import org.transitclock.ipc.interfaces.ReportingInterface;
import org.transitclock.ipc.rmi.AbstractServer;
import org.transitclock.ipc.servers.reporting.service.OnTimePerformanceService;
import org.transitclock.ipc.servers.reporting.service.RunTimeService;
import org.transitclock.ipc.servers.reporting.service.SpeedMapService;

import java.time.*;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

/**
 * @author carabalb
 * Server to allow Reporting information to be queried.
 */
public class ReportingServer extends AbstractServer implements ReportingInterface {

    // Should only be accessed as singleton class
    private static ReportingServer singleton;

    private SpeedMapService speedMapService;

    private RunTimeService runTimeService;

    private OnTimePerformanceService onTimePerformanceService;

    private static final Logger logger =
            LoggerFactory.getLogger(ReportingServer.class);

    private ReportingServer(String agencyId) {
        super(agencyId, ReportingInterface.class.getSimpleName());
        speedMapService = new SpeedMapService();
        runTimeService = new RunTimeService();
        onTimePerformanceService = new OnTimePerformanceService();
    }

    public static ReportingServer start(String agencyId) {
        if (singleton == null) {
            singleton = new ReportingServer(agencyId);
        }

        if (!singleton.getAgencyId().equals(agencyId)) {
            logger.error("Tried calling ReportingServer.start() for " +
                            "agencyId={} but the singleton was created for agencyId={}",
                    agencyId, singleton.getAgencyId());
            return null;
        }

        return singleton;
    }

    // Speed Map Reports
    @Override
    public List<IpcStopPathWithSpeed> getStopPathsWithSpeed(LocalDate beginDate, LocalDate endDate,
                                                            LocalTime beginTime, LocalTime endTime,
                                                            String routeIdOrShortName, ServiceType serviceType,
                                                            String headsign, boolean readOnly) throws Exception{

        return speedMapService.getStopPathsWithSpeed(beginDate, endDate, beginTime, endTime, routeIdOrShortName, serviceType,
                headsign, getAgencyId(), readOnly);
    }

    @Override
    public List<IpcStopWithDwellTime> getStopsWithAvgDwellTimes(LocalDate beginDate, LocalDate endDate,
                                                                LocalTime beginTime, LocalTime endTime,
                                                                String routeIdOrShortName, ServiceType serviceType,
                                                                boolean timePointsOnly, String headsign,
                                                                boolean readOnly) throws Exception {

        return speedMapService.getStopsWithAvgDwellTimes(beginDate, endDate, beginTime, endTime, routeIdOrShortName,
                serviceType, timePointsOnly, headsign, readOnly);
    }

    @Override
    public IpcDoubleSummaryStatistics getAverageRunTime(LocalDate beginDate, LocalDate endDate,
                                    LocalTime beginTime, LocalTime endTime, String routeIdOrShortName,
                                    ServiceType serviceType, boolean timePointsOnly,
                                    String headsign, boolean readOnly) throws Exception {

        return runTimeService.getAverageRunTime(beginDate, endDate, beginTime, endTime, routeIdOrShortName,
                serviceType, timePointsOnly, headsign, readOnly);
    }

    // Run Times Reports
    @Override
    public IpcRunTime getRunTimeSummary(LocalDate beginDate, LocalDate endDate,
                                  LocalTime beginTime, LocalTime endTime,
                                  String routeIdOrShortName, String headsign,
                                  String startStop, String endStop,
                                  ServiceType serviceType, boolean timePointsOnly,
                                  boolean currentTripsOnly, boolean readOnly) throws Exception {

        return runTimeService.getRunTimeSummary(beginDate, endDate, beginTime, endTime, routeIdOrShortName, headsign,
                startStop, endStop, serviceType, timePointsOnly, currentTripsOnly, this.getAgencyId(), readOnly);
    }

    @Override
    public List<IpcRunTimeForTrip> getRunTimeForTrips(LocalDate beginDate, LocalDate endDate,
                                         LocalTime beginTime, LocalTime endTime,
                                         String routeIdOrShortName, String headsign,
                                         String startStop, String endStop,
                                         ServiceType serviceType, boolean timePointsOnly,
                                         boolean currentTripsOnly, boolean readOnly) throws Exception {

        return runTimeService.getRunTimeForTrips(beginDate, endDate, beginTime, endTime, routeIdOrShortName, headsign,
                startStop, endStop, serviceType, timePointsOnly, currentTripsOnly, this.getAgencyId(), readOnly);
    }

    // On Time Performance Reports
    @Override
    public List<IpcArrivalDepartureScheduleAdherence> getArrivalsDeparturesForOtp(
            LocalDate beginDate, LocalDate endDate, LocalTime beginTime, LocalTime endTime,
            String routeIdOrShortName, ServiceType serviceType,
            boolean timePointsOnly, String headsign) throws Exception{
        return onTimePerformanceService.getArrivalsDeparturesForOtp(beginDate, endDate, beginTime, endTime,
                routeIdOrShortName, serviceType, timePointsOnly, headsign, false);
    }

    @Override
    public List<IpcArrivalDepartureScheduleAdherence> getArrivalsDeparturesForOtp(
            LocalDate beginDate, LocalDate endDate, LocalTime beginTime, LocalTime endTime,
            String routeIdOrShortName, ServiceType serviceType,
            boolean timePointsOnly, String headsign, boolean readOnly) throws Exception{
        return onTimePerformanceService.getArrivalsDeparturesForOtp(beginDate, endDate, beginTime, endTime,
                routeIdOrShortName, serviceType, timePointsOnly, headsign, readOnly);
    }

}
