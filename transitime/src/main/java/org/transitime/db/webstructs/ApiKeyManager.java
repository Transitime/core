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

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.IntegerConfigValue;
import org.transitime.configData.DbSetupConfig;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.db.webstructs.ApiKey;
import org.transitime.utils.Time;

/**
 * Manages the ApiKeys. Caches them so API can quickly determine if key is
 * valid.
 *
 * @author SkiBu Smith
 *
 */
public class ApiKeyManager {

	// Cache of the ApiKeys loaded from database.
	// Map is keyed on the API key.
	private final Map<String, ApiKey> apiKeyCache;

	// Name of the database containing the keys
	private final String dbName;

	// For preventing too frequent db reads
	private long lastTimeKeysReadIntoCache = 0;

	private static IntegerConfigValue lastTimeKeysReadLimitSec = new IntegerConfigValue(
			"transitime.api.apiKeyLastUpdateLimitSec", 3,
			"Amount of time to wait in sec before updating the apiKeyCache");

	// This is a singleton class
	private static ApiKeyManager singleton = new ApiKeyManager();

	private static final Logger logger = LoggerFactory
			.getLogger(ApiKeyManager.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor private because singleton class
	 */
	private ApiKeyManager() {
		// Set the name of the db to get the data from.
		// Use the db name, such as "web".
		dbName = DbSetupConfig.getDbName();

		// Create the cache. Cache will actually be populated when first
		// checking if key is valid. This way don't do a db read at startup.
		apiKeyCache = new HashMap<String, ApiKey>();
	}

	/**
	 * Get singleton instance.
	 * 
	 * @return
	 */
	public static ApiKeyManager getInstance() {
		return singleton;
	}

	/**
	 * Returns true if key is valid. Uses cache of keys so doesn't have to
	 * access database each time. If key not in cache then will reread keys from
	 * database in case it was just added. But won't do so more than every few
	 * seconds since more frequent access could allow an app with a bad key to
	 * cause the db to be queried to often putting an unneeded burden on the db.
	 * <p>
	 * Synchronized because can have simultaneous access and using a cache.
	 * 
	 * @param key
	 *            The key to investigate
	 * @return True if key is valid
	 */
	public synchronized boolean isKeyValid(String key) {
		try {
			// If key is already in cache return true
			if (apiKeyCache.get(key) != null)
				return true;

			// Want to make sure a user doesn't overwhelm the system by
			// repeatedly trying to use an invalid key. So if the cache was
			// just updated a few x seconds ago then don't update it again
			// right now. Simply return false.
			if (System.currentTimeMillis() < lastTimeKeysReadIntoCache
					+ lastTimeKeysReadLimitSec.getValue() * Time.MS_PER_SEC)
				return false;
			lastTimeKeysReadIntoCache = System.currentTimeMillis();

			// Key wasn't in cache so update the cache in case it was added
			apiKeyCache.clear();
			for (ApiKey apiKey : getApiKeys()) {
				apiKeyCache.put(apiKey.getKey(), apiKey);
			}

			return apiKeyCache.get(key) != null;
		} catch (Exception e) {
			logger.error("Problem checking key \"{}\" to see if valid.", key, e);
			return false;
		}
	}

	/**
	 * Gets the API keys from the database. Gets the session for db access. The
	 * session is specified by parameters in CoreConfig including
	 * CoreConfig.getAgencyId() for the name of the database (such as "web") and
	 * CoreConfig.getDbHost(), CoreConfig.getDbUserName(), and
	 * CoreConfig.getDbPassword(). The db host, user name, and password can also
	 * be set in the hibernate.cfg.xml file if the parameter
	 * transitime.hibernate.configFile in the CoreConfig is set.
	 * 
	 * @return
	 */
	public List<ApiKey> getApiKeys() {
		Session session = HibernateUtils.getSession(DbSetupConfig.getDbName());
		return ApiKey.getApiKeys(session);
	}

	private static final String KEY_SALT = "some salt";

	/**
	 * Generates the key based on the application name. This isn't intended to
	 * be secure so simply use hashCode() to generate a key and use hex form of
	 * it for consistency and compactness.
	 * 
	 * @param applicationName
	 * @return
	 */
	private String generateKey(String applicationName) {
		String saltedApplicationName = applicationName + KEY_SALT;
		return Integer.toHexString(saltedApplicationName.hashCode());
	}

	/**
	 * Generates the new ApiKey and stores it in the db.
	 * 
	 * @param applicationName
	 * @param applicationUrl
	 * @param email
	 * @param phone
	 * @param description
	 * @return The new ApiKey or null if there was a problem such as the key
	 * @throws IllegalArgumentException
	 * @throws HibernateException
	 */
	public ApiKey generateApiKey(String applicationName, String applicationUrl,
			String email, String phone, String description)
			throws IllegalArgumentException, HibernateException {
		// Make sure don't already have key for this application name
		List<ApiKey> currentApiKeys = getApiKeys();
		for (ApiKey currentApiKey : currentApiKeys) {
			if (currentApiKey.getApplicationName().equals(applicationName)) {
				// Already have a key for that application so return null
				logger.error("Already have key for application name \"{}\"",
						applicationName);
				throw new IllegalArgumentException("Already have key for "
						+ "application name \"" + applicationName + "\"");
			}
		}

		// Determine what the key should be
		String key = generateKey(applicationName);

		// Create the new ApiKey
		ApiKey newApiKey = new ApiKey(applicationName, key, applicationUrl,
				email, phone, description);

		// Store new ApiKey in database
		newApiKey.storeApiKey(dbName);

		// Return the new key
		return newApiKey;
	}

	/**
	 * Deletes the ApiKey from the database
	 * 
	 * @param key
	 */
	public void deleteKey(String key) {
		List<ApiKey> apiKeys = getApiKeys();
		for (ApiKey apiKey : apiKeys) {
			if (apiKey.getKey().equals(key)) {
				// Found the right key. Delete from database
				apiKey.deleteApiKey(dbName);

				// Also delete key from the cache
				apiKeyCache.remove(key);

				// Found the key so done here
				return;
			}
		}

		// That key not found in database so report error
		logger.error("Could not delete key {} because it was not in database",
				key);
	}

	/**
	 * For testing and debugging. Currently creates a new key for an
	 * application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 5) {
			System.err.println("Must supply arguments for applicationName, "
					+ "applicationUrl, email, phone, and description");
			System.exit(-1);
		}
		ApiKeyManager manager = ApiKeyManager.getInstance();
		ApiKey apiKey = manager.generateApiKey(args[0], args[1], args[2],
				args[3], args[4]);
		System.out.println(apiKey);

		// try {
		// ApiKey apiKey = generateApiKey("applicationName",
		// "applicationUrl", "email", "phone",
		// "description");
		// System.out.println(apiKey);
		//
		// } catch (IllegalArgumentException e) {
		// e.printStackTrace();
		// } catch (HibernateException e) {
		// e.printStackTrace();
		// }
		//
		//
		// ApiKeyManager manager = new ApiKeyManager();
		// boolean valid = manager.isKeyValid("1852453479"/* "sldkfj" */);
		// int xx = 9;

	}
}
