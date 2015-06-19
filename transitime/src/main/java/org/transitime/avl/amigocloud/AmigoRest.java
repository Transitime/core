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
package org.transitime.avl.amigocloud;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For getting the results of a http request.
 * 
 * @author AmigoCloud
 *
 */
@SuppressWarnings("deprecation")
public class AmigoRest {
	private static final Logger logger = 
			LoggerFactory.getLogger(AmigoRest.class);

	/********************** Member Functions **************************/
	
	private static DefaultHttpClient getThreadSafeClient() {
		DefaultHttpClient client = new DefaultHttpClient();
		ClientConnectionManager mgr = client.getConnectionManager();
		HttpParams params = client.getParams();
		client =
				new DefaultHttpClient(new ThreadSafeClientConnManager(params,
						mgr.getSchemeRegistry()), params);
		return client;
	}

	private HttpClient httpclient = getThreadSafeClient();

	private String apiToken;

	/**
	 * Constructor
	 * 
	 * @param token
	 */
	public AmigoRest(String token) {
		apiToken = token;
	}

	/**
	 * Gets result of HTTP request to the URL and returns it as a string.
	 * 
	 * @param url
	 * @return
	 */
	public String get(String url) {
		String uri;
		uri = url + "?token=" + apiToken;
		HttpGet httpget = new HttpGet(uri);
		try {
			HttpResponse response = httpclient.execute(httpget);

			BufferedReader rd = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent()));

			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Queried url \"{}\" and it returned \"{}|\"", 
						url, result.toString());
			}
			return result.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
