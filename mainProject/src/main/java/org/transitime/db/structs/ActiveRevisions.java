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

	// Not from database. Instead, specified which db to use
	@Transient
	private String projectId;
	
	@Column
	@Id
	private int configRev;
	
	@Column
	private int travelTimesRev;

	/********************** Member Functions **************************/

	/**
	 * Constructor made private so that have to use getActiveRevisions() to get
	 * object.
	 */
	private ActiveRevisions() {
	}
	
	/**
	 * So can set member variable projectId for when object is read from db.
	 * 
	 * @param projectId
	 */
	private void setProjectId(String projectId) {
		this.projectId = projectId;
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
		activeRevisions.setProjectId(projectId);
		
		// Return the object
		return activeRevisions;
	}
	
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
	
	/*
	 * Just for debugging
	 */
	public static void main(String[] args) {
		ActiveRevisions activeRevisions = get("mbta");
		activeRevisions.setTravelTimesRev(1);
	}
}
