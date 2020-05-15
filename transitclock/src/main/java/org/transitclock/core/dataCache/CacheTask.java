package org.transitclock.core.dataCache;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.dataCache.scheduled.ScheduleBasedHistoricalAverageCache;
import org.transitclock.db.hibernate.HibernateUtils;

import java.util.Date;

/**
 * A task populating the cache on startup.  Designed to be
 * run in parallel.
 */
public class CacheTask implements ParallelTask {

    private static final Logger logger =
            LoggerFactory.getLogger(CacheTask.class);

    /**
     * type of cache we are dealing with
     */
    public enum Type {
        TripDataHistoryCacheFactory,
        StopArrivalDepartureCacheFactory,
        FrequencyBasedHistoricalAverageCache,
        ScheduleBasedHistoricalAverageCache
    }

    private Date startDate;
    private Date endDate;
    private Type type;

    public CacheTask(Date startDate, Date endDate, Type type) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
    }

    @Override
    public void run() throws Exception {
        Session session = null;
        try {
            session = HibernateUtils.getSession();
            logger.info("Populating {} cache for period {} to {}", type, startDate, endDate);
            switch (type) {
                case TripDataHistoryCacheFactory:
                    TripDataHistoryCacheFactory.getInstance().populateCacheFromDb(session, startDate, endDate);
                    break;
                case StopArrivalDepartureCacheFactory:
                    StopArrivalDepartureCacheFactory.getInstance().populateCacheFromDb(session, startDate, endDate);
                    break;
                case FrequencyBasedHistoricalAverageCache:
                    FrequencyBasedHistoricalAverageCache.getInstance().populateCacheFromDb(session, startDate, endDate);
                    break;
                case ScheduleBasedHistoricalAverageCache:
                    ScheduleBasedHistoricalAverageCache.getInstance().populateCacheFromDb(session, startDate, endDate);
                    break;
                default:
                    throw new IllegalArgumentException("unknown type=" + type);
            }

        } finally {
            logger.info("Finished Populating {} cache for period {} to {}", type, startDate, endDate);
            if (session != null) {
                // this session is in a separate thread and needs to be reclaimed
                // as it counts against the connection pool
                session.close();
            }
        }
    }

}
