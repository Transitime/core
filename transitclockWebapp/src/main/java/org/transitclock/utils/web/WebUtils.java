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

package org.transitclock.utils.web;

import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.webstructs.WebAgency;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class WebUtils {

	private static final String EMPTY_STRING = "";

	private static StringConfigValue apiHostname =
			new StringConfigValue("transitime.web.apiHostname",
					"localhost",
					"The DNS of the api tier.  The default is typical");

	protected static String getApiHostname() {
		return apiHostname.getValue();
	}

	private static IntegerConfigValue apiPort =
			new IntegerConfigValue("transitime.web.apiPort",
					8080,
					"The port of the api tier.  The default is typical");


	private static StringConfigValue headerBrandingText =
			new StringConfigValue(
					"transitclock.web.headerBrandingText",
					"The",
					"Optional branding text included in the Header of the Web application");

	public static String getHeaderBrandingText() {
		return headerBrandingText.getValue();
	}

	private static BooleanConfigValue showApiKey =
					new BooleanConfigValue(
									"transitclock.web.showApiKey",
									true,
									"Option to hide the API Key from the UI. Please note API Key information can still be derived" +
													"by monitoring network calls.");

	public static Boolean showApiKey() {
		return showApiKey.getValue();
	}

	public static StringConfigValue apiKey =
				new StringConfigValue(
					"transitclock.apikey",
								EMPTY_STRING,
									"API Key that the web service will use to make calls to the API");

	public static String getApiKey() {
		return apiKey.getValue();
	}

	public static String getShowableApiKey(){
		if(showApiKey()){
			return getApiKey();
		}
		return EMPTY_STRING;
	}

	protected static Integer getApiPort() {
		return apiPort.getValue();
	}

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

    public static StringBuffer getApiRequest(String command, Map<String, String> properties) throws IOException {
    	String url = "http://" + getApiHostname() + ":" + getApiPort() +  "/api/v1/key/"
				+ System.getProperty("transitime.apikey")
				+ "/agency/"
				+ WebAgency.getCachedOrderedListOfWebAgencies().get(0).getAgencyId()
				+ command;

    	int propertyCount = 0;
    	if (properties != null && !properties.isEmpty()) {
    	    for (String key : properties.keySet()) {
    	        if (propertyCount == 0) {
    	            url += "?" + key + "=" + properties.get(key);
                } else {
    	            url += "&" + key + "=" + properties.get(key);
                }
                propertyCount++;
            }
        }


    	URLConnection connection = new URL(url).openConnection();
    	connection.setReadTimeout(60 * 1000);
    	connection.setConnectTimeout(60 * 1000);
    	connection.setUseCaches(false);
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    	StringBuffer out = new StringBuffer();

    	String line = "";
    	while ((line = reader.readLine()) != null) {
    		out.append(line);
		}

		return out;
	}
}
