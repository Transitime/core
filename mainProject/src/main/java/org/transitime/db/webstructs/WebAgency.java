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

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.configData.DbSetupConfig;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.db.structs.Agency;
import org.transitime.ipc.clients.ConfigInterfaceFactory;
import org.transitime.ipc.interfaces.ConfigInterface;
import org.transitime.utils.Encryption;
import org.transitime.utils.IntervalTimer;
import org.transitime.utils.Time;

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

	@Column
	private final boolean active;
	
	@Column(length = 60)
	private final String dbName;
	
	@Column(length = 60)
	private final String dbType;
	
	@Column(length = 120)
	private final String dbHost;
	
	@Column(length = 60)
	private final String dbUserName;
	
	// Passwords should be stored encrypted since multiple people might have
	// access to the database containing the WebAgency objects.
	@Column(length = 60)
	private final String dbEncryptedPassword;

	// For getting GTFS data about agency
	@Transient
	private Agency agency;

	// Cache
	static private Map<String, WebAgency> webAgencyMapCache;
	static private long webAgencyMapCacheReadTime = 0;
	
	private static final Logger logger = LoggerFactory
			.getLogger(WebAgency.class);

	/********************** Member Functions **************************/

	/**
	 * Simple constructor.
	 * 
	 * @param agencyId
	 * @param hostName
	 * @param active
	 * @param dbName
	 * @param dbType
	 * @param dbHost
	 * @param dbUserName
	 * @param dbPassword The non-encrypted password
	 */
	public WebAgency(String agencyId, String hostName, boolean active, String dbName,
			String dbType, String dbHost, String dbUserName, String dbPassword) {
		this.agencyId = agencyId;
		this.hostName = hostName;
		this.active = active;
		this.dbName = dbName;
		this.dbType = dbType;
		this.dbHost = dbHost;
		this.dbUserName = dbUserName;
		this.dbEncryptedPassword = Encryption.encrypt(dbPassword);
	}

	/**
	 * Needed because Hibernate requires no-arg constructor for reading in data
	 */
	@SuppressWarnings("unused")
	private WebAgency() {
		this.agencyId = null;
		this.hostName = null;
		this.active = false;
		this.dbName = null;
		this.dbType = null;
		this.dbHost = null;
		this.dbUserName = null;
		this.dbEncryptedPassword = null;
	}

	/**
	 * Stores this WebAgency object in the specified db.
	 * 
	 * @param dbName
	 *            Name of the db that the WebAgency object is stored in
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
	 * Returns the first (there can be multiple) GTFS agency object for the
	 * specified agencyId. The GTFS agency object is cached. First time it is
	 * accessed it is read from server via RMI.
	 * 
	 * @return The Agency object, or null if can't access the agency via RMI
	 */
	public Agency getAgency() {
		// If agency hasn't been accessed yet do so now...
		if (agency == null) {
			ConfigInterface inter = ConfigInterfaceFactory.get(agencyId);
			
			if (inter == null) {
				logger.error("Could not access via RMI agencyId={}", agencyId);
			} else {
				try {
					// Get the agencies via RMI
					List<Agency> agencies = inter.getAgencies();

					// Use the first agency if there are multiple ones
					agency = agencies.get(0);
				} catch (RemoteException e) {
					logger.error(
							"Could not get Agency object for agencyId={}. {}",
							agencyId, e.getMessage());
				}
			}
		}
		
		// Return the RMI based agency object
		return agency;
	}
	
	/**
	 * Returns the GTFS agency name. It is obtained via RMI as necessary.
	 * 
	 * @return The GTFS agency name
	 */
	public String getAgencyName() {
		Agency agency = getAgency();
		return agency != null ? agency.getName() : null;
	}
	
	/**
	 * Specifies name of database to use for reading in the WebAgency objects.
	 * Currently using the command line option transitime.core.agencyId .
	 * 
	 * @return Name of db to retrieve WebAgency objects from
	 */
	static private String getWebAgencyDbName() {
		return DbSetupConfig.getDbName();
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
		String webAgencyDbName = getWebAgencyDbName();
		logger.info("Reading WebAgencies data from database \"{}\"...", webAgencyDbName);
		IntervalTimer timer = new IntervalTimer();

		Session session = HibernateUtils.getSession(webAgencyDbName);
		try {
			String hql = "FROM WebAgency";
			Query query = session.createQuery(hql);
			@SuppressWarnings("unchecked")
			List<WebAgency> list = query.list();
			Map<String, WebAgency> map = new HashMap<String, WebAgency>();
			for (WebAgency webAgency : list) {
				map.put(webAgency.getAgencyId(), webAgency);
			}

			logger.info("Done reading WebAgencies from database. Took {} msec. "
					+ "They are {}",
					timer.elapsedMsec(), map.values());
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
	 * Returns map of all agencies. Values are cached so won't be automatically
	 * updated when agencies are changed in the database. Will update cache if
	 * haven't done so in 5 minutes. This way will automatically get new
	 * agencies that are added yet will not be reading from the database for
	 * every page hit that needs list of agencies. Also, agencies automatically
	 * removed.
	 * 
	 * @return the cached map of WebAgency objects
	 */
	static private Map<String, WebAgency> getWebAgencyMapCache() {
		// If haven't read in web agencies yet or in a while, do so now
		if (webAgencyMapCache == null
				|| System.currentTimeMillis() - webAgencyMapCacheReadTime > 5 * Time.MS_PER_MIN) {
			webAgencyMapCache = getMapFromDb();
			webAgencyMapCacheReadTime = System.currentTimeMillis();
		}

		return webAgencyMapCache;
	}
	
	/**
	 * Returns collection of all agencies. Values are cached so won't be
	 * automatically updated when agencies are changed in the database. Will
	 * update cache if haven't done so in 5 minutes. This way will automatically
	 * get new agencies that are added yet will not be reading from the database
	 * for every page hit that needs list of agencies.
	 * 
	 * @return Collection of the WebAgency objects configured in db
	 */
	static public Collection<WebAgency> getCachedWebAgencies() {
		return getWebAgencyMapCache().values();
	}
	
	/**
	 * Gets specified WebAgency from the cache. If the agency is not defined in
	 * the cache then will reread the agencies from the database. In this way
	 * can add an agency to the database and the system will automatically pick
	 * up the new agency. 
	 * 
	 * @param agencyId
	 * @return The specified WebAgency, or null if it doesn't exist.
	 */
	static public WebAgency getCachedWebAgency(String agencyId) {
		// Get the web agency from the cache
		WebAgency webAgency = getWebAgencyMapCache().get(agencyId);

		// If web agency was not in cache update the cache and try again
		if (webAgency == null) {
			logger.error("Did not find agencyId={} in WebAgencies table for "
					+ "database {}. Will reload data from database to see if "
					+ "agency only recently added.", 
					agencyId, getWebAgencyDbName());
			webAgencyMapCache = getMapFromDb();
			webAgencyMapCacheReadTime = System.currentTimeMillis();
			webAgency = webAgencyMapCache.get(agencyId);
		}

		// Return the possibly null web agency
		return webAgency;
	}

	/**
	 * Returns collection of all agencies as read from database. No caching is
	 * done. The database is read each time this method is called, so it should
	 * not be used for every API call for example.
	 * 
	 * @return
	 */
	static public Collection<WebAgency> getWebAgencies() {
		return getMapFromDb().values();
	}
	
	@Override
	public String toString() {
		return "WebAgency [" 
				+ "agencyId=" + agencyId 
				+ ", hostName="	+ hostName
				+ ", active=" + active
				+ ", dbName=" + dbName
				+ ", dbType=" + dbType
				+ ", dbHost=" + dbHost
				+ ", dbUserName=" + dbUserName
				+ ", dbEncryptedPassword=" + dbEncryptedPassword
				+ "]";
	}

	public String getAgencyId() {
		return agencyId;
	}

	public String getHostName() {
		return hostName;
	}

	public boolean isActive() {
		return active;
	}

	/**
	 * Returns name of the db for the agency
	 * 
	 * @return
	 */
	public String getDbName() {
		return dbName;
	}
	
	public String getDbType() {
		return dbType;
	}

	public String getDbHost() {
		return dbHost;
	}

	public String getDbUserName() {
		return dbUserName;
	}

	public String getDbEncryptedPassword() {
		return dbEncryptedPassword;
	}

	public String getDbPassword() {
		return Encryption.decrypt(dbEncryptedPassword);
	}
		
	/**
	 * For storing a web agency in the web database
	 * 
	 * @param args
	 *            agencyId = args[0]; hostName = args[1]; dbName = args[2];
	 *            dbType = args[3]; dbHost = args[4]; dbUserName = args[5];
	 *            dbPassword = args[6];
	 */
	public static void main(String args[]) {
		// Determine all the params
		if (args.length <= 5) {
			System.err.println("Specify params for the WebAgency: agencyId = args[0]; hostName = args[1]; dbType = args[2]; dbHost = args[3]; dbUserName = args[4]; dbPassword = args[5];");
			System.exit(-1);
		}
		String agencyId = args[0];
		String hostName = args[1];
		boolean active = true;
		String dbName = args[2];
		String dbType = args[3];
		String dbHost = args[4];
		String dbUserName = args[5];
		String dbPassword = args[6];
		// Name of database where to store the WebAgency object
		String webAgencyDbName = "web";
		
		// Create the WebAgency object
		WebAgency webAgency = new WebAgency(agencyId, hostName, active, dbName,
				dbType, dbHost, dbUserName, dbPassword);
		System.out.println("Storing " + webAgency);
		
		// Store the WebAgency
		webAgency.store(webAgencyDbName);
	}
}
