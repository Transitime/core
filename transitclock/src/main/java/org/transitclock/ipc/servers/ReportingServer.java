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
import org.transitclock.reporting.service.runTime.*;
import org.transitclock.reporting.service.runTime.prescriptive.PrescriptiveRunTimeService;

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

    @Inject
    private RouteRunTimesService routeRunTimesService;

    @Inject
    private StopRunTimesService stopRunTimesService;

    @Inject
    private TripRunTimesService tripRunTimesService;

    @Inject
    private PrescriptiveRunTimeService prescriptiveRunTimeService;


    private static final Logger logger = LoggerFactory.getLogger(ReportingServer.class);


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

        return speedMapService.getAverageRunTime(beginDate, endDate, beginTime, endTime, routeIdOrShortName,
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
                                        String directionId,
                                        String tripPatternId,
                                        ServiceType serviceType,
                                        boolean readOnly) throws Exception {

        return runTimeService.getRunTimeSummary(beginDate, endDate, beginTime, endTime, routeIdOrShortName, headsign,
                                                directionId, tripPatternId, serviceType, readOnly);
    }

    @Override
    public IpcRunTimeForTripsAndDistribution getRunTimeForTrips(LocalDate beginDate,
                                                                      LocalDate endDate,
                                                                      LocalTime beginTime,
                                                                      LocalTime endTime,
                                                                      String routeIdOrShortName,
                                                                      String headsign,
                                                                      String tripPatternId,
                                                                      String directionId,
                                                                      ServiceType serviceType,
                                                                      boolean timePointsOnly,
                                                                      boolean readOnly) throws Exception {

        return tripRunTimesService.getRunTimeForTripsByStopPath(beginDate, endDate, beginTime, endTime,
                routeIdOrShortName, headsign, tripPatternId, directionId, serviceType, readOnly);
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

        return stopRunTimesService.getRunTimeForStopPaths(beginDate, endDate, beginTime, endTime, routeIdOrShortName,
                tripId, serviceType, timePointsOnly, this.getAgencyId(), readOnly);
    }

    /**
     * Get Dated Gtfs Versions
     * @return
     * @throws Exception
     */
    @Override
    public List<IpcDatedGtfs> getDatedGtfs() throws Exception {
        return prescriptiveRunTimeService.getDatedGtfs();
    }

    /**
     * New prescriptive runtimes method
     * @param beginDate
     * @param endDate
     * @param routeIdOrShortName
     * @param serviceType
     * @param configRev
     * @param readOnly
     * @return
     * @throws Exception
     */
    @Override
    public IpcPrescriptiveRunTimesForPatterns getPrescriptiveRunTimeBands(LocalDate beginDate,
                                                                               LocalDate endDate,
                                                                               String routeIdOrShortName,
                                                                               ServiceType serviceType,
                                                                               int configRev,
                                                                               boolean readOnly) throws Exception {

        return prescriptiveRunTimeService.getPrescriptiveRunTimeBands(beginDate, endDate, routeIdOrShortName,
                                                                        serviceType, configRev, readOnly);
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
                                                                                  String headsign,
                                                                                  boolean readOnly) throws Exception{

        return onTimePerformanceService.getArrivalsDeparturesForOtp(beginDate, endDate, beginTime, endTime,
                routeIdOrShortName, serviceType, timePointsOnly, headsign, readOnly);
    }

    @Override
    public List<IpcRunTimeForRoute> getRunTimeForRoutes(LocalDate beginDate, LocalDate endDate,
                                                        LocalTime beginTime, LocalTime endTime, ServiceType serviceType,
                                                        Integer earlyThreshold, Integer lateThreshold,
                                                        boolean readOnly) throws Exception{
        return routeRunTimesService.getRunTimeForRoutes(beginDate, endDate, beginTime, endTime, serviceType,
                                                        earlyThreshold, lateThreshold, readOnly);
    }

}
