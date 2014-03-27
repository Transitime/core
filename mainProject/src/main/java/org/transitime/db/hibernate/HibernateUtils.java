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

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.configData.CoreConfig;

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
	
	//public static final 
	
	private static HashMap<String, SessionFactory> sessionFactoryCache =
			new HashMap<String, SessionFactory>();

	public static final Logger logger = LoggerFactory
			.getLogger(HibernateUtils.class);

	/********************** Member Functions **************************/

	/**
	 * Creates a new session factory. This is to be cached and only access
	 * internally since creating one is expensive.
	 * 
	 * @return
	 */
	private static SessionFactory createSessionFactory() 
			throws HibernateException {
		logger.debug("Creating new Hibernate SessionFactory for projectId={}", 
				CoreConfig.getProjectId());
		
		// Create a Hibernate configuration based on the XML file we've put
		// in the standard place
		Configuration config = new Configuration();
		// Want to be able to specify a configuration file for now
		// since developing in Eclipse and want all config files
		// to be in same place. But the Config.configure(String) 
		// method can't seem to work with a Windows directory name such
		// as C:/users/Mike/software/hibernate.cfg.xml . Therefore create
		// a File object for that file name and pass in the File object
		// to configure().
		String fileName = CoreConfig.getHibernateConfigFileName();
		logger.info("Configuring Hibernate for projectId={} using config file={}",
				CoreConfig.getProjectId(), fileName);
		File f = new File(fileName);
		config.configure(f);

		// Add the annotated classes so that they can be used
		AnnotatedClassesList.addAnnotatedClasses(config);

		// Set the db info
		String dbUrl = "jdbc:mysql://" +
				CoreConfig.getDbHost() +
				"/" + CoreConfig.getProjectId();
		config.setProperty("hibernate.connection.url", dbUrl);
		String username = CoreConfig.getDbUserName();
		config.setProperty("hibernate.connection.username", username);
		String password = CoreConfig.getDbPassword();
		config.setProperty("hibernate.connection.password", password);
		
		logger.debug("For Hibernate factory project projectId={} " +
				"using url={} username={}, and configured password",
				CoreConfig.getProjectId(), dbUrl, username);
		
		// Get the session factory for persistence
		Properties properties = config.getProperties();
		ServiceRegistry serviceRegistry = 
				new ServiceRegistryBuilder().applySettings(properties).buildServiceRegistry();
		SessionFactory sessionFactory = config.buildSessionFactory(serviceRegistry);

		// Return the factory
		return sessionFactory;
	}
	
	/**
	 * Returns a cached Hibernate SessionFactory. Returns null if there is
	 * a problem.
	 * 
	 * @param projectId
	 * @return
	 */
	public static SessionFactory getSessionFactory(String projectId) 
			throws HibernateException{
		SessionFactory factory;
		
		synchronized(sessionFactoryCache) {
			factory = sessionFactoryCache.get(projectId);
			// If factory not yet created for this projectId then create it
			if (factory == null) {
				try {
					factory = createSessionFactory();
					sessionFactoryCache.put(projectId, factory);
				} catch (Exception e) {
					logger.error("Could not create SessionFactor for projectId={}", projectId, e);
				}
			}						
		}
		
		return factory;
	}

}
