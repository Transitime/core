package org.transitclock.ipc.interfaces;

import org.transitclock.core.ServiceType;
import org.transitclock.ipc.data.IpcArrivalDepartureScheduleAdherence;
import org.transitclock.ipc.data.IpcDoubleSummaryStatistics;
import org.transitclock.ipc.data.IpcStopWithDwellTime;

import java.rmi.Remote;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.DoubleSummaryStatistics;
import java.util.List;

/**
 * Defines the RMI interface used for obtaining schedule adherence information.
 *
 * @author carabalb
 *
 */
public interface ReportingInterface extends Remote {
    List<IpcArrivalDepartureScheduleAdherence> getArrivalsDeparturesForRoute(LocalDate beginDate, LocalDate endDate,
                                                                             LocalTime beginTime, LocalTime endTime,
                                                                             String routeIdOrShortName, ServiceType serviceType,
                                                                             boolean timePointsOnly, String headsign) throws Exception;

    List<IpcArrivalDepartureScheduleAdherence> getArrivalsDeparturesForRoute(LocalDate beginDate, LocalDate endDate,
                                                                             LocalTime beginTime, LocalTime endTime,
                                                                             String routeId, ServiceType serviceType,
                                                                             boolean timePointsOnly, String headsign,
                                                                             boolean readOnly) throws Exception;

    List<IpcStopWithDwellTime> getStopsWithAvgDwellTimes(LocalDate beginDate, LocalDate endDate,
                                                         LocalTime beginTime, LocalTime endTime, String routeIdOrShortName,
                                                         ServiceType serviceType, boolean timePointsOnly, String headsign,
                                                         boolean readOnly) throws Exception;

    IpcDoubleSummaryStatistics getAverageRunTime(LocalDate beginDate, LocalDate endDate,
                                                 LocalTime beginTime, LocalTime endTime, String routeIdOrShortName,
                                                 ServiceType serviceType, boolean timePointsOnly,
                                                 String headsign, boolean readOnly) throws Exception;
}