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

package org.transitime.db.webstructs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.configData.CoreConfig;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.utils.IntervalTimer;

/**
 * For keeping track of agency data for a website. Contains info such as the
 * host of the agency so that can use RMI to connect to it.
 *
 * @author SkiBu Smith
 *
 */
@Entity
@DynamicUpdate
@Table(name = "WebAgencies")
public class WebAgency {

	@Id
	@Column(length = HibernateUtils.DEFAULT_ID_SIZE)
	private final String agencyId;

	@Column(length = 120)
	private final String hostName;

	// Cache
	static private Map<String, WebAgency> map;

	private static final Logger logger = LoggerFactory
			.getLogger(WebAgency.class);

	/********************** Member Functions **************************/

	/**
	 * @param agencyId
	 * @param hostName
	 */
	public WebAgency(String agencyId, String hostName) {
		this.agencyId = agencyId;
		this.hostName = hostName;
	}

	/**
	 * Needed because Hibernate requires no-arg constructor for reading in data
	 */
	@SuppressWarnings("unused")
	private WebAgency() {
		this.agencyId = null;
		this.hostName = null;
	}

	/**
	 * Stores this WebAgency object in the specified db.
	 * 
	 * @param dbName
	 */
	public void store(String dbName) {
		Session session = HibernateUtils.getSession(dbName);
		try {
			Transaction transaction = session.beginTransaction();
			session.save(this);
			transaction.commit();
		} catch (Exception e) {
			throw e;
		} finally {
			// Make sure that the session always gets closed, even if
			// exception occurs
			session.close();
		}
	}

	/**
	 * Specifies name of database to use. Currently using the command line
	 * option transitime.core.agencyId .
	 * 
	 * @return
	 */
	static private String getDbName() {
		return CoreConfig.getDbName();
	}

	/**
	 * Reads in WebAgency objects from the database and returns them as a map
	 * keyed on agencyId.
	 * 
	 * @return
	 * @throws HibernateException
	 */
	static private Map<String, WebAgency> getMapFromDb()
			throws HibernateException {
		String dbName = getDbName();
		logger.info("Reading WebAgencies data from database \"{}\"...", dbName);
		IntervalTimer timer = new IntervalTimer();

		Session session = HibernateUtils.getSession(dbName);
		try {
			String hql = "FROM WebAgency";
			Query query = session.createQuery(hql);
			@SuppressWarnings("unchecked")
			List<WebAgency> list = query.list();
			Map<String, WebAgency> map = new HashMap<String, WebAgency>();
			for (WebAgency webAgency : list) {
				map.put(webAgency.getAgencyId(), webAgency);
			}

			logger.info("Done reading WebAgencies from database. Took {} msec",
					timer.elapsedMsec());
			return map;
		} catch (Exception e) {
			throw e;
		} finally {
			// Make sure that the session always gets closed, even if
			// exception occurs
			session.close();
		}
	}

	/**
	 * Gets specified WebAgency from the cache. Updates the cache if necessary.
	 * 
	 * @param agencyId
	 * @return The specified WebAgency, or null if it doesn't exist.
	 */
	static public WebAgency getCachedWebAgency(String agencyId) {
		// If haven't read in web agencies yet, do so now
		if (map == null)
			map = getMapFromDb();

		// Get the web agency from the cache
		WebAgency webAgency = map.get(agencyId);

		// If web agency was not in cache update the cache and try again
		if (webAgency == null) {
			logger.error("Did not find agencyId={} in WebAgencies table for "
					+ "database {}. Will reload data from database.", 
					agencyId, getDbName());
			map = getMapFromDb();
			webAgency = map.get(agencyId);
		}

		// Return the possibly null web agency
		return webAgency;
	}

	@Override
	public String toString() {
		return "WebAgency [" + "agencyId=" + agencyId + ", hostName="
				+ hostName + "]";
	}

	public String getAgencyId() {
		return agencyId;
	}

	public String getHostName() {
		return hostName;
	}

}
