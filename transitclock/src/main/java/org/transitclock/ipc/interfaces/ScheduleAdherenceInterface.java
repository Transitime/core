package org.transitclock.ipc.interfaces;

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
    List<IpcArrivalDeparture> getArrivalsDeparturesForRoute(Date beginDate, Date endDate, String routeId, boolean readOnly) throws Exception;
    List<IpcArrivalDeparture> getArrivalsDeparturesForRoute(Date beginDate, Date endDate, String routeId) throws Exception;
}