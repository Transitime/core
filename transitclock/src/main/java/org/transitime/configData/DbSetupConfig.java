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

package org.transitime.configData;

import org.transitime.config.IntegerConfigValue;
import org.transitime.config.StringConfigValue;

/**
 * Config params for database
 *
 * @author SkiBu Smith
 *
 */
public class DbSetupConfig {

	public static String getDbName() {
		return dbName.getValue();
	}
	private static StringConfigValue dbName = 
			new StringConfigValue("transitime.db.dbName", 
					null, // Null as default to use the projectId
					"Specifies the name of the database. If not set then the "
					+ "transitime.core.agencyId will be used.");

	
	public static String getDbHost() {
		return dbHost.getValue();
	}
	private static StringConfigValue dbHost = 
			new StringConfigValue("transitime.db.dbHost", 
					null, // Null as default so can get it from hibernate config
					"Specifies the name of the machine the database for the " +
					"project resides on. Use null value to use values from " +
					"hibernate config file. Set to \"localhost\" if database " +
					"running on same machine as the core application.");
	
	public static String getDbType() {
		return dbType.getValue();
	}
	private static StringConfigValue dbType =
			new StringConfigValue("transitime.db.dbType",
					"mysql",
					"Specifies type of database when creating the URL to "
					+ "connect to the database. Can be mysql or postgresql. "
					+ "Should work for other dbs as well. Default is mysql.");
	
	public static String getDbUserName() {
		return dbUserName.getValue();
	}
	private static StringConfigValue dbUserName = 
			new StringConfigValue("transitime.db.dbUserName", 
					null,
					"Specifies login for the project database. Use null " +
					"value to use values from hibernate config file.");
	
	public static String getDbPassword() {
		return dbPassword.getValue();
	}
	private static StringConfigValue dbPassword = 
			new StringConfigValue("transitime.db.dbPassword", 
					null,
					"Specifies password for the project database. Use null " +
					"value to use values from hibernate config file.",
					false); // Don't log password in configParams log file
	
	public static Integer getSocketTimeoutSec() {
		return socketTimeoutSec.getValue();
	}
	public static IntegerConfigValue socketTimeoutSec =
			new IntegerConfigValue("transitime.db.socketTimeoutSec", 
					60,
					"So can set low-level socket timeout for JDBC connections. "
					+ "Useful for when a session dies during a request, such as "
					+ "for when a db is rebooted. Set to 0 to have no timeout.");
	
	/**
	 * So that have flexibility with where the hibernate config file is.
	 * This way can easily access it within Eclipse.
	 * @return
	 */
	public static String getHibernateConfigFileName() {
		return hibernateConfigFileName.getValue();
	}
	private static StringConfigValue hibernateConfigFileName = 
			new StringConfigValue("transitime.hibernate.configFile", 
					"hsql_hibernate.cfg.xml",
					"Specifies the database dependent hibernate.cfg.xml file "
					+ "to use to configure hibernate. The system will look both "
					+ "on the file system and in the classpath. Can specify "
					+ "mysql_hibernate.cfg.xml or postgres_hibernate.cfg.xml");

    public static Integer getBatchSize() {
        return batchSize.getValue();
    }
    private static IntegerConfigValue batchSize =
            new IntegerConfigValue("transitime.db.batchSize",
                    100,
                    "Specifies the database batch size, defaults to 100");

}
