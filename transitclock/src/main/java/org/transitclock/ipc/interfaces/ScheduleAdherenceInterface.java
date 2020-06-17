package org.transitclock.ipc.interfaces;

import org.transitclock.core.ServiceType;
import org.transitclock.ipc.data.IpcArrivalDeparture;

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
    List<IpcArrivalDeparture> getArrivalsDeparturesForRoute(Date beginDate, Date endDate, String routeIdOrShortName, ServiceType serviceType) throws Exception;
    List<IpcArrivalDeparture> getArrivalsDeparturesForRoute(Date beginDate, Date endDate, String routeId, ServiceType serviceType, boolean readOnly) throws Exception;
}