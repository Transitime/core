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
package org.transitime.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copies a file from the specified URL and stores it locally.
 * 
 * @author SkiBu Smith
 *
 */
public class HttpGetFile {

	private static final String USER_AGENT = "Transitime";
	private static final String DEFAULT_FILE_NAME = "DEFAULT_FILE";
	
	private String urlStr;
	private String dirNameForResult;
	private String fullFileNameForResult;
	
	protected static final Logger logger = LoggerFactory
			.getLogger(HttpGetFile.class);

	/********************** Member Functions **************************/

	public HttpGetFile(String urlStr, String dirName) {
		this.urlStr = urlStr;
		this.dirNameForResult = dirName;
		
		// Make sure dir name ends with a "/"
		if (!dirNameForResult.endsWith("/"))
			dirNameForResult += "/";
		
		fullFileNameForResult = dirNameForResult + getFileNameFromUrl(urlStr);
	}
	
	/**
	 * Simply a getter. Returns the full name where the file is stored. 
	 * @return
	 */
	public String getFullFileName() {
		return fullFileNameForResult;
	}
	
	/**
	 * Gets the file name from the URL so it can be used as part
	 * of the file name for storing the results.
	 * @param urlStr
	 * @return the file name to use for storing the results
	 */
	private static String getFileNameFromUrl(String urlStr) {
		int lastSlashPos = urlStr.lastIndexOf('/');
		if (lastSlashPos == -1) {
			logger.error("Couldn't determine file name so using {}", 
					DEFAULT_FILE_NAME);
			return DEFAULT_FILE_NAME;
		}
		
		return urlStr.substring(lastSlashPos + 1);
	}
	
	public void getFile() throws IOException {
		IntervalTimer timer = new IntervalTimer();
		
		logger.info("Getting URL={}", urlStr);
		URL url = new URL(urlStr);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("User-Agency", USER_AGENT);
		
		// Get and log response code
		int expectedContentLength = connection.getContentLength();
		logger.info("Response code for getting file is {} and file size is {} bytes", 
				connection.getResponseCode(),
				expectedContentLength);

		// Open input stream for reading data
		InputStream in = connection.getInputStream();
		
		// Open file for where results are to be written
		File file = new File(fullFileNameForResult);
		// Make sure output directory exists
		file.getParentFile().mkdirs();
		// Open the stream
		FileOutputStream fos = new FileOutputStream(file);
		
		// Copy contents to file
		byte[] buffer = new byte[4096];
		int length;
		int totalLength = 0;
		while ((length = in.read(buffer)) > 0) {
			fos.write(buffer, 0, length);
			totalLength += length;
		}
		
		// Close things up
		fos.close();
		
		if (totalLength == expectedContentLength)
			logger.info("Successfully copied file to {}. Length was {} bytes. Took {} msec.", 
				fullFileNameForResult, totalLength, timer.elapsedMsec());
		else
			logger.error("When copying file {} the expected length was {} but only copied {} bytes",
					fullFileNameForResult, expectedContentLength, totalLength);
	}
		
}
