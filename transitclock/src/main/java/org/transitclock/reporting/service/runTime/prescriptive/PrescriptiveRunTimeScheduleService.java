package org.transitclock.reporting.service.runTime.prescriptive;

import org.apache.commons.collections.map.HashedMap;
import org.transitclock.applications.Core;
import org.transitclock.core.ServiceType;
import org.transitclock.core.ServiceTypeUtil;
import org.transitclock.db.structs.*;
import org.transitclock.db.structs.Calendar;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.ipc.data.IpcPrescriptiveRunTime;
import org.transitclock.ipc.data.IpcStopTime;
import org.transitclock.reporting.service.runTime.PrescriptiveAdjustmentResult;
import org.transitclock.reporting.service.runTime.prescriptive.model.PrescriptiveRunTimeState;

import javax.inject.Inject;
import java.time.LocalTime;
import java.util.*;

public class PrescriptiveRunTimeScheduleService {

    @Inject
    private PrescriptiveRunTimeService prescriptiveRunTimeService;

    /**
     * Get Prescriptive RunTime Information for Run Times
     * @param beginTime
     * @param endTime
     * @param routeIdOrShortName
     * @param headsign
     * @param directionId
     * @param tripPatternId
     * @param readOnly
     * @return
     * @throws Exception
     */
    public List<IpcStopTime> getPrescriptiveRunTimesSchedule(LocalTime beginTime,
                                                             LocalTime endTime,
                                                             String routeIdOrShortName,
                                                             String headsign,
                                                             String directionId,
                                                             String tripPatternId,
                                                             ServiceType serviceType,
                                                             boolean readOnly) throws Exception {


        return null;
    }

}
