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

package org.transitime.db.structs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.DynamicUpdate;
import org.transitime.db.hibernate.HibernateUtils;

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

	// For the configuration data for routes, stops, schedule, etc.
	@Column
	@Id
	private int configRev;
	
	// For the travel time configuration data. Updated independently of
	// configRev.
	@Column
	private int travelTimesRev;

	// Not from database. Instead, specified which db to use. Useful for
	// when calling setConfigRev(int) or setTravelTimesRev(int) because
	// can then automatically open a new session using the project ID
	// and then store the object.
	@Transient
	private String projectId;
	
	/********************** Member Functions **************************/

	/**
	 * Constructor made private so that have to use getActiveRevisions() to get
	 * object. Sets the revisions to -1 so that when incremented they will be 0.
	 */
	private ActiveRevisions() {
		configRev = -1;
		travelTimesRev = -1;
	}
	
	/**
	 * Reads revisions from database.
	 * 
	 * @param projectId
	 * @return
	 * @throws HibernateException
	 */
	public static ActiveRevisions get(String projectId) 
			throws HibernateException {	
		// There should only be a single object so don't need a WHERE clause
		String hql = "FROM ActiveRevisions";
		Session session = HibernateUtils.getSession(projectId);
		Query query = session.createQuery(hql);
		ActiveRevisions activeRevisions = null;
		try {
			activeRevisions = 
					(ActiveRevisions) query.uniqueResult();
		} catch (Exception e) {
			System.err.println("Exception when reading ActiveRevisions object " +
					"from database so will create it");
		} finally {
			// If couldn't read from db use default values and write the
			// object to the database.
			if (activeRevisions == null) {
				activeRevisions = new ActiveRevisions();
				Transaction transaction = session.beginTransaction();
				session.persist(activeRevisions);
				transaction.commit();
			}

			// Always close the session
			session.close();
		}
		
		// Always make sure the project ID is set
		activeRevisions.projectId = projectId;
		
		// Return the object
		return activeRevisions;
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
	 * Creates new db session and writes the new value to the db.
	 * 
	 * @param travelTimeRev
	 */
	public void setTravelTimesRev(int travelTimeRev) {
		Session session = HibernateUtils.getSession(projectId);
		this.travelTimesRev = travelTimeRev;
		try {
			Transaction transaction = session.beginTransaction();
			session.saveOrUpdate(this);
			transaction.commit();
		} catch (Exception e) {
			throw e;
		} finally {
			session.close();
		}
	}
	
	/**
	 * Creates new db session and writes the new value to the db.
	 * 
	 * @param configRev
	 */
	public void setConfigRev(int configRev) {
		Session session = HibernateUtils.getSession(projectId);
		this.configRev = configRev;
		try {
			session.saveOrUpdate(this);
		} catch (Exception e) {
			throw e;
		} finally {
			session.close();
		}	
	}

	public int getConfigRev() {
		return configRev;
	}

	public int getTravelTimesRev() {
		return travelTimesRev;
	}
	
	@Override
	public String toString() {
		return "ActiveRevisions [" 
				+ "projectId=" + projectId 
				+ ", configRev=" + configRev 
				+ ", travelTimesRev=" + travelTimesRev 
				+ "]";
	}

	/*
	 * Just for debugging
	 */
	public static void main(String[] args) {
		ActiveRevisions activeRevisions = get("mbta");
		activeRevisions.setTravelTimesRev(1);
	}

}
