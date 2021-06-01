package org.transitclock.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.transitclock.reporting.dao.RunTimeRoutesDao;
import org.transitclock.reporting.service.OnTimePerformanceService;
import org.transitclock.reporting.service.RunTimeService;
import org.transitclock.reporting.service.SpeedMapService;
import org.transitclock.reporting.service.runTime.*;

public class ReportingModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(RunTimeRoutesDao.class).in(Scopes.SINGLETON);
        bind(RunTimeService.class).in(Scopes.SINGLETON);
        bind(RouteRunTimesService.class).in(Scopes.SINGLETON);
        bind(TripRunTimesService.class).in(Scopes.SINGLETON);
        bind(StopRunTimesService.class).in(Scopes.SINGLETON);
        bind(PrescriptiveRunTimeService.class).in(Scopes.SINGLETON);
        bind(OnTimePerformanceService.class).in(Scopes.SINGLETON);
        bind(SpeedMapService.class).in(Scopes.SINGLETON);
    }
}
