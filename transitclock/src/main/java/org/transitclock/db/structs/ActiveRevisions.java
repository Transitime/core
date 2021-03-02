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

package org.transitclock.db.structs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.hibernate.HibernateUtils;

/**
 * For keeping track of current revisions. This table should only have a single
 * row, one that specified the configRev and the travelTimesRev currently being
 * used.
 *
 * @author SkiBu Smith
 *
 */
@Entity @DynamicUpdate
public class ActiveRevisions {

	// Need a generated ID since Hibernate required some type
	// of ID. Both configRev and travelTimesRev
	// might get updated and with Hibernate you can't read in an
	// object, modify an ID and then write it out again. Therefore
	// configRev and travelTimesRev can't be an ID. This means
	// that need a separate ID. Yes, somewhat peculiar.
	@Id 
	@Column 
	@GeneratedValue 
	private Integer id;

	// For the configuration data for routes, stops, schedule, etc.
	@Column
	private int configRev;
	
	// For the travel time configuration data. Updated independently of
	// configRev.
	@Column
	private int travelTimesRev;


	// For the traffic configuration data.  Updated independently of
	// configRev and travelTimesRev.
	@Column
	private Integer trafficRev;

	private static final Logger logger =
			LoggerFactory.getLogger(ActiveRevisions.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor. Sets the revisions to default values of -1.
	 */
	public ActiveRevisions() {
		configRev = -1;
		travelTimesRev = -1;
		trafficRev = null;
	}
	
	/**
	 * Gets the ActiveRevisions object using the passed in database session.
	 * 
	 * @param session
	 * @return the ActiveRevisions
	 * @throws HibernateException
	 */
	public static ActiveRevisions get(Session session) 
		throws HibernateException {
		// There should only be a single object so don't need a WHERE clause
		String hql = "FROM ActiveRevisions";
		Query query = session.createQuery(hql);
		ActiveRevisions activeRevisions = null;
		try {
			activeRevisions = (ActiveRevisions) query.uniqueResult();
		} catch (Exception e) {
			System.err.println("Exception when reading ActiveRevisions object " +
					"from database so will create it");
		} finally {
			// If couldn't read from db use default values and write the
			// object to the database.
			if (activeRevisions == null) {
				activeRevisions = new ActiveRevisions();
				session.persist(activeRevisions);
			}
		}
		
		// Return the object
		return activeRevisions;		
	}
	
	/**
	 * Reads revisions from database.
	 * 
	 * @param agencyId
	 * @return
	 * @throws HibernateException
	 */
	public static ActiveRevisions get(String agencyId) 
			throws HibernateException {
		Session session = null;
		try {
			// Get from db
			session = HibernateUtils.getSession(agencyId);
			ActiveRevisions activeRevisions = get(session);
			
			// Return the object
			return activeRevisions;
		} catch (HibernateException e) {
			logger.error("Exception in ActiveRevisions.get(). {}", 
					e.getMessage(), e);
		} finally {
			// Always make sure session gets closed
			if (session != null)
				session.close();
		}
		
		return null;
	}
	
	/**
	 * Updates configRev member and calls saveOrUpdate(this) on the session.
	 * Useful for when want to update the value but don't want to commit it
	 * until all other data is also written out successfully.
	 * 
	 * @param session
	 * @param configRev
	 */
	public void setConfigRev(Session session, int configRev) {
		this.configRev = configRev;
		session.saveOrUpdate(this);
	}
	
	/**
	 * Updates travelTimeRev member and calls saveOrUpdate(this) on the session.
	 * Useful for when want to update the value but don't want to commit it
	 * until all other data is also written out successfully.
	 * 
	 * @param session
	 * @param travelTimeRev
	 */
	public void setTravelTimesRev(Session session, int travelTimeRev) {
		this.travelTimesRev = travelTimeRev;
		session.saveOrUpdate(this);
	}
	
	/**
	 * Sets the travel time rev. Doesn't write it to db though. To write to db
	 * should flush the session that the object was read in by.
	 * 
	 * @param travelTimeRev
	 */
	public void setTravelTimesRev(int travelTimeRev) {
		this.travelTimesRev = travelTimeRev;
	}
	
	/**
	 * Sets the config rev. Doesn't write it to db though. To write to db
	 * should flush the session that the object was read in by.
	 * 
	 * @param configRev
	 */
	public void setConfigRev(int configRev) {
		this.configRev = configRev;
	}

	public int getConfigRev() {
		return configRev;
	}

	public Integer getTrafficRev() { return trafficRev; }

	public void setTrafficRev(Integer trafficRev) {
		this.trafficRev = trafficRev;
	}

	public int getTravelTimesRev() {
		return travelTimesRev;
	}
	
	/**
	 * @return True if both the configRev and travelTimesRev are both valid.
	 */
	public boolean isValid() {
		return configRev >= 0 && travelTimesRev >= 0;
	}

	@Override
	public String toString() {
		return "ActiveRevisions [" 
				+ "configRev=" + configRev 
				+ ", travelTimesRev=" + travelTimesRev
				+ ", trafficRev=" + trafficRev
				+ "]";
	}

}
