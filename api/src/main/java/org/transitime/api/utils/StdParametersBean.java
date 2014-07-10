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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * For getting the standard parameters from the URI used to access the feed.
 * Includes the key, agency, and the media type (JSON or XML). Does not include
 * command specific parameters.
 * 
 * @author SkiBu Smith
 * 
 */
public class StdParametersBean {
    @PathParam("key")
    private String key;
    
    @PathParam("agency")
    private String agencyId;
    
    @QueryParam("format")
    private String formatOverride;
    
    @HeaderParam("accept") 
    String acceptHeader;
    
    @Context 
    HttpServletRequest request;
    
    /********************** Member Functions **************************/

    /**
     * Returns the media type to use for the response based on optional accept
     * header and the optional format specification in the query string of the
     * URL. Setting format in query string overrides what is set in accept
     * header. This way it is always simple to generate a http get for
     * particular format simply by setting query string.
     * <p>
     * If format specification is incorrect then BadRequest
     * WebApplicationException is thrown.
     * <p>
     * The media type is not determined in the constructor because then an
     * exception would cause an ugly error message because it would be handled
     * before the root-resource class get method is being called.
     * 
     * @return The resulting media type
     */
    public String getMediaType() 
	    throws WebApplicationException {
	// Use default of APPLICATION_XML
	String mediaType = MediaType.APPLICATION_XML;
	
	// If mediaType specified in accept header then start with it
	if (acceptHeader != null) {
	    if (acceptHeader.contains(MediaType.APPLICATION_JSON))
		mediaType = MediaType.APPLICATION_JSON;
	    else if (acceptHeader.contains(MediaType.APPLICATION_XML))
		mediaType = MediaType.APPLICATION_XML;
	    else
		throw WebUtils.badRequestException("Accept header \"Accept: " + 
			acceptHeader + "\" is not valid. Must be \"" + 
			MediaType.APPLICATION_JSON + "\" or \"" + 
			MediaType.APPLICATION_XML + "\"");
	}
	
	// If mediaType format is overridden using the query string format
	// parameter then use it.
	if (formatOverride != null) {
	    // Always use lower case
	    formatOverride = formatOverride.toLowerCase();
	        
	    // If mediaType override set properly then use it 
	    if (formatOverride.equals("json"))
		mediaType = MediaType.APPLICATION_JSON;
	    else if (formatOverride.equals("xml"))
		mediaType = MediaType.APPLICATION_XML;
	    else if (formatOverride.equals("human"))
		mediaType = MediaType.TEXT_PLAIN;
	    else
		throw WebUtils.badRequestException("Format \"format=" + 
			formatOverride + "\" from query string not valid. " +
			"Format must be \"json\" or \"xml\"");
	}

	return mediaType;
    }

    /**
     * Simple getter for the key
     * 
     * @return
     */
    public String getKey() {
        return key;
    }

    /**
     * Simple getter for the agency ID
     * 
     * @return
     */
    public String getAgencyId() {
        return agencyId;
    }

    /**
     * Returns the HttpServletRequest.
     * 
     * @return
     */
    public HttpServletRequest getRequest() {
	return request;
    }
    
}

