package org.transitclock.core.reporting;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.transitclock.applications.Core;
import org.transitclock.core.ServiceType;
import org.transitclock.db.query.ArrivalDepartureQuery;
import org.transitclock.db.query.RunTimeForRouteQuery;
import org.transitclock.db.query.TripQuery;
import org.transitclock.db.structs.*;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimesForPattern;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimesForPatterns;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimesForTimeBand;
import org.transitclock.reporting.dao.RunTimeRoutesDao;
import org.transitclock.reporting.service.RunTimeService;
import org.transitclock.reporting.service.runTime.prescriptive.PrescriptiveRunTimeService;
import org.transitclock.reporting.service.runTime.prescriptive.PrescriptiveTimebandService;
import org.transitclock.reporting.service.runTime.prescriptive.helper.DatedGtfsService;
import org.transitclock.reporting.service.runTime.prescriptive.model.DatedGtfs;
import org.transitclock.reporting.service.runTime.prescriptive.timebands.PrescriptiveRuntimeClusteringService;
import org.transitclock.utils.Time;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;


public class PrescriptiveDatedGTFSTest extends AbstractPrescriptiveRunTimesTests{

    private MockedStatic<Core> singletonCore;

    @Test
    public void prescriptiveDatedGtfs() throws Exception {
        String servicePeriod = "169 - 2022-04-18 - 2022-06-12";

        List<FeedInfo> feedInfos = getFeedInfo(servicePeriod);

        // Mock Core
        Time time = new Time(TimeZone.getDefault().getDisplayName());
        Core mockCore = mock(Core.class, Mockito.RETURNS_DEEP_STUBS);

        // Setup static mocks
        singletonCore = mockStatic(Core.class);
        singletonCore.when(() -> Core.getInstance()).thenReturn(mockCore);

        when(mockCore.getTime()).thenReturn(time);
        when(mockCore.getDbConfig().getFeedInfos()).thenReturn(feedInfos);

        List<DatedGtfs> datedGtfsList = DatedGtfsService.getDatedGtfs();

        assertEquals(3, datedGtfsList.size());

        for(DatedGtfs datedGtfs : datedGtfsList){
            System.out.println(datedGtfs.toString());
        }

        singletonCore.close();
    }


}
