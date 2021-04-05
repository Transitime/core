package org.transitclock.core.dataCache;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.dataCache.scheduled.ScheduleBasedHistoricalAverageCache;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.ArrivalDeparture;

import java.util.Date;
import java.util.List;

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
    private List<ArrivalDeparture> results;

    public CacheTask(Date startDate, Date endDate, Type type, List<ArrivalDeparture> defaultInput) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.results = defaultInput;
    }

    @Override
    public String toString() {
        return type.name();
    }

    @Override
    public void run() throws Exception {
        Session session = null;
        try {
            session = HibernateUtils.getSession();
            if (this.results == null) {
                Criteria criteria = session.createCriteria(ArrivalDeparture.class);
                results = criteria.add(Restrictions.between("time", startDate, endDate)).list();
            }

            logger.info("Populating {} cache for period {} to {}", type, startDate, endDate);
            switch (type) {
                case TripDataHistoryCacheFactory:
                    TripDataHistoryCacheFactory.getInstance().populateCacheFromDb(results);
                    break;
                case StopArrivalDepartureCacheFactory:
                    StopArrivalDepartureCacheFactory.getInstance().populateCacheFromDb(results);
                    break;
                case FrequencyBasedHistoricalAverageCache:
                    FrequencyBasedHistoricalAverageCache.getInstance().populateCacheFromDb(results);
                    break;
                case ScheduleBasedHistoricalAverageCache:
                    ScheduleBasedHistoricalAverageCache.getInstance().populateCacheFromDb(results);
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
