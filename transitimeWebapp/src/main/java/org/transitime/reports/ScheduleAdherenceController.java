/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitime.reports;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.DoubleType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.hsqldb.lib.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.db.structs.ArrivalDeparture;

public class ScheduleAdherenceController {
	 
	private static final Logger logger = LoggerFactory
			.getLogger(ScheduleAdherenceController.class);	
	// TODO: Combine routeScheduleAdherence and stopScheduleAdherence
	// - Make this a REST endpoint
	
	public static List<Object> routeScheduleAdherence(Date startDate,
			Date endDate,
			String startTime,
			String endTime,
			List<String> routeIds,
			boolean byRoute) {

		endDate = new Date(endDate.getTime() + TimeUnit.DAYS.toMillis(1));
		
		String sqlProjection = "avg(scheduledTime - time) AS scheduleAdherence";

		ProjectionList proj = Projections.projectionList();
        
		proj.add(Projections.rowCount(), "count");
        		
		if (byRoute)
			proj.add(Projections.groupProperty("routeId"), "routeId");
		else
			proj.add(Projections.property("routeId"), "routeId")
				.add(Projections.groupProperty("tripId"), "tripId");
		
		proj.add(Projections.sqlProjection(sqlProjection, new String[] { "scheduleAdherence" },
						new Type[] { DoubleType.INSTANCE }), "scheduleAdherence");

		DetachedCriteria criteria = DetachedCriteria.forClass(ArrivalDeparture.class)
				.add(Restrictions.between("time", startDate, endDate))
				.add(Restrictions.isNotNull("scheduledTime"));
	
		
		String sql = "time({alias}.time) between ? and ?";
		String[] values = { startTime, endTime };
		Type[] types = { StringType.INSTANCE, StringType.INSTANCE };
		criteria.add(Restrictions.sqlRestriction(sql, values, types));
		
		
		criteria.setProjection(proj).setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);

		if (routeIds != null && routeIds.size() > 0)
			criteria.add(Restrictions.in("routeId", routeIds));

		return dbify(criteria);

	}
	
	public static List<Object> stopScheduleAdherence(Date startDate,
			Date endDate,
			String startTime,
			String endTime,
			List<String> stopIds,
			boolean byStop) {

		endDate = new Date(endDate.getTime() + TimeUnit.DAYS.toMillis(1));
		
		String sqlProjection = "(scheduledTime - time) AS scheduleAdherence";

		ProjectionList proj = Projections.projectionList();
  
        		
		if (byStop)
			proj.add(Projections.groupProperty("stopId"), "stopId")
				.add(Projections.rowCount(), "count");
		else
			proj.add(Projections.property("routeId"), "routeId")
				.add(Projections.property("stopId"), "stopId")
				.add(Projections.property("tripId"), "tripId");
		
		proj.add(Projections.sqlProjection(sqlProjection, new String[] { "scheduleAdherence" },
						new Type[] { DoubleType.INSTANCE }), "scheduleAdherence");

		DetachedCriteria criteria = DetachedCriteria.forClass(ArrivalDeparture.class)
				.add(Restrictions.between("time", startDate, endDate))
				.add(Restrictions.isNotNull("scheduledTime"));
	
		
		String sql = "time({alias}.time) between ? and ?";
		String[] values = { startTime, endTime };
		Type[] types = { StringType.INSTANCE, StringType.INSTANCE };
		criteria.add(Restrictions.sqlRestriction(sql, values, types));
		
		
		criteria.setProjection(proj).setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);

		if (stopIds != null && stopIds.size() > 0)
			criteria.add(Restrictions.in("stopId", stopIds));

		return dbify(criteria);

	}
	
	public static List<Integer> routeScheduleAdherenceSummary(Date startDate,
			Date endDate,
			String startTime,
			String endTime,
			Double earlyLimit,
			Double lateLimit,
			List<String> routeIds) {

		endDate = endOfDay(endDate);
		String sqlProjection = "(scheduledTime - time) AS scheduleAdherence";

		ProjectionList proj = Projections.projectionList();
		proj.add(Projections.sqlProjection(sqlProjection, new String[] { "scheduleAdherence" },
				new Type[] { DoubleType.INSTANCE }), "scheduleAdherence");

		DetachedCriteria criteria = DetachedCriteria.forClass(ArrivalDeparture.class)
				.add(Restrictions.between("time", startDate, endDate))
				.add(Restrictions.isNotNull("scheduledTime"));
		if (routeIds != null && !routeIds.isEmpty()) {
			for (String routeId : routeIds) {
				if (StringUtils.isNotBlank(routeId)) {
					criteria.add(Restrictions.eq("routeId", routeId));
				}
			}
		}
		String sql = "time({alias}.time) between ? and ?";
		String[] values = { startTime, endTime };
		Type[] types = { StringType.INSTANCE, StringType.INSTANCE };
		criteria.add(Restrictions.sqlRestriction(sql, values, types));
		
		logger.info("sql=" + sql);
		
		criteria.setProjection(proj).setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
				
		int count = 0;
		int early = 0;
		int late = 0;
		int ontime = 0;
		List<Object> results = dbify(criteria);
		for (Object o : results) {
			count++;
			java.util.HashMap hm = (java.util.HashMap)o;
			Double d = (Double)hm.get("scheduleAdherence");
			if (d > lateLimit) {
				late++;
			} else if (d < earlyLimit) {
				early++;
			} else {
				ontime++;
			}
		}
		logger.info("query complete -- earlyLimit={}, lateLimit={}, early={}, ontime={}, late={}, count={}",
				earlyLimit, lateLimit, early, ontime, late, count);
		double earlyPercent = (1.0 - (double)(count - early)/count) * 100;
		double onTimePercent = (1.0 - (double)(count - ontime)/count) * 100;
		double latePercent = (1.0 - (double)(count - late)/count) * 100;
		logger.info("count={} earlyPercent={} onTimePercent={} latePercent={}",
				count, earlyPercent, onTimePercent, latePercent);
		Integer[] summary = new Integer[] {count, (int) earlyPercent, (int) onTimePercent, (int) latePercent};
		return Arrays.asList(summary);
	}
	
	 private static Date endOfDay(Date endDate) {
		 Calendar c = Calendar.getInstance();
		 c.setTime(endDate);
		 c.set(Calendar.HOUR, 23);
		 c.set(Calendar.MINUTE, 59);
		 c.set(Calendar.SECOND, 59);
		 return c.getTime();
	}

	private static List<Object> dbify(DetachedCriteria criteria) {
		 Session session = HibernateUtils.getSession();
		 try {
			 return criteria.getExecutableCriteria(session).list();
		 }
		 finally {
			 session.close();
		 }
	 }
}
