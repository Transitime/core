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

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.hibernate.HibernateUtils;

/**
 * For testing ability to read and write to and from the db.
 * Doesn't actually store any useful information.
 *
 * @author SkiBu Smith
 *
 */
@Entity @DynamicUpdate
public class DbTest {

	@Id
	@Column
	private Integer id;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(DbTest.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor. Sets the revisions to default values of -1.
	 */
	public DbTest() {
		id = -1;
	}

	/**
	 * Reads all DbTest objects from db
	 * 
	 * @param agencyId
	 * @return List of all DbTest objects in database
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<DbTest> readAll(String agencyId) 
			throws HibernateException {
		Session session = HibernateUtils.getSession(agencyId);
		if (session == null) {
			logger.error("Could not get database session for agencyId={}",
					agencyId);
			return null;
		}
		
		String hql = "FROM DbTest";
		Query query = session.createQuery(hql);
		try {
			List<DbTest> dbTests = query.list();
			return dbTests;
		} catch (HibernateException e) {
			logger.error("Could not get DbTest object. {}. {}", 
					e.getCause().getMessage(), e.getMessage());
			throw e;
		} finally {
			session.close();
		}
	}
	
	/**
	 * Writes a DbTest object to the specified database.
	 * 
	 * @param agencyId
	 * @param id
	 * @return True if successfully wrote the object
	 * @throws HibernateException
	 */
	public static boolean write(String agencyId, int id) 
		throws HibernateException {
		Session session = HibernateUtils.getSession(agencyId);
		if (session == null) {
			logger.error("Could not get database session for agencyId={}",
					agencyId);
			return false;
		}
		
		// Create object to be stored
		DbTest dbTest = new DbTest();
		dbTest.id = id;
		
		// Store the object
		Transaction tx = null;
		try {
			// Put db access into a transaction because modifying data
			tx = session.beginTransaction();

			session.save(dbTest);
			
			// Make sure that everything actually written out to db
			tx.commit();

			// Stored object successfully so return true
			return true;
		} catch (HibernateException e) {
			// In case there was a problem
			if (tx != null)
				tx.rollback();

			logger.error("Could not write DbTest object. {}. {}", 
					e.getCause().getMessage(), e.getMessage());
			throw e;
		} finally {
			session.close();
		}
	}
	
	/**
	 * Deletes all the DbTest objects from the database.
	 * 
	 * @param agencyId
	 * @return Number of DbTest objects deleted
	 * @throws HibernateException
	 */
	public static int deleteAll(String agencyId) 
			throws HibernateException {
		Session session = HibernateUtils.getSession(agencyId);
		if (session == null) {
			logger.error("Could not get database session for agencyId={}",
					agencyId);
			return 0;
		}

		// Note that hql uses class name, not the table name
		String hql = "DELETE DbTest";
		try {
			// Put db access into a transaction because modifying data
			Transaction tx = session.beginTransaction();

			int numUpdates = session.createQuery(hql).executeUpdate();
			
			// Make sure that everything actually written out to db
			tx.commit();

			return numUpdates;
		} catch (HibernateException e) {
			logger.error("Could not delete DbTest objects. {}. {}", 
					e.getCause().getMessage(),
					e.getMessage());
			throw e;
		} finally {
			session.close();
		}
	}
	
	@Override
	public String toString() {
		return "DbTest [id=" + id + "]";
	}

}
