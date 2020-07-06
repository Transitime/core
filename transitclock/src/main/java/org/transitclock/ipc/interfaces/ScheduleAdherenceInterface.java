package org.transitclock.ipc.interfaces;

import org.transitclock.core.ServiceType;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.ipc.data.IpcArrivalDepartureScheduleAdherence;

import java.rmi.Remote;
import java.util.Date;
import java.util.List;

/**
 * Defines the RMI interface used for obtaining schedule adherence information.
 *
 * @author carabalb
 *
 */
public interface ScheduleAdherenceInterface extends Remote {
    List<IpcArrivalDepartureScheduleAdherence> getArrivalsDeparturesForRoute(Date beginDate, Date endDate, String routeIdOrShortName, ServiceType serviceType, boolean timePointsOnly) throws Exception;
    List<IpcArrivalDepartureScheduleAdherence> getArrivalsDeparturesForRoute(Date beginDate, Date endDate, String routeId, ServiceType serviceType, boolean timePointsOnly, boolean readOnly) throws Exception;
}