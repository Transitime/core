package org.transitclock.ipc.servers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.ServiceType;
import org.transitclock.ipc.data.*;
import org.transitclock.ipc.interfaces.ReportingInterface;
import org.transitclock.ipc.rmi.AbstractServer;
import org.transitclock.reporting.service.OnTimePerformanceService;
import org.transitclock.reporting.service.RunTimeService;
import org.transitclock.reporting.service.SpeedMapService;

import javax.inject.Inject;
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

    @Inject
    private SpeedMapService speedMapService;

    @Inject
    private RunTimeService runTimeService;

    @Inject
    private OnTimePerformanceService onTimePerformanceService;

    private static final Logger logger =
            LoggerFactory.getLogger(ReportingServer.class);


    public void start(String agencyId){
        start(agencyId, ReportingInterface.class.getSimpleName());
    }

    // Speed Map Reports
    @Override
    public List<IpcStopPathWithSpeed> getStopPathsWithSpeed(LocalDate beginDate,
                                                            LocalDate endDate,
                                                            LocalTime beginTime,
                                                            LocalTime endTime,
                                                            String routeIdOrShortName,
                                                            ServiceType serviceType,
                                                            String headsign,
                                                            boolean readOnly) throws Exception{

        return speedMapService.getStopPathsWithSpeed(beginDate, endDate, beginTime, endTime, routeIdOrShortName, serviceType,
                headsign, getAgencyId(), readOnly);
    }

    @Override
    public List<IpcStopWithDwellTime> getStopsWithAvgDwellTimes(LocalDate beginDate,
                                                                LocalDate endDate,
                                                                LocalTime beginTime,
                                                                LocalTime endTime,
                                                                String routeIdOrShortName,
                                                                ServiceType serviceType,
                                                                boolean timePointsOnly,
                                                                String headsign,
                                                                boolean readOnly) throws Exception {

        return speedMapService.getStopsWithAvgDwellTimes(beginDate, endDate, beginTime, endTime, routeIdOrShortName,
                serviceType, timePointsOnly, headsign, readOnly);
    }

    @Override
    public IpcDoubleSummaryStatistics getAverageRunTime(LocalDate beginDate,
                                                        LocalDate endDate,
                                                        LocalTime beginTime,
                                                        LocalTime endTime,
                                                        String routeIdOrShortName,
                                                        ServiceType serviceType,
                                                        boolean timePointsOnly,
                                                        String headsign,
                                                        boolean readOnly) throws Exception {

        return runTimeService.getAverageRunTime(beginDate, endDate, beginTime, endTime, routeIdOrShortName,
                serviceType, timePointsOnly, headsign, readOnly);
    }

    // Run Times Reports
    @Override
    public IpcRunTime getRunTimeSummary(LocalDate beginDate,
                                        LocalDate endDate,
                                        LocalTime beginTime,
                                        LocalTime endTime,
                                        String routeIdOrShortName,
                                        String headsign,
                                        String startStop,
                                        String endStop,
                                        ServiceType serviceType,
                                        boolean timePointsOnly,
                                        boolean currentTripsOnly,
                                        boolean readOnly) throws Exception {

        return runTimeService.getRunTimeSummary(beginDate, endDate, beginTime, endTime, routeIdOrShortName, headsign,
                startStop, endStop, serviceType, timePointsOnly, currentTripsOnly, this.getAgencyId(), readOnly);
    }

    @Override
    public List<IpcRunTimeForTrip> getRunTimeForTrips(LocalDate beginDate,
                                                      LocalDate endDate,
                                                      LocalTime beginTime,
                                                      LocalTime endTime,
                                                      String routeIdOrShortName,
                                                      String headsign,
                                                      String tripPatternId,
                                                      ServiceType serviceType,
                                                      boolean timePointsOnly,
                                                      boolean currentTripsOnly,
                                                      boolean readOnly) throws Exception {

        return runTimeService.getRunTimeForTrips(beginDate, endDate, beginTime, endTime, routeIdOrShortName, headsign,
                tripPatternId, serviceType, timePointsOnly, currentTripsOnly, this.getAgencyId(), readOnly);
    }

    @Override
    public List<IpcRunTimeForStopPath> getRunTimeForStopPaths(LocalDate beginDate,
                                                          LocalDate endDate,
                                                          LocalTime beginTime,
                                                          LocalTime endTime,
                                                          String routeIdOrShortName,
                                                          String tripId,
                                                          ServiceType serviceType,
                                                          boolean timePointsOnly,
                                                          boolean readOnly) throws Exception {
        return runTimeService.getRunTimeForStopPaths(beginDate, endDate, beginTime, endTime, routeIdOrShortName,
                tripId, serviceType, timePointsOnly, this.getAgencyId(), readOnly);
    }

    // On Time Performance Reports
    @Override
    public List<IpcArrivalDepartureScheduleAdherence> getArrivalsDeparturesForOtp(LocalDate beginDate,
                                                                                  LocalDate endDate,
                                                                                  LocalTime beginTime,
                                                                                  LocalTime endTime,
                                                                                  String routeIdOrShortName,
                                                                                  ServiceType serviceType,
                                                                                  boolean timePointsOnly,
                                                                                  String headsign) throws Exception{

        return onTimePerformanceService.getArrivalsDeparturesForOtp(beginDate, endDate, beginTime, endTime,
                routeIdOrShortName, serviceType, timePointsOnly, headsign, false);
    }

    @Override
    public List<IpcArrivalDepartureScheduleAdherence> getArrivalsDeparturesForOtp(LocalDate beginDate,
                                                                                  LocalDate endDate,
                                                                                  LocalTime beginTime,
                                                                                  LocalTime endTime,
                                                                                  String routeIdOrShortName,
                                                                                  ServiceType serviceType,
                                                                                  boolean timePointsOnly,
                                                                                  String headsign,
                                                                                  boolean readOnly) throws Exception{

        return onTimePerformanceService.getArrivalsDeparturesForOtp(beginDate, endDate, beginTime, endTime,
                routeIdOrShortName, serviceType, timePointsOnly, headsign, readOnly);
    }

}
