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

package org.transitime.api.utils;

import javax.ws.rs.WebApplicationException;

import org.transitime.api.ApiKeyManager;

/**
 * For making sure that the application key is valid.
 * <p>
 *
 * @author SkiBu Smith
 *
 */
public class KeyValidator {

    private ApiKeyManager apiKeyManager = new ApiKeyManager();
    
    // This is a singleton class
    private static KeyValidator singleton = new KeyValidator();

    /********************** Member Functions **************************/

    /**
     * Constructor private because singleton class
     */
    private KeyValidator() {}
    
    /**
     * Get singleton instance.
     * 
     * @return
     */
    public static KeyValidator getInstance() {
	return singleton;
    }
    
    /**
     * Returns true if the application key is valid.
     * 
     * @param key
     * @return
     */
    private boolean keyIsValid(String key) {
	return apiKeyManager.isKeyValid(key);
    }
    
    /**
     * Throws exception if the specified application key is not valid.
     * 
     * @param stdParameters
     * @throws WebApplicationException
     */
    public void validateKey(StandardParameters stdParameters) 
	    throws WebApplicationException {
	String key = stdParameters.getKey();
	if (!keyIsValid(key)) {	
        	throw WebUtils.badRequestException("Application key \"" + key +
        		"\" is not valid.");
	}
    }
}
