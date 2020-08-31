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
    List<IpcArrivalDepartureScheduleAdherence> getArrivalsDeparturesForOtp(LocalDate beginDate, LocalDate endDate,
                                                                             LocalTime beginTime, LocalTime endTime,
                                                                             String routeIdOrShortName, ServiceType serviceType,
                                                                             boolean timePointsOnly, String headsign) throws Exception;

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
                           String routeIdOrShortName, String headsign,
                           String startStop, String endStop,
                           ServiceType serviceType, boolean timePointsOnly,
                           boolean currentTripsOnly, boolean readOnly) throws Exception;

    List<IpcRunTimeForTrip> getRunTimeForTrips(LocalDate beginDate, LocalDate endDate,
                                  LocalTime beginTime, LocalTime endTime,
                                  String routeIdOrShortName, String headsign,
                                  String startStop, String endStop,
                                  ServiceType serviceType, boolean timePointsOnly,
                                  boolean currentTripsOnly, boolean readOnly) throws Exception;
}