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
package org.transitime.db.hibernate;

import java.io.ByteArrayOutputStream;


import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.configData.DbSetupConfig;
import org.hibernate.service.ServiceRegistryBuilder;

/**
 * Utilities for dealing with Hibernate issues such as sessions.
 * 
 * @author SkiBu Smith
 *
 */
public class HibernateUtils {

	// Should be set to what is used in hibernate.cfg.xml where the
	// batch_size is set, e.g. <property name="hibernate.jdbc.batch_size">25</property>
	public static final int BATCH_SIZE = 25;
	
	// When Using @Column for route, stop, etc IDs don't need the default of
	// 255 characters. Therefore can use shorter fields. 
	public static final int DEFAULT_ID_SIZE = 60;
	
	// Cache. Keyed on database name
	private static HashMap<String, SessionFactory> sessionFactoryCache =
			new HashMap<String, SessionFactory>();

	public static final Logger logger = 
			LoggerFactory.getLogger(HibernateUtils.class);

	/********************** Member Functions **************************/

	/**
	 * Creates a new session factory. This is to be cached and only access
	 * internally since creating one is expensive.
	 * 
	 * @param dbName
	 * @return
	 */
	private static SessionFactory createSessionFactory(String dbName) 
			throws HibernateException {
		logger.debug("Creating new Hibernate SessionFactory for dbName={}", 
				dbName);
		
		// Create a Hibernate configuration based on customized config file
		Configuration config = new Configuration();
		
		// Want to be able to specify a configuration file for now
		// since developing in Eclipse and want all config files
		// to be in same place. But the Config.configure(String) 
		// method can't seem to work with a Windows directory name such
		// as C:/users/Mike/software/hibernate.cfg.xml . Therefore create
		// a File object for that file name and pass in the File object
		// to configure().
		String fileName = DbSetupConfig.getHibernateConfigFileName();
		logger.info("Configuring Hibernate for dbName={} using config file={}",
				dbName, fileName);
		File f = new File(fileName);
		if (!f.exists()) {
			logger.info("The Hibernate file {} doesn't exist as a regular file "
					+ "so seeing if it is in classpath.", fileName);
			
			// Couldn't find file directly so look in classpath for it
			ClassLoader classLoader = HibernateUtils.class.getClassLoader();
			URL url = classLoader.getResource(fileName);
			if (url != null)
				f = new File(url.getFile());
		}
		if (f.exists())
			config.configure(f);
		else {
			logger.error("Could not load in hibernate config file {}", fileName);
		}
		
		// Add the annotated classes so that they can be used
		AnnotatedClassesList.addAnnotatedClasses(config);

		// Set the db info for the URL, user name, and password. Use values 
		// from CoreConfig if set. If they are not set then the values will be 
		// obtained from the hibernate.cfg.xml 
		// config file.
		String dbUrl = null;
		if (DbSetupConfig.getDbHost() != null) {
			dbUrl = "jdbc:" + DbSetupConfig.getDbType() + "://" +
					DbSetupConfig.getDbHost() +
					"/" + dbName;
			config.setProperty("hibernate.connection.url", dbUrl);			
		} else {
			dbUrl = config.getProperty("hibernate.connection.url");
		}
		
		String dbUserName = DbSetupConfig.getDbUserName();
		if (dbUserName != null) {
			config.setProperty("hibernate.connection.username",	dbUserName);
		} else {
			dbUserName = config.getProperty("hibernate.connection.username");
		}
		
		if (DbSetupConfig.getDbPassword() != null)
			config.setProperty("hibernate.connection.password", 
					DbSetupConfig.getDbPassword());
		
		// Log info, but don't log password. This can just be debug logging
		// even though it is important because the C3P0 connector logs the info.
		logger.debug("For Hibernate factory project dbName={} " +
				"using url={} username={}, and configured password",
				dbName, dbUrl, dbUserName);
		
		// Get the session factory for persistence
		Properties properties = config.getProperties();
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(properties).build();
		SessionFactory sessionFactory = 
				config.buildSessionFactory(serviceRegistry);

		// Return the factory
		return sessionFactory;
	}
	
