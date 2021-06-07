package org.transitclock.ipc.interfaces;

import org.transitclock.core.ServiceType;
import org.transitclock.ipc.data.*;

import java.rmi.Remote;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Defines the RMI interface used for obtaining schedule adherence information.
 *
 * @author carabalb
 *
 */
public interface ReportingInterface extends Remote {
    IpcPrescriptiveRunTimes getPrescriptiveRunTimes(LocalTime beginTime,
                                                    LocalTime endTime,
                                                    String routeIdOrShortName,
                                                    String headsign,
                                                    String directionId,
                                                    String tripPatternId,
                                                    ServiceType serviceType,
                                                    boolean readOnly) throws Exception;


    List<IpcStopTime> getPrescriptiveRunTimesSchedule(LocalTime beginTime,
                                                      LocalTime endTime,
                                                      String routeIdOrShortName,
                                                      String headsign,
                                                      String directionId,
                                                      String tripPatternId,
                                                      ServiceType serviceType,
                                                      boolean readOnly) throws Exception;

    List<IpcArrivalDepartureScheduleAdherence> getArrivalsDeparturesForOtp(LocalDate beginDate, LocalDate endDate,
                                                                           LocalTime beginTime, LocalTime endTime,
                                                                           String routeId, ServiceType serviceType,
                                                                           boolean timePointsOnly, String headsign,
                                                                           boolean readOnly) throws Exception;

    List<IpcStopWithDwellTime> getStopsWithAvgDwellTimes(LocalDate beginDate, LocalDate endDate,
                                                         LocalTime beginTime, LocalTime endTime, String routeIdOrShortName,
                                                         ServiceType serviceType, boolean timePointsOnly, String headsign,
                                                         boolean readOnly) throws Exception;

    List<IpcStopPathWithSpeed> getStopPathsWithSpeed(LocalDate beginDate, LocalDate endDate,
                                                     LocalTime beginTime, LocalTime endTime,
                                                     String routeIdOrShortName, ServiceType serviceType,
                                                     String headsign, boolean readOnly) throws Exception;

    IpcDoubleSummaryStatistics getAverageRunTime(LocalDate beginDate, LocalDate endDate,
                                                 LocalTime beginTime, LocalTime endTime, String routeIdOrShortName,
                                                 ServiceType serviceType, boolean timePointsOnly,
                                                 String headsign, boolean readOnly) throws Exception;

    IpcRunTime getRunTimeSummary(LocalDate beginDate, LocalDate endDate,
                           LocalTime beginTime, LocalTime endTime,
                           String routeIdOrShortName, String headsign, String directionId,
                           String tripPatternId, ServiceType serviceType, boolean readOnly) throws Exception;

    IpcRunTimeForTripsAndDistribution getRunTimeForTrips(LocalDate beginDate, LocalDate endDate,
                                  LocalTime beginTime, LocalTime endTime,
                                  String routeIdOrShortName, String headsign,
                                  String tripPatternId, String directionId, ServiceType serviceType,
                                  boolean timePointsOnly, boolean readOnly) throws Exception;

    List<IpcRunTimeForStopPath> getRunTimeForStopPaths(LocalDate beginDate, LocalDate endDate,
                                                   LocalTime beginTime, LocalTime endTime,
                                                   String routeIdOrShortName, String tripId, ServiceType serviceType,
                                                   boolean timePointsOnly, boolean readOnly) throws Exception;

    List<IpcRunTimeForRoute> getRunTimeForRoutes(LocalDate beginDate, LocalDate endDate,
                                                        LocalTime beginTime, LocalTime endTime, ServiceType serviceType,
                                                        Integer earlyThreshold, Integer lateThreshold, boolean readOnly) throws Exception;
}