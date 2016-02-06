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

package org.transitime.utils.web;

import javax.servlet.http.HttpServletRequest;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class WebUtils {

    /********************** Member Functions **************************/

    /**
     * Goes through all the request parameters, such as from the query string,
     * and puts them into a String version of a JSON set of key values. This
     * string can be used as the data parameter for a JQuery AJAX call to
     * forward all parameters to the page being requested via AJAX.
     * 
     * @param request
     * @return The parameters to be used as data for an AJAX call
     */
    public static String getAjaxDataString(HttpServletRequest request) {
	String queryStringParams = "";
	java.util.Map<String, String[]> paramsMap = request.getParameterMap();
	boolean firstParam = true;
	for (String paramName : paramsMap.keySet()) {
		if (!firstParam)
		    queryStringParams += ", ";
		firstParam = false;

		queryStringParams += paramName + ":[";
		String paramValues[] = paramsMap.get(paramName);
		boolean firstValue = true;
	    for (String paramValue : paramValues) {
			if (!firstValue)
			    queryStringParams += ", ";
			firstValue = false;

	    	queryStringParams += "\"" + paramValue + "\"";
	    }
	    queryStringParams += "]";
	}

	return queryStringParams;
    }
    
    public static String getQueryParamsString(HttpServletRequest request) {
		String queryStringParams = "";
		java.util.Map<String, String[]> paramsMap = request.getParameterMap();
		boolean firstParam = true;
		for (String paramName : paramsMap.keySet()) {
			if (!firstParam)
			    queryStringParams += "&";
			firstParam = false;
	
			queryStringParams += paramName + "=";
			String paramValues[] = paramsMap.get(paramName);
			boolean firstValue = true;
		    for (String paramValue : paramValues) {
				if (!firstValue)
				    queryStringParams += ",";
				firstValue = false;
	
		    	queryStringParams += paramValue;
		    }
		}

	return queryStringParams;
    }

}
