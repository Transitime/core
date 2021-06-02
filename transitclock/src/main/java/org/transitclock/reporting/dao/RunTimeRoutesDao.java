package org.transitclock.reporting.dao;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.ServiceType;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.query.RunTimeForRouteQuery;
import org.transitclock.db.structs.RunTimesForRoutes;
import org.transitclock.utils.IntervalTimer;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunTimeRoutesDao {

    private static final Logger logger =
            LoggerFactory.getLogger(RunTimeRoutesDao.class);

    /**
     *
     * @param rtQuery
     * @return
     * @throws Exception
     */
    public List<RunTimesForRoutes> getRunTimesForRoutes(RunTimeForRouteQuery rtQuery) throws Exception {
        IntervalTimer timer = new IntervalTimer();

        // Get the database session. This is supposed to be pretty light weight
        Session session = HibernateUtils.getSession(rtQuery.isReadOnly());

        Map<String, Object> parameterNameAndValues = new HashMap<>();

        String hql = "SELECT " +
                "distinct rt " +
                "FROM " +
                "RunTimesForRoutes rt " +
                getRunTimesForStopsJoin(rtQuery) +
                "WHERE " +
                getTimeRange(rtQuery, parameterNameAndValues) +
                getServiceTypeWhere(rtQuery, parameterNameAndValues) +
                getHeadSignWhere(rtQuery, parameterNameAndValues) +
                getDirectionIdWhere(rtQuery, parameterNameAndValues) +
                getTripPatternWhere(rtQuery, parameterNameAndValues) +
                getTripIdWhere(rtQuery, parameterNameAndValues) +
                "ORDER BY rt.routeShortName, rt.startTime DESC";

        try {
            Query query = session.createQuery(hql);
            for (Map.Entry<String, Object> e : parameterNameAndValues.entrySet()) {
                query.setParameter(e.getKey(), e.getValue());
            }
            List<RunTimesForRoutes> results = query.list();

            logger.debug("Getting runtimes for routes from database took {} msec",
                    timer.elapsedMsec());

            return results;

        } catch (HibernateException e) {
            Core.getLogger().error("Unable to retrieve runtimes for routes", e);
            return Collections.EMPTY_LIST;
        } finally {
            // Clean things up. Not sure if this absolutely needed nor if
            // it might actually be detrimental and slow things down.
            session.close();
        }
    }

    private String getRunTimesForStopsJoin(RunTimeForRouteQuery rtQuery) {
        if(rtQuery.includeRunTimesForStops()){
            return "JOIN FETCH rt.runTimesForStops rts ";
        }
        return "";
    }

    public static String getTimeRange(RunTimeForRouteQuery rtQuery, Map<String, Object> parameterNameAndValues) {
        String hql = "";

        Integer beginTime = rtQuery.getBeginTime();
        Integer endTime = rtQuery.getEndTime();
        LocalDateTime beginDate;
        LocalDateTime endDate = null;

        // Set Default Values For Dates
        if(rtQuery.getBeginDate() == null) {
            beginDate = LocalDate.now().atStartOfDay();
        }
        else {
            beginDate = rtQuery.getBeginDate().atStartOfDay();
        }

        if(rtQuery.getEndDate() != null) {
            endDate =  rtQuery.getEndDate().atTime(LocalTime.MAX).plusHours(3);
        }

        // Set Query and Params for Dates
        parameterNameAndValues.put("beginDate", Timestamp.valueOf(beginDate));

        if (endDate != null && endDate.isAfter(beginDate)) {
            hql += " rt.startTime BETWEEN :beginDate AND :endDate ";
            parameterNameAndValues.put("endDate", Timestamp.valueOf(endDate));
        } else if (endDate != null && endDate.isEqual(beginDate)) {
            endDate = endDate.plusDays(1);
            hql += " rt.startTime BETWEEN :beginDate AND :endDate ";
            parameterNameAndValues.put("endDate", Timestamp.valueOf(endDate));
        } else if (endDate == null || endDate.isBefore(beginDate)) {
            hql += " rt.startTime >= :beginDate ";
        }

        // Set Default Values for Time
        beginTime = beginTime != null ? beginTime : 0;
        endTime = endTime != null ? endTime : Integer.MAX_VALUE;

        parameterNameAndValues.put("beginTime", beginTime);
        parameterNameAndValues.put("endTime", endTime);

        // Set Query and Params for Times
        hql += "AND rt.scheduledStartTime BETWEEN :beginTime AND :endTime ";

        return hql;
    }

    private static String getServiceTypeWhere(RunTimeForRouteQuery rtQuery, Map<String, Object> parameterNameAndValues){
        String hql = "";
        ServiceType serviceType = rtQuery.getServiceType();
        if(serviceType != null) {
            hql += " AND rt.serviceType = :serviceType ";
            parameterNameAndValues.put("serviceType", serviceType);
        }
        return hql;
    }

    private String getHeadSignWhere(RunTimeForRouteQuery rtQuery, Map<String, Object> parameterNameAndValues) {
        String hql = "";
        String headsign = rtQuery.getHeadsign();
        if(StringUtils.isNotBlank(headsign)) {
            hql += " AND rt.headsign = :headsign ";
            parameterNameAndValues.put("headsign", headsign);
        }
        return hql;
    }

    private String getDirectionIdWhere(RunTimeForRouteQuery rtQuery, Map<String, Object> parameterNameAndValues) {
        String hql = "";
        String directionId = rtQuery.getDirectionId();
        if(StringUtils.isNotBlank(directionId)) {
            hql += " AND rt.directionId = :directionId ";
            parameterNameAndValues.put("directionId", directionId);
        }
        return hql;
    }

    private String getTripPatternWhere(RunTimeForRouteQuery rtQuery, Map<String, Object> parameterNameAndValues) {
        String hql = "";
        String tripPatternId = rtQuery.getTripPatternId();
        if(StringUtils.isNotBlank(tripPatternId)) {
            hql += " AND rt.tripPatternId like :tripPatternId ";
            parameterNameAndValues.put("tripPatternId", getTripPatternWildCard(tripPatternId));
        }
        return hql;
    }

    private String getTripPatternWildCard(String tripPatternId){
        String splitTripPatternId [] = tripPatternId.split("_");
        if(splitTripPatternId.length == 6){
            return "%" + splitTripPatternId[2] + "_" + splitTripPatternId[3] + "_" + splitTripPatternId[4] + "%";
        }
        return tripPatternId;
    }

    private String getTripIdWhere(RunTimeForRouteQuery rtQuery, Map<String, Object> parameterNameAndValues) {
        String hql = "";
        String tripId = rtQuery.getTripId();
        if(StringUtils.isNotBlank(tripId)) {
            hql += " AND rt.tripId = :tripId ";
            parameterNameAndValues.put("tripId", tripId);
        }
        return hql;
    }
}
