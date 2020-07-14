package org.transitclock.ipc.interfaces;

import org.transitclock.core.ServiceType;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.ipc.data.IpcArrivalDepartureScheduleAdherence;
import org.transitclock.ipc.data.IpcStopWithDwellTime;

import java.rmi.Remote;
import java.util.Date;
import java.util.List;

/**
 * Defines the RMI interface used for obtaining schedule adherence information.
 *
 * @author carabalb
 *
 */
public interface ReportingInterface extends Remote {
    List<IpcArrivalDepartureScheduleAdherence> getArrivalsDeparturesForRoute(Date beginDate, Date endDate, String routeIdOrShortName, ServiceType serviceType, boolean timePointsOnly, String headsign) throws Exception;
    List<IpcArrivalDepartureScheduleAdherence> getArrivalsDeparturesForRoute(Date beginDate, Date endDate, String routeId, ServiceType serviceType, boolean timePointsOnly, String headsign, boolean readOnly) throws Exception;

    List<IpcStopWithDwellTime> getStopsWithAvgDwellTimes(Date beginDate, Date endDate, String routeIdOrShortName,
                                                         ServiceType serviceType, boolean timePointsOnly, String headsign,
                                                         boolean readOnly) throws Exception;
}