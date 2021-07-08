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
package org.transitclock.reports;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.hibernate.type.DoubleType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.PredictionAccuracy;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * To find route performance information.
 * For now, route performance is the percentage of predictions for a route which are ontime.
 *
 * @author Simon Jacobs
 *
 */
public class RoutePerformanceQuery {
  private Session session;
  
  private static final Logger logger = LoggerFactory
      .getLogger(RoutePerformanceQuery.class);
  
  public static final String PREDICTION_TYPE_AFFECTED = "AffectedByWaitStop";
  public static final String PREDICTION_TYPE_NOT_AFFECTED = "NotAffectedByWaitStop";
  
  private static final String TRANSITIME_PREDICTION_SOURCE = "TransitClock";
  
  public List<Object[]> query(String agencyId, Date startDate, int numDays, double allowableEarlyMin, double allowableLateMin, String predictionType, String predictionSource) {
    
    int msecLo = (int) (allowableEarlyMin * 60 * 1000 * -1);
    int msecHi = (int) (allowableLateMin * 60 * 1000);
    Calendar c = Calendar.getInstance();
    c.setTime(startDate);
    c.add(Calendar.DAY_OF_YEAR,numDays);
    Date endDate = c.getTime();
    // Project to: # of predictions in which route is on time / # of predictions
    // for route. This cannot be done with pure Criteria API. This could be
    // moved to a separate class or XML file.
    String sqlProjection = "avg(predictionAccuracyMsecs)  AS avgAccuracy";

    try {
      session = HibernateUtils.getSession(agencyId);
            
      Projection proj = Projections.projectionList()
          .add(Projections.groupProperty("routeId"), "routeId")
          .add(Projections.sqlProjection(sqlProjection,
              new String[] { "avgAccuracy" }, 
              new Type[] { DoubleType.INSTANCE }), "performance");
          
      Criteria criteria = session.createCriteria(PredictionAccuracy.class)
        .setProjection(proj)
        .add(Restrictions.ge("arrivalDepartureTime", startDate))
        .add(Restrictions.le("arrivalDepartureTime", endDate))
      	.add(Restrictions.ge("predictionAccuracyMsecs", msecLo))
    	.add(Restrictions.le("predictionAccuracyMsecs", msecHi));
      
      
      if (predictionType == PREDICTION_TYPE_AFFECTED)
          criteria.add(Restrictions.eq("affectedByWaitStop", true));
      else if (predictionType == PREDICTION_TYPE_NOT_AFFECTED)
          criteria.add(Restrictions.eq("affectedByWaitStop", false));
      
      if (predictionSource != null && !StringUtils.isEmpty(predictionSource)) {
    	  if (predictionSource.equals(TRANSITIME_PREDICTION_SOURCE))
    		  criteria.add(Restrictions.eq("predictionSource", TRANSITIME_PREDICTION_SOURCE));
    	  else
    		  criteria.add(Restrictions.ne("predictionSource", TRANSITIME_PREDICTION_SOURCE));
      }
      
      criteria.addOrder(Order.desc("performance"));
      
      criteria.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
          
      @SuppressWarnings("unchecked")
      List<Object[]> results = criteria.list();
      

      return results;
    }
    catch(HibernateException e) {
      logger.error(e.toString());
      return null;
    }
    finally {
      session.close();
    }
  }

}
