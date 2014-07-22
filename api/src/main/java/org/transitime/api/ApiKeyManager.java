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

package org.transitime.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.configData.CoreConfig;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.db.webstructs.ApiKey;

/**
 * Manages the ApiKeys. Caches them so API can quickly determine if key is
 * valid.
 *
 * @author SkiBu Smith
 *
 */
public class ApiKeyManager {

    private final Map<String, ApiKey> apiKeyCache;
    
    private static final Logger logger = LoggerFactory
	    .getLogger(ApiKeyManager.class);

    /********************** Member Functions **************************/
    
    public ApiKeyManager() {
	// Create the cache
	apiKeyCache = new HashMap<String, ApiKey>();
	
	// Add all keys to map cache
	for (ApiKey apiKey : getApiKeys()) {
	    apiKeyCache.put(apiKey.getKey(), apiKey);
	}
    }
    
    /**
     * Returns true if key is valid. 
     * 
     * @param key
     * @return
     */
    public boolean isKeyValid(String key) {
	try {
	    // If key is already in cache return true
	    if (apiKeyCache.get(key) != null) 
	        return true;
	    
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
     * Gets the session for db access. The session is specified by parameters in
     * CoreConfig including CoreConfig.getAgencyId() for the name of the
     * database (such as "web") and CoreConfig.getDbHost(),
     * CoreConfig.getDbUserName(), and CoreConfig.getDbPassword(). The db host,
     * user name, and password can also be set in the hibernate.cfg.xml file if
     * the parameter transitime.hibernate.configFile in the CoreConfig is set.
     * 
     * @return
     */
    public static List<ApiKey> getApiKeys() {
	Session session = HibernateUtils.getSession(CoreConfig.getAgencyId());
	return ApiKey.getApiKeys(session);
    }

    private static final String KEY_SALT = "some salt";

    /**
     * Generates the key based on the application name. This isn't intended to
     * be secure so simply use hashCode() to generate a key.
     * 
     * @param applicationName
     * @return
     */
    private static String generateKey(String applicationName) {
	String saltedApplicationName = applicationName + KEY_SALT;
	return Integer.toString(saltedApplicationName.hashCode());
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
     public static ApiKey generateApiKey(String applicationName,
	    String applicationUrl, String email, String phone,
	    String description) 
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
	Session session = HibernateUtils.getSession(CoreConfig.getAgencyId());
	try {
	    Transaction transaction = session.beginTransaction();
	    session.save(newApiKey);
	    transaction.commit();
	} catch (Exception e) {
	    throw e;
	} finally {
	    session.close();
	}

	// Return the new key
	return newApiKey;
    }

     /**
      * For testing and debugging. Currently creates a new key for an application.
      * 
      * @param args
      */
     public static void main(String[] args) {
	if (args.length != 5) {
	    System.err.println("Must supply arguments for applicationName, "
		    + "applicationUrl, email, phone, and description");
	    System.exit(-1);
	}
	ApiKey apiKey = generateApiKey(args[0], args[1], args[2], args[3],
		args[4]);
	System.out.println(apiKey);
	 
//	 try {
//	    ApiKey apiKey = generateApiKey("applicationName",
//	    	    "applicationUrl", "email", "phone",
//	    	    "description");
//	     System.out.println(apiKey);
//	     
//	} catch (IllegalArgumentException e) {
//	    e.printStackTrace();
//	} catch (HibernateException e) {
//	    e.printStackTrace();
//	}
//	 
//	     
//	ApiKeyManager manager = new ApiKeyManager();
//	boolean valid = manager.isKeyValid("1852453479"/* "sldkfj" */);
//	int xx = 9;

     }
}