	/**
	 * Returns a cached Hibernate SessionFactory. Returns null if there is a
	 * problem.
	 * 
	 * @param agencyId
	 *            Used as the database name if the property
	 *            transitime.core.dbName is not set
	 * @return
	 */
	public static SessionFactory getSessionFactory(String agencyId) 
			throws HibernateException{
		// Determine the database name to use. Will usually use the
		// projectId since each project has a database. But this might
		// be overridden by the transitime.core.dbName property.
		String dbName = DbSetupConfig.getDbName();
		if (dbName == null)
			dbName = agencyId;
		
		SessionFactory factory;
		
		synchronized(sessionFactoryCache) {
			factory = sessionFactoryCache.get(dbName);
			// If factory not yet created for this projectId then create it
			if (factory == null) {
				try {
					factory = createSessionFactory(dbName);
					sessionFactoryCache.put(dbName, factory);
				} catch (Exception e) {
					logger.error("Could not create SessionFactory for "
							+ "dbName={}", dbName, e);
					throw e;
				}
			}						
		}
		
		return factory;
	}

	/**
	 * Clears out the session factory so that a new one will be created for the
	 * dbName. This way new db connections are made. This is useful for dealing
	 * with timezones and postgres. For that situation want to be able to read
	 * in timezone from db so can set default timezone. Problem with postgres is
	 * that once a factory is used to generate sessions the database will
	 * continue to use the default timezone that was configured at that time.
	 * This means that future calls to the db will use the wrong timezone!
	 * Through this function one can read in timezone from database, set the
	 * default timezone, clear the factory so that future db connections will
	 * use the newly configured timezone, and then successfully process dates.
	 */
	public static void clearSessionFactory() {
		sessionFactoryCache.clear();
	}
	
	/**
	 * Returns session for the specified agencyId.
	 * <p>
	 * NOTE: Make sure you close the session after the query!! Use a try/catch
	 * around the query and close the session in a finally block to make sure it
	 * happens. The system only gets a limited number of sessions!!
	 * 
	 * @param agencyId
	 *            Used as the database name if the property
	 *            transitime.core.dbName is not set
	 * @return The Session. Make sure you close it when done because system only
	 *         gets limited number of open sessions.
	 * @throws HibernateException
	 */
	public static Session getSession(String agencyId) 
		throws HibernateException {
		SessionFactory sessionFactory = 
				HibernateUtils.getSessionFactory(agencyId);
		Session session = sessionFactory.openSession();
		return session;
	}
	
	/**
	 * Returns the session for the database name specified by the
	 * transitime.db.dbName Java property.
	 * <p>
	 * NOTE: Make sure you close the session after the query!! Use a try/catch
	 * around the query and close the session in a finally block to make sure it
	 * happens. The system only gets a limited number of sessions!!
	 * 
	 * @return The Session. Make sure you close it when done because system only
	 *         gets limited number of open sessions.
	 */
	public static Session getSession() {
		SessionFactory sessionFactory = 
				HibernateUtils.getSessionFactory(DbSetupConfig.getDbName());
		Session session = sessionFactory.openSession();
		return session;
	}
	
	/**
	 * Determines the size of a serializable object by serializing it in memory
	 * and then measuring the resulting size in bytes.
	 * 
	 * @param obj
	 *            Object to be serialized
	 * @return Size of serialized object in bytes or -1 if object cannot be
	 *         serialized
	 */
	public static int sizeof(Object obj) {

	    ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
	    try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);

			objectOutputStream.writeObject(obj);
			objectOutputStream.flush();
			objectOutputStream.close();
		} catch (IOException e) {
			return -1;
		}

	    return byteOutputStream.toByteArray().length;
	}
}
